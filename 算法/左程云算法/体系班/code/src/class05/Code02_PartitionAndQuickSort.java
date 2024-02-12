 package class05;

public class Code02_PartitionAndQuickSort {

    public static void swap(int[] arr, int i, int j) {
        arr[i] = arr[i] ^ arr[j];
        arr[j] = arr[i] ^ arr[j];
        arr[i] = arr[i] ^ arr[j];
    }

    /**
     * arr[l...r] 以arr[r]位置的数做划分 分割两边的数
     * <=x 在左
     * >x  在右
     * @param arr
     * @param l
     * @param r
     * @return
     */
    public static int partition(int[] arr, int l, int r) {
        if (l > r) {
            return -1;
        }
        if (l == r) {
            return l;
        }
        int lessEqual = l - 1;
        int index = l;
        while (index < r) {
            if (arr[index] <= arr[r]) {
                swap(arr, index, ++lessEqual);
            }
            index++;
        }
        swap(arr, ++lessEqual, r);
        return lessEqual;
    }

    /**
     * 荷兰国旗  以arr[r] 为分割
     * @param arr
     * @param l
     * @param r
     * @return
     */
    public static int[] netherlandsFlag(int[] arr, int l, int r) {
        if (l > r) {
            return new int[] {-1, -1};
        }
        if (l == r) {
            return new int[] {l, r};
        }
        int less = l - 1;
        int more = r;
        int index = l;
        while (index < more) {
            if (arr[index] == arr[r]) {
                index++;
            } else if (arr[index] < arr[r]) {
                swap(arr, index++, ++less);
            } else {
                swap(arr, index, --more);
            }
        }
        swap(arr, more, r);
        return new int[] {less + 1, more};
    }

    public static void quickSort1(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        process1(arr, 0, arr.length - 1);
    }

    public static void process1(int[] arr, int l, int r) {
        if (l >= r) {
            return;
        }
        int m = partition(arr, l, r);
        process1(arr, l, m - 1);
        process1(arr, m + 1, r);
    }

}