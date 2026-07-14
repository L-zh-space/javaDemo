package com.example.javademo.basic;

import java.io.*;

/**
 * Java异常体系详解
 *
 * 【面试常问】
 * - Throwable → Error（不可处理） + Exception（可处理）
 * - Exception → RuntimeException（非受检） + 其他Exception（受检，必须try-catch或throws）
 * - try-catch-finally 执行顺序，finally 一定会执行吗？
 * - throw vs throws 区别
 *
 * 【常见坑】
 * - finally 中如果有 return，会覆盖 try 中的 return
 * - catch 多个异常时，子类异常必须写在父类前面
 * - 不要在 finally 中抛异常，会覆盖 try 中的异常
 */
public class ExceptionDemo {

    // ==================== 自定义异常 ====================
    // 受检异常（Checked Exception）：继承 Exception
    static class BusinessException extends Exception {
        public BusinessException(String message) {
            super(message);
        }
    }

    // 非受检异常（Unchecked Exception）：继承 RuntimeException
    static class BizRuntimeException extends RuntimeException {
        public BizRuntimeException(String message) {
            super(message);
        }
    }

    // 模拟一个可能失败的业务方法
    static int divide(int a, int b) throws BusinessException {
        if (b == 0) {
            // 受检异常必须声明 throws 或 try-catch
            throw new BusinessException("除数不能为0！");
        }
        return a / b;
    }

    // 模拟空指针场景
    static int getLength(String s) {
        if (s == null) {
            // 非受检异常可以不声明 throws
            throw new BizRuntimeException("字符串不能为null");
        }
        return s.length();
    }

    public static void main(String[] args) {
        System.out.println("========== 1. 异常体系 ==========");
        System.out.println("Throwable");
        System.out.println("  ├── Error（不可恢复，如OOM、StackOverflow）");
        System.out.println("  └── Exception");
        System.out.println("        ├── RuntimeException（非受检，如NPE、数组越界）");
        System.out.println("        └── 其他Exception（受检，如IOException、SQLException）");

        System.out.println("\n========== 2. try-catch-finally 执行顺序 ==========");
        System.out.println("结果: " + tryCatchFinallyDemo());

        System.out.println("\n========== 3. finally 中 return 的陷阱 ==========");
        System.out.println("结果: " + finallyReturnTrap());  // 返回2，不是1！

        System.out.println("\n========== 4. 受检异常处理 ==========");
        try {
            System.out.println("10 / 3 = " + divide(10, 3));
            System.out.println("10 / 0 = " + divide(10, 0));  // 抛异常
        } catch (BusinessException e) {
            System.out.println("捕获到业务异常: " + e.getMessage());
        }

        System.out.println("\n========== 5. 非受检异常 ==========");
        try {
            getLength(null);
        } catch (BizRuntimeException e) {
            System.out.println("捕获到运行时异常: " + e.getMessage());
        }

        System.out.println("\n========== 6. try-with-resources（自动关闭资源）==========");
        // Java7+ 语法，实现了 AutoCloseable 的资源会自动关闭
        // 不需要手动写 finally { if (xxx != null) xxx.close(); }
        try (BufferedReader reader = new BufferedReader(
                new StringReader("Hello\nWorld"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("读取: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // reader 在这里已被自动关闭

        System.out.println("\n========== 7. 多catch顺序 ==========");
        try {
            int[] arr = new int[3];
            arr[5] = 10;  // ArrayIndexOutOfBoundsException
        } catch (ArrayIndexOutOfBoundsException e) {
            // 子类异常必须在前，否则编译报错
            System.out.println("捕获到数组越界: " + e.getClass().getSimpleName());
        } catch (RuntimeException e) {
            System.out.println("捕获到运行时异常");
        }
    }

    // 演示 try-catch-finally 执行顺序和返回值
    static int tryCatchFinallyDemo() {
        try {
            System.out.println("  try 块执行");
            return 1;
        } catch (Exception e) {
            System.out.println("  catch 块执行");
            return 2;
        } finally {
            System.out.println("  finally 块执行（总是执行）");
        }
    }

    // ⚠️ 陷阱：finally中的return会覆盖try中的return
    static int finallyReturnTrap() {
        try {
            return 1;  // 这个return会被覆盖
        } finally {
            return 2;  // ⚠️ 不推荐！会吞掉所有异常，还改变返回值
        }
    }
}
