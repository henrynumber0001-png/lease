package com.nocompanyname.lease.common.minio;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "minio.endpoint")
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) { //在 @Bean 方法下的参数列表中 的参数，会由 Spring 自动从容器中查找并传入。即，不需要先@AutoWired自动装配了。
        //更推荐使用方法参数注入，因为依赖关系更清楚
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}
