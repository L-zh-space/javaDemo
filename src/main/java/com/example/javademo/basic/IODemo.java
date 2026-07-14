package com.example.javademo.basic;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Java IO流详解
 *
 * 【面试常问】
 * - IO流的分类：字节流 vs 字符流，输入流 vs 输出流，节点流 vs 处理流
 * - 字节流：InputStream / OutputStream（用于二进制文件：图片、视频）
 * - 字符流：Reader / Writer（用于文本文件，处理编码）
 * - 缓冲流（BufferedXxx）为什么快？减少了磁盘IO次数
 * - 序列化：Serializable 接口，transient 关键字，serialVersionUID
 * - NIO：Path / Files 工具类
 *
 * 【常见坑】
 * - 字符流 vs 字节流混用导致乱码
 * - 忘记关闭流导致资源泄漏（用try-with-resources解决）
 * - FileReader 使用系统默认编码，不同环境可能不一致
 */
public class IODemo {

    // 测试用的临时文件路径
    private static final String TEST_FILE = "test_io_demo.txt";

    // ==================== 序列化演示类 ====================
    static class User implements Serializable {
        // ⚠️ 序列化版本号：不指定的话JVM自动生成，类结构变化会导致反序列化失败
        private static final long serialVersionUID = 1L;

        private String name;
        private int age;
        // transient 修饰的字段不会被序列化
        private transient String password;

        public User(String name, int age, String password) {
            this.name = name;
            this.age = age;
            this.password = password;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", password='" + password + "'}";
        }
    }

    public static void main(String[] args) {
        System.out.println("========== 1. 字节流（FileInputStream / FileOutputStream）==========");
        writeWithByteStream();
        readWithByteStream();

        System.out.println("\n========== 2. 字符流（FileReader / FileWriter）==========");
        writeWithCharStream();
        readWithCharStream();

        System.out.println("\n========== 3. 缓冲流（BufferedReader / BufferedWriter）==========");
        writeWithBuffer();
        readWithBuffer();

        System.out.println("\n========== 4. 对象序列化（ObjectOutputStream）==========");
        serializeDemo();

        System.out.println("\n========== 5. NIO Files 工具类 ==========");
        nioDemo();

        // 清理测试文件
        try { Files.deleteIfExists(Path.of(TEST_FILE)); } catch (IOException ignored) {}
        try { Files.deleteIfExists(Path.of("user.ser")); } catch (IOException ignored) {}
    }

    // 字节流写入 — 适合二进制文件
    static void writeWithByteStream() {
        // try-with-resources 自动关闭流
        try (FileOutputStream fos = new FileOutputStream(TEST_FILE)) {
            String content = "Hello, 字节流！";
            fos.write(content.getBytes(StandardCharsets.UTF_8));  // 字符串→字节
            System.out.println("字节流写入成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 字节流读取
    static void readWithByteStream() {
        try (FileInputStream fis = new FileInputStream(TEST_FILE)) {
            byte[] buffer = new byte[1024];
            int len = fis.read(buffer);
            String content = new String(buffer, 0, len, StandardCharsets.UTF_8);
            System.out.println("字节流读取: " + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 字符流写入 — 适合文本文件，自动处理编码
    static void writeWithCharStream() {
        try (FileWriter fw = new FileWriter(TEST_FILE, StandardCharsets.UTF_8)) {
            fw.write("你好，字符流！\n第二行内容");
            System.out.println("字符流写入成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 字符流读取
    static void readWithCharStream() {
        try (FileReader fr = new FileReader(TEST_FILE, StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int len = fr.read(buffer);
            System.out.println("字符流读取: " + new String(buffer, 0, len));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 缓冲流 — 带缓冲区减少IO次数，性能更高
    static void writeWithBuffer() {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(TEST_FILE, StandardCharsets.UTF_8))) {
            bw.write("第一行：缓冲流写入");
            bw.newLine();  // 跨平台的换行符
            bw.write("第二行：效率更高（自带8KB缓冲区）");
            bw.newLine();
            bw.write("第三行：减少磁盘IO次数");
            System.out.println("缓冲流写入成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void readWithBuffer() {
        try (BufferedReader br = new BufferedReader(
                new FileReader(TEST_FILE, StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 1;
            // readLine() 逐行读取，返回 null 表示读完
            while ((line = br.readLine()) != null) {
                System.out.println("  行" + lineNum++ + ": " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 序列化演示
    static void serializeDemo() {
        User user = new User("张三", 25, "123456");
        System.out.println("序列化前: " + user);

        // 序列化（对象 → 字节流 → 文件）
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("user.ser"))) {
            oos.writeObject(user);
            System.out.println("序列化成功");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 反序列化（文件 → 字节流 → 对象）
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("user.ser"))) {
            User restored = (User) ois.readObject();
            System.out.println("反序列化后: " + restored);
            // 注意 password 为 null — 因为被 transient 修饰
            System.out.println("（password=null 因为 transient 修饰，不参与序列化）");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // NIO Files 工具类演示
    static void nioDemo() {
        try {
            Path path = Path.of(TEST_FILE);
            // 一次性读取所有行
            System.out.println("Files.readAllLines: " + Files.readAllLines(path, StandardCharsets.UTF_8));
            // 一次性读取全部内容
            String content = Files.readString(path, StandardCharsets.UTF_8);
            System.out.println("Files.readString: " + content.replace("\r\n", " | "));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
