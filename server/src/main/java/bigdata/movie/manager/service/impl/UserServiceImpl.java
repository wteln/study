package bigdata.movie.manager.service.impl;

import bigdata.movie.manager.dao.UserMapper;
import bigdata.movie.common.entity.User;
import bigdata.movie.manager.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
