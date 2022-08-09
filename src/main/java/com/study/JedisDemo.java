package com.study;

import redis.clients.jedis.Jedis;

public class JedisDemo {

    public static void main(String[] args) {
        Jedis jedis = JedisUtils.getJedis();
        jedis.auth("123456");
        System.out.println(jedis.ping());
    }
}
