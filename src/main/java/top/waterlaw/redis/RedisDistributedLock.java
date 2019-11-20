package top.waterlaw.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class RedisDistributedLock implements Lock {

    private ThreadLocal<String> exclusiveOwnerThread = new ThreadLocal<>();
    private String lockKey;
    private long internalLockLeaseTime; // 锁过期时间
    private long time = 1000; // 获取锁超时时间

    public RedisDistributedLock(String lockKey, long internalLockLeaseTime) {
        this.lockKey = lockKey;
        this.internalLockLeaseTime = internalLockLeaseTime;
    }

    public void lock() {
        while (!tryLock()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        try {
            return tryLock(time, TimeUnit.MICROSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        Jedis jedis = new Jedis("localhost", 6379);
        String value = "";
        Long start = System.currentTimeMillis();
        // 获取分布式锁
        Thread t = Thread.currentThread();
        exclusiveOwnerThread.set(t.getName());
        SetParams params = SetParams.setParams().nx().px(this.internalLockLeaseTime);
        try {
            for(;;) {
                String lock = jedis.set(this.lockKey, value, params);
                if("OK".equals(lock)) {
                    System.out.println("tryLock SUCCESS "+exclusiveOwnerThread.get());
                    return true;
                }
                long l = System.currentTimeMillis() - start;
                if(l >= time) {
                    System.out.println("tryLock FAILED "+exclusiveOwnerThread.get());
                    return false;
                }
                Thread.sleep(100);
            }
        }finally {
            jedis.close();
        }
    }

    public void unlock() {
        String script =
                "if redis.call('get',KEYS[1]) == ARGV[1] then" +
                        "   return redis.call('del',KEYS[1]) " +
                        "else" +
                        "   return 0 " +
                        "end";
        String value = "";
        // 删除分布式锁
        // 需要判断所得所有者
        Thread t = Thread.currentThread();
        if(exclusiveOwnerThread.get().equals(t.getName())) {
            Jedis jedis = new Jedis("localhost", 6379);
            // 删除键值
            try {
                Object result = jedis.eval(script, Collections.singletonList(lockKey),
                        Collections.singletonList(value));
                if("1".equals(result.toString())){
                    // 成功
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                jedis.close();
            }
        }
    }

    public Condition newCondition() {
        return null;
    }
}
