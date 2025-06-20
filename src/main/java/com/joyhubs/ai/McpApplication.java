package com.joyhubs.ai;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class McpApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(McpApplication.class).run(args);
    }

}
