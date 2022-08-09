package com.study;

import org.apache.commons.lang3.time.DateUtils;
import redis.clients.jedis.Jedis;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class PhoneCode {
    // 手机验证码功能 jedis实现
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("请输入手机号：");
        String phone = sc.nextLine();

        // 生成手机对应的验证码，同时开始进行次数统计
        int count = verifyCode(phone);
        if(count>2){
            return;
        }

        System.out.println("请输入手机验证码: ");
        String phoneCode = sc.nextLine();

        // 将输入的手机验证码与 redis中生成的验证码进行对比
        getRedisCode(phoneCode,phone);
    }

    //1、生成6位数字验证码
    public static String getCode(){
        String code = "";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code+=random.nextInt(10);// 生成 0-bound 之间的数字，执行字符串拼接操作
        }
        return code;
    }

    // 2、每个手机每天只能发送三次，验证码放到redis中，设置过期时间，2分钟有效
    public static int verifyCode(String phone){
        Jedis jedis = JedisUtils.getJedis();//连接redis

        // 拼接手机次数的key
        String countKey = "VerifyCode:"+phone+":count";
        // 拼接手机验证码的key
        String codeKey = "VerifyCode:"+phone+":code";

        // 每个手机每天只能发送三次
        String count = jedis.get(countKey);


        if(count==null){
            jedis.setex(countKey,(int) getTodayLeftSeconds(),"1");// 给这个键设置过期时间，仅限于当天剩余时间内
        }else if(getTodayLeftSeconds()==86400 || getTodayLeftSeconds()==0){ // 如果剩余的时间为86400或者为0 那么相当于进入了下一天，那么这个键要重新设置为1进行更新
            jedis.setex(countKey,(int) getTodayLeftSeconds(),"1");// 给这个键设置过期时间，仅限于当天剩余时间内
        }else if(Integer.parseInt(count)<=2){
            jedis.incr(countKey);// 发送次数+1
        }else if(Integer.parseInt(count)>=3){
            System.out.println("发送次数达到上限，不能再次发送!");
            jedis.close();
        }

        // 发送的验证发得放到redis中
        jedis.setex(codeKey,120,getCode());// 给这个键设置两分钟过期
        jedis.close();

        return Integer.parseInt(count);

    }

    //获取直到今天结束剩余的秒数
    public static long getTodayLeftSeconds(){
        return 86400 - DateUtils.getFragmentInSeconds(Calendar.getInstance(), Calendar.DATE);
    }

    //3、验证码校验
    public static void getRedisCode(String code,String phone){
       // 从redis中获取 验证码，再进行验证
        Jedis jedis = JedisUtils.getJedis();

        // 拼接手机验证码的key
        String codeKey = "VerifyCode:"+phone+":code";

        String redisCode = jedis.get(codeKey);

        // 判断 用户输入的code 与 redisCode 是否相同
        if(redisCode.equals(code)){
            System.out.println("登陆成功!");
        }else {
            System.out.println("登陆失败!");
        }
        jedis.close();
    }


}
