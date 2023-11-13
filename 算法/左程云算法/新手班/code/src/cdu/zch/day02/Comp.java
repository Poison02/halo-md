package cdu.zch.day02;

import static cdu.zch.utils.CodeUtils.swap;

/**
 * @author Poison02
 * 使用对数器验证排序
 */
public class Comp {

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
    }

    public static void swap(int[] arr, int i, int j) {
        int temp = arr[j];
        arr[j] = arr[i];
        arr[i] = temp;
    }

    // 返回一个数组arr，arr长度[0,maxLen-1],arr中的每个值[0,maxValue-1]
    public static int[] lenRandomValueRandom(int maxLen, int maxValue) {
        int len = (int) (Math.random() * maxLen);
        int[] ans = new int[len];
        for (int i = 0; i < len; i++) {
            ans[i] = (int) (Math.random() * maxValue);
        }
        return ans;
    }

    public static int[] copyArray(int[] arr) {
        int[] ans = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ans[i] = arr[i];
        }
        return ans;
    }

    // arr1和arr2一定等长
    public static boolean isSorted(int[] arr) {
        if (arr.length < 2) {
            return true;
        }
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (max > arr[i]) {
                return false;
            }
            max = Math.max(max, arr[i]);
        }
        return true;
    }

    public static void main(String[] args) {
        int maxLen = 5;
        int maxValue = 1000;
        int testTime = 10000;
        for (int i = 0; i < testTime; i++) {
            int[] arr1 = lenRandomValueRandom(maxLen, maxValue);
            int[] tmp = copyArray(arr1);
            insertSort(arr1);
            if (!isSorted(arr1)) {
                for (int j = 0; j < tmp.length; j++) {
                    System.out.print(tmp[j] + " ");
                }
                System.out.println();
                System.out.println("插入排序错了！");
                break;
            }
        }

    }

}
