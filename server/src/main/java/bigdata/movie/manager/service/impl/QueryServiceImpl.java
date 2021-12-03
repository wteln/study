package bigdata.movie.manager.service.impl;

import bigdata.movie.manager.vo.MovieDetail;
import bigdata.movie.common.entity.Query;
import bigdata.movie.manager.service.QueryService;
import bigdata.movie.manager.util.AppException;
import bigdata.movie.manager.util.Util;
import bigdata.movie.manager.vo.Page;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import org.apache.hadoop.fs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
@Service
public class QueryServiceImpl implements QueryService {
  private static final Logger logger = LoggerFactory.getLogger(QueryService.class);

  private final String sparkHome;
  private final String sparkMaster;
  private final String taskJarPath;
  private final String resultBase;
  private final FileSystem fs;
  private final Map<String, QueryContext> queries;

  @Autowired
  public QueryServiceImpl(@Value("${spark.home}") String sparkHome,
                          @Value("${spark.master}") String sparkMaster,
                          @Value("${spark.task-jar}") String taskJarPath,
                          @Value("${spark.result-base}") String resultBase, FileSystem fs) {
    this.sparkHome = sparkHome;
    this.sparkMaster = sparkMaster;
    this.taskJarPath = taskJarPath;
    this.resultBase = resultBase;
    this.fs = fs;
    queries = new ConcurrentHashMap<>();
  }

  public String submitQuery(Query query) throws AppException {
    logger.info("begin to exec query {}", query);
    long qid = System.currentTimeMillis();
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("%s/bin/spark-submit --class bigdata.movie.task.Filter --master %s %s " +
            "--output %s/filter/%d.csv --minRate %.1f --maxRate %.1f",
        sparkHome, sparkMaster, taskJarPath, resultBase, qid, query.getMinRate(), query.getMaxRate()));
    if (!Strings.isNullOrEmpty(query.getName())) {
      builder.append(String.format(" --name %s", query.getName()));
    }
    if (!Strings.isNullOrEmpty(query.getCategory())) {
      builder.append(String.format(" --category %s", query.getCategory()));
    }
    if (!Strings.isNullOrEmpty(query.getTag())) {
      builder.append(String.format(" --tag %s", query.getTag()));
    }
    String command = builder.toString();
    logger.info("exec command: " + command);

    try {
      Process process = Runtime.getRuntime().exec(command);
      queries.put(qid + "", new QueryContext(process));
      TimeUnit.SECONDS.sleep(1);
      if (!process.isAlive()) {
        int status = process.exitValue();
        if (status != 0) {
          throw new AppException(Util.QUERY_SUBMIT_ERROR, "failed to submit query");
        }
      }
      return qid + "";
    } catch (IOException | InterruptedException e) {
      throw new AppException(Util.QUERY_SUBMIT_ERROR, "failed to submit query", e);
    }
  }

  @Override
  public String getQueryStatus(String queryId) throws AppException {
    // 先检查子进程
    QueryContext context = queries.get(queryId);
    if (context == null) {
      throw new AppException(Util.QUERY_EXEC_ERROR, "context lost");
    }
    context.touch();
    if (context.process.isAlive()) {
      logger.info("process for {} is running", queryId);
      return null;
    }
    int status = context.process.exitValue();
    if (status != 0) {
      try {
        String error = CharStreams.toString(new InputStreamReader(context.process.getErrorStream()));
        throw new AppException(Util.QUERY_EXEC_ERROR, error);
      } catch (IOException e) {
        throw new AppException(Util.QUERY_EXEC_ERROR, "exec query error", e);
      }
    }

    // 再检查hdfs文件是否写成功
    try {
      Path path = new Path(String.format("%s/filter/%s.csv", resultBase, queryId));
      Path successPath = new Path(path, "_SUCCESS");
      logger.info("check path {}", successPath);
      if (fs.exists(path) && fs.exists(successPath)) {
        return path.toString();
      } else {
        logger.info("data is not ready for {}", queryId);
        return null;
      }
    } catch (IOException e) {
      throw new AppException(Util.QUERY_EXEC_ERROR, "generate result error", e);
    }
  }

  @Override
  public Page<MovieDetail> readQueryResult(String queryId, int pageNo, int pageSize) throws AppException {
    QueryContext context = queries.get(queryId);
    if (context == null) {
      throw new AppException(Util.QUERY_EXEC_ERROR, "context lost");
    }

    Path path = new Path(String.format("%s/filter/%s.csv", resultBase, queryId));
    List<Path> files = new ArrayList<>();
    try {
      RemoteIterator<LocatedFileStatus> fileStatues = fs.listFiles(path, false);
      while (fileStatues.hasNext()) {
        files.add(fileStatues.next().getPath());
      }
    } catch (IOException e) {
      throw new AppException(Util.QUERY_EXEC_ERROR, "list file error");
    }

    for (Path file : files) {
      try (FSDataInputStream fos = fs.open(file);
           BufferedReader br = new BufferedReader(new InputStreamReader(fos))) {
        String line;
        while ((line = br.readLine()) != null) {
          String[] sps = line.split(",");
          MovieDetail detail = MovieDetail.builder()
              .id(Long.parseLong(sps[0]))
              .title(sps[1])
              .genres(sps[2])
              .rate(sps[3])
              .tag(sps[4])
              .build();
          context.cache.add(detail);
        }
      } catch (IOException e) {
        throw new AppException(Util.QUERY_EXEC_ERROR, "fetch result error", e);
      }
    }
    Page<MovieDetail> page = new Page<>();
    page.setTotal(context.cache.size());
    page.setPageNo(pageNo);
    page.setPageSize(pageSize);

    page.setItems(context.cache.subList((pageNo - 1) * pageSize, Math.min(pageNo * pageSize, context.cache.size())));
    return page;
  }

  private static class QueryContext {
    final Process process;
    final List<MovieDetail> cache;
    long expireTime;

    private QueryContext(Process process) {
      this.process = process;
      cache = new ArrayList<>();
      touch();
    }

    void touch() {
      expireTime = System.currentTimeMillis() + 30 * 60 * 1000; // 30min
    }
  }
}
