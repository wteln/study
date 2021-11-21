package bigdata.movie.manager.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDetail {
  private Long id;
  private String title;
  private String genres;
  private String rate;
  private String tag;
}
