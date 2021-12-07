# workid-help
## 背景

所有雪花算法都需要workerId以及datacenterId进行机器的唯一区分

如







部分雪花算法的workerId默认实现为机器ip的位运算,最典型的实现方式如下

```SQL
public static byte getLastIPAddress() {
        if (LAST_IP != 0) {
            return LAST_IP;
}

        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            byte[] addressByte = inetAddress.getAddress();
            LAST_IP = addressByte[addressByte.length - 1];
} catch (Exception e) {
            throw new RuntimeException("Unknown Host Exception", e);
}

        return LAST_IP;
}
```

但是这种做法也会有问题

1. InetAddress.getLocalHost()无法获取容器内/多网卡的ip地址

2. 如果项目部署在同一个机器上,ip获取就会重复

基于以上,本项目解决的是从**redis获取不重复的workid**

## 实现原理

自己看源码

## 快速接入

> 只基于spring-cloud测试过,spring-boot理论支持,未测试



### 添加pom

```SQL
        <dependency>
            <groupId>com.lyzh</groupId>
            <artifactId>workerid-help</artifactId>
            <version>1.0.5</version>
        </dependency>
```

### 添加配置扫描(已配置忽略)

```SQL
@SpringBootApplication(scanBasePackages = {"com.lyzh.*"})
```

### 添加配置参数

workerId下的参数可以不配置,保持默认值

使用spring的redisTemplate,所以一定要配置redis参数

```SQL
workerId:
  maxWorkId: 31 #默认值255
  appName: score-new #默认值取spring.application.name
spring:
  redis:
    cluster:
      nodes:xxx,xxxx,xxx

```

### 替换原始wokerid

```SQL
    @Autowired
WorkerIdHelp workerIdHelp;
    
    workerIdHelp.getWorkerId()
```

![](https://secure1.wostatic.cn/static/ngyDba1PKebzdVrntqkFuT/image.png)