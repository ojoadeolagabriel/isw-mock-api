package com.interswitchgroup.util;

import com.interswitchgroup.App;
import com.interswitchgroup.config.ComponentConfig;
import com.interswitchgroup.data.dao.HttpStatusCodeDao;
import com.interswitchgroup.data.dto.HttpStatusCodeInfo;

import java.util.List;

public class SpringContext {

    public static List<HttpStatusCodeInfo> getHttpStatusCodes(){
        HttpStatusCodeDao httpStatusCodeDao = (HttpStatusCodeDao) App.context.getBean("httpStatusCodeDao");
        return httpStatusCodeDao.codes();
    }
    public static ComponentConfig getConfig(){
        ComponentConfig componentConfig = (ComponentConfig) App.context.getBean("componentConfig");
        return componentConfig;
    }
}
