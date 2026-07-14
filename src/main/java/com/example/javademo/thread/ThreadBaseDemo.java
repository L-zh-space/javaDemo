package com.example.javademo.thread;

/**
 * 线程基础：创建方式、生命周期、常用方法
 *
 * 【面试常问】
 * - 创建线程的3种方式：继承Thread、实现Runnable、实现Callable（有返回值）
 * - Thread vs Runnable：Runnable更好（避免单继承限制，任务与线程分离）
 * - 线程生命周期：NEW → RUNNABLE → BLOCKED/WAITING/TIMED_WAITING → TERMINATED
 * - start() vs run()：start() 启动新线程，run() 只是普通方法调用
 * - sleep() vs wait()：sleep不释放锁，wait释放锁；sleep是Thread方法，wait是Object方法
 * - join()：等待线程执行完毕
 *
 * 【常见坑】
 * - 直接调run()不会启动新线程，要在当前线程中执行
 * - 线程start()后不能再次start()，会抛IllegalThreadStateException
 */
public class ThreadBaseDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 1. 三种创建方式 ==========");
        createThreadDemo();

        System.out.println("\n========== 2. Thread vs Runnable ==========");
        threadVsRunnable();

        System.out.println("\n========== 3. 线程生命周期 ==========");
        lifeCycleDemo();

        System.out.println("\n========== 4. start() vs run() ==========");
        startVsRun();

        System.out.println("\n========== 5. join() 等待线程 ==========");
        joinDemo();

        System.out.println("\n========== 6. sleep() vs wait() ==========");
        sleepVsWait();
    }

    // ==================== 方式1：继承 Thread ====================
    static class MyThread extends Thread {
        public MyThread(String name) { super(name); }

        @Override
        public void run() {
            System.out.println("  方式1-Thread: " + getName() + " 正在执行");
        }
    }

    // ==================== 方式2：实现 Runnable ====================
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("  方式2-Runnable: " + Thread.currentThread().getName() + " 正在执行");
        }
    }

    static void createThreadDemo() throws InterruptedException {
        // 方式1
        MyThread t1 = new MyThread("线程A");
        t1.start();

        // 方式2
        Thread t2 = new Thread(new MyRunnable(), "线程B");
        t2.start();

        // 方式2 Lambda简写（最常用）
        Thread t3 = new Thread(() -> {
            System.out.println("  方式2-Lambda: " + Thread.currentThread().getName() + " 正在执行");
        }, "线程C");
        t3.start();

        // 方式3：Callable + FutureTask（有返回值、可抛异常）
        java.util.concurrent.FutureTask<String> futureTask = new java.util.concurrent.FutureTask<>(() -> {
            Thread.sleep(100);
            return "Callable的返回值";
        });
        Thread t4 = new Thread(futureTask, "线程D");
        t4.start();
        // 获取返回值 — get() 会阻塞直到线程执行完毕
        try {
            String result = futureTask.get();
            System.out.println("  方式3-Callable 返回值: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(200); // 等子线程完成
    }

    static void threadVsRunnable() {
        // Runnable 的优势1：避免单继承限制
        class MyTask extends BaseClass implements Runnable {  // 可以继承其他类
            @Override
            public void run() {
                System.out.println("  Runnable可以继承其他类，Thread则不能");
            }
        }
        new Thread(new MyTask()).start();
        System.out.println("  优势：Runnable 将任务与线程解耦，同一个Runnable可被多个Thread复用");
    }

    static class BaseClass {
        // 模拟已有父类
    }

    static void lifeCycleDemo() throws InterruptedException {
        Thread t = new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }, "test");

        System.out.println("  创建后: " + t.getState());        // NEW
        t.start();
        System.out.println("  start后: " + t.getState());     // RUNNABLE (或 TIMED_WAITING)

        Thread.sleep(100);
        System.out.println("  运行中: " + t.getState());       // TIMED_WAITING（在sleep中）

        t.join();  // 等待线程结束
        System.out.println("  结束后: " + t.getState());       // TERMINATED
    }

    static void startVsRun() throws InterruptedException {
        Thread t = new Thread(() -> {
            System.out.println("  执行线程: " + Thread.currentThread().getName());
        }, "子线程");

        System.out.println("  当前线程: " + Thread.currentThread().getName());

        // 对比 start()
        t.start();         // 新建线程执行 → 打印 "子线程"
        t.join();

        // ⚠️ 直接调 run() 不会启动新线程！
        System.out.print("  直接调用 run(): ");
        t.run();           // 在当前线程执行 → 打印 "main"
    }

    static void joinDemo() throws InterruptedException {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("  子线程工作完成！");
            } catch (InterruptedException ignored) {}
        });

        worker.start();
        System.out.println("  主线程等待子线程...");
        worker.join();  // 阻塞直到worker执行完毕
        System.out.println("  子线程已结束，主线程继续");
    }

    static void sleepVsWait() {
        final Object lock = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("  线程1: 获得锁，sleep 1秒（不释放锁）");
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                System.out.println("  线程1: sleep结束，释放锁");
            }
        });

        Thread t2 = new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {} // 确保t1先获得锁
            synchronized (lock) {
                System.out.println("  线程2: 获得锁（在t1 sleep结束后才获得，证明sleep不释放锁）");
            }
        });

        t1.start();
        t2.start();
        try { t1.join(); t2.join(); } catch (InterruptedException ignored) {}

        System.out.println("  结论: sleep() 不释放锁，wait() 释放锁");
    }
}
