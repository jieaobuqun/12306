package com.alibaba.trz;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Train {
	
	private QueryConfig []config;
	
	private int trainIndex = 0;
	
	private static CloseableHttpClient httpClient;
	
	public Train (QueryConfig []config) {
		this.config = config;
	}

	public void refreshTickets () {
		int num = 0;
		boolean hasTicket = false;
		while (!hasTicket) {
		    ++num;
			for (QueryConfig conf : config) {
				String []urls = conf.getUrls();
				for (String url : urls) {
					hasTicket = request(num, url, conf);
					if (hasTicket) return;
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}	
			}
		}
	}

	public boolean request(int num, String url, QueryConfig conf) {
		httpClient = getClient();
		if (httpClient == null) return true;

		// Get方法
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = null;
		
		// 用于输出显示更加规范
		final int screenSize = 28;
		// 请求多少次显示一次HTTP状态码
		final int timesShow = 10;
		// 请求多少次输出换行，根据以上两个常量来决定
		final int timesNewLine = screenSize / config.length * timesShow;
		// 请求多少次输出一次列车信息
		final int timesTrainInfo = timesNewLine * 4;

		label: try {
			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if ( conf == config[0] && ( (num + 1) % timesNewLine == 0 || 
			                            (num % timesTrainInfo == 0 && trainIndex % 2 == 1) ) ) {
			    trainIndex = 0;
			    System.out.println();
			}
			if ((num + 2) % timesShow == 0)   
				System.out.print(statusCode + "  ");
			if (statusCode != 200) break label;

			HttpEntity entity = response.getEntity();
			if (entity == null) break label;

			String body = EntityUtils.toString(entity);
			// parsing JSON
			JSONObject result = JSONObject.parseObject(body);
			JSONArray trains = result.getJSONArray("data");
			if (trains == null || trains.isEmpty()) break label;

			int trainCount = conf.getTrainCount();
			List<String> trainFound = new LinkedList<String>();
			for (Object obj : trains) {
				JSONObject train = (JSONObject) obj;
				JSONObject info = train.getJSONObject("queryLeftNewDTO");
				String trainName = info.getString("station_train_code");
				Seat[] seats = conf.getSeats(trainName);
				if (seats == null)
					continue;
				
				trainCount--;
				trainFound.add(trainName);
				
				if ((num + 1) % timesTrainInfo == 0) {		
					System.out.print(
							"train: " + trainName + "\t" +
							"date: " + info.getString("start_train_date") + "\t" +
							conf.getFromCity().name() + "-" + conf.getToCity().name() + "  " );
					
					for (Seat seat : seats)  {
						System.out.print(seat.name() + ": " + 
										info.getString(seat + "_num") + " ");
						
						if (seat.name().length() == 2)
						    System.out.print(" ");
					}
					
					if (++trainIndex % 2 == 0)
					    System.out.println();
					else
					    System.out.print("\t\t");
				}
				
				boolean hasTicket = false;
				for (int i = 0; !hasTicket && i < seats.length; ++i){
					if ( !info.getString(seats[i] + "_num").equals("无") && 
						 !info.getString(seats[i] + "_num").equals("--")) {
						System.out.print(
								"\n" + "train: " + trainName +
								" (" + conf.getFromCity().name() + "-" +
										conf.getToCity().name() + ") " +
								" ,date: " + info.getString("start_train_date"));
						System.out.println(", " + seats[i].name() + ": " + 
								info.getString(seats[i] + "_num"));
						hasTicket = true;
						break;
					}	
				}
				
				if (!hasTicket) continue;
				
				for (int i = 0; i < 2; ++i)
					playVideo();
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
			//e.printStackTrace();
		}

		try {
			if (response != null)
				response.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}

		return false;
	}
	
	public static CloseableHttpResponse getRequest (String url) {
		httpClient = getClient();
		if (httpClient == null) return null;

		// Post方法
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = null;
		
		try {
			response = httpClient.execute(httpGet);
			//System.out.println( "Get request: " + response.getStatusLine() );
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

	private static CloseableHttpClient getClient () {
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

		CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslsf).build();
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
		String path = Constant.resourcePath + "train.wav";
		File file = new File(path);

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