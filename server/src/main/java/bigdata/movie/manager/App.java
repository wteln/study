package bigdata.movie.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;

@SpringBootApplication(exclude = {
    GsonAutoConfiguration.class
})
public class App {
    public static void main(String[] args) {
        try {
            SpringApplication.run(App.class, args);
        }catch (Throwable e) {
            e.printStackTrace();
        }
    }
}