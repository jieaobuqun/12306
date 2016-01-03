package com.alibaba.trz;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

public class View extends JPanel{

	private String username = "jieaobuqun";

	private String password = "";
	
	private String baseUrl = "https://kyfw.12306.cn/otn/";
	
}
