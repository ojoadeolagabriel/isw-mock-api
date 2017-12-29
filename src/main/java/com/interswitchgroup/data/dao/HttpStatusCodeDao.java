package com.interswitchgroup.data.dao;

import com.interswitchgroup.data.dto.HttpStatusCodeInfo;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component(value = "httpStatusCodeDao")
public class HttpStatusCodeDao {

    @Cacheable(value = "httpStatusCodeCache")
    public List<HttpStatusCodeInfo> codes() {
        List<HttpStatusCodeInfo> codes = new ArrayList<>();
        HttpStatus[] values = HttpStatus.values();
        for (HttpStatus status : values) {
            codes.add(new HttpStatusCodeInfo() {{
                setCode(status.value());
                setMessage(status.getReasonPhrase());
            }});
        }
        return codes;
    }
}
