package com.example.javademo.designpattern;

import java.io.*;
import java.lang.reflect.Constructor;

/**
 * 单例模式 — 5种实现方式
 *
 * 【面试常问】
 * - 饿汉式 vs 懒汉式
 * - DCL（双重检查锁定）为什么用 volatile 修饰INSTANCE？
 *   → 防止指令重排：INSTANCE = new Singleton() 分3步：
 *     1.分配内存 2.初始化对象 3.INSTANCE指向内存地址
 *     可能2和3重排，导致其他线程拿到未初始化的对象
 * - 枚举单例如何防止反射破坏和序列化破坏？
 *
 * 【常见坑】
 * - volatile 必不可少（DCL场景）
 * - 懒汉式的synchronized有性能开销
 * - 反射和序列化可以破坏单例
 */
public class SingletonDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. 饿汉式（Eager）==========");
        EagerSingleton e1 = EagerSingleton.getInstance();
        EagerSingleton e2 = EagerSingleton.getInstance();
        System.out.println("e1 == e2: " + (e1 == e2));
        System.out.println("类加载时就创建，线程安全但可能浪费内存");

        System.out.println("\n========== 2. 懒汉式（Lazy）==========");
        LazySingleton l1 = LazySingleton.getInstance();
        LazySingleton l2 = LazySingleton.getInstance();
        System.out.println("l1 == l2: " + (l1 == l2));
        System.out.println("调用时才创建，但有synchronized性能开销");

        System.out.println("\n========== 3. DCL双重检查锁定 ==========");
        DCLSingleton d1 = DCLSingleton.getInstance();
        DCLSingleton d2 = DCLSingleton.getInstance();
        System.out.println("d1 == d2: " + (d1 == d2));
        System.out.println("volatile保证可见性+防止指令重排，两次check减少锁竞争");

        System.out.println("\n========== 4. 静态内部类（推荐）==========");
        InnerClassSingleton i1 = InnerClassSingleton.getInstance();
        InnerClassSingleton i2 = InnerClassSingleton.getInstance();
        System.out.println("i1 == i2: " + (i1 == i2));
        System.out.println("利用类加载机制保证线程安全，懒加载，无synchronized开销");

        System.out.println("\n========== 5. 枚举（最安全）==========");
        EnumSingleton.INSTANCE.doSomething();
        System.out.println("天然防反射、防序列化破坏，代码最简洁");

        System.out.println("\n========== 6. 反射破坏单例 ==========");
        reflectionAttack();

        System.out.println("\n========== 7. 序列化破坏单例 ==========");
        serializationAttack();
    }

    // ==================== 1. 饿汉式 ====================
    // 类加载时就创建实例，线程安全（JVM保证类加载线程安全）
    static class EagerSingleton {
        private static final EagerSingleton INSTANCE = new EagerSingleton();

        private EagerSingleton() {}  // 私有构造器

        public static EagerSingleton getInstance() {
            return INSTANCE;
        }
    }

    // ==================== 2. 懒汉式 ====================
    // 第一次调用getInstance时才创建，但是每次都要加锁
    static class LazySingleton {
        private static LazySingleton instance;

        private LazySingleton() {}

        public static synchronized LazySingleton getInstance() {
            if (instance == null) {
                instance = new LazySingleton();
            }
            return instance;
        }
    }

    // ==================== 3. DCL双重检查锁定 ====================
    // ⚠️ 面试重点：volatile为什么必不可少？
    static class DCLSingleton {
        // volatile 的作用：
        // 1. 保证可见性（一个线程修改，其他线程立即可见）
        // 2. 禁止指令重排（防止步骤2和3被重排）
        private static volatile DCLSingleton instance;

        private DCLSingleton() {}

        public static DCLSingleton getInstance() {
            if (instance == null) {                    // 第一重检查（不加锁，快速判断）
                synchronized (DCLSingleton.class) {
                    if (instance == null) {            // 第二重检查（加锁，防止多线程同时进）
                        instance = new DCLSingleton();
                    }
                }
            }
            return instance;
        }
    }

    // ==================== 4. 静态内部类 ====================
    // 利用类加载机制：外部类加载时内部类不会加载，调用时才加载
    static class InnerClassSingleton {
        private InnerClassSingleton() {}

        public static InnerClassSingleton getInstance() {
            return Holder.INSTANCE;
        }

        // 静态内部类：只有首次调用getInstance时才会被加载
        private static class Holder {
            static final InnerClassSingleton INSTANCE = new InnerClassSingleton();
        }
    }

    // ==================== 5. 枚举 ====================
    // 枚举的实例创建是线程安全的，且天然防止反射和序列化破坏
    enum EnumSingleton {
        INSTANCE;

        public void doSomething() {
            System.out.println("  枚举单例执行中...");
        }
    }

    // ==================== 攻击演示 ====================
    static void reflectionAttack() {
        try {
            Constructor<EagerSingleton> constructor =
                    EagerSingleton.class.getDeclaredConstructor();
            constructor.setAccessible(true);  // 暴力反射
            EagerSingleton refInstance = constructor.newInstance();
            System.out.println("反射创建: " + refInstance);
            System.out.println("原始实例: " + EagerSingleton.getInstance());
            System.out.println("反射可以破坏普通单例！（枚举除外）");
        } catch (Exception e) {
            System.out.println("反射攻击失败: " + e.getCause().getMessage());
        }
    }

    static void serializationAttack() {
        // 序列化
        EagerSingleton original = EagerSingleton.getInstance();
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("singleton.ser"))) {
            oos.writeObject(original);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 反序列化 — 会创建新对象！
        // 注意：EagerSingleton没有实现Serializable，会报错
        // 实际单例需要加 readResolve() 方法来防止序列化破坏
        System.out.println("序列化/反序列化会创建新对象（需加 readResolve() 防护）");
        System.out.println("  private Object readResolve() { return INSTANCE; }");

        try { new File("singleton.ser").delete(); } catch (Exception ignored) {}
    }
}
