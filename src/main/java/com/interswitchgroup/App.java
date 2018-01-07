package com.interswitchgroup;

import com.interswitchgroup.proxy.MockProxyGatewayInitializer;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;

@SpringBootApplication
public class App implements ApplicationContextAware {
    public static ApplicationContext context;
    public static JsonObject adhocConfig = new JsonObject();

    public static void main(String[] args) throws IOException {
        SpringApplication.run(App.class, args);
        adhocConfig.put("globalSuccessResponseCode", "90000");
        MockProxyGatewayInitializer.init();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
