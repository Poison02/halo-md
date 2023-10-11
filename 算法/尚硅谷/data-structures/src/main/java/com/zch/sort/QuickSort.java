package com.zch.sort;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * 快速排序
 * @author Zch
 * @date 2023/10/11
 **/
public class QuickSort {

    public static void main(String[] args) {
        int[] arr = {-9,78,0,23,-567,70, -1,900, 4561};
        System.out.println("排序前：");
        System.out.println(Arrays.toString(arr));

        quickSort(arr, 0, arr.length - 1);

        System.out.println("排序后：");
        System.out.println(Arrays.toString(arr));

        //测试快排的执行速度
        // 创建要给80000个的随机的数组
        /*int[] arr = new int[8000000];
        for (int i = 0; i < 8000000; i++) {
            arr[i] = (int) (Math.random() * 8000000); // 生成一个[0, 8000000) 数
        }

        System.out.println("排序前");
        Date data1 = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date1Str = simpleDateFormat.format(data1);
        System.out.println("排序前的时间是=" + date1Str);

        quickSort(arr, 0, arr.length-1);

        Date data2 = new Date();
        String date2Str = simpleDateFormat.format(data2);
        System.out.println("排序前的时间是=" + date2Str);*/
        //System.out.println("arr=" + Arrays.toString(arr));
    }

    public static void quickSort(int[] arr, int left, int right) {
        int l = left;
        int r = right;
        // 中值
        int pivot = arr[(left + right) / 2];
        int tmp = 0;
        while (l < r) {
            while (arr[l] < pivot) {
                l++;
            }
            while (arr[r] > pivot) {
                r--;
            }
            if (l >= r) {
                break;
            }

            // 到这里说明 左边有比中间大的且右边有比中间小的
            tmp = arr[l];
            arr[l] = arr[r];
            arr[r] = tmp;

            // 如果交换完发现左值等于中值，则右边界左移
            if (arr[l] == pivot) {
                r--;
            }

            // 如果交换完发现右值等于中值，则左边界右移
            if (arr[r] == pivot) {
                l++;
            }
        }

        if (l == r) {
            l += 1;
            r -= 1;
        }

        // 向左递归
        if (left < l) {
            quickSort(arr, left, r);
        }


        // 向右递归
        if (r < right) {
            quickSort(arr, l, right);
        }

    }

}
