package com.example.javademo;

import com.example.javademo.basic.*;
import com.example.javademo.collection.*;
import com.example.javademo.thread.*;
import com.example.javademo.jvm.*;
import com.example.javademo.database.*;
import com.example.javademo.designpattern.*;
import com.example.javademo.algorithm.*;

/**
 * Java八股学习Demo — 总入口
 * 依次运行各模块的示例，展示核心知识点的运行结果
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Java八股学习Demo — 秋招面试核心知识点");
        System.out.println("========================================\n");

        System.out.println(">>> 一、Java基础");
        System.out.println("----------------------------------------");
        OOPDemo.main(args);
        StringDemo.main(args);
        ExceptionDemo.main(args);
        IODemo.main(args);

        System.out.println("\n>>> 二、集合框架");
        System.out.println("----------------------------------------");
        ListDemo.main(args);
        MapDemo.main(args);
        SetDemo.main(args);

        System.out.println("\n>>> 三、多线程与并发");
        System.out.println("----------------------------------------");
        try {
            ThreadBaseDemo.main(args);
            LockDemo.main(args);
            ThreadPoolDemo.main(args);
        } catch (InterruptedException e) {
            System.out.println("多线程模块被中断: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        System.out.println("\n>>> 四、JVM");
        System.out.println("----------------------------------------");
        JVMDemo.main(args);

        System.out.println("\n>>> 五、数据库（手写SQL）");
        System.out.println("----------------------------------------");
        H2JDBCDemo.main(args);
        SQLPractice.main(args);

        System.out.println("\n>>> 六、设计模式");
        System.out.println("----------------------------------------");
        SingletonDemo.main(args);
        FactoryDemo.main(args);
        ProxyDemo.main(args);

        System.out.println("\n>>> 七、算法与数据结构");
        System.out.println("----------------------------------------");
        SortDemo.main(args);
        SearchDemo.main(args);
        StructureDemo.main(args);

        System.out.println("\n========================================");
        System.out.println("  全部模块运行完毕！");
        System.out.println("  建议逐模块手写练习，加深理解。");
        System.out.println("========================================");
    }
}
