package de.sranko_informatik.si_excel_to_json_jar_gui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class GuiApplication {

    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(GuiApplication.class);
        application.setAdditionalProfiles("ssl");
        application.run(args);

    }

}