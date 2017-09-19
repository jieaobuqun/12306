package com.trz.railway;


public class Main {

	public static void main(String[] args) throws Exception {
	    /* 模拟登录 */
		Command.login();

        /* 进行刷票 */
        TrainConfig[] config = Command.setTrainConfig();
        Train train = new Train(config);

        train.init();
        train.refreshTickets();
        Train.closeClient();
	}
}
