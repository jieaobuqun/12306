package com.trz.railway;


public class Main {

	public static void main(String[] args) throws Exception {
	    /* 模拟登录 */
		Command.login();

        /* 进行刷票 */
        Command.refreshTickets();

        /* 模拟提交 */
        Command.submit();
	}
}
