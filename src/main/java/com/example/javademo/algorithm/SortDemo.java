package com.example.javademo.algorithm;

import java.util.Arrays;

/**
 * 排序算法手写实现
 *
 * 【面试常问 — 5种排序算法复杂度对比】
 * ┌──────────┬──────────┬──────────┬──────────┬──────────┐
 * │ 算法     │ 最好     │ 平均     │ 最坏     │ 空间     │
 * ├──────────┼──────────┼──────────┼──────────┼──────────┤
 * │ 冒泡     │ O(n)     │ O(n²)    │ O(n²)    │ O(1)     │
 * │ 选择     │ O(n²)    │ O(n²)    │ O(n²)    │ O(1)     │
 * │ 插入     │ O(n)     │ O(n²)    │ O(n²)    │ O(1)     │
 * │ 快速     │ O(nlogn) │ O(nlogn) │ O(n²)    │ O(logn)  │
 * │ 归并     │ O(nlogn) │ O(nlogn) │ O(nlogn) │ O(n)     │
 * └──────────┴──────────┴──────────┴──────────┴──────────┘
 * 稳定：冒泡、插入、归并 | 不稳定：选择、快排
 *
 * 【常见坑】
 * - 快排的基准选择影响性能（有序数组选第一个为基准 → 退化为O(n²)）
 * - 归并排序需要额外O(n)空间
 * - Arrays.sort() 对基本类型用快排，对对象类型用归并排序（Timsort）
 */
public class SortDemo {

    public static void main(String[] args) {
        int[] arr = {5, 2, 8, 3, 1, 9, 4, 7, 6};

        System.out.println("========== 1. 冒泡排序 O(n²) ==========");
        bubbleSort(arr.clone());

        System.out.println("\n========== 2. 选择排序 O(n²) ==========");
        selectionSort(arr.clone());

        System.out.println("\n========== 3. 插入排序 O(n²) ==========");
        insertionSort(arr.clone());

        System.out.println("\n========== 4. 快速排序 O(nlogn) ==========");
        int[] arr4 = arr.clone();
        quickSort(arr4, 0, arr4.length - 1);
        System.out.println("结果: " + Arrays.toString(arr4));

        System.out.println("\n========== 5. 归并排序 O(nlogn) ==========");
        int[] arr5 = arr.clone();
        mergeSort(arr5, 0, arr5.length - 1);
        System.out.println("结果: " + Arrays.toString(arr5));

        System.out.println("\n========== 6. 性能对比 ==========");
        benchmark();
    }

    // ==================== 1. 冒泡排序 ====================
    // 相邻元素两两比较，大的往后"冒"
    static void bubbleSort(int[] arr) {
        int n = arr.length;
        System.out.println("初始: " + Arrays.toString(arr));

        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;  // 优化：如果一轮没有交换，说明已有序
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            System.out.println("  第" + (i + 1) + "轮: " + Arrays.toString(arr));
            if (!swapped) break;  // 已有序，提前结束
        }
    }

    // ==================== 2. 选择排序 ====================
    // 每次选择未排序部分的最小值，放到已排序部分的末尾
    static void selectionSort(int[] arr) {
        int n = arr.length;
        System.out.println("初始: " + Arrays.toString(arr));

        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            // 找 i 之后的最小值
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            // 交换
            int temp = arr[i];
            arr[i] = arr[minIndex];
            arr[minIndex] = temp;
            System.out.println("  第" + (i + 1) + "轮: " + Arrays.toString(arr));
        }
    }

    // ==================== 3. 插入排序 ====================
    // 像打扑克牌一样，把新元素插入到已排序部分的正确位置
    static void insertionSort(int[] arr) {
        int n = arr.length;
        System.out.println("初始: " + Arrays.toString(arr));

        for (int i = 1; i < n; i++) {
            int key = arr[i];  // 要插入的元素
            int j = i - 1;
            // 把比key大的元素都往后移
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;  // 插入到正确位置
            System.out.println("  第" + i + "轮: " + Arrays.toString(arr) + " (插入" + key + ")");
        }
    }

    // ==================== 4. 快速排序 ====================
    // 选基准 → 分区（左边小右边大）→ 递归排序左右
    static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high);
            quickSort(arr, low, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, high);
        }
    }

    // 分区函数：选第一个元素为基准，左边放小的，右边放大的
    static int partition(int[] arr, int low, int high) {
        int pivot = arr[low];  // 选第一个元素为基准（可优化为随机选择）
        int i = low;
        int j = high;

        while (i < j) {
            // 从右向左找第一个小于pivot的
            while (i < j && arr[j] >= pivot) j--;
            // 从左向右找第一个大于pivot的
            while (i < j && arr[i] <= pivot) i++;
            // 交换
            if (i < j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        // 基准归位
        arr[low] = arr[i];
        arr[i] = pivot;
        return i;
    }

    // ==================== 5. 归并排序 ====================
    // 分治：对半拆分 → 递归排序 → 合并两个有序数组
    static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;  // 防止溢出写法
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    // 合并两个有序子数组 [left, mid] 和 [mid+1, right]
    static void merge(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left;       // 左子数组指针
        int j = mid + 1;    // 右子数组指针
        int k = 0;          // temp指针

        // 两个子数组比较，小的先放入temp
        while (i <= mid && j <= right) {
            temp[k++] = (arr[i] <= arr[j]) ? arr[i++] : arr[j++];
        }
        // 把剩余元素放入
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];

        // 复制回原数组
        System.arraycopy(temp, 0, arr, left, temp.length);
    }

    // ==================== 性能对比 ====================
    static void benchmark() {
        int size = 10000;
        int[] original = new int[size];
        for (int i = 0; i < size; i++) {
            original[i] = (int) (Math.random() * size);
        }

        System.out.println("对 " + size + " 个随机数排序：");

        int[] arr;

        arr = original.clone();
        long start = System.nanoTime();
        bubbleSortSilent(arr);
        System.out.println("  冒泡排序: " + (System.nanoTime() - start) / 1_000_000 + " ms");

        arr = original.clone();
        start = System.nanoTime();
        selectionSortSilent(arr);
        System.out.println("  选择排序: " + (System.nanoTime() - start) / 1_000_000 + " ms");

        arr = original.clone();
        start = System.nanoTime();
        insertionSortSilent(arr);
        System.out.println("  插入排序: " + (System.nanoTime() - start) / 1_000_000 + " ms");

        arr = original.clone();
        start = System.nanoTime();
        quickSort(arr, 0, arr.length - 1);
        System.out.println("  快速排序: " + (System.nanoTime() - start) / 1_000_000 + " ms");

        arr = original.clone();
        start = System.nanoTime();
        mergeSort(arr, 0, arr.length - 1);
        System.out.println("  归并排序: " + (System.nanoTime() - start) / 1_000_000 + " ms");

        arr = original.clone();
        start = System.nanoTime();
        Arrays.sort(arr);
        System.out.println("  JDK自带:  " + (System.nanoTime() - start) / 1_000_000 + " ms (DualPivotQuickSort)");
    }

    // 静默版（不打印每一轮）
    static void bubbleSortSilent(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    static void selectionSortSilent(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) minIdx = j;
            }
            int temp = arr[i]; arr[i] = arr[minIdx]; arr[minIdx] = temp;
        }
    }

    static void insertionSortSilent(int[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            int key = arr[i], j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j]; j--;
            }
            arr[j + 1] = key;
        }
    }
}
