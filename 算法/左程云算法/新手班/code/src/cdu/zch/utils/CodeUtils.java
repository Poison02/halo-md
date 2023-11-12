package cdu.zch.utils;

public class CodeUtils {
    /**
     * 将数组中 i 与 j 上的值进行交换
     *
     * @param arr 数组
     * @param i   i
     * @param j   j
     */
    public static void swap(int[] arr, int i, int j) {
        int temp = arr[j];
        arr[j] = arr[i];
        arr[i] = temp;
    }

    /**
     * 打印 arr 数组
     *
     * @param arr 数组
     */
    public static void print(int[] arr) {
        for (int j : arr) {
            System.out.print(j + " ");
        }
        System.out.println();
    }
}
