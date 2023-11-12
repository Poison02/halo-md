package cdu.zch.day01;

import static cdu.zch.utils.CodeUtils.print;
import static cdu.zch.utils.CodeUtils.swap;

/**
 * @author Poison02
 * 选择排序
 *   - 思路：最开始将第一位作为最小值，然后循环数组，每次循环数组都将最小值与后续比较，
 *          若后续有值小于最小值，则将后面的值与最小值进行交换，这样交换后的值就是最小值。
 *          依次这样循环，每次都将最小值往前面放，即每次都选择最小值放前面。
 */
public class SelectSort {

    /**
     * 传过来一个无序数组，返回排序后的数组
     * @param arr 原数组
     */
    public static void selectSort(int[] arr) {
        // 如果数组为空或者长度为0，1，直接返回
        if (arr == null || arr.length < 2) {
            return;
        }
        int N = arr.length;
        for (int i = 0; i < N; i++) {
            // 记录最小值的索引
            int minIndex = i;
            // 从 i 的后一位开始比较
            for (int j = i + 1; j < N; j++) {
                // 找出最小值的索引
                // 如果后续有更小的值，则改变 minIndex，否则不变
                minIndex = arr[j] < arr[minIndex] ? j : minIndex;
            }
            // 将 i 位置上的值与 minIndex 上的值进行交换
            swap(arr, i, minIndex);
        }
    }

    public static void main(String[] args) {
        int[] arr = {4, 3, 7, 2, 6 ,8, 5, 9, 1};
        print(arr);
        selectSort(arr);
        print(arr);
    }

}
