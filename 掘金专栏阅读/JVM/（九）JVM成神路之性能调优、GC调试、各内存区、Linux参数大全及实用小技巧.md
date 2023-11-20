## 一、JVM运行时数据区参数列表

  虚拟机的运行时数据区中，堆空间无疑是最重要的，除堆空间之外，虚拟机栈/本地方法栈（HotSpot中二合一）、元数据空间、本地内存等区域也有对应的参数，接下来依次列出。

### 1.1、通用参数

- `-client`：以客户端模式启动JVM。

- `-server`：以服务器模式启动JVM。

- `-agentlib`：装载本地`lib`库。

- `-agentpath`： 按全路径装载本地库。

- `-classpath`：声明jvm搜索目录名、jar、zip文档名，之间用分号;分隔。

- ```
  -D property=value
  ```

  ：设置系统[属性名/值]对。

  - 应用程序运行时可通过`System.getProperty("property")`获取。

- ```
  -enableassertions
  ```

  ：设置JVM是否启动断言机制，默认关闭(1.4开始支持的)。

  - 后续跟`-esa`代表开启，跟`-dsa`代表关闭。

- `-jar`：指定以`jar`包的形式执行一个应用程序。

- `-javaagent:jarpath`：指定`JVM`启动时装入java语言设备代理。

- ```
  -verbose
  ```

  ：这个参数比较有趣，可以用于输出JVM一些信息，如下：

  - 结尾跟`:class`：输出JVM类加载相关信息，JVM找不到类时可以用于排查。
  - 结尾跟`:gc`：输出每次GC相关的简略信息。
  - 结尾跟`:jni`：输出`native`方法调用的相关状况，可以用于诊断`jni`调用错误。

- `-version`：输出当前机器Java环境版本信息。

- ```
  -version:release
  ```

  ：指定当前机器以某个特定的版本执行。

  - `-version:"1.5.0_04 1.5*&1.5.1_02+"`：以1.5或比1.5更高的版本执行。

- `-help`：输出Java所有标准参数及其描述。

- `-X`： 输出非标准的参数列表及其描述。

- `-Xint`：设置JVM以纯解释器模式执行。

- `-Xcomp`：完全采用即时编译器模式执行程序。

- `-Xmixed`：采用解释器+JIT即时编译器的混合模式共同执行。

- `-Xbatch`：禁止JVM后台编译，将编译过程放到前台任务执行。

- ```
  -Xbootclasspath
  ```

  ：让

  ```
  Bootstrap
  ```

  从指定目录下加载库、

  ```
  jar、zip
  ```

  包。

  - 结尾跟`/a:path`：将指定路径的全部文件追加到默认`bootstrap`路径中。
  - 结尾跟`/p:path`：让JVM优先于`bootstrap`默认路径加载指定路径的文件。

- `-Xcheck:jni`： 对JNI函数进行附加检查，校验传递给JNI函数参数的合法性。

- `-Xfuture`：让`JVM`对类文件执行严格模式检查（默认不使用）。

- `-Xnoclassgc`：关闭针对`class`的gc（开启后类不会被卸载/会导致OOM）。

- `-Xincgc`： 开启增量gc，会减少停顿，但会导致吞吐量下降。

- `-Xprof`：跟踪正运行的程序，适合于开发环境调试。

- `-Xrs`：减小JVM对操做系统信号（`signals`）的使用。

- `-XX:DisableExplicitGC`：禁止程序内部调用`System.gc()`触发GC。

- `-XX:MaxFDLimit`：文件描述符的最大数量限制。

- `-XX:UseThreadPriorities`：启用本地线程优先级。

- `-XX:UnlockExperimentalVMOptions`：开启未知参数识别机制。

- `-XX:PrintFlagsInitial`：输出当前程序的参数默认值。

### 1.2、堆空间常用参数

- `-Xms`：JVM启动时堆空间大小。
- `-Xmx`：堆空间的最大大小。
- `-XX:NewSize=n`/`-Xmn`：分配年轻代的空间大小。
- `-XX:NewRatio=n`：设置年轻代和年老代的比值。
- `-XX:SurvivorRatio`：设置新生代中`Eden`区和`Survivor`区的大小比值。
- `- XX:TargetsurvivorRatio`：设置`Survivor`区的目标使用率，默认为`50%`。
- `-XX:MaxTenuringThreshold`：设置年老代对象的晋升年龄。
- `-XX:PretenureSizeThreshold`：指定直接进入年老代大对象的阈值。
- `-XX:MaxHeapFreeRatio`：GC后允许堆中空闲内存占的最大比例。
- `-XX:MinHeapFreeRatio`：GC后允许堆中空闲内存占的最小比例。
- `-XX:MaxNewSize`：设置新生代内存的最大的可分配大小。
- `-XX:UseLargePages`：开启大页面内存技术（大内存下使用）。
- `-XX:LargePageSizeInBytes`：设置Java堆空间的大页面尺寸。
- `-XX:HandlePromotionFailire`：是否开启空间分代担保机制。
- `-XX:HeapDumpOnOutOfMemoryError`：堆空间首次发生OOM时输出`dump`日志。
- `-XX:HeapDumpPath`：和上面的参数配套使用，指定输出的位置。
- `-XX:HeapDumpBeforeFullGC`：在`FullGC`前`dump`。
- `-XX:HeapDumpAfterFullGC`：在`FullGC`后`dump`。
- `-XX:OnOutOfMemoryError`：当JVM首次发生OOM时，可以执行制定脚本。
- `-XX:+UseGCOverheadLimit`：在抛出OOM前限制JVM耗费在GC上的时间比例。
- `-XX:UseAdaptiveSizePolicy`：是否开启自适应堆比例调整机制（并行GC器）。
- `-XX:MaxGCPauseMillis`：设置每次新生代垃圾回收的期望最大停顿时间。
- `-XX:UseCompressedOops`：是否开启对象指针压缩机制。
- `-XX:CompressedClassSpaceSize`：是否开启类指针压缩机制。
- `-XX:UseTLAB`：是否开启TLAB分配机制。
- `-XX:TLABWasteTargetPercent`：指定`TLAB`与整个`Eden`区的占比。
- `-XX:TLABSize`：显示指定TLAB区域的大小。
- `-XX:ResizeTLAB`：是否开启JVM自适应的TLAB大小自调整机制。
- `-XX:BiasedLockingStartupDelay`：设置`sync`匿名偏向锁的延迟启动时间。
- `-XX:PreBlockSpin`：指定`Sync`自旋锁次数（1.6被弃用，引入自适应自旋）。
- `-XX:PermSize`：设置非堆空间初始大小（1.7后被弃用，改为元空间）。
- `-XX:MaxPermSize`：设置非堆空间最大大小（1.7后被弃用，改为元空间）。
- `-XX:AlwaysPreTouch`：是否开启物理内存分配替换虚拟内存分配。

### 1.3、Java栈参数

- `-Xss`：设置虚拟机栈的默认大小。
- `-XX:ThreadStackSize`：设置线程栈默认大小。
- `-XX:+DoEscapeAnalysis`：是否开启逃逸分析机制。
- `-XX:PrintEscapeAnaysis`：输出逃逸分析信息。
- `-XX:EliminateAllocations`：是否开启标量替换机制。
- `-XX:EliminateLocks`：是否开启同步消除机制。
- `-XX:PrintEliminateAllocations`：输出标量替换信息。

### 1.4、元数据空间参数

- `-XX:MetaspaceSize`：指定元数据空间的初始大小。
- `-XX:MaxMetaspaceSize`：指定元数据空间的最大大小。
- `-XX:MinMetaspaceFreeRatio`：FullGC后，允许元空间空闲内存的最小比例。
- `-XX:MaxMetaspaceFreeRatio`：FullGC后，允许元空间空闲内存的最大比例。
- `-XX:MinMetaspaceExpansion`：元空间内存不足时，设置增量内存的最小大小。
- `-XX:MaxMetaspaceExpansion`：元空间内存不足时，设置增量内存的最大大小。
- `-XX:CompileThreshold`：方法调用计数器编译触发阈值设置。
- `-XX:ReservedCodeCacheSize`：热点代码缓存区的最大大小。
- `-XX:InitialCodeCacheSize`：设置热点代码缓存区的初始大小。
- `-XX:+UseCodeCacheFlushing`：热点代码空间已满时，取消部分冷代码的编译。
- `-XX:UseCounterDecay`：是否开启热度衰减机制。
- `-XX:CounterHalfLifeTime`：调整半衰周期的时间，单位为秒。
- `-XX:PrintCompilation`：当方法被编译时输出相关信息。
- `XX:BackgroundCompilation`：禁止JVM后台编译，将编译任务在前台执行。
- `-XX:CITime`：JVM关闭时，输出各种编译的统计信息。
- `-XX:TraceClassLoading`：跟踪类加载信息。
- `-XX:TraceClassLoadingPreorder`：跟踪所有被使用的类加载信息。
- `-XX:TraceClassUnloading`：跟踪类卸载信息。
- `-XX:TraceLoaderConstraints`：跟踪类加载器相关信息。
- `-XX:TraceClassResolution`：跟踪常量池信息。

### 1.5、直接内存参数

- `-XX:DirectMemorySize`：设置直接内存的初始空间值（不设置默认与`Xmx`参数值相同）。
- `-XX:MaxDirectMemorySize`：设置直接内存的最大空间值。

### 1.6、调试通用参数

- `-XX:+PrintVMOptions`：输出当前运行程序的显式启动参数。
- `-XX:+PrintCommandLineFlags`：输出传递给虚拟机的显式和隐式参数。
- `-XX:+PrintFlagsInitial`：查看所有的参数的默认初始值。
- `-XX:+PrintFlagsFinal`：输出所有的系统参数的最终值。
- `-XX:PrintTLAB`：输出TLAB分配相关的信息
- `-XX:CITime`：输出JIT即时编译的耗时。
- `-XX:ErrorFile`：保存错误日志或者数据到文件中。
- `-XX:OnError=”;”`：出现致命错误时执行自定义的指定脚本。

### 1.7、GC调试通用参数

- `-XX:+PrintGC`/`-verbose:gc`：输出GC的简略日志。
- `-XX:+PrintGCDetails`：输出GC发生时的详细日志。
- `-XX:+PrintGCTimeStamps`：输出GC发生的时间。
- `-XX:+PrintGCApplicationConcurrentTime`：输出应用程序的执行时间。
- `-XX:+PrintGCApplicationStoppedTime`：输出应用由于GC而产生的停顿时间。
- `-Xloggc`：将输出的GC日志转储到指定目录中。
- `-XX:+PrintReferenceGC`：跟踪并输出软/弱/虚引用和`Finallize`队列信息。
- `-XX:+PrintHeapAtGC`：每次GC前后打印堆信息。
- `-XX:PrintGCCause`：输出导致本次GC触发的原因。

## 二、新生代垃圾收集器参数列表

  上述阶段中，将一些JVM通用参数、JVM运行时数据区参数以及部分调试参数进行了简单整理，接下来看看GC相关的一些参数。

### 2.1、Serial收集器参数

- `-XX:UseSerialGC`：是否启用`Serial`作为新生代收集器。

### 2.2、ParNew收集器参数

- `-XX:UseParNewGC`：是否启用`ParNew`作为新生代收集器。

### 2.3、ParallelScavenge收集器参数

- `-XX:UseParallelGC`：是否启用`ParallelScavenge`作为新生代收集器。
- `-XX:MaxGCPauseMillis`：设置GC发生时允许的最大停顿时间。
- `-XX:GCTimeRatio`：精准控制GC发生时的吞吐量占比。
- `-XX:UseAdaptiveSizePolicy`：是否开启JVM自适应的GC调节策略。
- `-XX:ParallelGCThreads`：指定GC工作时的并行线程数（默认为CPU核数）。

## 三、年老代垃圾收集器参数列表

### 3.1、SerialOld收集器参数

- `-XX:UseSerialGC`：是否启用`SerialOld`作为年老代收集器。

### 3.2、ParallelOld收集器参数

- `-XX:UseParallelOldGC`：是否启用`ParallelOld`作为年老代收集器。

### 3.3、CMS收集器参数

- `-XX:UseConcMarkSweepGC`：是否启用`CMS`作为年老代收集器。
- `-XX:UseCMSInitiatingoccupancyonlyn`：指定触发CMS回收的阈值。
- `-XX:CMSInitIatingOccupancyFaction`：指定空间占用达到多少比例时触发`MSC`工作。
- `-XX:+UseCMSInitiatingOccupancyOnly`：强制设定的回收阈值，达到即触发GC。
- `-XX:UseCMSCompactAtFullCollection`：内存碎片化严重时是否开启`MSC`工作。
- `-XX:CMSFullGCsBeforeCompaction`：设置间隔多少次`FullGC`触发一次`MSC`工作，默认`0`。
- `-XX:ParallelCMSThreads`：指定CMS执行GC工作时的并发线程数。
- `-XX:CMSClassUnloadingEnabled`：是否开启类元数据卸载（回收）机制。
- `-XX:CMSInitiatingPermOccupancyFraction`：指定元空间GC的触发比例。
- `-XX:GCTimeRatio`：指定GC停顿时间与用户线程工作时间的占比。
- `-XX:MaxGCPauseMillis`：指定一次GC允许发生的最大停顿时间。
- `-XX:CMSIncrementalMode`：是否启用增量回收模式（1.8中被废弃，1.9中移除）。
- `-XX:CMSScavengeBeforeRemark`：是否在重新标记阶段前触发一次新生代GC。
- `-XX:CMSMaxAbortablePrecleanTime`：默认为`5s`，表示`AbortablePreclean`阶段的最大时间。
- `-XX:CMSScheduleRemarkEdenPenetration`：指定`Eden`区使用比例超过`N%`时，就结束预清理阶段进入`remark`阶段。
- `-XX:CMSParallellnitialMarkEnabled`：在初始阶段是否采用多线程执行。
- `-XX:CMSParallelRemarkEnabled`：在重新标记阶段是否采用多线程执行。
- `-XX:CMSClassUnloadingEnabled`：在正常GC阶段中清除过期的`class`数据，不等到`FullGC`时再清除。

## 四、整堆分区垃圾收集器参数列表

### 4.1、G1收集器参数

- `-XX:+UseG1GC`：让JVM使用G1收集器。

- `-XX:ConcGCThreads`：指定并发GC工作阶段的并发线程数量。

- `-XX:ParallelGCThreads`：指定STW阶段，GC工作的并行线程数。

- `-XX:MaxGCPauseMillis`：期望的目标停顿时间(默认`200ms`)。

- `-XX:GCPauseIntervalMillis`：GC的间隔时间。

- `-XX:G1HeapRegionSize`：指定单个分区大小(`1~32MB`，且必须是2的N次幂)。

- `-XX:G1NewSizePercent`：新生代初始空间占比(默认整堆的`5%`)。

- `-XX:G1MaxNewSizePercent`：新生代最大空间占比。

- `GCTimeRatio`：GC停顿的时间占比（G1会根据此值调整堆空间）。

- ```
  -XX:TargetSurvivorRatio
  ```

  ：G1中空间分配担保的触发比例。

  - 当`Survivor`空间达到填充容量阈值时(默认`50%`)，将对象转入年老代。

- `-XX:MaxTenuringThreshold`：新生代空间对象的晋升年龄阈值(默认`15`)。

- `-XX:InitiatingHeapOccupancyPercent`：年老代空间触发`MixedGC`的阈值。

- `-XX:GCPauseIntervalMillis`：设置停顿间隔时间（作用于G1-回收阶段）。

- `-XX:ExplicitGCInvokesconcurrent`：对显示调用触发的GC是否启用并发回收。

- ```
  -XX:G1ReservePercent
  ```

  ：预留年老代的空闲

  ```
  Region
  ```

  数，为分代担保做准备，默认

  ```
  10
  ```

  。

  - 默认为整堆`45%`，年老代空间使用比例达到该阈值时触发混合GC。

- ```
  -XX:G1MixedGCLiveThresholdPercent
  ```

  ：单个

  ```
  Region
  ```

  触发GC的垃圾占比阈值。

  - 默认为`85%`，单个分区中垃圾对象达到该阈值时才可被选作目标区域回收。

- ```
  -XX:G1MixedGCCountTarget
  ```

  ：指定回收阶段时，分为几次筛选回收。

  - 默认8次，在G1最后的筛选回收阶段可以回收一段时间，然后暂停回收，恢复系统运行，过一会儿再回收，这样做可以让系统不至于单次停顿时间过长。

- ```
  -XX:G1HeapWastePercent
  ```

  ：GC回收停止的空闲

  ```
  Region
  ```

  阈值(默认

  ```
  5%
  ```

  )。

  - 回收阶段是基于复制算法来完成的，回收一个分区时会将该区内的所有存活对象移入到另外一个区域，然后统一清除该分区，这样最终就会出现一个空的`Region`，而当空闲的`Region`数量达到`5%`时，G1会结束本次`MixedGC`。

- `-XX:G1OldCSetRegionThresholdPercent`：每轮`MixedGC`回收分区的最大比例，默认`10%`。

- `-XX:SoftRefLRUPolicyMSPerMB`：指定每兆空间中软引用的存活时间，默认为`1000ms`。

- `-XX:G1UseAdaptiveIHOP`：是否开启`G1-IHOP`分析预测机制（默认开启）。

- `-XX:ParallelRefProcEnabled`：是否启用并发引用处理机制（默认关闭）。

- `-XX:G1RSetUpdatingPauseTimePercent`：降低处理`Rset`记忆集时的停顿时间。

- `-XX:G1RSetRegionEntries`：降低`RSet`粗化的程度。

- `-XX:G1SummarizeRSetStatsPeriod`：诊断参数，可以查看`Rset`的周期频率报告。

### 4.2、ZGC收集器参数

- `-XX:UnlockExperimentalVMOptions`：是否解锁JVM隐藏的额外参数（实验参数）。
- `-XX:UseZGC`：是否启用`ZGC`作为JVM整堆的垃圾收集器。
- `-XX:ZCollectionInterval`：定期触发一次GC（默认不开启，单位：秒）。
- `-XX:ZProactive`：设置ZGC主动触发GC的阈值（默认开启）。

### 4.3、ShenandoahGC收集器参数

- `-XX:UseShenandoahGC`：是否启用`ShenandoahGC`作为JVM整堆的垃圾收集器。

> 其实整堆收集器中，除开G1之外，其他两款整堆收集器，因为没有做分代实现，所以能够暴露给外部操作的JVM参数其实也并不会太多，大概也只能调调线程数、内存大小、GC触发条件、GC频率这类的了。因为这些整堆收集器本身就能够很好的驾驭已分配的堆空间。

## 五、Linux系统常用指令

  Linux系统是每位开发者逃不开的话题，程序发布、上线中间件、数据库部署等几乎都会基于Linux系统。因此，对于Linux你大概不需要掌握的特别精通，但至少对于它的一些常用指令必须要会使用，毕竟项目上线、线上排查等场景下，我们都必须要通过指令进行操作。

### 5.1、目录管理与文件操作指令

- `pwd`：显示当前所在目录的路径。

- ```
  cd
  ```

  ：切换目录，后面需要跟具体操作：

  - `..`：回到上一级目录。
  - `/`：回到根目录。
  - `~`：回到`/home/user/`目录下。
  - `dirName`：进入当前目录的`dirName`子目录。
  - `/xx/xx/xxx`：进入到指定路径的目录下。

- ```
  ls
  ```

  ：查看当前目录下的所有文件，可跟选项如下：

  - `-a`：显示目录下的所有文件，包含隐藏文件。
  - `-l`：显示目录中所有文件的详细信息，如权限、所有者、群组、大小、日期等。
  - `-f`：显示文件的类型。
  - `-r`：逆向模式，从后至前的显示整个目录下所有文件。
  - `-R`：递归模式，显示当前目录下所有子目录的内容。
  - `-s`：按文件大小排序显示。
  - `-h`：按`KB、MB、GB`等单位显示文件大小。
  - `ls -l xxx`：显示名称为`xxx`文件的详细信息。

- `ll`：显示当前目录下所有文件，并显示文件的详细信息。

- `tree`或`lstree`：显示文件和目录由根目录开始的树形结构。

- ```
  find
  ```

  ：在系统中搜索指定文件：

  - `-type`：按类型搜索。
  - `-name`：按文件名搜索。
  - `-atime +N`：按时间搜索`N`天内未被使用过的文件。
  - `-mtime -N`：按时间搜索过去`N`天内创建的文件。
  - `-size`：按文件大小搜索文件。

- `man 指令名称`：查询、解释一个指令令的使用方法、注意事项。

- `locate filename`：查询某个文件的具体位置。

- `whatis 指令名称`：查询某个指令的作用。

- ```
  in file link
  ```

  ：为文件

  ```
  file
  ```

  创建一个

  ```
  link
  ```

  物理链接（

  ```
  Windows
  ```

  的快捷方式）。

  - `-s`：创建一个软链接。

- `mkdir xxx`：新建名称为`xxx`的文件夹。

- `touch filename`：创建文件名为`filename`的文件。

- ```
  cp /xx/xx.xx /xx/xx.xx
  ```

  ：将

  ```
  /xx/xx.xx
  ```

  文件复制到其他路径。

  - `-i`：互动模式，如果目标目录下存在相同文件是，提示是否确认覆盖？
  - `-r`：递归模式，将目录下所有的子目录、子文件等全部复制。
  - `-v`：详细进度模式，显示当前复制的完成进度。

- ```
  mv /xx/xx.xx /xx/xx.xx
  ```

  ：将

  ```
  /xx/xx.xx
  ```

  文件移动（剪切）到其他路径。

  - `-i`：互动模式，如果目标目录下存在相同文件是，提示是否确认覆盖？
  - `-f`：强制模式，不管目标路径下是否存在同名文件，都直接强制覆盖。
  - `-v`：详细进度模式，显示当前移动的完成进度。

- ```
  rm filename
  ```

  ：删除指定文件，可选参数：

  - `-i`：互动模式，提示是否确认删除该文件？
  - `-f`：强制模式，强制删除目标文件。
  - `-r`：递归模式，递归删除目标文件所有的子目录及其文件。
  - `-v`：详细进度模式，显示当前删除的完成进度。
  - `rm -rf /*`：老梗，代表强制递归删除根目录下的所有文件。

- `rmdir xxx`：删除指定文件夹目录。

### 5.2、文本操作指令

- `cat filename`：打开`filename`文件查看文件内容。

- `more filename`：打开`filename`文件，输出一屏数据。

- `less filename`：和`more`作用相同，输出一屏数据，但可以上下滑动。

- `tac filename`：和`less`作用相同，但是从末尾开始显示，支持上下滑动。

- ```
  tail filename
  ```

  ：查看文件末尾十行数据内容。

  - `-n`：输出指定的`n`行末尾数据。
  - `-f`：监控某个文本文件，实时输出最新追加的数据，通常用于监控日志。

- `head -n filename`：输出文件头部`n`行数据。

- `grep xx filename`：在`filename`文件内查找`xx`字符。

- ```
  wc
  ```

  ：查看指定文件或进程的数量：

  - `-l`：查看指定目标的行数。
  - `-w`：查看指定文件的单词数量。
  - `-c`：查询指定文件的字节数量。
  - `-m`：查询指定文件的字符数量。

- ```
  vi filename
  ```

  ：编辑指定文件的内容（

  ```
  vi
  ```

  命令是

  ```
  Unix/Linux
  ```

  通用的文件编辑器，

  ```
  vim
  ```

  是

  ```
  vi
  ```

  的增强版）。

  - 进入

    ```
    vi
    ```

    工具时的命令：

    - `vi n filename`：打开指定文件并将光标置于第`n`行。
    - `vi /pattern filename`：打开指定文件并将光标置于第一个与`pattern`匹配处。
    - `vi -r filename`：打开指定文件，并恢复上次系统崩溃时的状态。
    - `vi filename...filename`：同时编辑多个文件，依次进行编辑。

  - 屏幕翻滚类操作（命令模式）：

    - `Ctrl+u`：向前滚动半屏。
    - `Ctrl+d`：向后滚动半屏。
    - `Ctrl+f`：向前滚动一屏。
    - `Ctrl+b`：向后滚动一屏。
    - `[n]z`：将第`n`行滚至屏幕顶部,不指定`n`时，将当前行滚至屏幕顶部。

  - 插入文本类操作（插入模式）：

    - `i`：在光标位置前插入。
    - `I`：在当前行的行首插入。
    - `a`：在光标位置后插入。
    - `A`：在当前行的行尾插入。
    - `o`：在当前行上面新增一行。
    - `O`：在当前行下面新增一行。
    - `s`：替换光标位置的字符。
    - `S`：替换光标位置的行。

  - 删除、复制、粘贴、查找、替换、撤销类操作（命令模式）：

    - `x`：删除光标后一个字符。
    - `X`：删除光标前一个字符。
    - `dd`：删除当前行。
    - `dG`：向下删除到最后一行。
    - `D0`：从光标位置删除至行首。
    - `[n]x`：删除光标后n个字符。
    - `[n]X`：删除光标前n个字符。
    - `[n]dd`：向下删除n行。
    - `dw`：删除当前的单词。
    - `d$`：删除光标至行尾。
    - `yy`：复制当前行。
    - `[n]yy`：向下复制`n`行。
    - `p`：在下一行位置粘贴内容。
    - `r`：替换光标处的字符。
    - `R`：替换光标所到处的字符（按`ESC`键结束）。
    - `u`：撤销操作。
    - `ctrl+r`：反撤销操作。
    - `/[filed]`：查找`filed`关键字（按`n`查找下一个）。
    - `?[filed]`：查找`filed`关键字（按`n`查找下一个）。

  - 退出保存命令（底行模式/需先按

    ```
    ESC
    ```

    ）：

    - `:w`：保存更改内容但不退出本次编辑。
    - `:w file`：将内容保存到`file`文件中，不退出本次编辑。
    - `:w!`：强制保存更改内容，不退出本次编辑。
    - `:wq`：保存更改内容并退出编辑。
    - `:wq!`：强制保存更改内容并退出编辑。
    - `:q`：不保存更改内容，直接退出编辑。
    - `:q!`：不保存更改内容，强制退出编辑。
    - `:e!`：放弃本次编辑中的所有更改内容，从上次保存的时刻重新编辑。

### 5.3、文件压缩/解压/备份指令

- ```
  tar
  ```

  ：压缩解压命令：

  - `-z`：让打包的文件具备`gz`压缩性质（`gz`格式，压缩速度最快）。
  - `-j`：让打包的文件具备`bzip`压缩性质（`bz2`格式，压缩文件最小）。
  - `-J`：让打包的文件具备`xz`压缩性质（`xz`格式，压缩率最佳）。
  - `-x`：解压打包文件。
  - `-t`：查看压缩文件中的内容。
  - `-c`：将文件打成压缩包。
  - `-C`：解压时指定解压位置，如`tar -xf xx.tar.gz -C /usr/xxx/`。
  - `-v`：显示解压时的压缩包文件列表。
  - `-f`：参数后面指定要解压或压缩的文件名。
  - `-p`：保留备份数据的原本权限与属性，一般用于打包重要的配置信息。
  - `-P`：保留绝对路径。
  - 压缩命令示例：`tar -czvf xx.tar.gz xx.txt xx.conf`。
  - 解压命令示例：`tar -xf xx.tar.gz -C /usr/xxx/`。

- ```
  zip
  ```

  ：

  ```
  zip
  ```

  压缩命令（需要额外安装）：

  - `-m`：将目标文件压缩后，删除原文件。
  - `-o`：将压缩包内所有文件的最新变动时间改为压缩的时间。
  - `-q`：安静模式，在压缩的时候不显示压缩执行的过程。
  - `-r`：递归模式，将指定目录下的所有子文件以及目录一起压缩。
  - `-x`：可以跟一个文件列表，压缩时排除文件列表中的文件。

- ```
  unzip
  ```

  ：

  ```
  zip
  ```

  解压命令（需要额外安装）：

  - `-o`：强制模式，解压时如果存在同名的文件直接覆盖。
  - `-l`：不解压查看压缩包中包含的文件。
  - `-v`：解压时显示执行过程中的详细信息。
  - `-t`：检查解压文件是否正确（是否损坏、数据丢失等）。
  - `-q`：安静模式，在解压的时不显示解压执行的过程。
  - `-d`：指定文件解压后存储的目录。
  - `-x`：指定不要解压压缩包中的那些文件。

- `bzip2`：将文件打包成拓展名为`.bz2`的压缩包。

- `bunzip2`：将文件拓展名为`.bz2`的压缩包解压。

- `gzip`：将文件打包成拓展名为`.gz`的压缩包。

- `gunzip`：将文件拓展名为`.gz`的压缩包解压。

- `compress`：将文件打包成拓展名为`.Z`的压缩包。

- `uncompress`：将文件拓展名为`.Z`的压缩包解压。

- `rar`：将文件打包成拓展名为`.rar`的压缩包。

- `unrar`：将文件拓展名为`.rar`的压缩包解压。

### 5.4、系统操作、权限管理/用户群组管理指令

- ```
  shutdown
  ```

  ：关机命令：

  - `-k`：不会真正关机，仅发出关机的警告（结合`sh`脚本做运维监控用）。
  - `-r`：关机后重启。
  - `-t`：在指定的时间后关机（默认单位：`min`）。

- ```
  reboot
  ```

  ：重启命令：

  - `-d`：关机时不会将内存中的数据写入到`/var/log/wtmp`档案内。
  - `-f`：不管是否有应用阻止关机，强制性重启。
  - `-n`：在重开机前不做将记忆体数据写回硬盘（包含了`-d`操作）。
  - `-w`：不会真的重启，只是把数据记录写到`/var/log/wtmp`档案里。

- ```
  kill
  ```

  ：终止（杀）进程指令（只是给进程发个信号，让进程“自杀”）：

  - `-l`：列出所有可用的信号名称。

  - `-p`：输出`pid`但并不发送操作信号。

  - ```
    -signal
    ```

    ：给进程传递信号（目前有三种）：

    - `-HUP/-1`：重新启动进程。
    - `-KILL/-9`：终止（杀掉）进程。
    - `-TERM/-15`：结束进程。

- `date`：更改或查看目前日期。

- `cal`：显示月历及年历。

- `arch`：显示机器的处理器架构。

- ```
  uname
  ```

  ：显示机器的信息：

  - `-m`：显示机器的处理器架构。
  - `-r`：显示当前系统的内核版本。

- ```
  chmod
  ```

  ：用于改变

  ```
  Linux
  ```

  系统文件或目录的访问权限，支持文字、数字设定法：

  - `-R`：递归模式，如果打算更改一个目录的权限，加这个才可更改所有子文件权限。
  - 文件使用者分为所有者、群组、其他用户三种：
    - `u`：文件或目录的所有者。
    - `g`：所有者所在的用户群组。
    - `o`：其他用户。
    - `a`：所有用户（包含`u、o、a`）。
  - 文件权限分为读、写、执行三个级别：
    - `r`：读权限，数字为`4`。
    - `w`：写权限，数字为`2`。
    - `x`：执行权限，数字为`1`。
  - 权限操作：
    - `+`：添加权限。
    - `-`：移除权限。
    - `=`：覆盖原有权限，让当前设置的权限成为唯一权限。
  - 示例：
    - `chmod a+rwx filename`：为所有用户添加`filename`文件的读、写、执行权限。
    - `chmod 777 filename`：这条指令是上面那条指令的数字设定法。

- `su username`：将当前登录的用户切换指定用户（更改时需要输入密码）。

- `useradd username`：添加一个名字为`username`的用户（`root`用户操作）。

- ```
  passwd username
  ```

  ：更改

  ```
  username
  ```

  用户的密码，其他操作如下：

  - `-l username`：禁止指定用户登录。
  - `-u username`：解除被禁止登录的用户。
  - `-d username`：清除指定用户的登录密码，该用户之后无需密码即可登录。

- `userdel -r username`：删除指定用户账号（`-r`：递归删除用户的所有目录）。

- `groupadd groupname`：创建用户组。

- `w`：查看当前登录用户的详细信息。

- `who`：查看当前登录的所有用户信息。

- `last`：查看用户登录的记录（多次登录的记录）。

- `lastlog`：查看所有用户最后的登录时间。

- `touch /etc/nologin`：禁止除`root`账号外的所有用户登录。

### 5.5、网络、内存、磁盘管理指令

- ```
  ping ip/domain-name
  ```

  ：检测与某个节点之间通信是否正常：

  - `-c`：设置要求响应的次数。
  - `-d`：使用`Socket`的`SO_DEBUG`功能。
  - `-f`：大量且快速的发送网络封包给一台机器，看它的回应。
  - `-i`：指定收发信息的间隔时间，单位为秒。
  - `-s`：设置数据包的大小，单位：字节。
  - `-r`：忽略目标机器的网关，直接将数据保送到远程主机上。
  - `-q`：不显示命令的执行过程，只显示结果。
  - `-v`：详细显示命令的执行过程，包括非回应信息和其它信息。
  - `-t`：设置存活数值`TTL`的大小。

- ```
  ftp ip/domain-name
  ```

  ：远程文件传输（下载、上传）文件：

  - `-d`：显示指令执行过程中的详细信息。
  - `-i`：关闭互动模式，每次执行不询问任何问题。
  - `-g`：关闭本地主机文件名称支持特殊字符的扩充特性。
  - `-n`：不试用自动登录，每次连接都需要手动输入账号密码。
  - `-v`：显示`ftp`文件传输的进度信息。
  - FTP内部指令：
    - 大部分指令与Linux的基本操作指令相同，如`cd`等。
    - `get remote-file local-file`：从其他机器中下载文件到当前机器。
    - `mget remote-files`：批量下载。
    - `put local-file remote-file`：将本地文件上传到远程机器。
    - `mput local-files`：批量上传。
    - `bye`：退出ftp服务。
    - .......

- `telnet ip/domain-name`：远程登录主机。

- `rlogin ip/domain-name`：也是远程登录主机的作用，与`telnet`指令类似。

- ```
  netstat
  ```

  ：查看

  ```
  Linux
  ```

  系统的网络情况：

  - `-a`：显示所有连接中的`Socket`。
  - `-f`：显示`FIB`信息。
  - `-c`：持续输出网络状态。
  - `-i`：显示网络界面信息。
  - `-n`：不显示网络名称，显示真实网络IP。
  - `-o`：显示计数器信息。
  - `-r`：显示网络路径表信息。
  - `-t`：显示`TCP`连接信息。
  - `-u`：显示`UDP`连接信息。
  - `-v`：显示版本信息。
  - `-w`：显示`RAW`连接信息。
  - `netstat -nat|grep -i "8080"|wc -l`：查看`8080`端口的连接数。

- `ifconfig`：查看和设置网卡信息。

- `ip addr`：查看网卡信息。

- `route`：查看和操作当前机器的路由表。

- `netstat`：查看本机的网络状态，可看到端口占用情况和网络连接情况。

- `traceroute`：显示一个请求到目标服务器所经的全部路由节点（排错用）。

- `iftop`：查看实时网络io情况。

- `lsof`：检查端口是否被占用。

- `dig`：查看域名解析信息。

- `curl`：发送一个http请求，检测目标服务器是否可以正常工作。

- `wget`：下载一个网络文件到本地机器。

- `yum install`：在线安装需要用到的工具，类似于Python的PIP指令。

- `firewall-cmd --state`：查看防火墙状态。

- `systemctl list-unit-files|grep firewalld.service`：查看防火墙信息。

- `systemctl restart firewalld.service`：重启防火墙。

- `systemctl stop firewalld.service`：关闭防火墙。

- `systemctl disable firewalld.service`：开机时禁止启动防火墙。

- `firewall-cmd --zone=public --add-port=8080/tcp --permanent`：开放`8080`端口。

- `firewall-cmd --zone=public --list-ports`：查看防火墙已开放的端口。

- ```
  ps
  ```

  ：查看当前系统中在后台执行的进程信息：

  - `-a`：查看当前运行的所有进程。
  - `-u`：列出所有进程所属者的名称以及运行时长。
  - `-x`：列出所有程序，包括那些没有终端机的进程。
  - `-ef`：显示进程的全部信息，包括父进程ID、创建者、创建时间、PID等。
  - `-l`：只显示与本次登录有关的进程信息。
  - `-T`：查看某个进行内的线程信息。
  - `-p`：指定一个进程ID，与`-T`一同使用。
  - `-m`：输出后台所有进程对于系统内存的占用情况。
  - `-r`：只单独显示在执行的前台进程。

- ```
  top
  ```

  ：查看系统执行程序对内存、CPU、磁盘的使用情况（会实时刷新）：

  - `-H`：查看所有线程的负载情况。
  - `top -H -p pid`：根据pid列表指定进程下的所有线程信息。

- ```
  service
  ```

  ：查看系统服务信息：

  - `service servicename -status`：查看指定名称服务的运行状态。
  - `service --status-all`：查看所有服务的运行状态。
  - `service servicename start`：启动指定名称的系统服务。
  - `service servicename restart`：重启指定名称的系统服务。
  - `service servicename stop`：关闭指定名称的系统服务。

- `free`：查看内存信息及使用信息。

- ```
  mount dir
  ```

  ：挂载指定目录下的文件系统（

  ```
  Linux
  ```

  文件系统只有挂载了才能使用）：

  - `-a`：挂上`/etc/fstab`下的全部文件系统。
  - `-t`：制定所挂上来的文件系统的名称（类似于`Windows`的盘符重命名）。
  - `-n`：挂上文件系统，但不把文件系统的数据写入`/etc/mtlab`这个文件。
  - `-w`：将文件系统设为可读写。
  - `-r`：挂上来的文件系统设为只读。

- `umount dir`：卸载某个挂上来的文件系统。

- ```
  df
  ```

  ：检查硬盘分区与已经挂载的文件系统的磁盘空间，既查看硬盘的使用情况：

  - `-a`：显示所有文件系统和各分区的硬盘使用情况。
  - `-i`：列出`I-nodes`的使用情况。
  - `-k`：将各硬盘分区以及挂载的文件分区大小用`k`表示。
  - `-t`：列出某一文件系统的所有分区磁盘空间使用情况。
  - `-x`：列出不是某一文件系统的所有分区空间使用情况（`-t`的反作用）。
  - `-T`：列出每个分区所属的文件系统名称。

- ```
  du
  ```

  ：查看文件及目录大小：

  - `-a`：显示所有目录下每个文件所占的空间大小。
  - `-b`：显示目录及其文件大小，以字节为单位。
  - `-c`：显示文件总大小。
  - `-h`：指定大小单位，提高信息可读性，如`KB、MB、GB`等。
  - `-s`：列出各文件大小的总和。
  - `-x`：只计算属于同一文件系统的文件。

- `fsck`：检测和修复Linux文件系统。

- `iostat`：查看磁盘IO的状态。

- `iotop`：与`top`命令类似，实时显示各个进程的io状态。

### 5.6、Linux超级实用的小技巧

  对于上述罗列的指令是一些经常接触Linux系统时常用的指令，如某些指令未列出，可参考：[Linux命令大全。](https://link.juejin.cn?target=https%3A%2F%2Fwww.runoob.com%2Flinux%2Flinux-command-manual.html)不过在Linux系统中，掌握了众多指令的同时，也要熟练Linux系统中的一些操作，接下来分享一些个人看来比较实用的小技巧！

------

①同时执行多条指令时，不同命令之间可以用`;`隔开，如：

> `cd /usr/xxx/;ls`
>  如上指令执行后的结果则是：进入到指定的目录下，并查看该目录下的所有文件信息。

------

②如果执行的某条指令耗时比较长，导致自己当前终端需要等待执行完成，无法继续操作时，可以在指令最后加上`&`符号，将任务交给Linux的后台进程完成，如：

> `cp /xxx/xxx.xx /xxx/xx/xx.xx &`
>  该条指令执行后，会将拷贝文件的任务交给后台进程执行，当前终端可以继续操作。

------

③如果当你想要执行一个他人编写好的脚本文件，但却不知道通过什么命令执行时，你可以尝试在前面通过加个`.`来执行，如下：

> `. xxx.sh`
>  快速执行指定的脚本文件，当然，也不仅仅只局限于`.sh`后缀的脚本，该方式可以用于执行大部分的脚本文件。

------

④如果你想将某个指令执行后的结果，作为其他指令的入参，或想将其执行结果写出到某个文件，那么你可以尝试`>、>>、<`等这些符号，如：

> `ls > xxx.txt`
>  上述命令执行后，会将`ls`指令的执行结果（即当前目录下的所有文件信息）写入到`xxx.txt`文件中，如果`xxx.txt`文件不存在，系统会默认创建出来。同时，你也可以通过`>>`将结果追加到某个文件中，如：`ls >> xxx.txt`。

------

⑤你如果想要同时执行多个命令，但后面的命令需要建立在前面命令执行成功的基础之上时，你可以尝试使用`&&`符号，如：

> `cp /usr/soft/a.txt new.txt && cat new.txt`
>  上述命令中，如果直接执行`cat new.txt`必然是不行的，因为当前目录下不存在`new.txt`文件，所以需要先执行拷贝的命令，将其从其他目录下复制过来后，才能执行`cat`命令。因此，可以将两个指令用`&&`连接，只有当`cp`命令执行成功后，再执行`cat`指令。

------

⑥当执行一个指令耗时比较长或有可能执行失败时，但又得需要考虑执行失败之后的处理，这种情况可以考虑使用`||`，如：

> `wget xxx || wget xxx`
>  执行该指令后，因为`wget`是从网络上下载文件，所以有时会因为网络问题导致失败，此时我们可以通过`||`，再次执行`wget`指令，重新下载。

------

⑦你执行的命令中，下一条指令需要上一条的结果作为入参，那么可以使用`|`管道命令，如下：

> `ps aux | grep java`
>  查询Linux后台的所有进程信息，但是只显示Java的进程。

------

⑧当你想要同时操作Linux中的多个文件时，可以选择使用通配符`*`，如下：

> `rm -rf /xxx/xx/*.txt`
>  如上命令执行之后，会删除指定目录下的所有`.txt`后缀文件。

------

⑨...........

  其实在Linux下还有各种各样的符号操作，如`$、%、?、#、()、[]、{}、(())、[[]]`等，它们都有各自的作用与功能，有些会在`Shell`编写中经常用到，如`$、{}`等。同时，上述介绍的几种小技巧中所用的“符号”，在运维人员手中可能会组成一大长串令人“难以直视”的命令，但作为开发人员，对于前面那些技巧简单掌握即可，毕竟咱也不是专业搞运维或Linux云计算/调度开发的。