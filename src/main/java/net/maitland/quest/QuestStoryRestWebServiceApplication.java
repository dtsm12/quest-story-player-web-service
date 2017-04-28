package net.maitland.quest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by David on 04/04/2017.
 */
@SpringBootApplication
public class QuestStoryRestWebServiceApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(QuestStoryRestWebServiceApplication.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(QuestStoryRestWebServiceApplication.class, args);
    }
}
