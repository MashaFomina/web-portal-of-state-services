package portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.util.Locale;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
@ImportResource("classpath:appconfig-root.xml")
public class Application {
    public static void main(String[] args) {
        /*ApplicationContext context
                = new ClassPathXmlApplicationContext("locale.xml");

        String name = context.getMessage("NotEmpty",
                new Object[] { 28,"" }, Locale.ENGLISH);

        System.out.println("Not empty: " + name);*/

        SpringApplication.run(Application.class, args);
    }
}
