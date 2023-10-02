package com.zch.queue;

import java.util.Scanner;

/**
 * @author Zch
 * @date 2023/10/2
 **/
public class CircleArrayQueueDemo {

    public static void main(String[] args) {
        CircleArrayQueue arrayQueue = new CircleArrayQueue(3);
        Scanner input = new Scanner(System.in);
        char key = ' ';
        boolean loop = true;
        while (loop) {
            System.out.println("s(show): 显示队列");
            System.out.println("e(exit): 退出程序");
            System.out.println("a(add): 添加数据到队列");
            System.out.println("g(get): 从队列取出数据");
            System.out.println("h(head): 查看队列头的数据");
            key = input.next().charAt(0);
            switch (key) {
                case 's':
                    try {
                        arrayQueue.print();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 'e':
                    input.close();
                    loop = false;
                    break;
                case 'a':
                    System.out.println("请输入一个数：");
                    int val = input.nextInt();
                    try {
                        arrayQueue.add(val);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 'g':
                    try {
                        int e = arrayQueue.get();
                        System.out.printf("取出的数据是：%d\n", e);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 'h':
                    try {
                        int peek = arrayQueue.peek();
                        System.out.printf("头部的数据是: %d\n", peek);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                default:
                    break;
            }
        }
        System.out.println("程序退出！");
    }

}

class CircleArrayQueue {
    int maxSize;
    int front;
    int rear;
    int[] arr;

    public CircleArrayQueue(int maxSize) {
        this.maxSize = maxSize;
        this.front = 0;
        this.rear = 0;
        arr = new int[this.maxSize];
    }

    public boolean isFull() {
        return (this.rear + 1) % this.maxSize == this.front;
    }

    public boolean isEmpty() {
        return this.rear == this.front;
    }

    public void add(int val) {
        if (isFull()) {
            throw new RuntimeException("队列已满，不能添加元素啦！");
        }
        this.arr[this.rear] = val;
        this.rear = (this.rear + 1) % this.maxSize;
    }

    public int get() {
        if (isEmpty()) {
            throw new RuntimeException("队列为空，不能得到元素！");
        }
        int val = this.arr[this.front];
        this.front = (this.front + 1) % this.maxSize;
        return val;
    }

    public int peek() {
        if (isEmpty()) {
            throw new RuntimeException("队列为空，不能得到元素！");
        }
        return this.arr[this.front];
    }

    public void print() {
        if (isEmpty()) {
            throw new RuntimeException("队列为空，不能得到元素！");
        }
        for (int i = front; i < this.front + size(); i++) {
            System.out.printf("arr[%d] = %d", i % this.maxSize, this.arr[i % this.maxSize]);
        }
        System.out.println();
    }

    // 有效个数
    public int size() {
        return (this.rear + this.maxSize - this.front) % this.maxSize;
    }
}
