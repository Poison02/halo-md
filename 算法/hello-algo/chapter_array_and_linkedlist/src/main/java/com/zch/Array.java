package com.zch;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Zch
 * @date 2023/9/6
 **/
public class Array {

    /**
     * 随机返回一个数组元素
     * @param nums
     * @return
     */
    static int randomAccess(int[] nums) {
        // 在区间 [0, nums.length) 中随机抽取一个数字
        int randomIndex = ThreadLocalRandom.current().nextInt(0, nums.length);
        int randomNum = nums[randomIndex];
        return randomNum;
    }

    /**
     * 扩展数组长度
     * @param nums
     * @param enlarge
     * @return
     */
    static int[] extend(int[] nums, int enlarge) {
        // 初始化一个扩展长度后的数组
        int[] res = new int[nums.length + enlarge];
        // 将原数组中的所有元素复制到新数组
        System.arraycopy(nums, 0, res, 0, nums.length);
        return res;
    }

    /**
     * 在数组的索引index处插入元素num
     * @param nums
     * @param num
     * @param index
     */
    static void insert(int[] nums, int num, int index) {
        // 把索引index以及之后的所有元素向后移动一位
        for (int i = nums.length - 1; i > index; i--) {
            nums[i] = nums[i - 1];
        }
        // 将num赋给index处元素
        nums[index] = num;
    }

    /**
     * 删除索引index处元素
     * @param nums
     * @param index
     */
    static void remove(int[] nums, int index) {
        // 把索引index之后的所有元素向前移动一位
        for (int i = index; i < nums.length - 1; i++) {
            nums[i] = nums[i + 1];
        }
    }

    /**
     * 遍历数组
     * @param nums
     */
    static void traverse(int[] nums) {
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            count++;
        }
        for (int num : nums) {
            count++;
        }
    }

    /**
     * 在数组中查找指定元素
     * @param nums
     * @param target
     * @return
     */
    static int find(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        int[] arr = new int[5];
        System.out.println("数组 arr = " + Arrays.toString(arr));
        int[] nums = { 1, 3, 2, 5, 4 };
        System.out.println("数组 nums = " + Arrays.toString(nums));

        /* 随机访问 */
        int randomNum = randomAccess(nums);
        System.out.println("在 nums 中获取随机元素 " + randomNum);

        /* 长度扩展 */
        nums = extend(nums, 3);
        System.out.println("将数组长度扩展至 8 ，得到 nums = " + Arrays.toString(nums));

        /* 插入元素 */
        insert(nums, 6, 3);
        System.out.println("在索引 3 处插入数字 6 ，得到 nums = " + Arrays.toString(nums));

        /* 删除元素 */
        remove(nums, 2);
        System.out.println("删除索引 2 处的元素，得到 nums = " + Arrays.toString(nums));

        /* 遍历数组 */
        traverse(nums);

        /* 查找元素 */
        int index = find(nums, 3);
        System.out.println("在 nums 中查找元素 3 ，得到索引 = " + index);
    }

}
