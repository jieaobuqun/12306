package com.trz.railway;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.trz.railway.Enum.City;
import com.trz.railway.Enum.Seat;

public class TrainConfig {
	
	private String []url;
	
	private City fromCity;
	
	private City toCity;
	
	private Map<String, Seat[]> trainSeatsMap;
	
	public TrainConfig(City fromCity, City toCity, Map<String, Seat[]> trainSeatsMap,
					   Date[] dates) {
		this.fromCity = fromCity;
		this.toCity = toCity;
		this.trainSeatsMap = trainSeatsMap;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		url = new String[dates.length];

		String baseUrl = "https://kyfw.12306.cn/otn/leftTicket/queryX";
		String dateParam = "leftTicketDTO.train_date";
		String fromCityParam = "leftTicketDTO.from_station";
		String toCityParam = "leftTicketDTO.to_station";
		String restParam = "purpose_codes=ADULT";
		for (int i = 0; i < url.length; ++i){
			url[i] = baseUrl + '?' + dateParam + '=' + dateFormat.format(dates[i]) + '&'
					 + fromCityParam + '=' + this.fromCity + '&'
					 + toCityParam + '=' + this.toCity + '&' + restParam;
			
			//System.out.println(url[i]);
		}
	}
	
	public int getTrainCount () {
		return trainSeatsMap.size();
	}
	
	public Seat[] getSeats (String train) {
		return trainSeatsMap.get(train);
	}
	
	public List<String> getAbsentTrain(List<String> trains) {
		List<String> result = new LinkedList<>();
	    for ( Map.Entry<String, Seat[]> pair : trainSeatsMap.entrySet() ) {
	        if ( trains.indexOf( pair.getKey() ) == -1 )
	        	result.add(pair.getKey());
	    }
		
		return result;
	}
	
	public String[] getUrls () {
		return url;
	}
	
	public City getFromCity () {
		return fromCity;
	}
	
	public City getToCity () {
		return toCity;
	}
}
