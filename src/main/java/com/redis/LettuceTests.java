package com.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.UUID;

/**
 * 利用redis实现分布式锁
 *
 * @author
 * @date
 */
public class LettuceTests {
    private static volatile boolean isOk = true;
    public static void main(String[] args) {
//        RedisClient redisClient = RedisClient.create("redis://1234@127.0.0.1");
//        StatefulRedisConnection<String, String> connect = redisClient.connect();
//        RedisCommands<String, String> sync = connect.sync();
//        sync.setex("t",10,"test");
//        sync.set("k1", "hellolettuce");
//        String v = sync.get("k1");
//        System.out.println(v);
//
//        List<String> strings = sync.keys("*");
//        System.out.println("objects = " + strings);
//
//        String getdel = null;
//        System.out.println("getdel = " + getdel);


        RedisClient redisClient = RedisClient.create("redis://1234@127.0.0.1");
        StatefulRedisConnection<String, String> connect = redisClient.connect();
        RedisCommands<String, String> redisCommands = connect.sync();
        //锁key
        String key = "lock";
        //唯一标识，释放锁时确保是自己持有的锁
        String value = UUID.randomUUID().toString();
        //锁续期标示
        try {
            if ("OK".equals(redisCommands.set(key, value, SetArgs.Builder.nx().ex(30)))) {
                //do sth
                LockDelay lockDelay = new LockDelay(value, redisCommands);
                Thread lockDelayThread = new Thread(lockDelay);
                lockDelayThread.setDaemon(true);
                lockDelayThread.start();
                System.out.println("主线程sleep 50s");
                lockDelay.stop();

                Thread.sleep(25000);
                //lockDelayThread.interrupt();
            } else {
                System.out.println("获取锁失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放锁
            Object eval = redisCommands.eval("if redis.call('get', KEYS[1]) == ARGV[1] " +
                            "then return redis.call('del', KEYS[1]) " +
                            "else return 0 end",
                    ScriptOutputType.INTEGER,
                    new String[]{key}, value);
            System.out.println("eval = " + eval);
        }
    }
}
