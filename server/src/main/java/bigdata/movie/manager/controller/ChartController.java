package bigdata.movie.manager.controller;

import bigdata.movie.common.entity.ResponseEntity;
import bigdata.movie.manager.service.ChartService;
import bigdata.movie.manager.util.AppException;
import bigdata.movie.manager.vo.ChartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
public class ChartController {
  private final ChartService chartService;

  @Autowired
  public ChartController(ChartService chartService) {
    this.chartService = chartService;
  }

  @GetMapping("/charts")
  public ResponseEntity<ChartVo> getData(@RequestParam("type") String type) throws AppException {
    ChartVo origin = chartService.getChart(type);
    List<Object[]> tuples = new ArrayList<>(origin.getXs().size());
    for (int i = 0; i < origin.getXs().size(); i++) {
      tuples.add(new Object[]{origin.getXs().get(i), origin.getYs().get(i)});
    }
    tuples.sort(Comparator.comparing(o -> ((String) o[0])));
    List<String> xs = new ArrayList<>(tuples.size());
    List<Long> ys = new ArrayList<>(tuples.size());
    tuples.forEach(a ->{
      xs.add((String) a[0]);
      ys.add((Long) a[1]);
    });
    return ResponseEntity.success(new ChartVo(xs,ys));
  }
}
