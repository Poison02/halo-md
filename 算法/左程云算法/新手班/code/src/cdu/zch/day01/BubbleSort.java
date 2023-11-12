package cdu.zch.day01;

import static cdu.zch.utils.CodeUtils.print;
import static cdu.zch.utils.CodeUtils.swap;

/**
 * @author Poison02
 * 冒泡排序
 *   -思路：每两个数两两进行比较，将比较大的数往后移，即每次都将最大的数放在了最后
 */
public class BubbleSort {

    public static void bubbleSort(int[] arr) {
        // 如果数组为空或者长度为0，1，直接返回
        if (arr == null || arr.length < 2) {
            return;
        }
        int N = arr.length;
        // 每次都在 0 ~ N - 1 这个范围里面做比较
        for (int end = N - 1; end >= 0; end --) {
            // 进行比较的操作
            for (int second = 1; second <= end; second ++) {
                // 两两进行比较，前面比后面大就交换
                if (arr[second - 1] > arr[second]) {
                    swap(arr, second - 1, second);
                }
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = {4, 3, 7, 2, 6 ,8, 5, 9, 1};
        print(arr);
        bubbleSort(arr);
        print(arr);
    }

}
