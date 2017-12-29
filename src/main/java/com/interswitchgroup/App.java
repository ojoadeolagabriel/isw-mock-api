package com.interswitchgroup;

import com.interswitchgroup.proxy.MockProxyGatewayInitializer;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;

@SpringBootApplication
public class App implements ApplicationContextAware {
    public static ApplicationContext context;

    public static void main(String[] args) throws IOException {
        SpringApplication.run(App.class, args);
        MockProxyGatewayInitializer.init();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
