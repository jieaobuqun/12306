package com.alibaba.trz;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

public class Action {
	/* 设置配置信息 */
	public static Config[] setConfig () {
		Config[] config = new Config[2];
		
		/*设置日期*/
		Calendar cal = Calendar.getInstance();
		Date[] toDates = new Date[2];
		Date[] backDates = new Date[3];
		
		/*去程*/
		cal.set(2016, 8, 30); // 1 月用0表示, 2表示3月		
		toDates[0] = cal.getTime();
		cal.set(2016, 8, 29);
		toDates[1] = cal.getTime();
		
		/*返回*/
		cal.set(2016, 9, 7);
		backDates[0] = cal.getTime();
		cal.set(2016, 9, 8);
		backDates[1] = cal.getTime();
		cal.set(2016, 9, 9);
		backDates[2] = cal.getTime();	
		
		/*设置车次和座位信息*/
		Seat[] seat = {Seat.硬卧};
		Map<String, Seat[]> toTrains = new HashMap<String, Seat[]>();
		Map<String, Seat[]> backTrains = new HashMap<String, Seat[]>();
		
		toTrains.put("Z257", seat);
		backTrains.put("Z258", seat);
		
		/*设置起止车站*/
		config[0] = new Config(City.杭州, City.宜昌, toTrains, toDates);
		config[1] = new Config(City.宜昌, City.杭州, backTrains, backDates);
		
		return config;
	}
	
	/* 模拟登录 */
	public static void login () {
		Bean bean = new Bean();
		/*预获取Cookie*/
		String loginHtm = Constant.baseUrl + "login/init";
		CloseableHttpResponse response = Action.request(loginHtm, null);
		String setCookie = response.getFirstHeader("Set-Cookie").getValue();
		Train.closeResponse(response);
		
		String []cookies = setCookie.split("[;\\s]+");
		for (String cookie : cookies){
			String []pair = cookie.split("=");
			bean.updateCookie(pair[0], pair[1]);
		}
		
		/*用户识别验证码*/
		String captchauUrl = Constant.baseUrl + 
				"passcodeNew/getPassCodeNew?module=login&rand=sjrand";
		View view = new View("登录", captchauUrl, bean);
		view.captcha();
		
		try {
			Thread.sleep(1000);
			synchronized (bean) {
				bean.wait();
			}
		} catch (Exception e) {
		}
	
		/*请求登录*/
		String loginUrl = Constant.baseUrl + "login/loginAysnSuggest";
		String username = "jieaobuqun";
		String password = "tian7124";
		String randCode = bean.getRandCode();
		System.out.println(randCode);
		
		Header []params = new BasicHeader[3];
		params[0] = new BasicHeader("username", username);
		params[1] = new BasicHeader("password", password);
		params[2] = new BasicHeader("randCode", randCode);
		
		response = Action.request(loginUrl, params);
		
		try {
			Header []headers = response.getAllHeaders();
			for (Header head : headers) {
				System.out.println(head.getName() + ":" + head.getValue());
			}
			System.out.println( EntityUtils.toString( response.getEntity() ) );
		} catch (Exception e) {
		}
		
		Train.closeResponse(response);
	}
	
	/* 设置Cookie */
	public static String setCookie () {
		String cookie = "";
		Header[] headers = new Header[5];
		headers[0] = new BasicHeader("BIGipServerotn", "686817802.38945.0000");
		headers[1] = new BasicHeader("JSESSIONID", 
				"0A02F02898FA4DB242EAA78FFBE204D1535FE58D2D");
		headers[2] = new BasicHeader("current_captcha_type", 
				"Z");
		
		
		return cookie;
	}
	
	/* 获取图片 */
	public static BufferedImage getImage (String url) {
		//File file = new File(Constant.resourcePath + "image.jpg");
		CloseableHttpResponse response = Train.getRequest(url);
		BufferedImage myImage = null;
		
		try {
			//byte []data = EntityUtils.toByteArray( response.getEntity() );
			myImage = ImageIO.read(response.getEntity().getContent());
			//FileUtils.writeByteArrayToFile(file, data);
			response.close();
		} catch (Exception e) {
			System.out.println("Get file error!");
		}
		
		return myImage;	
	}
	
	/*获取图片对象*/
	public static BufferedImage getImage () {
		BufferedImage icon = null;
		try {
			icon = ImageIO.read(new File(Constant.resourcePath + "icon.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return icon;
	}
	
	/*返回请求，直到成功*/
	public static CloseableHttpResponse request(String url, Header []params) {
		boolean success = false;
		CloseableHttpResponse response = null;
		
		while (!success) {
			response = params == null ? Train.getRequest(url) : 
										Train.postRequest(url, params);
			success = response != null && response.getStatusLine().getStatusCode() == 200;
		}
		
		return response;
	}
}
