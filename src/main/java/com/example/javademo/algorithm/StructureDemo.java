package com.example.javademo.algorithm;

/**
 * 手写数据结构：链表、栈、队列
 *
 * 【面试常问】
 * - 链表反转（迭代法 + 递归法）
 * - 链表是否有环（快慢指针）
 * - 栈的实现（数组版 + 链表版）
 * - 队列的实现（数组版 + 链表版）
 *
 * 【常见坑】
 * - 链表操作注意空指针
 * - 链表反转时注意保存next引用
 * - 循环队列的判空/判满条件
 */
public class StructureDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. 手写单向链表 ==========");
        ListNode head = buildList(new int[]{1, 2, 3, 4, 5});
        System.out.print("原始链表: ");
        printList(head);

        System.out.println("\n========== 2. 链表反转（迭代法）==========");
        head = reverseListIterative(head);
        System.out.print("反转后: ");
        printList(head);

        System.out.println("\n========== 3. 链表反转（递归法）==========");
        head = reverseListRecursive(head);
        System.out.print("再次反转: ");
        printList(head);

        System.out.println("\n========== 4. 检测环形链表 ==========");
        detectCycle(head);

        System.out.println("\n========== 5. 手写栈（数组实现）==========");
        stackDemo();

        System.out.println("\n========== 6. 手写队列（链表实现）==========");
        queueDemo();
    }

    // ==================== 链表节点 ====================
    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) { this.val = val; }
    }

    static ListNode buildList(int[] arr) {
        ListNode dummy = new ListNode(0);
        ListNode cur = dummy;
        for (int v : arr) {
            cur.next = new ListNode(v);
            cur = cur.next;
        }
        return dummy.next;
    }

    static void printList(ListNode head) {
        ListNode cur = head;
        while (cur != null) {
            System.out.print(cur.val);
            if (cur.next != null) System.out.print(" → ");
            cur = cur.next;
        }
        System.out.println();
    }

    // ==================== 链表反转（迭代）====================
    // 核心：用3个指针 prev→cur→next，依次翻转箭头
    static ListNode reverseListIterative(ListNode head) {
        ListNode prev = null;
        ListNode cur = head;

        while (cur != null) {
            ListNode next = cur.next;  // 1. 先保存下一个节点
            cur.next = prev;           // 2. 翻转箭头
            prev = cur;                // 3. prev前进
            cur = next;                // 4. cur前进
        }
        return prev;  // prev就是新的头节点
    }

    // ==================== 链表反转（递归）====================
    static ListNode reverseListRecursive(ListNode head) {
        // 递归终止：空链表或只有一个节点
        if (head == null || head.next == null) {
            return head;
        }
        // 递归反转后面的链表
        ListNode newHead = reverseListRecursive(head.next);
        // 把当前节点放到反转后的后面
        head.next.next = head;
        head.next = null;
        return newHead;
    }

    // ==================== 检测环形链表（快慢指针）====================
    static void detectCycle(ListNode head) {
        // 制造一个环方便演示
        ListNode cycleHead = buildList(new int[]{1, 2, 3, 4, 5});
        // 5指向3，形成环
        ListNode cur = cycleHead;
        while (cur.next != null) cur = cur.next;
        cur.next = cycleHead.next.next;  // 5 → 3

        // Floyd判圈算法
        ListNode slow = cycleHead;
        ListNode fast = cycleHead;
        boolean hasCycle = false;

        while (fast != null && fast.next != null) {
            slow = slow.next;       // 慢指针走1步
            fast = fast.next.next;  // 快指针走2步
            if (slow == fast) {     // 相遇 → 有环
                hasCycle = true;
                break;
            }
        }

        System.out.println("有环? " + hasCycle);
        if (hasCycle) {
            // 找环的入口：重新让一个指针从头走，另一个从相遇点走
            slow = cycleHead;
            // 注意：这里用原来的cur（即fast/相遇点）
            while (slow != fast) {
                slow = slow.next;
                fast = fast.next;
            }
            System.out.println("环的入口节点值: " + slow.val);
        }

        // 断开环避免内存问题（仅演示用）
        cur.next = null;
    }

    // ==================== 手写栈（数组实现）====================
    static void stackDemo() {
        class MyStack {
            private int[] data;
            private int top;  // 栈顶指针，指向下一个可插入位置

            MyStack(int capacity) {
                data = new int[capacity];
                top = 0;
            }

            void push(int val) {
                if (top == data.length) {
                    System.out.println("    栈满！");
                    return;
                }
                data[top++] = val;
            }

            int pop() {
                if (top == 0) {
                    System.out.println("    栈空！");
                    return -1;
                }
                return data[--top];
            }

            int peek() {
                if (top == 0) return -1;
                return data[top - 1];
            }

            boolean isEmpty() { return top == 0; }
            int size() { return top; }
        }

        MyStack stack = new MyStack(5);
        stack.push(1); stack.push(2); stack.push(3);
        System.out.println("入栈: 1, 2, 3");
        System.out.println("栈顶: " + stack.peek());
        System.out.println("出栈: " + stack.pop());  // 3
        System.out.println("出栈: " + stack.pop());  // 2
        System.out.println("栈大小: " + stack.size());
    }

    // ==================== 手写队列（链表实现）====================
    static void queueDemo() {
        class MyQueue {
            private ListNode head;  // 队头（出队）
            private ListNode tail;  // 队尾（入队）
            private int size;

            MyQueue() {
                head = null;
                tail = null;
                size = 0;
            }

            void enqueue(int val) {
                ListNode node = new ListNode(val);
                if (tail == null) {
                    head = tail = node;
                } else {
                    tail.next = node;
                    tail = node;
                }
                size++;
            }

            int dequeue() {
                if (head == null) {
                    System.out.println("    队列空！");
                    return -1;
                }
                int val = head.val;
                head = head.next;
                if (head == null) tail = null;
                size--;
                return val;
            }

            int peek() {
                return head != null ? head.val : -1;
            }

            boolean isEmpty() { return head == null; }
            int size() { return size; }
        }

        MyQueue queue = new MyQueue();
        queue.enqueue(1); queue.enqueue(2); queue.enqueue(3);
        System.out.println("入队: 1, 2, 3");
        System.out.println("队头: " + queue.peek());
        System.out.println("出队: " + queue.dequeue());  // 1
        System.out.println("出队: " + queue.dequeue());  // 2
        System.out.println("队列大小: " + queue.size());
        System.out.println("出队: " + queue.dequeue());  // 3
        System.out.println("队列空? " + queue.isEmpty());
    }
}
