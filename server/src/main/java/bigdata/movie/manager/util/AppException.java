package bigdata.movie.manager.util;

public class AppException extends Exception {
    private final int code;
    private final String msg;

    public AppException(int code, String msg) {
        super(String.format("%d:%s",code, msg));
        this.code = code;
        this.msg = msg;
    }

    public AppException(int code, String msg, Throwable e) {
        super(String.format("%d:%s",code, msg), e);
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
