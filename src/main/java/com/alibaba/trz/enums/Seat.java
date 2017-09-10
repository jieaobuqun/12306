package com.alibaba.trz.enums;

public enum Seat {
	商务座("swz"), 特等座("tz"), 一等座("zy"), 二等座("ze"), 高级软卧("gr"), 
	软卧("rw"), 硬卧("yw"), 软座("rz"), 硬座("yz"), 无座("wz");
	
	private final String text;
	
	private Seat (final String text) {
		this.text =text;
	}
	
	@Override
	public String toString (){
		return this.text;
	}
}
