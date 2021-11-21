package bigdata.movie.manager.controller;

import bigdata.movie.common.entity.ResponseEntity;
import bigdata.movie.manager.util.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    //这个注解是指当controller中抛出这个指定的异常类的时候，都会转到这个方法中来处理异常
    @ExceptionHandler(Exception.class)
    //将返回的值转成json格式的数据
    @ResponseBody
    //返回的状态码
    @ResponseStatus(value= HttpStatus.OK)     //服务内部错误
    public ResponseEntity<Object> handlerUserNotExistException(Exception ex) throws Exception {
        logger.error("catch exception: ", ex);
        Throwable t = ex;
        while (t.getClass() != AppException.class && t.getCause() != null) {
            t = t.getCause();
        }
        if(t.getCause() == null) {
            throw ex;
        }
        return ResponseEntity.fail(((AppException) t).getCode(),((AppException) t).getMsg());
    }
}
