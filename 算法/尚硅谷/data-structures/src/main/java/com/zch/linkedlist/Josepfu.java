package com.zch.linkedlist;

/**
 * 经典约瑟夫环问题
 * @author Zch
 * @date 2023/10/3
 **/
public class Josepfu {

    public static void main(String[] args) {
        CircleLinkedList circleLinkedList = new CircleLinkedList();
        circleLinkedList.add(5);
        circleLinkedList.list();

        circleLinkedList.countBy(1, 5, 2); // 2 -> 4 -> 1 -> 5 -> 3
    }

}

class CircleLinkedList {
    public Boy first = null;

    // 添加节点
    public void add(int nums) {
        if (nums < 1) {
            System.out.println("nums需要 >=1 ");
            return;
        }
        Boy cur = null;
        for (int i = 1; i <= nums; i++) {
            Boy boy = new Boy(i);
            if (i == 1) {
                first = boy;
                cur = first;
                cur.next = first;
            } else {
                cur.next = boy;
                boy.next = first;
                cur = boy;
            }
        }
    }

    public void list() {
        if (first == null) {
            System.out.println("链表为空！");
            return;
        }
        Boy tmp = first;
        while (true) {
            System.out.println(tmp.no);
            if (tmp.next == first) {
                break;
            }
            tmp = tmp.next;
        }
    }

    /**
     * 约瑟夫环问题
     * @param start 从start开始
     * @param nums nums个节点
     * @param k 数 k 下
     */
    public void countBy(int start, int nums, int k) {
        if (first == null || start < 1 || start > nums) {
            System.out.println("输入的参数有误！");
            return;
        }
        Boy helper = first;
        while (true) {
            // 说明到了最后一个节点（这里的最后一个节点指下一个就是first）
            if (helper.next == first) {
                break;
            }
            helper = helper.next;
        }
        // 在准备数数前，first和helper需要移动到开始位置以及开始位置的前一个
        for (int i = 0; i < start - 1; i++) {
            first = first.next;
            helper = helper.next;
        }
        while (true) {
            //这里就是只有一个节点了
            if (helper == first) {
                break;
            }
            // 这里开始数数，first和helper需要移动 k - 1 位
            for (int i = 0; i < k - 1; i++) {
                first = first.next;
                helper = helper.next;
            }
            // 这个时候first指向的就是要删除的节点
            System.out.printf("删除的节点是：%d", first.no);
            System.out.println();
            // 准备删除，操作是：first向后移一位，helper接上first
            first = first.next;
            helper.next = first;
        }
        // 到这里就是只剩一个节点了
        System.out.printf("留到最后的是: %d", first.no);
    }
}

class Boy {
    public int no;
    public Boy next;

    public Boy(int no) {
        this.no = no;
    }
}
