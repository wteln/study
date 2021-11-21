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
    userService.updateById(user);
    return ResponseEntity.success(user);
  }

  @SuppressWarnings("unchecked")
  @GetMapping("/users")
  public ResponseEntity<Page<User>> listUser(@RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                                             @RequestParam(value = "pageSize", defaultValue = "50") int pageSize)
      throws AppException {
    int count = userService.count(new QueryWrapper<User>().lambda()
        .ne(User::isDel, true)
        .orderByDesc(User::getUsername)
    );
    List<User> users = userService.list(new QueryWrapper<User>().lambda()
        .ne(User::isDel, true)
        .orderByDesc(User::getUsername)
        .last(String.format("limit %d, %d", (pageNo - 1) * pageSize, pageSize))
    );
    Page<User> page = new Page<>();
    page.setTotal(count);
    page.setItems(users);
    page.setPageNo(pageNo);
    page.setPageSize(pageSize);
    return ResponseEntity.success(page);
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<Object> deleteUser(@PathVariable("id") long id)
      throws AppException {
    userService.removeById(id);
    return ResponseEntity.success(null);
  }


}
