package com.trz.railway;

public class Constant {

	/** UTF8编码 */
	public static final String UTF8_ENCODE 			    = "UTF-8";


	/******************************************** 12306请求URL, 按请求顺序 *************************************/

    /** 12306登录URL */
    public static final String LOGIN_URL                = "https://kyfw.12306.cn/otn/login/init";
    /** 12306登录AUTH URL */
    public static final String LOGIN_AUTH_URL           = "https://kyfw.12306.cn/passport/web/auth/uamtk";
    /** 12306登录AUTH CLIENT URL */
    public static final String LOGIN_AUTH_CLIENT_URL    = "https://kyfw.12306.cn/otn/uamauthclient";
    /** 12306登录GET_JS URL */
    public static final String LOGIN_GET_JS_URL         = "https://kyfw.12306.cn/otn/HttpZF/GetJS";
    /** 12306登录LOG_DEVICE URL */
    public static final String LOGIN_LOG_DEVICE_URL     = "https://kyfw.12306.cn/otn/HttpZF/logdevice";
    /** 12306验证码URL */
    public static final String CAPTCHA_URL              = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand";
    /** 12306验证码校验URL */
    public static final String CAPTCHA_CHECK_URL        = "https://kyfw.12306.cn/passport/captcha/captcha-check";
    /** 12306登录提交URL */
    public static final String LOGIN_SUBMIT_URL         = "https://kyfw.12306.cn/passport/web/login";
    /** 12306初始化URL */
    public static final String INIT_URL                 = "https://kyfw.12306.cn/otn/leftTicket/init";
    /** 12306提交校验是否登录 */
    public static final String SUBMIT_CHECK_USER_URL    = "https://kyfw.12306.cn/otn/login/checkUser";
    /** 12306提交获取加密KEY */
    public static final String SUBMIT_REQUEST_URL       = "https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest";
    /** 12306提交信息页面 */
    public static final String SUBMIT_INIT_URL          = "https://kyfw.12306.cn/otn/confirmPassenger/initDc";
    /** 12306获取乘客信息*/



}
