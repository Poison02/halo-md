package class04;

public class Code_MergeSort {

    // 递归方法实现
    public static void mergeSort1(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        process(arr, 0, arr.length - 1);
    }

    public static void process(int[] arr, int L, int R) {
        if (L == R) {
            return;
        }
        int mid = L + ((R - L) >> 1);
        process(arr, L, mid);
        process(arr, mid + 1, R);
        merge(arr, L, mid, R);
    }

    public static void merge(int[] arr, int L, int mid, int R) {
        // 新建辅助数组 大小为 L - R 的大小
        int[] help = new int[R - L + 1];
        int i = 0;
        // 新建两个指针，分别操作左右两边
        int p1 = L;
        int p2 = mid + 1;
        while (p1 <= mid && p2 <= R) {
            // 若两个指针没有越界，则将数从小到大放入辅助数组中，两个数相等则从左开始放
            help[i++] = arr[p1] <= arr[p2] ? arr[p1++] : arr[p2++];
        }
        // 考虑指针越界问题，左边越界将右边的数依次放入数组中，反之
        while (p1 <= mid) {
            help[i++] = arr[p1++];
        }
        while (p2 <= R) {
            help[i++] = arr[p2++];
        }
        // 将辅助数组中的数 copy 到原数组中
        for (i = 0; i < help.length; i++) {
            arr[L + i] = help[i];
        }
    }

    // 非递归方法实现
    public static void mergeSort2(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        int n = arr.length;
        // 步长
        int mergeSize = 1;
        while (mergeSize < n) {
            int l = 0;
            while (l < n) {
                if (mergeSize >= n - 1) {
                    break;
                }
                int mid = mergeSize - 1;
                int r = m + Math.min(mergeSize, n - m - 1);
                merge(arr, l, mid, r);
                l = r + 1;
            }
            // 防止整数溢出
            if (mergeSize > n / 2) {
                break;
            }
            // 步长 * 2
            mergeSize <<= 1;
        }
    }

}