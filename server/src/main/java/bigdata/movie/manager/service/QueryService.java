package bigdata.movie.manager.service;

import bigdata.movie.manager.vo.MovieDetail;
import bigdata.movie.common.entity.Query;
import bigdata.movie.manager.util.AppException;
import bigdata.movie.manager.vo.Page;

public interface QueryService {
  String submitQuery(Query query) throws AppException;
  String getQueryStatus(String queryId) throws AppException;
  Page<MovieDetail> readQueryResult(String queryId, int pageNo, int pageSize) throws AppException;
}
