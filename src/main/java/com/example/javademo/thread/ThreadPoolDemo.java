package com.example.javademo.thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池详解
 *
 * 【面试常问 — 线程池7大参数】
 * corePoolSize    核心线程数（一直存活，除非 allowCoreThreadTimeOut）
 * maximumPoolSize 最大线程数（核心+临时）
 * keepAliveTime   临时线程空闲存活时间
 * unit            keepAliveTime的时间单位
 * workQueue       阻塞队列（存放等待执行的任务）
 * threadFactory   线程工厂（自定义线程名等）
 * handler         拒绝策略（当线程和队列都满了）
 *
 * 【执行流程】
 * 新任务 → 核心线程(有空闲) → 是 → 执行
 *                    → 否 → 队列(未满) → 入队等待
 *                                       → 队列满 → 创建临时线程(未超max) → 执行
 *                                                                       → 超max → 拒绝策略
 *
 * 【拒绝策略4种】
 * AbortPolicy      抛异常（默认）
 * CallerRunsPolicy 回退给调用线程执行
 * DiscardPolicy    直接丢弃（不抛异常）
 * DiscardOldest    丢弃最老的任务
 *
 * 【常见坑】
 * - 不要用 Executors.newFixedThreadPool — 队列无界可能OOM
 * - 不要用 Executors.newCachedThreadPool — 线程数无界可能OOM
 * - 必须用 new ThreadPoolExecutor(...) 显式指定参数
 * - 线程池用完要 shutdown()
 */
public class ThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 1. 线程池7参数详解 ==========");
        threadPoolParams();

        System.out.println("\n========== 2. 线程池执行流程演示 ==========");
        executionFlow();

        System.out.println("\n========== 3. 拒绝策略演示 ==========");
        rejectPolicyDemo();

        System.out.println("\n========== 4. Executors 工具类（不推荐但面试常问）==========");
        executorsDemo();

        System.out.println("\n========== 5. submit vs execute ==========");
        submitVsExecute();

        System.out.println("\n========== 6. ScheduledThreadPool ==========");
        scheduledDemo();
    }

    static void threadPoolParams() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                      // corePoolSize 核心线程数
                5,                      // maximumPoolSize 最大线程数
                60, TimeUnit.SECONDS,   // keepAlive 临时线程60秒空闲后回收
                new LinkedBlockingQueue<>(10),  // 最多排队10个任务
                new ThreadFactory() {   // 自定义线程工厂
                    private final AtomicInteger count = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "学习线程-" + count.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.AbortPolicy() // 拒绝策略
        );
        System.out.println("参数说明:");
        System.out.println("  corePoolSize=2:    核心线程2个，有任务就执行");
        System.out.println("  maxPoolSize=5:     最多5个线程（核心2+临时3）");
        System.out.println("  keepAlive=60s:     临时线程空闲60秒销毁");
        System.out.println("  queue=10:          队列容量10（等待执行的任务）");
        System.out.println("  AbortPolicy:       队列满了抛异常");
        executor.shutdown();
    }

    static void executionFlow() {
        // 小容量线程池，便于观察执行流程
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 2, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),  // 队列容量只有1
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        System.out.println("线程池: 核心1, 最大2, 队列容量1");

        for (int i = 0; i < 4; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("  任务" + taskId + " 开始执行 → "
                        + Thread.currentThread().getName());
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                System.out.println("  任务" + taskId + " 完成 ← "
                        + Thread.currentThread().getName());
            });
        }

        System.out.println("任务0 → 核心线程");
        System.out.println("任务1 → 队列排队");
        System.out.println("任务2 → 创建临时线程（队列满了）");
        System.out.println("任务3 → 触发拒绝策略 CallerRunsPolicy → 由main线程执行");

        executor.shutdown();
        try { executor.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
    }

    static void rejectPolicyDemo() {
        // 创建一个小容量线程池来触发拒绝策略
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.AbortPolicy()
        );

        System.out.println("线程池: 核心1, 最大1, 队列1 — 最多容纳2个任务");

        executor.execute(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        });
        executor.execute(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        });

        // 第三个任务：线程和队列都满了 → 触发拒绝策略
        try {
            executor.execute(() -> System.out.println("这个任务不会执行"));
        } catch (RejectedExecutionException e) {
            System.out.println("❌ 拒绝策略(AbortPolicy): " + e.getClass().getSimpleName());
            System.out.println("   任务被拒绝执行");
        }

        executor.shutdownNow();
    }

    static void executorsDemo() {
        // 面试常问，但生产环境不推荐！
        System.out.println("⚠️ 以下方法面试常考但生产不推荐使用:");
        System.out.println("  newFixedThreadPool:  队列无界(Integer.MAX_VALUE) → 可能OOM");
        System.out.println("  newCachedThreadPool: 线程数无界(Integer.MAX_VALUE) → 可能OOM");
        System.out.println("  newSingleThreadExecutor: 队列无界 → 可能OOM");
        System.out.println("  ✅ 生产应使用 new ThreadPoolExecutor(参数)");
    }

    static void submitVsExecute() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // execute — 无返回值，异常会直接抛出
        executor.execute(() -> {
            System.out.println("  execute执行，无返回值");
        });

        // submit — 返回 Future，可以获取结果和异常
        Future<Integer> future = executor.submit(() -> {
            Thread.sleep(200);
            return 42;
        });

        try {
            Integer result = future.get(1, TimeUnit.SECONDS);  // 带超时的get
            System.out.println("  submit返回值: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }

    static void scheduledDemo() throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // 延迟执行
        scheduler.schedule(() -> {
            System.out.println("  延迟500ms执行");
        }, 500, TimeUnit.MILLISECONDS);

        // 定时执行
        AtomicInteger count = new AtomicInteger(0);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            int c = count.incrementAndGet();
            System.out.println("  定时任务第" + c + "次执行");
        }, 200, 300, TimeUnit.MILLISECONDS);

        Thread.sleep(1200);
        future.cancel(false);
        scheduler.shutdown();
        System.out.println("  定时任务已取消");
    }
}
