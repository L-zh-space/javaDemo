package com.example.javademo.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * synchronized / Lock / 死锁 详解
 *
 * 【面试常问】
 * - synchronized 锁升级过程：偏向锁 → 轻量级锁（CAS自旋） → 重量级锁（内核态）
 * - synchronized 修饰普通方法（锁this）、静态方法（锁Class）、代码块（锁指定对象）
 * - ReentrantLock vs synchronized：可中断、可超时、可公平、多条件变量
 * - ReentrantLock 底层 AQS（AbstractQueuedSynchronizer）
 * - 死锁4个条件：互斥、持有并等待、不可剥夺、循环等待
 * - volatile 关键字：保证可见性、禁止指令重排，不保证原子性
 *
 * 【常见坑】
 * - synchronized 是可重入锁（同一个线程可以多次获取同一把锁）
 * - Lock 必须在 finally 中 unlock，否则异常会导致锁永不释放
 * - volatile 不能保证 i++ 的原子性（i++是读-改-写3步）
 */
public class LockDemo {

    private static int counter = 0;
    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 1. synchronized 基本用法 ==========");
        synchronizedDemo();

        System.out.println("\n========== 2. ReentrantLock 基本用法 ==========");
        reentrantLockDemo();

        System.out.println("\n========== 3. 死锁演示 ==========");
        deadlockDemo();

        System.out.println("\n========== 4. volatile 可见性 ==========");
        volatileDemo();

        System.out.println("\n========== 5. volatile 不保证原子性 ==========");
        volatileAtomicityDemo();

        System.out.println("\n========== 6. ReentrantReadWriteLock ==========");
        readWriteLockDemo();
    }

    // ==================== synchronized ====================
    static void synchronizedDemo() throws InterruptedException {
        counter = 0;
        int threadCount = 10;
        int incrementsPerThread = 1000;

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    // 方式1：synchronized 代码块
                    synchronized (lock) {
                        counter++;
                    }
                    // 方式2：也可以定义 synchronized 方法，等价于 synchronized(this)
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) t.join();
        System.out.println("synchronized 保证线程安全: counter=" + counter
                + " (预期=" + threadCount * incrementsPerThread + ")");

        // 对比：不加锁的结果
        counter = 0;
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter++;  // 不安全的操作
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("不加锁（线程不安全）: counter=" + counter
                + " (预期=" + threadCount * incrementsPerThread + ")");
    }

    // ==================== ReentrantLock ====================
    static void reentrantLockDemo() throws InterruptedException {
        ReentrantLock rLock = new ReentrantLock();
        counter = 0;
        int threadCount = 10;

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    rLock.lock();
                    try {
                        counter++;
                    } finally {
                        rLock.unlock();  // ⚠️ 必须在finally中unlock！
                    }
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("ReentrantLock 结果: counter=" + counter);

        // ReentrantLock 高级特性：尝试获取锁
        Thread t1 = new Thread(() -> {
            rLock.lock();
            try {
                System.out.println("  线程1获得锁，sleep 2秒");
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            } finally {
                rLock.unlock();
            }
        });
        t1.start();
        Thread.sleep(100); // 确保t1先拿到锁

        // tryLock — 尝试获取，拿不到立即返回false
        boolean got = rLock.tryLock();
        System.out.println("  tryLock() 立即返回: " + got);

        // tryLock(timeout) — 带超时的尝试
        try {
            got = rLock.tryLock(3, TimeUnit.SECONDS);
            System.out.println("  tryLock(3秒) 等待后: " + got);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t1.join();
        System.out.println("  ReentrantLock vs synchronized:");
        System.out.println("  - 可尝试获取锁（tryLock）");
        System.out.println("  - 可中断等待（lockInterruptibly）");
        System.out.println("  - 可设置公平锁（new ReentrantLock(true)）");
        System.out.println("  - 可有多个条件变量（newCondition）");
    }

    // ==================== 死锁 ====================
    static void deadlockDemo() {
        Object resourceA = new Object();
        Object resourceB = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (resourceA) {
                System.out.println("  线程1: 持有 A，等待 B...");
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}

                synchronized (resourceB) {
                    System.out.println("  线程1: 获得 B");
                }
            }
        }, "死锁线程1");

        Thread t2 = new Thread(() -> {
            synchronized (resourceB) {
                System.out.println("  线程2: 持有 B，等待 A...");
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}

                synchronized (resourceA) {
                    System.out.println("  线程2: 获得 A");
                }
            }
        }, "死锁线程2");

        // 设为守护线程 — 避免整个JVM被死锁卡住不退
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        System.out.println("  线程1状态: " + t1.getState() + " (BLOCKED = 等待获取锁)");
        System.out.println("  线程2状态: " + t2.getState());
        System.out.println("  ⚠️ 发生死锁！解决方案：");
        System.out.println("    1. 固定加锁顺序（两个线程都先A后B）");
        System.out.println("    2. 使用ReentrantLock.tryLock()加超时");
        System.out.println("    3. 减少锁粒度，降低锁持有时间");
        System.out.println("  死锁4条件：互斥、持有并等待、不可剥夺、循环等待 → 破坏任一个即可");
    }

    // ==================== volatile ====================
    // ⚠️ 不加volatile，线程可能看不到另一个线程的修改（JMM缓存不一致）
    private static /*volatile*/ boolean running = true;

    static void volatileDemo() throws InterruptedException {
        running = true;
        Thread reader = new Thread(() -> {
            int count = 0;
            // 如果没有volatile，这个循环可能永远不会退出
            // 因为reader线程可能缓存了running的值
            while (running) {
                count++;
            }
            System.out.println("  reader线程退出，循环次数: " + count);
        });
        reader.start();

        Thread.sleep(100);
        System.out.println("  设置 running = false");
        running = false;

        reader.join(2000);  // 等最多2秒
        if (reader.isAlive()) {
            System.out.println("  ⚠️ 线程未退出！没有volatile时可能发生（取决于JVM实现）");
            System.out.println("  running字段需要用volatile修饰以保证可见性");
            reader.interrupt();
        } else {
            System.out.println("  volatile保证了可见性，线程正常退出");
        }
    }

    // volatile 不能保证原子性
    private static volatile int volatileCounter = 0;

    static void volatileAtomicityDemo() throws InterruptedException {
        volatileCounter = 0;
        int threadCount = 10;

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    volatileCounter++;  // volatile不能保证这行原子性！
                    // i++ 实际上是：读取 → +1 → 写入，三步不是原子的
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("volatile counter: " + volatileCounter
                + " (预期=" + threadCount * 1000 + "，不保证原子性)");
        System.out.println("解决: 用 AtomicInteger 或 synchronized");
    }

    // ==================== ReentrantReadWriteLock ====================
    static void readWriteLockDemo() throws InterruptedException {
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        Lock readLock = rwLock.readLock();
        Lock writeLock = rwLock.writeLock();

        // 读锁共享 — 多个线程可以同时读
        System.out.println("  读锁共享测试：");
        for (int i = 0; i < 3; i++) {
            final int id = i;
            new Thread(() -> {
                readLock.lock();
                try {
                    System.out.println("    读线程" + id + " 持有读锁");
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                } finally {
                    readLock.unlock();
                }
            }).start();
        }

        Thread.sleep(500);
        // 写锁独占 — 写时不能读也不能写
        System.out.println("  写锁独占测试：");
        new Thread(() -> {
            writeLock.lock();
            try {
                System.out.println("    写线程持有写锁（此时其他读/写线程都会被阻塞）");
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            } finally {
                writeLock.unlock();
            }
        }).start();

        Thread.sleep(1000);
        System.out.println("  读多写少场景用ReentrantReadWriteLock提升并发性能");
    }
}
