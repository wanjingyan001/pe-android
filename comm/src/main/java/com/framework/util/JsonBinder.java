package com.framework.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Mars
 * @description json 解析，序列化
 * @create 2013-12-20 上午9:25:49
 */
public class JsonBinder {
    public static final Gson gson = createGson();

    public static Gson createGson() {
        return new GsonBuilder().serializeNulls().create();
    }

    /**
     * 将对象转成jsonString
     */
    public static <T> String toJsonString(T t) {
        Gson gson = createGson();
        return gson.toJson(t).toString();
    }

    /**
     */
    public static JSONObject toJson(Object obj) throws JSONException {
        Gson gson = createGson();
        String str = gson.toJson(obj);
        return new JSONObject(str);
    }

    /**
     * 将json字符串转成对象
     */
    public static <T> T fromJson(String json, Class<T> cla) throws JsonSyntaxException {
        return gson.fromJson(json, cla);
    }

    /**
     * new TypeToken<List<Music>>(){}.getType()
     */
    public static <T> List<T> fromJson(String jsonStr, Type type) throws JsonSyntaxException {
        Gson gson = createGson();
        return gson.fromJson(jsonStr, type);
    }
}
