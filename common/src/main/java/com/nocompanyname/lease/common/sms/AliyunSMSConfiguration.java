package com.nocompanyname.lease.common.sms;


import com.aliyun.teaopenapi.models.Config;
import com.aliyun.dysmsapi20170525.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliyunSMSProperties.class)
@ConditionalOnProperty(name = "aliyun.sms.endpoint")
public class AliyunSMSConfiguration {

    @Bean
    public Client createClient(AliyunSMSProperties properties) throws Exception {
         Config config = new Config().setAccessKeyId(properties.getAccessKeyId())
                .setAccessKeySecret(properties.getAccessKeySecret())
                .setEndpoint(properties.getEndpoint());
        return new Client(config);
    }

}
