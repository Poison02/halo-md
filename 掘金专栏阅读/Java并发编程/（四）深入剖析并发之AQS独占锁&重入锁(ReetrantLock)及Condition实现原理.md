## 一、JUC中的Lock锁接口

在我们并发编程的文章一开始，我们都是在围绕着线程安全问题叙述它的解决方案，在前面的文章中我们曾提到过CAS无锁机制、synchronized关键字等多种解决方案，在其中CAS机制属于乐观锁类型，synchronized关键字属于悲观锁类型，而我们本章要谈到的基于AQS实现的ReetrantLock也是属于悲观锁类型的实现。但是它与我们之前聊的synchronized并不相同，synchronized关键字属于隐式锁，锁的获取和释放都是隐式的，且不需要开发人员干预。而我们本章要讲的则是显式锁，即锁的获取和释放都需要我们手动编码实现。在JDK1.5时，官方在Java.uitl.concurrent并发包中添加了Lock锁接口，该接口中定义了lock()[获取锁]和unlock()[释放锁]两个方法对显式锁的加锁与解锁操作提供了支持。显式锁的使用方式如下：

```java
java复制代码Lock lock = new ReetrantLock(); //创建锁对象
lock.lock(); //获取锁操作
try{
    //需要锁修饰的代码块....
} finally{
    lock.unlock(); //释放锁操作
}
```

如上代码在程序运行时，当前线程执行lock()方法之后，则代表着当前线程占用了锁资源，在当前线程未执行unlock()方法之前，其他线程由于获取不到锁资源无法进入被锁修饰的代码块执行，所以会一直被阻塞至当前线程释放锁时。不过我们在编码过程中需要注意的是解锁操作unlock()方法必须放入finally代码块中，这样能够确保即使加锁代码执行过程中抛出了异常线程最终也能释放锁资源，避免程序造成死锁现象。当然Lock接口中除开定义了lock()与unlock()方法外，还提供了以下相关方法：

```java
java复制代码    /**
     * 获取锁：
     *     如果当前锁资源空闲可用则获取锁资源返回，
     *     如果不可用则阻塞等待，不断竞争锁资源，直至获取到锁返回。
     */
    void lock();
    
    /**
     * 释放锁：
     *     当前线程执行完成业务后将锁资源的状态由占用改为可用并通知阻塞线程。
     */
    void unlock();

    /**
     * 获取锁：(与lock方法不同的在于可响应中断操作，即在获取锁过程中可中断)
     *     如果当前锁资源可用则获取锁返回。
     *     如果当前锁资源不可用则阻塞直至出现如下两种情况：
     *        1.当前线程获取到锁资源。
     *        2.接收到中断命令，当前线程中断获取锁操作。
     */
    void lockInterruptibly() throws InterruptedException;

    /**
     * 非阻塞式获取锁：
     *    尝试非阻塞式获取锁，调用该方法获取锁立即返回获取结果。
     *    如果获取到了锁则返回true，反之返回flase。
     */
    boolean tryLock();

    /**
     * 非阻塞式获取锁：
     *   根据传入的时间获取锁，如果线程在该时间段内未获取到锁返回flase。
     *   如果当前线程在该时间段内获取到了锁并未被中断则返回true。
     */
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;


    /**
     * 获取等待通知组件（该组件与当前锁资源绑定）：
     *    当前线程只有获取到了锁资源之后才能调用该组件的wait()方法，
     *    当前线程调用await()方法后，当前线程将会释放锁。
     */
    Condition newCondition();
```

通过分析如上Lock接口提供的方法可以得知，Lock锁提供了很多synchronized锁不具备的特性，如下：

- ①获取锁中断操作(synchronized关键字是不支持获取锁中断的)；
- ②非阻塞式获取锁机制；
- ③超时中断获取锁机制；
- ④多条件等待唤醒机制Condition等。

## 二、Lock接口的实现者：ReetrantLock重入锁

ReetrantLock，JDK1.5时JUC包下添加的一个类，实现于Lock接口，作用与synchronized相同，不过对比于synchronized更加灵活，但是使用时需要我们手动获取/释放锁。
 ReetrantLock本身是支持重入的一把锁，即支持当前获取锁的线程对锁资源进行多次重复的锁获取，在此同时还支持公平锁与非公平锁。这里的公平与非公平指的是获取锁操作执行后锁资源获取的先后顺序，如果先执行获取锁操作的线程先获取锁，那么就代表当前的锁是公平的，反之，如果先执行获取锁操作的线程还需要和后面执行获取锁操作的线程竞争锁资源，那么则代表当前锁是非公平的。在这里值得注意的是：非公平锁虽然会出现线程竞争锁资源的情况，但是一般而言非公平锁的效率在绝大部分情况下也远远超出公平锁。不过在某些特殊的业务场景下，比如更加注重锁资源获取的先后顺序，那么公平锁才是最好的选择。在前面我们也曾提到过ReetrantLock支持锁重入即当前线程能够多次执行获取锁操作，但是我们在使用ReetrantLock过程中要明白的是：ReetrantLock执行了几次获取锁操作也需要执行多少次释放锁操作。案例如下：

```java
java复制代码import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Task implements Runnable {
    public static Lock lock = new ReentrantLock();
    public static int count = 0;

    @Override
    public void run() {
        for (int i = 0; i<10000;i++){
            lock.lock(); // 第一次获取锁
            lock.lock(); // 第二次获取锁
            try {
                count++; // 非原子性操作：存在线程安全问题
            } finally {
                lock.unlock(); // 第一次释放锁
                lock.unlock(); // 第二次释放锁
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        Task task = new Task();
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(count);
        // 执行结果：20000
    }
}
```

上面的这个例子很简单，t1,t2两个线程同时对共享资源count进行++的非原子性操作，我们在这里使用ReentrantLock锁解决存在的线程安全问题。同时我们在上述代码中，获取了两次锁资源，因为ReentrantLock支持锁重入，所以此时获取两次锁是没有问题的，不过在finally中执行释放锁资源时需要注意：也应该执行两次unlock释放锁的操作。从上述案例中分析我们可以发现，其实ReentrantLock的用法相对来说比较简单，我们接下来也可以分析一下ReentrantLock所提供的一些方法以便于更加全面的认识它。如下：

```java
java复制代码// 查询当前线程调用lock()的次数
int getHoldCount() 

// 返回目前持有此锁的线程，如果此锁不被任何线程持有,返回null  
protected Thread getOwner(); 

// 返回一个集合，它包含可能正等待获取此锁的线程，其内部维持一个队列(后续分析)
protected Collection<Thread> getQueuedThreads(); 

// 返回正等待获取此锁资源的线程估计数
int getQueueLength();

// 返回一个集合，它包含可能正在等待与此锁相关的Condition条件的线程(估计值)
protected Collection<Thread> getWaitingThreads(Condition condition); 

// 返回调用当前锁资源Condition对象await方法后未执行signal()方法的线程估计数
int getWaitQueueLength(Condition condition);

// 查询指定的线程是否正在等待获取当前锁资源
boolean hasQueuedThread(Thread thread); 

// 查询是否有线程正在等待获取当前锁资源
boolean hasQueuedThreads();

// 查询是否有线程正在等待与此锁相关的Condition条件
boolean hasWaiters(Condition condition); 

// 返回当前锁类型，如果是公平锁返回true，反之则返回flase
boolean isFair() 

// 查询当前线程是持有当前锁资源
boolean isHeldByCurrentThread() 

// 查询当前锁资源是否被线程持有
boolean isLocked()
```

通过观察我们不难得知，ReentrantLock作为Lock接口的实现者，除开在实现了Lock接口定义的方法外，ReentrantLock也拓展了一些其他方法。我们可以通过一个简单的案例来熟悉一下ReentrantLock一些其他方法的作用。案例如下：

```java
java复制代码import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Task implements Runnable {

    public static ReentrantLock lock = new ReentrantLock();
    public static int count = 0;

    // ReentrantLock的简单使用案例
    @SneakyThrows
    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            lock.lock(); // 第一次阻塞式获取锁
            lock.tryLock(); // 第二次非阻塞式获取锁
            lock.tryLock(10,TimeUnit.SECONDS); // 第三次非阻塞等待式获取锁
            try {
                count++; // 非原子性操作：存在线程安全问题
            } finally {
                lock.unlock(); // 第一次释放锁
                lock.unlock(); // 第二次释放锁
                lock.unlock(); // 第三次释放锁
            }
        }
    }

    public void reentrantLockApiTest() {
        lock.lock(); // 获取锁
        try {
            //获取当前线程调用lock()方法的次数
            System.out.println("线程：" + Thread.currentThread().getName() + "\t调用lock()次数：" + lock.getHoldCount());
            // 判断当前锁是否为公平锁
            System.out.println("当前锁资源类型是否为公平锁？" + lock.isFair());
            // 获取等待获取当前锁资源的估计线程数
            System.out.println("目前有：" + lock.getQueueLength() + "个线程正在等待获取锁资源！");
            // 指定线程是否在等待获取当前锁资源
            System.out.println("当前线程是否在等待获取当前锁资源？" + lock.hasQueuedThread(Thread.currentThread()));
            // 判断当前锁资源是否有线程在等待获取
            System.out.println("当前锁资源是否存在线程等待获取？" + lock.hasQueuedThreads());
            // 判断当前线程是否持有当前锁资源
            System.out.println("当前线程是否持有当前锁资源？" + lock.isHeldByCurrentThread());
            // 判断当前锁资源是否被线程持有
            System.out.println("当前锁资源是否被线程占用？" + lock.isLocked());
        } finally {
            lock.unlock(); // 释放锁
        }
    }


    public static void main(String[] args) throws InterruptedException {
        Task task = new Task();
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(count); // 执行结果：20000
        /**
         * 执行结果：
         *   线程：main	调用lock()次数：1
         *   当前锁资源类型是否为公平锁？false
         *   目前有：0个线程正在等待获取锁资源！
         *   当前线程是否在等待获取当前锁资源？false
         *   当前锁资源是否存在线程等待获取？false
         *   当前线程是否持有当前锁资源？true
         *   当前锁资源是否被线程占用？true
         */
        task.reentrantLockApiTest();
    }
}
```

通过上面的简单案例我们可以看到ReentrantLock锁的使用还是比较简单的，所以我们关于ReentrantLock的应用暂时先告一段落，接下来我们一步步的带着大家分析去ReentrantLock内部实现原理，其实ReentrantLock是基于AQS框架实现的，所以在研究ReentrantLock内部实现之前我们先带大家深入了解一下AQS。

## 三、JUC并发包内核：并发基础组件AQS

AQS全称为AbstractQueuedSynchronizer(抽象的队列同步器)，Java并发包中的核心基础组件，它是用来构建信号量、锁、门阀等其他同步组件的基础框架。

### AQS工作原理简述

在之前的《[彻底理解Java并发编程之Synchronized关键字实现原理剖析](https://juejin.cn/post/6977744582725681182)》中谈到过，synchronized重量级锁底层的实现是基于ObjectMonitor对象中的计数器实现的，而在AQS中也存在着异曲同工之处，它内部通过一个用volatile关键字修饰的int类型全局变量state作为标识来控制同步状态。当状态标识state为0时，代表着当前没有线程占用锁资源，反之当状态标识state不为0时，代表着锁资源已经被线程持有，其他想要获取锁资源的线程必须进入同步队列等待当前持有锁的线程释放。AQS通过内部类Node构建FIFO(先进先出)的同步队列用来处理未获取到锁资源的线程，将等待获取锁资源的线程加入到同步队列中进行排队等待。同时AQS使用内部类ConditionObject用来构建等待队列，当Condition调用await()方法后，等待获取锁资源的线程将会加入等待队列中，而当Condition调用signal()方法后，线程将从等待队列转移到同步队列中进行锁资源的竞争。值得我们注意的是在这里存在两种类型的队列：
 ①同步队列：当线程获取锁资源发现已经被其他线程占有而加入的队列；
 ②等待队列(可能存在多个)：当Condition调用await()方法后加入的队列；
 大家在理解时不可将两者混为一谈。我们可以首先分析一下AQS中的同步队列，AQS同步队列模型如下：

```java
java复制代码public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer{
// 指向同步队列的头部
private transient volatile Node head;
// 指向同步队列的尾部
private transient volatile Node tail;
// 同步状态标识
private volatile int state;
// 省略......
}
```

其中head以及tail是AQS的全局变量，其中head指向同步队列的头部，但是需要注意的是head节点为空不存储信息，而tail指向同步队列的尾部。AQS中同步队列采用这种方式构建双向链表结构方便队列进行节点增删操作。state则为我们前面所提到的同步状态标识，当线程在执行过程中调用获取锁的lock()方法后，如果state=0，则说明当前锁资源未被其他线程获取，当前线程将state值设置为1，表示获取锁成功。如果state=1，则说明当前锁资源已被其他线程获取，那么当前线程则会被封装成Node节点加入同步队列进行等待。Node节点是对每一个获取锁资源线程的封装体，其中包括了当前执行的线程本身以及线程的状态，如是否被阻塞、是否处于等待唤醒、是否中断等。每个Node节点中都关联着前驱节点prev以及后继节点next，这样能够方便持有锁的线程释放后能快速释放下一个正在等待的线程。Node类结构如下：

```java
java复制代码static final class Node {
    // 共享模式
    static final Node SHARED = new Node();
    // 独占模式
    static final Node EXCLUSIVE = null;
    // 标识线程已处于结束状态
    static final int CANCELLED =  1;
    // 等待被唤醒状态
    static final int SIGNAL    = -1;
    // Condition条件状态
    static final int CONDITION = -2;
    // 在共享模式中使用表示获得的同步状态会被传播
    static final int PROPAGATE = -3;

    // 等待状态,存在CANCELLED、SIGNAL、CONDITION、PROPAGATE四种
    volatile int waitStatus;

    // 同步队列中前驱结点
    volatile Node prev;

    // 同步队列中后继结点
    volatile Node next;

    // 获取锁资源的线程
    volatile Thread thread;

    // 等待队列中的后继结点（与Condition有关，稍后会分析）
    Node nextWaiter;

    // 判断是否为共享模式
    final boolean isShared() {
        return nextWaiter == SHARED;
    }
    // 获取前驱结点
    final Node predecessor() throws NullPointerException {
        Node p = prev;
        if (p == null)
            throw new NullPointerException();
        else
            return p;
    }
    // 省略代码.....
}
```

在其中SHARED和EXCLUSIVE两个全局常量分别代表着共享模式和独占模式，共享模式即允许多个线程同时对一个锁资源进行操作，例如：信号量Semaphore、读锁ReadLock等采用的就是基于AQS的共享模式实现的。而独占模式则代表着在同一时刻只运行一个线程对锁资源进行操作，如ReentranLock等组件的实现都是基于AQS的独占模式实现。全局变量waitStatus则代表着当前被封装成Node节点的线程的状态，一共存在五种情况：

- 0 初始值状态：waitStatus=0，代表节点初始化。
- CANCELLED 取消状态：waitStatus=1，在同步队列中等待的线程等待超时或者被中断，需要从同步队列中取消该Node的节点，其节点的waitStatus为CANCELLED，进入该状态后的节点代表着进入了结束状态，当前节点将不会再发生变化。
- SIGNAL 信号状态：waitStatus=-1，被标识为该状态的节点，当其前驱节点的线程释放了锁资源或被取消，将会通知该节点的线程执行。简单来说被标记为当前状态的节点处于等待唤醒状态，只要前驱节点释放锁，就会通知标识为SIGNAL状态的后续节点的线程执行。
- CONDITION 条件状态：waitStatus=-2，与Condition相关，被表示为该状态的节点处于等待队列中，节点的线程等待在Condition条件，当其他线程调用了Condition的signal()方法后，CONDITION状态的节点将从等待队列转移到同步队列中，等待获取竞争锁资源。
- PROPAGATE 传播状态：waitStatus=-3，该状态与共享模式有关，在共享模式中，被标识为该状态的节点的线程处于可运行状态。

![Node节点结构](（四）深入剖析并发之AQS独占锁&重入锁(ReetrantLock)及Condition实现原理.assets/5ee9ff27e49a40aa9a46fad490f90f5atplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)
 全局变量pre和next分别代表着当前Node节点对应的前驱节点和后继节点，thread代表当前被封装的线程对象。nextWaiter代表着等待队列中，当前节点的后继节点(与Condition有关稍后分析)。到这里其实我们对于Node数据类型的结构有了大概的了解了。总之，AQS作为JUC的核心组件，对于锁存在两种不同的实现，即独占模式(如ReetrantLock)与共享模式(如Semaphore)。但是不管是独占模式还是共享模式的实现类，都是建立在AQS的基础上实现，其内部都维持着一个队列，当试图获取锁的线程数量超过当前模式限制时则会将线程封装成一个Node节点加入队列进行等待。而这一系列操作都是由AQS帮我们完成，无论是ReetrantLock还是Semaphore，其实它们的绝大部分方法最终都是直接或间接的调用AQS完成的。下面是AQS整体类图结构：
 ![AQS整体类图结构](（四）深入剖析并发之AQS独占锁&重入锁(ReetrantLock)及Condition实现原理.assets/6079a74eb0354af3ac96ab84d549ed27tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

- **AbstractOwnableSynchronizer抽象类：** 内部定义了存储当前持有锁资源线程以及获取存储线程信息方法。
- **AbstractQueuedSynchronizer抽象类：** AQS指的就是AbstractQueuedSynchronizer的首字母缩写，整个AQS框架的核心类。内部以虚拟队列的形式实现了线程对于锁资源获取(tryAcquire)与释放(tryRelease)，但是在AQS中没有对锁获取与锁释放的操作进行默认实现，具体的逻辑需要子类实现，这样使得我们在开发过程中能够更加灵活的运用它。
- **Node内部类：** AbstractQueuedSynchronizer中的内部类，用于构建AQS内部的虚拟队列，方便于AQS管理需要获取锁的线程。
- **Sync内部抽象类：** ReentrantLock的内部类，继承AbstractQueuedSynchronizer类并实现了其定义的锁资源获取(tryAcquire)与释放(tryRelease)方法，同时也定义了lock()方法，提供给子类实现。
- **NonfairSync内部类：** ReentrantLock的内部类，继承Sync类，非公平锁的实现者。
- **FairSync内部类：** ReentrantLock的内部类，继承Sync类，公平锁的实现者。
- **Lock接口：** Java锁类的顶级接口，定义了一系列锁操作的方法，如：lock()、unlock()、tryLock等。
- **ReentrantLock：** Lock锁接口的实现者，内部存在Sync、NonfairSync、FairSync三个内部类，在创建时可以根据其内部fair参数决定使用公平锁/非公平锁，其内部操作绝大部分都是基于间接调用AQS方法完成。

我们可以通过上面类图关系看出AQS是一个抽象类，但是在其源码实现中并不存在任何抽象方法，这是因为AQS设计的初衷更倾向于作为一个基础组件，并不希望直接作为操作类对外输出，为真正的实现类提供基础设施，如构建同步队列，控制同步状态等。从设计模式角度来看，AQS采用的模板模式的模式构建的，其内部除了提供并发操作核心方法以及同步队列操作外，还提供了一些模板方法让子类自己实现，如加锁操作及解锁操作，为什么这么做呢？这是因为AQS作为基础组件，封装的是核心并发操作，但是实现上分为两种模式，即共享模式与独占模式，而这两种模式的加锁与解锁实现方式是不一样的，但AQS只关注内部公共方法实现并不关心外部不同模式的具体逻辑实现，所以提供了模板方法给子类使用，也就是说实现独占锁，如ReentrantLock需要自己实现tryAcquire()方法和tryRelease()方法，而实现共享模式的Semaphore，则需要实现tryAcquireShared()方法和tryReleaseShared()方法，这样做的好处是显而易见，无论是共享模式还是独占模式，其基础的实现都是同一套组件(AQS)，只不过加锁/解锁的逻辑不同，更重要的是如果我们需要自定义锁的话，也变得非常简单，只需要选择不同的模式实现不同的加锁和解锁的模板方法即可，AQS提供给独占模式和共享模式的模板方法如下：

```java
java复制代码//独占模式下获取锁的方法
protected boolean tryAcquire(int arg) {
    throw new UnsupportedOperationException();
}
//独占模式下释放锁的方法
protected boolean tryRelease(int arg) {
    throw new UnsupportedOperationException();
}
//共享模式下获取锁的方法
protected int tryAcquireShared(int arg) {
    throw new UnsupportedOperationException();
}
//共享模式下释放锁的方法
protected boolean tryReleaseShared(int arg) {
    throw new UnsupportedOperationException();
}
//判断是否持有独占锁的方法
protected boolean isHeldExclusively() {
    throw new UnsupportedOperationException();
}
```

到此我们对于AQS这个并发核心组件的原理大致有了一定了解，接下来我们会带着大家基于ReetrantLock进一步分析AQS的具体实现过程。

## 四、基于ReetrantLock分析AQS独占模式实现过程及原理

### 4.1、ReetrantLock中的NonfairSync非公平锁

AQS同步器对于同步状态标识state的管理是基于其内部FIFO双向链表的同步队列实现的。当一条线程获取锁失败时，AQS同步器会将该线程本身及其相关信息封装成Node节点加入同步队列，同时也会阻塞当前线程，直至同步状态标识state被释放时，AQS才会将同步队列中头节点head内的线程唤醒，让其尝试修改state标识获取锁。下面我们重点来分析一下获取锁、释放锁以及将线程封装成节点加入队列的具体逻辑，这里先从ReetrantLock非公平锁的角度入手分析AQS的具体实现。

```java
java复制代码// 构造函数：默认创建的锁属于非公平锁(NonfairSync)类型
public ReentrantLock() {
    sync = new NonfairSync();
}
// 构造函数：根据传入参数创建锁类型(true公平锁/false非公平锁)
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
// 加锁/获取锁操作
public void lock() {
     sync.lock();
}
```

#### 4.1.1、ReetrantLock中获取锁lock()方法原理分析

我们先从非公平锁的角度开始分析：

```java
java复制代码/**
 * 非公平锁类<Sync子类>
 */
static final class NonfairSync extends Sync {
    // 加锁
    final void lock() {
        // 执行CAS操作，修改同步状态标识获取锁资源
        // 因为存在多条线程同时修改的可能，所以需要用CAS操作保证原子性
        if (compareAndSetState(0, 1))
            // 成功则将独占锁线程设置为当前线程  
            setExclusiveOwnerThread(Thread.currentThread());
        else acquire(1); // 否则再次请求同步状态
    }
}
```

在NonfairSync类中对于获取锁的实现过程大概如下：首先对state进行cas操作尝试将同步状态标识从0修改为1.如果成功则返回true，代表成功获取同步状态，获取锁资源成功，之后再将独占锁线程设置为当前获取同步状态的线程。反之，如果为false则代表获取锁失败，当返回false时执行`acquire(1)`方法，该方法对于线程中断操作不敏感，代表着即使当前线程获取锁失败被加入同步队列等待，后续对当前线程执行中断操作，当前线程也不会从同步队列中移出。`acquire(1)`如下：

```java
java复制代码public final void acquire(int arg) {
    // 再次尝试获取同步状态
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

`acquire()`是AQS中提供的方法，这里传入参数arg代表着获取同步状态后设置的值(即要设置state的值，而state为0时是锁资源释放状态，1则是锁资源占用状态)，因为要获取锁，所以这里一般传递参数为1，进入方法后首先会执行`tryAcquire(arg)`方法，在前面的分析中我们发现AQS是将该方法交由子类实现的，因此NonfairSync的`tryAcquire(arg)`方法是由ReetrantLock类内部Sync类实现。代码如下：

```java
java复制代码// NonfairSync类
static final class NonfairSync extends Sync {
    protected final boolean tryAcquire(int acquires) {
         return nonfairTryAcquire(acquires);
     }
 }

// ReetrantLock类内部类 - Sync类
abstract static class Sync extends AbstractQueuedSynchronizer {
  // NonfairTryAcquire方法
  final boolean nonfairTryAcquire(int acquires) {
      // 获取当前执行线程及当前同步器的状态标识值
      final Thread current = Thread.currentThread();
      int c = getState();
      // 判断同步状态是否为0，并尝试再次获取同步状态
      if (c == 0) {
          //执行CAS操作尝试修改同步标识
          if (compareAndSetState(0, acquires)) {
              // 如果为true则将独占锁线程设置为当前线程
              setExclusiveOwnerThread(current);
              return true;
          }
      }
      // 如果当前线程已获取锁，属于重入锁，再次获取锁后将state值加1
      else if (current == getExclusiveOwnerThread()) {
          // 对当前state值进行自增
          int nextc = c + acquires;
          if (nextc < 0) // overflow
              throw new Error("Maximum lock count exceeded");
          // 设置当前同步状态，当前只有一个线程持有锁，因为不会发生线程安全问
          // 题，可以直接执行 setState(nextc);
          setState(nextc);
          return true;
      }
      return false;
  }
  //省略......
}
```

分析如上代码我们可以从中得知，在非公平锁的`nonfairTryAcquire(acquires)`方法中做了两件事：

- 一、尝试重新修改同步标识获取锁资源(因为可能存在上个获取锁的线程在当前线程上次获取锁失败到目前这段时间之前释放了锁)，成功则将独占锁线程设置为当前获取同步状态的线程，最后返回ture。
- 二、判断当前线程current是否为独占锁线程OwnerThread，如果是则代表着当前线程已经获取过锁资源还未释放，属于锁重入，那么对state进行自增1，返回true。
- 如果当前线程前面两个判断都不满足，则返回false，也就代表着`nonfairTryAcquire(acquires)`执行结束。

不过在这个方法中值得注意的是，`nonfairTryAcquire(acquires)`方法中修改state同步标识时使用的是cas操作保证线程安全，因此只要任意一个线程调用`nonfairTryAcquire(acquires)`方法并设置成功即可获取锁，不管该线程是新到来的还是已在同步队列的线程，毕竟这是非公平锁，并不保证同步队列中的线程一定比新到来线程请求(可能是head结点刚释放同步状态然后新到来的线程恰好获取到同步状态)先获取到锁，这点跟后面还会分析的公平锁不同。那么我们再次回到之前NonfairSync类中的lock()方法中调用的`acquire(1)`方法：

```java
java复制代码public final void acquire(int arg) {
    // 再次尝试获取同步状态
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

在这里，如果`tryAcquire(arg)`执行后能够成功获取锁返回true，这个if自然不用继续往下执行，这是最理想的状态。但是如果当`tryAcquire(arg)`返回false时，则会继续执行`addWaiter(Node.EXCLUSIVE)`封装线程入列操作(因为ReetrantLock属于独占式锁，所以Node节点类型属于Node.EXCLUSIVE)。addWaiter方法代码如下：

```java
java复制代码private Node addWaiter(Node mode) {
    // 将请求同步状态失败的线程封装成Node节点
    Node node = new Node(Thread.currentThread(), mode);

    Node pred = tail;
    // 如果是第一个节点加入肯定为空，跳过。
    // 如果不是第一个节点则直接执行CAS入队操作，尝试在尾部快速添加
    if (pred != null) {
        node.prev = pred;
        // 使用CAS执行尾部节点替换，尝试在尾部快速添加
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    // 如果第一次加入或者CAS操作没有成功执行enq入队操作
    enq(node);
    return node;
}
```

在`addWaiter()`方法中，首先将当前线程和传入的节点类型Node.EXCLUSIVE封装成了一个Node节点，然后将AQS中的全局变量tail(指向AQS内部维护的同步队列队尾的节点)赋值给了pred用于判断，如果队尾节点不为空，则代表同步队列中已经存在节点，直接尝试执行CAS操作将当前封装的Node快速追加到队列尾部，如果CAS失败则执行enq(node)方法。当然，如果在判断时，tail节点为空，也就代表着同步队列中还没有任何节点存在，那么也会直接执行enq(node)方法。我们接着继续分析enq(node)函数的实现：

```java
java复制代码private Node enq(final Node node) {
    // 死循环
    for (;;) {
         Node t = tail;
         // 如果队列为null，即没有头结点
         if (t == null) { // Must initialize
             // 创建并使用CAS设置头结点
             if (compareAndSetHead(new Node()))
                 tail = head;
         } else { // 队尾添加新结点
             node.prev = t;
             if (compareAndSetTail(t, node)) {
                 t.next = node;
                 return t;
             }
         }
     }
}
```

在这个方法中使用了`for(;;)`开始了一个死循环并在其内进行CAS操作(可以避免并发问题出现)。在其中做了两件事情：一是如果AQS内部的同步队列还没有初始化则创建一个新的节点然后再调用`compareAndSetHead()`方法将该节点设置为头节点；二是如果同步队列已经存在的情况下则将传递进来的节点快速添加到队尾。注意这两个步骤都存在同一时间内多条线程一同操作的可能，如果有一条线程修改head和tail成功，那么其他线程将继续循环，直到修改成功，这里使用CAS原子操作进行头节点head设置和尾节点tail替换，可以保证线程安全。同时从这里也可以看出head节点本身不存任何数据，仅仅只是一个new出来的Node节点，它只是作为一个牵头节点，而tail永远指向尾部节点(前提是队列不为null)。

> 例：线程T1,T2,T3,T4,T5,T6六条线程同时进行入队操作，但是只有T2入队成功，其他五条线程(T1,T3,T4,T5,T6)将会继续循环直至入队成功为止。

添加到同步队列的节点都会进入一个自旋过程，每个节点都在观察时机等待条件满足时，开始获取同步状态，然后从同步队列中退出并结束自旋，回到之前的`acquire()`方法，自旋过程是在`acquireQueued(addWaiter(Node.EXCLUSIVE),arg))`方法中执行的，代码如下：

```java
java复制代码final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false; // 阻塞挂起标识
        // 一个死循环自旋
        for (;;) {
            // 获取前驱节点
            final Node p = node.predecessor();
            // 如果p为头节点才尝试获取同步状态
            if (p == head && tryAcquire(arg)) {
                // 将node设置为头节点
                setHead(node);
                // 将原有的head节点设置为null方便GC
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            // 如果前驱节点不是head，判断是否阻塞挂起线程
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            // 如果最终都没能成功获取同步状态，结束该线程的请求
            cancelAcquire(node);
    }
}
```

当前节点中的线程在死循环（自旋）执行过程中，当节点的前驱节点为头节点时开始尝试获取同步状态(符合FIFO原则)。head节点是当前占有同步状态标识的线程节点，只有当head节点释放同步状态唤醒后继节点时，后继节点才可能获取同步状态，所以这也是为什么说：只有当节点的前驱节点为头节点时才开始尝试获取同步状态的原因，在此之外的其他时候将被挂起。如果当前节点已经开始尝试获取同步状态，进入if后则会执行`setHead()`方法将当前线程设置为head节点，如下：

```java
java复制代码// 将传递的节点设置为同步队列的头节点
private void setHead(Node node) {
    head = node;
    // 清空当前节点存储的数据信息
    node.thread = null;
    node.prev = null;
}
```

node节点被设置为head头节点后，当前节点存储的线程以及前驱节点信息将会清空，因为当前线程已经成功获取到了锁资源，没有必要再存储线程信息，同时因为当前节点已经成为了头节点，不存在前驱节点了，所以也会被清空信息。head节点只保留指向后继节点的信息方便当前节点释放锁资源时唤醒后继线程。如上则是节点的前驱节点为头节点时会执行的逻辑，如果节点的前驱节点并不是head则会执行`if  (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) interrupted = true;`逻辑，代码如下：

```java
java复制代码private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    // 获取当前节点的等待状态
    int ws = pred.waitStatus;
    // 如果为等待唤醒（SIGNAL）状态则返回true
    if (ws == Node.SIGNAL)
        return true;
    // 如果当前节点等待状态大于0则说明是结束状态，
    // 遍历前驱节点直到找到没有结束状态的节点
    if (ws > 0) {
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        // 如果当前节点等待状态小于0又不是SIGNAL状态，
        // 则将其设置为SIGNAL状态，代表该节点的线程正在等待唤醒
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}

private final boolean parkAndCheckInterrupt() {
    // 将当前线程挂起
    LockSupport.park(this);
    // 获取线程中断状态,interrupted()是判断当前中断状态，
    // 而并不是中断线程，因此结果可能是true也可能false并返回
    return Thread.interrupted();
}

LockSupport → park()方法：
public static void park(Object blocker) {
    Thread t = Thread.currentThread();
    // 设置当前线程的监视器blocker
    setBlocker(t, blocker);
    // 调用了native方法到JVM级别的阻塞机制阻塞当前线程
    UNSAFE.park(false, 0L);
    // 阻塞结束后把blocker置空
    setBlocker(t, null);
}
```

`shouldParkAfterFailedAcquire()`方法的作用是判断节点的前驱节点是否为等待唤醒状态(SIGNAL状态)，如果是则返回true。如果前驱节点的waitStatus大于0(只有CANCELLED结束状态=1>0)，既代表该前驱结点已没有用了，应该从同步队列移除，执行do/while循环遍历所有前驱节点，直到寻找到非CANCELLED状态的节点。但是如果当前节点的前驱节点的waitStatus不为CANCELLED结束状态，也不为SIGNAL等待唤醒状态，也就是代表节点是刚从Condition的条件等待队列转移到同步队列，结点状态为CONDITION状态，因此需要转换为SIGNAL状态，那么则将其转换为SIGNAL状态，等待被唤醒。
 当`shouldParkAfterFailedAcquire()`方法返回true则代表着当前节点的前驱节点为(SIGNAL等待唤醒状态，但是该前驱节点又不是head头节点时，则使用`parkAndCheckInterrupt()`挂起线程，然后将节点状态改变为WAITING状态。当节点状态为WAITING状态时则需要等待unpark()操作来唤醒它，到此ReetrantLock内部间接通过AQS的FIFO的同步队列就完成了lock()加锁操作，下面我们可以总结一下整体的流程图：
 ![tryAcquire(arg)执行过程](（四）深入剖析并发之AQS独占锁&重入锁(ReetrantLock)及Condition实现原理.assets/909ad64ab6074172893da1caac2ced37tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)
 ![AQS之图解独占式获取锁过程](（四）深入剖析并发之AQS独占锁&重入锁(ReetrantLock)及Condition实现原理.assets/e31b6c2f1e244198b3c231803a028020tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

#### 4.1.2、ReetrantLock中一些其他获取锁资源方法的原理

在前面已经带着大家详细的谈到了ReetrantLock.lock()方法的具体实现原理了，那么我们在开发过程中，有时还会用到可中断的获取方式加锁，例如调用ReetrantLock的`lockInterruptibly()、tryLock()`，那么这些方法最终底层都会间接的调用到`doAcquireInterruptibly()`方法。如下：

```java
java复制代码 private void doAcquireInterruptibly(int arg)
    throws InterruptedException {
    // 封装一个Node节点尝试入队操作
    final Node node = addWaiter(Node.EXCLUSIVE);
    boolean failed = true;
    try {
        for (;;) {
            // 获取当前节点的前驱节点
            final Node p = node.predecessor();
            // 如果前驱节点为head节点则尝试获取锁资源/同步状态标识
            if (p == head && tryAcquire(arg)) {
                // 获取成功后将当前节点设置成head节点
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                // 直接抛异常，中断线程的同步状态请求
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

与lock()方法的区别在于：

```java
java复制代码/** ---------------lock()--------------- */
// 如果前驱节点不是head，判断是否阻塞挂起线程
if (shouldParkAfterFailedAcquire(p, node) &&
    parkAndCheckInterrupt())
    interrupted = true;
    
/** --------lockInterruptibly()、tryLock()------- */
if (shouldParkAfterFailedAcquire(p, node) &&
    parkAndCheckInterrupt())
    // 直接抛异常，中断线程的同步状态请求
    throw new InterruptedException();
```

在可中断式获取锁资源的方式中，当检测到线程的中断操作后，直接抛出异常，从而中断线程的同步状态请求，移除同步队列。

#### 4.1.3、ReetrantLock中的unlock()释放锁原理分析

一般而言，我们在使用ReetrantLock这类显式锁时，获取锁之后也需要我们手动释放锁资源。在ReetrantLock中当你调用了lock()获取锁资源之后，也需要我们手动调用unlock()释放锁。unlock()释放锁的代码如下：

```java
java复制代码// ReetrantLock → unlock()方法
public void unlock() {
    sync.release(1);
}

// AQS → release()方法
public final boolean release(int arg) {
    // 尝试释放锁
    if (tryRelease(arg)) {
        // 获取头结点用于判断
        Node h = head;
        if (h != null && h.waitStatus != 0)
            // 唤醒后继节点的线程
            unparkSuccessor(h);
        return true;
    }
    return false;
}

// ReentrantLock → Sync → tryRelease(int releases)方法
protected final boolean tryRelease(int releases) {
  // 对于同步状态进行修改：获取锁是+，释放锁则为-
  int c = getState() - releases;
  // 如果当前释放锁的线程不为持有锁的线程则抛出异常
  if (Thread.currentThread() != getExclusiveOwnerThread())
      throw new IllegalMonitorStateException();
  boolean free = false;
  // 判断状态是否为0，如果是则说明已释放同步状态
  if (c == 0) {
      free = true;
      // 设置Owner为null
      setExclusiveOwnerThread(null);
  }
  // 设置更新同步状态
  setState(c);
  return free;
}
```

释放锁的逻辑相对与获取锁的逻辑来说要简单许多，`unlock()`方法最终是调用`tryRelease(int releases)`释放锁的，而`tryRelease(int releases)`则是ReetrantLock实现的方法，因为在AQS中没有提供具体实现，而是交由了子类自己实现具体的逻辑。释放锁资源后会使用`unparkSuccessor(h)`唤醒后继节点的线程。unparkSuccessor(h)的代码如下：

```java
java复制代码private void unparkSuccessor(Node node) {
    // node一般为当前线程所在的节点,获取当前线程的等待状态
    int ws = node.waitStatus;
    if (ws < 0) // 置零当前线程所在的节点状态，允许失败
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next; // 获取当前节点的后继节点
    if (s == null || s.waitStatus > 0) { // 如果为空或已结束
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            // 等待状态<=0的节点，代表是还有效的节点
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread); // 唤醒后继节点线程
}
```

在`unparkSuccessor(h)`方法中，最终是通过`unpark()`方法唤醒后继节点中未放弃竞争锁资源的线程，也就是waitStatus<=0的节点s，在我们前面分析获取锁原理时，曾分析到一个自旋的方法`acquireQueued()`，我们现在可以结合起来一同理解。s节点的线程被唤醒后，会执行`acquireQueued()`方法中的代码`if (p == head && tryAcquire(arg))`进行判断操作(就算p不为head头结点也不会有影响，因为会执行`shouldParkAfterFailedAcquire()`方法)，当前持有锁资源的线程所在的节点node释放之后，s经过`unparkSuccessor()`方法的逻辑处理之后，s便成为了AQS同步队列中最前端的未放弃锁资源竞争的线程，那最终经过`shouldParkAfterFailedAcquire()`方法逻辑处理之后，s节点也会成为head头结点的next节点。所以最终在自旋方法中，第二次循环到`if (p == head && tryAcquire(arg))`逻辑时`p==head`的判断式也就会成立了，然后s会将自己设置为head头结点表示自己已经获取到了锁资源，最后整个`acquire()`方法执行结束。

> 总而言之，在AQS内部维护着一个FIFO的同步队列，当一个线程执行ReetrantLock.lock()方法获取锁失败时，该线程会被封装成Node节点加入同步队列等待锁资源的释放，期间不断执行自旋逻辑。当该线程所在节点的前驱节点为队列头结点时，当前线程就会开始尝试对同步状态标识state进行修改(+1)，如果可以修改成功则代表获取锁资源成功，然后将自己所在的节点设置为队头head节点，表示自己已经持有锁资源。那么当一个线程调用ReetrantLock.unlock()释放锁时，最终会调用Sync内部类中的tryRelease(int releases)方法再次对同步状态标识state进行修改(-1)，成功之后唤醒当前线程所在节点的后继节点中的线程。

### 4.2、ReetrantLock中的FairSync公平锁

在前面我们已经详细的分析了ReetrantLock中非公平锁的实现过程，那么我们接下来再去一探ReetrantLock中公平锁的实现原理。不过在此之前我们先对于需要的公平和非公平的概念有个认知。所谓的公平与非公平是基于线程到来的时间顺序为基准来区分的，公平锁指的是完全遵循FIFO原则的一种模式。也就代表着，在时间顺序上来看，公平锁模式下，先执行获取锁逻辑的线程就一定会先持有锁资源。同理，非公平锁则反之。下面我们来看一下公平锁FairSync类中`tryAcquire(int acquires)`方法的实现。

```java
java复制代码// ReetrantLock → FairSync → tryAcquire(int acquires)
protected final boolean tryAcquire(int acquires) {
    // 获取当前线程
    final Thread current = Thread.currentThread();
    // 获取同步状态标识值
    int c = getState();
    if (c == 0) { // 如果为0代表目前没有线程持有锁资源
    // 在公平锁实现中这里先判断同步队列是否存在节点
        if (!hasQueuedPredecessors() &&
            compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```

FairSync类中获取锁方法`tryAcquire(int acquires)`的实现与NonFairSync类中获取锁方法`nonfairTryAcquire(int acquires)`唯一不同的是：公平锁的实现中，在尝试修改state之前会先调用`hasQueuedPredecessors()`判断AQS内部的同步队列是否存在节点。如果存在则说明在此之前已经有线程提交了获取锁的请求，那么当前线程会被直接封装成Node节点追加到队尾等待。而在非公平锁的`tryAcquire(int acquires)`实现中，不管队列中是否已经存在节点，都会先尝试修改同步状态标识state获取锁，当获取锁失败时才会将当前线程封装成Node节点加入队列。但是我们在实际开发过程中，如果不需要考虑业务处理时执行顺序的情况下，我们应该优先考虑使用非公平锁，因为往往在实际应用过程中，非公平锁的性能会大大超出公平锁！

### 4.3、实际开发过程中ReetrantLock与synchronized如何抉择？

在前面的文章：《[彻底理解Java并发编程之Synchronized关键字实现原理剖析](https://link.juejin.cn?target=https%3A%2F%2Fwww.jianshu.com%2Fp%2F884eb51266e4)》中我们曾详细的谈到过Java中的隐式锁的synchronized的底层实现，我们也曾谈到在JDK1.6之后，JVM对于Synchronized关键字进行了很大程度上的优化，那么在实际开发过程中我们又该如何在ReetrantLock与synchronized进行选择呢？synchronized相对来说使用更加方便、语义更清晰，同时JVM也为我们自动优化了。而ReetrantLock则使用起来更加灵活，同时也提供了多样化的支持，比如超时获取锁、可中断式获取锁、等待唤醒机制的多个条件变量(Condition)等。所以在我们需要用到这些功能时我们可以选择ReetrantLock。但是具体采用哪个还是需要根据业务需求决定。

> 例如：某个项目在凌晨一点至凌晨五点流量非常巨大，但是其他时间内相对来说访问频率并不高，对于这种情况采用哪种锁更为合适？答案是ReetrantLock。
>  为什么？因为在前面关于synchronized的文章中我们曾提到过，synchronized的锁升级/膨胀是不可逆的，几乎在Java程序运行过程中不会出现锁降级的情况。那么在这种业务场景下，流量剧增的那段时间会有可能导致synchronized直接膨胀成重量级锁，而synchronized一旦升级到重量级锁，那么这把锁之后的每次获取锁都是重量级锁，这样会大大的影响程序性能。

### 4.4、ReetrantLock实现总结

- 基础组件：
  - 同步状态标识：对外显示锁资源的占有状态
  - 同步队列：存放获取锁失败的线程
  - 等待队列：用于实现多条件唤醒
  - Node节点：队列的每个节点，线程封装体
- 基础动作：
  - cas修改同步状态标识
  - 获取锁失败加入同步队列阻塞
  - 释放锁时唤醒同步队列第一个节点线程
- 加锁动作：
  - 调用tryAcquire()修改标识state，成功返回true执行，失败加入队列等待
  - 加入队列后判断节点是否为signal状态，是就直接阻塞挂起当前线程
  - 如果不是则判断是否为cancel状态，是则往前遍历删除队列中所有cancel状态节点
  - 如果节点为0或者propagate状态则将其修改为signal状态
  - 阻塞被唤醒后如果为head则获取锁，成功返回true，失败则继续阻塞
- 解锁动作：
  - 调用tryRelease()释放锁修改标识state，成功则返回true，失败返回false
  - 释放锁成功后唤醒同步队列后继阻塞的线程节点
  - 被唤醒的节点会自动替换当前节点成为head节点

## 五、多条件等待唤醒机制之神奇的Condition实现原理

在Java并发编程中，每个Java堆中的对象在“出生”的时刻都会“伴生”一个监视器对象，而每个Java对象都会有一组监视器方法：`wait()`、`notify()`以及`notifyAll()`。我们可以通过这些方法实现Java多线程之间的协作和通信，也就是等待唤醒机制，如常见的生产者-消费者模型。但是关于Java对象的这组监视器方法我们在使用过程中，是需要配合synchronized关键字才能使用，因为实际上Java对象的等待唤醒机制是基于monitor监视器对象实现的。与synchronized关键字的等待唤醒机制相比，Condition则更为灵活，因为synchronized的`notify()`只能随机唤醒等待锁的一个线程，而Condition则可以更加细粒度的精准唤醒等待锁的某个线程。与synchronized的等待唤醒机制不同的是，在monitor监视器模型上，一个对象拥有一个同步队列和一个等待队列，而AQS中一个锁对象拥有一个同步队列和多个等待队列。对象监视器Monitor锁实现原理如下：
 ![Monitor监视器锁实现原理](（四）深入剖析并发之AQS独占锁&重入锁(ReetrantLock)及Condition实现原理.assets/3697c4d696184d3bab8d9f6b05a25e84tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

### 5.1、快速认识及上手Condition实战

Condition是一个接口类，具体实现者为AQS内部的ConditionObject类，Condition中定义方法如下：

```java
java复制代码public interface Condition {
    /**
    * 调用当前方法会使当前线程处于等待状态直到被通知(signal)或中断
    * 当其他线程调用singal()或singalAll()方法时，当前线程将被唤醒
    * 当其他线程调用interrupt()方法中断当前线程等待状态
    * await()相当于synchronized等待唤醒机制中的wait()方法
    */
    void await() throws InterruptedException;
    
    /**
    * 作用与await()相同，但是该方法不响应线程中断操作
    */
    void awaitUninterruptibly();
    
    /**
    * 作用与await()相同，但是该方法支持超时中断（单位：纳秒）
    * 当线程等待时间超出nanosTimeout时则中断等待状态
    */
    long awaitNanos(long nanosTimeout) throws InterruptedException;
    
    /**
    * 作用与awaitNanos(long nanosTimeout)相同，但是该方法可以声明时间单位
    */
    boolean await(long time, TimeUnit unit) throws InterruptedException;
    
    /**
    * 作用与await()相同，在deadline时间内被唤醒返回true，其他情况则返回false
    */
    boolean awaitUntil(Date deadline) throws InterruptedException;
    
    /**
    * 当有线程调用该方法时，唤醒等待队列中的一个线程节点
    * 并将该线程从等待队列移动同步队列阻塞等待锁资源获取
    * signal()相当于synchronized等待唤醒机制中的notify()方法
    */
    void signal();
    
    /**
    * 作用与signal()相同，不过该方法的作用是唤醒该等待队列中的所有线程节点
    * signalAll()相当于synchronized等待唤醒机制中的notifyAll()方法
    */
    void signalAll();
}
```

如上便是Condition接口中定义的方法，总体可以分为两类，一类是线程挂起/等待类的await方法，另一类则是线程唤醒类的signal方法，接下来我们运用Condition来实现一个经典的消费者/生产者的小案例简单了解一下Condition的使用：

```java
java复制代码public class Bamboo {
    private int bambooCount = 0;
    private boolean flag = false;

    Lock lock = new ReentrantLock();
    Condition producerCondition = lock.newCondition();
    Condition consumerCondition = lock.newCondition();

    public void producerBamboo() {
        lock.lock(); // 获取锁资源
        try {
            while (flag) { // 如果有竹子
                try {
                    producerCondition.await(); // 挂起生产竹子的线程
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            bambooCount++; // 竹子数量+1
            System.out.println(Thread.currentThread().getName() + "....生产竹子，目前竹子数量：" + bambooCount);
            flag = true; // 竹子余量状态改为true
            consumerCondition.signal(); // 生产好竹子之后，唤醒消费竹子的线程
        } finally {
            lock.unlock(); // 释放锁资源
        }
    }

    public void consumerBamboo() {
        lock.lock(); // 获取锁资源
        try {
            while (!flag) { // 如果没有竹子
                try {
                    consumerCondition.await(); // 挂起消费竹子的线程
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            bambooCount--; // 竹子数量-1
            System.out.println(Thread.currentThread().getName() + "....消费竹子，目前竹子数量：" + bambooCount);
            flag = false; // 竹子余量状态改为false
            producerCondition.signal(); // 消费完成竹子之后，唤醒生产竹子的线程
        } finally {
            lock.unlock(); // 释放锁资源
        }
    }
}
/**------------------分割线--------------------**/
// 测试类
public class ConditionDemo {
    public static void main(String[] args){
        Bamboo b = new Bamboo();
        Producer producer = new Producer(b);
        Consumer consumer = new Consumer(b);

        // 生产者线程组
        Thread t1 = new Thread(producer,"生产者-t1");
        Thread t2 = new Thread(producer,"生产者-t2");
        Thread t3 = new Thread(producer,"生产者-t3");

        // 消费者线程组
        Thread t4 = new Thread(consumer,"消费者-t4");
        Thread t5 = new Thread(consumer,"消费者-t5");
        Thread t6 = new Thread(consumer,"消费者-t6");

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
    }
}
// 生产者
class Producer implements Runnable{
    private Bamboo bamboo;

    public Producer(Bamboo bamboo) {
        this.bamboo = bamboo;
    }
    @Override
    public void run() {
        for (;;){
            bamboo.producerBamboo();
        }
    }
}

// 生产者
class Consumer implements Runnable{
    private Bamboo bamboo;

    public Consumer(Bamboo bamboo) {
        this.bamboo = bamboo;
    }
    @Override
    public void run() {
        for (;;){
            bamboo.consumerBamboo();
        }
    }
}
```

如上代码中运用一个生产/消费竹子的案例简单的使用了一下Condition。在该案例中存在六条线程，t1,t2,t3为生产者线程组，t4,t5,t6为消费者线程组，六条线程同时执行，需要保证生产线程组先生产竹子后消费者线程组才能消费竹子，否则消费者线程组的线程只能等待直至生产者线程组生产出竹子为止，不能出现重复消费的情况。在Bamboo类中定义了两个方法：producerBamboo()以及consumerBamboo()用于生产和消费竹子。并且同时定义了一个全局的ReetrantLock锁，用于保证两组线程在同时执行过程中不出现线程安全问题。而因为需要保证生产/消费的前后顺序，所以基于lock锁对象创建了两个等待条件：producerCondition、consumerCondition，前者控制生产线程组在竹子数量不为零时，生产线程等待，后者则控制消费者线程组。这里同时定义了一个flag标志对外展示竹子的余量情况，为false则代表没有竹子，需先生产竹子，生产完成后唤醒消费者线程，为true时则反之。

> 在如上案例中对比synchronized的等待/唤醒机制来说，优势在于可以创建两个等待条件producerCondition、consumerCondition，因为存在两个等待队列，所以可以精准的控制生产者线程组和消费者线程组。而如果使用synchronized的wait()/notify()来实现如上案例则可能出现消费线程在消费完成竹子之后唤醒线程时唤醒的还是消费线程这种情况，因为在Monitor对象中只存在一个等待队列，如果在synchronized想避免出现这种问题出现则只能使用notifyAll()唤醒等待队列中的所有线程。但是因为需要唤醒等待队列中的所有线程，所以性能方面会比Condition慢上许多。

### 5.2、Condition实现原理分析

在前面我们提到过，Condition只是一个接口，具体的落地实现者为AQS内部的ConditionObject类，在本文最开始分析AQS时我们也曾提到，在AQS内部存在两种队列：同步队列以及等待队列，等待队列则是基于Condition而言的。同步队列与等待队列中的节点类型都是AQS内部的Node构成的，只不过等待队列中的Node节点的waitStatus为CONDITION状态。在ConditionObject类中存在两个节点：firstWaiter、lastWaiter用于存储等待队列中的队首节点以及队尾节点，每个节点使用Node.nextWaiter存储下一个节点的引用，因此等待队列是一个单向队列。所以AQS同步器的总体结构如下： ![AQS整体结构](（四）深入剖析并发之AQS独占锁&重入锁(ReetrantLock)及Condition实现原理.assets/ba7775ff99de4041a2c147ac9286bda2tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)
 如上图，与同步队列不同的是：每个Condition都对应一个等待队列，如果在一个ReetrantLock锁上创建多个Condition，也就相当于会存在多个等待队列。同时，虽然同步队列与等待队列中的节点都是由Node类构成的，但是同步队列中的Node节点是存在pred前驱节点以及next后继节点引用的双向链表类型，而等待队列中的每个节点则只使用nextWaiter存储后继节点引用的单向链表类型。但是与同步队列一致，等待队列也是一种FIFO的队列，队列每个节点都会存储Condition对象上等待的线程信息。当一个线程调用await挂起类的方法时，该线程首先会释放锁，同时构建一个Node节点封装线程的相关信息，并将其加入等待队列，直到被唤醒、中断或者超时才会从队列中移除。下面我们从源码角度探究Condition等待/唤醒机制的原理：

```java
java复制代码public final void await() throws InterruptedException {
    // 判断线程是否出现中断信号
    if (Thread.interrupted())
       // 响应中断则直接抛出异常中断线程执行
      throw new InterruptedException();
    // 封装线程信息构建新的节点加入等待队列并返回
    Node node = addConditionWaiter();
    // 释放当前线程持有的锁锁资源，不管当前线程重入多少次，全部置0
    int savedState = fullyRelease(node);
    int interruptMode = 0;
    // 判断节点是否在同步队列(SyncQueue)中,即是否被唤醒
    while (!isOnSyncQueue(node)) {
      // 如果不需要唤醒，则在JVM级别挂起当前线程
      LockSupport.park(this);
      // 判断是否被中断唤醒，如果是退出循环
      if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
          break;
    }
    // 被唤醒后执行自旋操作尝试获取锁，同时判断线程是否被中断
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
      interruptMode = REINTERRUPT;
    // 取消后进行清理
    if (node.nextWaiter != null) 
      // 清理等待队列中不为CONDITION状态的节点
      unlinkCancelledWaiters();
    if (interruptMode != 0)
      reportInterruptAfterWait(interruptMode);
}

// 构建节点封装线程信息入队方法
private Node addConditionWaiter() {
    Node t = lastWaiter;
    // 判断节点状态是否为结束状态，如果是则移除
    if (t != null && t.waitStatus != Node.CONDITION) {
        unlinkCancelledWaiters();
        t = lastWaiter;
    }
    // 构建新的节点封装当前线程相关信息，节点状态为CONDITION等待状态
    Node node = new Node(Thread.currentThread(), Node.CONDITION);
    // 将节点加入队列
    if (t == null)
        firstWaiter = node;
    else
        t.nextWaiter = node;
    lastWaiter = node;
    return node;
}
```

从如上代码观察中，不难发现，await()主要做了四件事：

- 一、调用addConditionWaiter()方法构建新的节点封装线程信息并将其加入等待队列
- 二、调用fullyRelease(node)释放锁资源(不管此时持有锁的线程重入多少次都一律将state置0)，同时唤醒同步队列中后继节点的线程。
- 三、调用isOnSyncQueue(node)判断节点是否存在同步队列中，在这里是一个自旋操作，如果同步队列中不存在当前节点则直接在JVM级别挂起当前线程
- 四、当前节点线程被唤醒后，即节点从等待队列转入同步队列时，则调用acquireQueued(node, savedState)方法执行自旋操作尝试重新获取锁资源

至此，整个await()方法结束，整个线程从调用await()方法→构建节点入列→释放锁资源唤醒同步队列后继节点→JVM级别挂起线程→唤醒竞争锁资源流程完结。其他await()等待类方法原理类似则不再赘述，下面我们再来看看singal()唤醒方法：

```java
java复制代码public final void signal() {
     // 判断当前线程是否持有独占锁资源，如果未持有则直接抛出异常
    if (!isHeldExclusively())
          throw new IllegalMonitorStateException();
      Node first = firstWaiter;
      // 唤醒等待队列第一个节点的线程
      if (first != null)
          doSignal(first);
}
```

在这里，singal()唤醒方法一共做了两件事：

- 一、判断当前线程是否持有独占锁资源，如果调用唤醒方法的线程未持有锁资源则直接抛出异常（共享模式下没有等待队列，所以无法使用Condition）
- 二、唤醒等待队列中的第一个节点的线程，即调用doSignal(first)方法

下面我们来看看doSignal(first)方法的实现：

```java
java复制代码private void doSignal(Node first) {
    do {
        // 移除等待队列中的第一个节点，如果nextWaiter为空
        // 则代表着等待队列中不存在其他节点，那么将尾节点也置空
        if ( (firstWaiter = first.nextWaiter) == null)
            lastWaiter = null;
        first.nextWaiter = null;
    // 如果被通知上个唤醒的节点没有进入同步队列（可能出现被中断的情况），
    // 等待队列中还存在其他节点则继续循环唤醒后继节点的线程
    } while (!transferForSignal(first) &&
             (first = firstWaiter) != null);
}

// transferForSignal()方法
final boolean transferForSignal(Node node) {
    /*
     * 尝试修改被唤醒节点的waitStatus为0即初始化状态
     *      如果设置失败则代表着当前节点的状态不为CONDITION等待状态，
     *      而是结束状态了则返回false返回doSignal()继续唤醒后继节点
     *  为什么说设置失败则代表着节点不为CONDITION等待状态？
     *      因为可以执行到此处的线程必定是持有独占锁资源的，
     *      而此处使用的是cas机制修改waitStatus，失败的原因只有一种：
     *          预期值waitStatus不等于CONDITION
     */
    if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
        return false;
    // 快速追加到同步队列尾部，同时返回前驱节点p
    Node p = enq(node);
    // 判断前驱节点状态是否为结束状态或者在设置前驱节点状态为SIGNAL失败时，
    // 唤醒被通知节点内的线程
    int ws = p.waitStatus;
    if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
        // 唤醒node节点内的线程
        LockSupport.unpark(node.thread);
    return true;
}
```

在如上代码中，可以通过我的注释发现，doSignal()也只做了三件事：

- 一、将被唤醒的第一个节点从等待队列中移除，然后再维护等待队列中firstWaiter和lastWaiter的指向节点引用
- 二、将等待队列中移除的节点追加到同步队列尾部，如果同步队列追加失败或者等待队列中还存在其他节点的话，则继续循环唤醒其他节点的线程
- 三、加入同步队列成功后，如果前驱节点状态已经为结束状态或者在设置前驱节点状态为SIGNAL失败时，直接通过LockSupport.unpark()唤醒节点内的线程

至此，Signal()方法逻辑结束，不过需要注意的是：我们在理解Condition的等待/唤醒原理的时候需要将await()/signal()方法结合起来理解。在signal()逻辑完成后，被唤醒的线程则会从前面的await()方法的自旋中退出，因为当前线程所在的节点已经被移入同步队列，所以`while (!isOnSyncQueue(node))`条件不成立，循环自然则终止，进而被唤醒的线程会调用`acquireQueued()`开始尝试获取锁资源。

## 六、Condition接口与Monitor对象等待/唤醒机制的区别

最后我们来简单的对比一下ReetrantLock的Condition多条件等待/唤醒机制与与Synchronized的Monitor对象锁等待/唤醒机制之间的区别：

| 对比项           | Monitor       | Condition                       |
| ---------------- | ------------- | ------------------------------- |
| 前置条件         | 需持有对象锁  | 需持有独占锁且创建Condition对象 |
| 调用方式         | Object.wait() | condition.await类方法都可       |
| 队列数量         | 一个          | 多个                            |
| 等待时释放锁资源 | 支持          | 支持                            |
| 线程中断         | 不支持        | 支持                            |
| 超时中断         | 不支持        | 支持                            |
| 超时等待         | 支持          | 支持                            |
| 精准唤醒线程     | 不支持        | 支持                            |
| 唤醒全部线程     | 支持          | 支持                            |