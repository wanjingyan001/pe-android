package com.sogukj.pe.service;

/**
 * Created by fei on 16/7/6.
 */

/**
 * Created by fei on 16/7/6.
 */
public enum PayloadError {
    paramsError,       //参数错误
    paramsMissing,     //缺少参数
    paramsLengthError, //参数长度错误
    notFound,          //没找到
    unauthorized,      //token无效
    smsError,          //短信服务错误
    dzhError,          //大智慧接口错误
    dzhEmptyKlineError,//大智慧kline为空错误
    effectError,       //全网影响力接口错误
    hylandaError,      //海量响应错误
    illegalActionType, //非法操作类型
    illegalECode,      //非法外码
    shouldNotBeHere,   //不该到这里来
    systemError,       //系统错误
    undefinedUnionid,  //微信用户没有unionid
    illegalLevel,      //用户等级不符合要求
    illegalInvitation, //邀请码无效
    priceInvalid,      //价格不正确
}