package com.alibaba.trz;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

public class Action {
	/* 设置配置信息 */
	public static QueryConfig[] setConfig () {
		QueryConfig[] config = new QueryConfig[4];
		
		/*设置日期*/
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 1, 4); // 1 月用0表示, 2表示3月
		Date[] dates = { cal.getTime() };
		
		/*设置车次和座位信息*/
		Seat[] seat = {Seat.硬卧};
		Map<String, Seat[]> map = new HashMap<String, Seat[]>();
		map.put("Z257", seat);
		
		Map<String, Seat[]> map2 = new HashMap<String, Seat[]>();
		map2.put("Z47", seat);
		map2.put("Z257", seat);
		
		/*设置起止车站*/
		config[0] = new QueryConfig(City.上海, City.重庆, map, dates);
		config[1] = new QueryConfig(City.杭州, City.重庆, map, dates);
		config[2] = new QueryConfig(City.杭州, City.宜昌, map, dates);
		config[3] = new QueryConfig(City.杭州, City.武汉, map2, dates);
		
		return config;
	}
	
	/* 模拟登录 */
	public static void login () {
		String loginUrl = Constant.baseUrl + "login/loginAysnSuggest";
		String username = "jieaobuqun";
		String password = "";
		String randCode = "";
		
		Header []params = new BasicHeader[3];
		params[0] = new BasicHeader("username", username);
		params[1] = new BasicHeader("password", password);
		params[2] = new BasicHeader("randCode", randCode);
		
		boolean login = false;
		CloseableHttpResponse response = null;
		
		while (!login) {
			response = Train.postRequest(loginUrl, params);
			login = response != null && response.getStatusLine().getStatusCode() == 200;
		}
		
		try {
			System.out.println( EntityUtils.toString( response.getEntity() ) );
		} catch (Exception e) {
		}
	}
	
	/* 获取图片 */
	public static File getImage (String url) {
		File file = new File(Constant.resourcePath + "image.jpg");
		CloseableHttpResponse response = Train.getRequest(url);
		
		try {
			byte []data = EntityUtils.toByteArray( response.getEntity() );
			FileUtils.writeByteArrayToFile(file, data);
			response.close();
		} catch (Exception e) {
			System.out.println("Get file error!");
		}
		
		return file;	
	}
}
