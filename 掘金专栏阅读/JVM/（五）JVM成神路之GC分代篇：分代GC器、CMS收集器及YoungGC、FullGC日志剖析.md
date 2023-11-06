## 一、堆空间回顾与GC收集器概述

GC覆盖的范围有堆空间与元空间，而主要的作用范围则是堆空间，所以先简单回顾堆空间后，再对于GC中的一些概念进行阐述，有了这些基础后再对GC收集器进行阐述。

### 1.1、堆空间回顾

在前面[《JVM运行时内存区域划分》](https://link.juejin.cn?target=https%3A%2F%2Fwww.jianshu.com%2Fp%2Fcf89ef2689c9)中曾提及过：JVM的堆空间结构会根据运行时具体采用的GC收集器来决定。在所有的GC收集器中，大体会将堆空间分为分代、分区两大类：
 ![堆空间结构](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6d74f8599f5e491984580144cbbbcc72~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)
 如上图，分代堆空间中会分为新生代与年老代两个区域，而新生代又会分为`Eden*1、Survivor*2`三块。其中新生代采用复制算法，HotSpot中因为调整了`Eden`与`Survivor`区域的比例为`8:1:1`，所以说新生代的内存最多浪费`10%`，最大容量为`80%+10%=90%`。而当`Survivor`空间不足以存放存活对象时，会依赖于年老代进行分配担保，承接符合标准的对象进入年老代空间。

### 1.2、GC收集器概述

上篇的垃圾收集相关算法是GC机制的方法论，而垃圾收集器则是GC机制的具体实现。

但在Java的生态中，存在很多款GC收集器，其中并不存在一款最好最优的收集器，也不存在所谓的万能收集器。因为实际开发过程中，我们需要根据项目的业务类型，选出对应用程序而言最合适的收集器即可。

> 不过在了解GC收集器之前，首先得明白几个GC收集器中常见的名词。

#### 1.2.1、GC收集器中的名词解释

在GC收集器中存在一些经常出现的名词，这些名词也是在认识GC收集器之前不得不了解的，如：串行回收、并行回收、独占执行、并发执行、吞吐量、停顿时间、吞吐量优先、响应时间优先等。

##### 串行、并行与独占、并发

- ①串行`Serial`收集：所有用户线程停止，单条GC线程回收堆的情况被称为串行回收。
- ②并行`Parallel`收集：所有用户线程停止，多条GC线程回收堆的情况（需多核CPU支持）。
- ③独占`Monopoly`执行：这里是指GC工作时，GC线程会抢占所有资源执行，整个应用程序会被停止。
- ④并发`Concurrent`执行：这里的并发是指用户线程和GC线程同时（交替）执行的情况，不会停下某类线程。

##### 吞吐量

吞吐量是性能优化中的一个重要指标，它是指CPU用于执行用户代码的时间与CPU总耗时的比值，在Java中，吞吐量的计算公式为：

> 吞吐量 = 用户代码执行总时长 /（用户代码执行总时长 + 垃圾回收总时长）。

如JVM在线上执行了`100min`，其中执行用户代码花费了`99min`，垃圾回收总用时`1min`，那么吞吐量则为`99min/(99min+1min)=99%`。

##### 停顿时间

停顿时间是指GC收集器在工作时，所有用户线程（整个应用程序）的暂停时间。对于独占类的GC收集器而言，停顿时间会比较长。而对于并发类的GC收集器来说，因为GC线程和用户线程是交替执行的，所以程序的停顿时间会缩短，但总体GC效率不如独占GC收集器，因此系统的吞吐量会降低。

基于独占收集器和并发收集器的特性而言，就牵扯出了两个调优时的新名词：**吞吐量优先与响应时间优先。** 相对而言，在设计系统架构选择GC收集器或进行调优时，最终都是在追求更高的吞吐量以及更短的响应时间。

- 吞吐量优先：为了确保程序的更高吞吐，允许GC发生时出现长时间暂停。
- 响应时间优先：为了确保用户更好的体验，可以牺牲一定的吞吐量换取更快的响应速度，发生GC时暂停时间越短越好。

#### 1.2.2、Java中的GC收集器概述

在如今的官方JDK中，JVM的GC收集器具体实现存在十款，分别为`Serial、ParNew、Parallel Scavenge、CMS、Serial Old（MSC）、Parallel Old、G1、ZGC、Shenandoah、Epsilon`等，如下：
 ![Java十款GC收集器](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ad3998af35ea4fc29b7b7985a877aa1b~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)
 在上图中共有十款GC收集器，它们可以根据回收时的属性分为分代和分区两种类型：

- 分代收集器：`Serial、ParNew、Parallel Scavenge、CMS、Serial Old（MSC）、Parallel Old`
- 分区收集器：`G1、ZGC、Shenandoah`

其中`Epsilon`是个例外，这款收集器是JDK11提供的，这款GC收集器俗称为“废物收集器”，装载该收集器的Java程序，在运行期间不会发生任何GC相关的操作，程序所分配的堆空间一旦用完，Java程序就会因`OOM`原因退出。`Epsilon`收集器主要是用于程序上线前做测试使用，如：性能测试、内存压力测试、VM接口测试等。在程序启动时选择装载`Epsilon`收集器，这样可以帮助我们过滤掉GC机制引起的**性能假象**。

而本篇重点是叙述分代GC，所以重点先分析一下分代收集器。六款分代收集器，它们分别作用于不同的区域：

- 新生代收集器：`Serial、ParNew、Parallel Scavenge`
- 年老代收集器：`CMS、Serial Old（MSC）、Parallel Old`

![分代GC收集器](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/55102a653ed142b29a35e8796824a5f7~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)
 如上图所示，两者之间存在连线则代表两个GC收集器可以搭配使用，所以一共存在六种搭配方案：

| 新生代            | 年老代                         |
| ----------------- | ------------------------------ |
| Serial            | CMS（主用）/Serial Old（备用） |
| Serial            | Serial Old（MSC）              |
| ParNew            | CMS（主用）/Serial Old（备用） |
| ParNew            | Serial Old（MSC）              |
| Parallel Scavenge | Serial Old（MSC）              |
| Parallel Scavenge | Parallel Old                   |

在上表中，可以看到`CMS`是可以和`MSC`搭配的，关于具体为何我们后续分析，也包括为什么`Parallel Scavenge`不能和`CMS`进行搭配，后续分析完GC收集器实现后再阐述。

## 二、分代GC收集器详解

JVM中的分代GC收集器，除开被划分为新生代和年老代外，也会根据其收集过程，分为单线程和多线程属性的收集器。其中`Serial、Serial Old（MSC）`属于单线程的收集器，而`ParNew、Parallel Scavenge、CMS、Parallel Old`则属于并发型的多线程收集器。但接下来我们会从分代角度出发，对GC收集器进行全面阐述。

### 2.1、新生代GC收集器详解

前面提到过新生代收集器主要包含`Serial、ParNew、Parallel Scavenge`，首先来看看作用于新生代的`Serial`收集器。

#### 2.1.1、Serial收集器（单线程）

`Serial`是最原始的新生代收集器，同时它属于单线程的GC收集器，所以也被称为串行收集器。顾名思义，它在执行GC工作时，是以单线程运行的，并且该收集器在发生GC时，会产生STW，也就是会停止所有用户线程。但正由于会停止其他用户线程，所以在执行GC时并不会出现线程间的切换。因此，在单颗`CPU`的机器上，它的清理效率非常高。一般来说，采用`Client`模式运行的JVM，选取该款收集器作为内嵌GC是个不错的选择。

> `Serial`收集器小结：
>  启动参数：`-XX:+UseSerialGC`（开启该参数后，年老代会使用`MSC`）。
>  收集动作：串行GC，单线程。
>  采用算法：复制算法。
>  STW：GC过程在STW中执行。
>  GC发生时，执行过程如下：
>  ![Serial收集器执行过程](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/eb47aef4581845ee93ebc829dc6b7f8c~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

因为该款收集器GC过程中是需要全程发生在STW中的，所以基于系统层面来说，对用户体验感欠佳。就好比你在线看片（指电影），看两分钟转几圈，看一段时间后又看圈，反反复复的卡顿....，对于你而言，这显然一件令人难以接受的事情。

#### 2.1.2、ParNew收集器（多线程）

`ParNew`收集器是基于`Serial`收集器的演进版，从严格意义上来看，它可以被称为`Serial`收集器的多线程版本，同样是作用于新生代区域的收集器。在整个实现上，除开GC收集阶段会使用多条线程回收外，其他实现几乎与`Serial`收集器大致相同。

> `ParNew`收集器小结：
>  启动参数：`-XX:+UseParNewGC`。
>  收集动作：并行GC，多线程。
>  采用算法：复制算法。
>  STW：GC过程发生在STW中，采用多线程回收。
>  GC发生时，执行过程如下：
>  ![ParNew收集器执行过程](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/343c52dbf978400ebd68012fd349d68d~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

因为该款收集器与`Serial`唯一的不同点就在于使用了多线程，所以GC发生时仍旧会造成程序停顿。但也因为使用了多线程回收，因此能够在很大程度上缩短系统的停顿时间，从而能够带来比`Serial`更好的用户体验。

但该款GC收集器因为采用了多线程，所以需要多核CPU的支持，该收集器会根据**CPU**核数，开启**不同的GC线程数**，从而达到**最优**的垃圾回收效果（也可以通过`-XX:ParallelGCThreads`参数指定）。但如若是单核的机器上运行时，其效率可能还不如`Serial`。

> 一般如果你的程序是以`Server`模式运行的程序，而老年代又采用了`CMS`收集器，那么新生代搭配`ParNew`是个不错的选择。

#### 2.1.3、Parallel Scavenge收集器（多线程）

`Parallel Scavenge`同样是一款作用于新生代的多线程GC收集器，但与`ParNew`收集器不同的是：`ParNew`通过控制GC线程数量来缩短程序暂停时间，更关心程序的**响应时间**，而`Parallel Scavenge`更关心的是**程序运行的吞吐量**，也就是更注重一段时间内，用户代码执行时长与程序执行总时长的占比。

> `Parallel Scavenge`收集器小结：
>  启动参数：`-XX:+UseParallelGC`。
>  收集动作：并行GC，多线程。
>  采用算法：复制算法。
>  STW：GC过程发生在STW中，采用多线程回收。
>  GC发生时，执行过程如下：
>  ![Parallel Scavenge收集器执行过程](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/99296716ec6a4d50b822511505343036~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

从上述小结来看，`PS`收集器和`ParNew`收集器好像并未有太大的区别。但实际上它们两者之间基于的底层GC框架完全不同，同时关注的方向也完全不同。`PS`收集器的目标是让程序达到一个可控制的吞吐量（`Throughput`），所以`PS`也被称为**吞吐量优先**的垃圾收集器。

> `PS`收集器可以通过`-XX:MaxGCPauseMillis`与`-XX:GCTimeRatio`参数精准控制GC发生时的时间以及吞吐量占比。同时与`ParNew`收集器最大的不同在于：`PS`收集器还可以通过开启`-XX:+UseAdaptiveSizePolicy`参数，让JVM启动自适应的GC调节策略，开启该参数后，JVM会根据当前系统的运行状态调整吞吐比与GC时间，从而确保能够提供最合适的停顿时间和吞吐量。

- 那如果使用`PS`收集器的时候，我们通过参数手动将GC时间设的很小，然后将吞吐占比设的很高，岂不是GC回收会变得非常完美？
- 答案是：并非如此。因为在追求响应时间的时候必然会牺牲吞吐量，而追求吞吐量的同时必然会牺牲响应时间。好比你通过参数将GC时间设置的很小，那么`PS`在运行时会将新生代空间调小，如从原本的`1GB`调整到`800MB`，收集`800MB`的空间必然速度会比`1GB`的快很多。但与之相对应的收集频率会增高，可能原本原来`60s`收集一次，每次收集停顿`100ms`，而现如今内存被调小后，`40s`就要发生一次GC，每次GC停顿`80ms`，你可以对比这两者之间的区别：
- `24min/1GB`空间-GC开销：`(24min/60s)*100ms=24000ms`
- `24min/800MB`空间-GC开销：`(24min/40s)*80ms=28800ms`
- 因此，最终可以得到一个结果，虽然响应时间确实降低了，但吞吐量也降了下来了。

> 所以一般线上情况，对于调优没有丰富经验的情况下，我们不应该自己去手动调整这些参数，而是开启JVM的自适应策略，由JVM自行调整。

### 2.2、年老代GC收集器详解

年老代收集器主要有`CMS、Serial Old（MSC）、Parallel Old`三款，与新生代的收集器一样，同样存在单线程和多线程收集器之分，接下来我们对年老代收集器进行依次分析。

#### 2.2.1、Serial Old（MSC）收集器（单线程）

`Serial Old（MSC）`与`Serial`收集器相同，同样是一款单线程串行回收的收集器，但不同的是：`MSC`是一款作用于年老代空间的收集器，它采用标记-整理算法对年老代空间进行回收。同时，该款收集器也可作为`CMS`的备用收集器使用。

> `Serial Old（MSC）`收集器小结：
>  启动参数：`-XX:+UseSerialGC`（开启该参数后，新生代会使用`Serial`）。
>  收集动作：串行GC，单线程。
>  采用算法：标记-整理算法。
>  STW：GC过程发生在STW中，采用单线程执行串行回收。
>  GC发生时，执行过程如下：
>  ![Serial Old（MSC）收集器执行过程](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/df280e838860431da76a0783b7e02a24~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

`Serial Old（MSC）`与新生代收集器`Serial`差距不大，回收过程也是采用单线程做串行收集，属于`Serial`的年老代版本。

#### 2.2.2、Parallel Old收集器（多线程）

`Parallel Old`则是`Parallel Scavenge`收集器的年老代版本，同样采用多线程进行并行收集，其内部采用标记-整理算法。与新生代的`PS`收集器相同的是：`PO`同样追求的是**吞吐量优先**。

> `Parallel Old`收集器小结：
>  启动参数：`-XX:+UseParallelOldGC`。
>  收集动作：并行GC，多线程。
>  采用算法：标记-整理算法。
>  STW：GC过程发生在STW中，采用多线程回收。
>  GC发生时，执行过程如下：
>  ![Parallel Old收集器执行过程](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c53701ae8b9647e39161657c00f38a18~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

`PO`作为`PS`收集器的年老代版本，其特性与`PS`大致相同，所以该款收集器同样适用于**注重吞吐量或对CPU资源敏感**的系统。

#### 2.2.3、CMS收集器（多线程/并发）

`CMS`收集器全称为`ConcurrentMarkSweep`，该款回收器是GC机制中的一座里程碑，在该款收集器中首次实现了并发收集的概念，也就是不停止用户线程，GC线程与用户线程一同工作的情况。同时该款收集器追求的是**最短的回收时间**，属于多线程收集器，其内部采用标记-清除算法。

> `CMS`收集器小结：
>  启动参数：`-XX:+UseConcMarkSweepGC`。
>  收集动作：并发GC，多线程并行执行。
>  采用算法：标记-清除算法。
>  STW：GC过程会发生STW，但并非整个GC过程都在STW中执行，采用多线程回收。
>  GC发生时，执行过程如下：
>  ![CMS收集器执行过程](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/11063ea890394b01bd0761e2c7ae848d~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

从上面的CMS执行图中可以明确看出，CMS对比其他的GC收集器，回收过程明显复杂很多，CMS收集器的回收工作会分为四个步骤：初始标记、并发标记、重新标记以及并发清除。

- ①初始标记：仅标记`GcRoot`节点直接关联的对象，该阶段速度会很快，需在`STW`中进行。
- ②并发标记：该阶段主要是做GC溯源工作（`GcTracing`），从根节点出发，对整个堆空间进行可达性分析，找出所有存活对象，该阶段的GC线程会与用户线程同时执行。
- ③重新标记：这个阶段主要是为了修正“并发标记”阶段由于用户线程执行造成的GC标记变动的那部分对象，该阶段需要在`STW`中执行，并且该阶段的停顿时间会比初始阶段要长不少。
- ④并发清除：在该阶段主要是对存活对象之外的垃圾对象进行清除，该阶段不需要停止用户线程，是并发执行的。
- PS：其实在并发标记和重新标记中间存在两步细节操作：预清理以及可终止的预清理。

在整个收集过程中，除开初始标记与重新标记阶段，其他的收集动作都是与用户线程并发执行的。因此，CMS收集器在发生GC时，造成的程序暂停是非常短暂的，对于用户体验感而言，相对比之前的收集器而言是**最优者**。也正由于CMS收集器**并发收集、停顿延迟低**的特性，所以在有些地方也被称为**并发低停顿收集器**。

> 从如上的总结看来，CMS好像很不错哎~，但实际上，CMS也存在几个致命的缺点：会产生且无法回收浮动垃圾、对CPU资源非常依赖、GC完成后会造成大量内存碎片。

- ①CMS是一款完全基于多线程环境研发的收集器，默认情况下，回收过程中开启的线程数为`(CPU核数+3)/4`，也就代表着：一台八核的机器至少要开启`2~3`条GC线程。而当CPU核数少于`4`时，CMS的GC线程则会对用户线程性能造成很大影响，因为需要让出一半的CPU运算资源去执行GC回收工作。
- ②由于CMS收集器的回收工作是并发清除垃圾对象的，因此，在清除阶段用户线程依旧在执行，而用户线程执行就必然会造成新的垃圾产生，但这部分新产生的垃圾对象是无法标记的，所以只能等到下次GC发生时才可回收，而这部分垃圾则被称为“**浮动垃圾**”。
- ③因为CMS采用的是标记-清除算法，所以在回收工作结束之后会造成大量的内存碎片。
  - 为何不采用标-整算法呢？因为CMS是并发执行的，所以如果将存活对象压缩到内存一端，那么用户线程中的所有对象引用都需改变，实现起来及其复杂且影响效率。

因为CMS在回收时会产生浮动垃圾以及内存碎片，所以CMS一般来说都必须要要搭配一款其他的收集器作为后备方案，而可选项有且只有一个：那就是`Serial Old（MSC）`，当内存太过碎片化导致无法分配新对象时，或回收一次后存活对象+浮动垃圾占比达到指定阈值时则会触发`Serial Old（MSC）`收集器回收。
 决定着是否触发`Serial Old（MSC）`的关键参数有三个：

- `-XX:CMSInitIatingOccupancyFaction`：需要指定一个百分比，当存活对象+浮动垃圾占比达到该值时会触发`MSC`工作。
- `XX:UseCMSCompactAtFullCollection`：该参数默认开启，当内存太过碎片化导致无法分配新对象时，触发`MSC`发生`FullGC`。
- `XX:CMSFullGCsBeforeCompaction`：该参数可以设置间隔多少次`FullGC`后发生一次整理内存碎片的`FullGC`（`MSC`的GC），默认为`0`，既每次`FullGC`都会触发`MSC`回收。

### 2.3、分代GC收集器总结

就目前而言，分析过的GC收集器中，根据分代特征，可分为新生代、年老代收集器。基于线程角度出发，则可分为单线程串行、多线程并行收集器。而从关注度来看，又可分为吞吐量优先、响应时间优先两大类。

一般而言，如果你的程序是更为关注用户体验度，那么可以采用**响应速度优先**的收集器工作，因为该类收集器造成的程序暂停不会很久。但如若你的程序不需要与用户有特别多的交互，如批量处理、订单处理、报表计算、科学计算等类型的后台系统，那你则可以采用**吞吐量优先**的收集器，因为高吞吐量可以高效率地利用CPU资源。

## 三、收集器组合方案、CMS三色标记与跨代引用

### 3.1、GC组合方案分析

在第二个段落中，我们详细分析了JVM中每款不同的GC收集器，但在实际开发过程中，我们的程序采用哪个组合更好呢？其实并不存在所谓的最好组合，你要选择那套组合作为Java程序的收集器，更多的需根据具体的业务场景来决定。

> 如果你的程序追求低延迟，用户交互度较为频繁，那你可以采用`ParNew + CMS`组合（这也是淘宝早期的选择，但后面采用了自研JVM）。

> 如若你的程序追求高吞吐，后台计算工作较多，那么`Parallel Scavenge + Parallel Old`这组`PS+PO`的收集器会更适合你。

> 但你的程序写出来后，更多的情况下部署在单核或双核的机器时，那么最经典的`Serial + Serial Old`组合绝对是你的最佳选择。

![Java中的分代收集器](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/78fe7502d89e495792aaa4cc0c326b1e~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)
 我们再一次将目光聚集在这张图上，需要值得注意的是：在JDK1.8之前，可以采用虚线组合，但在JDK1.8之后，取消了上图中红线的组合，被视为弃用的收集器组合（但如果要用，也是可以用的）。到了JDK1.9时，红线组合被移除，也就代表着在1.9中无法再指定红线组合作为收集器使用。而到了后面的JDK14时，绿线组合也被弃用，同时官方也移除了`CMS`收集器，为了给`G1`铺路，使用`G1`代替了`CMS`。

#### 3.1.1、为何PS收集器不能和CMS收集器搭配使用？

因为在HotSpot中，底层存在一个分代GC的框架，`Serial/SerialOld/ParNew/CMS`都是基于该框架实现的，而在该框架内的新生代收集器和年老代收集器是可以相互之间搭配使用的，这也是所谓的`mix-and-match`规则。但PS收集器在实现时，发现原本的分代GC框架并不适用，则最终采用了自己的特殊框架进行了实现，所以`PS`收集器并不在前面所说的那个分代GC框架中。因此，`PS`不能跟使用了那个框架的`CMS`搭配使用。

### 3.2、三色标记算法

三色标记算法是自CMS收集器后，应用比较广泛的一种并发标记算法，它可以让JVM在发生GC时，只发生短暂的`STW`即可实现存活对象标记的一种算法。JVM中的`CMS`以及后续的不分代收集器，之所以可以做到低延迟的根本原因便在于此处。

> 三色标记思想：在该算法中，将对象分为了黑、白、灰三种颜色，释义如下：
>  黑：已经被标记完成，且依旧存活的对象。
>  灰：当前对象已经被标记完成，但关联节点（属性成员）还未标记的对象。
>  白：未曾标记过的对象，或不具备引用的对象（垃圾对象）。

#### 3.2.1、三色标记执行过程

废话不多说，先上一张三色标记的执行过程图：
 ![三色标记算法](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d6d1e4af71594395abb19f01b12e3748~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

- 实现了三色标记算法的GC收集器，在启动时会分别创建：黑、白、灰三个集合，在最开始所有的对象都在白色集合中。
- 在GC发生时，发生短暂的`STW`，将所有与`GcRoots`直接相连的对象转入灰色集合中。
- 之后并发执行，对灰色集合中的对象进行遍历，根据可达性分析算法进行对象存活标记，当一个对象的所有成员全部被标记完成后，该对象则会被移入到黑色集合中。同时，也会将该对象中被标记的成员从白色集合移入灰色集合中。
- 不断重复上一步操作，直至灰色集合彻底没了对象为止。
- 标记完成所有对象后，再次触发`STW`，通过`write-barrier`写屏障检测对象是否有变化，如果发生了改变则重新标记，纠正并发标记期间的“误标”。
- 并发执行清除工作，将白色集合中的所有对象全部回收（因为根据`GCRoots`节点进行可达性分析后，所有的存活对象都会从白色集合移入到黑色集合中，所以依旧留在白色集合中的对象必然为垃圾对象，这些对象就是需要被回收的对象）。
- 最终等待清除工作完成后，代表着整个GC过程结束，再把标记复位，将所有的对象再次放入白色集合中，等待迎接下次GC的到来。

#### 3.2.2、三色标记-并发标记导致的错标问题

采用三色标记算法的GC收集器为了追求低延迟，一般在标记完`GCRoots`直接关联的对象后，就会结束`STW`，转而采取**并发标记**的手段对其他对象进行标记。但因为并发标记是GC线程与用户线程一起工作的，所以很有可能导致出现如下情况：

> 被标记的黑色对象中，突然断开了对另一个对象的引用，导致另外一个原本已经被标记为黑色的对象突然变为了垃圾。

但是因为该对象已经被标记了，所以收集器不会对该对象进行再次标记，而等到清除工作发生时，因为当前这个对象在最初是被标记为了黑色，所以收集器也不会回收它。这种情况则被称为三色标记导致的“错标/误标/多标”，也被称为并发标记产生的浮动垃圾。

对于该问题而言并非什么大事，因为这次错标产生的浮动垃圾，在下次GC时依旧会被回收，正所谓“躲得过初一，躲不过十五”，是垃圾早晚都会被“干掉”，这点在JVM中是毋庸置疑的，因此这个问题不必太过留意。

#### 3.2.3、三色标记-并发执行导致的漏标问题

假设在执行三色标记的过程中，出现了如下情况：

> ①一条用户线程在执行过程中，断开了一个未标记的白色对象连接，然后该对象又被一个已经标记成黑色的对象建立起了引用连接。如下图：
>  ![三色标记-漏标问题-情况①](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b15713c2b54c4f6c950f9606b606e347~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)
>  白色对象断开了左侧灰色对象的引用，又与右侧的黑色对象建立了新的引用关系。

> ②一条用户线程在执行过程中，正好在GC线程标记时，将一个灰色对象与一个未标记的白色对象之间的引用连接断开了，然后当GC标记完成这个灰色对象，将其标记为黑色后，之前断开的白色对象又重新与之建立起了引用关系。如下图：
>  ![三色标记-漏标问题-情况②](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/37ea61cc5a514537b83f6510f51d2af6~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)
>  GC标记前，白色对象断开了与灰色对象的引用，四秒钟之后GC标记灰色对象完成，而此时恰巧白色对象又重新与标记结束后成为黑色的对象重新建立了引用关系。

而当出现这两种情况时，因为重新建立引用的白色对象“父节点”已经被标记黑色了，所以GC线程不会再次标记该对象以及其成员对象，所以这些白色对象会被一直停留在白色集合中。最终导致的结果就是这些依旧存在引用的存活对象会被“误判”为垃圾对象清除掉。而这种情况会直接影响到应用程序的正确性，是不可接受的。

> 先来思考一下引起漏标问题的原因：
>  条件一：灰色对象断开了与白色对象的引用（直接引用或间接引用都可）。
>  条件二：已经标为黑色的对象重新与白色对象建立了引用关系。
>  只有当一个对象同时满足了如上两个条件时才可发生漏标问题。
>  上个简单的代码案例理解一下：

```java
java复制代码Object X = obj.fieldX; // 获取obj.fieldX成员对象
obj.fieldX = null; // 将原本obj.fieldX的引用断开
objA.fieldX = X; // 将断开引用的X白色对象与黑色对象objA建立引用
```

从如上代码角度来看，假设`obj`是一个灰色对象，此时先获取它的成员`fieldX`并将其赋值给变量`X`，让其堆中实例与变量`X`保持着引用关系。紧接着再将`obj.fieldX`置空，断开与`obj`对象的引用关系，最后再与黑色对象`objA`建立起引用关系，最终关系如下：

> 灰色对象`obj`，白色对象`obj.fieldX/X`，黑色对象`objA`。
>  白色对象`X`在GC机制标记灰色对象`obj`成员属性之前，与灰色对象断开了引用，然后又“勾搭”上了黑色对象`objA`，此刻白色对象`X`就会被永远停留在白色集合中，直至清除阶段到来，被“误判”为垃圾回收掉。

其实解决漏标问题的思路也挺简单的，和之前[《并发编程》](https://link.juejin.cn?target=https%3A%2F%2Fwww.jianshu.com%2Fp%2F1a21a0a6c6be)中解决线程安全问题一样，线程安全问题是存在三个必要条件的，破坏掉其中任意条件后，线程安全问题就不会出现。而刚刚前面也分析过，对象漏标的问题也存在两个必要条件，那么我们也只需要破坏掉其中任意条件即可。比如上述案例中，我们只要能够**通过特殊手段记录一下`X`对象，然后将它作为灰色对象再遍历标记一次**即可。

- 采用三色标记算法的收集器又是如何具体解决漏标问题的呢？
- CMS：增量更新 + 写屏障
- G1：STAB + 写屏障
- ZGC：读屏障

在本篇中，先对`CMS`解决漏标的方案进行分析，对于`G1、ZGC`收集器的漏标问题解决则放到下篇文章中进行阐述。

#### 3.2.4、CMS解决漏标问题：增量更新 + 写屏障

在了解写屏障之前，我们首先来看看HotSpot中为对象成员赋值的实现，大体逻辑如下：

```c++
c++复制代码void oop_field_store(oop* field, oop new_value) { 
    *field = new_value; // 赋值操作：新值替换老值
} 
```

而所谓的写屏障，则是指在赋值操作前后加入一些逻辑处理（类似于SpringAOP面向切面前后置处理的思想），如下：

```c++
c++复制代码void oop_field_store(oop* field, oop new_value) {
    pre_write_barrier(field); // 写前屏障
    *field = new_value; // 赋值操作：新值替换老值
    post_write_barrier(field, value);  // 写后屏障
} 
```

而CMS收集器则是通过在写屏障的后置处理中，实现了增量更新的逻辑，从而解决了漏标问题。

增量更新（`Increment Update`）是专门针对于对象新增引用的，当一个未标记的白色对象被其他对象重新引用时，这个白色对象会被记录下来，如下：

```c++
c++复制代码// 写后屏障
void post_write_barrier(oop* field, oop new_value) {  
  if($gc_phase == GC_CONCURRENT_MARK && !isMarkd(field)) {
      remark_set.add(new_value); // 记录新引用的对象（白色对象）
  }
}
```

从如上源码中可以观察出：对于赋值的新增引用，会在写后屏障中会被放到一个特定的集合记录，等并发标记阶段的`GCRoots`遍历标记完成后，在重新标记阶段会去找到集合里面的引用，再把源头标记为灰色，然后重新去扫描标记这些对象。

> CMS通过写屏障+增量更新这种手段，破坏了之前分析漏标问题时的第二个条件：已经标为黑色的对象重新与白色对象建立了引用关系。
>  通过增量更新的手段，会将这些重新建立了引用的“源头”再次恢复为灰色对象，然后在重新标记阶段会再次标记，同时为了避免重新标记阶段时再次发生漏标问题，所以重新标记阶段是必须要发生STW的。
>  HotSpot中写屏障的具体实现可参考：[《BarrierSet源码分析》](https://link.juejin.cn?target=https%3A%2F%2Fblog.csdn.net%2Fqq_31865983%2Farticle%2Fdetails%2F103667290)。

### 3.3、跨代引用

跨代引用是指年老代空间中的对象引用了新生代的对象，或者新生代中的对象引用了年老代中的对象。面对这种情况，在进行可达性分析扫描存活对象时，不可能从新生代一直扫描至年老代的，因为这样就会出现整堆扫描的情况，效率必然会很低。

在HotSpot虚拟机中，为了解决跨代引用的问题，会专门在内存中开辟一块小空间用于维护这些特殊的引用，从而达到让GC不必扫描整个堆空间的目的。而开辟的这块小空间则被称为**记忆集、卡表**。

#### 3.1、记忆集（Remember Set）

我们都知道在发生新生代GC时都会通过根可达算法先判断垃圾对象，之后再对非存活对象进行统一回收，但是如果有年老代对象引用了新生代对象，那么根据根可达算法的特性，年老代也会被加入扫描范围，这样下来一次新生代的GC代价太大。所以为了解决跨代引用的问题，在新生代引入了记录集的数据结构，记录从非收集区到收集区的引用指针集合，避免在通过根可达算法判断对象存活时把整个老年代加入扫描范围。

> GC时，GC收集器只需通过记忆集判断出某一块非收集区域是否存在指向收集区域的指针即可，无需进行详细的根搜索过程。
>  记忆集可根据不同的记忆粒度实现：
>  ①字宽/字长精度：精确到每个字宽(32bit/64bit)，每一个跨代引用指针
>  ②对象精度：精确到每个对象，对象的字段中包含跨代引用指针
>  ③卡精度：精准到每一块内存区域，内存区域中有对象存在跨代指针

#### 3.2、卡表（Card Table）

卡表是记忆集第三种精度的实现，也是HotSpot虚拟机中记忆集的实现方式，卡表中记录中记忆集的记录精度、与堆内存区域的映射关系等。

> 在HotSpot中卡表是使用一个字节数组实现：`CARD_TABLE[this addredd >>9]=0`，数组中每个元素对应着其标识的内存区域，称为卡页，hotSpot使用的卡页大小为2^9 即512字节，也就是说内存中每连续的512字节会被当作一个卡页作为卡表的一个元素。

如果有年老代的对象引用了新生代的对象，那么该对象所在区域对应的卡页元素设置为1，反之则为0，不过要注意的是：**`CMS`的卡表位于老年代**。(G1以后的GC收集器不分代，所以G1以后的记忆集不是通过数组实现的，而是通过哈希表结构实现)。

> JVM对于卡页的维护也是通过写屏障的方式。

## 四、GC日志解读

对于GC机制而言，这块区域是程序员做JVM调优的关键，而调优前必然得读懂GC发生后产生的日志。在JVM中GC日志相关的参数如下：

- ①`-XX:+PrintGC`或`-verbose:gc`：打印GC日志
- ②`-XX:+PrintGCDetails`：打印GC的详细日志
- ③`-XX:+PrintGCTimeStamps`：输出GC的时间戳（以基准时间的形式）
- ④`-XX:+PrintGCDateStamps`：输出GC的时间戳（以日期的形式）
- ⑤`-XX:+PrintHeapAtGC`：在发生GC的前后打印出堆的信息
- ⑥`-Xloggc:/xxx/xxx/xx.log`：GC日志文件的保存路径

其中`-XX:+PrintGC`或`-verbose:gc`参数只能输出GC时堆空间总体的变化信息，来个简单的案例理解一下：

```java
java复制代码// 启动参数：-Xms8M -Xmx8M -XX:+PrintGC
public class GC {
    static void newObject(){
        for (int i = 0; i <= 10000; i++)
            new Object();
    }

    public static void main(String[] args) throws InterruptedException {
        for (;;){
            newObject();
        }
    }
}
```

执行上述案例后，你的控制台中会得到如下日志：

```java
java复制代码[GC (Allocation Failure)  1527K->868K(7680K), 0.0011957 secs]
[GC (Allocation Failure)  1924K->1201K(7680K), 0.0032349 secs]
......
```

我们从输出的日志中随意找出一条来用于分析，如下：

> [GC[1] (Allocation Failure)[2]  1527K[3]->868K[4](7680K)[5], 0.0011957 secs[6]]

该日志只会大概的将堆空间的总体情况打印出来，日志信息解读如下：

- [1]

  ：此次GC的类型

  - `GC`：表示`Young GC`，新生代发生的GC类型
  - `Full GC`：全局GC，新生代、年老代以及元空间的GC类型

- [2]

  ：此次GC产生的原因

  - `Allocation Failure`：新创建的对象分配失败导致的GC
  - `Metadata GC Threshold`：元空间数据达到分配的空间阈值导致的GC
  - `System.gc()`：程序中手动通过`System.gc()`触发的GC
  - ......

- [3]：GC发生前，堆的已用空间大小

- [4]：GC发生后，堆的已用空间大小

- [5]：堆空间的总大小

- [6]：GC持续的时间

如下图：
 ![-XX:+PrintGC日志解读](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ef0eb460cf514d35a4c9a6a0413e1314~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

> 整条GC日志的规律为：GC类型+GC原因+堆空间描述+耗时描述。

### 4.1、GC日志详细信息解读

在前面提到过`-XX:+PrintGC`参数只能输出GC时堆的总体变化信息，这种日志对于线上遇到突发状况而言，几乎是很难从中获取到有用信息的。因此，一般而言线上都会采用`-XX:+PrintGCDetails`参数获取GC的详细日志信息。案例如下：

```java
java复制代码// 启动参数：-Xms8M -Xmx8M -XX:+PrintGCDetails
public class GC {
    // 作为GC Roots
    static List<Object> listObject = new ArrayList<>();
    
    // 往新生代空间中填充对象
    static void newObject(){
        for (int i = 0; i <= 100000; i++)
            new Object();
    }
    
    // 创建的对象与GCRoots保持引用，足以对象晋升年老代空间
    static void oldObject(){
        for (int i = 0; i <= 10000; i++)
            listObject.add(new Object());
    }

    public static void main(String[] args) throws InterruptedException {
        for (;;){
            newObject();
            oldObject();
        }
    }
}
```

运行上述程序后可以得到如下日志信息（为了方便观察已手动排版）：

```java
java复制代码[GC (Allocation Failure) [PSYoungGen: 1527K->492K(2048K)]
    1527K->892K(7680K), 0.0038507 secs] 
    [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 1548K->483K(2048K)]
    1948K->1174K(7680K), 0.0009940 secs] 
    [Times: user=0.00 sys=0.00, real=0.00 secs] 
    
省略大部分相同类型的日志.......

[Full GC (Ergonomics) [PSYoungGen: 2016K->0K(2048K)] 
    [ParOldGen: 4822K->4807K(5632K)] 6839K->4807K(7680K), 
    [Metaspace: 3625K->3625K(1056768K)], 0.0393051 secs] 
    [Times: user=0.06 sys=0.00, real=0.04 secs]
[Full GC (Allocation Failure)[PSYoungGen: 693K->693K(2048K)] 
    [ParOldGen: 5245K->5226K(5632K)] 5938K->5919K(7680K), 
    [Metaspace: 3626K->3626K(1056768K)], 0.0312005 secs] 
    [Times: user=0.03 sys=0.00, real=0.03 secs]
Heap
 PSYoungGen total 2048K, used 754K [0x00000000ffd80000,...)
  eden space 1536K, 49% used [0x00000000ffd80000,...)
  from space 512K, 0% used [0x00000000fff00000,...)
  to   space 512K, 0% used [0x00000000fff80000,...)
 ParOldGen total 5632K, used 5226K [0x00000000ff800000,...)
  object space 5632K, 92% used [0x00000000ff800000,...)
 Metaspace used 3657K, capacity 4540K, committed 4864K, reserved 1056768K
  class space used 402K, capacity 428K, committed 512K, reserved 1048576K
```

观察如上GC日志可以看出：在该程序运行之后，除开触发了新生代GC外，在后期随着存活的对象越来越多，最终也触发了`FullGC`。

> 同时在日志的最后，也会将每个Java内存空间中的占用情况显示出来，如新生代中`eden、form、to`区占用情况，年老代空间占用情况，元数据空间占用情况等。

接下来我们从普通GC日志出发，对上述日志中的信息进行阐述。

#### 4.1.1、YoungGC日志详解

先从上述日志中摘录一条普通GC日志下来：

> [GC[1] (Allocation Failure) [2][PSYoungGen[3]: 1527K[4]->492K[5](2048K[6])]
>  1527K[7]->892K[8](7680K[9]), 0.0038507 secs[10]]
>  [Times: user=0.00[11] sys=0.00[12], real=0.00 [13]secs]

对于这条GC日志解读如下：

- [1]：此次GC的类型（普通的`Young GC`）

- [2]：此次GC产生的原因（分配失败导致的GC）

- [3]：负责此次GC的收集器与GC类型（PS的新生代GC）

- [4]：GC发生前，新生代空间的已用大小（`1527KB`）

- [5]：GC回收后，新生代空间的已用大小（`492KB`）

- [6]：新生代空间分配到的总大小（`2048KB`）

- [7]：GC发生前，Java堆空间的已用大小（`1527KB`）

- [8]：GC回收后，Java堆空间的已用大小（`892KB`）

- [9]：Java堆空间分配到的总大小（`7680KB`）

- [10]：本次GC过程的总耗时（`0.0038507`秒）

- [11]

  ：本次GC过程的用户耗时（

  ```
  0
  ```

  ）

  - 这里是因为太短暂了，因此无法精准出具体的耗时，而并非真的为0。

- [12]：本次GC过程的系统耗时（`0`）

- [13]：本次GC过程的实际耗时（`0`）

![YoungGC日志详解](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4ff863df43f147778ca33f922b493a62~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

> 整条`YoungGC`的日志如上图所示，其中规律为：
>  GC类型+GC原因+GC收集器+新生代描述+堆空间描述+耗时描述。

#### 4.1.2、FullGC日志详解

同样的再摘录一条FullGC日志，如下：

> [Full GC[1] (Ergonomics[2]) [PSYoungGen[3]: 2016K[4]->0K[5](2048K)[6]]
>  [ParOldGen:[7] 4822K[8]->4807K[9](5632K)[10]] 6839K[11]->4807K[12](7680K)[13],
>  [Metaspace:[14] 3625K[15]->3625K[16](1056768K)[17]], 0.0393051 secs[18]]
>  [Times: user=0.06[19] sys=0.00[20], real=0.04 secs[21]]

- [1]：此次GC的类型（全局的`Full GC`）
- [2]：此次GC产生的原因（预计下次分配存放不下触发的GC）
- [3]：负责此次新生代GC的收集器（PS）
- [4]：GC发生前，新生代空间的已用大小（`2016KB`）
- [5]：GC回收后，新生代空间的占用大小（`0KB`）
- [6]：新生代空间分配到的总大小（`2048KB`）
- [7]：负责此次年老代GC的收集器（PO）
- [8]：GC发生前，年老代空间的已用大小（`4822KB`）
- [9]：GC回收后，年老代空间的占用大小（`4807KB`）
- [10]：年老代空间分配到的总大小（`5632KB`）
- [11]：GC发生前，Java堆空间的已用大小（`6839KB`）
- [12]：GC回收后，Java堆空间的已用大小（`4807KB`）
- [13]：Java堆空间分配到的总大小（`7680KB`）
- [14]：回收区域（`Metaspace`元数据空间）
- [15]：GC发生前，元数据空间的已用大小（`3625KB`）
- [16]：GC回收后，元数据空间的占用大小（`3625KB`）
- [17]：元数据空间分配到的总大小（`1056768KB`）
- [18]：本次GC过程的总耗时（`0.0393051`秒）
- [19]：本次GC过程的用户耗时（`0`）
- [20]：本次GC过程的系统耗时（`0`）
- [21]：本次GC过程的实际耗时（`0.04`秒）

![FullGC日志详解](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e4129d59033d4dfc97931f99a5bb3126~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

> 每条`FullGC`的日志如上图所示，其中规律为：
>  GC类型+GC原因+新生代描述+年老代描述+堆空间描述+元数据空间+耗时描述。

#### 4.1.3、诱发GC的原因

之前的日志中曾见到过几种导致GC的原因，如`Allocation Failure、Ergonomics、Metadata GC Threshold`等，那么诱发GC的原因究竟有多少种呢？其实在HotSpot源码中，运行时触发GC的原因都已经定义好了，在`/src/share/vm/gc_interface/gcCause.cpp`文件中定义了（基于`OPenJDK1.8`源码），如下：

```C++
C++复制代码#include "precompiled.hpp"
#include "gc_interface/gcCause.hpp"

const char* GCCause::to_string(GCCause::Cause cause) {
  switch (cause) {
    case _java_lang_system_gc:
      return "System.gc()";

    case _full_gc_alot:
      return "FullGCAlot";

    case _scavenge_alot:
      return "ScavengeAlot";

    case _allocation_profiler:
      return "Allocation Profiler";

    case _jvmti_force_gc:
      return "JvmtiEnv ForceGarbageCollection";

    case _gc_locker:
      return "GCLocker Initiated GC";

    case _heap_inspection:
      return "Heap Inspection Initiated GC";

    case _heap_dump:
      return "Heap Dump Initiated GC";

    case _no_gc:
      return "No GC";

    case _allocation_failure:
      return "Allocation Failure";

    case _tenured_generation_full:
      return "Tenured Generation Full";

    case _metadata_GC_threshold:
      return "Metadata GC Threshold";

    case _cms_generation_full:
      return "CMS Generation Full";

    case _cms_initial_mark:
      return "CMS Initial Mark";

    case _cms_final_remark:
      return "CMS Final Remark";

    case _cms_concurrent_mark:
      return "CMS Concurrent Mark";

    case _old_generation_expanded_on_last_scavenge:
      return "Old Generation Expanded On Last Scavenge";

    case _old_generation_too_full_to_scavenge:
      return "Old Generation Too Full To Scavenge";

    case _adaptive_size_policy:
      return "Ergonomics";

    case _g1_inc_collection_pause:
      return "G1 Evacuation Pause";

    case _g1_humongous_allocation:
      return "G1 Humongous Allocation";

    case _last_ditch_collection:
      return "Last ditch collection";

    case _last_gc_cause:
      return "ILLEGAL VALUE - last gc cause - ILLEGAL VALUE";

    default:
      return "unknown GCCause";
  }
  ShouldNotReachHere();
}
```

从`HotSpot`源码看来，其实导致GC被触发的原因有很多种，在GC日志信息中，可能出现的总计有二十余种，下面依次简单介绍一下[：](https://link.juejin.cn?target=https%3A%2F%2Fblog.csdn.net%2Flbh_paopao%2Farticle%2Fdetails%2F120269135)

- `System.gc()`：Java程序中手动调用`System.gc()`方法触发的GC。

- `FullGCAlot`：定期触发的GC（JDK内测专属，JVM开发时使用）。

- `ScavengeAlot`：定期触发的GC（JDK内测专属，JVM开发时使用）。

- `Allocation Profiler`：使用`-Xaprof`参数运行程序，在JVM结束时会触发的GC（JFK1.8被弃用了）。

- `JvmtiEnv ForceGarbageCollection`：强制调用本地方法库中的`native`方法：`ForceGarbageCollection(jvmtiEnv* env)`触发的GC。

- `GCLocker Initiated GC`：如果线程执行在 JNI 临界区时，刚好需要进行 GC，此时`GCLocker`将会阻止GC的发生，同时阻止其他线程进入JNI临界区，直到最后一个线程退出临界区时触发一次GC。

- ```
  Heap Inspection Initiated GC
  ```

  ：通过

  ```
  jmap
  ```

  命令进行堆检测时触发的GC。

  - 堆检测命令：`jmap -histo:live <pid>`

- ```
  Heap Dump Initiated GC
  ```

  ：通过

  ```
  jmap
  ```

  命令进行堆转储时触发的GC。

  - 堆转储命令：`jmap -dump:live,format=b,file=heap.out <pid>`

- `WhiteBox Initiated Young GC`：测试时主动触发的`Young GC`（需要增加`WhiteBox`的`Agent`才能使用）。

- `Update Allocation Context Stats`：这个GC仅用于获取更新的分配上下文统计信息。

- `Allocation Failure`：对象分配时内存不足导致分配失败触发的GC。

- `Tenured Generation Full`：年老代空间内存不足触发的GC。

- `Metadata GC Threshold`：元数据空间内存不足触发的GC。

- CMS收集器相关的GC日志信息：

  - `No GC`：用于表示`CMS`的并发标记阶段。
  - `CMS Generation Full`：CMS发生FullGC.
  - `CMS Initial Mark`：CMS初始标记阶段的日志信息。
  - `CMS Final Remark`：CMS重新标记阶段的日志信息。
  - `CMS Concurrent Mark`：CMS并发标记阶段的日志信息。

- 没弄明白的两个：

  - `Old Generation Expanded On Last Scavenge`
  - `Old Generation Too Full To Scavenge`
  - 如有明白这两玩意儿的评论区留言。

- `Ergonomics`：一般出现在PS+PO组合中，空间分配担保时触发的GC。

- G1收集器相关的GC日志信息：

  - `G1 Evacuation Pause`：G1中没有空闲的`region`区导致分配失败时触发的GC。
  - `G1 Humongous Allocation`：没有`Humongous`区分配大对象时触发的GC。

- `Last ditch collection`：在元数据空间分配数据时，分配失败且无法继续扩展内存时触发的GC。

- `ILLEGAL VALUE - last gc cause - ILLEGAL VALUE`：正常情况下该信息是看不到的。

- `unknown GCCause`：未知（未定义）的原因触发的GC。

## 五、GC分代篇总结

在本章中，我们依次从GC的一些基础概念，到分代收集器、各款收集器收集过程、CMS收集器及其执行过程、三色标记算法、三色标记-漏标/多标问题、YoungGC、FullGC日志解读、GC诱发原因等内容进行全面阐述。

> 在JVM的GC体系中，其实并不存在所谓的最好GC器，不同的场景下采用合适的GC收集器，才能在最大程度上追求最优的方案。各款GC收集器对比如下：

| GC收集器          | GC属性        | 作用区域 | GC算法    | 特性         | 应用场景            |
| ----------------- | ------------- | -------- | --------- | ------------ | ------------------- |
| Serial            | 串行回收      | 新生代   | 复制算法  | 响应速度优先 | 单核机器/client程序 |
| Serial Old        | 串行回收      | 年老代   | 标-整算法 | 响应速度优先 | 单核机器/client程序 |
| ParNew            | 并行回收      | 新生代   | 复制算法  | 响应速度优先 | 交互多/计算少的程序 |
| Parallel Scavenge | 并行回收      | 新生代   | 复制算法  | 吞吐量优先   | 计算多/交互少的程序 |
| Parallel Old      | 并行回收      | 年老代   | 标-整算法 | 吞吐量优先   | 计算多/交互少的程序 |
| CMS               | 并行/并发回收 | 年老代   | 标-清算法 | 响应速度优先 | 交互多/计算少的程序 |