package com.zch.linkedlist;

import java.util.Stack;

/**
 * @author Zch
 * @date 2023/10/3
 **/
public class SingleLinkedListDemo {

    public static void main(String[] args) {
        HeroNode hero1 = new HeroNode(1, "宋江", "及时雨");
        HeroNode hero2 = new HeroNode(2, "卢俊义", "玉麒麟");
        HeroNode hero3 = new HeroNode(3, "吴用", "智多星");
        HeroNode hero4 = new HeroNode(4, "林冲", "豹子头");

        //创建要给链表
        SingleLinkedList singleLinkedList = new SingleLinkedList();

        //加入，无顺序
        /*singleLinkedList.add(hero1);
        singleLinkedList.add(hero4);
        singleLinkedList.add(hero2);
        singleLinkedList.add(hero3);*/

        // 加入，按照顺序
        singleLinkedList.addOrder(hero1);
        singleLinkedList.addOrder(hero4);
        singleLinkedList.addOrder(hero2);
        singleLinkedList.addOrder(hero3);

        // singleLinkedList.list();

        // 返回链表第k个节点
        /*HeroNode lastIndexNode = findLastIndexNode(singleLinkedList.getHead(), 2);
        System.out.println(lastIndexNode);*/

        // 测试修改节点信息
        /*HeroNode newHeroNode = new HeroNode(1, "小宋", "及时雨");
        singleLinkedList.update(newHeroNode);*/

        // 反转链表
        /*reverseList(singleLinkedList.getHead());
        singleLinkedList.list();*/

        // singleLinkedList.list();
        // 从尾到头打印单链表
        reversePrint(singleLinkedList.getHead());
    }

    // 获取单链表节点个数
    public static int getLength(HeroNode head) {
        if (head.next == null) {
            return 0;
        }
        int length = 0;
        HeroNode tmp = head.next;
        while (tmp != null) {
            length++;
            tmp = tmp.next;
        }
        return length;
    }

    // 查找链表的倒数第k个节点
    public static HeroNode findLastIndexNode(HeroNode head, int k) {
        if (head.next == null) {
            return null;
        }
        HeroNode fast = head.next;
        while (k-- > 0) {
            fast = fast.next;
        }
        HeroNode slow = head.next;
        while (fast != null) {
            fast = fast.next;
            slow = slow.next;
        }
        return slow;
    }

    // 单链表反转
    public static void reverseList(HeroNode head) {
        if (head.next == null) {
            return;
        }
        HeroNode cur = head.next;
        HeroNode pre = null;
        while (cur != null) {
            HeroNode tmp = cur.next;
            cur.next = pre;
            pre = cur;
            cur = tmp;
        }
        head.next = pre;
    }

    // 从尾到头打印单链表，这里使用栈
    public static void reversePrint(HeroNode head) {
        if (head.next == null) {
            return;
        }
        Stack<HeroNode> stack = new Stack<>();
        HeroNode tmp = head.next;
        while (tmp != null) {
            stack.push(tmp);
            tmp = tmp.next;
        }
        // 遍历栈
        while (! stack.isEmpty()) {
            HeroNode node = stack.pop();
            System.out.println(node);
        }
    }

}

class SingleLinkedList {
    // 初始化一个虚拟头节点
    private HeroNode head = new HeroNode(0, "", "");

    public HeroNode getHead() {
        return head;
    }

    // 直接添加节点，不考虑顺序
    public void add(HeroNode heroNode) {
        // 使用一个临时节点遍历链表
        HeroNode tmp = head;
        while (true) {
            if (tmp.next == null) {
                break;
            }
            tmp = tmp.next;
        }
        // 将新节点链接到链表尾部
        tmp.next = heroNode;
    }

    public void addOrder(HeroNode heroNode) {
        HeroNode tmp = head;
        // 需要找的节点是要插入位置的前一个节点
        boolean flag = false;
        while (true) {
            // 已经到链表尾部了
            if (tmp.next == null) {
                break;
            }
            if (tmp.next.no > heroNode.no) {
                // 找到了该位置
                break;
            } else if (tmp.next.no == heroNode.no) {
                // 链表中有该节点了
                flag = true;
                break;
            }
            tmp = tmp.next;
        }
        if (flag) {
            System.out.println("链表中已有该节点，不能继续添加！");
        } else {
            heroNode.next = tmp.next;
            tmp.next = heroNode;
        }
    }

    // 根据 no 修改链表元素
    public void update(HeroNode newHeroNode) {
        if (head.next == null) {
            System.out.println("链表为空！");
            return;
        }
        HeroNode tmp = head.next;
        boolean flag = false;
        while (true) {
            if (tmp == null) {
                // 链表已经遍历结束了
                break;
            }
            if (tmp.no == newHeroNode.no) {
                flag = true;
                break;
            }
            tmp = tmp.next;
        }
        if (flag) {
            tmp.name = newHeroNode.name;
            tmp.nickname = newHeroNode.nickname;
        } else {
            System.out.println("未找到该节点！");
        }
    }

    // 根据 no 删除节点
    public void delete(HeroNode heroNode) {
        if (head.next == null) {
            System.out.println("链表为空！");
            return;
        }
        HeroNode tmp = head;
        boolean flag = false;
        while (true) {
            if (tmp.next == null) {
                break;
            }
            if (tmp.next.no == heroNode.no) {
                flag = true;
                break;
            }
            tmp = tmp.next;
        }
        if (flag) {
            tmp.next = tmp.next.next;
        } else {
            System.out.println("未找到该节点！");
        }
    }

    // 显示链表元素
    public void list() {
        if (head.next == null) {
            System.out.println("链表为空！");
            return;
        }
        HeroNode tmp = head.next;
        while (true) {
            if (tmp == null) {
                break;
            }
            System.out.println(tmp);
            tmp = tmp.next;
        }
    }
}

class HeroNode {
    public int no;
    public String name;
    public String nickname;
    public HeroNode next;

    public HeroNode(int no, String name, String nickname) {
        this.no = no;
        this.name = name;
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "HeroNode{" +
                "no=" + no +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
