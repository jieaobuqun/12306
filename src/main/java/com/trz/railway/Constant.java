package com.trz.railway;

public class Constant {

	/** UTF8编码 */
	public static final String UTF8_ENCODE 			    = "UTF-8";

    /** 12306登录URL */
    public static final String LOGIN_URL                = "https://kyfw.12306.cn/otn/resources/login.html";
    /** 12306验证码URL */
    public static final String CAPTCHA_URL              = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand";
    /** 12306验证码校验URL */
    public static final String CAPTCHA_CHECK_URL        = "https://kyfw.12306.cn/passport/captcha/captcha-check";
    /** 12306登录提交URL */
    public static final String LOGIN_SUBMIT_URL         = "https://kyfw.12306.cn/passport/web/login";
    /** 12306登录AUTH URL */
    public static final String LOGIN_AUTH_URL           = "https://kyfw.12306.cn/passport/web/auth/uamtk";
    /** 12306登录AUTH CLIENT URL */
    public static final String LOGIN_AUTH_CLIENT_URL    = "https://kyfw.12306.cn/otn/uamauthclient";
    /** 12306提交校验是否登录 */
    public static final String SUBMIT_CHECK_USER_URL    = "https://kyfw.12306.cn/otn/login/checkUser";
    /** 12306提交获取加密KEY */
    public static final String SUBMIT_REQUEST_URL       = "https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest";
    /** 12306提交信息页面 */
    public static final String SUBMIT_INIT_URL          = "https://kyfw.12306.cn/otn/confirmPassenger/initDc";
    /** 12306获取乘客信息 */
    public static final String GET_PASSENGER_URL        = "https://kyfw.12306.cn/otn/confirmPassenger/getPassengerDTOs";
    /** 12306获取最新验证码, --->暂时不用 */
    public static final String GET_PASSCODE_NEW_URL     = "https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew?module=passenger&rand=randp";
    /** 12306检查订单信息 */
    public static final String CHECK_ORDER_URL          = "https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo";
    /** 12306获取排队数量 */
    public static final String GET_QUEUE_COUNT_URL      = "https://kyfw.12306.cn/otn/confirmPassenger/getQueueCount";
    /** 12306确认排队 */
    public static final String CONFIRM_QUEUE_URL        = "https://kyfw.12306.cn/otn/confirmPassenger/confirmSingleForQueue";
    /** 12306获取排队时间 */
    public static final String ORDER_WAIT_TIME_URL      = "https://kyfw.12306.cn/otn/confirmPassenger/queryOrderWaitTime?tourFlag=dc&_json_att=&REPEAT_SUBMIT_TOKEN=";
    /** 12306排队结果 */
    public static final String RESULT_ORDER_QUEUE_URL   = "https://kyfw.12306.cn/otn/confirmPassenger/resultOrderForDcQueue";
    /** 12306订单页面 */
    public static final String PAY_ORDER_INIT_URL   = "https://kyfw.12306.cn/otn//payOrder/init";
}
