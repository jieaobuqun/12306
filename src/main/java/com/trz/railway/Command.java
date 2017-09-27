package com.trz.railway;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.trz.railway.Enum.City;
import com.trz.railway.Enum.Seat;
import com.trz.railway.Train.TrainInfo;

public class Command {

    private static Train            train;

    private static Configuration    configuration = Configuration.getInstance();

    /**
     * 设置配置信息
     */
    public static TrainConfig[] setTrainConfig() {
        ArrayList<TrainConfig> trainConfig = new ArrayList<>();

		/*设置日期*/
        Calendar cal = Calendar.getInstance();
        /*Date[] toDates = new Date[1];
        Date[] toDates1 = new Date[1];
        Date[] backDates = new Date[1];*/
        Date[] backDates1 = new Date[1];

		/*去程*/
        cal.set(2017, Calendar.SEPTEMBER, 30); // 1 月用0表示, 2表示3月
        //toDates[0] = cal.getTime();
        cal.set(2017, Calendar.OCTOBER, 1);
        //toDates1[0] = cal.getTime();
		
		/*返回*/
        cal.set(2017, Calendar.OCTOBER, 8);
        //backDates[0] = cal.getTime();
        cal.set(2017, Calendar.OCTOBER, 18);
        backDates1[0] = cal.getTime();
		
		/*设置车次和座位信息*/
        //Seat[] seat1 = {Seat.硬卧};
        Seat[] seat2 = {Seat.二等座};
        //Seat[] seat3 = {Seat.无座};

        /*Map<String, Seat[]> toTrains1 = new HashMap<>();
        Map<String, Seat[]> toTrains2 = new HashMap<>();
        Map<String, Seat[]> toTrains3 = new HashMap<>();
        Map<String, Seat[]> backTrains1 = new HashMap<>();*/
        Map<String, Seat[]> backTrains2 = new HashMap<>();

        /*toTrains1.put("Z47", seat1);
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
        backTrains2.put("D2264", seat2);*/
        backTrains2.put("G7686", seat2);


		/*设置起止车站*/
        /*trainConfig.add(new TrainConfig(City.杭州, City.武汉, toTrains1, toDates));
        trainConfig.add(new TrainConfig(City.杭州, City.宜昌, toTrains2, toDates));
        trainConfig.add(new TrainConfig(City.杭州, City.宜昌, toTrains3, toDates1));
        trainConfig.add(new TrainConfig(City.武汉, City.杭州, backTrains1, backDates));*/
        trainConfig.add(new TrainConfig(City.杭州, City.南京, backTrains2, backDates1));

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
        params.add(new BasicNameValuePair("_json_att", null));
        response = Command.request(Constant.SUBMIT_CHECK_USER_URL, params);

        object = parseResponse(response);

        if (object.getJSONObject("data").getBoolean("flag") == false) {
            System.out.println("提交时，校验登录态失败！");
            System.out.println(object);
            login();
        }

        /* 第二步，获取特殊加密码 */
        TrainInfo trainInfo = train.getTrainInfo();

        params.clear();
        params.add(new BasicNameValuePair("secretStr", trainInfo.secretStr));
        params.add(new BasicNameValuePair("train_date", trainInfo.train_date));
        params.add(new BasicNameValuePair("back_train_date", trainInfo.back_train_date));
        params.add(new BasicNameValuePair("tour_flag", trainInfo.tour_flag));
        params.add(new BasicNameValuePair("purpose_codes", trainInfo.purpose_codes));
        params.add(new BasicNameValuePair("query_from_station_name", trainInfo.query_from_station_name));
        params.add(new BasicNameValuePair("query_to_station_name", trainInfo.query_to_station_name));
        params.add(new BasicNameValuePair("undefined", trainInfo.undefined));

        response = Command.request(Constant.SUBMIT_REQUEST_URL, params);
        object = parseResponse(response);
        if (object.getBoolean("status") == false) {
            System.out.println("发起提交申请失败！");
            System.out.println(object);
            System.exit(0);
        }

        /* 第三步，请求提交页面 */
        params.clear();
        params.add(new BasicNameValuePair("_json_att", null));
        response = Command.request(Constant.SUBMIT_INIT_URL, params);

        String body = parseResponseToString(response);

        final String tokenVariable = "globalRepeatSubmitToken = '";
        int tokenIndex = body.indexOf(tokenVariable) + tokenVariable.length() + 1;
        int singleQuotesIndex = body.indexOf((int)'\'', tokenIndex);

        String repeatSubmitToken = body.substring(tokenIndex, singleQuotesIndex - tokenIndex);


        /* 第四步，获取乘客信息 */
        params.clear();
        params.add(new BasicNameValuePair("_json_att", null));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));
        response = Command.request(Constant.GET_PASSENGER_URL, params);
        object = parseResponse(response);

        JSONObject passenger = null;
        JSONArray passengers = object.getJSONObject("data").getJSONArray("normal_passengers");
        // 遍历获取乘客信息
        for(Object item : passengers) {
            JSONObject each = (JSONObject)item;

            if (each.getString("passenger_name").equals(configuration.getPassenger())) {
                passenger = each;
                break;
            }
        }

        if (passenger == null) {
            System.out.println("找不到乘客：" + configuration.getPassenger() + "的信息");
            System.exit(0);
        }

        /* 第五步, 获取最新验证码信息 */
        Command.request(Constant.GET_PASSCODE_NEW_URL, null);

        /* 第六步, 检查订单信息 */
        params.clear();
        params.add(new BasicNameValuePair("cancel_flag", "2"));
        params.add(new BasicNameValuePair("bed_level_order_num", "000000000000000000000000000000"));
        params.add(new BasicNameValuePair("passengerTicketStr", "O,0,1,田睿智,1,500226198804276213,15968103684,N"));
        params.add(new BasicNameValuePair("oldPassengerStr", "田睿智,1,500226198804276213,1_"));
        params.add(new BasicNameValuePair("tour_flag", "dc"));
        params.add(new BasicNameValuePair("randCode", null));
        params.add(new BasicNameValuePair("_json_att", null));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.CHECK_ORDER_URL, params);
        object = parseResponse(response);
        System.out.println(object);

        /* 第七步, 获取排队数量 */
        params.clear();
        params.add(new BasicNameValuePair("train_date", "Thu Sep 28 2017 00:00:00 GMT+0800 (CST)"));
        params.add(new BasicNameValuePair("train_no", "56000D222250"));
        params.add(new BasicNameValuePair("stationTrainCode", "D2222"));
        params.add(new BasicNameValuePair("seatType", "O"));
        params.add(new BasicNameValuePair("fromStationTelecode", "HGH"));
        params.add(new BasicNameValuePair("toStationTelecode", "NKH"));
        params.add(new BasicNameValuePair("leftTicket", "I25T%2BF%2B%2B83IK3TYbNuXwa07KuL70CS2E9NAVyDAmAY88pFEq"));
        params.add(new BasicNameValuePair("purpose_codes", "00"));
        params.add(new BasicNameValuePair("train_location", "H2"));
        params.add(new BasicNameValuePair("_json_att", ""));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.GET_QUEUE_COUNT_URL, params);
        object = parseResponse(response);
        System.out.println(object);

        /* 第八步, 确认排队 */
        params.clear();
        params.add(new BasicNameValuePair("passengerTicketStr", "O,0,1,田睿智,1,500226198804276213,15968103684,N"));
        params.add(new BasicNameValuePair("oldPassengerStr", "田睿智,1,500226198804276213,1_"));
        params.add(new BasicNameValuePair("randCode", ""));
        params.add(new BasicNameValuePair("purpose_codes", "00"));
        params.add(new BasicNameValuePair("key_check_isChange", "40A7B421F8CAC6A750B965AC92364EDB91A5194A18C39B9230678FC4"));
        params.add(new BasicNameValuePair("leftTicket", "I25T%2BF%2B%2B83IK3TYbNuXwa07KuL70CS2E9NAVyDAmAY88pFEq"));
        params.add(new BasicNameValuePair("train_location", "H2"));
        params.add(new BasicNameValuePair("choose_seats", ""));
        params.add(new BasicNameValuePair("seatDetailType", "000"));
        params.add(new BasicNameValuePair("roomType", "00"));
        params.add(new BasicNameValuePair("dwAll", "N"));
        params.add(new BasicNameValuePair("_json_att", ""));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.CONFIRM_QUEUE_URL, params);
        object = parseResponse(response);
        System.out.println(object);

        /* 第九步, 查询排队时间 */
        response = Command.request(Constant.ORDER_WAIT_TIME_URL + repeatSubmitToken, null);
        object = parseResponse(response);
        System.out.println(object);

        /* 第十步, 确认排队结果 */
        params.clear();
        params.add(new BasicNameValuePair("orderSequence_no", "E542818733"));
        params.add(new BasicNameValuePair("_json_att", ""));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.RESULT_ORDER_QUEUE_URL, params);
        object = parseResponse(response);
        System.out.println(object);

        /* 第十一步，请求订单页面 */
        params.clear();
        params.add(new BasicNameValuePair("_json_att", ""));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.PAY_ORDER_INIT_URL, params);
        object = parseResponse(response);
        System.out.println(object);

        /* 关闭client */
        Train.closeClient();
    }

    /**
     * 初始化
     */
    public static void init() {
        /* 请求登录页面 */
        CloseableHttpResponse response = Command.request(Constant.LOGIN_URL, null);
        Train.closeResponse(response);

            /* 请求验证 */
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("appid", "otn"));
        params.add(new BasicNameValuePair("_json_att", null));

        response = Command.request(Constant.LOGIN_AUTH_URL, params);
        JSONObject object = parseResponse(response);
        System.out.println(object);
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
            params.add(new BasicNameValuePair("_json_att", null));

            CloseableHttpResponse response = Command.request(Constant.CAPTCHA_CHECK_URL, params);

            JSONObject object = parseResponse(response);
            if (object.getInteger("result_code").equals(4) == false) {
                continue;
            }

            /* 登录 */
            String username = configuration.getUserName();
            String password = configuration.getPassword();

            params.clear();
            params.add(new BasicNameValuePair("appid", "otn"));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("_json_att", null));

            response = Command.request(Constant.LOGIN_SUBMIT_URL, params);

            object = parseResponse(response);
            if (object.getInteger("result_code").equals(0) == false) {
                continue;
            }

            /* 请求验证 */
            params.clear();
            params.add(new BasicNameValuePair("appid", "otn"));
            params.add(new BasicNameValuePair("_json_att", null));
            response = Command.request(Constant.LOGIN_AUTH_URL, params);
            object = parseResponse(response);
            String tk = object.getString("newapptk") != null ?  object.getString("newapptk") :
                                                                     object.getString("apptk");

            /* 请求CLIENT */
            params.clear();
            params.add(new BasicNameValuePair("tk", tk));
            params.add(new BasicNameValuePair("_json_att", null));
            response = Command.request(Constant.LOGIN_AUTH_CLIENT_URL, params);
            object = parseResponse(response);

            if (object.getInteger("result_code").equals(0) == false) {
                continue;
            }
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
            URL url = classLoader.getResource("icon.png");
            if (url != null) {
                icon = ImageIO.read(new File(url.getFile()));
            }
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
            object = JSON.parseObject(body);
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
