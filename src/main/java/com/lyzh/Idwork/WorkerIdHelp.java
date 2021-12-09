package com.lyzh.Idwork;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WorkerIdHelp {
    private static ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor();

    private static String WORKER_ID_PREFIX = "workId";
    private static  Integer WORKER_ID = 0;

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    InetUtils inetUtils;
    @Value("${workerId.maxWorkId:255}") //默认255
    private Integer maxWorkId;
    @Value("${workerId.appName:${spring.application.name}}") //默认1
    private String appName;
    @PostConstruct
    public synchronized void init() {
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        String ip = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        String redisKeyPrefix = WORKER_ID_PREFIX + "_" + appName + "_";
        Boolean isSuccess = redisTemplate.opsForValue().setIfAbsent(redisKeyPrefix+WORKER_ID, ip, 30, TimeUnit.SECONDS);
        while (!isSuccess && WORKER_ID < maxWorkId) {
            WORKER_ID++;
            isSuccess = redisTemplate.opsForValue().setIfAbsent(redisKeyPrefix+WORKER_ID, ip, 30, TimeUnit.SECONDS);
        }
        if (!isSuccess) {
            throw new RuntimeException("获取workid失败");
        }
        log.info("获取workid成功,workid:{}", WORKER_ID);
        TIMER.scheduleAtFixedRate(() ->
                //直接用set 防止redis的过期情况
                redisTemplate.opsForValue().set(redisKeyPrefix + WORKER_ID, ip, 30, TimeUnit.SECONDS), 0, 10, TimeUnit.SECONDS
        );
    }

    public Integer getWorkerId() {
        return WORKER_ID;
    }
}