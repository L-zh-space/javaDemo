package com.example.javademo.collection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ArrayList vs LinkedList 详解
 *
 * 【面试常问】
 * - ArrayList：底层 Object[] 数组，随机访问 O(1)，尾部插入 O(1)（摊销），中间插入 O(n)
 * - LinkedList：底层双向链表，随机访问 O(n)，头尾插入 O(1)，中间插入 O(1)（找到位置后）
 * - ArrayList 扩容：默认10，每次扩容1.5倍（oldCapacity >> 1）
 * - CopyOnWriteArrayList：写时复制，适合读多写少
 *
 * 【常见坑】
 * - foreach 遍历时不能删除元素，会抛 ConcurrentModificationException
 * - ArrayList.subList() 返回的是视图，修改会影响原List
 * - Arrays.asList() 返回的List不支持 add/remove
 */
public class ListDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. ArrayList 基本操作 ==========");
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("A"); arrayList.add("B"); arrayList.add("C");
        System.out.println("ArrayList: " + arrayList);
        System.out.println("索引1的元素: " + arrayList.get(1));  // O(1) 随机访问
        System.out.println("大小: " + arrayList.size());

        System.out.println("\n========== 2. ArrayList 扩容演示 ==========");
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add(i);
            // 扩容时机：size > elementData.length 时
        }
        System.out.println("添加15个元素后的 list: " + list);

        System.out.println("\n========== 3. LinkedList 基本操作 ==========");
        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.add("X"); linkedList.add("Y"); linkedList.add("Z");
        linkedList.addFirst("HEAD");   // 头部插入 O(1)
        linkedList.addLast("TAIL");    // 尾部插入 O(1)
        System.out.println("LinkedList: " + linkedList);
        System.out.println("第一个: " + linkedList.getFirst());
        System.out.println("最后一个: " + linkedList.getLast());

        System.out.println("\n========== 4. ArrayList vs LinkedList 性能对比 ==========");
        comparePerformance();

        System.out.println("\n========== 5. foreach 删除陷阱 ==========");
        foreachRemoveTrap();

        System.out.println("\n========== 6. Arrays.asList 陷阱 ==========");
        arraysAsListTrap();

        System.out.println("\n========== 7. 迭代器正确删除 ==========");
        iteratorRemove();

        System.out.println("\n========== 8. CopyOnWriteArrayList ==========");
        copyOnWriteDemo();
    }

    static void comparePerformance() {
        int size = 100000;

        // ArrayList 尾部插入
        long start = System.nanoTime();
        ArrayList<Integer> al = new ArrayList<>();
        for (int i = 0; i < size; i++) al.add(i);
        long alAddTime = System.nanoTime() - start;

        // LinkedList 尾部插入
        start = System.nanoTime();
        LinkedList<Integer> ll = new LinkedList<>();
        for (int i = 0; i < size; i++) ll.add(i);
        long llAddTime = System.nanoTime() - start;

        // ArrayList 随机访问
        start = System.nanoTime();
        for (int i = 0; i < size; i++) al.get((i * 12345) % size);
        long alGetTime = System.nanoTime() - start;

        // LinkedList 随机访问（很慢！）
        start = System.nanoTime();
        for (int i = 0; i < size; i++) ll.get((i * 12345) % size);
        long llGetTime = System.nanoTime() - start;

        System.out.println("--- 10万次操作 ---");
        System.out.println("尾部插入: ArrayList=" + alAddTime / 1_000_000 + "ms, "
                + "LinkedList=" + llAddTime / 1_000_000 + "ms");
        System.out.println("随机访问: ArrayList=" + alGetTime / 1_000_000 + "ms, "
                + "LinkedList=" + llGetTime / 1_000_000 + "ms");
        System.out.println("结论: ArrayList适合随机访问，LinkedList适合频繁头尾增删");
    }

    static void foreachRemoveTrap() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        try {
            for (String s : list) {
                if ("B".equals(s)) {
                    list.remove(s);  // ❌ ConcurrentModificationException
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("❌ foreach中remove抛出: " + e.getClass().getSimpleName());
            System.out.println("   原因: modCount != expectedModCount");
        }
    }

    static void iteratorRemove() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        // ✅ 方式1：使用迭代器的 remove
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            if ("B".equals(it.next())) {
                it.remove();
            }
        }
        System.out.println("迭代器remove后: " + list);

        // ✅ 方式2：使用 removeIf（Java8+）
        list.removeIf("C"::equals);
        System.out.println("removeIf后: " + list);
    }

    static void arraysAsListTrap() {
        List<String> list = Arrays.asList("A", "B", "C");
        System.out.println("Arrays.asList创建: " + list + " (类型: " + list.getClass().getSimpleName() + ")");
        try {
            list.add("D");  // ❌ 固定大小，不能add
        } catch (UnsupportedOperationException e) {
            System.out.println("❌ Arrays.asList() 返回的List不支持add: " + e.getClass().getSimpleName());
        }
        // ✅ 正确做法：包装一层
        List<String> modifiable = new ArrayList<>(Arrays.asList("A", "B", "C"));
        modifiable.add("D");
        System.out.println("包装后的ArrayList: " + modifiable);
    }

    static void copyOnWriteDemo() {
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
        cowList.add("A"); cowList.add("B"); cowList.add("C");

        // 遍历时修改不会抛异常（读的是旧快照）
        for (String s : cowList) {
            if ("B".equals(s)) {
                cowList.remove(s);  // 写入的是新数组，不影响当前遍历
            }
            System.out.print(s + " ");
        }
        System.out.println("\n遍历后: " + cowList);
        System.out.println("注意：遍历时删除了B，但遍历中仍然打印了B（读的是快照）");
    }
}
