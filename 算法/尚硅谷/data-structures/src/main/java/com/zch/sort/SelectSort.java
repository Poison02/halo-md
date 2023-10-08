package com.zch.sort;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * 选择排序
 * @author Zch
 * @date 2023/10/8
 **/
public class SelectSort {

    public static void main(String[] args) {
        int[] arr = {101, 34, 119, 1, -1, 90, 123};

        //创建要给80000个的随机的数组
        /*int[] arr = new int[80000];
        for (int i = 0; i < 80000; i++) {
            arr[i] = (int) (Math.random() * 8000000); // 生成一个[0, 8000000) 数
        }*/

        System.out.println("排序前");
        System.out.println(Arrays.toString(arr));

        /*Date data1 = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date1Str = simpleDateFormat.format(data1);
        System.out.println("排序前的时间是=" + date1Str);*/

        selectSort(arr);


        /*Date data2 = new Date();
        String date2Str = simpleDateFormat.format(data2);
        System.out.println("排序前的时间是=" + date2Str);*/

        System.out.println("排序后");
        System.out.println(Arrays.toString(arr));
    }

    public static void selectSort(int[] arr) {
        int n = arr.length;
        int min = 0;
        int minIndex = 0;
        for (int i = 0; i < n - 1; i++) {
            min = arr[i];
            minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < min) {
                    min = arr[j];
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                arr[minIndex] = arr[i];
                arr[i] = min;
            }
        }
    }

}
