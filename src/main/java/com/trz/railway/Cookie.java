package com.trz.railway;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

/**
 * @author jieaobuqun  2017-09-18.
 */
public class Cookie {

    /**
     * Cookie连接符
     */
    private static final String COOKIE_LINKER = "=";
    /**
     * Cookie分隔符
     */
    private static final String COOKIE_SPLITTER = ";";
    /**
     * 设置Cookie Header名称
     */
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    /**
     * 存储Cookie键值对
     */
    private Map<String, String> cookieMap = new HashMap<>();

    /** 单例对象*/
    private static class SingletonHolder {
        private static final Cookie INSTANCE = new Cookie();
    }

    public static Cookie getInstance() {
        return Cookie.SingletonHolder.INSTANCE;
    }

    private Cookie() {
    }


    public void setCookie(String cookieStr) {
        if (StringUtils.isBlank(cookieStr)) {
            return;
        }

        cookieStr = cookieStr.trim();
        String[] cookies = cookieStr.split(COOKIE_SPLITTER);

        for (String cookie : cookies) {
            if (StringUtils.isBlank(cookie)) {
                continue;
            }

            setPair(cookie);
        }
    }

    public void setCookie(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() != 200) {
            return;
        }

        Header[] headers = response.getAllHeaders();
        for (Header header : headers) {
            if (header.getName().equalsIgnoreCase(SET_COOKIE_HEADER)) {
                setPair(header.getValue());
            }
        }
    }

    public void setCookie(String key, String value) {
        cookieMap.put(key, value);
    }

    /**
     * 设置单条Cookie
     */
    private void setPair(String cookie) {
        String key, value;

        int index = cookie.indexOf(COOKIE_SPLITTER);
        if (index > 0) {
            cookie = cookie.substring(0, index);
        }

        String[] pair = cookie.split(COOKIE_LINKER);

        if (pair.length == 2) {
            key = pair[0].trim();
            value = pair[1].trim();

            if (StringUtils.isNotBlank(key)) {
                cookieMap.put(key, value);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder cookieBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
            cookieBuilder.append(entry.getKey());
            cookieBuilder.append(COOKIE_LINKER);
            cookieBuilder.append(entry.getValue());
            cookieBuilder.append(COOKIE_SPLITTER);
        }

        if (cookieBuilder.length() > 0) {
            cookieBuilder.deleteCharAt(cookieBuilder.length() - 1);
        }

        return cookieBuilder.toString();
    }
}
