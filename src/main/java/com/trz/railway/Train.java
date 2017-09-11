package com.trz.railway;

import java.io.File;
import java.io.IOException;
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

import com.trz.railway.Enum.Seat;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.jsoup.helper.StringUtil;

public class Train {
	
	private Config []config;
	
	private int trainIndex = 0;

	private String cookie;

	private static final String initUrl = "https://kyfw.12306.cn/otn/leftTicket/init";

	private static final String proxyIp = "111.13.7.42";

	private static final int proxyPort = 81;
	
	private static CloseableHttpClient httpClient;
	
	public Train (Config []config) {
		this.config = config;
	}

	public void refreshTickets () {
		int num = 0;
		boolean hasTicket = false;
        StringBuilder buffer = new StringBuilder();

		while (!hasTicket) {
			for (Config conf : config) {
				String []urls = conf.getUrls();
				for (String url : urls) {
					hasTicket = request(num, url, conf, buffer);
					if (hasTicket) return;
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}	
			}
            ++num;
		}
	}

	public boolean request(int num, String url, Config conf, StringBuilder buffer) {
		httpClient = getClient();
		if (httpClient == null) return true;

		// Get方法
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("cookie", cookie);
		CloseableHttpResponse response = null;
		
		// 屏幕能打印多少个状态码
		final int screenSize = 32;
		// 请求多少次显示一次HTTP状态码
		final int timesShow = 2;
        // 多少行状态码之后打印列车信息
        final int lineNum = 1;

		// 总共多少url
		int totalUrls = 0;
		for (Config con : config) {
		    totalUrls += con.getUrls().length;
		}
		// 请求多少次输出换行，根据以上两个常量来决定
		final int timesNewLine = screenSize / totalUrls * timesShow;
		// 请求多少次输出一次列车信息
		final int timesTrainInfo = timesNewLine * lineNum;

		label: try {
			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (config[0].getUrls()[0] == url && ((num > 0 && num % timesNewLine == 0) ||
                                                  (num > 1 && num % timesTrainInfo == 1 && trainIndex % 2 == 1)) ) {
			    trainIndex = 0;
			    System.out.println();
			}

			if (config[0].getUrls()[0] == url && num % timesTrainInfo == 1) {
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
			if (statusCode != 200) break label;

			HttpEntity entity = response.getEntity();
			if (entity == null) break label;

			String body = EntityUtils.toString(entity, "UTF-8");

			// parsing JSON
			JSONObject result = JSONObject.parseObject(body);
			JSONArray trains = result.getJSONObject("data").getJSONArray("result");
			if (trains == null || trains.isEmpty()) break label;

			int trainCount = conf.getTrainCount();
			List<String> trainFound = new LinkedList<>();
			for (Object obj : trains) {
                String train = (String) obj;
                String[] fields = train.split("\\|");

                // 计算位移
                int index = 0;
                while (fields[index].startsWith("预订") == false &&
                       fields[index].startsWith("列车运行图调整") == false &&
                       fields[index].startsWith("暂售至") == false &&
                       fields[index].endsWith("起售") == false) {
                    ++index;
                }

				String trainName = fields[index + 2];
				String dateString = getDateString(fields[index + 12]);
				Seat[] seats = conf.getSeats(trainName);
				if (seats == null)
					continue;
				
				trainCount--;
				trainFound.add(trainName);

				if (num > 0 && num % timesTrainInfo == 0) {
					System.out.format("train: %-5s    ", trainName);
					System.out.print("date: " + dateString + "  ");
					System.out.format("%2s - %2s", conf.getFromCity().name(), conf.getToCity().name());
					
					for (Seat seat : seats)  {
					    String seatNum = fields[index + 24 + seat.ordinal()];
						System.out.format("  %3s:%-2s", seat.name(), StringUtil.isBlank(seatNum) ? "无" : seatNum);
					}
					
					if (++trainIndex % 2 == 0)
					    System.out.println();
					else
					    System.out.print("\t\t\t");
				}
				
				boolean hasTicket = false;
				for (int i = 0; !hasTicket && i < seats.length; ++i){
                    String seatNum = fields[index + 20 + seats[i].ordinal()];
					if ( StringUtil.isBlank(seatNum) == false &&
                         !seatNum.equals("无") && !seatNum.equals("--") && !seatNum.equals("*")) {
                        System.out.format("train: %5s    ", trainName);
                        System.out.print("date: " + dateString + "  ");
                        System.out.format("%2s - %2s", conf.getFromCity().name(), conf.getToCity().name());
						System.out.format("  %3s:%2s", seats[i].name(), seatNum);
						hasTicket = true;
						break;
					}	
				}
				
				if (!hasTicket) continue;
				
				for (int i = 0; i < 2; ++i) {
                    playVideo();
                }
				return true;
			
			}
			
			if (trainCount == 0)  break label;
			
			// 有找不到的列车信息，可能写错了
			List<String> missingTrain = conf.getAbesentTrain(trainFound);
			System.out.print("找不到以下列车信息（" + conf.getFromCity().name()
							+  "--" + conf.getToCity().name() + "）：");
			for (String train : missingTrain)
				System.out.print(" " + train + " ");
			System.out.println();
			
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (response != null)
				response.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public void init(){
	    HttpGet httpGet = new HttpGet(initUrl);
        httpClient = getClient();
        try {
			while (true) {
                HttpResponse response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() != 200) {
                    continue;
                }

                Header[] headers = response.getHeaders("Set-Cookie");
                StringBuilder  builder = new StringBuilder();
                for (Header header : headers) {
                    builder.append( header.getValue().split(";")[0]);
                    builder.append(";");
                }

                builder.append("RAIL_DEVICEID=QSTF_Pp5d0KigjzCtBW6mwr7-27Djped6eybGe-F6Onbm25_9Zb_xovg740-xeYIynXIwzrm" +
                               "KKXZW5lthyM0PBWJgZNYy4Jj6OhPTd90jXUwornOW5QgbDacaAvfcHD1sCwdB3Ux9bMhPZFlh4zFOquQFvwc15h_");
                cookie = builder.toString();
                break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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
	
	public static CloseableHttpResponse getRequest (String url) {
		httpClient = getClient();
		if (httpClient == null) return null;

		// Post方法
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = null;
		
		try {
			response = httpClient.execute(httpGet);
		}  catch (Exception e) {
		}
		
		return response;
	}
	
	public static CloseableHttpResponse postRequest (String url, Header []params) {
		httpClient = getClient();
		if (httpClient == null) return null;

		// Post方法
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeaders(params);
		CloseableHttpResponse response = null;
		
		try {
			response = httpClient.execute(httpPost);
			//System.out.println( "Post request: " + response.getStatusLine() );
		}  catch (Exception e) {
		}
		
		return response;
	}
	
	public static void closeResponse (CloseableHttpResponse response) {
		try {
			if (response != null)
				response.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public static CloseableHttpClient getClient () {
		if (httpClient != null)
			return httpClient;

		SSLContextBuilder builder = new SSLContextBuilder();
		SSLConnectionSocketFactory sslsf = null;
		try {
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy() {
				@Override
				public boolean isTrusted(final X509Certificate[] chain, final String authType)
						throws CertificateException {
					return true;
				}
			});
			sslsf = new SSLConnectionSocketFactory(builder.build());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		HttpHost proxy = new HttpHost(proxyIp, proxyPort);
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

		CloseableHttpClient client = HttpClients.custom().setRoutePlanner(routePlanner).setSSLSocketFactory(sslsf).build();
		return client;
	}

	public static void closeClient () {
		if (httpClient == null)
			return;

		try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void playVideo () {
		ClassLoader classLoader = Train.class.getClassLoader();
		File file = new File(classLoader.getResource("train.wav").getFile());

		SourceDataLine auline = null;
		AudioInputStream audioInputStream = null;
		
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
			AudioFormat format = audioInputStream.getFormat();
			
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);		
			auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
		} catch (Exception e) {
			e.printStackTrace();
		}
	 
        auline.start();
        int nBytesRead = 0;
        final int EXTERNAL_BUFFER_SIZE = 524288;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
        
        try { 
            while (nBytesRead != -1) { 
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0) 
                    auline.write(abData, 0, nBytesRead);
            } 
        } catch (IOException e) { 
            e.printStackTrace();
            return;
        } finally { 
            auline.drain();
            auline.close();
        } 
	}
}