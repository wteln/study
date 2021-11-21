package bigdata.movie.manager.service.impl;

import bigdata.movie.manager.dao.MovieMapper;
import bigdata.movie.common.entity.Movie;
import bigdata.movie.manager.service.MovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements MovieService {
}
