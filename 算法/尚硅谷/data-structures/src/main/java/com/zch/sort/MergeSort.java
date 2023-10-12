package com.zch.sort;

import java.util.Arrays;

/**
 * 归并排序
 * @author Zch
 * @date 2023/10/12
 **/
public class MergeSort {

    public static void main(String[] args) {
        int[] arr = { 8, 4, 5, 7, 1, 3, 6, 2 };
        int[] tmp = new int[arr.length];
        System.out.println("排序前：");
        System.out.println(Arrays.toString(arr));

        mergeSort(arr, 0, arr.length - 1, tmp);

        System.out.println("排序后：");
        System.out.println(Arrays.toString(arr));
    }

    public static void mergeSort(int[] arr, int left, int right, int[] tmp) {
        if (left < right) {
            int mid = (left + right) / 2;
            // 递归左边的
            mergeSort(arr, left, mid, tmp);
            // 递归右边的
            mergeSort(arr, mid + 1, right, tmp);
            // 进行合并
            merge(arr, left, mid, right, tmp);
        }
    }

    public static void merge(int[] arr, int left, int mid, int right, int[] tmp) {
        int i = left;
        int j = mid + 1;
        int t = 0;

        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                tmp[t] = arr[i];
                i++;
                t++;
            } else {
                tmp[t] = arr[j];
                t++;
                j++;
            }
        }

        // 将剩下的也放入tmp中
        while (i <= mid) {
            tmp[t] = arr[i];
            t++;
            i++;
        }
        while (j <= right) {
            tmp[t] = arr[j];
            t++;
            j++;
        }

        // 将tmp拷贝到arr中
        t = 0;
        int tmpLeft = left;
        while (tmpLeft <= right) {
            arr[tmpLeft] = tmp[t];
            tmpLeft++;
            t++;
        }
    }

}
