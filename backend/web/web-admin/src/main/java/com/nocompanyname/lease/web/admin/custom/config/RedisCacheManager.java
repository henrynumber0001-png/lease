package com.nocompanyname.lease.web.admin.custom.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

//@Configuration
//public class RedisCacheManager { //配置 Spring Cache 使用 Redis 作为缓存时，key 和 value 如何序列化，以及缓存默认过期时间
//    @Bean
//    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {//ObjectMapper 是 Jackson 提供的 JSON 工具，主要负责 Java 对象和 JSON 之间的转换。
//
//        //创建一个 Redis value 的序列化器
//        //把 Java 对象转换成 JSON 存进 Redis
//        //从 Redis 读取时，再把 JSON 转回 Java 对象
//        GenericJackson2JsonRedisSerializer jsonSerializer =
//                new GenericJackson2JsonRedisSerializer(objectMapper);
//
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(30))
//                .serializeKeysWith( //开始配置 Redis 中 key 的序列化方式，得到 Key 的序列化器
//                        //指定 key 使用 StringRedisSerializer (也就是说，Redis 里的 key 会以普通字符串形式保存，而不是一堆看不懂的二进制数据)
//                        RedisSerializationContext.SerializationPair.fromSerializer(
//                                new StringRedisSerializer()
//                        )
//                )
//                .serializeValuesWith( //开始配置 Redis 中 value 的序列化方式，得到 Value 的序列化器
//                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
//                )
//                .disableCachingNullValues(); //表示不缓存 null 值
//    }
//    //最终获得 Redis 缓存配置对象（RedisCacheConfiguration）
//}
