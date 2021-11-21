package bigdata.movie.common.entity;

import lombok.Data;

@Data
public class Tag {
  private Long userId;
  private Long movieId;
  private String tag;
  private Long ts;
}
