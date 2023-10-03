package com.zch.linkedlist;

/**
 * 双向链表
 * @author Zch
 * @date 2023/10/3
 **/
public class DoubleLinkedListDemo {

    public static void main(String[] args) {
        // 测试
        System.out.println("双向链表的测试");
        // 先创建节点
        HeroNode2 hero1 = new HeroNode2(1, "宋江", "及时雨");
        HeroNode2 hero2 = new HeroNode2(2, "卢俊义", "玉麒麟");
        HeroNode2 hero3 = new HeroNode2(3, "吴用", "智多星");
        HeroNode2 hero4 = new HeroNode2(4, "林冲", "豹子头");
        // 创建一个双向链表
        DoubleLinkedList doubleLinkedList = new DoubleLinkedList();
        doubleLinkedList.add(hero1);
        doubleLinkedList.add(hero2);
        doubleLinkedList.add(hero3);
        doubleLinkedList.add(hero4);

        doubleLinkedList.list();

        // 修改
        HeroNode2 newHeroNode = new HeroNode2(4, "公孙胜", "入云龙");
        doubleLinkedList.update(newHeroNode);
        System.out.println("修改后的链表情况");
        doubleLinkedList.list();

        // 删除
        doubleLinkedList.delete(3);
        System.out.println("删除后的链表情况~~");
        doubleLinkedList.list();
    }

}

class DoubleLinkedList {
    public HeroNode2 head = new HeroNode2(0, "", "");

    public HeroNode2 getHead() {
        return head;
    }

    // 添加到最后
    public void add(HeroNode2 heroNode2) {
        HeroNode2 tmp = head;
        while (true) {
            if (tmp.next == null) {
                break;
            }
            tmp = tmp.next;
        }
        tmp.next = heroNode2;
        heroNode2.pre = tmp;
    }

    public void delete(int no) {
        if (head.next == null) {
            System.out.println("链表为空！");
            return;
        }
        HeroNode2 tmp = head.next;
        boolean flag = false;
        while (true) {
            if (tmp == null) {
                break;
            }
            if (tmp.no == no) {
                flag = true;
                break;
            }
            tmp = tmp.next;
        }
        if (flag) {
            tmp.pre.next = tmp.next;
            // 这里有一个坑，如果是最后一个节点就不需要执行下面这个，不然会有空指针！！！
            if (tmp.next != null) {
                tmp.next.pre = tmp.pre;
            }
        } else {
            System.out.println("链表为空！");
        }

    }

    public void update(HeroNode2 heroNode2) {
        if (head.next == null) {
            System.out.println("链表为空！");
            return;
        }
        HeroNode2 tmp = head.next;
        boolean flag = false;
        while (true) {
            if (tmp == null) {
                break;
            }
            if (tmp.no == heroNode2.no) {
                flag = true;
                break;
            }
            tmp = tmp.next;
        }
        if (flag) {
            tmp.name = heroNode2.name;
            tmp.nickname = heroNode2.nickname;
        } else {
            System.out.println("链表为空！");
        }

    }

    public void list() {
        if (head.next == null) {
            System.out.println("链表为空！");
            return;
        }
        HeroNode2 tmp = head.next;
        while (true) {
            if (tmp == null) {
                break;
            }
            System.out.println(tmp);
            tmp = tmp.next;
        }
    }
}

class HeroNode2 {
    public int no;
    public String name;
    public String nickname;
    public HeroNode2 pre;
    public HeroNode2 next;

    public HeroNode2(int no, String name, String nickname) {
        this.no = no;
        this.name = name;
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "HeroNode2{" +
                "no=" + no +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
