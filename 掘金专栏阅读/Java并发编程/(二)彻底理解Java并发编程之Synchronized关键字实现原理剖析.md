## 一、Synchronized应用方式及锁类型

众所周知，在项目开发过程中使用多线程的效果就是一个字：快！

多线程编程能够给我们的程序带来很大的性能收益，同时也能够把机器的性能发挥到极致。

而随着如今时代的进步发展，机器早就摆脱了单核的限制，所以当我们在开发过程中，只是编写单线程的程序时，在很多时候无疑会浪费机器的计算能力。正因如此，多线程编程在我们现在的开发过程中显的越来越重要，同时也成了一线大厂面试必问的一个门槛。

而当我们在研究`Java`并发编程时，线程安全问题是我们的重要关注点，而构成这个问题的根本原因无非就三个要素：“多线程、共享资源（临界资源）、非原子性操作”。一句话概叙线程安全问题产生的根本原因：**多条线程同时对一个共享资源进行非原子性操作时会诱发线程安全问题**。（如果对于这三个概念存在疑问，请仔细阅读我的上篇文章理解：[《JMM与Volatile》](https://juejin.cn/post/6977323236186914852)）。

既然程序会出现了线程安全问题，那又该怎么去解决呢？无他，破坏掉构成这个问题的三要素中的任何一个就可以啦！因此为了解决这个问题，我们可以去把多线程的并行执行，变为单线程串行执行，同一时刻只让一条线程执行，这种方案有一个高大尚而响亮的名字：**互斥锁/排他锁**。

也就是当多条线程，同时执行一段被互斥锁保护的代码（临界资源）时，需要先获取锁，这时只会有一个线程获取到锁资源成功执行，其他线程将陷入等待的状态，直到当前线程执行完毕释放锁资源之后，其他线程才能执行。

在Java并发编程中提供了一种机制：`synchronized`关键字来实现互斥锁的功能。

当然我们也需要注意`Synchronized`的另一个作用：`Synchronized`可以保证一个线程对临界资源（共享资源）发生了改变后，能对其他所有线程可见，也就是代替上章节所说的`Volatile`可见性作用。

> PS：`Synchronized`无法完全取代`Volatile`，因为`Synchronized`可以保证可见性、原子性、“有序性”，但是无法禁止指令重排序，这点我们会在后面分析。

### 1.1、Synchronized三种锁类型

`Synchronized`本质上都是依赖对象来锁，根据不同的对象类型，可以分为三种锁粒度：

- **`this`锁**：当前实例锁
- **`class`锁**：类对象锁
- **`Object`锁**：对象实例锁

### 1.2、Synchronized三种应用方式

- 修饰实例成员方法：使用`this`锁，线程想要执行被`Synchronized`关键字修饰的普通方法，必须先获取当前实例对象的锁资源；
- 修饰静态成员方法：使用`class`锁，线程想要执行被`Synchronized`关键字修饰的静态方法，必须先获取当前类对象的锁资源；
- 修饰代码块：使用`Object`锁，使用给定的对象实现锁功能，线程想要执行被`Synchronized`关键字修饰的代码块，必须先获取当前给定对象的锁资源。

#### 1.2.1、synchronized修饰实例成员方法

```java
java复制代码public class SyncIncrDemo implements Runnable{
    //共享资源(临界资源)
    static int i = 0;

    //synchronized关键字修饰实例成员方法
    public synchronized void incr(){
        i++;
    }
    @Override
    public void run() {
        for(int j=0;j<1000;j++){
            incr();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        SyncIncrDemo syncIncrDemo = new SyncIncrDemo();
        Thread t1=new Thread(syncIncrDemo);
        Thread t2=new Thread(syncIncrDemo);
        t1.start();
        t2.start();
        /**
        *join：使得放弃当前线程的执行，并返回对应的线程，例如下面代码的意思就是：
         程序在main线程中调用t1,t2线程的join方法，则main线程放弃cpu控制权，并返回
         t1,t2线程继续执行直到线程t1,t2执行完毕;
         所以结果是t1,t2线程执行完后，才到主线程执行，相当于在main线程中同步t1,t2
         线程，t1,t2执行完了，main线程才有执行的机会
        */
        t1.join();
        t2.join();
        System.out.println(i);
    }
    /**
     * 输出结果:
     * 2000
     */
}
```

上述代码中，我们开启`t1、t2`两个线程操作同一个共享资源，即`int`变量`i`，由于自增的`i++`操作，在我们上章节分析到该操作并不具备原子性，具体是分为三步来执行：

- ①先从主存中读取值；
- ②在自己工作内存进行`+1`操作；
- ③将结果刷新回主存。

如果`t2`线程，在`t1`线程读取旧值和写回新值期间，也就是`t2`在`t1`在自己工作内存中做`+1`计算时，读取全局资源`i`的值，那`t2`会和`t1`看到同一个值（`i=1`），并执行相同值的`+1`操作，这也就造成了线程不安全，因此对于`incr`方法必须使用`synchronized`修饰，做到多线程的互斥，解决线程安全问题。

此时我们应该注意到：`synchronized`修饰的`incr()`，是一个对象实例方法。在这样的情况下，当前线程的锁便是`this`实例锁，也就是当前实例对象`syncIncrDemo`（任意对象都可以作为锁对象，依赖于对象头实现，稍后会分析）。

从代码执行结果来看确实是正确的，倘若我们没有使用`synchronized`关键字修饰`incr()`方法，其最终输出结果就有可能小于`2000`，这便是`synchronized`关键字的作用，示意图如下：

![多线程执行图-1]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/8a817e19c90745db9a809d1215d979d1tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

这里我们还需要意识到：当一个线程正在访问一个被`synchronized`修饰的实例方法时，其他线程则不能访问该对象的其他被`synchronized`修饰的对象实例方法，毕竟一个对象只有一把锁，当一个线程获取了该对象的锁之后，其他线程无法获取该对象的锁，所以无法访问该对象的其他被`synchronized`修饰的对象实例方法。

```java
java复制代码public class A {
    public synchronized void x(){}
    
    public synchronized void y(){}
}
```

比如上述这个例子中，当一条线程正在执行`x()`时，其他线程访问`y()`方法也会陷入阻塞。

但是如果有其他方法未被`synchronized`修饰，又或者其他被`synchronized`修饰的是静态方法，这类方法其他线程还是可以访问的，再来看个例子：

```java
java复制代码public class A {
    public synchronized void x(){}
    
    public synchronized void y(){}
    
    public static void main(String[] args) {
        new Thread(()->{
            A a1 = new A();
            a1.x();
        },"AA").strat();
        
        new Thread(()->{
            A a2 = new A();
            a2.x();
        },"BB").strat();
    }
}
```

如果线程`AA`访问的是`a1`对象的`x()`方法，另一个线程`BB`访问的是`a2`对象的`x()`方法，这样是允许同时访问的，因为两个实例对象锁并不同。此时如果两个线程操作数据并非共享的，可以保障线程安全，遗憾的是如果两个线程操作的是共享数据，那么线程安全就有可能无法保证了，如下代码将演示出该情况：

```java
java复制代码public class SyncIncrDemo implements Runnable{
    //共享资源(临界资源)
    static int i = 0;
    
    //synchronized关键字修饰实例成员方法
    public synchronized void incr(){
        i++;
    }
    @Override
    public void run() {
        for(int j=0;j<1000;j++){
            incr();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        SyncIncrDemo syncIncrDemo1 = new SyncIncrDemo();
        SyncIncrDemo syncIncrDemo2 = new SyncIncrDemo();
        Thread t1=new Thread(syncIncrDemo1);
        Thread t2=new Thread(syncIncrDemo2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(i);
    }
    /**
     * 输出结果:
     * 1991
     */
}
```

上述代码与前面不同的是：我们同时创建了两个新实例`syncIncrDemo1、syncIncrDemo2`，然后启动两个不同的线程对共享变量`i`进行操作，可是结果是`1991`，而不是期望结果`2000`，因为上述代码犯了严重的错误。

虽然我们使用`synchronized`修饰了`incr()`方法，但却`new`了两个不同的实例对象，这也就意味着存在着两把不同的实例对象锁，因此`t1`和`t2`都会获取各自的对象锁，`t1、t2`线程使用的不是同一把锁，因此线程安全是无法保证的，示意图如下：

![多线程执行图-2]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/9f4f3407bdab4281a2e4fdbf7c60f2e5tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

解决这种困境的方式是将`incr()`方法使用`static`来修饰，这样的话，锁对象就类的`class`对象，无论创建多少个实例对象，但对于的类对象（`class`对象）来说，虚拟机只会加载字节码后生成一个，在这样的情况下，锁对象就是唯一的。

下面我们看看如何使用将`synchronized`作用于静态的`incr()`方法。

#### 1.2.2、synchronized修饰静态成员方法

当`synchronized`用于修饰静态方法时，其锁就是当前类的`class`对象，当使用`class`锁时，当前`Java`程序中，一个类只会生成一个`class`对象，不会因为`new`出多个实例造成多把锁、线程分别获取不同锁资源的情况发生。

由于静态成员不属于任何一个实例对象，而是类成员，因此可以通过`class`对象锁控制静态成员的并发操作。需要注意的是：如果一个线程`A`，调用一个被`synchronized`修饰的普通实例方法；而线程`B`通过这个实例对象，调用被`synchronized`修饰的`static`方法，这是允许同时执行的，并不会发生互斥现象。

因为访问静态`synchronized`方法的线程，获取的是当前类的`class`对象的锁资源；而访问非静态`synchronized`方法的线程，获取的是当前实例对象锁资源，看如下代码：

```java
java复制代码public class SyncIncrDemo implements Runnable{
    //共享资源(临界资源)
    static int i = 0;

    //synchronized关键字修饰实例成员方法   锁对象：this  当前 new 的实例对象
    public synchronized void reduce(){
        i--;
    }
    
    //synchronized关键字修饰静态成员方法   锁对象：class  SyncIncrDemo.class
    public static synchronized  void incr(){
        i++;
    }
    @Override
    public void run() {
        for(int j=0;j<1000;j++){
            incr();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        SyncIncrDemo syncIncrDemo1 = new SyncIncrDemo();
        SyncIncrDemo syncIncrDemo2 = new SyncIncrDemo();
        Thread t1=new Thread(syncIncrDemo1);
        Thread t2=new Thread(syncIncrDemo2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(i);
    }
    /**
     * 输出结果:
     * 2000
     */
}
```

由于`synchronized`修饰的是静态`incr()`方法，与修饰实例方法不同的是：**实例方法其锁对象是当前实例对象（`this`对象），而静态方法的锁对象是当前类的`class`对象**，这样就算`new`出多个实例对象，也不会在多线程同时执行`incr()`方法出现线程安全问题，示意图如下：

![多线程执行图-3]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/8d96ecfd2c14478ebdea9824b043959btplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

注意代码中的`reduce()`方法是普通实例方法，其对象锁是当前实例对象，如果别的线程调用该方法，将不会产生互斥现象，毕竟锁对象不同。我们应该意识到这种情况下，可能会发生线程安全问题，毕竟`reduce()`方法也操作了共享变量`i`。

> PS：无论`synchronized`是修饰对象实例方法，还是修饰静态成员方法，使用的锁都是`this`锁类型的，只不过在修饰对象实例方法时，这个`this`指的是当前`new`出来的对象，因为对象实例方法是属于当前对象的。
>  当`synchronized`是修饰静态成员方法时，这个`this`指的是`class`对象，因为静态成员不属于任何一个实例对象，是类成员（这里可能有点抽象难以理解，但是只要记住，`synchronized`修饰在方法上时，用的就是`this`对象作为锁对象）。

#### 1.2.3、synchronized修饰代码块

除了使用`synchronized`关键字修饰实例方法、静态方法外，还可以用它修饰代码块。毕竟在某些情况下，编写的方法体可能比较大，比如`2000`行代码的方法，如果直接使用`synchronized`关键字修饰这个方法，那么该方法执行的过程会比较耗时，而这`2000`行代码中，也并非所有的代码都会发生线程安全问题。

假设`2000`行代码中还存在一些比较耗时的操作（如`IO`操作），这种情况直接对整个方法进行同步操作，那必然会导致大量的线程阻塞，最终得不偿失。这时，我们可以使用同步代码块的方式，对需要保障线程安全的代码进行包裹，这样就无需对整个方法用`synchronized`关键字修饰了，代码示例如下：

```java
java复制代码public class SyncIncrDemo implements Runnable{
    //共享资源(临界资源)
    static int i = 0;

    //synchronized关键字修饰代码块
    public void methodA(){
        //省略一千行代码....
        
        /** 
        * 假设我们此时只有这里存在对共享资源操作，我们如果对整个方法进行同步
        * 那么是不应该的，而我们可以使用同步这段代码的形式使用`synchronized`
        * 关键字对它进行同步修饰
        */
        synchronized(SyncIncrDemo.class){
            i++;
        }
        
        // 省略八百行代码....
    }
    @Override
    public void run() {
            methodA();
    }
    
    public static void main(String[] args) throws InterruptedException {
        SyncIncrDemo syncIncrDemo = new SyncIncrDemo();
        for(int j=0;j<1000;j++){
            new Thread(syncIncrDemo).start();
        }
        Thread.sleep(10000);
        System.out.println(i);
    }
    /**
     * 输出结果:
     * 1000
     */
}
```

从上述代码可以看出，我们使用`synchronized`修饰代码块时，将`class`类对象做为锁资源（即锁对象），每次当线程进入`synchronized`包裹的代码块时，就会要求当前线程持有`SyncIncrDemo.class`类对象锁。如果当前有其他线程正持有该锁，那么新到的线程就必须阻塞等待，这样也就保证了同时只会有一个线程执行`i++`操作。

当然，除了类对象作为锁资源外，我们还可以使用`this`对象（代表当前实例），或者给予一个对象作为锁对象，如下代码：

```java
java复制代码// 当前实例
synchronized(this){
    i++;
}

// 给予对象
Object obj = new Object();
synchronized(obj){
    i++;
}
```

到这里，关于`synchronized`的基本描述与使用就告一段落了，接下来需要研究的是：**`synchronized`关键字底层的实现原理**，从而进一步加深对于`synchronized`的理解。

## 二、Synchronized底层原理剖析

前面提到`synchronized`是依赖于对象实现的锁功能（对象头以及`Monitor`），而从官方的虚拟机规范文档上，能看到关于同步的描述是这样的：

> `Java`虚拟机中的同步(`Synchronization`)基于进入和退出管程(`Monitor`)对象实现。

可以看到`Java`中的`synchronized`同步，的确是基于`Monitor`（管程）对象来实现的。

- 获取锁：进入管程对象（显式型：`monitorenter`指令）
- 释放锁：退出管程对象（显式型：`monitorexit`指令）

不过要明白一点，当我们使用`synchronized`修饰方法时，无法通过`javap`看到进入/退出管程对象的指令。因为当`synchronized`修饰方法时，**是通过调用指令，读取运行时常量池中方法的`ACC_SYNCHRONIZED`标志来实现的**，`synchronized`修饰方法时使用的隐式同步。

不过无论是显式同步，还是隐式同步，都是依靠进入/退出管程对象来实现的同步（关于显式和隐式稍后会分析），不过值得一提的是：在`Java`中关于同步的概念，并不仅仅在`synchronized`中体现，`synchronized`只是同步的一种实现，它并不能完全代表`Java`的同步机制。

### 2.1、理解Java对象内存布局

在`JVM`中，一个`Java`对象在内存的布局，会分为三个区域：对象头、实例数据以及对齐填充：

![Java对象内存布局]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/7f8de26a025541d69f22a2be84c61e1atplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

**①对象头**：存储`MarkWord`和类型指针（`ClassMetadataAddress/KlassWord`）；如果是数组对象，还会存在数组长度（`ArrayLength`）。

**②实例数据**：存放当前对象属性成员信息，以及父类属性成员信息，比如：

```java
java复制代码public class A {
    private int x;
    private int y;
    private long z;
}
```

这个类存在两个`int`和一个`long`类型的属性，那么就是`4 + 4 + 8 = 16byte`大小。

**③对齐填充**：由于虚拟机要求对象起始地址必须是`8byte`的整数倍，所以虚拟机会对于每个对象做`8`的倍数填充，如果这个对象的大小（对象头+实例数据大小）已经是`8`的整数倍了，则不会出现对齐填充。

> 为此，对齐填充并不是每个对象都有，这部分仅仅是为了字节对齐，避免减少堆内存的碎片空间和方便`OS`读取。

关于`Java`对象头则是`synchronized`底层实现的关键要素，下面我们重点分析对象头的构成，  `JVM`采取两个字宽（`Word/Class`指针大小）存储对象头。

如果该对象是数组，额外需要存储数组长度，所以`32`位虚拟机采取`3`个字宽存储对象头，而`64`位虚拟机采取两个半字宽存储对象头，而在`32`位虚拟机中，一个字宽的大小为`4byte/32bit`；`64`位虚拟机下，一个字宽大小为`8byte/64bit`，`64`位开启指针压缩的情况下，`MarkWord`为`8byte`，`KlassWord`为`4byte`。

而关于对象头内的具体内容，很多资料都含糊不清，我在这里例出如下信息（如有任何疑问欢迎留言），先给出`32`位虚拟机下的对象头结构信息：

| 虚拟机位数 | 对象头结构信息                   | 说明                                                         | 大小          |
| ---------- | -------------------------------- | ------------------------------------------------------------ | ------------- |
| `32`位     | `MarkWord`                       | `HashCode`、分代年龄、是否偏向锁和锁标记位                   | `4byte/32bit` |
| `32`位     | `ClassMetadataAddress/KlassWord` | 类型指针指向对象的类元数据，JVM通过这个指针确定该对象是哪个类的实例 | `4byte/32bit` |
| `32`位     | `ArrayLenght`                    | 如果是数组对象存储数组长度，非数组对象不存在                 | `4byte/32bit` |

再来看看`64`位虚拟机下的对象头结构：

| 虚拟机位数 | 对象头结构信息                   | 说明                                                         | 大小          |                                     |
| ---------- | -------------------------------- | ------------------------------------------------------------ | ------------- | ----------------------------------- |
| `64`位     | `MarkWord`                       | `unused、HashCode`、分代年龄、是否偏向锁和锁标记位           | `8byte/64bit` |                                     |
| `64`位     | `ClassMetadataAddress/KlassWord` | 类型指针指向对象的类元数据，JVM通过这个指针确定该对象是哪个类的实例 | `8byte/64bit` | 开启指针压缩的情况下为`4byte/32bit` |
| `64`位     | `ArrayLenght`                    | 如果是数组对象存储数组长度，非数组对象不存在                 | `4byte/32bit` |                                     |

其中`32`位虚拟机中，对象头内的`MarkWord`，在默认情况下，存储着对象的`HashCode`、分代年龄、是否偏向锁、锁标记位等信息。而`64`位虚拟机中，对象头内的`MarkWord`，默认存储着`HashCode`、分代年龄、是否偏向锁、锁标记位、`unused`，如下：

| 虚拟机位数 | 锁状态         | HashCode | 分代年龄 | 是否偏向锁 | 锁标志信息 |
| ---------- | -------------- | -------- | -------- | ---------- | ---------- |
| `32`位     | 无锁态（默认） | `25bit`  | `4bit`   | `1bit`     | `2bit`     |

| 虚拟机位数 | 锁状态         | HashCode | 分代年龄 | 是否偏向锁 | 锁标志信息 | unused  |
| ---------- | -------------- | -------- | -------- | ---------- | ---------- | ------- |
| `64`位     | 无锁态（默认） | `31bit`  | `4bit`   | `1bit`     | `2bit`     | `26bit` |

由于对象头的信息，与对象自身定义的成员属性数据没有关系，对象头属于额外的存储成本。考虑到`JVM`的空间效率，`MarkWord`被设计成为一个非固定的数据结构，为了方便存储更多有效的数据，它会根据对象本身的状态，复用自己的存储空间，除了上述列出的`MarkWord`默认存储结构外，还有如下可能变化的结构（前面32位，后面64位）：

![32位虚拟机markword变化信息]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/7e39fb4ed51242919306035e50ace994tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

![64位虚拟机markword变化信息]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/ec11b555844244c8ada05789267f498btplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

从上图中可以看到，当对象状态为偏向锁时，`MarkWord`存储的是偏向的线程`ID`。

当状态为轻量级锁时，`MarkWord`存储的是指向线程栈中`LockRecord`的指针，`LockRecord`是什么呢？由于`MarkWord`的空间有限，随着对象状态的改变，原本存储在对象头里的一些信息，如`HashCode`、对象年龄等，就没有足够的空间存储。这时为了保证这些数据不丢失，就会拷贝一份原本的`MarkWord`放到线程栈中，这个拷贝过去的`MarkWord`叫作`Displaced Mark Word`，同时会配合一根指向对象的指针，形成`LockRecord`（锁记录），而原本对象头中的`MarkWord`，就只会存储一根指向`LockRecord`的指针。

下面再来对`MarkWord`的信息稍作解释（后续会用到）：

- `unused`：未使用的空间；
- `identity_hashcode`：对象最原始的`hashcode`，就算重写`hashcode()`也不会改变；
- `age`：对象的`GC`年龄；
- `biased_lock`：是否偏向锁的标识；
- `lock`：锁标记位；
- `ThreadID`：持有偏向锁的线程`ID`；
- `epoch`：偏向锁时间戳；
- `ptr_to_lock_record`：指向线程本地栈中`lock_record`的指针；
- `ptr_to_heavyweight_monitor`：指向堆中`monitor`对象的指针。

在这里我们提到了轻量级锁和偏向锁，这是`JDK1.6`对`synchronized`优化后新增加的，稍后我们会简要分析。

这里我们主要先分析一下重量级锁，也就是通常说的`synchronized`对象锁，锁标识位为`10`，其中指针指向的是`monitor`对象（也称为管程或监视器锁）的起始地址。每个`Java`对象都存在着一个`monitor`对象与之关联。对象与其`monitor`之间的关系，有存在多种实现方式，如`monitor`可以与对象一起创建销毁，或当线程试图获取对象锁时自动生成，但当一个`monitor`被某个线程持有后，它便处于锁定状态。

在`HotSpot`虚拟机中，`monitor`是由`ObjectMonitor`实现的，其主要数据结构如下（位于`HotSpot`源码的`ObjectMonitor.hpp`文件中）：

```ini
ini复制代码位置：openjdk\hotspot\src\share\vm\runtime\objectMonitor.hpp
实现：C/C++
代码：

ObjectMonitor() {
    _header       = NULL; //markOop对象头
    _count        = 0; //记录个数
    _waiters      = 0, //等待线程数
    _recursions   = 0; //重入次数
    _object       = NULL; //监视器锁寄生的对象。锁不是平白出现的，而是寄托存储于对象中。
    _owner        = NULL;  //指向获得ObjectMonitor对象的线程或基础锁
    _WaitSet      = NULL; //处于wait状态的线程，会被加入到_WaitSet
    _WaitSetLock  = 0 ; 
    _Responsible  = NULL;
    _succ         = NULL;
    _cxq          = NULL;
    FreeNext      = NULL;
    _EntryList    = NULL; //处于等待锁block状态的线程，会被加入到该列表
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ; // _owner is (Thread *) vs SP/BasicLock
    _previous_owner_tid = 0; // 监视器前一个拥有者线程的ID
}
```

`Monitor`存在于堆中，什么是`Monitor`？我们可以把它理解为一个同步工具，也可以描述为一种同步机制，但是它的本质就是一个特殊的对象。

万物皆对象，而`Java`的所有对象都是天生的`Monitor`，每一个`Java`对象都有成为`Monitor`的潜质。因为在`Java`的设计中，每一个`Java`对象自打娘胎里出来就带了一把看不见的锁，它叫做内部锁或者`Monitor`锁。

`Monitor`是线程私有的数据结构，每一个线程都有一个可用`monitor record`列表，同时还有一个全局的可用列表。每一个被锁住的对象，都会和一个`monitor`关联（对象头的`MarkWord`中的`LockWord`，指向`monitor`的起始地址），同时`monitor`中有一个`Owner`字段，存放拥有该锁的线程唯一标识，表示该锁被这个线程占用，`Monitor`内部结构如下：

![Monitor对象结构]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/a1db025869c04fc7b86864d2c380d8betplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

- `Contention List`：竞争队列，所有请求锁的线程，首先被放在这个竞争队列中（后续`1.8`版本中的`_cxq`）。
- `Entry List`：`Contention List`中那些有资格成为候选资源的线程被移动到`Entry List`中。
- `Wait Set`：调用`Object.wait()`方法后，被阻塞的线程被放置在这里。
- `OnDeck`：任意时刻，最多只有一个线程正在竞争锁资源，该线程被称为`OnDeck`。
- `Owner`：初始时为`NULL`，表示当前没有任何线程拥有该`monitor record`，当线程成功拥有该锁后，保存线程唯一标识，当锁被释放时，又设置为`NULL`，当前已经获取到所资源的线程被称为`Owner`。
- `!Owner`：当前释放锁的线程。
- `RcThis`：表示`blocked`阻塞或`waiting`等待在该`monitor record`上的线程个数。
- `Nest`：用来实现重入锁的计数。
- `Candidate`：用来避免不必要的阻塞或等待线程唤醒，因为每一次只有一个线程能够成功拥有锁，如果每次前一个释放锁的线程，唤醒所有正在阻塞或等待的线程，会引起不必要的上下文切换（从阻塞到就绪，然后因为竞争锁失败又被阻塞），从而导致性能严重下降。`Candidate`只有两种可能的值，`0`表示没有需要唤醒的线程；`1`表示要唤醒一个继任线程来竞争锁。
- `HashCode`：保存从对象头拷贝过来的`HashCode`值（可能还包含`GC age`）。
- `EntryQ`：关联一个系统互斥锁（`semaphore`），阻塞所有试图锁住`monitor record`失败的线程。

`ObjectMonitor`中有两个队列，`_WaitSet`和`_EntryList`，用来保存`ObjectWaiter`对象列表（ 每个等待锁的线程都会被封装成`ObjectWaiter`对象），`_owner`指向持有`ObjectMonitor`对象的线程，当多个线程同时访问一段同步代码时，首先会加入`_EntryList`集合，当线程获取到对象的`monitor`后进入`_Owner`区域，并把`monitor`中的`owner`变量设置为当前线程，同时`monitor`中的计数器`count+1`。

若线程调用`Object.wait()`方法，将释放当前持有的`monitor`，`owner`变量恢复为`null`，`count`自减`1`，同时该线程进入`WaitSet`集合中等待被唤醒。若当前线程执行完毕，也将释放`monitor`(锁)并复位变量的值，以便其他线程进入获取`monitor`。如下图所示：

![状态转变]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/1e158c107cfa477a98cf67da04db403ctplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

由此看来，`monitor`对象存在于堆空间内，每个`Java`对象的对象头，其中`markword`存放指向`Monitor`对象的指针，`synchronized`关键字便是通过这种方式获取锁的，这也是为什么`Java`中任意对象可以作为锁的原因。

同时也是`notify/notifyAll/wait`等方法，存在于顶级对象`Object`中的原因（这点稍后会进一步分析），有了上述知识基础后，下面我们将进一步分析`synchronized`在字节码层面的具体语义实现。

### 2.2、从反编译字节码理解synchronized修饰代码块的原理

先来看看编译前的`Java`源文件：

```typescript
typescript复制代码public class SyncDemo{
    int i;
    public void incr(){
        synchronized(this){
            i++;
        }
    }
}
```

使用`javac`编译如上代码，并使用`javap -p -v -c`进行反汇编，会得到如下字节码：

```c
c复制代码Classfile /C:/Users/XYSM/Desktop/com/SyncDemo.class
  Last modified 2020-6-17; size 454 bytes
  MD5 checksum 457e08e7b9caa345db5c5cca53d8d612
  Compiled from "SyncDemo.java"
public class com.SyncDemo
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   ...... //省略常量池信息
{
  int i;
    descriptor: I
    flags:
  
  // 构造函数
  public com.SyncDemo();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
  
  /*-------synchronized修饰incr()中代码块，反汇编之后得到的字节码文件--------*/
  public void incr();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=3, locals=3, args_size=1
         0: aload_0
         1: dup
         2: astore_1
         3: monitorenter        // monitorenter进入同步
         4: aload_0
         5: dup
         6: getfield      #2                  // Field i:I
         9: iconst_1
        10: iadd
        11: putfield      #2                  // Field i:I
        14: aload_1
        15: monitorexit         // monitorexit退出同步
        16: goto          24
        19: astore_2
        20: aload_1
        21: monitorexit         // 第二次出现monitorexit退出同步
        22: aload_2
        23: athrow
        24: return
      Exception table:
         // 省略其他字节码信息........


}
SourceFile: "SyncDemo.java"
```

跟`synchronized`有关的指令，只需关注如下字节码：

```arduino
arduino复制代码 3: monitorenter        // monitorenter进入同步
 15: monitorexit         // monitorexit退出同步
 21: monitorexit         // 第二次出现monitorexit退出同步
```

从字节码中可知，`synchronized`修饰代码块，是基于进入管程`monitorenter`和退出管程`monitorexit`指令实现的，其中`monitorenter`指令指向同步代码块的开始位置，`monitorexit`指令则指明同步代码块的结束位置。

当执行`monitorenter`指令时，当前线程将试图获取`objectref`（即对象锁）所对应的`monitor`的持有权，当`objectref`的`monitor`计数器为`0`，那线程可以尝试占有`monitor`，如果将计数器值成功设置为`1`，表示获取锁成功，伪代码如下：

```java
java复制代码// monitorenter指令伪代码：
if(count == 0){
    count = count + 1;
    获取锁成功！
} else{
    当前锁资源已被其他线程持有，进入阻塞！
}

// monitorexit指令伪代码：
count = 0;
```

但值得注意的是：如果当前线程已经拥有`objectref`的`monitor`的持有权，那它可以重入这个 `monitor`（关于重入性稍后会分析），重入时计数器的值也会`+1`。

倘若其他线程已经拥有`objectref`的`monitor`的所有权，那当前线程将被阻塞，直到持有的线程执行完毕，即`monitorexit`指令被执行，前一个线程将释放`monitor`(锁)，并设置计数器值为`0`，其他线程将有机会持有`monitor`。

同时，`JVM`将会确保无论方法通过何种方式结束，方法中调用过的每条`monitorenter`指令获取锁，都有执行其对应`monitorexit`指令释放锁。说人话就是：无论这个方法是正常结束，还是异常结束，都会保证线程释放锁。这也是为什么大家在上述字节码文件中，能看到两个`monitorexit`指令的原因。

为了保证在方法异常结束时，`monitorenter`和`monitorexit`指令依然可以正确配对执行，编译器会自动产生一个异常处理器，这个异常处理器可处理所有的异常，它的目的就是用来执行`monitorexit`指令释放锁。

从字节码中看到的第二个`monitorexit`指令，它就是异常结束时，会被执行的释放`monitor`指令，确保在方法执行过程中，由于异常导致的方法意外结束时，不出现死锁现象。

### 2.3、反编译字节码理解synchronized修饰方法原理

方法级的同步是隐式锁，即无需通过字节码指令来控制的，获取锁、释放锁的实现位置，分别位于方法调用和返回操作时。`JVM`可以从方法常量池中的`method_info Structure`方法表结构中，靠`ACC_SYNCHRONIZED`访问标志来区分一个方法是否为同步方法。

当方法调用时，调用指令时将会检查方法的`ACC_SYNCHRONIZED`访问标志是否被设置，如果设置了，线程执行前，将需要先持有`monitor`（虚拟机规范中用的是管程一词），然后再执行方法，最后再方法结束时释放`monitor`。

在方法执行期间，执行线程持有了`monitor`，其他任何线程都无法再获得同一个`monitor`。如果一个同步方法执行期间抛出了异常，并且在方法内部无法处理此异常，那这个同步方法所持有的`monitor`，将在异常抛到同步方法之外时自动释放。

下面我们看看字节码层面如何实现，编译前`Java`源文件：

```arduino
arduino复制代码public class SyncDemo{
    int i;
    public synchronized void reduce(){
        i++;
    }
}
```

同样先用`javac`编译，再用`javap -p -v -c`得到反汇编后的字节码：

```yaml
yaml复制代码Classfile /C:/Users/XYSM/Desktop/com/SyncDemo.class
  Last modified 2020-6-17; size 454 bytes
  MD5 checksum 457e08e7b9caa345db5c5cca53d8d612
  Compiled from "SyncDemo.java"
public class com.SyncDemo
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   ...... //省略常量池信息
{
  int i;
    descriptor: I
    flags:
  
  // 构造函数
  public com.SyncDemo();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
        
  // synchronized修饰方法        
  public synchronized void reduce();
    descriptor: ()V
    flags: ACC_PUBLIC, ACC_SYNCHRONIZED
    Code:
      stack=3, locals=1, args_size=1
         0: aload_0
         1: dup
         2: getfield      #2                  // Field i:I
         5: iconst_1
         6: iadd
         7: putfield      #2                  // Field i:I
        10: return
      LineNumberTable:
        line 11: 0
        line 12: 10
    
    // 省略其他字节码信息........
}
SourceFile: "SyncDemo.java"
```

从字节码中可以看出，`synchronized`修饰的方法，并没有出现`monitorenter`指令和`monitorexit`指令，取得代之的是：**在`flags: ACC_PUBLIC`之后增加了一个`ACC_SYNCHRONIZED`标识**。这个标识指明了当前方法是一个同步方法，`JVM`通过这个`ACC_SYNCHRONIZED`访问标志，来辨别一个方法是否为同步方法，从而执行相应的同步调用。这便是`synchronized`修饰在方法上的实现原理。

同时，大家还得明白，在`Java`早期版本中，`synchronized`属于重量级锁，效率低下，因为`monitor`监视器锁，依赖于底层操作系统的`Mutex Lock`来实现，而操作系统实现线程之间的切换时，需要从用户态转换到内核态，这个切态过程需要较长的时间，并且更方面成本较高，这也是早期的`synchronized`性能效率低的原因。

不过值得庆幸的是在`Java6`之后，`Java`官方从`JVM`层面对`synchronized`进行了优化，所以现在的`synchronized`锁，效率也十分不错了。`Java6`之后，为了减少获得锁、释放锁带来的性能消耗，引入了轻量级锁和偏向锁，下面简单了解一下官方对`synchronized`锁的优化。

## 三、Java6对于synchronized的优化：锁膨胀

`JDK1.6`之后，`synchronized`锁的状态总共有四种：**无锁状态、偏向锁、轻量级锁和重量级锁**。随着线程的竞争，锁可以从偏向锁升级到轻量级锁，再升级的重量级锁，但是锁的升级一般是单向的，也就是说只能从低到高升级，通常不会出现锁的降级。

> 但是有个细节值得的注意，不会出现锁降级，只是针对用户线程而言，对于重量级锁还是会出现锁降级的情况，降级发生于`STW`阶段，降级对象就是那些仅仅能被`VMThread`访问，而没有其他`JavaThread`访问的`Monitor`对象（具体参考：[重量级锁降级](https://link.juejin.cn?target=https%3A%2F%2Fzhuanlan.zhihu.com%2Fp%2F28505703) ）。

关于重量级锁，前面我们已详细分析过，下面我们将介绍偏向锁和轻量级锁，以及`JVM`的其他优化手段，不过毕竟涉及到具体过程比较繁琐，如需了解详细过程可以查阅《深入理解Java虚拟机原理》，在此并不会对锁升级进行细节性的分析，而是阶段性的总结。

### 3.1、无锁态

当我们在Java程序中`new`一个对象时，会默认启动匿名偏向锁，但是值得注意的是有个小细节，**偏向锁的启动有个延时，默认是`4`秒**，也就是：`JVM`启动四秒之后才会开启匿名偏向锁，在`JVM`启动的前四秒内，`new`的对象不会启动匿名偏向锁，why？

因为`JVM`虚拟机自己有一些默认启动的线程，里面有好多`sync`代码，这些`sync`代码启动时，就知道肯定会有竞争，如果使用偏向锁，就会造成偏向锁不断的进行锁撤销和锁升级的操作，效率较低。

还有一点值得注意，对于一个对象而言，就算启动了匿名偏向锁，这个对象的脑袋里，也没有任何的线程`ID`。因为是新创建的对象，所以对于一个新`new`对象而言，不管有没有启动匿名偏向锁，都被称为概念上的无锁态对象。

毕竟就算启动了匿名偏向锁，但是在没有成为真正的偏向锁之前，`markword`信息中的`threadID`是空的，因为此时没有线程获取该锁（但是当对象成为匿名偏向锁时，`mrakword`中的锁标志位仍然会改为`101`，偏向锁的标志）。

### 3.2、偏向锁

偏向锁是`Java6`之后加入的新锁，它是一种针对加锁操作的优化手段。

经过官方研究发现，在大多数情况下，锁不仅不存在多线程竞争，而且总是由同一线程多次获得，为了减少同一线程获取锁的代价，如`CAS`操作带来的耗时等，从而引入了偏向锁。

偏向锁的核心思想是：如果一个线程获得了锁，那么锁就进入偏向模式，此时`Mark Word`的结构也变为偏向锁结构，当这个线程再次请求锁时，无需再做任何同步操作，即不需要再走获取锁的流程，这样就省去了大量有关锁申请的操作，从而也就提高程序的性能。

换句通俗易懂的话说：偏向锁其中的“偏”是偏心的偏，就是这个锁会偏向于第一个获得它的线程，在接下来的执行过程中，假如该锁没有被其他线程所持有，也没有其他线程来竞争该锁，那么持有偏向锁的线程，将永远不需要进行获取锁操作。在此线程之后的执行过程中，如果再次进入或者退出同一段同步块代码，并不需要再做加锁或者解锁动作，而是会做以下操作：

- `Load-and-test`，就是简单判断一下当前线程`id`是否与`Markword`中的线程`id`是否一致；
- 如果一致，则说明此线程持有的偏向锁，没有被其他线程覆盖，直接执行下面的代码；
- 如果不一致，则要检查一下对象是否还属于可偏向状态，即检查“是否偏向锁”标志位；
- 如果还未偏向，则利用`CAS`操作来竞争锁，再次将`ID`放进去，即重复第一次获取锁的动作。

但是当第二个线程来尝试获取锁时，如果此对象已经偏向了，并且不是偏向自己，则说明出现了竞争。此时会根据该锁的竞争情况，可能会产生偏向撤销，重新偏向的现象。

但大部分情况下，就是直接膨胀成轻量级锁了。所以，对于没有锁竞争的场合，偏向锁有很好的优化效果，毕竟极有可能连续多次是同一个线程申请相同的锁。但是对于锁竞争比较激烈的场合，偏向锁就失效了，毕竟这样场合，极有可能每次申请锁的线程都不相同，为此这种场合下，可以通过`JVM`参数关闭偏向锁，否则会得不偿失。

#### 3.2.1、偏向锁撤销过程

- 在一个安全点停下拥有锁的线程；
- 遍历线程栈，如果存在锁记录的话，需要修复锁记录和Markword，使其变成无锁状态；
- 唤醒当前线程，将当前锁升级成轻量级锁。

所以，如果程序中，大部分同步代码块，在大多数情况下，都会出现两个及以上的线程竞争，此时偏向锁就会是一种累赘，对于这种情况，我们可以一开始就通过`XX:-UseBiasedLocking`把偏向锁关闭，从而做到性能上的优化。

#### 3.2.2、偏向锁膨胀过程

当第一个线程进入时，发现是匿名偏向状态，这时会通过`cas`操作，把自己的`threadId`设置到`MarkWord`中，如果替换成功，则证明成功拿到偏向锁，失败则锁膨胀。

当线程第二次进入同步块时，经过一些比较之后，如果发现自己的线程`id`，和对象头中的偏向线程`id`一致，在当前线程栈的`lock record`中添加一个空的`Displaced Mark Word`，由于操作的是私有线程栈，所以不需要`cas`操作，`synchronized`带来的开销基本可以忽略。

当其他线程进入同步块时，发现偏向线程不是自己，则进入偏向锁撤销的逻辑。当达到全局安全点时，如果发现偏向线程挂了，那就把偏向锁撤销，并将对象头内的`MarkWord`修复为无锁状态，自己尝试获取偏向锁（这个过程被称为重新偏向）。

可如果原本的偏向线程还存活，重新偏向失败后，锁开始膨胀为轻量级锁，原来的线程仍然持有锁，下面我们接着了解轻量级锁。

### 3.3、轻量级锁

倘若偏向锁失败，`Synchronized`并不会立即升级为重量级锁，它会先进入轻量级锁状态，此时`MarkWord`的结构也变为轻量级锁的结构。轻量级锁能提升程序性能的依据是：“对于绝大部分的锁，在整个同步周期内都不存在竞争”，注意这是经验数据。

#### 3.3.1、轻量级锁膨胀过程

当膨胀为轻量级锁时，首先根据`markwork`判断是否有线程持有锁，如果有，则在当前线程栈中创建一个`lock record`复制`mark word`，并且通过`cas`机制，把当前线程栈的`lock record`地址，放到对象头中。

> 细节：之前持有偏向锁的线程，会优先进行`cas`，尝试设置`mrakword`中的锁信息指针。

如果成功，则说明获取到轻量级锁；如果失败，则说明锁已经被其他持有了，此时记录线程的重入次数(把`lock record`的`markword`设置为`null`)，此时线程会自旋（自适应自旋），确保在竞争不激烈的情况下，仍然可以不膨胀为真正意义上的“内核态重量级锁”，从而减少消耗。

如果自旋后还未等到锁，则说明目前竞争较重，需要膨胀为重量级的锁，代码如下：

```c
c复制代码void ObjectSynchronizer::slow_enter(Handle obj, BasicLock* lock, TRAPS) {
  markOop mark = obj->mark();
  assert(!mark->has_bias_pattern(), "should not see bias pattern here");
  // 如果是无锁状态
  if (mark->is_neutral()) {
    //设置Displaced Mark Word并替换对象头的mark word
    lock->set_displaced_header(mark);
    if (mark == (markOop) Atomic::cmpxchg_ptr(lock, obj()->mark_addr(), mark)) {
      TEVENT (slow_enter: release stacklock) ;
      return ;
    }
  } else
  if (mark->has_locker() && THREAD->is_lock_owned((address)mark->locker())) {
    assert(lock != mark->locker(), "must not re-lock the same lock");
    assert(lock != (BasicLock*)obj->mark(), "don't relock with same BasicLock");
    // 如果是重入，则设置Displaced Mark Word为null
    lock->set_displaced_header(NULL);
    return;
  }

  ...
  // 走到这一步说明已经是存在多个线程竞争锁了 需要膨胀为重量级锁
  lock->set_displaced_header(markOopDesc::unused_mark());
  ObjectSynchronizer::inflate(THREAD, obj())->enter(THREAD);
}
```

不过需要了解的是，轻量级锁适用的场景是：**线程交替执行同步块的场合**，如果同一时间存在多个线程访问同一把锁，就会导致轻量级锁膨胀为重量级锁。但在`JDK1.4`之后，膨胀到重量级锁阶段后，最开始的重量级锁不会直接进入内核态级别的重量锁，而是会进入一个“自旋锁”阶段，后续被优化成了自适应自旋。

#### 3.3.2、轻量级锁小细节

轻量级锁主要有自旋、自适应自旋两种类型。

**①自旋锁**：所谓自旋，是指当有另外一个线程来竞争锁时，这个线程会在原地循环等待，而不是把该线程挂起阻塞，直到那个持有锁的线程释放锁之后，这个线程就可以马上尝试获取锁。

> 注意，线程在原地自旋时，会消耗`cpu`，就相当于在执行一个啥也没有的`for`循环。

所以，轻量级锁适用于那些同步代码块执行时长很短的场景，这样，线程原地等待很短的时间，就能够获得锁了。经验表明，大部分同步代码块执行的时间都特别短，也正是基于这个原因，才有了轻量级锁这么个东西。

不过自旋锁会存在一些问题，如下：

- 如果同步代码块执行的很慢，需要等待很长时间，这时其他线程自旋会消耗大量`CPU`；
- 本来前一个线程释放锁后，当前线程是能够拿到锁的，但假如这时有好几个线程都在自旋等待这把锁，那就有可能造成当前线程拿不到锁，还得继续原地空循环消耗`CPU`，甚至有可能一直获取不到锁；

基于这些问题，我们必须通过`-XX:PreBlockSpin`给线程空循环设置一个次数，当线程超过了这个次数，我们就认为，继续使用自旋锁就不适合了，此时锁会再次膨胀，升级为重量级锁。默认情况下，自旋的次数为`10`次，或者自旋线程超过`CPU`核数一半时，会发生锁膨胀（自旋锁是在`JDK1.4.2`时引入的）。

**②自适应自旋锁**：所谓自适应自旋锁，就是线程空循环的次数并非固定的，而是会动态根据实际情况来改变自旋等待的次数，其大概原理是这样的（在重量级锁阶段自旋）：

> 假如`T1`线程刚刚成功拿到锁，当它把锁释放后，`T2`线程获得该锁，并且`T2`在运行的过程中，此时`T1`又过来拿锁了，但`T2`还没有释放该锁，所以`T1`只能阻塞等待，但是虚拟机认为：**由于`T1`刚刚获得过该锁，那么虚拟机会觉得`T1`这次自旋，也很有可能再次成功拿到该锁，所以会延长`T1`自旋的次数。**

另外，如果对于某一个锁，一个线程自旋之后，很少成功获得该锁，那么以后这个线程要获取该锁时，是有可能直接跳过自旋过程，直接走重量级锁的逻辑，以免空循环等待浪费资源。

同时，当锁资源的竞争已经非常激烈后，自适应自旋存在的意义已经没有必要了，因为存在大量线程竞争同一把锁，就算自旋一段时间，其他线程还需要继续自旋等待，此时自旋带来的开销，已经大于在内核态挂起线程的开销了。所以，在竞争很激烈的情况下，自适应自旋的次数可能会为`0`，也就是不再尝试自旋，而是直接膨胀为真正意义上的“内核态重量级锁”。

### 3.4、重量级锁

关于重量级锁，在前面已经详细分析过了，重量级锁就是传统意义的互斥锁了，当出现较大竞争、锁膨胀为重量级锁时，对象头的`markword`指向堆中的`monitor`，此时会将线程封装为一个`ObjectWaiter`对象，并插入到`monitor`的`_cxq`队列中，然后挂起当前线程。

当持有锁的线程释放后，会把`_cxq`里面的所有线程（`ObjectWaiter`对象），转移到`EntryList`中去，并且会从`EntryList`中挑选一个线程唤醒，被选中的线程叫做`Heir Presumptive`假定继承人（应该是这样翻译），就是图中的`Ready Thread`，假定继承人被唤醒后会尝试获得锁，但`synchronized`是非公平锁，所以假定继承人不一定能获得锁（这也是它叫"假定"继承人的原因）：

![Monitor对象结构]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/781dbcbab79f4ef1b3ee207ef296be6ftplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

如果线程获得锁后，调用`Object.wait()`方法，则会将线程加入到`WaitSet`中，当被`Object.notify()`唤醒后，会将线程从`WaitSet`移动到`_cxq`或`EntryList`中去。

> 需要注意：当调用一个锁对象的`wait、notify`方法时，如当前锁的状态是偏向锁或轻量级锁，则会先膨胀成重量级锁，因为`wait、notify`方法要依赖于`Monitor`对象实现。

### 3.5、锁状态总结

- 无锁态：`JVM`启动后四秒内的普通对象，和四秒后的匿名偏向锁对象
- 偏向锁状态：只有一个线程进入临界区
- 轻量级锁状态：多个线程交替进入临界区
- 重量级锁：多个线程同时进入临界区

下面来张图，总结一下锁膨胀/升级的过程：

![锁膨胀过程]((二)彻底理解Java并发编程之Synchronized关键字实现原理剖析.assets/6a836bce3a0c44aeb97fa3caf1a244datplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

### 3.6、Object对象四种锁状态分析

```csharp
csharp复制代码public class ObjectHead {
    public static void main(String[] args) throws InterruptedException {
        /** 
        无锁态：虚拟机刚启动时 new 出来的对象处于无锁状态
        **/
        Object obj = new Object();
        // 查看对象内部信息
        System.out.println(ClassLayout.parseInstance(obj).toPrintable());


        /** 
        匿名偏向锁：休眠4S后再创建出来的对象处于匿名偏向锁状态
        PS：当一个线程在执行被synchronized关键字修饰的代码或方法时，如果看到该锁
        对象是处于匿名偏向锁状态的（标志位为偏向锁但是对象头中MrakWord内threadID
        为空），那么这个线程将会利用cas机制把自己的线程ID设置到mrakword中，此后
        如果没有其他线程来竞争该锁，那么这个线程再执行被需要获取该锁的代码将不需
        要经过任何获取锁和释放锁的过程。
        **/
        Thread.sleep(4000);
        Object obj1 = new Object();
        System.out.println(ClassLayout.parseInstance(obj1).toPrintable());

        /** 
        轻量级锁：对于真正的无锁态对象obj加锁之后的对象处于轻量级锁状态
        **/
        synchronized (obj) {
            // 查看对象内部信息
            System.out.println(ClassLayout.parseInstance(obj).toPrintable());
        }

        /** 
        重量级锁：调用wait方法之后锁对象直接膨胀为重量级锁状态
        **/
        new Thread(()->{
            try {
                obj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(1);
        synchronized (obj) {
            // 查看对象内部信息
            System.out.println(ClassLayout.parseInstance(obj).toPrintable());
        }
    }
}

输出结果：
java.lang.Object object internals:  锁标志位状态：001：真正意义上无锁状态
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           e5 01 00 20 (11100101 00000001 00000000 00100000) (536871397)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

java.lang.Object object internals:  锁标志位状态：101：匿名偏向锁状态
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 00 00 00 (00000101 00000000 00000000 00000000) (5)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           e5 01 00 20 (11100101 00000001 00000000 00100000) (536871397)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

java.lang.Object object internals:  锁标志位状态：000：轻量级锁状态
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           18 f5 41 01 (00011000 11110101 01000001 00000001) (21099800)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           e5 01 00 20 (11100101 00000001 00000000 00100000) (536871397)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

java.lang.Object object internals:  锁标志位状态：010：重量级锁状态
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           5a de db 17 (01011010 11011110 11011011 00010111) (400285274)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           e5 01 00 20 (11100101 00000001 00000000 00100000) (536871397)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

/**抛出异常原因：违法的监控状态异常。当某个线程试图等待一个自己并不拥有的对象（Obj）的监控器或者通知其他线程等待该对象（Obj）的监控器时，抛出该异常。**/
Exception in thread "Thread-0": java.lang.IllegalMonitorStateException
	at java.lang.Object.wait(Native Method)
	at java.lang.Object.wait(Object.java:502)
	at com.sixstar.springbootvolatilesynchronized.Synchronized.ObjectHead.lambda$main$0(ObjectHead.java:27)
	at java.lang.Thread.run(Thread.java:748)
```

## 四、Synchronized细节及其他特性分析

### 4.1、同步消除

同步消除是`JVM`另外一种对锁的优化机制，这种优化更彻底，`Java`虚拟机在编译代码时，通过会对运行上下文进行扫描，从而去除不可能存在共享资源竞争的锁，通过这种方式消除没有必要的锁，可以节省毫无意义的获取锁开销，如下：

```java
java复制代码// 情况一：
public void appendString(String s1, String s2) {
    /*
    StringBuffer是线程安全，由于sb只会在append方法中使用,不可能被其他线程引用
    因此sb属于不可能共享的资源,JVM会自动消除内部的锁
    */
    StringBuffer sb = new StringBuffer();
    sb.append(s1).append(s2);
}

// 情况二：
StringBuffer sb = new StringBuffer();
public synchronized void appendString(String s1, String s2) {
    /*
    StringBuffer是线程安全，由于sb是在appendString方法中使用,而appendString
    是被synchronized修饰的，是线程安全的，那么没有必要再这里获取两把锁
    因此JVM会自动消除内部的锁，有些小伙伴看到这里会疑惑，这不是锁重入吗？
    其实并不是，锁重入指的是同一个锁资源被线程多次获取时直接跳过获取锁逻辑，稍后会分析
    */
    sb.append(s1).append(s2);
}
```

`StringBuffer`的`append`是一个同步方法，但是在`appendString`方法中，`sb`属于一个局部变量，并且不会被其他线程所使用，因此`sb`不可能存在线程竞争的情景，为此`JVM`会自动将其锁消除。

### 4.2、Synchronized重入性

从互斥锁的设计上来说，当一个线程试图操作一个被其他线程持有锁的临界资源时，这时将会陷入阻塞状态。但当一个线程再次请求自己持有的锁，所保护的临界资源时，这种情况属于重入锁，重入请求将会成功。

在`java`中，`synchronized`是基于原子性的内部锁机制，它支持锁的重入性，因此在一个线程调用`synchronized`方法的同时，在其方法体内部调用该对象另一个`synchronized`方法，也就是说：一个线程得到一个对象锁后，再次请求该对象锁，这是是允许的，这就是`synchronized`的可重入性，如下：

```java
java复制代码public class SyncIncrDemo implements Runnable{
    //共享资源(临界资源)
    static int i = 0;

    //synchronized关键字修饰实例成员方法
    public synchronized void incr(){
        i++;
    }
    @Override
    public void run() {
        synchronized(this){
            for(int j=0;j<1000;j++){
                incr();
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        SyncIncrDemo syncIncrDemo = new SyncIncrDemo();
        Thread t1=new Thread(syncIncrDemo);
        Thread t2=new Thread(syncIncrDemo);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(i);
    }
}
```

上述代码中，创建了一个`SyncIncrDemo`实例，以及启动两个线程，线程启动后会去执行`run`方法，而在`run`方法内部使用了`synchronized`修饰代码块，并将`this`对象作为锁资源，那么线程必须先获取当前实例`syncIncrDemo`这把锁，才能执行`for`循环代码。

而当一个线程成功获取到锁时，会发现`for`循环内部调用了该类中，另外一个被`synchronized`修饰的成员实例方法`incr()`，这时难道要再去获取一次当前实例锁资源？我们在前面分析到，成员实例方法最终的锁对象，还是当前`this`实例对象，而当前线程已经拿到了`this`锁，所以并不需要再次获取锁。

此类情况就是重入锁最直接的体现，不过值得注意的是：`synchronized`是基于`Monitor`实现的，每次重入时`monitor`中的计数器仍然会`+1`。还有一个细节需要稍微留意，就是当当子类继承父类时，子类也是可以通过可重入锁调用父类的同步方法。

### 4.3、synchronized与线程等待/唤醒机制

所谓等待唤醒机制，本篇主要指的是`notify/notifyAll`和`wait`方法，在使用这三个方法时，必须处于`synchronized`代码块或者`synchronized`方法中，否则就会抛出`IllegalMonitorStateException`异常。

这是因为调用这几个方法前，必须拿到当前对象的监视器`monitor`对象，也就是说`notify、notifyAll、wait`方法依赖于`monitor`对象。在前面的分析中，我们知道`monitor`依靠对象头的`MarkWord`中的指针来寻址，而`synchronized`关键字决定着一个`Java`对象，会不会生成`monitor`对象。

这也就是为什么`notify、notifyAll、wait`方法，必须在`synchronized`代码块或者`synchronized`方法调用的原因。

```java
java复制代码Object obj = new Object();
synchronized (obj) {
   obj.wait();
   obj.notify();
   obj.notifyAll();         
 }
```

同时，与`sleep`方法不同的是：`wait`方法调用完成后，线程将被挂起，但`wait`方法将会释放当前持有的监视器锁(`monitor`)，直到有线程调用`notify/notifyAll`方法后才能继续执行，而`sleep`方法只让线程休眠并不释放锁（类似于`for(;;){}`死循环）。

不过`notify/notifyAll`方法调用后，并不会马上释放监视器锁，而是在相应的`monitorexit`指令执行结束后，才会自动释放锁。

### 4.4、synchronized与线程中断机制

#### 4.4.1、线程中断

关于`Java`线程对象调用`start()`方法后，如果想中止该线程可以调用`Thread.stop()`方法强制让该线程关闭，但遗憾的是`stop()`方法的使用是强制式停止的，因此会造成很严重的问题，在`JDK1.2`后被遗弃。

为此，在目前的`Java`版本中，并没有提供“强制性停止正在执行线程”的方法，取而代之的是协调式的方式，在目前的`Java`版本中，提供了如下三个有关线程中断的`API`：

```arduino
arduino复制代码//中断线程（实例方法）
public void Thread.interrupt();
//判断线程是否被中断（实例方法）
public boolean Thread.isInterrupted();
//判断是否被中断并清除当前中断状态（静态方法）
public static boolean Thread.interrupted();
```

当一个线程处于被阻塞状态，或者试图执行一个阻塞操作时，使用`Thread.interrupt()`方式可以中断该线程。注意：此时将会抛出一个`InterruptedException`的异常，同时中断状态将会被复位(由中断状态改为非中断状态)，如下代码将演示该过程：

```csharp
csharp复制代码public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread() {
        @Override
        public void run() {
            //while在try中，通过异常中断就可以退出run循环
            try {
                while (true) {
                    //当前线程处于阻塞状态，异常必须捕捉处理，无法往外抛出
                    TimeUnit.SECONDS.sleep(2);
                }
            } catch (InterruptedException e) {
                System.out.println("Interruted When Sleep");
                boolean interrupt = this.isInterrupted();
                //中断状态被复位
                System.out.println("interrupt:"+interrupt);
            }
        }
    };
    t1.start();
    TimeUnit.SECONDS.sleep(2);
    //中断处于阻塞状态的线程
    t1.interrupt();

    /**
     * 输出结果:
       Interruted When Sleep
       interrupt:false
     */
}
```

如上述代码所示，我们创建一个线程，并在线程中调用了`sleep`方法，从而使用线程进入阻塞状态。启动线程后，调用线程的`interrupt`方法中断阻塞异常，并抛出`InterruptedException`异常，此时中断状态也将被复位。

这里有些人可能会诧异，为什么不用`Thread.sleep(2000)`，而是用`TimeUnit.SECONDS.sleep(2)`？其实原因很简单，前者并没有明确的单位说明，而后者非常明确表达秒的单位，事实上后者的内部实现，最终还是调用了`Thread.sleep(2000)`，但为了编写的代码语义更清晰，建议使用`TimeUnit.SECONDS.sleep(2)`的方式（注意`TimeUnit`是个枚举类型）。

除了阻塞中断的情景，处于运行期且非阻塞的状态的线程，在这种情况下，直接调用`Thread.interrupt()`中断线程，是不会得到任响应的，如下代码，将无法中断非阻塞状态下的线程：

```csharp
csharp复制代码public static void main(String[] args) throws InterruptedException {
    Thread t1=new Thread(){
        @Override
        public void run(){
            while(true){
                System.out.println("未被中断");
            }
        }
    };
    t1.start();
    TimeUnit.SECONDS.sleep(2);
    t1.interrupt();

    /**
     * 输出结果(无限执行):
         未被中断
         未被中断
         未被中断
         ......
     */
}
```

虽然我们调用了`interrupt`方法，但线程`t1`并未被中断，因为目前`Java`中的线程中断，都是协调式的，在这里只是由`mian`线程向`t1`线程发送一个中断信号，但是`t1`线程还在执行，那么它并不会停止，所以对于处于非阻塞状态的线程，需要我们手动进行中断检测并结束程序，改进后代码如下：

```csharp
csharp复制代码public static void main(String[] args) throws InterruptedException {
    Thread t1=new Thread(){
        @Override
        public void run(){
            while(true){
                //判断当前线程是否被中断
                if (this.isInterrupted()){
                    System.out.println("线程中断");
                    break;
                }
            }

            System.out.println("已跳出循环,线程中断!");
        }
    };
    t1.start();
    TimeUnit.SECONDS.sleep(2);
    t1.interrupt();

    /**
     * 输出结果:
        线程中断
        已跳出循环,线程中断!
     */
}
```

是的，我们在代码中，使用了实例方法`isInterrupted`判断线程是否已被中断，如果被中断将跳出循环以此结束线程，注意非阻塞状态调用`interrupt()`并不会导致中断状态重置。

综合所述，可以简单总结一下中断两种情况，一种是当线程处于阻塞状态，或者试图执行一个阻塞操作时，我们可以使用实例方法`interrupt()`进行线程中断，执行中断操作后将会抛出`interruptException`异常（该异常必须捕捉无法向外抛出）并将中断状态复位。

另外一种是当线程处于运行状态时，我们也可调用实例方法`interrupt()`进行线程中断，但同时必须手动判断中断状态，并编写中断线程的代码（其实就是结束run方法体的代码）。有时我们在编码时可能需要兼顾以上两种情况，那么就可以如下编写：

```java
java复制代码public void run(){
    try {
    //判断当前线程是否已中断,注意interrupted方法是静态的，
    // 执行后会对中断状态进行复位
    while (!Thread.interrupted()) {
        TimeUnit.SECONDS.sleep(2);
    }
    } catch (InterruptedException e) {

    }
}
```

#### 4.4.2、synchronized与线程中断

事实上，线程的中断操作，对于正在等待获取`synchronized`锁对象的线程而言，并不起作用，也就是对于`synchronized`来说，如果一个线程在等待锁，那么结果只有两种，要么它获得这把锁继续执行，要么它就阻塞等待，即使调用中断线程的方法，也不会生效。演示代码如下：

```csharp
csharp复制代码public class SyncBlock implements Runnable{
    public synchronized void occupyLock() {
        System.out.println("Trying to call occupyLock()");
        while(true) // 从不释放锁
            Thread.yield();
    }

    /**
     * 在构造器中创建新线程并启动获取对象锁
     */
    public SyncBlock() {
        //该线程已持有当前实例锁
        new Thread() {
            public void run() {
                occupyLock(); // 当前线程获取锁
            }
        }.start();
    }
    public void run() {
        //中断判断
        while (true) {
            if (Thread.interrupted()) {
                System.out.println("中断线程!!");
                break;
            } else {
                occupyLock();
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        SyncBlock sync = new SyncBlock();
        Thread t = new Thread(sync);
        //启动后调用occupyLock()方法,无法获取当前实例锁处于等待状态
        t.start();
        TimeUnit.SECONDS.sleep(1);
        //中断线程,无法生效
        t.interrupt();
    }
}
```

我们在`SyncBlock`构造函数中，创建一个新线程并启动，然后调用`occupyLock()`获取到当前实例锁，由于`SyncBlock`自身也是线程，启动后在其`run`方法中，也调用了`occupyLock()`，但由于对象锁被其他线程占用，导致`t`线程只能等待锁，此时我们调用了`t.interrupt()`但并不能中断线程。

### 4.5、为什么synchronized不能禁止指令重排序？

开头我们说过一个结论：**`synchronized`能保证有序性，却不能禁止指令重排序**。

在阐述这个问题答案前，如果有小伙伴对于指令重排序、有序性、可见性，这些概念还不太清楚，那请先移步另外一篇文章：[玩命死磕Java内存模型（JMM）与Volatile关键字底层原理。](https://link.juejin.cn?target=https%3A%2F%2Fwww.jianshu.com%2Fp%2Fee8303ed0e6c)

实际上`synchronized`关键字所保证的原子性、可见性、有序性，实际上都是基于一个思路：**将之前的多线程并行执行，变为了单线程的串行执行。**

在`Java`程序中，倘若在本线程内，所有操作都视为有序行为。如果是多线程环境下，一个线程中观察另外一个线程，所有操作都是无序的，前半句指的是单线程内，保证串行语义执行的一致性，后半句则指指令重排现象，和工作内存与主内存同步延迟现象。

那实际对于单线程而言，所有操作都是有序的，因此`synchronized`将之前的多线程并行执行，变为了单线程的串行执行之后，必然可以保证“有序性”。而对于单线程而言，指令重排是对单线程的执行有利的，此时就没有必要去禁止指令重排序，禁止了反而影响单线程的性能。

所以对于这个问题，为什么`synchronized`能够保证有序性，却不能禁止指令重排序？那是因为`synchronized`没有必要禁止指令重排序，否则还会影响程序性能。

### 4.6、synchronized与ReentrantLock相比性能不好的原因

`synchronized`是基于进入和退出管程`Monitor`实现的，而`Monitor`底层是依赖于`OS`的`Mutex Lock`，获取锁和释放锁都需要经过系统调用，而系统调用涉及到用户态和内核态的切换，会经过`0x80`中断，经过内核调用后再返回用户态，因此而效率低下。

而`ReentrantLock`底层实现依赖于特殊的`CPU`指令，比如发送`lock`指令和`unlock`指令，不需要用户态和内核态的切换，所以效率高（这里和volatile底层原理类似）。

不过相对来说，在并发竞争不大的情况下，`synchronized`的性能反而会超越`ReentrantLock`，毕竟`synchronized`有同步消除、偏向锁这些机制，可以确保在竞争不激烈的情况下，程序性能得到很好释放。