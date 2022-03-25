package com.redis;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;

public class LockDelay implements Runnable {
    String key = "lock";
    String value;
    boolean isOk = true;
    private RedisCommands redisCommands;

    public LockDelay(String value, RedisCommands redisCommands) {
        this.value = value;
        this.redisCommands = redisCommands;
    }

    @Override
    public void run() {
        while (isOk) {
            try {
                System.out.println("延时线程 开始");
                Thread.sleep(10000);
                //KEYS 参数
                String[] keys = new String[]{key};
                //ARGV 参数
                String[] values = new String[]{value, String.valueOf(30)};
                //这里采用 lua 脚本 “续费锁” ，注意要续费是自己持有的锁， value 值唯一确认现在这把锁是自己持有的
                Object eval = redisCommands.eval("if redis.call('get', KEYS[1]) == ARGV[1] " +
                                "then return redis.call('expire', KEYS[1],ARGV[2]) " +
                                "else return 0 end",
                        ScriptOutputType.INTEGER,
                        keys, values);
                if (Integer.parseInt(eval.toString()) == 1) {
                    System.out.println("延期成功，将锁超时时间重置为 " + 30 + "s");
                } else {
                    isOk = false;
                    System.out.println("延期失败");
                }
                System.out.println("延时线程 结束");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("延时线程结束");
    }

    /**
     * 停止线程
     */
    public void stop() {
        this.isOk = false;
    }
}
