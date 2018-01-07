package com.interswitchgroup.web;

import com.interswitchgroup.config.ComponentConfig;
import com.interswitchgroup.data.dao.HttpStatusCodeDao;
import com.interswitchgroup.data.dao.LoggerRouteDao;
import com.interswitchgroup.data.dto.Log;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/web/isw-api-mock/public/logs")
public class LogContoller {
    @Value("${route.admin.url}")
    String routeAdminUrl;
    @Autowired
    ComponentConfig componentConfig;
    @Autowired
    HttpStatusCodeDao httpStatusCodeDao;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView log(@RequestParam(required = false, defaultValue = "15") int interval) {
        List<Log> fetch = LoggerRouteDao.fetch(DateTime.now().plusMinutes(-interval).toDate().getTime(), DateTime.now().toDate().getTime());
        ModelAndView modelAndView = new ModelAndView("logs");
        modelAndView.addObject("data", fetch);
        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(
            @RequestParam(required = false, defaultValue = "120") int filterInterval,
            @RequestParam(value = "btnFilterRoute", required = false) String filterCriteria) {
        List<Log> fetch = LoggerRouteDao.fetchAndFilterByDesc(filterCriteria, DateTime.now().plusMinutes(-filterInterval).toDate().getTime(), DateTime.now().toDate().getTime());
        ModelAndView modelAndView = new ModelAndView("logs");
        modelAndView.addObject("data", fetch);
        return modelAndView;
    }
}
