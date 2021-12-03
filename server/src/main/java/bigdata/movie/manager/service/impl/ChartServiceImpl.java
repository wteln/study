package bigdata.movie.manager.service.impl;

import bigdata.movie.manager.service.ChartService;
import bigdata.movie.manager.util.AppException;
import bigdata.movie.manager.util.Util;
import bigdata.movie.manager.vo.ChartVo;
import bigdata.movie.manager.vo.MovieDetail;
import org.apache.hadoop.fs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChartServiceImpl implements ChartService {
  private final String resultBase;
  private final FileSystem fs;

  @Autowired
  public ChartServiceImpl(@Value("${spark.result-base}") String resultBase, FileSystem fs) {
    this.resultBase = resultBase;
    this.fs = fs;
  }

  @Override
  public ChartVo getChart(String type) throws AppException {
    Path path = new Path(String.format("%s/charts/%s.csv", resultBase, type));
    List<Path> files = new ArrayList<>();
    try {
      RemoteIterator<LocatedFileStatus> fileStatues = fs.listFiles(path, false);
      while (fileStatues.hasNext()) {
        files.add(fileStatues.next().getPath());
      }
    } catch (IOException e) {
      throw new AppException(Util.QUERY_EXEC_ERROR, "list file error");
    }

    List<String> keys = new ArrayList<>();
    List<Long> values = new ArrayList<>();
    for (Path file : files) {
      try (FSDataInputStream fos = fs.open(file);
           BufferedReader br = new BufferedReader(new InputStreamReader(fos))) {
        String line;
        while ((line = br.readLine()) != null) {
          String[] sps = line.split(">>>>>");
          keys.add(sps[0]);
          values.add(Long.parseLong(sps[1].trim()));
        }
      } catch (IOException e) {
        throw new AppException(Util.QUERY_EXEC_ERROR, "fetch result error", e);
      }
    }
    return new ChartVo(keys, values);
  }
}
