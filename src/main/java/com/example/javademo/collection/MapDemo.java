package com.example.javademo.collection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HashMap / Hashtable / TreeMap / LinkedHashMap 详解
 *
 * 【面试常问 — HashMap 是重中之重】
 * - HashMap 底层结构：JDK8+ = 数组 + 链表 + 红黑树（链表长度>=8且数组长度>=64时转红黑树）
 * - put 流程：hash(key) -> (n-1)&hash 定位数组下标 -> 链表/红黑树插入 -> 判断扩容
 * - hash 方法：(h = key.hashCode()) ^ (h >>> 16)，高16位参与运算，减少碰撞
 * - 默认容量16，负载因子0.75，扩容阈值=容量x负载因子
 * - 扩容时容量翻倍，rehash重新分配位置（原位置 或 原位置+旧容量）
 * - HashMap 线程不安全（put可能导致数据丢失、resize可能死循环JDK7）
 * - ConcurrentHashMap 线程安全（JDK8: CAS + synchronized，锁粒度在链表头节点）
 *
 * 【常见坑】
 * - 作为key的对象必须正确重写 equals() 和 hashCode()
 * - HashMap允许null key和null value，Hashtable不允许
 * - 多线程环境必须用 ConcurrentHashMap
 */
public class MapDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. HashMap 基本操作 ==========");
        HashMap<String, Integer> map = new HashMap<>();
        map.put("apple", 10);
        map.put("banana", 20);
        map.put("orange", 30);
        map.put("apple", 100);  // key相同，覆盖旧值
        System.out.println("HashMap: " + map);
        System.out.println("apple的值: " + map.get("apple"));
        System.out.println("包含banana? " + map.containsKey("banana"));

        // 遍历方式
        System.out.print("遍历: ");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.print(entry.getKey() + "=" + entry.getValue() + " ");
        }
        System.out.println();

        System.out.println("\n========== 2. HashMap 哈希冲突演示 ==========");
        hashCollisionDemo();

        System.out.println("\n========== 3. HashMap vs Hashtable ==========");
        hashMapVsHashTable();

        System.out.println("\n========== 4. TreeMap（有序）==========");
        treeMapDemo();

        System.out.println("\n========== 5. LinkedHashMap（保持插入/访问顺序）==========");
        linkedHashMapDemo();

        System.out.println("\n========== 6. ConcurrentHashMap ==========");
        concurrentHashMapDemo();

        System.out.println("\n========== 7. equals和hashCode的重要性 ==========");
        equalsHashCodeDemo();
    }

    // 演示hash冲突：不同key定位到同一个数组位置
    static void hashCollisionDemo() {
        // 自定义hashCode固定返回1的类——演示极端冲突
        class BadKey {
            String name;
            BadKey(String name) { this.name = name; }

            @Override
            public int hashCode() { return 1; }  // 全部冲突！

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof BadKey)) return false;
                return Objects.equals(name, ((BadKey) o).name);
            }

            @Override
            public String toString() { return "BadKey{'" + name + "'}"; }
        }

        HashMap<BadKey, String> badMap = new HashMap<>();
        badMap.put(new BadKey("张三"), "值1");
        badMap.put(new BadKey("李四"), "值2");
        badMap.put(new BadKey("王五"), "值3");
        System.out.println("3个不同key（hashCode全=1）放入同一HashMap: " + badMap);
        System.out.println("它们都在同一链表上，通过equals区分不同元素");
        System.out.println("JDK8+ 当链表>=8且数组>=64时会转红黑树（O(n)->O(logn)）");
    }

    static void hashMapVsHashTable() {
        HashMap<String, String> hm = new HashMap<>();
        hm.put(null, "null-value");  // HashMap 允许 null key
        hm.put("key", null);          // 允许 null value
        System.out.println("HashMap null key: " + hm.get(null));

        try {
            Hashtable<String, String> ht = new Hashtable<>();
            ht.put(null, "value");  // Hashtable 不允许 null
        } catch (NullPointerException e) {
            System.out.println("Hashtable.put(null) -> NullPointerException");
        }
        System.out.println("区别: HashMap非线程安全允许null，Hashtable线程安全(synchronized)不允许null");
    }

    static void treeMapDemo() {
        TreeMap<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("banana", 2);
        treeMap.put("apple", 1);
        treeMap.put("orange", 3);
        System.out.println("TreeMap（自动排序）: " + treeMap);
        System.out.println("第一个key: " + treeMap.firstKey());
        System.out.println("最后一个key: " + treeMap.lastKey());
        System.out.println("获取 apple~orange 范围: " + treeMap.subMap("apple", "orange"));
    }

    static void linkedHashMapDemo() {
        // LRU缓存常用 — accessOrder=true 按访问顺序排序
        LinkedHashMap<String, Integer> lhm = new LinkedHashMap<>(16, 0.75f, true);
        lhm.put("A", 1); lhm.put("B", 2); lhm.put("C", 3);
        System.out.println("初始顺序: " + lhm.keySet());  // [A, B, C]

        lhm.get("A");  // 访问A，A会被移到末尾
        System.out.println("访问A后: " + lhm.keySet());  // [B, C, A]

        lhm.get("B");
        System.out.println("访问B后: " + lhm.keySet());  // [C, A, B]

        System.out.println("accessOrder=true 适合实现LRU缓存（最近最少使用淘汰）");
    }

    static void concurrentHashMapDemo() {
        Map<String, Integer> chm = new ConcurrentHashMap<>();
        chm.put("a", 1); chm.put("b", 2);
        System.out.println("ConcurrentHashMap: " + chm);
        System.out.println("特点: JDK7分段锁 -> JDK8 CAS+synchronized，锁粒度在链表头");

        // putIfAbsent — 原子操作，不存在才put
        chm.putIfAbsent("a", 100);
        chm.putIfAbsent("c", 3);
        System.out.println("putIfAbsent后: " + chm);
    }

    // 演示：只重写equals没重写hashCode的后果
    static void equalsHashCodeDemo() {
        class Person {
            String name;
            Person(String name) { this.name = name; }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Person)) return false;
                return Objects.equals(name, ((Person) o).name);
            }
            // 没有重写 hashCode() — HashMap无法正确定位
        }

        class PersonFixed {
            String name;
            PersonFixed(String name) { this.name = name; }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof PersonFixed)) return false;
                return Objects.equals(name, ((PersonFixed) o).name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name);
            }
        }

        // 错误示例：没重写hashCode
        HashMap<Person, String> bad = new HashMap<>();
        Person p1 = new Person("张三");
        bad.put(p1, "值");
        System.out.println("原对象get: " + bad.get(p1));                // 能找到
        System.out.println("新对象(内容相同)get: " + bad.get(new Person("张三"))); // null!

        // 正确示例：同时重写了equals和hashCode
        HashMap<PersonFixed, String> good = new HashMap<>();
        good.put(new PersonFixed("张三"), "值");
        System.out.println("修复后新对象get: " + good.get(new PersonFixed("张三")));
        System.out.println("结论: HashMap的key必须正确重写equals()和hashCode()！");
    }
}
