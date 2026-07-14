package com.example.javademo.jvm;

/**
 * JVM核心概念演示
 *
 * 【面试常问】
 * 1. 类加载过程：加载 → 验证 → 准备 → 解析 → 初始化
 * 2. 双亲委派机制：AppClassLoader → ExtClassLoader → BootstrapClassLoader
 *    收到加载请求先委托父加载器，父加载不了才自己加载
 * 3. 运行时数据区：
 *    - 堆（Heap）：所有线程共享，存放对象实例，GC主要区域
 *    - 方法区（Method Area）：存放类信息、常量、静态变量（JDK8+ 元空间）
 *    - 虚拟机栈（VM Stack）：线程私有，每个方法对应一个栈帧
 *    - 本地方法栈：native方法
 *    - 程序计数器：记录线程执行位置
 * 4. GC算法：
 *    - 标记-清除：产生内存碎片
 *    - 标记-整理：消除碎片但有移动开销
 *    - 复制：内存利用率50%，适合新生代
 *    - 分代收集：新生代用复制(Eden+Survivor)，老年代用标记-整理
 *
 * 【常见坑】
 * - 方法区/元空间也会OOM（加载太多类或常量）
 * - System.gc() 只是建议JVM GC，不保证一定执行
 */
public class JVMDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. 类加载器层级 ==========");
        classLoaderDemo();

        System.out.println("\n========== 2. 运行时数据区概览 ==========");
        runtimeDataArea();

        System.out.println("\n========== 3. 对象创建和GC ==========");
        gcDemo();

        System.out.println("\n========== 4. 堆内存参数 ==========");
        heapMemoryDemo();

        System.out.println("\n========== 5. 引用类型 ==========");
        referenceDemo();
    }

    static void classLoaderDemo() {
        // 应用程序类加载器 (AppClassLoader)
        ClassLoader appCL = JVMDemo.class.getClassLoader();
        System.out.println("AppClassLoader: " + appCL);
        System.out.println("  -> 加载 classpath 下的类");

        // 扩展/平台类加载器 (PlatformClassLoader, JDK9+)
        ClassLoader platformCL = appCL.getParent();
        System.out.println("PlatformClassLoader: " + platformCL);
        System.out.println("  -> 加载 JDK 扩展类");

        // 启动类加载器 (BootstrapClassLoader) — C++实现，Java中为null
        ClassLoader bootstrapCL = platformCL.getParent();
        System.out.println("BootstrapClassLoader: " + bootstrapCL + " (C++实现，Java返回null)");
        System.out.println("  -> 加载核心类库 rt.jar / java.base");

        // 双亲委派：为什么这样设计？
        // 防止核心类被篡改（比如自定义一个 java.lang.String 不会被加载）
        System.out.println("\n双亲委派机制：");
        System.out.println("1. 收到加载请求 → 检查是否已加载");
        System.out.println("2. 未加载 → 委托父加载器");
        System.out.println("3. 父加载器无法加载 → 自己尝试加载");
        System.out.println("目的：保证核心类的安全，避免重复加载");
    }

    static void runtimeDataArea() {
        System.out.println("JVM 运行时数据区：");
        System.out.println("┌──────────────────────────────────────┐");
        System.out.println("│  线程共享区域                          │");
        System.out.println("│  - 堆 (Heap)                          │");
        System.out.println("│    新生代: Eden + Survivor0 + Survivor1│");
        System.out.println("│    老年代: Tenured                     │");
        System.out.println("│  - 方法区/元空间 (Metaspace)           │");
        System.out.println("│    类信息、常量、静态变量               │");
        System.out.println("├──────────────────────────────────────┤");
        System.out.println("│  线程私有区域                          │");
        System.out.println("│  - 虚拟机栈 (VM Stack)                 │");
        System.out.println("│    栈帧: 局部变量表 + 操作数栈 + 方法返回│");
        System.out.println("│  - 本地方法栈                          │");
        System.out.println("│  - 程序计数器                          │");
        System.out.println("└──────────────────────────────────────┘");
    }

    static void gcDemo() {
        System.out.println("GC算法对比：");
        System.out.println("┌──────────┬────────┬────────┬──────────┐");
        System.out.println("│ 算法     │ 碎片   │ 效率   │ 适用     │");
        System.out.println("├──────────┼────────┼────────┼──────────┤");
        System.out.println("│ 标记-清除 │ 有     │ 中     │ 老年代   │");
        System.out.println("│ 标记-整理 │ 无     │ 低     │ 老年代   │");
        System.out.println("│ 复制     │ 无     │ 高     │ 新生代   │");
        System.out.println("│ 分代收集 │ 少     │ 高     │ 实际使用  │");
        System.out.println("└──────────┴────────┴────────┴──────────┘");

        System.out.println("\n新生代 Minor GC 过程：");
        System.out.println("1. 新对象分配在Eden区");
        System.out.println("2. Eden满 → Minor GC → 存活对象copy到Survivor0");
        System.out.println("3. 下次Minor GC → Eden+Survivor0存活对象copy到Survivor1");
        System.out.println("4. 反复在Survivor间来回复制（年龄+1）");
        System.out.println("5. 年龄到达阈值(默认15) → 晋升到老年代");
        System.out.println("\n老年代满 → Major GC / Full GC (STW，暂停所有用户线程)");

        // 演示GC
        System.out.println("\n当前堆内存状态：");
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        System.out.println("  堆总大小: " + totalMemory / 1024 / 1024 + " MB");
        System.out.println("  已使用: " + usedMemory / 1024 / 1024 + " MB");
        System.out.println("  空闲: " + freeMemory / 1024 / 1024 + " MB");
    }

    static void heapMemoryDemo() {
        System.out.println("常见堆内存参数（JVM启动参数）：");
        System.out.println("  -Xms512m      初始堆大小");
        System.out.println("  -Xmx2048m     最大堆大小");
        System.out.println("  -Xmn256m      新生代大小");
        System.out.println("  -XX:SurvivorRatio=8  Eden:S0:S1 = 8:1:1");
        System.out.println("  -XX:MaxTenuringThreshold=15  晋升老年代年龄阈值");
        System.out.println("  -XX:MetaspaceSize=128m       元空间初始大小");
        System.out.println("  -XX:MaxMetaspaceSize=256m    元空间最大大小");

        System.out.println("\n当前JVM内存参数：");
        Runtime rt = Runtime.getRuntime();
        System.out.println("  最大堆: " + rt.maxMemory() / 1024 / 1024 + " MB");
        System.out.println("  当前堆: " + rt.totalMemory() / 1024 / 1024 + " MB");
        System.out.println("  可用处理器: " + rt.availableProcessors() + " 个");
    }

    static void referenceDemo() {
        System.out.println("Java 4种引用类型（从强到弱）：");

        // 1. 强引用 — 最常见的引用，GC绝不会回收
        Object strongRef = new Object();
        System.out.println("1. 强引用: new Object() → 不会被GC回收");

        // 2. 软引用 — 内存不足时回收
        java.lang.ref.SoftReference<byte[]> softRef =
                new java.lang.ref.SoftReference<>(new byte[10 * 1024 * 1024]);
        System.out.println("2. 软引用: SoftReference → 内存不足时回收（适合缓存）");

        // 3. 弱引用 — 下次GC就回收
        java.lang.ref.WeakReference<Object> weakRef =
                new java.lang.ref.WeakReference<>(new Object());
        System.out.println("3. 弱引用: WeakReference → 下一次GC就回收（ThreadLocal用）");

        // 4. 虚引用 — 无法通过它获取对象，仅用于跟踪回收
        java.lang.ref.PhantomReference<Object> phantomRef =
                new java.lang.ref.PhantomReference<>(new Object(), null);
        System.out.println("4. 虚引用: PhantomReference → 仅跟踪对象回收时间");
    }
}
