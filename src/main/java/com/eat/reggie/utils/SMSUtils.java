package com.eat.reggie.utils;

import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;

import java.util.HashMap;
import java.util.Set;

public class SMSUtils {

    /**
     * 发送短信工具类
     * @param phoneNum 用户手机号
     * @param datas 提供验证码及失效时间（分）
     */
    public static void sendMessage(String phoneNum, String[] datas){
        //生产环境请求地址：app.cloopen.com
        String serverIp = "app.cloopen.com";
        //请求端口
        String serverPort = "8883";
        //主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
        String accountSId = "2c94811c88bf35030189af1064cc3a83";
        String accountToken = "68b3d6aec8304f218cdc3afa6822ce7c";
        //请使用管理控制台中已创建应用的APPID
        String appId = "2c94811c88bf35030189af10661c3a8a";
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init(serverIp, serverPort);
        sdk.setAccount(accountSId, accountToken);
        sdk.setAppId(appId);
        sdk.setBodyType(BodyType.Type_JSON);
        String templateId= "1";
        HashMap<String, Object> result = sdk.sendTemplateSMS(phoneNum,templateId,datas);
        if("000000".equals(result.get("statusCode"))){
            //正常返回输出data包体信息（map）
            HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();
            for(String key:keySet){
                Object object = data.get(key);
                System.out.println(key +" = "+object);
            }
        }else{
            //异常返回输出错误码和错误信息
            System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
        }
    }
}
