package com.alibaba.trz;

public class Main {

	public static void main(String[] args) {
		Config[] config = Action.setConfig();
		Train train = new Train(config);
		
		train.refreshTickets();
		Train.closeClient();
		//Action.login();
	}
}
