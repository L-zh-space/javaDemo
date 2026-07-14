package com.example.javademo.collection;

import java.util.*;

/**
 * HashSet / TreeSet / LinkedHashSet 详解
 *
 * 【面试常问】
 * - HashSet：底层是 HashMap（key存元素，value是PRESENT常量）
 * - 去重原理：先hashCode定位桶，再用equals比较，两者都相等才判定重复
 * - TreeSet：底层是 TreeMap，元素自动排序，要求元素实现Comparable
 * - LinkedHashSet：底层是 LinkedHashMap，保持插入顺序
 *
 * 【常见坑】
 * - 放入HashSet的对象必须正确重写equals和hashCode
 * - TreeSet的元素必须可比较（实现Comparable或传Comparator）
 * - TreeSet不能放null（需要比较会NPE）
 */
public class SetDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. HashSet 基本操作 ==========");
        HashSet<String> set = new HashSet<>();
        set.add("apple");
        set.add("banana");
        set.add("apple");  // 重复，不会添加
        set.add("orange");
        System.out.println("HashSet: " + set);
        System.out.println("大小: " + set.size());
        System.out.println("包含apple? " + set.contains("apple"));

        System.out.println("\n========== 2. HashSet 去重原理 ==========");
        hashSetDedupDemo();

        System.out.println("\n========== 3. TreeSet（排序）==========");
        treeSetDemo();

        System.out.println("\n========== 4. LinkedHashSet（保持顺序）==========");
        linkedHashSetDemo();

        System.out.println("\n========== 5. Set 集合运算（交/并/差）==========");
        setOperations();
    }

    static void hashSetDedupDemo() {
        class Student {
            String name;
            int id;

            Student(String name, int id) { this.name = name; this.id = id; }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Student)) return false;
                Student s = (Student) o;
                return id == s.id && Objects.equals(name, s.name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, id);
            }

            @Override
            public String toString() { return "Student{name='" + name + "', id=" + id + "}"; }
        }

        HashSet<Student> students = new HashSet<>();
        students.add(new Student("张三", 1));
        students.add(new Student("李四", 2));
        students.add(new Student("张三", 1));  // 内容相同，hashCode和equals都相等 → 不添加
        students.add(new Student("王五", 3));

        System.out.println("Set内容: " + students);
        System.out.println("大小: " + students.size() + " (预期3，内容相同的对象被去重)");
    }

    static void treeSetDemo() {
        // TreeSet 默认自然顺序排序
        TreeSet<Integer> numbers = new TreeSet<>();
        numbers.add(5); numbers.add(2); numbers.add(8); numbers.add(1);
        System.out.println("TreeSet（自动排序）: " + numbers);
        System.out.println("first: " + numbers.first() + ", last: " + numbers.last());
        System.out.println("小于5的元素: " + numbers.headSet(5));
        System.out.println("大于等于5的元素: " + numbers.tailSet(5));

        // 自定义Comparator：按字符串长度排序
        TreeSet<String> byLength = new TreeSet<>(
                Comparator.comparingInt(String::length)
                        .thenComparing(Comparator.naturalOrder())  // 长度相同时按字典序
        );
        byLength.add("aaa");
        byLength.add("bb");
        byLength.add("c");
        byLength.add("dddd");
        byLength.add("ee");  // 长度和"bb"相同(2)，按字典序"ee">"bb"
        System.out.println("按长度排序的TreeSet: " + byLength);
        // 注意：如果长度相同且不去thenComparing，第二个长度相同的元素会被覆盖！
    }

    static void linkedHashSetDemo() {
        LinkedHashSet<String> lhs = new LinkedHashSet<>();
        lhs.add("C");
        lhs.add("A");
        lhs.add("B");
        lhs.add("A");  // 重复，不添加
        System.out.println("LinkedHashSet: " + lhs);
        System.out.println("遍历顺序 = 插入顺序（与HashSet不同）");

        // 对比普通HashSet
        HashSet<String> hs = new HashSet<>();
        hs.add("C"); hs.add("A"); hs.add("B");
        System.out.println("HashSet对比: " + hs + "（无序）");
    }

    static void setOperations() {
        Set<Integer> set1 = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Set<Integer> set2 = new HashSet<>(Arrays.asList(4, 5, 6, 7, 8));

        // 并集
        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);
        System.out.println("并集: " + union);

        // 交集
        Set<Integer> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        System.out.println("交集: " + intersection);

        // 差集（set1有set2没有）
        Set<Integer> difference = new HashSet<>(set1);
        difference.removeAll(set2);
        System.out.println("差集(set1-set2): " + difference);
    }
}
