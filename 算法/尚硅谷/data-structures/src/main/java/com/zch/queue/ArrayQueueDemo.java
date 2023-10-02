package com.zch.queue;

import com.sun.javafx.runtime.async.AbstractRemoteResource;

import java.util.Scanner;

/**
 * @author Zch
 * @date 2023/10/2
 **/
public class ArrayQueueDemo {

    public static void main(String[] args) {
        ArrayQueue arrayQueue = new ArrayQueue(3);
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

class ArrayQueue {
    int maxSize;
    int front;
    int rear;
    int[] arr;

    public ArrayQueue(int maxSize) {
        this.maxSize = maxSize;
        arr = new int[this.maxSize];
        front = -1;
        rear = -1;
    }

    public boolean isFull() {
        return this.rear == this.maxSize - 1;
    }

    public boolean isEmpty() {
        return this.rear == this.front;
    }

    public void add(int val) {
        if (isFull()) {
            throw new RuntimeException("队列已满，不能添加元素啦！");
        }
        this.rear++;
        arr[this.rear] = val;
    }

    public int get() {
        if (isEmpty()) {
            throw new RuntimeException("队列为空，没有任何元素！");
        }
        this.front++;
        return arr[this.front];
    }

    public int peek() {
        if (isEmpty()) {
            throw new RuntimeException("队列为空，没有任何元素！");
        }
        return arr[this.front + 1];
    }

    public void print() {
        if (isEmpty()) {
            throw new RuntimeException("队列为空，没有任何元素！");
        }
        for (int i = 0; i < this.arr.length; i++) {
            System.out.printf("arr[%d] = %d", i, this.arr[i]);
        }
        System.out.println();
    }
}
