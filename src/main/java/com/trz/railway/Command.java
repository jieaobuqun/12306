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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.trz.railway.Enum.City;
import com.trz.railway.Enum.Seat;

@SuppressWarnings("unused")
public class Command {

    private static Train train;

    private static Config config = Config.getInstance();

    /**
     * 设置配置信息
     */
    public static TrainConfig[] setTrainConfig() {
        ArrayList<TrainConfig> trainConfig = new ArrayList<>();

		/*设置日期*/
        Calendar cal = Calendar.getInstance();
        Date[] toDates = new Date[1];
        Date[] backDates = new Date[1];

		/*去程*/
        // 1 月用0表示, 2表示3月
        cal.set(2019, Calendar.DECEMBER, 25);
        toDates[0] = cal.getTime();

		/*返回*/
        //cal.set(2017, Calendar.OCTOBER, 8);
        //backDates[0] = cal.getTime();
		
		/*设置车次和座位信息*/
        //Seat[] seat1 = {Seat.硬卧};
        Seat[] seat2 = {Seat.二等座};
        //Seat[] seat3 = {Seat.无座};

        Map<String, Seat[]> toTrains1 = new HashMap<>(16);
        //Map<String, Seat[]> backTrains1 = new HashMap<>();

        toTrains1.put("G571", seat2);

		/*设置起止车站*/
        trainConfig.add(new TrainConfig(City.北京, City.重庆, toTrains1, toDates));

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
        Document document = Jsoup.parse(body);
        String repeatSubmitToken = getJsString(body, "globalRepeatSubmitToken");
        String isDw = getJsString(body, "isDw");

        // 得到解析的JSON对象
        JSONArray init_seatTypes = getJsonArray(body, "init_seatTypes");
        JSONArray defaultTicketTypes = getJsonArray(body, "defaultTicketTypes");
        JSONArray init_cardTypes = getJsonArray(body, "init_cardTypes");
        JSONObject ticket_seat_codeMap = getJsonObject(body, "ticket_seat_codeMap");
        JSONObject ticketInfoForPassengerForm = getJsonObject(body, "ticketInfoForPassengerForm");
        JSONObject orderRequestDTO = getJsonObject(body, "orderRequestDTO");

        /* 第四步，获取乘客信息 */
        params.clear();
        params.add(new BasicNameValuePair("_json_att", null));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));
        response = Command.request(Constant.GET_PASSENGER_URL, params);
        object = parseResponse(response);

        JSONObject passenger = null;
        JSONArray passengers = object.getJSONObject("data").getJSONArray("normal_passengers");
        // 遍历获取乘客信息
        for (Object item : passengers) {
            JSONObject each = (JSONObject)item;

            if (each.getString("passenger_name").equals(config.getPassenger())) {
                passenger = each;
                break;
            }
        }

        if (passenger == null) {
            System.out.println("找不到乘客：" + config.getPassenger() + "的信息");
            System.exit(0);
        }

        /* 第五步, 获取最新验证码信息 */
        Command.request(Constant.GET_PASSCODE_NEW_URL, null);

        /* 第六步, 检查订单信息 */
        String passengerTicketStr = trainInfo.seatType + ",0," + "1" + "," +
                                    passenger.getString("passenger_name") + "," +
                                    passenger.getString("passenger_id_type_code") + "," +
                                    passenger.getString("passenger_id_no") + "," +
                                    (passenger.getString("mobile_no") == null ? "" : passenger.getString("mobile_no"))
                                    + "," +
                                    (StringUtils.isBlank(passenger.getString("save_status")) ? "N" : "Y");

        String oldPassengerStr = passenger.getString("passenger_name") + "," +
                                 passenger.getString("passenger_id_type_code") + "," +
                                 passenger.getString("passenger_id_no") + "," +
                                 passenger.getString("passenger_type") + "_";

        String randCode = "";

        params.clear();
        params.add(new BasicNameValuePair("cancel_flag", "2"));
        params.add(new BasicNameValuePair("bed_level_order_num", "000000000000000000000000000000"));
        params.add(new BasicNameValuePair("passengerTicketStr", passengerTicketStr));
        params.add(new BasicNameValuePair("oldPassengerStr", oldPassengerStr));
        params.add(new BasicNameValuePair("tour_flag", ticketInfoForPassengerForm.getString("tour_flag")));
        params.add(new BasicNameValuePair("randCode", randCode));
        params.add(new BasicNameValuePair("_json_att", null));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.CHECK_ORDER_URL, params);
        object = parseResponse(response);
        if (object.getBoolean("status") == false) {
            System.out.println("检查订单信息失败！");
            System.out.println(object);
            System.exit(0);
        }

        String choose_seats = object.getJSONObject("data").getString("choose_Seats");


        /* 第七步, 获取排队数量 */
        params.clear();
        params.add(new BasicNameValuePair("train_date",
                                          new Date(orderRequestDTO.getJSONObject("train_date").getLong("time"))
                                            .toString()));
        params.add(new BasicNameValuePair("train_no", orderRequestDTO.getString("train_no")));
        params.add(new BasicNameValuePair("stationTrainCode", orderRequestDTO.getString("station_train_code")));
        params.add(new BasicNameValuePair("seatType", trainInfo.seatType));
        params.add(new BasicNameValuePair("fromStationTelecode", orderRequestDTO.getString("from_station_telecode")));
        params.add(new BasicNameValuePair("toStationTelecode", orderRequestDTO.getString("to_station_telecode")));
        params.add(new BasicNameValuePair("leftTicket",
                                          ticketInfoForPassengerForm.getJSONObject("queryLeftTicketRequestDTO")
                                                                    .getString("ypInfoDetail")));
        params.add(new BasicNameValuePair("purpose_codes", ticketInfoForPassengerForm.getString("purpose_codes")));
        params.add(new BasicNameValuePair("train_location", ticketInfoForPassengerForm.getString("train_location")));
        params.add(new BasicNameValuePair("_json_att", null));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.GET_QUEUE_COUNT_URL, params);
        object = parseResponse(response);
        if (object.getBoolean("status") == false) {
            System.out.println("获取排队数量失败！");
            System.out.println(object);
            System.exit(0);
        }

        /* 第八步, 确认排队 */
        String seatDetailType = document.getElementById("x_no").text() +
                                document.getElementById("z_no").text() +
                                document.getElementById("s_no").text();

        String dwAll = "N";
        String roomType = "00";
        String train_location = ticketInfoForPassengerForm.getString("train_location");

        params.clear();
        params.add(new BasicNameValuePair("passengerTicketStr", passengerTicketStr));
        params.add(new BasicNameValuePair("oldPassengerStr", oldPassengerStr));
        params.add(new BasicNameValuePair("randCode", randCode));
        params.add(new BasicNameValuePair("purpose_codes", ticketInfoForPassengerForm.getString("purpose_codes")));
        params.add(
            new BasicNameValuePair("key_check_isChange", ticketInfoForPassengerForm.getString("key_check_isChange")));
        params.add(new BasicNameValuePair("leftTicket", ticketInfoForPassengerForm.getString("leftTicketStr")));
        params.add(new BasicNameValuePair("train_location", ticketInfoForPassengerForm.getString("train_location")));
        params.add(new BasicNameValuePair("choose_seats", choose_seats));
        params.add(new BasicNameValuePair("seatDetailType", seatDetailType));
        params.add(new BasicNameValuePair("roomType", roomType));
        params.add(new BasicNameValuePair("dwAll", dwAll));
        params.add(new BasicNameValuePair("_json_att", null));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.CONFIRM_QUEUE_URL, params);
        object = parseResponse(response);
        if (object.getBoolean("status") == false) {
            System.out.println("确认排队失败！");
            System.out.println(object);
            reSubmit();
            return;
        }

        /* 第九步, 查询排队时间 */
        response = Command.request(Constant.ORDER_WAIT_TIME_URL + repeatSubmitToken, null);
        object = parseResponse(response);
        if (object.getBoolean("status") == false) {
            System.out.println("查询排队时间失败！");
            System.out.println(object);
            System.exit(0);
        }

        /* 第十步, 确认排队结果 */
        params.clear();
        params.add(new BasicNameValuePair("orderSequence_no", "E542818733"));
        params.add(new BasicNameValuePair("_json_att", null));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.RESULT_ORDER_QUEUE_URL, params);
        object = parseResponse(response);
        if (object.getBoolean("status") == false) {
            System.out.println("确认排队结果失败！");
            System.out.println(object);
            System.exit(0);
        }

        /* 第十一步，请求订单页面 */
        params.clear();
        params.add(new BasicNameValuePair("_json_att", null));
        params.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken));

        response = Command.request(Constant.PAY_ORDER_INIT_URL, params);
        body = parseResponseToString(response);

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
            String username = config.getUserName();
            String password = config.getPassword();

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
            String tk = object.getString("newapptk") != null ? object.getString("newapptk") :
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

    /**
     * 得到JSON数组
     */
    private static JSONArray getJsonArray(String body, String key) {
        return JSON.parseArray(getJsonString(body, key));
    }

    /**
     * 解析得到JSON对象
     */
    private static JSONObject getJsonObject(String body, String key) {
        return JSON.parseObject(getJsonString(body, key));
    }

    /**
     * 得到字符串对象
     */
    private static String getJsonString(String body, String key) {
        int startIndex = body.indexOf(key + '=');
        int endIndex = body.indexOf(';', startIndex);

        char[] charArray = body.substring(startIndex + key.length(), endIndex).trim().toCharArray();

        for (int i = 0; i < charArray.length; ++i) {
            if (charArray[i] == '[' || charArray[i] == '{') {
                startIndex = i;
                break;
            }
        }

        return new String(charArray, startIndex, charArray.length - startIndex);
    }

    /**
     * 解析得到JS字符串
     */
    private static String getJsString(String body, String key) {
        int startIndex = body.indexOf(key + '=');
        if (startIndex == -1) {
            startIndex = body.indexOf(key + " =");
        }
        startIndex = body.indexOf('\'', startIndex);
        int endIndex = body.indexOf('\'', startIndex + 1);

        return body.substring(startIndex + 1, endIndex).trim();
    }

    /**
     * 获取文档Id内容
     */
    private static String getIdElementVal(Document doc, String id) {
        Element element = doc.getElementById(id);
        return element != null ? element.val() : null;
    }

    /**
     * 提交失败，重试
     */
    private static void reSubmit() {
        Command.refreshTickets();
        Command.submit();
    }
}
