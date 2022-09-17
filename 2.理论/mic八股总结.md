# 框架

## 1.SpringBoot 约定优于配置

1.一种软件设计的范式，核心思想是减少软件开发人员对于配置项的维护。

2.传统的Spring框架开发需要做很多配置：

    --管理jar包依赖

    --web.xml维护

    --Dispatch-Servlet.xml配置项维护

    --应用部署到Web容器中

    --第三方组件集成到Spring IOC容器中的配置项维护

3.-SpringBootStarter启用依赖可以管理所有的jar包版本

    -如果应用了mvc的相关jar，Spring Boot会自动内置Tomcat容器来运行web应用，无需单独部署

    -SpringBoot的自动装配机制，通过扫描约定路径下面的Spring.factories文件识别配置类，实现Bean的自动装配

    -默认加载的配置文件是 applicaiton.properties

## 2.如何理解SpringBoot里面的Starter？

   Starter 是Spring Boot 的四大核心功能特性之一，除此之外，Spring Boot 还有自动装配、Actuator监控等特性。这些特性是为了让开发者只需要关心业务逻辑，减少对配置和外部环境的依赖。

    Starter是启动项依赖，主要有一下几个作用：

    --<u>以功能为维度，来维护对应的jar包版本依赖</u>。Starter组件会把对应功能的所有jar包依赖全部导入进来，避免自己引入的麻烦。

    --<u>Starter内部集成了自动装配的机制</u>，程序依赖某个组件以后，组件会自动集成到Spring生态下，并对相关的Bean进行一个管理。只需要在application.properties文件中维护就行了。这也是**约定优于配置**的理念的体现。

## 3.Spring Boot中自动装配的原理？

    自动装配：自动的把第三方组件的Bean装配到Spring IOC容器里面。

    @SpringBootApplication注解是一个复合注解，由 @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan实现。实现自动装配主要靠的就是@EnableAutoConfiguration注解。

    自动装配的实现主要是靠三个核心技术:

    1.引入Starter启动依赖组件的时候，<u>组件里必须要有@Configuration配置类</u>，在这个配置类里面通过@Bean注解生命需要装配到IOC容器的Bean对象。

    2.配置类放在第三方jar包里面，然后通过SpringBoot中约定优于配置的思想，把配置类全路径放在clasapath:/META-INF/spring.factories文件中，这一步主要靠**SpringFactoriesLoader**完成。

    3.SpringBoot拿到配置类之后，通过**ImportSelector接口**，实现这些配置类的动态加载。

## 4.spring如何解决循环依赖？

    循环依赖：两个或者多个Bean互相持有对方的引用就会发生循环依赖，会导致注入死循环。

    循环依赖有三种形态：

        1.互相依赖。a，b互相依赖。

        2.三者依赖。a，b，c之间循环依赖。

        3.自我依赖。自己依赖自己。

```
PS：
对象创建的过程：
    getBean(classA)
    1.先从缓存里找，找不到往下走
    2.实例化classA(new)
    3.依赖注入classA的成员变量(setter方法) ==> 去getBean(classB) 
                            [相同的创建对象操作，此时A已经实例化了，B初始化完成后，进入一级缓存]
    4.初始化classA
    5.最终的classA的引用进入一级缓存

三级缓存分别是什么：
    1.一级缓存：完全创建完毕的Bean实例
    2.二级缓存：创建中的Bean实例
    3.三级缓存：Objectfactory对象存储提前暴露的Bean实例的引用(为了AOP的代理对象而服务的)
```

![d6f63135eed5564efe166c7b472d8fab.png](https://raw.githubusercontent.com/TravisL24/pic-repo/main/picGo/2022/09/12/20220912134626.png)

   

     <u>解决方案</u>：Spring设计了三级缓存去解决循环依赖的问题。调用getBean()方法的时候，Spring会先去一级缓存里寻找目标Bean，如果一级缓存没有，就去二级缓存找，如果都没有说明该Bean还没有被实例化。Spring容器会去实例化目标Bean，并标记是否存在循环依赖。，否则，便会标记该Bean 存在循环依赖，然后将等待下一次轮询赋值，也就是解析@Autowired 注解。等@Autowired 注解赋值完成后（PS：完成赋值的Bean 称为成熟Bean） ，会将目标Bean 存入到一级缓存。

## 5.Spring IOC /DI的理解

    IOC是<u>控制反转</u>。核心思想是把对象的管理权限交给容器。如果程序需要用到某个实例，直接去IOC容器中获取就行，降低了对象与对象之间的耦合性。

    IOC的**工作流程**：

        1.<u>IOC容器初始化</u>。通过xml / 注解等Bean的声明方式，通过解析和加载后生成BeanDefinition，然后把BeanDefinition注册到IOC容器中，其中包含了这个bean定义的基本属性。IOC容器就是对这些注册Bean的定义信息进行处理和维护。

        2.<u>完成Bean初始化和依赖注入</u>。通过反射针对没有设置lazy-init属性的单例bean进行初始化。完成Bean的依赖注入。

        3.<u>Bean的使用</u>。通过@Autowired / BeanFactory.getBean()获取到指定的bean实例。

## 6.Spring MVC的理解

    Spring MVC是属于是Spring FrameWork生态里的一个模块，是在Servlet基础上构建并使用MVC模式设计的一个Web框架。

    Spring MVC的整体架构设计对Java Web里面的MVC架构模式做了增强和扩展，分一下几个放面。

        --传统MVC框架的Controller ==> 前端控制器 DispatcherServlet + 后端控制器Controller

        --Model模型 ==> 业务层Sevice + 数据访问层 Repository

        --视图层可以支持不同的视图，JSP，Freemark等

   **具体工作流程**： 

        ①浏览器请求经过核心控制器DispatcherServlet，把请求分发到对应的Controller里面。 ②Controller里面处理完业务逻辑之后，返回ModelAndView。

        ③DispatcherServlet寻找到ViewResolver视图解析器，找到ModelAndView指定的视图，并将数据显示到客户端。

## 7.为什么要使用Spring框架？

    1.轻量。基本版本2MB。

    2.IoC/DI：通过IOC容器实现了Bean的生命周期管理，通过DI实现依赖注入，从而实现对象以来的松耦合管理。

    3.AOP：支持面向切面的编程把应用业务逻辑和系统服务分开。

    4.MVC框架：Spring MVC提供功能更强大且灵活的Web框架支持。

    5.事务管理：通过AOP实现了事务的统一管理，对应用开发中的事务处理提供了非常灵活的支持。

## 8.Spring中有两个id相同的Bean，会出什么问题？

    1.id的属性是表示Bean的唯一标志符号，在Spring启动的时候就会验证id的唯一性，重复就会报错，发生在**Spring对XML文件解析转化为BeanDefinition**的阶段。

    2.如果是两个不同的Spring配置文件，有两个相同的bean，IOC容器加载的时候，默认会<u>对多个相同id的bean做覆盖</u>。

    3.使用注解去声明Bean的时候，只会注册第一个声明的Bean实例。使用@Autowired根据类型实现依赖注入的时候，会提示没有后面的Bean。使用@Resource注解按名称实现注入的时候，会出现类型不一致。发生在**Bean初始化后的依赖注入**阶段。

## 9.Spring中Bean的作用域？

    我们常规是用xml文件或者注解告诉IOC容器，那些Bean需要被IOC容器管理，所以每个Bean实例都是存在生命周期的，即作用域。

    1.singleton，单例。整个Spring容器中只有一个Bean实例。

    2.prototype，原型。每次从IOC容器获取指定Bean的时候，都是一个新的实例对象。

    还有<u>基于会话维度</u>控制Bean生命周期。

    3.request，每一次http请求都会创建一个新的Bean。

    4.session，同一个session共享同一个Bean实例，反之，是不同的

    5.globalSession，针对全局Session的维度，共享同一个bean.

## 10.BeanFactory和FactoryBean的区别？

    Spring的核心功能是IOC容器，本质上就是一个Bean的工厂。它能根据xml里面声明的Bean配置进行bean的加载和初始化，然后BeanFactory去生产各种Bean。

    <u>BeanFactory是所有Spring Bean容器的顶级接口</u>，为容器定义了一套规范，并且提供了getBean这种方法从容器中获取指定的Bean实例。BeanFactory还解决了Bean之间的依赖注入。

    <u>FactoryBean是一个工厂Bean，他是一个接口</u>。主要是动态的生成某一个类型的Bean实例，我们可以自定义一个Bean并且加载到IOC容器里面，有一个重要的方法就是getObject()，可以动态的构建Bean。

## 11.@Resource和@Autowired的区别

    @Autowired是<u>Spring里面提供的一个注解</u>，默认是**根据类型实现Bean的依赖注入**。有一个required属性默认为true，表示强制要求bean实例的注入。如果存在多个相同类型的Bean实例，需要@Primary / @Qualifier 注解去决定注入的Bean实例。

    @Resource是JDK提供的注解，Spring只是提供了这个注解的支持。**区别在于@Resource可以支持 ByName 和 ByType两种方式注入**。如果没有配置，默认从名字先匹配，再按照类型去匹配。

## 12. Cookie和Session的区别？

    **Cookie是客户端浏览器保存服务端数据的一种机制**。通过浏览器网页访问的时候，<u>浏览器可以把某一些状态数据以K-V的方式写入到**Cookie并存储到客户端浏览器**中</u>。

    **Session表示一个会话，是属于服务器端的容器对象**，每一个浏览器的请求，Servlet容器都会分配一个Session。<u>Session的本质是一个ConcurrentHashMap</u>，可以<u>存储当前会话的一些状态数据</u>。服务端可以利用session来存储客户端在同一次会话里面的多次请求记录。

    **实现有状态的http协议方式**：

        1.客户端第一次访问服务端，服务端创建Session，并生成唯一的sessionId。

        2.服务端把sessionId写入客户端浏览器的Cookie中，在客户端实现保存。

        3.后续请求，通过sessionId，可以让服务器识别当前的会话状态。

# MySQL

## 1.如何保证MySQL的高可用性？

高可用性：描述一个系统正常时间段内正常运行的时间

影响MySQL不可用的<u>因素</u>：

    1.机器资源耗尽

    2.单点故障 ** （重点）

    3.人为操作

    4.网络

**解决单点故障的方案**：主从集群。

    <u>请求的转发</u>。使用路由转发，保证主集群写，从集群读。

    <u>保证一致性</u>。

            写请求进主库，会写入到bin log。

            1.从库使用IO Thread向主库索取对应的bin log文件。

            2.主库dump对应的bin log文件 发给从库。

            3.从库把bin log写入到 relay log。

            4.从库读取 relay log并执行。

## 2.MySQL 事务实现的原理

    MySQL的事务满足ACID特性，所以事务的实现原理就是InnoDB如何保证ACID特性。

    A表示**原子性**。InnoDB设计了一个**undo_log表**，事务执行过程中，将修改之前的数据快照保存到undo_log中，出现错误就从undo_log里面读取数据执行反操作。

    C表示**一致性**。主要依赖于**业务层面**去保障。本身也可以通过主键唯一余数，字段长度和类型的保障。

    I表示**隔离性**。在多个并行事务对同一数据进行操作的时候，避免多个事务的干扰导致数据混乱。InnoDB提供**四种隔离级别**的实现。未提交读 + 已提交读 + 可重复读 + 串行化。 默认是RR，可重复读，然后使用了MVCC机制解决了脏读和不可重复读的问题，再用行锁/表锁实现了幻读的方式。

    D表示**持久性**。只要事务提交成功，数据的结果影响一定是永久的。InnoDB设计了**Buffer Pool缓冲区**，数据变更<u>先进缓冲区，再找机会持久化到磁盘</u>。为了避免持久化的时候宕机，使用了**redo_log文件**，存储了数据被修改之后的值，提交事务得时候可以直接把本次修改的值刷到磁盘上持久化。

## 3.MySQL性能优化

    MySQL性能优化从四个部分优化。(硬件/操作系统层面 + 架构层面 + MySQL程序配置 + SQL优化)

    1.**硬件/操作系统**。主要是CPU，内存，网络，磁盘读写。

    2.**架构设计层面**。MySQL是磁盘IO访问非常频繁的关系数据库。

        -- <u>搭建MySQL主从集群</u>。保证服务的高可用性。

        --<u>读写分离设计</u>。在读多写少的场景，可以避免读写冲突造成的性能影响。

        --<u>分库分表机制</u>。分库可以降低单个服务器节点的IO压力。分表可以降低单表数据量，提高sql查询效率。

        --<u>针对热点数据</u>。引入Redis等缓解MySQL压力。

    3.**程序配置**。对最大连接数，binlog日志，buffer pool做修改。

    4.**SQL优化**。

        --慢查询日志+工具得到有问题的SQL列表。

        --用Explain来查看，关注type， key， rows， filterd等字段

        --show profile工具，查看资源消耗问题。

    

## 4.b树和b+树的理解

    **B树**：是一种多路平衡查找树，满足平衡二叉树的规则，但是它可以有多个子树，子树的数量取决于关键字的数量。所以相同数据量的情况下，B树的高度要低于平衡二叉树。

    **B+树**：在B树做了增强。

        1.B+树的数据是存储在叶子节点上，并且通过链表的方式把叶子节点中的数据进行连接。

        2.B+树的子路数量等于关键字数。

    B树和B+树都是应用在文件系统和数据库系统中，用来<u>减少磁盘IO带来的性能损耗</u>。InnoDB对存储在磁盘上的数据建立了一个索引，然后把索引数据及索引列对应的磁盘地址，以B+树的方式存储。



# Redis

## 1.Redis存在线程安全问题吗？

    Redis**服务端层面**。Redis Server本身是一个线程安全的K-V数据库。所以在Server层面上**不会存在线程安全问题**。虽然Redis 6.0增加了多线程模型，但是多线程是用来处理网络IO事件的，<u>指令部分仍然是主线程处理，所以不存在多个线程执行操作指令的问题</u>。

    Redis**客户端层面**。虽然 Server中的指令是原子的，但是如果多个Redis客户端同时执行多个指令的时候就**无法保证原子性**。<u>解决方法</u>有：Redis的原子指令 / 多客户端资源访问加锁 / Lua脚本实现。

## 2.Redis的内存淘汰算法和原理是什么？

    **内存淘汰算法**：

        1.Random算法

        2.TTL算法

        3.LRU算法

        4.LFU算法。多了一个访问频率的部分。

## 3.Redis和MySQL如何保证数据一致性？

    1.更新数据库，再更新缓存

        如果缓存更新失败，就会不一致。

    2.删除缓存，再更新数据库

        理论上可以。但删除缓存和 更新数据库不是原子性的。利用MQ消息队列的可靠性消息通信来实现最终一致性。

## 4.Redis中的AOF重写过程？

    AOF是Redis中的一种数据持久化方式，采用了<u>指令追加</u>的方式。因为是实时的实现数据指令的持久化，aof文件可能会过大，造成IO性能问题。

    **AOF重写机制**：把AOF文件中相同的指令进行压缩，只保留最新的指令。具体分为三步：

    1.根据当前Redis内存里的数据，重新<u>构建新的AOF文件</u>。

    2.读取Redis当前数据，<u>写入到新的AOF文件</u>。

    3.重写完成后，<u>替代旧的AOF文件</u>。        

这个过程比较耗时，放到后台子进程去完成。同时为了<u>在重写时，数据还在变化，Redis会把新的变更追加到AOF的重写缓冲区中</u>，重写完成后，把缓冲区的内容追加到AOF文件中。

# 并发编程

## 1.AQS？

    AQS是JUC包中核心的一个抽象类，它为多线程访问共享资源提供了一个队列同步器。内部分两个部分实现 **volatile修饰的state变量 + 双向链表维护的FIFO等待队列。**

    等待队列的工作原理是：多个线程通过对state共享变量进行修改实现竞态条件，竞争失败的线程加入到FIFO队列并阻塞。抢到资源的线程释放之后，后续线程按FIFO的顺序实现有序唤醒。资源分 **独占资源** + **共享资源**。

## 2.AQS为什么使用双向链表？

    <u>双向链表的特征</u>是：两个指针，一个指向前置节点，一个指向后置节点。所以双向链表可以支持常量O(1)时间内找到前驱节点，并且在插入和删除比单向链表更方便。

    AQS使用的三个点：

        --没有竞争到锁的线程进入阻塞队列并阻塞等待的前提是：<u>前置节点是正常状态</u>。避免链表中存在异常线程导致后续无法唤醒的问题。所以<u>阻塞前要判断前置节点状态</u>。

        --Lock接口有一个lockInterruptibly()方法，表示处于阻塞的线程允许被中断。当<u>这个线程被唤醒并中断后</u>，被修改为CANCELLED状态，<u>并没有从等待队列中移除。后续移除时</u>，单向链表需要从头开始遍历，效率低并且容易和锁唤醒操作竞争。

        --刚加入队列的线程会自旋尝试竞争锁。但是按公平锁设计，只有第一个节点才有必要去竞争，<u>判断是否为第一个节点也需要判断前置节点是否为头节点</u>。

## 3.ConcurrentHashMap的size()方法是线程安全的吗？

    size()方法是**不安全**的。size()方法是一个非同步方法，put()和size()方法并没有实现同步锁。put()的逻辑是：在hash表上添加/修改某个元素，然后对总的元素个数进行累加。**线程的安全性局限在hash表数组力度的锁同步，避免数据竞争带来的安全问题**。

    数组元素个数的累加有<u>两种方案</u>：

        1.线程竞争不激烈的时候，直接使用 CAS对一个long类型的变量做原子递增。

        2.激烈的时候使用一个CounterCell数组，基于分而治之的思想减少多线程竞争。size()会遍历数组累加，汇总得到结果。

    size()不加锁！因为<u>加锁会造成数据写入的并发冲突</u>，如果是读写锁会让put的锁范围扩大，都是<u>对性能有影响的</u>。

## 4.wait和notify为什么要放在synchronized代码块内？

    wait和notify用于实现多线程之间的协调，wait让线程阻塞，notify让阻塞的线程唤醒。多线程通过这种方式实现线程之间的通信。**实现通信除了管道流以外，只有通过修改共享变量来实现**，线程1修改变量s，线程2获取修改后的s完成通信。但优于多线程本身可以并行，所以线程2访问s前需要知道线程1修改过s，否则等待。**让线程知道s被修改过的办法就需要synchronized关键词实现一个互斥条件**。通过共享变量实现通信的场景，必须要竞争到变量的锁资源。<u>有了synchronized同步锁，就可以实现多个通信线程之间的互斥，实现条件等待和唤醒</u>。

    另外，JDK强制要求wait/notify写在同步代码块里面，否则会抛出IllegalMonitorStateException。





    
