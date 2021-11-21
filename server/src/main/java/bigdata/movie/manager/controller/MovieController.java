package bigdata.movie.manager.controller;

import bigdata.movie.common.entity.Movie;
import bigdata.movie.common.entity.ResponseEntity;
import bigdata.movie.manager.service.MovieService;
import bigdata.movie.manager.util.AppException;
import bigdata.movie.manager.util.Util;
import bigdata.movie.manager.vo.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movies")
    public ResponseEntity<Page<Movie>> listMovies(@RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                                                  @RequestParam(value = "pageSize", defaultValue = "50") int pageSize) {
        List<Movie> movies = movieService.list(new QueryWrapper<Movie>().lambda()
                .last(String.format("limit %d, %d", (pageNo-1)*pageSize, pageSize))
        );
        Page<Movie> page = new Page<>();
        page.setItems(movies);
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        page.setTotal(movieService.count());
        return ResponseEntity.success(page);
    }

    @PostMapping("/movies")
    public ResponseEntity<Movie> createMovie(Movie movie) throws AppException {
        Util.assertAdmin();
        movieService.save(movie);
        return ResponseEntity.success(movie);
    }

    @PutMapping("/movies")
    public ResponseEntity<Movie> updateMovie(Movie movie) throws AppException {
        Util.assertAdmin();
        movieService.updateById(movie);
        return ResponseEntity.success(movie);
    }
}
