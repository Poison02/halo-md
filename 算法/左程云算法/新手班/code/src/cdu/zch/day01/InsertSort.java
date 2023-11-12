package cdu.zch.day01;

import static cdu.zch.utils.CodeUtils.print;
import static cdu.zch.utils.CodeUtils.swap;

/**
 * @author Poison02
 * 插入排序：
 *   - 思路：将前面某个范围（0 ~ 0，0 ~ 1，0 ~ 2，...，0 ~ n - 1）看成是有序的，
 *          然后后面的每一个数依次进去这个范围内一一进行比较，最后就是有序数组
 */
public class InsertSort {

    /**
     * 插入排序
     * @param arr
     */
    public static void insertSort(int[] arr) {
        // 如果数组为空或者长度为0，1，直接返回
        if (arr == null || arr.length < 2) {
            return;
        }
        int N = arr.length;
        /**
         * 0 - 0
         * 0 - 1
         * 0 - 2
         * ...
         * 0 - N - 1
         * end表示有序范围的右边界，而 0 是特殊的，它就是排好序的
         */
        for (int end = 1; end < N; end ++) {
            int curIndex = end;
            // 如果当前索引已经是最前面且当前索引的前面一位的值大于当前索引的值，
            // 则进行交换
            while (curIndex - 1 >= 0 && arr[curIndex - 1] > arr[curIndex]) {
                swap(arr, curIndex - 1, curIndex);
                // 移动 curIndex（重要），这一步相当于是在有序数组里面进行排序
                // 也就是找到 curIndex指的值的正确位置
                curIndex--;
            }
        }

        // 上面的核心代码可以写成下面这样，道理是一样的
        /*for (int end = 1; end < N; end ++) {
            for (int pre = end - 1; pre >= 0 && arr[pre] > arr[pre + 1]; pre--) {
                swap(arr, pre, pre + 1);
            }
        }*/
    }

    public static void main(String[] args) {
        int[] arr = {4, 3, 7, 2, 6 ,8, 5, 9, 1};
        print(arr);
        insertSort(arr);
        print(arr);
    }
}
