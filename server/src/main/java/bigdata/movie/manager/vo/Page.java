package bigdata.movie.manager.vo;

import lombok.Data;

import java.util.List;

@Data
public class Page<T> {
    private List<T> items;
    private int pageNo;
    private int pageSize;
    private int total;
}
