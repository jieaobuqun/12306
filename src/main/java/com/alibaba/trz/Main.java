package com.alibaba.trz;


public class Main {

	public static void main(String[] args) throws Exception {
		Config[] config = Action.setConfig();
		Train train = new Train(config);

		train.init();
		train.refreshTickets();
		Train.closeClient();

		//Action.login();

	}
}
