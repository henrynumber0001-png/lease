package com.nocompanyname.lease.common.cache;


import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@EnableCaching //开启 @Cacheable、@CacheEvict、@CachePut 等注解的功能
@Configuration
public class RedisCacheConfig {

    /*
    开启缓存功能
    默认缓存30分钟
    不同业务缓存使用不同TTL
    key用字符串序列化
    value用JSON序列化
    最终创建RedisCacheManager
     */

    //CacheManager 是真正管理缓存的核心对象
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) { //RedisConnectionFactory = Redis连接池，所有的读写删Redis操作，都要通过它连接Redis

        //创建默认缓存配置（默认缓存30分钟）
        RedisCacheConfiguration defaultConfig = createCacheConfig(Duration.ofMinutes(30));

        //每个缓存空间自己的配置（不同业务缓存使用不同TTL）
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                CacheNames.ADMIN_ROOM_DETAIL, createCacheConfig(Duration.ofMinutes(20)),
                CacheNames.ADMIN_APARTMENT_DETAIL, createCacheConfig(Duration.ofMinutes(20)),

                CacheNames.APP_ROOM_DETAIL, createCacheConfig(Duration.ofMinutes(60)),
                CacheNames.APP_APARTMENT_DETAIL, createCacheConfig(Duration.ofMinutes(60)),
                CacheNames.APP_REGION_LIST, createCacheConfig(Duration.ofHours(12)),
                CacheNames.APP_LABEL_LIST, createCacheConfig(Duration.ofHours(2)),
                CacheNames.APP_FACILITY_LIST, createCacheConfig(Duration.ofHours(2))
        );

        //RedisCacheManager 是 Redis缓存总管
        //Spring在执行 @Cacheable, @CachePut, @CacheEvict 等缓存操作时，真正调用的就是 RedisCacheManager
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig) //默认缓存规则
                .withInitialCacheConfigurations(cacheConfigurations) //特殊缓存规则
                .transactionAware() //缓存操作参与Spring事务（失败回滚）
                .build(); //生成RedisCacheManager给到IoC容器
    }

    //RedisCacheConfiguration 用于配置 RedisCacheManager，最终执行缓存管理的是 RedisCacheManager
    //RedisCacheConfiguration 里面存的是规则说明书，用这份说明书，创建缓存管理器
    private RedisCacheConfiguration createCacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                )
                .disableCachingNullValues();
    }
}

/*
@Cacheable 的语义是：缓存这个方法的返回值。

Spring Cache 实际做的是一层代理，大致逻辑可以理解成：
public RoomDetailVo getDetailById(Long id) {
    String cacheName = "admin:room:detail";
    Object key = id;

    // 1. 先去 Redis 查
    Object cacheValue = cacheManager
            .getCache(cacheName)
            .get(key);

    if (cacheValue != null) {
        return (RoomDetailVo) cacheValue;
    }

    // 2. Redis 没有，才执行你的原方法
    RoomDetailVo result = target.getDetailById(id);

    // 3. 把原方法的返回值 result 放进 Redis
    cacheManager
            .getCache(cacheName)
            .put(key, result);

    // 4. 返回给调用方
    return result;
}

所以 RedisCacheManager 不需要你告诉它“value 是 RoomDetailVo”。它拿到的是方法执行后的返回对象

RedisCacheManager 真正负责的是：
1.根据 cacheNames 找到对应的缓存区域。
2.根据 key 生成缓存 key。
3.调用 Redis 查询这个 key。
4.未命中时，把方法返回值写入 Redis。
5.写入时使用你配置的序列化器，比如 GenericJackson2JsonRedisSerializer。

getDetailById(1)
        ↓
方法返回 RoomDetailVo
        ↓
Spring Cache 拿到返回值
        ↓
RedisCacheManager 准备写缓存
        ↓
GenericJackson2JsonRedisSerializer 把 RoomDetailVo 转成 JSON
        ↓
Redis 存储
 */

