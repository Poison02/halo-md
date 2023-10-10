package com.zch.sort;

import java.util.Arrays;

/**
 * 希尔排序
 *
 * @author Zch
 * @date 2023/10/10
 **/
public class ShellSort {

    public static void main(String[] args) {
        int[] arr = {8, 9, 1, 7, 2, 3, 5, 4, 6, 0};
        System.out.println("排序前：");
        System.out.println(Arrays.toString(arr));
        shellSort2(arr);
        System.out.println("排序后：");
        System.out.println(Arrays.toString(arr));
    }

    public static void shellSort(int[] arr) {
        int tmp = 0;
        for (int gap = arr.length / 2; gap > 0; gap /= 2) {
            for (int i = 0; i < arr.length; i++) {
                for (int j = i - gap; j >= 0; j -= gap) {
                    if (arr[j] > arr[j + gap]) {
                        tmp = arr[j];
                        arr[j] = arr[j + gap];
                        arr[j + gap] = tmp;
                    }
                }
            }
        }
    }

    public static void shellSort2(int[] arr) {
        int tmp = 0;
        for (int gap = arr.length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < arr.length; i++) {
                int j = i;
                tmp = arr[j];
                if (arr[j] < arr[j - gap]) {
                    while (j - gap >= 0 && tmp < arr[j - gap]) {
                        arr[j] = arr[j - gap];
                        j -= gap;
                    }
                    arr[j] = tmp;
                }
            }
        }
    }

}
