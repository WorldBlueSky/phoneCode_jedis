package com.study;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JedisUtils {
    private static JedisPool jp;

    static {
        // 加载Jedis连接池配置参数
        InputStream is = JedisUtils.class.getResourceAsStream("/jedisConf.properties");
        Properties prop = new Properties();
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String host = prop.getProperty("jedis.host");
        int port = Integer.parseInt(prop.getProperty("jedis.port"));
        int maxTotal = Integer.parseInt(prop.getProperty("jedis.maxTotal"));
        int maxIdle = Integer.parseInt(prop.getProperty("jedis.maxIdle"));

        // 设置Jedis连接池参数
        JedisPoolConfig jpc = new JedisPoolConfig();
        jpc.setMaxTotal(maxTotal);
        jpc.setMaxIdle(maxIdle);

        // 初始化Jedis连接池
        jp = new JedisPool(jpc, host, port);
    }

    // 从Jedis连接池获取连接
    public static Jedis getJedis() {
        Jedis jedis = jp.getResource();
        jedis.auth("123456");
        return jedis;
    }
}