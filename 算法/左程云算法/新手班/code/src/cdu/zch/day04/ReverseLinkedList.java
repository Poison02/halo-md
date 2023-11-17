package cdu.zch.day04;

/**
 * @author Poison02
 * 单链表和双链表的反转
 */
public class ReverseLinkedList {

    public static class SingleNode {
        int value;
        SingleNode next;

        public SingleNode(int value, SingleNode next) {
            this.value = value;
            this.next = next;
        }
    }

    public static class DoubleNode {
        int value;
        DoubleNode last;
        DoubleNode next;

        public DoubleNode(int value, DoubleNode last, DoubleNode next) {
            this.value = value;
            this.last = last;
            this.next = next;
        }
    }

    public static SingleNode reverse(SingleNode head) {
        if (head == null) {
            return null;
        }
        SingleNode pre = null;
        SingleNode next = null;
        while (head != null) {
            next = head.next;
            head.next = pre;
            pre = head;
            head = next;
        }
        return pre;
    }

    public static DoubleNode reverse(DoubleNode head) {
        if (head == null) {
            return null;
        }
        DoubleNode pre = null;
        DoubleNode next = null;
        while (head != null) {
            next = head.next;
            head.next = pre;
            head.last = next;
            pre = head;
            head = next;
        }
        return pre;
    }

}
