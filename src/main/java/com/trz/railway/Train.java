package com.trz.railway;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.helper.StringUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.trz.railway.Enum.Seat;

public class Train {

    /**
     * 列车排序
     */
    private int                         trainIndex = 0;
    /**
     * 刷票配置
     */
    private TrainConfig[]               config;
    /**
     * 代理IP
     */
    private static final String         proxyIp = "120.55.38.20";
    /**
     * 代理端口
     */
    private static final int            proxyPort = 3128;
    /**
     * Cookie
     */
    private static Cookie               cookie = Cookie.getInstance();
    /**
     * 请求客户端
     */
    private static CloseableHttpClient  httpClient;


    public Train(TrainConfig[] config) {
        this.config = config;
    }

    /**
     * 刷票总入口
     */
    public void refreshTickets() {
        int num = 0;
        boolean hasTicket;
        StringBuilder buffer = new StringBuilder();

        while (true) {
            for (TrainConfig conf : config) {
                String[] urls = conf.getUrls();
                for (String url : urls) {
                    hasTicket = request(num, url, conf, buffer);
                    if (hasTicket) { return; }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            ++num;
        }
    }

    /**
     * 发送HTTP请求
     */
    private boolean request(int num, String url, TrainConfig conf, StringBuilder buffer) {
        httpClient = getClient();
        if (httpClient == null) { return true; }

        // Get方法
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;

        // 屏幕能打印多少个状态码
        final int screenSize = 36;
        // 请求多少次显示一次HTTP状态码
        final int timesShow = 2;
        // 多少行状态码之后打印列车信息
        final int lineNum = 1;

        // 总共多少url
        int totalUrls = 0;
        for (TrainConfig con : config) {
            totalUrls += con.getUrls().length;
        }
        // 请求多少次输出换行，根据以上两个常量来决定
        final int timesNewLine = screenSize / totalUrls * timesShow;
        // 请求多少次输出一次列车信息
        final int timesTrainInfo = timesNewLine * lineNum;

        label:
        try {
            response = Train.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (config[0].getUrls()[0].equals(url) && ((num > 0 && num % timesNewLine == 0) ||
                                                       (num > 1 && num % timesTrainInfo == 1 && trainIndex % 2 == 1))) {
                trainIndex = 0;
                System.out.println();
            }

            if (config[0].getUrls()[0].equals(url) && num % timesTrainInfo == 1) {
                System.out.print(buffer.toString());
                buffer.setLength(0);
            }

            if (num % timesShow == 0) {
                if (num == 0 || num % timesTrainInfo != 0) {
                    System.out.print(statusCode + "  ");
                } else {
                    buffer.append(statusCode + "  ");
                }
            }
            if (statusCode != 200) { break label; }

            HttpEntity entity = response.getEntity();
            if (entity == null) { break label; }

            String body = EntityUtils.toString(entity, "UTF-8");

            // parsing JSON
            JSONObject result = JSONObject.parseObject(body);
            JSONArray trains = result.getJSONObject("data").getJSONArray("result");
            if (trains == null || trains.isEmpty()) { break label; }

            int trainCount = conf.getTrainCount();
            List<String> trainFound = new LinkedList<>();
            for (Object obj : trains) {
                String train = (String)obj;
                String[] fields = train.split("\\|");

                // 计算位移
                int index = 0;
                while (fields[index].startsWith("预订") == false &&
                       fields[index].startsWith("列车运行图调整") == false &&
                       fields[index].startsWith("暂售至") == false &&
                       fields[index].endsWith("起售") == false &&
                       fields[index].endsWith("系统维护时间") == false) {
                    ++index;
                }

                String trainName = fields[index + 2];
                String dateString = getDateString(fields[index + 12]);
                Seat[] seats = conf.getSeats(trainName);
                if (seats == null) { continue; }

                trainCount--;
                trainFound.add(trainName);

                if (num > 0 && num % timesTrainInfo == 0) {
                    System.out.format("train: %-5s    ", trainName);
                    System.out.print("date: " + dateString + "  ");
                    System.out.format("%2s - %2s", conf.getFromCity().name(), conf.getToCity().name());

                    for (Seat seat : seats) {
                        String seatNum = fields[index + 20 + seat.ordinal()];
                        System.out.format("  %3s:%-2s", seat.name(), StringUtil.isBlank(seatNum) ? "无" : seatNum);
                    }

                    if (++trainIndex % 2 == 0) { System.out.println(); } else { System.out.print("\t\t\t"); }
                }

                boolean hasTicket = false;
                for (int i = 0; !hasTicket && i < seats.length; ++i) {
                    String seatNum = fields[index + 20 + seats[i].ordinal()];
                    if (StringUtil.isBlank(seatNum) == false &&
                        !seatNum.equals("无") && !seatNum.equals("--") && !seatNum.equals("*")) {
                        System.out.format("train: %5s    ", trainName);
                        System.out.print("date: " + dateString + "  ");
                        System.out.format("%2s - %2s", conf.getFromCity().name(), conf.getToCity().name());
                        System.out.format("  %3s:%2s", seats[i].name(), seatNum);
                        hasTicket = true;
                        break;
                    }
                }

                if (!hasTicket) { continue; }

                for (int i = 0; i < 2; ++i) {
                    playVideo();
                }
                return true;

            }

            if (trainCount == 0) { break label; }

            // 有找不到的列车信息，可能写错了
            List<String> missingTrain = conf.getAbsentTrain(trainFound);
            System.out.print("找不到以下列车信息（" + conf.getFromCity().name()
                             + "--" + conf.getToCity().name() + "）：");
            for (String train : missingTrain) { System.out.print(" " + train + " "); }
            System.out.println();

            return false;
        } catch (Exception e) {
            //e.printStackTrace();
        }

        try {
            if (response != null) { response.close(); }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 初始化请求
     */
    public void init() {
        HttpGet httpGet = new HttpGet(Constant.INIT_URL);
        httpClient = getClient();
        if (httpClient == null) { return; }

        cookie.setCookie("RAIL_DEVICEID", "Gt43Gx4YEL8A2BIHpGV_jRn4LqI44fiHETjpO2S4itAMZf5"
        + "mEtjlAn1DR332z1ZEmHGI9FDcAe1akY8bN0QL0uZWi21eR_raOtidElgbdNLnLRXZdbuelo3Jyghas5OZDkQBGCC3NjNa8NnxO_LGdEjWsE3tJMFV");
        cookie.setCookie("RAIL_EXPIRATION", "150617059899");

        try {
            while (true) {
                HttpResponse response = Train.execute(httpGet);
                if (response.getStatusLine().getStatusCode() != 200) {
                    continue;
                }

                break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 格式化日期字符串
     */
    private String getDateString(String dateString) {
        Date date = null;
        DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
        DateFormat dateFormat2 = new SimpleDateFormat("yyyy-M-d");
        try {
            date = dateFormat1.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateFormat2.format(date);
    }

    /**
     * 获取请求应答
     */
    public static CloseableHttpResponse getRequest(String url) {
        httpClient = getClient();
        if (httpClient == null) { return null; }

        // Post方法
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;

        try {
            response = Train.execute(httpGet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 发送POST请求
     */
    public static CloseableHttpResponse postRequest(String url, List<NameValuePair> params) {
        httpClient = getClient();
        if (httpClient == null) { return null; }

        // Post方法
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;

        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Constant.UTF8_ENCODE);
            httpPost.setEntity(entity);
            response = Train.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 关闭应答
     */
    public static void closeResponse(CloseableHttpResponse response) {
        try {
            if (response != null) { response.close(); }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /**
     * 获取请求客户端
     */
    private static CloseableHttpClient getClient() {
        if (httpClient != null) { return httpClient; }

        SSLContextBuilder builder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslsf;
        try {
            TrustSelfSignedStrategy strategy = new TrustSelfSignedStrategy() {
                @Override
                public boolean isTrusted(final X509Certificate[] chain, final String authType)
                    throws CertificateException {
                    return true;
                }
            };

            builder.loadTrustMaterial(null, strategy);
            sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        HttpHost proxy = new HttpHost(proxyIp, proxyPort);
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

        return HttpClients.custom().setRoutePlanner(routePlanner).setSSLSocketFactory(sslsf).build();
    }

    /**
     * 执行HTTP GET请求
     */
    public static CloseableHttpResponse execute(HttpGet httpGet) {
        CloseableHttpResponse response = null;

        try {
            httpGet.addHeader(new BasicHeader("cookie", cookie.toString()));
            response = httpClient.execute(httpGet);
            cookie.setCookie(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 执行HTTP POST请求
     */
    public static CloseableHttpResponse execute(HttpPost httpPost) {
        CloseableHttpResponse response = null;

        httpPost.addHeader(new BasicHeader("cookie", cookie.toString()));

        try {
            response = httpClient.execute(httpPost);
            cookie.setCookie(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 关闭客户端
     */
    public static void closeClient() {
        if (httpClient == null) { return; }

        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放鸣笛音乐
     */
    private static void playVideo() {
        ClassLoader classLoader = Train.class.getClassLoader();
        URL url = classLoader.getResource("train.wav");
        if (url == null) return;

        File file = new File(url.getFile());

        SourceDataLine auline = null;
        AudioInputStream audioInputStream = null;

        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioInputStream.getFormat();

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            auline = (SourceDataLine)AudioSystem.getLine(info);
            auline.open(format);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (auline == null) return;
        auline.start();
        int nBytesRead = 0;
        final int EXTERNAL_BUFFER_SIZE = 524288;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        try {
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0) { auline.write(abData, 0, nBytesRead); }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            auline.drain();
            auline.close();
        }
    }
}
