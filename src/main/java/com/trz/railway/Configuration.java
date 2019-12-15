package com.trz.railway;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 倚枭  2017-09-19.
 */
public class Configuration {

    /** 用户名 */
    private String userName;
    /** 密码 */
    private String password;
    /** 乘客姓名 */
    private String passenger;
    /** 属性连接符 */
    private static final String LINK_CHAR = "=";

    /* 初始化 */
    static {
        URL url = Object.class.getClassLoader().getResource("config");
        BufferedReader reader = null;

        if (url != null) {
            File file = new File(url.getFile());
            try {
                reader =  new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null ) {
                    if (StringUtils.isBlank(line)) {
                        System.out.println("invalid config line: " + line);
                        continue;
                    }

                    int index = line.indexOf(LINK_CHAR);
                    if (index == -1) {
                        System.out.println("invalid config line: " + line);
                        continue;
                    }

                    String key = line.substring(0, index).trim();
                    String value = line.substring(index + 1).trim();

                    if (StringUtils.isBlank(key)) {
                        System.out.println("invalid config line: " + line);
                        continue;
                    }

                    Field field = Configuration.class.getDeclaredField(key);
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
        private static final Configuration INSTANCE = new Configuration();
    }

    private Configuration() { }

    public static Configuration getInstance() {
        return Configuration.SingletonHolder.INSTANCE;
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
