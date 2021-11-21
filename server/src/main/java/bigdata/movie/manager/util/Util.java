package bigdata.movie.manager.util;

import bigdata.movie.manager.enums.LoginState;
import com.mysql.cj.log.Log;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

public class Util {
    public static final int NOT_LOGIN = -1;
    public static final int UNKNOWN_USER = -2;
    public static final int INCORRECT_PASSWORD = -3;
    public static final int NOT_ADMIN = -4;
    public static final int QUERY_SUBMIT_ERROR = -10;
    public static final int QUERY_EXEC_ERROR = -10;
    public static final int UNKNOWN = -100;

    public static String md5(String text) {
        return new String(DigestUtils.md5Digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    public static void assertLogin() throws AppException {
        if(loginState() == LoginState.not_login) {
            throw new AppException(NOT_LOGIN, "require login");
        }
    }

    public static void assertAdmin() throws AppException {
        if(loginState() == LoginState.admin_login) {
            throw new AppException(NOT_LOGIN, "require login");
        }
    }

    public static String currentUser() throws AppException {
        HttpServletRequest request =((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        for (Cookie cookie : request.getCookies()) {
            if(cookie.getName().equals("name")) {
                return cookie.getValue();
            }
        }
        throw new AppException(NOT_LOGIN, "require login");
    }

    public static LoginState loginState() {
        HttpServletRequest request =((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            return LoginState.not_login;
        }
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("isAdmin")) {
                if(Boolean.getBoolean(cookie.getValue())) {
                    return LoginState.admin_login;
                } else {
                    return LoginState.normal_login;
                }
            }
        }
        return LoginState.not_login;
    }
}
