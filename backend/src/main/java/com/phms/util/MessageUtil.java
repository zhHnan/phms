package com.phms.util;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.DefaultCredentialProvider;
import com.aliyun.auth.credentials.provider.ICredentialProvider;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * @Author：hnz
 * @Project：backend
 * @name：MessageUtil
 * @Date：2026/2/1 23:51
 * @Filename：MessageUtil
 */
@Component
public class MessageUtil {

    @Value("${aliyun.region-id:cn-beijing}")
    private String regionId;

    @Value("${aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.access-key-secret}")
    private String accessKeySecret;

    public void sendMsg(String phone, String code) {
        Credential credential = com.aliyun.auth.credentials.Credential.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build();

        ICredentialProvider provider = StaticCredentialProvider.create(credential);
        try (AsyncClient client = AsyncClient.builder()
                .region(regionId)
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dypnsapi.aliyuncs.com")
                )
                .build()) {

            SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = SendSmsVerifyCodeRequest.builder()
                    .signName("速通互联验证码")
                    .templateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}")
                    .templateCode("100001")
                    .phoneNumber(phone)
                    .build();

            CompletableFuture<SendSmsVerifyCodeResponse> response = client.sendSmsVerifyCode(sendSmsVerifyCodeRequest);
            SendSmsVerifyCodeResponse resp = response.get();
            System.out.println(new Gson().toJson(resp));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

}
