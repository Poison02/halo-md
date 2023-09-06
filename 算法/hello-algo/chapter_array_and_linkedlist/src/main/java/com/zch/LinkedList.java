package com.zch;

import com.zch.util.ListNode;
import com.zch.util.PrintUtil;

/**
 * @author Zch
 * @date 2023/9/6
 **/
public class LinkedList {

    static void insert(ListNode n0, ListNode P) {
        ListNode n1 = n0.next;
        P.next = n1;
        n0.next = P;
    }

    static void remove(ListNode n0) {
        if (n0.next == null) {
            return;
        }
        ListNode p = n0.next;
        ListNode n1 = p.next;
        n0.next = n1;
    }

    static ListNode access(ListNode head, int index) {
        for (int i = 0; i < index; i++) {
            if (head == null) {
                return null;
            }
            head = head.next;
        }
        return head;
    }

    static int find(ListNode head, int target) {
        int index = 0;
        while (head != null) {
            if (head.val == target) {
                return index;
            }
            head = head.next;
            index++;
        }
        return -1;
    }

    public static void main(String[] args) {
        /* 初始化链表 */
        // 初始化各个节点
        ListNode n0 = new ListNode(1);
        ListNode n1 = new ListNode(3);
        ListNode n2 = new ListNode(2);
        ListNode n3 = new ListNode(5);
        ListNode n4 = new ListNode(4);
        // 构建引用指向
        n0.next = n1;
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        System.out.println("初始化的链表为");
        PrintUtil.printLinkedList(n0);

        /* 插入节点 */
        insert(n0, new ListNode(0));
        System.out.println("插入节点后的链表为");
        PrintUtil.printLinkedList(n0);

        /* 删除节点 */
        remove(n0);
        System.out.println("删除节点后的链表为");
        PrintUtil.printLinkedList(n0);

        /* 访问节点 */
        ListNode node = access(n0, 3);
        System.out.println("链表中索引 3 处的节点的值 = " + node.val);

        /* 查找节点 */
        int index = find(n0, 2);
        System.out.println("链表中值为 2 的节点的索引 = " + index);
    }



}
