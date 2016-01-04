package com.alibaba.trz;

public class Main {

	public static void main(String[] args) {
		/*QueryConfig[] config = Action.setConfig();
		Train train = new Train(config);
		
		train.refreshTickets();
		Train.closeClient();*/
		View view = new View("登录", Constant.baseUrl + 
				"passcodeNew/getPassCodeNew?module=login&rand=sjrand");
		view.captcha();
	}
}
