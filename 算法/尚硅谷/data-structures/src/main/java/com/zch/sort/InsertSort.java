package com.zch.sort;

import java.util.Arrays;

/**
 * 插入排序
 * @author Zch
 * @date 2023/10/9
 **/
public class InsertSort {

    public static void main(String[] args) {
        int[] arr = {101, 34, 119, 1, -1, 89};
        System.out.println("排序前：");
        System.out.println(Arrays.toString(arr));

        insertSort(arr);
        System.out.println("排序后：");
        System.out.println(Arrays.toString(arr));
    }

    public static void insertSort(int[] arr) {
        // 第一步的时候将第0个元素看成是有序的
        for (int i = 1; i < arr.length; i++) {
            int insertVal = arr[i];
            int insertIndex = i - 1;
            // 进行左边有序数组的排序
            while (insertIndex >= 0 && insertVal < arr[insertIndex]) {
                arr[insertIndex + 1] = arr[insertIndex];
                insertIndex--;
            }
            // 当不需要插入的时候
            arr[insertIndex + 1] = insertVal;
        }
    }

}
