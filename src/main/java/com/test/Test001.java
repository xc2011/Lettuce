package com.test;

public class Test001 {

    private static /*volatile*/ boolean stop = false;

    //    private static final Logger logger = LoggerFactory.getLogger( "Test001");
    public static void main(String[] args) {
        //创建一个线程，并sleep一会儿然后再将stop的值修改为true
        Thread tl = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stop = true;
//            logger.debug("tl线程已成功修改stop的值为true. ");
            System.out.println("tl线程已成功修改stop的值为true");
        });

        //创建一个线程，并sleep一会儿然后再去读取stop的值，这里能读到true吗？
        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("t2线程读取到的stop变量的值为：{}");
            System.out.println(stop);
        }); 
        //分别开启两个线程，七2线程在门线程修改后再去读取
        tl.start();
        t2.start();
        //主线程调用f。。，主线程能感知到stop变量的值发生变化从而退出循环正常结束吗
        loop();
    }

    private static void loop() {
        int i = 0;
        while (!stop) {
            i++;
            System.out.println("i = " + i);
        }
//        get().debug("主线程读取到stop被修改为true退出循环i= {}", i);
    }
}
