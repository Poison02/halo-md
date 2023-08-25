# JVM内存区域

Java虚拟机的内存空间分为**五个部分**。

- 程序计数器
- Java虚拟机栈
- 本地方法栈
- 堆
- 方法区

jdk1.8前后的虚拟机内存空间有点变化，下面两个图展示：

- JDK1.7

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-runtime1.7-16929293058111.png)

- JDK1.8

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-runtime1.8-16929293058122.png)

可以看到，主要的区别是**没有了方法区**，而是增加了元空间在本地内存。

**线程私有的：**

- 程序计数器
- 虚拟机栈
- 本地方法栈

线程共享的：

- 堆
- 方法区
- 直接内存

下面一一进行讲解

## 程序计数器

### 定义

程序计数器是一块比较小的内存空间，使当前线程正在执行的那条字节码指令的地址，可以看作当前线程所执行的字节码的行号指示器。若当前线程正在执行的是一个本地方法，那么此时程序计数器为`Undefined`。

### 作用

字节码解释器工作时通过改变这个计数器的值来选取下一跳需要执行的字节码指令，分支、循环、跳转、异常处理、线程恢复等功能都需要依赖这个计数器来完成；除此之外，为了线程能够恢复到正确的执行位置，每条线程都需要有一个独立的程序计数器，各线程之间计数器互不影响，独立存储，我们称这类内存区域为线程私有的内存。

综上，程序计数器的两个作用为：

- **字节码解释器通过改变计数器的值来一次读取指令，从而实现代码的流程控制等；**
- **多线程情况下，程序计数器记录的是当前线程执行的位置，从而当线程切换回来的时候，就知道上次线程执行到哪儿了。**

### 特点

- 内存较小的内存空间
- 线程私有
- 生命周期：随着线程创建而创建，结束而销毁
- 唯一一个不会出现 `OutOfMemory` 的内存区域

## Java虚拟机栈

### 定义

Java虚拟机栈是描述Java方法运行过程的内存模型。

Java虚拟机会为每一个即将运行的Java方法创建一块叫做**栈帧**的区域，用于存放该方法运行过程中的一些信息。

我们都知道方法调用的数据需要通过栈进行传递，每一次方法调用都会有一个对应的栈帧被压入栈中，每一个方法调用结束后，都会有一个栈帧被弹出。

栈是由一个个栈帧组成，而每个栈帧都拥有：局部变量表、操作数栈、动态链接、方法返回地址。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-zhanzhen-16929293058123.png)

### 栈帧

**局部变量表**

主要存放编译器可知的各种数据类型（boolean、byte、short、char、int、long、float、double）、对象引用（reference类型，不同于对象本身，可能是一个指向对象起始地址的引用指针，也可能是指向一个代表对象的句柄或其他与此对象相关的位置）。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-jububianliangbiao-16929293058124.png)

局部变量表定义为一个数字数组，用于存储方法参数、定义在方法体内部的局部变量。

局部变量表容量大小是在**编译期**确定的。最基本的存储单元是**slot**，32位类型占用一个slot，64位类型占用两个slot。

对于slot的理解：

- JVM虚拟机会为局部变量表中的每个slot都分配一个访问索引，通过这个索引即可成功访问到局部变量表中指定的局部变量值。
- 如果当前帧是由构造方法后者实例方法创建的，那么该对象引用this，会存放在index为0的slot处，其余的参数表顺序继续排列。
- 栈帧中的局部变量表中的槽位是可以重复的，如果一个局部变量表过了它的作用域，那么其作用域之后声明的新的局部变量就有可能会复用过期的局部变量的槽位，从而达到节省资源的目的。

在栈帧中，与**性能调优关系**最密切的部分就是**局部变量表**，方法执行时，虚拟机使用局部变量表完成方法的传递，局部变量表中的变量也是重要的垃圾回收根节点，只要被局部变量表中直接或间接引用的对象都不会被回收。

**操作数栈**

主要作为方法调用的中转站使用，用于存放方法执行过程中产生的中间计算结果。另外，计算过程中产生的临时变量也会放在操作数栈中。

- **栈顶缓存技术**：由于操作数是存储在内存中，频繁的进行内存读写操作影响执行速度，将栈顶元素全部缓存到物理CPU的寄存器中，以此降低对内存的读写次数，提升执行引擎的执行效率。
- 每个操作数栈会拥有一个明确的栈深度，用于存储数值，最大深度在编译期就定义好。32位类型占用一个栈单位深度，64位占用两个栈单位深度。
- 并非采用访问索引方式进行数据访问，而是只能通过**标准的入栈、出栈操作**完成一个数据访问。

**动态链接**

主要服务一个方法现需要调用其他方法的场景。Class文件的常量池里保存有大量的符号引用比如方法引用的符号引用。当一个方法要调用其他方法，需要将常量池中指向**方法的符号引用转化为其在内存地址中的直接引用**。**动态链接的作用就是为了将符号引用转换为调用方法的直接引用**，这个过程也被称为**动态链接**。通俗地说，就是如果被调用的方法在编译器就被确定，那么就只能在运行期将调用的方法地符号引用转换为直接引用，这种引用转换类型具备动态性，因此被称为动态链接。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-dongtailianjie-16929293058125.png)

栈空间虽然不是无限的，但一般正常调用地情况下是不会出现问题的。不过，如果函数调用陷入无限循环的话，就会导致栈中被压入太多栈帧而占用太多空间，导致栈空间过深。那么当线程请求栈地深度超过当前Java虚拟机栈地最大深度的时候，就会抛出 `StackOverFlowError` 错误。

Java方法有两种返回方式，一种是return正常返回，另一种是抛出异常。不管是哪种方式，都会导致栈帧被弹出。也就是说，**栈帧随着方法调用而创建，随着方法结束而销毁。无论方法正常完成还是异常完成都算作方法结束。**

除了 `StackOverFlowError` 错误之外，栈还可能会出现 `OutOfMemoryError` 错误，这是因为如果栈的内存大希奥可以动态扩展，如果虚拟机在动态扩展栈时无法申请到足够的内存空间，则抛出 `OutOfMemoryError` 错误。

### 特点

- 运行速度快，仅次于程序计数器。
- 局部变量表随着栈帧的创建而创建，大小在编译期确定，创建时只分配事先规定的大小即可。在方法运行过程中，局部变量表的大小不会发生改变。
- Java虚拟机会出现两种错误：`StackOverFlowError`和`OutOfMemoryError`。
  - `StackOverFlowError`：若栈的内存大小不允许动态扩展，那么当线程请求栈的深度超过当前Java虚拟机栈的最大深度的时候，就抛出这个错误。
  - `OutOfMemoryError`：如果栈的内存大小允许动态扩展，如果虚拟机在动态扩展栈时无法申请到足够的内存空间，就会抛出这个错误。
- Java虚拟机栈也是线程私有，随着线程创建而创建，随着线程结束而销毁。
- 出现 `StackOverFlowError`时，内存空间可能还有很多。

**方法返回地址**

这里也可以称为是方法调用相关的所有，包括方法调用和方法返回。

## 本地方法栈

### 定义

本地方法栈是为JVM运行`Native`方法准备的空间，由于很多Native方法都是用C或C++实现的，所以它通常又叫做C栈。它与Java虚拟机栈实现的功能类似，只不过本地方法栈是描述本地方法运行过程的内存模型。

### 栈帧

本地方法被执行时，也会创建相应的栈帧，栈帧用于存放局部变量表、操作数栈、动态链接、方法出口信息等

方法执行结束后，相应的栈帧也会出栈释放内存空间。也会抛出 `StackOverFlowError` 和 `OutOfMemoryError` 错误。

## 堆

### 定义

Java虚拟机中内存最大的一块，Java堆是所有线程共享的一块内存区域。Java中创建的**对象几乎都存放在堆中**。**此内存区域的唯一目的就是存放对象实例，几乎所有的对象实例以及数组都在这里分配内存。**

前面提到的是**几乎**所有对象都在堆中，为什么这样说呢？

Java 世界中“几乎”所有的对象都在堆中分配，但是，随着 JIT 编译器的发展与逃逸分析技术逐渐成熟，栈上分配、标量替换优化技术将会导致一些微妙的变化，所有的对象都分配到堆上也渐渐变得不那么“绝对”了。从 JDK 1.7 开始已经默认开启逃逸分析，如果某些方法中的对象引用没有被返回或者未被外面使用（也就是未逃逸出去），那么对象可以直接在栈上分配内存。

Java堆是垃圾收集器管理的主要区域，因此也被称作**GC堆（Gerage Collected Heap）**。从垃圾回收的角度，由于现在收集器基本都采用分代垃圾收集算法，所以Java堆还可以细分为：新生代、老年代；再细致一点有：Eden、Servivor、Old等空间。进一步划分的目的是为了更好的回收内存，或者更快的分配内存。

再JDK7版本以及JDK7版本之前，堆内存通常分为下面三部分：

1. 新生代内存（Young Generation）
2. 老年代（Old Generation）
3. 永久代（Permanent Generation）

如下图所示（Eden区、两个Servivor区S0和S1都属于新生代，中间一层属于老年代，最下面一层属于永久代）

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-heap-16929293058126.png)

**左边是JDk1.7，右边是JDK1.8**

**JDK8之后PermGen（永久代）已被MetaSpace取代。元空间使用的是本地内存**

大部分情况，对象都会首先在Eden区域分配，在一次新生代垃圾回收后，如果对象还存活，则会进入S0或者S1，并且对象的年龄还会加1（Eden区 -> Survivor区后对象的初始年龄变为1），当它的年龄增加到一定程度（默认是15岁），就会晋升到老年代中。对象晋升到老年代的年龄阈值，可以通过参数 `-XX:MaxTenuringThreshold` 来设置。

> 动态年龄的计算代码如下：
>
> ```c++
> uint ageTable::compute_tenuring_threshold(size_t survivor_capacity) {
> 	//survivor_capacity是survivor空间的大小
> size_t desired_survivor_size = (size_t)((((double) survivor_capacity)*TargetSurvivorRatio)/100);
> size_t total = 0;
> uint age = 1;
> while (age < table_size) {
> total += sizes[age];//sizes数组是每个年龄段对象大小
> if (total > desired_survivor_size) break;
> age++;
> }
> uint result = age < MaxTenuringThreshold ? age : MaxTenuringThreshold;
> 	...
> }
> 
> ```
>
> 

堆这里最容易出现的就是 `OutOfMemoryError` 错误，并且出现这种错误之后的表现形式还会有几种，比如：

1. `java.lang.OutOfMemoryError: GC Overhead Limit Exceeded`：当JVM花太多时间执行垃圾回收并且只能回收很少的堆空间时，就会发生此错误。
2. `java.lang.OutOfMemoryError: Java heap space`：假如在创建新的对象时，堆内存中的空间不足以存放新创建的对象，就会引发此错误。（和配置的最大堆内存有关，且受制于物理内存大小。最大堆内存可通过 `-Xmx` 参数配置，若没有特别配置，将会使用默认值）
3. ...

### 新生代与老年代

- 老年代比新生代生命周期长
- 新生代与老年代默认比例 `1:2` ：JVM调参数，`XX:NewRatio=2`，表示新生代占1，老年代占2，新生代占整个堆的1/3
- HotSpot中，Eden空间和另外两个Survivor空间缺省所占的比例是：`8:1:1`
- 几乎所有的Java对象都是**在Eden区被new出来的**，Eden放不了的大对象，就直接进入老年代了。

### 对象分配过程

- new的对象先放在Eden区，大小有限制
- 如果创建新对象时，Eden空间填满了，就会触发`Minor GC`，将Eden不再被其他对象引用的对象进行销毁，再加载新的对象放到Eden区，特别注意的是Survivor区满了是不会触发`Minor GC`的，而是Eden空间填满了，`Minor GC`才顺便清理Survivor区
- 将Eden中剩余的对象移到`Servivor0`区
- 再次触发垃圾回收，此时上次Servivor下来的，放在`Survivor0`区的，如果没有回收，就会放到`Survivor1`区
- 再次经历垃圾回收，又会将幸存者重新放回Servivor0区，以此类推
- 默认是15次循环，超过15次，则会将Survivor区幸村下来的转去老年区，JVM参数设置次数：`-XX:MaxTenuringThreshold=N`进行设置
- 频繁在新生区代收集，很少在老年代区收集，几乎不再永久代区/元空间收集

### Full GC / Major GC触发条件

- 显式调用 `System.gc()`，老年代的空间不够，方法区的空间不够等都会触发`Full GC`，同时对新生代和老年代回收，`Full GC`的STW的时间最长，应该要避免
- 在出现`Major GC`之前，会先触发`Minor GC`，如果老年代的空间还是不够就会触发`Major GC`，STW的时间长于`Minor GC`

## 方法区

### 定义

方法区属于JVM运行时数据区域的一块逻辑区域，是各个线程共享的内存区域。

方法区存放以下信息：

- 已经被虚拟机加载的类信息、字段信息、方法信息
- 常量
- 静态变量
- 即时编译器编译后的代码缓存

**方法区和永久代以及元空间的关系？**

方法区和永久代以及元空间的关系就像Java中接口和类的关系，类实现了接口，这里的类就可以看作是永久代和元空间，接口可以看作是方法区，也就是说永久代以及元空间是HotSpot虚拟机对虚拟机规范中方法区的两种实现方式。并且，永久代是jdk1.8之前的方法区实现，jdk.1.8及以后的实现变成了元空间。

**为什么要将永久代替换为元空间？**

>  详细请见《深入理解Java虚拟机》第3版2.2.5



1. 整个永久代有一个JVM本身设置的固定大小上限，无法进行调整，而元空间使用的是本地内存，受本机可用内存的限制，虽然元空间仍旧可能溢出，但是比原来出现的几率小（元空间溢出得到错误：`java.lang.OutOfMemoryError: MetaSpace`）

   你可以使用 `-XX: MaxMetaspaceSize` 标志设置最大元空间大小，默认值是`undefined`，这意味着它只受系统内存的限制。`-XX: MetaspaceSize`调整标志定义元空间的初始大小如果未指定此标志，则Metaspace将根据运行时的应用程序需求动态地调整大小。

2. 元空间里面存放的是类的元数据，这样加载多少类的元数据就不由 `MaxPermSize` 控制了，而是由系统的实际可用空间类控制，这样能加载的类就更多了。

3. 在JDK8，合并HotSpot和JRockit的代码时，JRockit从来没有一个叫永久代的东西，合并之后就没有币摇额外的设置这么一个永久代的地方了。

**方法区常用参数？**

jdk1.8前永久代没有移除的时候通过下面这些参数调节方法区大小：

```java
-XX: PermSize=N // 永久代初始大小
-XX: MaxPermSize=N // 永久代最大大小，超过这个值将会抛出OutOfMemory
```

相对而言，垃圾收集行为在这个区域是比较少出现的，但并非数据进去方法区之后就永久存在了。

jdk1.8开始，永久代彻底移除，取而代之的是元空间，下面是常用参数：

```java
-XX: MetaspaceSize=N // 设置Metaspace的初始大小
-XX: MaxMetaspaceSize=N // 设置Metaspace的最大大小
```

与永久代很大的不同就是，如果不指定大小的话，随着更多类的创建，虚拟机会耗尽所有可用的系统内存。

## 运行时常量池

Class文件中除了有类的版本、字段、方法、接口等描述信息外，还有用于存放编译期生成的各种字面量（Literal）和符号引用（Symbolic Reference）的**常量池表（Constant Pool Table）**。

字面量是源代码中的固定值的表示法，即通过字面我们就能知道其值的含义。字面量包括整数、浮点数和字符串字面量。常见的符号引用包括类符号引用、字段符号引用、方法符号引用、接口方法符号等。

《深入理解Java虚拟机》第三版7.34节的描述：

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-7.34-16929293058127.png)

常量池表会在类加载后存放到方法区的运行时常量池中。

运行时常量池的功能类似于传统编程语言的符号表，尽管它包含了比典型符号表更广泛的数据。

既然运行时常量池是方法区的一部分，自然收到方法区内存的限制，当常量池无法再申请到内存时就会抛出 `OutOfMemoryError`错误。

## 字符串常量池

**字符串常量池**是JVM为了提升性能和减少内存消耗针对字符串（String类）专门开辟的一块区域，主要目的是为了避免字符串的重复创建。

```java
// 在堆中创建字符串对象“ab”
// 将字符串对象“ab”的引用保存在字符串常量池中
String aa = "ab";
// 直接返回字符串常量池中字符串对象“ab”的引用
String bb = "ab";
System.out.println(aa == bb); // true
```

HotSpot 虚拟机中字符串常量池的实现是 `src/hotspot/share/classfile/stringTable.cpp` ,`StringTable` 可以简单理解为一个固定大小的`HashTable` ，容量为 `StringTableSize`（可以通过 `-XX:StringTableSize`

 参数来设置），保存的是字符串（key）和 字符串对象的引用（value）的映射关系，字符串对象的引用指向堆中的字符串对象。

JDK1.7之前，字符串常量池存放在永久代。JDK1.7字符串常量池和静态变量从永久代移动了Java堆中。

- jdk1.6

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-1.6-16929293058128.png)

- jdk1.7

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-1.7-16929293058129.png)

**jdk1.7为什么要将字符串常量池移动到堆中？**

主要是因为永久代中GC的回收效率太低（参考前文说过的，很少在永久代回收），只有在整堆收集（Full GC）的时候才会被执行GC。Java程序中通常会有大量的被创建的字符串等待回收，将字符串常量池放到堆中，能够更高效及时回收字符串内存。

> 运行时常量池、方法区、字符串常量池这些都是不随虚拟机实现而改变的逻辑概念，是公共且抽象的，Metaspace、Heap是与具体某种虚拟机实现相关的物理概念，是私有且具体的。

## 直接内存

### 定义

是一种特殊的内存缓冲区，并不在Java堆或方法区中分配（但也可能被Java操作），而是通过JNI的方式在本地内存上分配的。

直接内存并不是虚拟机运行时数据区的一部分，也不是虚拟机规范中定义的内存区域，但是这部分内存也被频繁使用。而且也可能导致 `OutOfMemoryError` 错误出现。

JDK1.4中新加入的**NIO（Non-Blocking I/O，也被称为New I/O）**，引入了一种基于**通道（Channel）与缓存区（Buffer）的I/O方式，它可以直接使用Native函数库直接分配堆外内存，然后通过一个存储在Java队中的DirectByteBuffer对象作为这块内存的引用进行操作。这样就能在一些场景中显著提高性能，因为避免了在Java堆和Native堆之间来回复制数据。**

直接内存的分配不会收到Java堆的限制，但是，既然是内存就会收到本机总内存大小以及处理器寻址空间的限制。

类似的概念还有**堆外内存**。

堆外内存就是把内存对象分配在堆（新生代、老年代、永久代）以外的内存，这些内存只接受操作系统管理（而不是虚拟机），这样做的结果就是能够在一定程度上减少垃圾回收堆应用程序造成的影响。

### 直接内存与堆内存比较

- 直接内存申请空间耗费更高的性能
- 直接内存读取IO的性能要优于普通的堆内存
- 直接内存作用链：本地IO -> 直接内存 -> 本地IO
- 堆内存作用链：本地IO -> 直接内存 -> 非直接内存 -> 直接内存 -> 本地IO

# 前言

当需要排查各种内存一出问题、当垃圾收集称为系统达到更高并发的瓶颈时，我们就需要对这些自动化的技术实施必要的监控和调节。

程序计数器、虚拟机栈、本地方法栈随着线程而生而死；栈帧随着方法的开始而入栈，随着方法的结束而出栈。这几个区域的内存分配和回收都具有确定性，在这几个区域内不需要过多考虑回收的问题，因为方法结束或者线程结束时，内存自然就跟着回收了。

而对于Java堆和方法区，我们只有在程序运行期间才能知道会创建哪些对象，这部分内存的分配和回收都是动态的，垃圾收集器所关注的正式这部分内存。

本节分为两大部分：

- 垃圾回收策略与算法
- 垃圾收集器

# 垃圾回收策略与算法

## 内存分配与回收策略

对象的内存分配，就是在堆上分配（也可能经过JIT编译后被拆散成标量类型并间接在栈上分配），对象主要分配在新生代的Eden区上，少数情况下可能分配陪在老年代，**分配规则不固定**，取决于当前使用的垃圾收集器组合以及相关的参数配置。

### 对象优先在Eden区分配

大多数情况下，对下给你在新生代中Eden区分配。当Eden区没有足够的空间进行分配时，虚拟机将发起一次**Minor GC**。

### 大对象直接进入老年代

大对象就是需要大量连续内存空间的对象，如字符串、数组等。

一个大对象能够存入Eden区的概率比较小，发生分配担保的概率比较大，而分配担保需要涉及大量的复制，就会造成效率低下。

大对象直接进入老年代的行为是由虚拟机动态决定的，它与具体使用的垃圾回收期和相关参数有关。大对象直接进入老年代是一种优化策略，旨在避免将大对象放入新生代，从而减少新生代的垃圾回收频率和成本。

虚拟机提供了一个 `-XX:PretenureSizeThreshold`参数，令大于这个设置值的对象直接在老年代分配，这样做的目的是避免在Eden区及两个Survivor区之间发生大量的内存复制。

- G1垃圾回收器会根据 `-XX:G1HeapRegionSize` 参数设置的堆区域大小和 `-XX:G1MixedGCLiveTHresholdPrecent` 参数设置的阈值，来决定哪些对象会直接进入老年代。
- Parallel Scavenge垃圾回收器中，默认情况下，并没有一个固定的阈值（ `-XX:ThresholdTolelrance` 是动态调整）来决定何时直接在老年代分配大对象。而是由虚拟机根据当前的堆内存情况和历史数据动态决定。

### 长期存活的对象将进入老年代

既然虚拟机采用了分代收集的思想来管理内存，那么内存回收时就必须能识别那些对象应该放在新生代，哪些对象应该在老年代。为了做到这一点，虚拟机给每个对象一个对象年龄计数器。

大部分情况，对象首先在Eden区域分配。如果对象在Eden出生并经过第一次Minor GC后仍然能够活下来，并且能被Survivor容纳，将被移动到Survivor区域，并且该对象的年龄+1（Eden区 -> Survivor区后对象的年龄加1）.

对象在Survivor中每熬过一次Minor GC年龄就会增加1岁，当年龄增加到一定程度（默认15），就会被晋升到老年代中。这个年龄阈值，可以通过参数 `-XX:MaxTenuringThreshold` 来设置。

针对HotSpotVM的实现，它里面的GC其实准确分类只有两大种：

- Partial GC（部分收集）

  - Young GC（Minor GC，新生代收集），只对新生代进行垃圾收集；

  - Old GC（Major GC，老年代收集），只对老年代进行垃圾收集。需要注意的是Major GC在有的语境中也用于指代整堆收集；

  - Mixed GC（混合收集）：对整个新生代和部分老年代进行垃圾收集。

- Full GC（整堆收集），收集整个Java堆和方法区

### 空间分配担保

空间分配担保是为了确保Minor GC之前老年代本身还有容纳新生代所有对象的剩余空间。

《深入理解Java虚拟机》第三章对于空间分配担保的描述如下：

> JDK6 update24之前，发生Minor GC之前，虚拟机必须先检查老年代最大可用的连续空间是否大于新生代所有对象总空间，如果这个条件成立，那这一次Minor GC可以确保是安全的。如果不成立，则虚拟机会先查看 `-XX:HandlePromotionFailure` 参数的设置值是否允许担保失败（Handle Promotion Failure）：如果允许，那会继续检查老年代最大可用的连续空间是否大于历次晋升到老年代对象的平均大小，如果大于，将尝试进行一次Minor GC，尽管这次Minor GC是有风险的；如果小于，或者 `-XX:HandlePromotionFailure` 设置不允许冒险，那这时就要改为一次Full GC。
>
> JDK6 update24之后，规则就变为只要老年代的连续空间大于新生代对象总大小或者历次晋升的平均大小，就会进行Minor GC， 否则将进行Full GC。

通过清除老年代中的废弃数据来扩大老年代空闲空间，以便给新生代作担保。这个过程就是分配担保。

> 总结以下有哪些情况可能会触发JVM进行Full GC。
>
> 1. `System.gc()` 方法的调用，此方法的调用是建议JVM进行Full GC，注意这只是建议而非一定，但在很多情况下它会触发Full GC，从而增加Full GC的频率。通常情况下我们只需要让虚拟机自己去管理内存即可，我们可以通过 `-XX:DisableExplicitGC` 来禁止调用该方法。
> 2. 老年代空间不足 老年代空间不足会触发Full GC操作，若进行该操作后空间依然不足，则会抛出如下错误：`java.lang.OutOfMemoryError: Java heap space`
> 3. 永久代空间不足 JVM规范中运行时数据区域中的方法区，在HotSpot虚拟机中也成为永久代（Permanet Generation），存放一些类信息、常量、静态变量等数据，当系统要加载的类、反射的类和调用的方法较多时，永久代可能会被占满，会触发Full GC。如果经过Full GC仍然回收不了，那么JVM会抛出以下错误：`java.lang.OutOfMemoryError: PermGen space`
> 4. CMS GC时出现 `promotion failed` 和 `concurrent mode failure` promotion failed，就是上文说的担保失败，而concurrent mode failure是在执行CMS GC的过程中同时有对象要放入老年代，而此时老年代空间不足造成的。
> 5. 统计得到的Minor GC晋升到老年代的平均大小小于老年代的剩余空间。

## 死亡对象判断方法

堆中几乎放着所有的对象实例，对堆垃圾回收前的第一步就是要判断哪些对象已经死亡（即不能再被任何途径使用的对象）

若一个对象不被任何对象或变量引用，那么它就是无效对象，需要被回收

### 引用计数法

给对象添加一个引用计数器（对象头维护者一个counter计数器）：

- 每当有一个地方引用它，计数器就加1；
- 当引用失效，计数器就减1；
- 任何时候计数器为0的对象就是不可能再被使用的。

**这个方法实现简单，效率高，但是目前主流的虚拟机中并没有选择这个算法来管理内存，其最主要的原因是他很难解决对象之间循环引用的问题。**（循环应用就是A对象引用B，B对象也引用A）

所谓对象之间的相互引用问题，如下面代码所示：除了对象 `objA` 和 `objB` 相互引用着对方之外，这两个对象之间再无任何引用。但是因为它们互相引用对方，导致它们的引用计数器都不为0，于是引用计数算法无法通知GC回收器回收他们。

```java
public class ReferenceCountingGc {
    Object instance = null;
    public static void main(String[] args) {
        ReferenceCountingGc objA = new ReferenceCountingGc();
        ReferenceCountingGc objB = new ReferenceCountingGc();
        objA.instance = objB;
        objB.isntance = objA;
        objA = null;
        objB = null;
    }
}
```

### 可达性分析算法

这个算法的基本思想就是通过一系列的称为`GC Roots`的对象作为起点，从这些对象开始向下搜索，节点所走过的路径称为引用链，当一个对象到`GC Roots`没有任何引用链相连的话，则证明此对象是不可用的，需要被回收。

如下图，`obj6 ~ obj10` 之间虽然有引用关系，但是GC Roots不可达，因此为需要被回收的对象。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-gcroots-169293150416519.png)

**哪些对象可以作为GC Roots呢？**

- 虚拟机栈（栈帧中的本地变量表）中引用的对象
- 本地方法栈（Native方法）中引用的对象
- 方法区中类静态属性引用的对象
- 方法区中常量引用的对象
- 所有被同步锁持有的对象
- JNI（Java Native Interface）引用的对象

**对象可以被回收，就代表一定会被回收吗？**

即使在可达性分析法中不可达的对象，也并非是“非死不可”的，这时候它们暂时处于“缓刑阶段”，要真正宣告一个对象死亡，至少要经历两次标记过程；可达性分析法中不可达的对象被第一次标记并且进行一次筛选，筛选的条件是此对象是否有必要执行 `finalize` 方法。当对象没有覆盖 `finalize` 方法，或 `finalize` 方法已经被虚拟机调用过时，虚拟机将这两种情况视为没有必要执行。

被判定为需要执行的对象将会被放在一个队列中进行第二次标记，除非这个对象与引用链上的任何一个对象建立关联，否则就会被真的回收。

## 引用类型总结

无论是通过引用计数法判断对象引用数量，还是通过可达性分析法判断对象的引用链是否可达，判定对象的存活都与引用有关。

JDK1.2之前，Java中引用的定义很传统：如果reference类型的数据存储的数值代表的是另一块内存的起始地址，就成这块内存代表一个引用。

JDK1.2之后，Java对引用的概念进行了扩充，将引用分为强引用（StrongReference）、软引用（SoftReference）、弱引用（WeakReference）、虚引用（PhantomReference）四种（引用强度逐渐减弱）。

### 强引用

以前我们使用的大部分引用实际上都是强引用，这是使用最普遍的引用（类似 "`Object obj = new Object()`" 这类的引用，就是强引用）。如果一个对象具有强引用，那就类似于**必不可少的生活用品**，垃圾回收器绝不会回收它。当内存空间不足，Java虚拟机宁愿抛出`OOM`错误，使程序异常终止，也不会随意回收具有强引用的对象来解决内存不足问题。

### 软引用

如果一个对象只具有软引用，那就类似于**可有可无的生活用品**。如果内存空间足够，垃圾回收器就不会回收它，如果内存空间不足，就会回收这些对象的内存。只要垃圾回收器没有回收它，该对象就可以被程序使用。软引用可用来实现内存敏感的高速缓存。

软引用可以和一个引用队列（ReferenceQueue）联合使用，如果软引用所引用的对象被垃圾回收，JAVA 虚拟机就会把这个软引用加入到与之关联的引用队列中。

### 弱引用

如果一个对象只具有弱引用，那就类似于**可有可无的生活用品**。弱引用与软引用的区别在于：只具有弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。不过，由于垃圾回收器是一个优先级很低的线程， 因此不一定会很快发现那些只具有弱引用的对象。

弱引用可以和一个引用队列（ReferenceQueue）联合使用，如果弱引用所引用的对象被垃圾回收，Java 虚拟机就会把这个弱引用加入到与之关联的引用队列中。

### 虚引用

"虚引用"顾名思义，就是形同虚设，与其他几种引用都不同，虚引用并不会决定对象的生命周期。如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收。

它仅仅是提供了一种确保对象被 finalize 以后，做某些事情的机制，比如，通常用来做所谓的 Post-Mortem 清理机制。

**虚引用主要用来跟踪对象被垃圾回收的活动**

**虚引用与软引用和弱引用的一个区别在于：** 虚引用必须和引用队列（ReferenceQueue）联合使用。当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在回收对象的内存之前，把这个虚引用加入到与之关联的引用队列中。程序可以通过判断引用队列中是否已经加入了虚引用，来了解被引用的对象是否将要被垃圾回收。程序如果发现某个虚引用已经被加入到引用队列，那么就可以在所引用的对象的内存被回收之前采取必要的行动。

特别注意，在程序设计中一般很少使用弱引用与虚引用，使用软引用的情况较多，这是因为**软引用可以加速 JVM 对垃圾内存的回收速度，可以维护系统的运行安全，防止内存溢出（OutOfMemory）等问题的产生**。

## 回收方法区内存

方法区中存放生命周期较长的类信息、常量、静态变量，每次垃圾收集只有少量垃圾被清除。方法区中主要清楚两种垃圾：

- 废弃常量
- 无用的类

### 如何判断一个常量是废弃常量？

运行时常量池主要回收的是废弃的常量。那么，我们如何判断一个常量是废弃常量呢？

> 1. **JDK1.7 之前运行时常量池逻辑包含字符串常量池存放在方法区, 此时 hotspot 虚拟机对方法区的实现为永久代**
>
> 2. **JDK1.7 字符串常量池被从方法区拿到了堆中, 这里没有提到运行时常量池,也就是说字符串常量池被单独拿到堆,运行时常量池剩下的东西还在方法区, 也就是 hotspot 中的永久代** 。
>
> 3. **JDK1.8 hotspot 移除了永久代用元空间(Metaspace)取而代之, 这时候字符串常量池还在堆, 运行时常量池还在方法区, 只不过方法区的实现从永久代变成了元空间(Metaspace)**

假如在字符串常量池中存在字符串 "abc"，如果当前没有任何 String对象引用该字符串常量的话，就说明常量 "abc" 就是废弃常量，如果这时发生内存回收的话而且有必要的话，"abc" 就会被系统清理出常量池了。

### 如何判断一个类是无用的类？

方法区主要回收的是无用的类，那么如何判断一个类是无用的类的呢？

判定一个常量是否是“废弃常量”比较简单，而要判定一个类是否是“无用的类”的条件则相对苛刻许多。类需要同时满足下面 3 个条件才能算是 **“无用的类”**：

- 该类所有的实例都已经被回收，也就是 Java 堆中不存在该类的任何实例。
- 加载该类的 `ClassLoader` 已经被回收。
- 该类对应的 `java.lang.Class` 对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。

虚拟机可以对满足上述 3 个条件的无用类进行回收，这里说的仅仅是“可以”，而并不是和对象一样不使用了就会必然被回收。

## 垃圾收集算法

### 标记-清除算法

标记-清除（Mark-and-Sweep）算法分为标记和清除两个阶段：首先标记处所有不需要回收的对象，在标记完成后统一回收掉所有没有被标记的对象。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-biaojiqingchu-169293150416620.png)

**标记**的过程是：遍历所有的`GC Roots`，然后将所有`GC Roots`可达的对象**标记为存活的对象**。

**清除**的过程是：将遍历堆中所有的对象，将没有标记的对象全部清除掉。于此同时，清除那些被标记过的对象的标记，以便下次的垃圾回收。

它是最基础的收集算法，后续算法都是对其不足进行改进得到。这种垃圾收集算法会带来两个明显的问题：

1. **效率问题**：标记和清除两个过程效率都不高
2. **空间问题**：标记清楚后会产生大量不连续的内存碎片（碎片太多可能导致需要分配较大对象时，无法找到足够的连续内存而不得不提前触发另一次垃圾收集动作）

### 复制算法

为了解决标记-清除算法的效率和内存碎片问题，复制（Copying）收集算法出现了。它可以将内存分为大小相同的两块，每次使用其中的一块。当这一块的内存使用完后，就将还存活的对象复制到另一块去，然后再把使用的空间一次清理掉。这样就使每次的内存回收都是对内存区间的一半进行回收。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-fuzhi-169293150416621.png)

这种算法存在下面问题：

- **可用内存变小**：缩小为原来的一半
- **不适合老年代**：如果存活对象数量比较大，复制性能会变得很差

### 标记-整理算法

标记-整理（Mark-and-Compact）算法是根据老年代的特点提出的一种标记算法，标记过程仍然与标记-清除算法一样，但后续步骤不是直接对可回收对象回收，而是让所有存活的对象向一端移动，然后直接清理掉端边界以外的内存。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-biaojizhengli-169293150416622.png)

**标记**：它的第一个阶段与**标记-清除算法**是一模一样的，均是遍历 `GC Roots`，然后将存活的对象标记。

**整理**：移动所有**存活的对象**，且按照内存地址次序依次排列，然后将末端内存地址以后的内存全部回收。因此，第二阶段才称为整理阶段。

由于多了整理这一步，因此效率也不高，适合老年代这种垃圾回收频率不是很高的场景。

老年代的对象一般寿命比较长，因此每次垃圾回收会有大量对象存活，如果采用复制算法，每次都需要复制大量存活的对象，效率很低。

### 分代收集算法

当前虚拟机的垃圾收集都采用分代收集算法，这种算法没有什么新的思想，只是根据对象存活周期的不同将内存分为几块。一般将 Java 堆分为新生代和老年代，这样我们就可以根据各个年代的特点选择合适的垃圾收集算法。

比如在**新生代**中，每次收集都会有大量对象死去，所以可以选择”**复制**“算法，只需要付出少量对象的复制成本就可以完成每次垃圾收集。而**老年代**的对象存活几率是比较高的，而且没有额外的空间对它进行分配担保，所以我们必须选择“**标记-清除**”或“**标记-整理**”算法进行垃圾收集。

# 垃圾收集器

**如果说收集算法是内存回收的方法论，那么垃圾收集器就是内存回收的具体体现。**

虽然我们对各个收集器进行比较，但并非要挑选出一个最好的收集器。因为直到现在为止还没有最好的垃圾收集器出现，更加没有万能的垃圾收集器，**我们能做的就是根据具体应用场景选择适合自己的垃圾收集器**。试想一下：如果有一种四海之内、任何场景下都适用的完美收集器存在，那么我们的 HotSpot 虚拟机就不会实现那么多不同的垃圾收集器了。

JDK 默认垃圾收集器（使用 `java -XX:+PrintCommandLineFlags -version` 命令查看）：

- JDK 8：Parallel Scavenge（新生代）+ Parallel Old（老年代）
- JDK 9 ~ JDK20: G1

### Serial收集器

Seaial（串行）收集器是最基本、历史最悠久的垃圾收集器了。这是一个单线程收集器。他的单线程的意义不仅仅意味着他只会使用一条垃圾收集线程去完成垃圾收集工作，更重要的是它在进行垃圾收集工作的时候必须暂停其他所有的工作线程（“Stop The World”），直到它收集结束。

**新生代采用复制算法，老年代采用标记-整理算法**。

一般客户端应用所需内存较小，不会创建太多对象，而且堆内存不大，因此垃圾收集器回收时间短，即使在这段时间停止一切用户线程，也不会感觉明显卡顿。因此 Serial 垃圾收集器**适合客户端**使用。

由于 Serial 收集器只使用一条 GC 线程，避免了线程切换的开销，从而简单高效。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-serial-169293150416623.png)

但是 Serial 收集器有没有优于其他垃圾收集器的地方呢？当然有，它**简单而高效（与其他收集器的单线程相比）**。Serial 收集器由于没有线程交互的开销，自然可以获得很高的单线程收集效率。Serial 收集器对于运行在 Client 模式下的虚拟机来说是个不错的选择。

### ParNew收集器

ParNew收集器其实就是Serial收集器的多线程版本，除了使用多线程进行垃圾收集外，其余行为（控制参数、收集算法、回收策略等）和Serial收集器完全一样。

**新生代采用复制算法，老年代采用标记-整理算法**。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-parnew-169293150416624.png)

ParNew 是 Serial 的多线程版本。由多条 GC 线程并行地进行垃圾清理。但清理过程依然需要 Stop The World。

ParNew 追求“**低停顿时间**”,与 Serial 唯一区别就是使用了多线程进行垃圾收集，在多 CPU 环境下性能比 Serial 会有一定程度的提升；但**线程切换需要额外的开销**，因此在单 CPU 环境中表现不如 Serial。

它是许多运行在 Server 模式下的虚拟机的首要选择，除了 Serial 收集器外，只有它能与 CMS 收集器配合工作。

### Parallel Scavenge收集器

**这是jdk1.8默认收集器。**

**新生代采用复制算法，老年代采用标记-整理算法**。

Parallel Scavenge 和 ParNew 一样，都是多线程、新生代垃圾收集器。但是两者有巨大的不同点：

- Parallel Scavenge：追求 CPU 吞吐量，能够在较短时间内完成指定任务，因此适合没有交互的后台计算。
- ParNew：追求降低用户停顿时间，适合交互式应用。

```java
-XX:+UseParallelGC

    使用 Parallel 收集器+ 老年代串行

-XX:+UseParallelOldGC

    使用 Parallel 收集器+ 老年代并行
```

`吞吐量 = 运行用户代码时间 / (运行用户代码时间 + 垃圾收集时间)`

追求高吞吐量，可以通过减少 GC 执行实际工作的时间，然而，仅仅偶尔运行 GC 意味着每当 GC 运行时将有许多工作要做，因为在此期间积累在堆中的对象数量很高。单个 GC 需要花更多的时间来完成，从而导致更高的暂停时间。而考虑到低暂停时间，最好频繁运行 GC 以便更快速完成，反过来又导致吞吐量下降。

- 通过参数 `-XX:GCTimeRadio` 设置垃圾回收时间占总 CPU 时间的百分比。
- 通过参数 `-XX:MaxGCPauseMillis` 设置垃圾处理过程最久停顿时间。
- 通过命令 -XX:+UseAdaptiveSizePolicy 开启自适应策略。我们只要设置好堆的大小和 `MaxGCPauseMillis` 或 `GCTimeRadio`，收集器会自动调整新生代的大小、Eden 和 Survivor 的比例、对象进入老年代的年龄，以最大程度上接近我们设置的 `MaxGCPauseMillis` 或 `GCTimeRadio`。

### Serial Old收集器

Serial Old 收集器是 Serial 的老年代版本，都是单线程收集器，只启用一条 GC 线程，都适合客户端应用。它们唯一的区别就是：Serial Old 工作在老年代，使用“标记-整理”算法；Serial 工作在新生代，使用“复制”算法。

### Parallel Old收集器

Parallel Old 收集器是 Parallel Scavenge 的老年代版本，追求 CPU 吞吐量。**Parallel Scavenge 收集器的老年代版本**。使用多线程和“标记-整理”算法。在注重吞吐量以及 CPU 资源的场合，都可以优先考虑 Parallel Scavenge 收集器和 Parallel Old 收集器。

### CMS收集器

CMS（Concurrent Mark Sweep，并发标记清除）收集器是以获取最短回收停顿时间为目标的收集器（追求低停顿），它在垃圾收集时使得用户线程和 GC 线程并发执行，因此在垃圾收集过程中用户也不会感到明显的卡顿。

- 初始标记：Stop The World，仅使用一条初始标记线程对所有与 GC Roots 直接关联的对象进行标记。
- 并发标记：使用**多条**标记线程，与用户线程并发执行。此过程进行可达性分析，标记出所有废弃对象。速度很慢。
- 重新标记：Stop The World，使用多条标记线程并发执行，将刚才并发标记过程中新出现的废弃对象标记出来。
- 并发清除：只使用一条 GC 线程，与用户线程并发执行，清除刚才标记的对象。这个过程非常耗时。

并发标记与并发清除过程耗时最长，且可以与用户线程一起工作，因此，**总体上说**，CMS 收集器的内存回收过程是与用户线程**一起并发执行**的。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-cms-169293150416625.png)

CMS 的缺点：

- 吞吐量低
- 无法处理浮动垃圾
- 使用“标记-清除”算法产生碎片空间，导致频繁 Full GC

对于产生碎片空间的问题，可以通过开启 -XX:+UseCMSCompactAtFullCollection，在每次 Full GC 完成后都会进行一次内存压缩整理，将零散在各处的对象整理到一块。设置参数 -XX:CMSFullGCsBeforeCompaction 告诉 CMS，经过了 N 次 Full GC 之后再进行一次内存整理。

### G1收集器

**G1（Garbage-First）是一款面向服务器的垃圾收集器，主要针对配备多颗处理器及大容量内存的机器，以极高概率满足GC停顿时间要求的同时，还具备吞吐量性能特征。**

被视为 JDK1.7 中 HotSpot 虚拟机的一个重要进化特征。它具备以下特点：

- **并行与并发**：G1 能充分利用 CPU、多核环境下的硬件优势，使用多个 CPU（CPU 或者 CPU 核心）来缩短 Stop-The-World 停顿时间。部分其他收集器原本需要停顿 Java 线程执行的 GC 动作，G1 收集器仍然可以通过并发的方式让 java 程序继续执行。
- **分代收集**：虽然 G1 可以不需要其他收集器配合就能独立管理整个 GC 堆，但是还是保留了分代的概念。
- **空间整合**：与 CMS 的“标记-清除”算法不同，G1 从整体来看是基于“标记-整理”算法实现的收集器；从局部上来看是基于“标记-复制”算法实现的。
- **可预测的停顿**：这是 G1 相对于 CMS 的另一个大优势，降低停顿时间是 G1 和 CMS 共同的关注点，但 G1 除了追求低停顿外，还能建立可预测的停顿时间模型，能让使用者明确指定在一个长度为 M 毫秒的时间片段内，消耗在垃圾收集上的时间不得超过 N 毫秒。

G1 收集器的运作大致分为以下几个步骤：

- **初始标记**
- **并发标记**
- **最终标记**
- **筛选回收**

**G1 收集器在后台维护了一个优先列表，每次根据允许的收集时间，优先选择回收价值最大的 Region(这也就是它的名字 Garbage-First 的由来)** 。这种使用 Region 划分内存空间以及有优先级的区域回收方式，保证了 G1 收集器在有限时间内可以尽可能高的收集效率（把内存化整为零）。

**从 JDK9 开始，G1 垃圾收集器成为了默认的垃圾收集器。**

### ZGC收集器

与 CMS 中的 ParNew 和 G1 类似，ZGC 也采用复制算法，不过 ZGC 对该算法做了重大改进。

在 ZGC 中出现 Stop The World 的情况会更少！

Java11 的时候 ，ZGC 还在试验阶段。经过多个版本的迭代，不断的完善和修复问题，ZGC 在 Java 15 已经可以正式使用了！

不过，默认的垃圾回收器依然是 G1。你可以通过下面的参数启动 ZGC：

```bash
$ java -XX:+UseZGC className
```

关于 ZGC 收集器的详细介绍推荐阅读美团技术团队的 [新一代垃圾回收器 ZGC 的探索与实践](https://tech.meituan.com/2020/08/06/new-zgc-practice-in-meituan.html) 这篇文章。

# 前言

本章主要有以下几个方面：

- 类文件结构
- 类加载过程
- 类加载器

# 类文件结构

## JVM的无关性

谈论JVM的无关性，主要有以下两个：

- 平台无关性：任何操作系统都能运行Java代码
- 语言无关性：JVM能运行除Java以外的其他代码

Java源代码首先使用`javac`编译器编译成`.class`文件，然后由JVM执行`.class`文件，从而程序开始运行。

JVM 只认识 `.class` 文件，它不关心是何种语言生成了 `.class` 文件，只要 `.class` 文件符合 JVM 的规范就能运行。 目前已经有 JRuby、Jython、Scala 等语言能够在 JVM 上运行。它们有各自的语法规则，不过它们的编译器 都能将各自的源码编译成符合 JVM 规范的 .class 文件，从而能够借助 JVM 运行它们。

> Java 语言中的各种变量、关键字和运算符号的语义最终都是由多条字节码命令组合而成的， 因此字节码命令所能提供的语义描述能力肯定会比 Java 语言本身更加强大。 因此，有一些 Java 语言本身无法有效支持的语言特性，不代表字节码本身无法有效支持。

## Class文件结构

Class 文件是二进制文件，它的内容具有严格的规范，文件中没有任何空格，全都是连续的 0/1。Class 文件 中的所有内容被分为两种类型：无符号数、表。

- 无符号数 无符号数表示 Class 文件中的值，这些值没有任何类型，但有不同的长度。u1、u2、u4、u8 分别代表 1/2/4/8 字节的无符号数。
- 表 由多个无符号数或者其他表作为数据项构成的复合数据类型。

如下所示：

```java
ClassFile {
    u4             magic; // Class 文件的标志
    u2             minor_version; // Class 的小版本号
    u2             major_version; // Class 的大版本号
    u2             constant_pool_count; // 常量池的数量
    cp_info        constant_pool[constant_pool_count-1]; // 常量池
    u2             access_flags; // Class 的访问标记
    u2             this_class; // 当前类
    u2             super_class; // 父类
    u2             interfaces_count; // 接口数量
    u2             interfaces[interfaces_count]; // 一个类可以实现多个接口
    u2             fields_count; // 字段数量
    field_info     fields[fields_count]; // 一个类可以有多个字段
    u2             methods_count; // 方法数量
    method_info    methods[methods_count]; // 一个类可以有个多个方法
    u2             attributes_count; // 此类的属性表中的属性数
    attribute_info attributes[attributes_count]; // 属性表集合
}

```

通过IDEA插件 `jclasslib` 可以更直观的观察class文件结构。

class文件具体由以下几个构成：

- 魔数
- 版本信息
- 常量池
- 访问标志
- 类索引、夫类索引、接口索引集合
- 字段表集合
- 方法表集合
- 属性表集合

### 魔数（Magic Number）

```java
u4             magic; // Class 文件的标志
```

每个class文件的头4个字节称为魔数（Magic Number），它唯一的作用就是**确定这个文件是否为一个能被虚拟机接收的class文件**。class 文件的魔数是用 16 进制表示的`0xCAFEBABE`，如果读取的文件不是以这个魔数开头，Java虚拟机将拒绝加载它。

### class文件版本号（Minor&Major Version）

```java
    u2             minor_version; // Class 的次版本号
    u2             major_version; // Class 的主版本号
```

紧接着魔数的四个字节存储的是class文件的版本号：5到6字节是**次版本号**，7到8字节是**主版本号**。

每当 Java 发布大版本（比如 Java 8，Java9）的时候，主版本号都会加 1。你可以使用 `javap -v` 命令来快速查看 Class 文件的版本号信息。

高版本的 Java 虚拟机可以执行低版本编译器生成的 Class 文件，但是低版本的 Java 虚拟机不能执行高版本编译器生成的 Class 文件。所以，我们在实际开发的时候要确保开发的的 JDK 版本和生产环境的 JDK 版本保持一致。

### 常量池（Constant Pool）

```java
u2             constant_pool_count; // 常量池的数量
cp_info        constant_pool[constant_pool_count-1]; // 常量池
```

紧接着主次版本号之后的是常量池，常量池的数量是 `constant_pool_count-1`（**常量池计数器是从 1 开始计数的，将第 0 项常量空出来是有特殊考虑的，索引值为 0 代表“不引用任何一个常量池项”**）。

常量池主要存放两大常量：

- **字面量**
- **符号引用**

字面量比较接近于 Java 语言层面的的常量概念，如文本字符串、声明为 final 的常量值等。

而符号引用则属于编译原理方面的概念。包括下面三类常量：

- 类和接口的全限定名
- 字段的名称和描述符
- 方法的名称和描述符

常量池中每一项常量都是一个表，这 14 种表有一个共同的特点：**开始的第一位是一个 u1 类型的标志位 -tag 来标识常量的类型，代表当前这个常量属于哪种常量类型。**

|                类型                | 标志（tag） |          描述          |
| :--------------------------------: | :---------: | :--------------------: |
|        `CONSTANT_utf8_info`        |      1      |   UTF-8 编码的字符串   |
|      `CONSTANT_Integer_info`       |      3      |       整形字面量       |
|       `CONSTANT_Float_info`        |      4      |      浮点型字面量      |
|        `CONSTANT_Long_info`        |     ５      |      长整型字面量      |
|       `CONSTANT_Double_info`       |     ６      |   双精度浮点型字面量   |
|       `CONSTANT_Class_info`        |     ７      |   类或接口的符号引用   |
|       `CONSTANT_String_info`       |     ８      |    字符串类型字面量    |
|      `CONSTANT_FieldRef_info`      |     ９      |     字段的符号引用     |
|     `CONSTANT_MethodRef_info`      |     10      |   类中方法的符号引用   |
| `CONSTANT_InterfaceMethodRef_info` |     11      |  接口中方法的符号引用  |
|    `CONSTANT_NameAndType_info`     |     12      |  字段或方法的符号引用  |
|     `CONSTANT_MethodType_info`     |     16      |      标志方法类型      |
|    `CONSTANT_MethodHandle_info`    |     15      |      表示方法句柄      |
|   `CONSTANT_InvokeDynamic_info`    |     18      | 表示一个动态方法调用点 |

对于 CONSTANT_Class_info（此类型的常量代表一个类或者接口的符号引用），它的二维表结构如下：

| 类型 | 名称       | 数量 |
| ---- | ---------- | ---- |
| u1   | tag        | 1    |
| u2   | name_index | 1    |

tag 是标志位，用于区分常量类型；name_index 是一个索引值，它指向常量池中一个 CONSTANT_Utf8_info 类型常量，此常量代表这个类（或接口）的全限定名，这里 name_index 值若为 0x0002，也即是指向了常量池中的第二项常量。

CONSTANT_Utf8_info 型常量的结构如下：

| 类型 | 名称   | 数量   |
| ---- | ------ | ------ |
| u1   | tag    | 1      |
| u2   | length | 1      |
| u1   | bytes  | length |

tag 是当前常量的类型；length 表示这个字符串的长度；bytes 是这个字符串的内容（采用缩略的 UTF8 编码）

`.class` 文件可以通过`javap -v class类名` 指令来看一下其常量池中的信息(`javap -v class类名-> temp.txt`：将结果输出到 temp.txt 文件)。

### 访问标志（Access Flags）

```java
u2             access_flags; // Class 的访问标记
```

在常量池结束之后，紧接着的两个字节代表访问标志，这个标志用于识别一些类或者接口层次的访问信息，包括：这个 Class 是类还是接口，是否为 `public` 或者 `abstract` 类型，如果是类的话是否声明为 `final` 等等。

### 类索引（This Class）、父类索引（Super Class）、接口（Interface）索引集合

```java
u2             this_class; // 当前类
u2             super_class; // 父类
u2             interfaces_count; // 接口数量
u2             interfaces[interfaces_count]; // 一个类可以实现多个接口
```

类索引和父类索引都是一个 u2 类型的数据，而接口索引集合是一组 u2 类型的数据的集合，Class 文件中由这三项数据来确定类的继承关系。类索引用于确定这个类的全限定名，父类索引用于确定这个类的父类的全限定名。

由于 Java 不允许多重继承，所以父类索引只有一个，除了 java.lang.Object 之外，所有的 Java 类都有父类，因此除了 java.lang.Object 外，所有 Java 类的父类索引都不为 0。一个类可能实现了多个接口，因此用接口索引集合来描述。这个集合第一项为 u2 类型的数据，表示索引表的容量，接下来就是接口的名字索引。

类索引和父类索引用两个 u2 类型的索引值表示，它们各自指向一个类型为 CONSTANT_Class_info 的类描述符常量，通过该常量总的索引值可以找到定义在 CONSTANT_Utf8_info 类型的常量中的全限定名字符串。

### 字段表集合（Fields）

```java
u2             fields_count; // 字段数量
field_info     fields[fields_count]; // 一个类会可以有个字段
```

字段表集合存储本类涉及到的成员变量，包括实例变量和类变量，但不包括方法中的局部变量。

每一个字段表只表示一个成员变量，本类中的所有成员变量构成了字段表集合。字段表结构如下：

| 类型 | 名称             | 数量             | 说明                                                         |
| ---- | ---------------- | ---------------- | ------------------------------------------------------------ |
| u2   | access_flags     | 1                | 字段的访问标志，与类稍有不同                                 |
| u2   | name_index       | 1                | 字段名字的索引                                               |
| u2   | descriptor_index | 1                | 描述符，用于描述字段的数据类型。 基本数据类型用大写字母表示； 对象类型用“L 对象类型的全限定名”表示。 |
| u2   | attributes_count | 1                | 属性表集合的长度                                             |
| u2   | attributes       | attributes_count | 属性表集合，用于存放属性的额外信息，如属性的值。             |

- **access_flags:** 字段的作用域（`public` ,`private`,`protected`修饰符），是实例变量还是类变量（`static`修饰符）,可否被序列化（transient 修饰符）,可变性（final）,可见性（volatile 修饰符，是否强制从主内存读写）。
- **name_index:** 对常量池的引用，表示的字段的名称；
- **descriptor_index:** 对常量池的引用，表示字段和方法的描述符；
- **attributes_count:** 一个字段还会拥有一些额外的属性，attributes_count 存放属性的个数；
- **attributes[attributes_count]:** 存放具体属性具体内容。

上述这些信息中，各个修饰符都是布尔值，要么有某个修饰符，要么没有，很适合使用标志位来表示。而字段叫什么名字、字段被定义为什么数据类型这些都是无法固定的，只能引用常量池中常量来描述。

> 字段表集合中不会出现从父类（或接口）中继承而来的字段，但有可能出现原本 Java 代码中不存在的字段，譬如在内部类中为了保持对外部类的访问性，会自动添加指向外部类实例的字段。

### 方法表集合（Methods）

```java
u2             methods_count;//方法数量
method_info    methods[methods_count];//一个类可以有个多个方法
```

methods_count 表示方法的数量，而 method_info 表示方法表。

Class 文件存储格式中对方法的描述与对字段的描述几乎采用了完全一致的方式。方法表的结构如同字段表一样，依次包括了访问标志、名称索引、描述符索引、属性表集合几项。

注意：因为`volatile`修饰符和`transient`修饰符不可以修饰方法，所以方法表的访问标志中没有这两个对应的标志，但是增加了`synchronized`、`native`、`abstract`等关键字修饰方法，所以也就多了这些关键字对应的标志。

### 属性表集合（Attributes）

```java
u2             attributes_count; // 此类的属性表中的属性数
attribute_info attributes[attributes_count]; // 属性表集合
```

在 Class 文件，字段表，方法表中都可以携带自己的属性表集合，以用于描述某些场景专有的信息。与 Class 文件中其它的数据项目要求的顺序、长度和内容不同，属性表集合的限制稍微宽松一些，不再要求各个属性表具有严格的顺序，并且只要不与已有的属性名重复，任何人实现的编译器都可以向属性表中写 入自己定义的属性信息，Java 虚拟机运行时会忽略掉它不认识的属性。

每个属性对应一张属性表，属性表的结构如下：

| 类型 | 名称                 | 数量             |
| ---- | -------------------- | ---------------- |
| u2   | attribute_name_index | 1                |
| u4   | attribute_length     | 1                |
| u1   | info                 | attribute_length |

# 类加载过程

## 类生命周期

类从被加载到虚拟机内存开始，到卸载出内存为止，它的整个生命周期包括以下 7 个阶段：

- 加载（loading）
- 验证（Verification）
- 准备（Preparation）
- 解析（Resolution）
- 初始化（Initialization）
- 使用（Using）
- 卸载（Unloading）

验证、准备、解析 3 个阶段统称为连接。

这七个阶段的顺序图如下：

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-类的生命周期-169293151600533.png)

## 类加载过程

**Class 文件需要加载到虚拟机中之后才能运行和使用，那么虚拟机是如何加载这些 Class 文件呢？**

系统加载 Class 类型的文件主要三步：**加载->连接->初始化**。连接过程又可分为三步：**验证->准备->解析**。

详见：[https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.3](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.3)

### 加载

#### 加载的过程

“加载”是“类加载”过程的一个阶段，不能混淆这两个名词。在加载阶段，虚拟机需要完成 3 件事：

- 通过类的全限定名获取该类的二进制字节流。
- 将二进制字节流所代表的静态结构转化为方法区的运行时数据结构。
- 在内存中创建一个代表该类的 java.lang.Class 对象，作为方法区这个类的各种数据的访问入口。

#### 获取二进制字节流

对于 Class 文件，虚拟机没有指明要从哪里获取、怎样获取。除了直接从编译好的 .class 文件中读取，还有以下几种方式：

- 从 zip 包中读取，如 jar、war 等；
- 从网络中获取，如 Applet；
- 通过动态代理技术生成代理类的二进制字节流；
- 由 JSP 文件生成对应的 Class 类；
- 从数据库中读取，如 有些中间件服务器可以选择把程序安装到数据库中来完成程序代码在集群间的分发。

#### “非数组类”与“数组类”加载比较

- 非数组类加载阶段可以使用系统提供的引导类加载器，也可以由用户自定义的类加载器完成，开发人员可以通过定义自己的类加载器控制字节流的获取方式（如重写一个类加载器的 `loadClass()` 方法）。
- 数组类本身不通过类加载器创建，它是由 Java 虚拟机直接创建的，再由类加载器创建数组中的元素类。

#### 注意事项

- 虚拟机规范未规定 Class 对象的存储位置，对于 HotSpot 虚拟机而言，Class 对象比较特殊，它虽然是对象，但存放在方法区中。
- 加载阶段与连接阶段的部分内容交叉进行，加载阶段尚未完成，连接阶段可能已经开始了。但这两个阶段的开始时间仍然保持着固定的先后顺序。

### 验证

#### 验证的重要性

验证阶段确保 Class 文件的字节流中包含的信息符合当前虚拟机的要求，并且不会危害虚拟机自身的安全。

#### 验证的过程

- 文件格式验证 验证字节流是否符合 Class 文件格式的规范，并且能被当前版本的虚拟机处理，验证点如下：
  - 是否以魔数 0XCAFEBABE 开头。
  - 主次版本号是否在当前虚拟机处理范围内。
  - 常量池是否有不被支持的常量类型。
  - 指向常量的索引值是否指向了不存在的常量。
  - CONSTANT_Utf8_info 型的常量是否有不符合 UTF8 编码的数据。
  - ......
- 元数据验证 对字节码描述信息进行语义分析，确保其符合 Java 语法规范。
- 字节码验证 本阶段是验证过程中最复杂的一个阶段，是对方法体进行语义分析，保证方法在运行时不会出现危害虚拟机的事件。
- 符号引用验证 本阶段发生在解析阶段，确保解析正常执行。
  - 如果无法通过符号引用验证，JVM 会抛出异常，比如：
    - `java.lang.IllegalAccessError`：当类试图访问或修改它没有权限访问的字段，或调用它没有权限访问的方法时，抛出该异常。
    - `java.lang.NoSuchFieldError`：当类试图访问或修改一个指定的对象字段，而该对象不再包含该字段时，抛出该异常。
    - `java.lang.NoSuchMethodError`：当类试图访问一个指定的方法，而该方法不存在时，抛出该异常。
    - ......

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-类加载验证-169293151600534.png)

### 准备

**准备阶段是正式为类变量（或称“静态成员变量”）分配内存并设置类变量初始值的阶段**。这些变量（不包括实例变量）所使用的内存都在方法区中进行分配。

对于该阶段有以下几点需要注意：

1. 这时候进行内存分配的仅包括类变量（ Class Variables ，即静态变量，被 `static` 关键字修饰的变量，只与类相关，因此被称为类变量），而不包括实例变量。实例变量会在对象实例化时随着对象一块分配在 Java 堆中。
2. 从概念上讲，类变量所使用的内存都应当在 **方法区** 中进行分配。不过有一点需要注意的是：JDK 7 之前，HotSpot 使用永久代来实现方法区的时候，实现是完全符合这种逻辑概念的。 而在 JDK 7 及之后，HotSpot 已经把原本放在永久代的字符串常量池、静态变量等移动到堆中，这个时候类变量则会随着 Class 对象一起存放在 Java 堆中。相关阅读：[《深入理解 Java 虚拟机（第 3 版）》勘误#75](https://github.com/fenixsoft/jvm_book/issues/75)
3. 这里所设置的初始值"通常情况"下是数据类型默认的零值（如 0、0L、null、false 等），比如我们定义了`public static int value=111` ，那么 value 变量在准备阶段的初始值就是 0 而不是 111（初始化阶段才会赋值）。特殊情况：比如给 value 变量加上了 final 关键字`public static final int value=111` ，那么准备阶段 value 的值就被赋值为 111。

**基本数据类型的零值**：

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-类型初始值-169293151600535.png)

### 解析

**解析阶段是虚拟机将常量池内的符号引用替换为直接引用的过程。**解析动作主要针对类或接口、字段、类方法、接口方法、方法类型、方法句柄和调用限定符 7 类符号引用进行。

### 初始化

**初始化阶段是执行初始化方法 `<clinit> ()`方法的过程，是类加载的最后一步，这一步 JVM 才开始真正执行类中定义的 Java 程序代码(字节码)。**

> 说明：`<clinit>()` 方法是由编译器自动收集类中的所有类变量的赋值动作和静态语句块（static {} 块）中的语句合并产生的，编译器收集的顺序是由语句在源文件中出现的顺序所决定的。

对于`<clinit> ()` 方法的调用，虚拟机会自己确保其在多线程环境中的安全性。因为 `<clinit> ()` 方法是带锁线程安全，所以在多线程环境下进行类初始化的话可能会引起多个线程阻塞，并且这种阻塞很难被发现。

静态语句块中只能访问定义在静态语句块之前的变量，定义在它之后的变量，在前面的静态语句块中可以赋值，但不能访问。如下方代码所示：

```java
public class Test {
    static {
        i = 0;  // 给变量赋值可以正常编译通过
        System.out.println(i);  // 这句编译器会提示“非法向前引用”
    }
    static int i = 1;
}
```

`<clinit>()` 方法不需要显式调用父类构造器，虚拟机会保证在子类的 `<clinit>()` 方法执行之前，父类的 `<clinit>()` 方法已经执行完毕。

由于父类的 `<clinit>()` 方法先执行，意味着父类中定义的静态语句块要优先于子类的变量赋值操作。如下方代码所示：

```java
static class Parent {
    public static int A = 1;
    static {
        A = 2;
    }
}

static class Sub extends Parent {
    public static int B = A;
}

public static void main(String[] args) {
    System.out.println(Sub.B); // 输出 2
}
```

`<clinit>()` 方法不是必需的，如果一个类没有静态语句块，也没有对类变量的赋值操作，那么编译器可以不为这个类生成 `<clinit>()` 方法。

接口中不能使用静态代码块，但接口也需要通过 `<clinit>()` 方法为接口中定义的静态成员变量显式初始化。但接口与类不同，接口的 `<clinit>()` 方法不需要先执行父类的 `<clinit>()` 方法，只有当父接口中定义的变量使用时，父接口才会初始化。

## 类卸载

**卸载类即该类的 Class 对象被 GC。**

卸载类需要满足 3 个要求:

1. 该类的所有的实例对象都已被 GC，也就是说堆不存在该类的实例对象。
2. 该类没有在其他任何地方被引用
3. 该类的类加载器的实例已被 GC

所以，在 JVM 生命周期内，由 jvm 自带的类加载器加载的类是不会被卸载的。但是由我们自定义的类加载器加载的类是可能被卸载的。

只要想通一点就好了，JDK 自带的 `BootstrapClassLoader`, `ExtClassLoader`, `AppClassLoader` 负责加载 JDK 提供的类，所以它们(类加载器的实例)肯定不会被回收。而我们自定义的类加载器的实例是可以被回收的，所以使用我们自定义加载器加载的类是可以被卸载掉的。

# 类加载器

## 类加载器

### 类加载器介绍

类加载器从 JDK 1.0 就出现了，最初只是为了满足 Java Applet（已经被淘汰） 的需要。后来，慢慢成为 Java 程序中的一个重要组成部分，赋予了 Java 类可以被动态加载到 JVM 中并执行的能力。

根据官方 API 文档的介绍：

> A class loader is an object that is responsible for loading classes. The class ClassLoader is an abstract class. Given the binary name of a class, a class loader should attempt to locate or generate data that constitutes a definition for the class. A typical strategy is to transform the name into a file name and then read a "class file" of that name from a file system.
>
> Every Class object contains a reference to the ClassLoader that defined it.
>
> Class objects for array classes are not created by class loaders, but are created automatically as required by the Java runtime. The class loader for an array class, as returned by Class.getClassLoader() is the same as the class loader for its element type; if the element type is a primitive type, then the array class has no class loader.

翻译过来大概的意思是：

> 类加载器是一个负责加载类的对象。`ClassLoader` 是一个抽象类。给定类的二进制名称，类加载器应尝试定位或生成构成类定义的数据。典型的策略是将名称转换为文件名，然后从文件系统中读取该名称的“类文件”。
>
> 每个 Java 类都有一个引用指向加载它的 `ClassLoader`。不过，数组类不是通过 `ClassLoader` 创建的，而是 JVM 在需要的时候自动创建的，数组类通过`getClassLoader()`方法获取 `ClassLoader` 的时候和该数组的元素类型的 `ClassLoader` 是一致的。

从上面的介绍可以看出:

- 类加载器是一个负责加载类的对象，用于实现类加载过程中的加载这一步。
- 每个 Java 类都有一个引用指向加载它的 `ClassLoader`。
- 数组类不是通过 `ClassLoader` 创建的（数组类没有对应的二进制字节流），是由 JVM 直接生成的。

```java
class Class<T> {
  ...
  private final ClassLoader classLoader;
  @CallerSensitive
  public ClassLoader getClassLoader() {
     //...
  }
  ...
}
```

简单来说，**类加载器的主要作用就是加载 Java 类的字节码（ `.class` 文件）到 JVM 中（在内存中生成一个代表该类的 `Class` 对象）。** 字节码可以是 Java 源程序（`.java`文件）经过 `javac` 编译得来，也可以是通过工具动态生成或者通过网络下载得来。

其实除了加载类之外，类加载器还可以加载 Java 应用所需的资源如文本、图像、配置文件、视频等等文件资源。本文只讨论其核心功能：加载类。

### 类加载器加载规则

JVM 启动的时候，并不会一次性加载所有的类，而是根据需要去动态加载。也就是说，大部分类在具体用到的时候才会去加载，这样对内存更加友好。

对于已经加载的类会被放在 `ClassLoader` 中。在类加载的时候，系统会首先判断当前类是否被加载过。已经被加载的类会直接返回，否则才会尝试加载。也就是说，对于一个类加载器来说，相同二进制名称的类只会被加载一次。

```java
public abstract class ClassLoader {
  ...
  private final ClassLoader parent;
  // 由这个类加载器加载的类。
  private final Vector<Class<?>> classes = new Vector<>();
  // 由VM调用，用此类加载器记录每个已加载类。
  void addClass(Class<?> c) {
        classes.addElement(c);
   }
  ...
}
```

**判断类是否相等**

任意一个类，都由**加载它的类加载器**和这个**类本身**一同确立其在 Java 虚拟机中的唯一性，每一个类加载器，都有一个独立的类名称空间。

因此，比较两个类是否“相等”，只有在这两个类是由同一个类加载器加载的前提下才有意义，否则，即使这两个类来源于同一个 Class 文件，被同一个虚拟机加载，只要加载它们的类加载器不同，那么这两个类就必定不相等。

这里的“相等”，包括代表类的 Class 对象的 `equals()` 方法、`isInstance()` 方法的返回结果，也包括使用 instanceof 关键字做对象所属关系判定等情况。

### 类加载器种类

JVM 中内置了三个重要的 `ClassLoader`：

- 启动类加载器（`Bootstrap ClassLoader`）： 负责将存放在 `<JAVA_HOME>\lib` 目录中的，并且能被虚拟机识别的（仅按照文件名识别，如 rt.jar，名字不符合的类库即使放在 lib 目录中也不会被加载）类库加载到虚拟机内存中。
- 扩展类加载器（`Extension ClassLoader`）： 负责加载 `<JAVA_HOME>\lib\ext` 目录中的所有类库，开发者可以直接使用扩展类加载器。
- 应用程序类加载器（`Application ClassLoader`）： 由于这个类加载器是 ClassLoader 中的 `getSystemClassLoader()` 方法的返回值，所以一般也称它为“系统类加载器”。它负责加载用户类路径（classpath）上所指定的类库，开发者可以直接使用这个类加载器，如果应用程序中没有自定义过自己的类加载器，一般情况下这个就是程序中默认的类加载器。

除了这三种类加载器之外，用户还可以加入自定义的类加载器来进行拓展，以满足自己的特殊需求。就比如说，我们可以对 Java 类的字节码（ `.class` 文件）进行加密，加载时再利用自定义的类加载器对其解密。

除了 `BootstrapClassLoader` 是 JVM 自身的一部分之外，其他所有的类加载器都是在 JVM 外部实现的，并且全都继承自 `ClassLoader`抽象类。这样做的好处是用户可以自定义类加载器，以便让应用程序自己决定如何去获取所需的类。

每个 `ClassLoader` 可以通过`getParent()`获取其父 `ClassLoader`，如果获取到 `ClassLoader` 为`null`的话，那么该类是通过 `BootstrapClassLoader` 加载的。

```java
public abstract class ClassLoader {
  ...
  // 父加载器
  private final ClassLoader parent;
  @CallerSensitive
  public final ClassLoader getParent() {
     //...
  }
  ...
}
```

**为什么 获取到 `ClassLoader` 为`null`就是 `BootstrapClassLoader` 加载的呢？** 这是因为`BootstrapClassLoader` 由 C++ 实现，由于这个 C++ 实现的类加载器在 Java 中是没有与之对应的类的，所以拿到的结果是 null。

下面我们来看一个获取 `ClassLoader` 的小案例：

```java
public class PrintClassLoaderTree {

    public static void main(String[] args) {

        ClassLoader classLoader = PrintClassLoaderTree.class.getClassLoader();

        StringBuilder split = new StringBuilder("|--");
        boolean needContinue = true;
        while (needContinue){
            System.out.println(split.toString() + classLoader);
            if(classLoader == null){
                needContinue = false;
            }else{
                classLoader = classLoader.getParent();
                split.insert(0, "\t");
            }
        }
    }

}
```

输出结果(JDK 8 )：

```text
|--sun.misc.Launcher$AppClassLoader@18b4aac2
    |--sun.misc.Launcher$ExtClassLoader@53bd815b
        |--null
```

从输出结果可以看出：

- 我们编写的 Java 类 `PrintClassLoaderTree` 的 `ClassLoader` 是`AppClassLoader`；
- `AppClassLoader`的父 `ClassLoader` 是`ExtClassLoader`；
- `ExtClassLoader`的父`ClassLoader`是`Bootstrap ClassLoader`，因此输出结果为 null。

### 自定义类加载器

我们前面也说说了，除了 `BootstrapClassLoader` 其他类加载器均由 Java 实现且全部继承自`java.lang.ClassLoader`。如果我们要自定义自己的类加载器，很明显需要继承 `ClassLoader`抽象类。

`ClassLoader` 类有两个关键的方法：

- `protected Class loadClass(String name, boolean resolve)`：加载指定二进制名称的类，实现了双亲委派机制 。`name` 为类的二进制名称，`resolve` 如果为 true，在加载时调用 `resolveClass(Class<?> c)` 方法解析该类。
- `protected Class findClass(String name)`：根据类的二进制名称来查找类，默认实现是空方法。

官方 API 文档中写到：

> Subclasses of `ClassLoader` are encouraged to override `findClass(String name)`, rather than this method.
>
> 建议 `ClassLoader`的子类重写 `findClass(String name)`方法而不是`loadClass(String name, boolean resolve)` 方法。

如果我们不想打破双亲委派模型，就重写 `ClassLoader` 类中的 `findClass()` 方法即可，无法被父类加载器加载的类最终会通过这个方法被加载。但是，如果想打破双亲委派模型则需要重写 `loadClass()` 方法。

## 双亲委派模型

### 介绍

类加载器有很多种，当我们想要加载一个类的时候，具体是哪个类加载器加载呢？这就需要提到双亲委派模型了。

双亲委派模型是描述类加载器之间的层次关系。它要求除了顶层的启动类加载器外，其余的类加载器都应当有自己的父类加载器。（父子关系一般不会以继承的关系实现，而是以组合关系来复用父加载器的代码）

根据官网介绍：

> The ClassLoader class uses a delegation model to search for classes and resources. Each instance of ClassLoader has an associated parent class loader. When requested to find a class or resource, a ClassLoader instance will delegate the search for the class or resource to its parent class loader before attempting to find the class or resource itself. The virtual machine's built-in class loader, called the "bootstrap class loader", does not itself have a parent but may serve as the parent of a ClassLoader instance.

翻译过来大概的意思是：

> `ClassLoader` 类使用委托模型来搜索类和资源。每个 `ClassLoader` 实例都有一个相关的父类加载器。需要查找类或资源时，`ClassLoader` 实例会在试图亲自查找类或资源之前，将搜索类或资源的任务委托给其父类加载器。 虚拟机中被称为 "bootstrap class loader"的内置类加载器本身没有父类加载器，但是可以作为 `ClassLoader` 实例的父类加载器。

从上面的介绍可以看出：

- `ClassLoader` 类使用委托模型来搜索类和资源。
- 双亲委派模型要求除了顶层的启动类加载器外，其余的类加载器都应有自己的父类加载器。
- `ClassLoader` 实例会在试图亲自查找类或资源之前，将搜索类或资源的任务委托给其父类加载器。

下图展示的各种类加载器之间的层次关系被称为类加载器的“**双亲委派模型(Parents Delegation Model)**”。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-类加载器-169293151600536.png)

> 注意 ⚠️：双亲委派模型并不是一种强制性的约束，只是 JDK 官方推荐的一种方式。如果我们因为某些特殊需求想要打破双亲委派模型，也是可以的

另外，类加载器之间的父子关系一般不是以继承的关系来实现的，而是通常使用组合关系来复用父加载器的代码。

```java
public abstract class ClassLoader {
  ...
  // 组合
  private final ClassLoader parent;
  protected ClassLoader(ClassLoader parent) {
       this(checkCreateClassLoader(), parent);
  }
  ...
}
```

在面向对象编程中，有一条非常经典的设计原则：**组合优于继承，多用组合少用继承。

### 执行流程

如果一个类加载器收到了类加载的请求，它首先不会自己去尝试加载这个类，而是把这个请求委派给父类加载器去完成，每一个层次的类加载器都是如此，因此所有的加载请求最终都应该传送到顶层的启动类加载器中，只有当父加载器反馈自己无法完成这个加载请求（找不到所需的类）时，子加载器才会尝试自己去加载。

在 java.lang.ClassLoader 中的 `loadClass` 方法中实现该过程。

```java
protected Class<?> loadClass(String name, boolean resolve)
    throws ClassNotFoundException
{
    synchronized (getClassLoadingLock(name)) {
        // 首先，检查该类是否已经加载过
        Class c = findLoadedClass(name);
        if (c == null) {
            // 如果 c 为 null，则说明该类没有被加载过
            long t0 = System.nanoTime();
            try {
                if (parent != null) {
                    // 当父类的加载器不为空，则通过父类的loadClass来加载该类
                    c = parent.loadClass(name, false);
                } else {
                    // 当父类的加载器为空，则调用启动类加载器来加载该类
                    c = findBootstrapClassOrNull(name);
                }
            } catch (ClassNotFoundException e) {
                // 非空父类的类加载器无法找到相应的类，则抛出异常
            }

            if (c == null) {
                // 当父类加载器无法加载时，则调用findClass方法来加载该类
                // 用户可通过覆写该方法，来自定义类加载器
                long t1 = System.nanoTime();
                c = findClass(name);

                // 用于统计类加载器相关的信息
                sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                sun.misc.PerfCounter.getFindClasses().increment();
            }
        }
        if (resolve) {
            // 对类进行link操作
            resolveClass(c);
        }
        return c;
    }
}
```

### 双亲委派模型的好处

像 java.lang.Object 这些存放在 rt.jar 中的类，无论使用哪个类加载器加载，最终都会委派给最顶端的启动类加载器加载，从而使得不同加载器加载的 Object 类都是同一个。

相反，如果没有使用双亲委派模型，由各个类加载器自行去加载的话，如果用户自己编写了一个称为 java.lang.Object 的类，并放在 classpath 下，那么系统将会出现多个不同的 Object 类，Java 类型体系中最基础的行为也就无法保证。

### 打破双亲委派模型

自定义加载器的话，需要继承 `ClassLoader` 。如果我们不想打破双亲委派模型，就重写 `ClassLoader` 类中的 `findClass()` 方法即可，无法被父类加载器加载的类最终会通过这个方法被加载。但是，如果想打破双亲委派模型则需要重写 `loadClass()` 方法。

为什么是重写 `loadClass()` 方法打破双亲委派模型呢？双亲委派模型的执行流程已经解释了：

> 类加载器在进行类加载的时候，它首先不会自己去尝试加载这个类，而是把这个请求委派给父类加载器去完成（调用父加载器 `loadClass()`方法来加载类）。

# HotSpot虚拟机对象探秘

## 对象的创建

对象的创建尤为重要！！！建议最好掌握

### Step1：类加载检查

虚拟机在解析`.class`文件的时候，如果遇到一条`new`指令，首先将去检查这个指令的参数是否能在常量池中定位到这个类的符号引用，并且检查这个符号引用代表的类是否已经被加载过、解析和初始化过。如果没有，那就必须先执行相应的类加载过程。

### Step2：分配内存

在**类加载检查**通过后，接下来虚拟机将会为新生对象**分配内存**。对象所需的内存大小在类加载完成后便可确定，为对象分配空间的任务等同于把一块确定大小的内存从Java堆中划分出来。**分配方式**有：**指针碰撞**和**空闲列表**两种，**选择那种分配方式由Java堆是否规整确定，而Java堆是否规整又由所采用的垃圾收集器是否带有压缩整理功能决定**。

**内存分配的两种方式**：

- **指针碰撞**
  - 适用场合：堆内存规整（即没有内存碎片）的情况下（说明采用的是**复制算法**或**标记整理法**）。
  - 原理：用过的内存全部整合到一边，没有用过的内存放在另一边，中间有一个分界指针，只需要向着没有用过的内存方向将该指针移动对象内存大小位置即可。
  - 使用该分配方式的GC收集器：Serial，ParNew。
- **空闲列表**
  - 适用场合：堆内存不规整的情况下（说明采用的是**标记清除算法**，有内存碎片）。
  - 原理：虚拟机会维护一个列表，该列表中会记录哪些内存块是可用的，在分配的时候，找一块儿足够大的内存块儿来划分给对象实例，最后更新列表记录。
  - 使用该分配方式的GC收集器：CMS

选择以上两种方式中的哪一种，取决于Java堆内存是否规整。而Java堆内存是否规整，取决于GC收集器的算法是**标记-清除**，还是**标记-整理**，值得注意的是，**复制算法**内存也是规整的。

**内存分配并发问题**：

在创建对象的时候有一个很重要的问题，就是线程安全，因为在实际开发过程中，创建对象是很频繁的事，作为虚拟机来说，必须要保证线程安全，通常来讲，虚拟机采用两种方式来保证线程安全：

- **CAS+失败重试**：CAS是乐观锁的一种实现方式。所谓乐观锁就是，每次不加锁而是假设没有冲突而去完成某项操作，如果因为冲突失败就重试，直到成功为止。**虚拟机采用CAS配上失败重试的方式保证更新操作的原子性**。
- **TLAB**：为每个线程预先在Eden区分配一块儿内存，JVM在给线程中对象分配内存时，首先在TLAB分配，当对象大于TLAB中的剩余内存或TLAB的剩余内存已经用尽时，再采用上述的CAS进行对象分配。

### Step3：初始化零值

内存分配完成后，虚拟机需要将分配到的内存空间都初始化为零值（不包括对象头），这一步操作保证了对象的实例字段在Java代码中可以不赋初始值就直接使用，程序能访问到这些字段的数据类型所对应的零值。

### Step4：设置对象头

初始化零值之后，**虚拟机要对对象进行必要的设置**，例如这个对象是哪个类的实例、如何才能找到类的元数据信息、对象的哈希码、对象的GC分代年龄等信息。**这些信息存放在对象头中**。另外，根据虚拟机当前运行状态的不同，如是否启用偏向锁等，对象头会有不同的设置方式。

### Step5：执行init方法

在上面工作都完成之后，从虚拟机的视角来看，一个新的对象已经产生了，但从Java程序的视角来看，对象创建才刚开始，`<init>`方法还没有执行，所有的字段都还为零。所以一般来说，执行new指令之后会接着执行 `<init>`方法，把对象按照程序员的意愿进行初始化，这样一个真正可用的对象才算完全产生出来。

## 对象的内存布局

在HotSpot虚拟机中，对象的内存布局分为以下3块区域：

- 对象头（Header）
- 实例数据（Instance Data）
- 对齐填充（Padding）

### 对象头

对象头包括两部分信息，**第一部分用于存储对象自身的运行时数据（哈希码，GC分代年龄，锁状态标志等）**，**另一部分是类型指针**，即对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。如果对象是一个数组，那么对象头还会包括数组长度。

### 实例数据

**这部分是对象真正存储的有效信息**，也是在程序中所定义的各种类型的字段内容。

### 对齐填充

**这部分不是必然存在的，也没有什么特别的含义，仅仅起占位作用。**

HotSpot虚拟机的自动内存管理系统要求对象起始地址必须是8字节的整数倍，换句话说就是对象的大小必须是8字节的整数倍。而对象头部分正好是8字节的倍数（1或2倍），因此，当对象实例数据部分没有对齐时，就需要通过对齐填充来补全。

## 对象的访问方式

建立对象就是为了使用对象，我们的Java程序通过栈上的reference数据来操作堆上的具体对象。对象的访问方式由虚拟机实现而定，目前主要是这两种方式：**句柄访问**，**直接指针访问**。

所有对象的存储空间都是在堆中分配的，但是这个对象的引用却是在堆栈中分配的。也就是说在简历一个对象时两个地方都分配内存，在堆中分配的内存实际建立这个对象，而在堆栈中分配的内存只是一个指向这个堆对象的指针（引用）而已。那么根据引用存放的地址类型的不同，对象有不同的访问方式。

### 句柄访问

堆中需要有一块叫做**句柄池**的区间，reference中存储的就是对象的句柄地址，而句柄中包含了对象实例数据与对象类型数据各自的具体地址信息。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-jubing-169293153130341.png)

### 直接指针访问

如果使用直接指针访问，reference中存储的直接就是对象的地址，通过引用能够直接访问对象，但是对象所在的内存空间需要额外的策略存储对象所属的类信息的地址。

![](https://halo-md.oss-cn-guangzhou.aliyuncs.com/halo/jvm-zhijiezhizhen-169293153130342.png)

HotSpot虚拟机主要使用的就是这种方式来进行对象访问。这两种对象访问方式各有优势。使用句柄来访问的最大好处是reference中存储的是稳定的句柄地址，在对象被移动时指挥改变句柄中的实例数据指针，而reference本身不需要修改。使用直接指针访问方式最大的好处就是速度快，它节省了一次指针定位的时间开销。