package com.alibaba.trz;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class P2P {
	/**
	 * 获取易贷网投资信息，有可投资项目，立即提醒
	 */
	public static boolean getEdaiStatus() {
		String url = "http://www.yidai.com/invest/index.html";
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			Elements items = doc.select(".invest-table .item");
			for (Element item : items) {
				Element li = item.children().last();
				String status = li.select(".ui-btn").html();
				if (!status.equals("还款中") && !status.equals("已满标")) {
					System.out.println("易贷有投资啦！");
					return true;
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		return false;
	}
}
