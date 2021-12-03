package bigdata.movie.manager.service;

import bigdata.movie.manager.util.AppException;
import bigdata.movie.manager.vo.ChartVo;

public interface ChartService {
  ChartVo getChart(String type) throws AppException;
}
