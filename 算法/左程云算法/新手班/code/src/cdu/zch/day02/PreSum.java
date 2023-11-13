package cdu.zch.day02;

/**
 * @author Poison02
 * 前缀和数组，可以采用二维数组，也可以采用一维数组
 */
public class PreSum {

    /**
     * 接受原数组，返回前缀和数组
     *
     * @param arr 原数组
     * @return 新数组
     */
    public static int[] rangeSum(int[] arr) {
        if (arr == null) {
            return null;
        }
        int N = arr.length;
        int[] newArr = new int[N];
        newArr[0] = arr[0];
        for (int i = 1; i < N; i++) {
            newArr[i] = newArr[i - 1] + arr[i];
        }
        return newArr;
    }

    public static int findLR(int[] arr, int L, int R) {
        if (arr == null) {
            return -1;
        }
        int[] pre = rangeSum(arr);
        return L == 0 ? pre[R] : (pre[R] - pre[L - 1]);
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8};
        int result = findLR(arr, 1, 3);
        System.out.println("result = " + result);
    }

}
