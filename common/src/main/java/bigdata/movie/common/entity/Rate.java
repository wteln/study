package bigdata.movie.common.entity;

import lombok.Data;

@Data
public class Rate {
  private Long userId;
  private Long movieId;
  private Double rate;
  private Long ts;
}
