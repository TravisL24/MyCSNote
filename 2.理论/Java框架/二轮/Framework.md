# 基本介绍

- 基于POJO轻量级和**最小侵入式开发**
- 通过依入和面向接口实现**松耦合**
- **基于切面**和惯例进行声明式编程
- 通过切面和模板**减少样板式代码**

## 非侵入式

对现有类结构无影响，增强JavaBean

## 松耦合

DAO和Service层 通过 DaoFactory实现松耦合

不让Service层太依赖于Dao

```
private CategoryDao categoryDao = DaoFactory.getInstance().createDao("类地址", CategoryDao.class);
```

## 切面编程

    代理对象去执行具体的方法

## IOC控制反转

外部容器实现对象的创建

IOC容器通过 配置对象的信息：

    -创建对象

    -处理对象间的依赖

    -对象创建时间 / 数量

# Spring模块

<img title="" src="https://raw.githubusercontent.com/TravisL24/pic-repo/main/picGo/2022/09/04/20220904152151.jpg" alt="spring模块.jpg" width="532" data-align="center">

## Core模块

```
applicaitonContext.xml 配置文件 
--> 对象创建 + 对象依赖
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    </beans>
```

### 创建容器

```
BeanFactory：xmlBeanFactory + xml配置文件
    /加载Spring的资源文件
    Resource resource = new ClassPathResource("applicationContext.xml");

    //创建IOC容器对象【IOC容器=工厂类+applicationContext.xml】
    BeanFactory beanFactory = new XmlBeanFactory(resource);

ApplicationContext： 直接通过ClassPathXmlApplicationContext对象来获取
    // 得到IOC容器对象
    ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
```

### 配置对象

```
 1.xml文件
 2.注解
 3.JavaConfig
```

#### 1.xml配置

```xml
修改 applicationContext.xml 文件配置信息
     <!--
        使用bean节点来创建对象
            id属性标识着对象
            name属性代表着要创建对象的类全名
        -->
    <bean id="user" class="User"/>
```

```java
通过IOC容器对象创建User对象
    ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");

    User user = (User) ac.getBean("user"); // 无参方式创建

```

##### 带参数的构造函数创建对象

```xml
<bean id="user" class="User">

  <!--通过constructor这个节点来指定构造函数的参数类型、名称、第几个-->
  <constructor-arg index="0" name="id" type="java.lang.String" value="1"></constructor-arg>
  <constructor-arg index="1" name="username" type="java.lang.String" value="zhongfucheng">        </constructor-arg>
</bean>
```

##### 工厂静态方法创建对象

```
1. 定义工厂静态方法返回对象
    public class Factory {

        public static User getBean() {

            return new User();
        }

    }

2. 配置文件使用工厂静态方法返回
    <!--工厂静态方法创建对象，直接使用class指向静态类，指定静态方法就行了-->
    <bean id="user" class="Factory" factory-method="getBean" >

    </bean>
```

##### 工厂非静态方法创建对象

```
1. 定义非静态方法

2. 配置文件使用工厂的非静态方法返回对象 （在于，先创建工厂对象，再指定方法）
    <!--首先创建工厂对象-->
    <bean id="factory" class="Factory"/>

    <!--指定工厂对象和工厂方法-->
    <bean id="user" class="User" factory-bean="factory" factory-method="getBean"/>
```

##### 装载集合

对象的属性 / 构造函数拥有集合，如何为集合赋值

```xml
构造函数
    <bean id="userService" class="bb.UserService" >
        <constructor-arg >
            <list>
                //普通类型
                <value></value>
            </list>
        </constructor-arg>
    </bean>

属性
    <property name="userDao">
         <list>
             <ref></ref>
         </list>
     </property>
```

#### 2.注解方式

```
1. 先引入context名称空间
    xmlns:context="http://www.springframework.org/schema/context"
2. 开启注解扫描器
    <context:component-scan base-package=""></context:component-scan>

    或者 ==> 自定义扫描类用@CompoentScan修饰符去扫描IOC容器的bean对象
    @Configuration // 表明这是配置类
    //启动扫描器，扫描bb包下的
        //也可以指定多个基础包
        //也可以指定类型
    @ComponentScan("bb")
    public class AnnotationScan {

    }


相关重要注解：
    @ComponentScan : 扫描器
    @Configuration : 这是配置类
    @Component : 把一个对象加入IOC容器
    @Repository : 持久层使用
    @Service  : 业务层
    @Controller : 控制层 
    @Resource : 依赖关系
        -不指定值，按类型找
        -指定值，按名字找
```

#### 3.Java方式

```
装配第三方库组件

1.编写@Configuration修饰的java类 == 配置类
@org.springframework.context.annotation.Configuration
public class Configuration {
    2.使用配置类创建Bean
    @Bean
    public UserDao userDao() {

        UserDao userDao = new UserDao();
        System.out.println("我是在configuration中的"+userDao);
        return userDao;
    }

}
```

### Bean对象创建细节

#### scope属性

```
singleton / prototype 控制对象创建
单例 在IOC容器之前就创建了
多例 在使用的时候才创建
```

#### 其他属性

```
lazy-init ： 单例修改为ture的时候，对象在使用的时候才会创建
init-method ： 对象创建的时候执行某个方法
destroy-method ： 对象销毁的时候执行某个方法


    <bean id="user" class="User" scope="singleton" 
        lazy-init="true" init-method="" destroy-method=""/>
```

### 依赖注入

```
对象之间的依赖 == 给对象上的属性赋值

1. 构造函数
2. set方法
3. 注解
```

## AOP部分

### cglib代理

```
动态代理， 目标对象可以没有接口

==> 构造了一个子类来扩展目标对象的功能
```

```java
//需要实现MethodInterceptor接口
public class ProxyFactory implements MethodInterceptor{

    // 维护目标对象
    private Object target;
    public ProxyFactory(Object target){
        this.target = target;
    }

    // 给目标对象创建代理对象
    public Object getProxyInstance(){
        //1. 工具类
        Enhancer en = new Enhancer();
        //2. 设置父类
        en.setSuperclass(target.getClass());
        //3. 设置回调函数
        en.setCallback(this);
        //4. 创建子类(代理对象)
        return en.create();
    }


    @Override
    public Object intercept(Object obj, Method method, Object[] args,
            MethodProxy proxy) throws Throwable {

        System.out.println("开始事务.....");

        // 执行目标对象的方法
        Object returnValue = method.invoke(target, args);

        System.out.println("提交事务.....");

        return returnValue;
    }

}


public class App {

    public static void main(String[] args) {

        UserDao userDao = new UserDao();
        // 构建了子类
        UserDao factory = (UserDao) new ProxyFactory(userDao).getProxyInstance();

        factory.save();
    }
}
```

### 手动实现AOP

#### 工厂静态方法

```java
public class ProxyFactory {
    //维护目标对象
    private static Object target;

    //维护关键点代码的类
    private static AOP aop;
    public static Object getProxyInstance(Object target_, AOP aop_) {

        //目标对象和关键点代码的类都是通过外界传递进来
        target = target_;
        aop = aop_;

        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        aop.begin();
                        Object returnValue = method.invoke(target, args);
                        aop.close();

                        return returnValue;
                    }
                }
        );
    }
}


//把该对象加入到容器中
@Component
public class AOP {

    public void begin() {
        System.out.println("开始事务");
    }
    public void close() {
        System.out.println("关闭事务");
    }
}


@Component
public class UserDao implements IUser{

    public void save() {

        System.out.println("DB:保存用户");

    }

}


<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">


    <bean id="proxy" class="aa.ProxyFactory" factory-method="getProxyInstance">
        <constructor-arg index="0" ref="userDao"/>
        <constructor-arg index="1" ref="AOP"/>
    </bean>

    <context:component-scan base-package="aa"/>

</beans>

// ------------ test ---------------------
// 代理对象给到接口 IUser

public class App {

    public static void main(String[] args) {

        ApplicationContext ac =
                new ClassPathXmlApplicationContext("aa/applicationContext.xml");


        IUser iUser = (IUser) ac.getBean("proxy");

        iUser.save();

    }
}

```

#### 工厂非静态方法

```java

package aa;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by ozc on 2017/5/11.
 */

public class ProxyFactory {

    public Object getProxyInstance(final Object target_, final AOP aop_) {

        //目标对象和关键点代码的类都是通过外界传递进来

        return Proxy.newProxyInstance(
                target_.getClass().getClassLoader(),
                target_.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        aop_.begin();
                        Object returnValue = method.invoke(target_, args);
                        aop_.close();

                        return returnValue;
                    }
                }
        );
    }
}

// ------------- 先创建工厂bean，再创建代理类对象
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">



    <!--创建工厂-->
    <bean id="factory" class="aa.ProxyFactory"/>


    <!--通过工厂创建代理-->
    <bean id="IUser" class="aa.IUser" factory-bean="factory" factory-method="getProxyInstance">
        <constructor-arg index="0" ref="userDao"/>
        <constructor-arg index="1" ref="AOP"/>
    </bean>


    <context:component-scan base-package="aa"/>


</beans>
```

#### 注解方式

没有接口的话，直接给到对象，使用了cglib代理的方式

```java

@Component
@Aspect//指定为切面类
public class AOP {


    //里面的值为切入点表达式
    @Before("execution(* aa.*.*(..))")
    public void begin() {
        System.out.println("开始事务");
    }


    @After("execution(* aa.*.*(..))")
    public void close() {
        System.out.println("关闭事务");
    }
}


@Component
public class UserDao implements IUser {

    @Override
    public void save() {
        System.out.println("DB:保存用户");
    }

}


public class App {

    public static void main(String[] args) {

        ApplicationContext ac =
                new ClassPathXmlApplicationContext("aa/applicationContext.xml");

        //这里得到的是代理对象....
        IUser iUser = (IUser) ac.getBean("userDao");

        System.out.println(iUser.getClass());

        iUser.save();

    }
}

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">


    <context:component-scan base-package="aa"/>

    <!-- 开启aop注解方式 -->
    <aop:aspectj-autoproxy></aop:aspectj-autoproxy>

</beans>
```

#### 优化和AOP注解API

```
API：
@Aspect 指定一个类为切面类
@Pointcut("execution( cn.itcast.e_aop_anno..*(..))") 指定切入点表达式
@Before("pointCut_()") 前置通知: 目标方法之前执行
@After("pointCut_()") 后置通知：目标方法之后执行（始终执行）
@AfterReturning("pointCut_()") 返回后通知： 执行方法结束前执行(异常不执行)
@AfterThrowing("pointCut_()") 异常通知: 出现异常时候执行
@Around("pointCut_()") 环绕通知： 环绕目标方法执行
```

```java
优化： 直接@Pointcut指定切入的表达式

@Component
@Aspect//指定为切面类
public class AOP {


    // 指定切入点表达式，拦截哪个类的哪些方法
    @Pointcut("execution(* aa.*.*(..))")
    public void pt() {

    }

    @Before("pt()")
    public void begin() {
        System.out.println("开始事务");
    }


    @After("pt()")
    public void close() {
        System.out.println("关闭事务");
    }
}


```

#### 切入点语法分析：

```
execution(modifiers-pattern? ret-type-pattern declaring-type-pattern? name-pattern(param-pattern) throws-pattern?)

?号代表0或1，可以不写
“*”号代表任意类型，0或多
方法参数为..表示为可变参数

modifiers-pattern?【修饰的类型，可以不写】
ret-type-pattern【方法返回值类型，必写】
declaring-type-pattern?【方法声明的类型，可以不写】
name-pattern(param-pattern)【要匹配的名称，括号里面是方法的参数】
throws-pattern?【方法抛出的异常类型，可以不写】
```

## Spring事务

```
事务回滚：
    如果是编译时异常不会自动回滚，如果是运行时异常，那会自动回滚！

    当前类 ，没有事务的方法调用有事物的方法，会触发事务吗？
        --不会，因为事务时AOP实现的，本质上是一个代理对象去完成调用事务方法，但是事务方法还是由原本的target对象自己去完成
    其他类，会触发事务，因为是代理对象自己去实现事务方法了

```

### 事务传播机制

```
Spring事务基于Spring AOP，Spring AOP底层用的动态代理，动态代理有两种方式：

基于接口代理(JDK代理)
    基于接口代理，凡是类的方法非public修饰，或者用了static关键字修饰，那这些方法都不能被Spring AOP增强
基于CGLib代理(子类代理)
    基于子类代理，凡是类的方法使用了private、static、final修饰，那这些方法都不能被Spring AOP增强
```

### 多线程问题

```
使用ThreadLocal对有状态的Bean封装
```

### BPP BeanPostProcessor

对 对象 做一个加工处理

```
Bean的生命周期：
    1. ResourceLoader 加载配置信息
    2. BeanDefintionReader 解析配置信息， 生成一个个BeanDefintion
    3. BeanDefintionRegistry管理BeanDefintion
    4. BeanFactoryPostProcessor对配置信息加工
    5. 实例化Bean
    6. InstantiationAwareBean，非必要
    7. BeanWarpper 完成对象属性配置(依赖)
    8. Aware接口，非必要
    9. BeanPostProcessor的before方法，非必要
    10. init-method或者实现InstantiationBean，非必要
    11. BeanPostProcessor的after方法，非必要
    12. 对象放入HashMap
    13. destroy或者DisposableBean
```

### 事务接口

```
编程式事务：
    TransactionDefinition： 事务的属性
    TransactionStatus： 事务的具体状态
    PlatformTransactionManager： 事务管理器接口，定义了一组行为

声明式事务： 
    TransactionProxyFactoryBean：生成代理对象
    TransactionInterceptor：实现对象的拦截
    TransactionAttrubute：事务配置的数据
```

# SpringMVC

![2947363386-5aaa69246a4a2_fix732.png](https://raw.githubusercontent.com/TravisL24/pic-repo/main/picGo/2022/09/07/20220907161927.png)

```
用户请求 -> DispatcherServlet 
   先 -> HandlerMapping映射器去看请求路径
   再 -> HanlerAdapter查看那些类实现了Controller接口/bean对象
               数据转换 -> Controller里面去处理业务返回一个 ModelAndView对象
    回到 -> DispatcherServlet，解析ModelAndView
```

## 参数绑定

Web端的值接收到Controller中处理

```
默认参数：
    HttpServletRequest：通过request对象获取请求信息
    HttpServletResponse：通过response处理响应信息
    HttpSession：通过session对象获取session中存放的对象
    Model：把model数据填充到request域中

简单类型：
    @RequestParam 参数绑定 / request传入参数名称和controller参数一致

pojo绑定：
    页面input的name和contriller的pojo形参中的属性一致的话，直接绑定到pojo

自定义参数绑定：
    日期类型，把请求日期数据串转换成日期类型
```

## controller方法的返回值

```
1. ModelAndView
    方法结束的时候定义ModelAndView，model和view分别设置
2. String
    - 逻辑视图名
    - redirect重定向
    - forward页面转发
3. void
    方法形参定义request和response
    - request转向页面； request.getRequestDispatcher("页面路径").forward(request, response)
    - response页面重定向： response.sendRedirect("url")
    - response 指定响应结果
```

## 集合类型绑定

```
1. 数组绑定
    controller方法参数使用：(Integer[] itemId)
   页面统一使用:itemId 作为name
2. list绑定
    pojo属性名为：itemsList
   页面：itemsList[index].属性名
3. map绑定
    pojo属性名为：Map<String, Object> itemInfo = new HashMap<String, Object>();
   页面：<td>姓名：<inputtype="text"name="itemInfo['name']"/>
```

## 异常处理

```
springmvc提供全局异常处理器（一个系统只有一个异常处理器）进行统一异常处理。

dao -> service -> controller -> 前端控制器 -> 全局异常处理器(HandlerExceptionResolver)
```

## 拦截器

```
1. 定义拦截器，实现 HandlerIntercepter接口
    preHandle
    postHandle
    afterCompletion： Handler执行完以后，统一异常处理
```

# MyBatis

```
spring + MyBatis整合
    1. 整合dao层，spring管理mapper接口，mapper扫描器自动扫描mapper接口在spring中进行注册

    2.整合service层，spring管理service接口

    3.整合springmvc

主要配置：
    1. mybatis配置文件sqlMapConfig.xml配置别名自动扫描(实体类)
    2. mapper扫描器(接口，数据库访问接口)
    3. 数据库连接池配置
    4. 声明式事务配置
    5. 启动注解扫描 <context:component-scan base-package="cn.itcast.ssm.controller"></context:component-scan>
    6. 配置注解映射器和适配器 <mvc:annotation-driven></mvc:annotation-driven>
    7. 视图解析器 <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
    8. 配置控制类 DispatcherServlet前端控制器
    9. 配置spring配置文件加载类 ClassLoaderListener
```

## 视图解析器配置

![23152935_623acc5fd5e2346691.webp](https://raw.githubusercontent.com/TravisL24/pic-repo/main/picGo/2022/09/08/20220908200512.webp)

## sqlMapConfig.xml

![23152945_623acc691d1e644801.webp](https://raw.githubusercontent.com/TravisL24/pic-repo/main/picGo/2022/09/08/20220908200551.webp)

## 配置数据源

![23153059_623accb32f0e888205.webp](https://raw.githubusercontent.com/TravisL24/pic-repo/main/picGo/2022/09/08/20220908200704.webp)

## 事务控制

![23153113_623accc1aa44e2055.webp](https://raw.githubusercontent.com/TravisL24/pic-repo/main/picGo/2022/09/08/20220908200759.webp)

## spring配置

扫描上面配置得所有文件

![23153125_623acccd507131029.webp](https://raw.githubusercontent.com/TravisL24/pic-repo/main/picGo/2022/09/08/20220908200913.webp)

# SpringBoot

## 1.Controller的使用

@RestController = @Controller + @ ResponseBody

## 2.统一处理异常

单独封装一个类来返回异常的json

## 3.自动装配原理

```
@SpringBootApplication：
    主要有三个重要的注解
    @SpringBootConfiguration: 支持以JavaConfig的方式来配置
    @EnableAutoConfiguration： 开启自动配置
    @ComponentScan： 扫描注解
```

### @EnableAutoConfiguration

```
@AutoConfiguartionPackage : 自动配置包，扫描的是@Entity注解

@Import ： 给IOC容器导入组件
    Spring启动的时候扫描所有jar路径下面的META-INF/spring.factories,包装成Properties对象
    从Properties对象获取到key值为EnableAutoConfiguration的数据，并添加到容器里

```

# SpringCloud

## CAP理论

```
C：一致性 consistency
    所有节点拥有数据的最新版本
A：可用性 availability
    数据具备高可用性
P：分区容错性 partition-tolerance
    容忍网络出现分区，分区之间网络不可达
```

## cloud的基础功能

```
Nacos：服务治理
Ribbon：客户端负载均衡
OpenFeign：声明式服务调用
Sentienl: 流量控制、熔断降级
```

### Nacos

```
服务注册发现 + 服务健康检测：
    支持基于DNS和基于RPC的服务发现
    服务端可以通过SDK或者Api进行服务注册
    服务消费者可以使用DNS或者Http查找的方式获取服务列表
动态配置服务：
    服务的配置信息分环境分类别外部管理，并支持热更新
    配置信息存在数据库中，支持配置信息的监听和版本回滚
动态DNS：
    支持权重路由，更好的实现中间层负载均衡
服务+元数据管理
    管理数据中心的所有服务及元数据
```

### RestTemplate和Ribbon

服务治理框架下，通过服务名来获取具体的服务实例位置(ip).

直接使用Spring封装好的RestTemplate工具类

```java
  
    // 传统的方式，直接显示写死IP是不好的！
    //private static final String REST_URL_PREFIX = "http://localhost:8001";
    
    // 服务实例名
    private static final String REST_URL_PREFIX = "http://MICROSERVICECLOUD-DEPT";

    /**
     * 使用 使用restTemplate访问restful接口非常的简单粗暴无脑。 (url, requestMap,
     * ResponseBean.class)这三个参数分别代表 REST请求地址、请求参数、HTTP响应转换被转换成的对象类型。
     */
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/consumer/dept/add")
    public boolean add(Dept dept) {
        return restTemplate.postForObject(REST_URL_PREFIX + "/dept/add", dept, Boolean.class);
    }
```

负载均衡：

    1.客户端负载均衡 Ribbon

        -服务实例的清单在 **客户端**，客户端进行负载均衡算法分配

    2.服务端负载均衡 Nginx

        -清单在服务端，服务端进行负载均衡分配

#### Ribbon细节

默认负载策略是轮询，可以自定义自己的策略

```java
@Configuration
public class MySelfRule
{
    @Bean
    public IRule myRule()
    {
        //return new RandomRule();// Ribbon默认是轮询，我自定义为随机
        //return new RoundRobinRule();// Ribbon默认是轮询，我自定义为随机
        
        return new RandomRule_ZY();// 我自定义为每台机器5次
    }
}
```



### OpenFeign

Feign是一种声明式、模板化的HTTP客户端。

在Spring Cloud中使用Feign, 我们可以做到使用HTTP请求远程服务时能与调用本地方法一样的编码体验。

#### 服务绑定

```java

// value --->指定调用哪个服务
// fallbackFactory--->熔断器的降级提示
@FeignClient(value = "MICROSERVICECLOUD-DEPT", fallbackFactory = DeptClientServiceFallbackFactory.class)
public interface DeptClientService {


    // 采用Feign我们可以使用SpringMVC的注解来对服务进行绑定！
    @RequestMapping(value = "/dept/get/{id}", method = RequestMethod.GET)
    public Dept get(@PathVariable("id") long id);

    @RequestMapping(value = "/dept/list", method = RequestMethod.GET)
    public List<Dept> list();

    @RequestMapping(value = "/dept/add", method = RequestMethod.POST)
    public boolean add(Dept dept);
}
```

#### 熔断器

```java

```