package com.trz.railway;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.trz.railway.Enum.City;
import com.trz.railway.Enum.Seat;

public class Command {

    /**
     * 设置配置信息
     */
    public static TrainConfig[] setTrainConfig() {
        ArrayList<TrainConfig> trainConfig = new ArrayList<>();

		/*设置日期*/
        Calendar cal = Calendar.getInstance();
        Date[] toDates = new Date[1];
        Date[] toDates1 = new Date[1];
        Date[] backDates = new Date[1];
		
		/*去程*/
        cal.set(2017, Calendar.SEPTEMBER, 30); // 1 月用0表示, 2表示3月
        toDates[0] = cal.getTime();
        cal.set(2017, Calendar.OCTOBER, 1);
        toDates1[0] = cal.getTime();
		
		/*返回*/
        cal.set(2017, Calendar.OCTOBER, 8);
        backDates[0] = cal.getTime();
		
		/*设置车次和座位信息*/
        Seat[] seat1 = {Seat.硬卧};
        Seat[] seat2 = {Seat.二等座};

        Map<String, Seat[]> toTrains1 = new HashMap<>();
        Map<String, Seat[]> toTrains2 = new HashMap<>();
        Map<String, Seat[]> toTrains3 = new HashMap<>();
        Map<String, Seat[]> backTrains1 = new HashMap<>();
        Map<String, Seat[]> backTrains2 = new HashMap<>();
        Map<String, Seat[]> backTrains3 = new HashMap<>();

        toTrains1.put("Z47", seat1);
        toTrains1.put("Z257", seat1);
        toTrains2.put("Z257", seat1);
        toTrains3.put("D2222", seat2);
        toTrains3.put("D656", seat2);
        toTrains3.put("D2262", seat2);
        toTrains3.put("D2246", seat2);
        backTrains1.put("Z45", seat1);
        backTrains1.put("Z255", seat1);
        backTrains2.put("Z258", seat1);
        backTrains2.put("D2248", seat2);
        backTrains2.put("D2224", seat2);
        backTrains2.put("D2264", seat2);

		/*设置起止车站*/
        trainConfig.add(new TrainConfig(City.杭州, City.武汉, toTrains1, toDates));
        trainConfig.add(new TrainConfig(City.杭州, City.宜昌, toTrains2, toDates));
        trainConfig.add(new TrainConfig(City.杭州, City.宜昌, toTrains3, toDates1));
        trainConfig.add(new TrainConfig(City.武汉, City.杭州, backTrains1, backDates));
        trainConfig.add(new TrainConfig(City.宜昌, City.杭州, backTrains2, backDates));
        trainConfig.add(new TrainConfig(City.宜昌, City.南京, backTrains3, backDates));

        return trainConfig.toArray(new TrainConfig[0]);
    }

    /**
     * 模拟登录
     */
    public static void login() {
        while (true) {
            CloseableHttpResponse response = Command.request(Constant.LOGIN_URL, null);
            Train.closeResponse(response);

		    /* 用户识别验证码 */
            View view = new View("登录");
            view.captcha();

            try {
                synchronized (Command.class) {
                    Command.class.wait();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

		    /* 验证验证码*/
            String randCode = view.getRandCode();

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("answer", randCode));
            params.add(new BasicNameValuePair("login_site", "E"));
            params.add(new BasicNameValuePair("rand", "sjrand"));

            response = Command.request(Constant.CAPTCHA_CHECK_URL, params);

            try {
                String body = EntityUtils.toString(response.getEntity());
                JSONObject object = JSON.parseObject(body);
                Train.closeResponse(response);

                if (object.get("result_code").equals("4") == false) {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            /* 登录 */
            Configuration configuration = Configuration.getInstance();
            String username = configuration.getUserName();
            String password = configuration.getPassword();

            params.clear();

            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("appid", "otn"));

            response = Command.request(Constant.LOGIN_SUBMIT_URL, params);

            try {
                String body = EntityUtils.toString(response.getEntity());
                JSONObject object = JSON.parseObject(body);
                if (object.get("result_code").equals("0") == false) {
                    continue;
                } else {
                    break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取图片验证码
     */
    public static BufferedImage getImage(String url) {
        CloseableHttpResponse response = Train.getRequest(url);
        BufferedImage myImage = null;

        try {
            myImage = ImageIO.read(response.getEntity().getContent());
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return myImage;
    }

    /**
     * 获取火车图片
     */
    public static BufferedImage getImage() {
        BufferedImage icon = null;
        ClassLoader classLoader = Train.class.getClassLoader();

        try {
            icon = ImageIO.read(new File(classLoader.getResource("icon.png").getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return icon;
    }

    /**
     * 返回请求，直到成功
     */
    public static CloseableHttpResponse request(String url, List<NameValuePair> params) {
        boolean success = false;
        CloseableHttpResponse response = null;

        while (!success) {
            response = CollectionUtils.isEmpty(params) ? Train.getRequest(url) :
                Train.postRequest(url, params);
            success = response != null && response.getStatusLine().getStatusCode() == 200;
        }

        return response;
    }
}
