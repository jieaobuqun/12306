package com.trz.railway;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * @author 倚枭  2017-09-19.
 */
public class Config {

    /** 用户名 */
    private String userName;
    /** 密码 */
    private String password;
    /** 乘客姓名 */
    private String passenger;

    /* 初始化 */
    static {
        URL url = Object.class.getClassLoader().getResource("config");
        BufferedReader reader = null;

        if (url != null) {
            File file = new File(url.getFile());
            try {
                Properties properties = new Properties();
                reader =  new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                properties.load(reader);
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    String key = entry.toString().trim();
                    String value = entry.toString().trim();

                    Field field = Config.class.getDeclaredField(key);
                    if (field == null) {
                        System.out.println("can not find field " + key);
                        continue;
                    }

                    field.setAccessible(true);
                    field.set(SingletonHolder.INSTANCE, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** 单例对象*/
    private static class SingletonHolder {
        private static final Config INSTANCE = new Config();
    }

    private Config() { }

    public static Config getInstance() {
        return Config.SingletonHolder.INSTANCE;
    }

    public String getUserName() {
        return userName;
    }

    @SuppressWarnings("unused")
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return Security.aesDecrypt(password);
    }

    @SuppressWarnings("unused")
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassenger() {
        return passenger;
    }

    @SuppressWarnings("unused")
    public void setPassenger(String passenger) {
        this.passenger = passenger;
    }
}
