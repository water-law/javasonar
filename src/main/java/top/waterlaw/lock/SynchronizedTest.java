package top.waterlaw.lock;


import org.openjdk.jol.info.ClassLayout;

import java.util.ArrayList;
import java.util.List;

public class SynchronizedTest {
    /**
     * Q: java 当中有哪些锁
     * A: 公平锁、非公平锁、自旋锁、偏向锁、轻量级锁、重量级锁
     *
     * Q: java 对象组成?
     * A: 1. 对象头(所有java对象的公共组成部分，固定字节)
     *    2. 实例对象(l 实例的 flag成员变量，不固定字节)
     *    3. 对齐数据(64位jvm对象头字节必须是8字节的整数倍，32则是4字节的整数倍，不固定字节)
     *
     * Q: 对象头组成？
     * A: 由 Mark Word 和 klass pointer 组成
     *
     * Q: Mark Word 和 Class Metadata Address 结构？
     * A: 64 bit JVM: Mark Word(64bit), klass pointer(32bit, JVM指令未压缩前为64bit)
     * 参考 JDK 官方文档 http://openjdk.java.net/groups/hotspot/docs/HotSpotGlossary.html
     *
     * Q: Mark Word 组成？
     * A:
     参考 openjdk 源码下的 openjdk/hotspot/src/share/vm/oops/markOop.hpp
     //  32 bits:
     //  --------
     //             hash:25 ------------>| age:4    biased_lock:1 lock:2 (normal object)
     //             JavaThread*:23 epoch:2 age:4    biased_lock:1 lock:2 (biased object)
     //             size:32 ------------------------------------------>| (CMS free block)
     //             PromotedObject*:29 ---------->| promo_bits:3 ----->| (CMS promoted object)
     //
     //  64 bits:
     //  --------
     //  unused:25 hash:31 -->| unused:1   age:4    biased_lock:1 lock:2 (normal object)
     //  JavaThread*:54 epoch:2 unused:1   age:4    biased_lock:1 lock:2 (biased object)
     //  PromotedObject*:61 --------------------->| promo_bits:3 ----->| (CMS promoted object)
     //  size:64 ----------------------------------------------------->| (CMS free block)
     //
     //  unused:25 hash:31 -->| cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && normal object)
     //  JavaThread*:54 epoch:2 cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && biased object)
     //  narrowOop:32 unused:24 cms_free:1 unused:4 promo_bits:3 ----->| (COOPs && CMS promoted object)
     //  unused:21 size:35 -->| cms_free:1 unused:7 ------------------>| (COOPs && CMS free block)
     //
     //  - hash contains the identity hash value: largest value is
     //    31 bits, see os::random().  Also, 64-bit vm's require
     //    a hash value no bigger than 32 bits because they will not
     //    properly generate a mask larger than that: see library_call.cpp
     //    and c1_CodePatterns_sparc.cpp.
     //
     //  - the biased lock pattern is used to bias a lock toward a given
     //    thread. When this pattern is set in the low three bits, the lock
     //    is either biased toward a given thread or "anonymously" biased,
     //    indicating that it is possible for it to be biased. When the
     //    lock is biased toward a given thread, locking and unlocking can
     //    be performed by that thread without using atomic operations.
     //    When a lock's bias is revoked, it reverts back to the normal
     //    locking scheme described below.
     //
     //    Note that we are overloading the meaning of the "unlocked" state
     //    of the header. Because we steal a bit from the age we can
     //    guarantee that the bias pattern will never be seen for a truly
     //    unlocked object.
     //
     //    Note also that the biased state contains the age bits normally
     //    contained in the object header. Large increases in scavenge
     //    times were seen when these bits were absent and an arbitrary age
     //    assigned to all biased objects, because they tended to consume a
     //    significant fraction of the eden semispaces and were not
     //    promoted promptly, causing an increase in the amount of copying
     //    performed. The runtime system aligns all JavaThread* pointers to
     //    a very large value (currently 128 bytes (32bVM) or 256 bytes (64bVM))
     //    to make room for the age bits & the epoch bits (used in support of
     //    biased locking), and for the CMS "freeness" bit in the 64bVM (+COOPs).
     //
     //    [JavaThread* | epoch | age | 1 | 01]       lock is biased toward given thread
     //    [0           | epoch | age | 1 | 01]       lock is anonymously biased
     //
     //  - the two lock bits are used to describe three states: locked/unlocked and monitor.
     //
     //    [ptr             | 00]  locked             ptr points to real header on stack
     //    [header      | 0 | 01]  unlocked           regular object header
     //    [ptr             | 10]  monitor            inflated lock (header is wapped out)
     //    [ptr             | 11]  marked             used by markSweep to mark an object
     //                                               not valid at any other time
     //
     //    We assume that stack/thread pointers have the lowest two bits cleared.
     */
    L l = new L();

    public static void main(String[] args) throws InterruptedException {
        //- 我这里是大端机器
//        System.out.println(Integer.toBinaryString(l.hashCode()));//不执行hashCode()则hashCode不存在
//        System.out.println(ClassLayout.parseInstance(l).toPrintable());//lock位:001，l是无状态的
        int NUM = 0;
        List<Thread> ts = new ArrayList<>();
//        for(int i =0; i < NUM; i++) {
//            Thread t = new Thread(SynchronizedTest::syncTest, i+"");
//            t.start();
//            ts.add(t);
//
//        }
        for(Thread t: ts) {
            t.join();
        }
        SynchronizedTest t = new SynchronizedTest();
        t.syncTest();
//        syncTest();
//        Thread t = new Thread(SynchronizedTest::syncTest);
//        t.start();
//        t.join();
//        System.out.println(ClassLayout.parseInstance(l).toPrintable());//lock位:001，l是无状态的

//        System.gc();
//        Thread.sleep(3000);
//        System.out.println(l);
//        System.out.println(ClassLayout.parseInstance(l).toPrintable());
//        Thread.sleep(10*100000000);
    }


    public void syncTest() {
        //- java 对象的状态有 5 种, 由 biased_lock:1 lock:2 共 3 bit 组成
        /**
         * 1. 无状态， 对象刚 new 出来的时候 001
         * 2. 偏向锁，只有一个线程持有这个对象 01
         * 3. 轻量锁，00
         * 4. 重量锁，10
         * 5. gc 标记，对象引用为空 11
         */
        synchronized (l) {  //- 锁的是 l 对象而不是代码块
//            System.out.println(Thread.currentThread().getName()+Thread.currentThread().getId());
//            System.out.println(Integer.toBinaryString(l.hashCode()));
            //- 上锁改变的是 java 对象头
            System.out.println(ClassLayout.parseInstance(l).toPrintable());//lock位:000，l是加上轻量级锁的
        }
    }
}

class L {
//    int flag = 0;
}

/**
 * Q: A 线程持有偏向锁，B 线程竞争锁，偏向锁升级为轻量级锁，A 继续执行，此时 B 线程会做什么？自旋吗？
 * 如果是自旋会不会出现一种情况，同是 AB 两个线程竞争锁，锁的状态可能因为这两个线程从偏向锁升级到轻量级在升级到重量级？
 * A: 线程 B 会再次获取锁 如果再失败 会膨胀成重量级锁 然后自旋一定次数失败后挂起
 */
