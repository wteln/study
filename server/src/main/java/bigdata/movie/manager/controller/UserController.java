package bigdata.movie.manager.controller;

import bigdata.movie.common.entity.ResponseEntity;
import bigdata.movie.common.entity.User;
import bigdata.movie.manager.service.UserService;
import bigdata.movie.manager.util.AppException;
import bigdata.movie.manager.util.Util;
import bigdata.movie.manager.vo.LoginVo;
import bigdata.movie.manager.vo.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/users/login")
  public ResponseEntity<User> login(HttpServletResponse response, @RequestBody LoginVo loginVo) {
    String pwd = Util.md5(loginVo.getPassword());
    User user = userService.getOne(new QueryWrapper<User>()
        .lambda()
        .eq(User::getUsername, loginVo.getUsername())
        .ne(User::isDel, true)
    );
    if (user == null) {
      return ResponseEntity.fail(Util.UNKNOWN_USER, "unknown user");
    }
    if (user.getPassword().equals(pwd)) {
      //登录成功
      Cookie cookie = new Cookie("isAdmin", user.isAdmin() + "");
      cookie.setSecure(true);
      cookie.setHttpOnly(false);
      response.addCookie(cookie);
      response.addCookie(new Cookie("name", user.getUsername()));
      return ResponseEntity.success(user);
    } else {
      return ResponseEntity.fail(Util.INCORRECT_PASSWORD, "incorrect password");
    }
  }

  @PostMapping("/users")
  public ResponseEntity<User> createUser(@RequestBody User user) throws AppException {
    user.setPassword(Util.md5(user.getPassword()));
    userService.save(user);
    return ResponseEntity.success(user);
  }

  @PutMapping("/users")
  public ResponseEntity<User> updateUser(@RequestBody User user) throws AppException {
    String name = Util.currentUser();
    if (!name.equals(user.getUsername())) {
      throw new AppException(Util.UNKNOWN_USER, "no privilege");
    }
    userService.updateById(user);
    return ResponseEntity.success(user);
  }

  @GetMapping("/users")
  public ResponseEntity<Page<User>> listUser(@RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                                                   @RequestParam(value = "pageSize", defaultValue = "50") int pageSize,
                                                   @RequestParam(value = "userId") long uid)
      throws AppException {
    User user = userService.getById(uid);
    int count = userService.count(new QueryWrapper<User>().lambda()
        .eq(user.isAdmin(), User::getId, uid)
        .ne(User::isDel, true));
    List<User> users = userService.list(new QueryWrapper<User>().lambda()
            .eq(user.isAdmin(), User::getId, uid)
            .ne(User::isDel, true)
            .last(String.format("limit %d, %d", (pageNo - 1) * pageSize, pageSize))
        );
    Page<User> page = new Page<>();
    page.setTotal(count);
    page.setItems(users);
    page.setPageNo(pageNo);
    page.setPageSize(pageSize);
    return ResponseEntity.success(page);
  }
}
