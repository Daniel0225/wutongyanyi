package com.yiheoline.qcloud.xiaozhibo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/10/11.
 */
public class FastJsonUtil {

    public static <T> T getObject(String jsonString, Class<T> cls) {
        return JSON.parseObject(jsonString, cls);
    }

    public static <T> List<T> getObjects(String jsonString, Class<T> cls) {
        return JSON.parseArray(jsonString, cls);
    }

    public static List<Map<String, String>> getKeyMapsList(String jsonString) {
        List<Map<String, String>> list;
        list = JSON.parseObject(jsonString, new TypeReference<List<Map<String, String>>>() {
        });
        return list;
    }

    //将实体类对象转成Json字符串
    public static String createJsonString(Object object) {
        String jsonString = "";
        try {
            jsonString = JSON.toJSONString(object);
        } catch (Exception e) {
        }

        return jsonString;
    }

}
