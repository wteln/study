package bigdata.movie.manager.controller;

import bigdata.movie.common.entity.Movie;
import bigdata.movie.common.entity.ResponseEntity;
import bigdata.movie.manager.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class InitController {
    private static final Logger logger = LoggerFactory.getLogger(InitController.class);
    private final MovieService movieService;
    private final String path;

    @Autowired
    public InitController(MovieService movieService,
                          @Value("{init.path:}") String path) {
        this.movieService = movieService;
        this.path = path;
    }

    @GetMapping("/init/movie")
    public ResponseEntity<Object> load() throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(new File(path, "movies.csv")))) {
            String line = br.readLine();
            if(line == null) {
                throw new IOException("empty file");
            }
            List<Movie> movieList = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] sps = line.split(",");
                Movie movie = new Movie();
                movie.setId(Long.parseLong(sps[0]));
                movie.setTitle(sps[1]);
                movie.setGenres(sps[2]);
                movieList.add(movie);
            }
            movieService.saveOrUpdateBatch(movieList);
        }
        return ResponseEntity.success(null);
    }
}
