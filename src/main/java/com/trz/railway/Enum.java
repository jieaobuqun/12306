package com.trz.railway;

@SuppressWarnings("ALL")
public class Enum {

    /**
     * 座位枚举
     */
    @SuppressWarnings("NonAsciiCharacters")
    public enum Seat {
        高级软卧("gr", "-1"), 占位1("zw1", "-1"), 软卧("rw", "-1"), 占位2("zw2", "-1"), 占位3("zw3", "-1"),
        无座("wz", "-1"), 占位4("zw4", "-1"), 硬卧("yw", "-1"), 硬座("yz", "-1"), 二等座("ze", "O"),
        一等座("zy", "M"), 商务座("swz", "9"), 动卧("dw", "-1"), 软座("rz", "-1"), 其他("qt", "-1");

        private final String text;

        private final String seatType;

        Seat (final String text, final String seatType) {
            this.text = text;
            this.seatType = seatType;
        }

        public String getText() {
            return text;
        }

        public String getSeatType() {
            return seatType;
        }
    }

    /**
     * 城市枚举
     */
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
