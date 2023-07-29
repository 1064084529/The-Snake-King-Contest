package edu.zjut.yzj.ai_battle_platform.backend.utils;

import com.aliyun.tea.TeaException;


import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.EventListener;

@Component
public class MsgUtil {

    private Environment env ;

    public MsgUtil(Environment env) {
        this.env = env;
        accessKeyId= env.getProperty("aliyuninfo.accessKeyId");
        accessKsySerect = env.getProperty("aliyuninfo.accessKsySerect");
        sigName = env.getProperty("aliyuninfo.signature");
        templateCode = env.getProperty("aliyuninfo.templateCode");
        try {
            client = createClient(accessKeyId, accessKsySerect);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    Client client;



    private  String accessKeyId;

    private  String accessKsySerect;

    private  String sigName;

    private  String templateCode;

    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public  Client createClient(String accessKeyId, String accessKeySecret) throws Exception {

        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new com.aliyun.dysmsapi20170525.Client(config);
    }

    public  void sendMsg(String phone,String code) throws Exception {

        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(sigName)
                .setTemplateCode(templateCode)
                .setTemplateParam("{\"code\":" + code+"}");

        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();

        try {
            // 复制代码运行请自行打印 API 的返回值
            client.sendSmsWithOptions(sendSmsRequest, runtime);
            System.out.println("phone= " + phone);
            System.out.println("code= " + code);
            System.out.println("已经发送短信");
        } catch (TeaException error) {
            // 如有需要，请打印 error
            error.printStackTrace();
        } catch (Exception _error) {
            // 如有需要，请打印 error
            _error.printStackTrace();
        }
    }



}
