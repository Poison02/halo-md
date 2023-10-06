package com.zch.recursion;

/**
 * 递归解决八皇后问题
 * @author Zch
 * @date 2023/10/6
 **/
public class Queue8 {

    int max = 8;
    // 这个数组用来存放皇后能放的位置
    // 这个一维数组，下标表示行，下标对应的值表示
    int[] array = new int[max];
    static int count = 0;
    static int judgeCount = 0;
    public static void main(String[] args) {
        //测试一把 ， 8皇后是否正确
        Queue8 queue8 = new Queue8();
        queue8.check(0);
        System.out.printf("一共有%d解法", count);
        System.out.println();
        System.out.printf("一共判断冲突的次数%d次", judgeCount);
    }

    // 这个函数用来放置皇后
    public void check(int n) {
        if (n == max) {
            // 表示已经放到了最后一个皇后
            print();
            return;
        }
        for (int i = 0; i < max; i++) {
            // 把当前皇后放到这个位置
            array[n] = i;
            if (judge(n)) {
                // 表示当前位置不冲突，继续下一个皇后
                check(n + 1);
            }
            // 如果冲突则直接换下一个
        }
    }

    // 这个函数用来判断当前位置能不能放这个皇后，n就表示皇后
    public boolean judge(int n) {
        judgeCount++;
        for (int i = 0; i < n; i++) {
            //1. array[i] == array[n]  表示判断 第n个皇后是否和前面的n-1个皇后在同一列
            //2. Math.abs(n-i) == Math.abs(array[n] - array[i]) 表示判断第n个皇后是否和第i皇后是否在同一斜线
            // n = 1  放置第 2列 1 n = 1 array[1] = 1
            // Math.abs(1-0) == 1  Math.abs(array[n] - array[i]) = Math.abs(1-0) = 1
            //3. 判断是否在同一行, 没有必要，n 每次都在递增
            if (array[i] == array[n] || Math.abs(n - i) == Math.abs(array[n] - array[i])) {
                return false;
            }
        }
        return true;
    }

    public void print() {
        count++;
        for (int i = 0; i < max; i++) {
            System.out.printf(array[i] + " ");
        }
        System.out.println();
    }

}
