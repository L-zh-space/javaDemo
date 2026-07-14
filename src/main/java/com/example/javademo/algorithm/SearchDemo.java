package com.example.javademo.algorithm;

import java.util.Arrays;

/**
 * 查找算法手写实现
 *
 * 【面试常问】
 * - 二分查找：有序数组中查找目标值 O(logn)
 * - 二分查找变体：找第一个/最后一个等于target的位置、找第一个大于等于target的位置
 * - 面试中最容易出错的算法之一（边界条件）
 *
 * 【常见坑】
 * - mid = (left + right) / 2 可能溢出 → 建议用 left + (right - left) / 2
 * - while 条件用 left <= right 还是 left < right？
 * - 搜索区间是 [left, right] 还是 [left, right)？
 */
public class SearchDemo {

    public static void main(String[] args) {
        int[] arr = {1, 3, 5, 5, 5, 7, 9, 11, 13, 15};
        System.out.println("有序数组: " + Arrays.toString(arr));

        System.out.println("\n========== 1. 基本二分查找 ==========");
        for (int target : new int[]{5, 7, 2}) {
            int idx = binarySearch(arr, target);
            System.out.println("  查找 " + target + ": "
                    + (idx >= 0 ? "找到，索引=" + idx : "未找到"));
        }

        System.out.println("\n========== 2. 查找第一个等于target的位置 ==========");
        System.out.println("  第一个5的位置: " + findFirst(arr, 5));
        System.out.println("  最后一个5的位置: " + findLast(arr, 5));

        System.out.println("\n========== 3. 查找第一个 >= target 的位置 ==========");
        System.out.println("  第一个 >= 6 的位置: " + lowerBound(arr, 6));
        System.out.println("  （arr[5]=7 是第一个>=6的元素）");

        System.out.println("\n========== 4. 二分查找常见错误 ==========");
        commonMistakes();
    }

    // ==================== 基本二分查找 ====================
    // 搜索区间 [left, right] — 左右都闭
    static int binarySearch(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;

        // left <= right — 因为区间[left, right]非空时才继续
        while (left <= right) {
            // ⚠️ 防止溢出：不用 (left+right)/2
            int mid = left + (right - left) / 2;

            if (arr[mid] == target) {
                return mid;  // 找到
            } else if (arr[mid] < target) {
                left = mid + 1;   // target在右边
            } else {
                right = mid - 1;  // target在左边
            }
        }
        return -1;  // 没找到
    }

    // 搜索区间[left, right)版本 — 左闭右开
    static int binarySearchV2(int[] arr, int target) {
        int left = 0;
        int right = arr.length;  // 注意：不是len-1

        // left < right — 区间[left, right)非空时继续
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] == target) {
                return mid;
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid;  // 注意：不是 mid-1，因为右边界不包含
            }
        }
        return -1;
    }

    // ==================== 查找第一个等于target ====================
    // 比如 [1,5,5,5,7] 中查找5，返回索引1（第一个5）
    static int findFirst(int[] arr, int target) {
        int left = 0, right = arr.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] == target) {
                // 关键：找到后不直接返回，继续向左搜索
                // 如果mid是0或者前一个元素不等于target，说明这就是第一个
                if (mid == 0 || arr[mid - 1] != target) {
                    return mid;
                }
                right = mid - 1;  // 继续向左找
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }

    // ==================== 查找最后一个等于target ====================
    static int findLast(int[] arr, int target) {
        int left = 0, right = arr.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] == target) {
                // 继续向右搜索
                if (mid == arr.length - 1 || arr[mid + 1] != target) {
                    return mid;
                }
                left = mid + 1;  // 继续向右找
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }

    // ==================== 查找第一个 >= target ====================
    // 俗称 lower_bound — C++ STL中的用法
    static int lowerBound(int[] arr, int target) {
        int left = 0, right = arr.length;  // 可能返回 arr.length（全都小于target）

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] >= target) {
                right = mid;   // mid可能是答案，但不能排除
            } else {
                left = mid + 1;
            }
        }
        return left;  // left 就是答案
    }

    static void commonMistakes() {
        System.out.println("常见错误1: mid计算溢出");
        int bigLeft = Integer.MAX_VALUE / 2;
        int bigRight = Integer.MAX_VALUE;
        int badMid = (bigLeft + bigRight) / 2;    // 溢出 → 负数
        int goodMid = bigLeft + (bigRight - bigLeft) / 2;  // 安全
        System.out.println("  错误写法(溢出): " + badMid);
        System.out.println("  正确写法: " + goodMid);

        System.out.println("\n常见错误2: while条件用错");
        System.out.println("  [left, right] → while(left <= right)");
        System.out.println("  [left, right) → while(left < right)");

        System.out.println("\n常见错误3: 边界更新错误");
        System.out.println("  [left, right]: left=mid+1, right=mid-1");
        System.out.println("  [left, right): left=mid+1, right=mid");

        System.out.println("\n记忆口诀:");
        System.out.println("  左闭右闭 [L,R] — while(L<=R) — R=mid-1");
        System.out.println("  左闭右开 [L,R) — while(L<R)  — R=mid");
    }
}
