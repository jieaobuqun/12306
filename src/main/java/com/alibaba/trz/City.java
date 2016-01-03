package com.alibaba.trz;

public enum City {
	北京("BJP"), 上海("SHH"), 广州("GZQ"), 深圳("SZQ"), 天津("TJP"), 重庆("CQW"), 
	苏州("SZH"), 成都("CDW"), 武汉("WHN"), 南京("NJH"), 杭州("HZH"), 青岛("QDK"),
	无锡("WXH"), 大连("DLT"), 长沙("CSQ"), 沈阳("SYT"), 宁波("NGH"), 佛山("FSQ"),
	宜昌("YCN");
	
	private final String code;
	
	private City (final String code) {
		this.code = code;
	}
	
	@Override
	public String toString () {
		return this.code;
	}
}
