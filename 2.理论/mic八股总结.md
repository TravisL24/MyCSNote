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
