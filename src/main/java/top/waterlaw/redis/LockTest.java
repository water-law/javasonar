package top.waterlaw.redis;

import redis.clients.jedis.Jedis;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class LockTest {
    static int thread_num = 1000;
    static CountDownLatch downLatch = new CountDownLatch(thread_num);
    static AtomicInteger fail = new AtomicInteger(0);

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.set("num", "0");
        RedisDistributedLock rdl = new RedisDistributedLock("lock", 50);
        for (int i = 0; i < thread_num; i++) {
            new Thread(() -> {
                boolean hasLock = rdl.tryLock();
                if(hasLock) {
                    try {
                        jedis.incr("num"); // 此函数执行时间和锁的过期时间应该要接近
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    finally {
                        rdl.unlock();
                    }
                }
                else {
                    fail.incrementAndGet();
                }
                downLatch.countDown();
            }).start();
        }
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("---------------------------------");
        System.out.println(fail.get());
        try {
            System.out.println(jedis.get("num"));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        System.out.println("---------------------------------");
        jedis.close();
    }
}
