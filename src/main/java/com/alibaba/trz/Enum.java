package com.alibaba.trz;

public class Enum {
    public enum Seat {
        高级软卧("gr"), 占位1("zw1"), 软卧("rw"), 占位2("zw2"), 占位3("zw3"), 无座("wz"), 占位4("zw4"),
        硬卧("yw"), 硬座("yz"), 二等座("ze"), 一等座("zy"), 商务座("swz"), 动卧("dw"), 软座("rz"), 其他("qt");

        private final String text;

        Seat (final String text) {
            this.text = text;
        }

        @Override
        public String toString (){
            return this.text;
        }
    }

    public enum City {
        北京("BJP"), 上海("SHH"), 广州("GZQ"), 深圳("SZQ"), 天津("TJP"), 重庆("CQW"),
        苏州("SZH"), 成都("CDW"), 武汉("WHN"), 南京("NJH"), 杭州("HZH"), 青岛("QDK"),
        无锡("WXH"), 大连("DLT"), 长沙("CSQ"), 沈阳("SYT"), 宁波("NGH"), 佛山("FSQ"),
        汉口("HKN"), 宜昌("HAN"), 厦门("XMS"), 荣昌("RCW");

        private final String code;

        City (final String code) {
            this.code = code;
        }

        @Override
        public String toString () {
            return this.code;
        }
    }
}
