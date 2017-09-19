package com.trz.railway;

public class Constant {

	/** UTF8编码 */
	public static final String UTF8_ENCODE 			    = "UTF-8";
    /** 12306公共URL前缀 */
    public static final String BASE_DOMAIN              = ".12306.cn";
    /** 12306登录URL */
    public static final String LOGIN_URL                = "https://kyfw.12306.cn/otn/login/init";
    /** 12306登录提交URL */
    public static final String LOGIN_SUBMIT_URL         = "https://kyfw.12306.cn/passport/web/login";
	/** 12306初始化URL */
    public static final String INIT_URL                 = "https://kyfw.12306.cn/otn/leftTicket/init";
    /** 12306验证码URL */
    public static final String CAPTCHA_URL              = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand";
    /** 12306验证码校验URL */
    public static final String CAPTCHA_CHECK_URL        = "https://kyfw.12306.cn/passport/captcha/captcha-check";

}
