package com.zch.recursion;

/**
 * 递归解决迷宫问题
 * @author Zch
 * @date 2023/10/5
 **/
public class MiGong {

    public static void main(String[] args) {
        int[][] map = new int[8][7];
        // 迷宫的0表示没有走过，1表示墙，2表示走过，3表示不通
        for (int i = 0; i < 8; i++) {
            map[i][0] = 1;
            map[i][6] = 1;
        }
        for (int i = 0; i < 7; i++) {
            map[0][i] = 1;
            map[7][i] = 1;
        }
        map[3][1] = 1;
        map[3][2] = 1;
        // 输出地图
        System.out.println("地图的情况");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 7; j++) {
                System.out.print(map[i][j] + " ");
            }
            System.out.println();
        }

        //使用递归回溯给小球找路
        setWay(map, 1, 1);

        //输出新的地图, 小球走过，并标识过的递归
        System.out.println("小球走过，并标识过的 地图的情况");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 7; j++) {
                System.out.print(map[i][j] + " ");
            }
            System.out.println();
        }
    }

    // 找出路的方法
    /**
     * @param map 地图
     * @param i 从哪个点出发
     * @param j 从哪个点出发
     * @return true / false
     */
    public static boolean setWay(int[][] map, int i, int j) {
        if (map[6][5] == 2) {
            // 若出口为2，则能找到路
            return true;
        } else {
            if (map[i][j] == 0) {
                // 当前位置是0，表示能走
                map[i][j] = 2;
                // 先设当前位置为2，然后开始按照 下 -> 右 -> 上 -> 左 的方向开始寻找路线
                if (setWay(map, i + 1, j)) {
                    return true;
                } else if (setWay(map, i, j + 1)) {
                    return true;
                } else if (setWay(map, i - 1, j)) {
                    return true;
                } else if (setWay(map, i, j - 1)) {
                    return true;
                } else {
                    // 上面的方向都不能走了，则将当前位置置为3
                    map[i][j] = 3;
                    return false;
                }
            } else {
                // 此时当前位置只能是 1 2 3，根据上面的定义，这里只能是走不通这一种选择
                return false;
            }
        }
    }

}
