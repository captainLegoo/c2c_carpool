package com.heima.stroke.handler;

import com.alibaba.fastjson.JSONObject;
import com.heima.commons.domin.bo.RoutePlanResultBO;
import com.heima.commons.domin.bo.TextValue;
import com.heima.commons.utils.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BaiduMapClient {
    //@Value("${baidu.map.api}")
    //private String api;
    //@Value("${baidu.map.ak}")
    //private String ak;
    private static final String api = "https://api.map.baidu.com/routematrix/v2/driving";
    private static final String ak = "pzqWfSQ4MOs3zAPVeQFc9Ozwv8LRj6GI";
    private final static Logger logger = LoggerFactory.getLogger(BaiduMapClient.class);

    public RoutePlanResultBO pathPlanning(String origins, String destinations) {
        try {
            Map map = new HashMap();
            map.put("origins", origins);
            map.put("destinations", destinations);
            map.put("ak", this.ak);
            String result = HttpClientUtils.doGet(api, map);
            return extractResultInfo(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RoutePlanResultBO extractResultInfo(String result) {
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONObject resultObject = jsonObject.getJSONArray("result").getJSONObject(0);
        JSONObject distanceObject = resultObject.getJSONObject("distance");
        int distanceValue = distanceObject.getInteger("value");
        String distanceText = distanceObject.getString("text");

        JSONObject durationObject = resultObject.getJSONObject("duration");
        int durationValue = durationObject.getInteger("value");
        String durationText = durationObject.getString("text");

        RoutePlanResultBO routePlanResultBO = new RoutePlanResultBO();

        TextValue distanceTextValue = new TextValue();
        distanceTextValue.setValue(distanceValue);
        distanceTextValue.setText(distanceText);
        routePlanResultBO.setDistance(distanceTextValue);

        TextValue durationTextValue = new TextValue();
        durationTextValue.setValue(durationValue);
        durationTextValue.setText(durationText);
        routePlanResultBO.setDuration(durationTextValue);
        logger.debug("routePlanResultBO: {}", routePlanResultBO);

        return routePlanResultBO;
    }
}
