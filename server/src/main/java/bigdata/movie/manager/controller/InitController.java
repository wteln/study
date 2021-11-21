package bigdata.movie.manager.controller;

import bigdata.movie.common.entity.Movie;
import bigdata.movie.common.entity.ResponseEntity;
import bigdata.movie.common.entity.User;
import bigdata.movie.common.enums.Age;
import bigdata.movie.common.enums.Gender;
import bigdata.movie.manager.service.MovieService;
import bigdata.movie.manager.service.UserService;
import bigdata.movie.manager.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class InitController {
    private static final Logger logger = LoggerFactory.getLogger(InitController.class);
    private final MovieService movieService;
    private final UserService userService;
    private final String path;

    @Autowired
    public InitController(MovieService movieService,
                          UserService userService,
                          @Value("${init.path:}") String path) {
        this.movieService = movieService;
        this.userService = userService;
        this.path = path;
    }

    @GetMapping("/init/movie")
    public ResponseEntity<Object> load(@RequestParam("type") String type) throws Exception {
        switch (type) {
            case "movie":
                loadMovie();
                break;
            case "user":
                loadUser();
                break;
            default:
                throw new UnsupportedOperationException("unsupported type " + type);
        }
        return ResponseEntity.success(null);
    }

    private void loadMovie() throws Exception {
        load("movies.csv", line -> {
            String[] sps = line.split(",");
            Movie movie = new Movie();
            movie.setId(Long.parseLong(sps[0]));
            movie.setTitle(sps[1]);
            movie.setGenres(sps[2]);
            return movie;
        }, movieService::saveOrUpdateBatch);
    }

    private void loadUser() throws Exception {
        User user = new User();
        user.setUsername("admin");
        user.setPassword(Util.md5("admin123"));
        userService.save(user);

        Map<String, Gender> map = new HashMap<>();
        map.put("M", Gender.male);
        map.put("F", Gender.female);
        load("users.dat", line -> {
            String[] sps = line.split("::");
            return User.builder()
                .id(Long.parseLong(sps[0]))
                .gender(map.get(sps[1]))
                .age(Integer.parseInt(sps[2]))
                .occupation(sps[3])
                .zipCode(sps[4])
                .build();
        }, userService::saveOrUpdateBatch);
    }

    private <T> void load(String name, Converter<T> converter, Save<T> saver) throws Exception {
        try(BufferedReader br = new BufferedReader(new FileReader(new File(path, name)))) {
            String line = br.readLine();
            if(line == null) {
                throw new IOException("empty file");
            }
            List<T> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                T item = converter.convert(line);
                list.add(item);
            }
            saver.save(list);
        }
    }

    private interface Converter<T> {
        T convert(String line) throws Exception;
    }

    private interface Save<T> {
        void save(List<T> items) throws Exception;
    }
}
