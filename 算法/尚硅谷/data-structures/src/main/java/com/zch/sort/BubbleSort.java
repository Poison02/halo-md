package com.zch.sort;

import java.util.Arrays;

/**
 * 冒泡排序
 * @author Zch
 * @date 2023/10/7
 **/
public class BubbleSort {

    public static void main(String[] args) {
        int[] arr = new int[] {3, 9, -1, 10, -2};
        bubbleSort(arr);
    }

    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        int tmp = 0;
        boolean flag = false;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换过一次就flag = true
                    flag = true;
                    tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                }
            }
            System.out.println("这是第" + (i + 1) + "趟排序");
            System.out.println(Arrays.toString(arr));

            if (! flag) {
                // 如果说没有交换过就直接退出
                break;
            } else {
                // 这里需要重置！
                flag = false;
            }
        }
    }

}
