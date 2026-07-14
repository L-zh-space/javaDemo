package com.example.javademo.basic;

/**
 * String / StringBuilder / StringBuffer 详解
 *
 * 【面试常问】
 * - String 为什么不可变？（底层 final char[]/byte[]，所有修改方法都返回新String）
 * - String 常量池：new String("abc") 创建了几个对象？
 * - StringBuilder vs StringBuffer：前者非线程安全但快，后者线程安全（synchronized）
 * - 字符串拼接底层会用 StringBuilder 优化，但循环拼接要注意
 *
 * 【常见坑】
 * - == 比较引用地址，equals 比较内容
 * - 循环中直接用 + 拼接会产生大量中间String对象
 * - 用 String 作为 synchronized 锁对象要小心（因为不可变，可能在别处被修改引用）
 */
public class StringDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. String 不可变性 ==========");
        String s1 = "hello";
        String s2 = s1.replace('h', 'H');
        System.out.println("s1 = " + s1);  // "hello" — 没变！
        System.out.println("s2 = " + s2);  // "Hello" — 返回的是新对象

        // 看似修改，实际是引用指向新对象
        String s3 = "hello";
        s3 = s3 + " world";  // s3 现在指向新对象 "hello world"，原 "hello" 不变
        System.out.println("s3 = " + s3);

        System.out.println("\n========== 2. 常量池 vs new ==========");
        // 字面量方式—放入常量池
        String a = "abc";
        String b = "abc";
        // new 方式—在堆上创建新对象
        String c = new String("abc");
        String d = new String("abc");

        System.out.println("a == b: " + (a == b));     // true — 同一个常量池对象
        System.out.println("a == c: " + (a == c));     // false — 堆上新对象
        System.out.println("c == d: " + (c == d));     // false — 两个不同的堆对象
        System.out.println("a.equals(c): " + a.equals(c)); // true — 内容相同

        // intern() 方法：返回常量池中的引用
        System.out.println("a == c.intern(): " + (a == c.intern())); // true

        System.out.println("\n========== 3. StringBuilder vs StringBuffer ==========");
        // StringBuilder — 非线程安全，单线程推荐
        StringBuilder sb = new StringBuilder();
        sb.append("Hello").append(" ").append("World");
        System.out.println("StringBuilder: " + sb.toString());

        // StringBuffer — 线程安全（方法用synchronized修饰），性能较低
        StringBuffer sbf = new StringBuffer();
        sbf.append("Hello").append(" ").append("World");
        System.out.println("StringBuffer: " + sbf.toString());

        System.out.println("\n========== 4. 字符串拼接性能对比 ==========");
        // ❌ 错误做法：循环中用 + 拼接 — 每次都创建新String，O(n²)
        long start = System.nanoTime();
        String bad = "";
        for (int i = 0; i < 10000; i++) {
            bad = bad + i;  // 每次循环创建一个新String对象
        }
        long stringTime = System.nanoTime() - start;

        // ✅ 正确做法：用 StringBuilder — O(n)
        start = System.nanoTime();
        StringBuilder good = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            good.append(i);
        }
        long sbTime = System.nanoTime() - start;

        System.out.println("String + 拼接耗时: " + stringTime / 1_000_000 + "ms");
        System.out.println("StringBuilder 耗时: " + sbTime / 1_000_000 + "ms");
        System.out.println("性能差距: " + (stringTime / sbTime) + " 倍");

        System.out.println("\n========== 5. 常见String面试题 ==========");
        // 下面代码创建了几个对象？
        String q1 = new String("xyz");  // 答：2个。常量池1个 + 堆1个（如果池里已有"xyz"则只创建堆上1个）

        String q2 = "ja" + "va";        // 答：1个。编译器优化为"java"，只在常量池
        String q3 = "ja";
        String q4 = "va";
        String q5 = q3 + q4;            // 答：底层是 new StringBuilder().append(q3).append(q4).toString()
        System.out.println("q2 == (q3 + q4): " + (q2 == q5)); // false，常量池 vs 堆
    }
}
