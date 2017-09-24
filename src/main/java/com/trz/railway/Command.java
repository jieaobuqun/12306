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
import com.trz.railway.Train.TrainInfo;

public class Command {

    private static Train train;

    private static String tk = "n02wApxJuOc0hero_NjApQAc7F7tV8tdFZA35Q51j2j0";

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
        Seat[] seat3 = {Seat.无座};

        Map<String, Seat[]> toTrains1 = new HashMap<>();
        Map<String, Seat[]> toTrains2 = new HashMap<>();
        Map<String, Seat[]> toTrains3 = new HashMap<>();
        Map<String, Seat[]> toTrains4 = new HashMap<>();
        Map<String, Seat[]> backTrains1 = new HashMap<>();
        Map<String, Seat[]> backTrains2 = new HashMap<>();
        Map<String, Seat[]> backTrains3 = new HashMap<>();

        toTrains1.put("Z47", seat1);
        toTrains1.put("Z257", seat1);
        toTrains2.put("Z257", seat1);
        toTrains2.put("D2188", seat2);
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
        backTrains2.put("K254", seat3);

		/*设置起止车站*/
        trainConfig.add(new TrainConfig(City.杭州, City.武汉, toTrains1, toDates));
        trainConfig.add(new TrainConfig(City.杭州, City.宜昌, toTrains4, toDates));
        trainConfig.add(new TrainConfig(City.杭州, City.宜昌, toTrains2, toDates));
        trainConfig.add(new TrainConfig(City.杭州, City.宜昌, toTrains3, toDates1));
        trainConfig.add(new TrainConfig(City.武汉, City.杭州, backTrains1, backDates));
        trainConfig.add(new TrainConfig(City.宜昌, City.杭州, backTrains2, backDates));
        trainConfig.add(new TrainConfig(City.宜昌, City.南京, backTrains3, backDates));

        return trainConfig.toArray(new TrainConfig[0]);
    }

    /**
     * 刷票
     */
    public static void refreshTickets() {
        TrainConfig[] config = Command.setTrainConfig();
        train = new Train(config);
        train.refreshTickets();
    }

    /**
     * 模拟选择
     */
    public static void submit() {
        JSONObject object;
        CloseableHttpResponse response;
        List<NameValuePair> params = new ArrayList<>();

        /* 第一步，检查是否登录 */
        /*params.add(new BasicNameValuePair("_json_att", ""));
        response = Command.request(Constant.SUBMIT_CHECK_USER_URL, params);

        object = parseResponse(response);

        if (object != null) {
            System.out.println("提交时，校验登录态失败！");
            System.out.println(object);
            login();
        }

        /* 第二步，获取特殊加密码 */
        TrainInfo trainInfo = train.getTrainInfo();
        params.clear();

        Cookie.getInstance().setCookie("tk", tk);

        params.add(new BasicNameValuePair("secretStr", trainInfo.secretStr));
        params.add(new BasicNameValuePair("train_date", trainInfo.train_date));
        params.add(new BasicNameValuePair("back_train_date", trainInfo.back_train_date));
        params.add(new BasicNameValuePair("tour_flag", trainInfo.tour_flag));
        params.add(new BasicNameValuePair("purpose_codes", trainInfo.purpose_codes));
        params.add(new BasicNameValuePair("query_from_station_name", trainInfo.query_from_station_name));
        params.add(new BasicNameValuePair("query_to_station_name", trainInfo.query_to_station_name));
        params.add(new BasicNameValuePair("undefined", trainInfo.undefined));

        Command.request(Constant.SUBMIT_REQUEST_URL, params);

        params.clear();
        params.add(new BasicNameValuePair("_json_att", ""));
        Command.request(Constant.SUBMIT_INIT_URL, params);

        Train.closeClient();
    }

    /**
     * 初始化
     */
    public static void init() {
        /* 请求登录页面 */
        List<NameValuePair> params = new ArrayList<>();
        CloseableHttpResponse response;
        params.add(new BasicNameValuePair("tk", "F2LmuIegr6F8zCYHF4IXvhG_n2P-_zdKP-6tOAhuj2j0"));
        response = Command.request(Constant.LOGIN_AUTH_CLIENT_URL, params);
        JSONObject object = parseResponse(response);

        //response = Command.request(Constant.LOGIN_URL, null);
        //Train.closeResponse(response);



        /* 获取JS */
        //response = Command.request(Constant.LOGIN_GET_JS_URL, null);
        //String body = parseResponseToString(response);

        /* 注册设备 */
        /*final String algIDParam = "algID";
        final String hashCodeParam = "hashCode";
        int algIDIndex = body.indexOf(algIDParam);
        int algIDEndIndex = body.indexOf(hashCodeParam, algIDIndex);
        String algID = body.substring(algIDIndex + algIDParam.length() + 4, algIDEndIndex - 4);

        String logDeviceUrl = Constant.LOGIN_LOG_DEVICE_URL + '?';
        logDeviceUrl += "algID=" + algID + '&' + hashCodeParam + '=' + JavaScript.getRandomCode() + '&';

        String other = "FMQw=1&q4f3=en-US&VySQ=FFEFJKecMLh_-lEGBkWLB-cNht3mVKrh&VPIf=1&custID=133&VEek=unknown&dzuS=27.0%20r0&"
        + "yD16=0&EOQP=e1d07183b0bcf39b1e2666fcf1a42815&lEnu=3232236035&jp76=e8eea307be405778bd87bbc8fa97b889&hAqN=MacIntel&"
        + "platform=WEB&ks0Q=2955119c83077df58dd8bb7832898892&TeRS=877x1436&tOHY=24xx900x1440&Fvje=i1l1o1s1&q5aJ=-8&"
        + "wNLf=99115dfb07133750ba677d055874de87&0aew=Mozilla/5.0%20(Macintosh;%20Intel%20Mac%20OS%20X%2010_11_6)%20"
        + "AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/60.0.3112.113%20Safari/537.36&E3gR=d70f5ebdc4479931918b34f3bf39eb4e&"
        + "timestamp=" + new Date().getTime();

        logDeviceUrl += other;

        response = Command.request(logDeviceUrl, null);
        JSONObject object = parseResponse(response);
        System.out.println(object);*/
    }

    /**
     * 模拟登录
     */
    public static void login() {
        while (true) {
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

            CloseableHttpResponse response = Command.request(Constant.CAPTCHA_CHECK_URL, params);

            JSONObject object = parseResponse(response);
            if (object.get("result_code").toString().equals("4") == false) {
                continue;
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

            object = parseResponse(response);
            if (object.get("result_code").toString().equals("0") == false) {
                continue;
            }

            /* 请求验证 */
            params.add(new BasicNameValuePair("appid", "otn"));
            response = Command.request(Constant.LOGIN_AUTH_URL, params);
            object = parseResponse(response);
            tk = object.getString("newapptk") != null ?  object.getString("newapptk") :
                                                              object.getString("apptk");

            /* 请求CLIENT */
            Cookie.getInstance().removeCookie("uamtk");
            params.clear();
            params.add(new BasicNameValuePair("tk", tk));
            params.add(new BasicNameValuePair("_json_att", ""));
            response = Command.request(Constant.LOGIN_AUTH_CLIENT_URL, params);
            object = parseResponse(response);
            break;
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

    /**
     * 解析返回的JSON对象
     */
    private static JSONObject parseResponse(CloseableHttpResponse response) {
        JSONObject object = null;

        try {
            String body = EntityUtils.toString(response.getEntity());
            object = JSON.parseObject(new String(body));
            Train.closeResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        return object;
    }

    /**
     * 解析返回的String对象
     */
    private static String parseResponseToString(CloseableHttpResponse response) {
        String object = null;

        try {
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        return object;
    }
}
