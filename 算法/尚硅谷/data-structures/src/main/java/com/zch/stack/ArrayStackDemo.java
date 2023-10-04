package com.zch.stack;

import java.util.Scanner;

/**
 * @author Zch
 * @date 2023/10/4
 **/
public class ArrayStackDemo {

    public static void main(String[] args) {
        //测试一下ArrayStack 是否正确
        //先创建一个ArrayStack对象->表示栈
        ArrayStack stack = new ArrayStack(4);
        String key = "";
        boolean loop = true; //控制是否退出菜单
        Scanner scanner = new Scanner(System.in);

        while(loop) {
            System.out.println("show: 表示显示栈");
            System.out.println("exit: 退出程序");
            System.out.println("push: 表示添加数据到栈(入栈)");
            System.out.println("pop: 表示从栈取出数据(出栈)");
            System.out.println("请输入你的选择");
            key = scanner.next();
            switch (key) {
                case "show":
                    stack.list();
                    break;
                case "push":
                    System.out.println("请输入一个数");
                    int value = scanner.nextInt();
                    stack.push(value);
                    break;
                case "pop":
                    try {
                        int res = stack.pop();
                        System.out.printf("出栈的数据是 %d\n", res);
                    } catch (Exception e) {
                        // TODO: handle exception
                        System.out.println(e.getMessage());
                    }
                    break;
                case "exit":
                    scanner.close();
                    loop = false;
                    break;
                default:
                    break;
            }
        }

        System.out.println("程序退出~~~");
    }

}

class ArrayStack {
    public int[] arr;
    public int size;
    public int top;

    public ArrayStack(int size) {
        this.size = size;
        this.top = 0;
        arr = new int[this.size];
    }

    public boolean isEmpty() {
        return this.top == 0;
    }

    public boolean isFull() {
        return this.top == this.size;
    }

    public void push(int num) {
        if (isFull()) {
            System.out.println("栈已满！");
            return;
        }
        this.arr[this.top] = num;
        this.top++;
    }

    public int pop() {
        if (isEmpty()) {
            System.out.println("栈为空！");
            return -1;
        }
        return this.arr[this.top--];
    }

    public void list() {
        if (isEmpty()) {
            System.out.println("栈为空！");
            return;
        }
        for (int i = this.top - 1; i >= 0; i--) {
            System.out.println(this.arr[i]);
        }
    }
}
