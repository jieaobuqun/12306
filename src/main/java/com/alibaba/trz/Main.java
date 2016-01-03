package com.alibaba.trz;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		QueryConfig[] config = new QueryConfig[4];
		
		/*设置日期*/
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 1, 4); // 1 月用0表示, 2表示3月
		Date[] dates = { cal.getTime() };
		
		/*设置车次和座位信息*/
		Seat[] seat = {Seat.硬卧};
		Map<String, Seat[]> map = new HashMap<String, Seat[]>();
		map.put("Z257", seat);
		
		/*设置起止车站*/
		config[0] = new QueryConfig(City.上海, City.重庆, map, dates);
		config[1] = new QueryConfig(City.杭州, City.重庆, map, dates);
		config[2] = new QueryConfig(City.杭州, City.宜昌, map, dates);
		config[3] = new QueryConfig(City.上海, City.宜昌, map, dates);
		
		Train train = new Train(config);
		train.refreshTickets();
	}
}
