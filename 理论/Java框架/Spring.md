# Spring框架

```
主要支持模块：
	1.IoC和AOP的容器
	2.JDBC和ORM的数据访问模块
	3.声明式事务的模块
	4.基于Servlet的MVC开发
	5.Reactive的Web开发
	6.JMS、JavaMail、JMX、缓存等模块
```



## IoC容器

```
-管理所有轻量级 JavaBean组件
-提供底层服务
	-组件生命周期管理、配置、组装
	-AOP支持
	-AOP为基础的声明式事务服务
```

### 原理

```
inversion of Control ==> 控制反转
```

```
核心需求：
	1.谁来创建组件？
	2.谁来根据依赖关系组装组件？
	3.如何正确的按照顺序销毁组件？
```

```java
// 让组件在IoC容器中被“装配”出来，就需要“注入”机制 
// (依赖注入 Dependency Injection)
public class BookService {
    private DataSource dataSource;

    // 等待外部用set方法来注入一个DataSource，dataSource怎么来的不需要关心
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}

// 组件的依赖关系和组件的创建需要提前配置，XML文件可以做
<beans>
    <bean id="dataSource" class="HikariDataSource" />
    <bean id="bookService" class="BookService">
        <property name="dataSource" ref="dataSource" /> // 依赖关系就出来了
    </bean>
    <bean id="userService" class="UserService">
        <property name="dataSource" ref="dataSource" />
    </bean>
</beans>
```

#### 依赖注入方式

```java
// set()方法注入 和 构造方法注入
// set方法上面有
// 构造方法注入
public class BookService {
    private DataSource dataSource;
	
    // 构造的时候直接把需要的这个组件给放进去
    public BookService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```



### 装配Bean

```xml
// <bean ...> id标识
<bean id=" " class=" ">
    
// userService的Bean里面使用注入别的Bean
<property name="..." ref="..." />
        
// 注入boolean、int、String这类的数据用 value 来做
<property name=" " value=" " />	
```

```java
// IoC容器得创建的把
// 创建容器的同时，还把配置文件里的bean给配置好了
ApplicationContext context = new ClassPathXmlApplicationContext(".xml");

// 获取Bean
UserService userService = context.getBean(UserService.class);
// 正常调用
User user = userService.login("", "");
```

#### ApplicationContext

```
Spring的容器，一个接口，有很多的实现的类
```

#### BeanFactory

```java
// 另一种容器，也是类似的
BeanFactory factory = new XmlBeanFactory(new ClassPathResource(".xml"));
MailService mailService = factory.getBean(MailService.class);
```

```
ApplicationContext是一次性创建所有的Bean
BeanFactory是第一次获取这个Bean的时候才会去创建它
```



### Annotation配置

```java
// component注解就是定义了这么一个bean
@Component
public class MailService {
    ...
}

@Component
public class UserService {
    // Autowired注解就是把对应类型的Bean注入到对应的字段里面
    @Autowired
    MailService mailService;

    ...
}

// ======容器需要启动起来===
// configuration注解说明这是一个配置类
// ComponentScan自动搜索所在包和子包@component Bean创建出来和@Autowired自动装配
@Configuration
@ComponentScan 
public class AppConfig {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class); //在这边传进去了
        UserService userService = context.getBean(UserService.class);
        User user = userService.login(" ", " ");
        System.out.println(user.getName());
    }
}
```



### 定制Bean

```
Singleton 单例：容器初始化的时候创建bean，容器关闭的时候销毁bean
Prototype 原型：getBean的时候都是新的实例，这种需要@Scope注解
```

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // @Scope("prototype")
public class MailSession {
    ...
}
```

#### 注入List

```java
@Component
public class Validators {
    @Autowired
    List<Validator> validators; // 所有的Validator的Bean都被注入了

    public void validate(String email, String password, String name) {
        for (var validator : this.validators) {
            validator.validate(email, password, name);
        }
    }
}
```

```java
// 定义接口
public interface Validator {
    void validate(String email, String password, String name);
}
// 实现接口
// @Order注解可以指定Bean的顺序
@Component
@Order(1)
public class EmailValidator implements Validator {
    public void validate(String email, String password, String name) {
    }
}

@Component
@Order(2)
public class PasswordValidator implements Validator {
    public void validate(String email, String password, String name) {
    }
}

@Component
@Order(3)
public class NameValidator implements Validator {
    public void validate(String email, String password, String name) {
    }
}
```

#### 可选注入

```
 @Autowired(required = false)
 没有找到就忽略，不会报错
```

#### 创建第三方Bean

```java
// 在@Configuration类里面写一个方法创建并返回，标记为@Bean
@Configuration
@ComponentScan
public class AppConfig {
    // 创建一个Bean:
    @Bean // 只会调用一次
    ZoneId createZoneId() {
        return ZoneId.of("Z");
    }
}
```



#### 初始化和销毁

```
Bean需要初始化，也需要清理一些资源
init() -> 初始化
shutdown() -> 执行清理
// 需要引入注解
<dependency>
    <groupId>javax.annotation</groupId>
    <artifactId>javax.annotation-api</artifactId>
    <version>1.3.2</version>
</dependency>

详细流程：
    1.调用构造方法创建实例；
    2.根据@Autowired进行注入；
    3.调用标记有@PostConstruct的init()方法进行初始化。
    4.而销毁时，容器会首先调用标记有@PreDestroy的shutdown()方法。
```

```java
@Component
public class MailService {
    @Autowired(required = false)
    ZoneId zoneId = ZoneId.systemDefault();

    @PostConstruct // 初始化需要标记
    public void init() {
        System.out.println("Init mail service with zoneId = " + this.zoneId);
    }

    @PreDestroy // 销毁需要标记
    public void shutdown() {
        System.out.println("Shutdown mail service");
    }
}
```

#### 别名

```
一个类型的Bean只有一个实例，但需要多个实例的时候就要选择不一样的名字
    1.@Bean("name")
    2.@Bean + @Qualifier("name")

在注入的时候需要
@Qualifier("name") 找到对应的
或者定义的时候设置一个@Primary
```



#### FactoryBean的使用

```java
//先实例化，再调用getObject()去创建真正的Bean
// e.g.
@Component
public class ZoneIdFactoryBean implements FactoryBean<ZoneId> {

    String zone = "Z";

    @Override
    public ZoneId getObject() throws Exception {
        return ZoneId.of(zone); // 这里才是真正的返回Bean
    }

    @Override
    public Class<?> getObjectType() { // 用来指定Bean的类型的
        return ZoneId.class;
    }
}
```



### Resource的使用

```java
// 把文件也注入进来，便于读取
@Component
public class AppService {
    @Value("classpath:/logo.txt")
    private Resource resource; // 这里就是注入资源了

    private String logo;

    @PostConstruct
    public void init() throws IOException {
        // 可以直接调用，不用自己去搜索了
        try (var reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            this.logo = reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
```



### 注入配置

```
配置文件一般用key-value形式写在.properties文件里面

用Resource来读取毕竟麻烦，可以用@PropertySource直接读配置文件
```

```java
@Configuration
@ComponentScan
@PropertySource("app.properties") // 表示读取classpath的app.properties
public class AppConfig {
    @Value("${app.zone:Z}") // 表示读取key为app.zone的value，如果没有就用Z替代
    String zoneId;

    @Bean
    ZoneId createZoneId() {
        return ZoneId.of(zoneId);
    }
}
```

```
javaBean也可以实现注入属性
```

```java
@Component
public class MailService {
    @Value("#{smtpConfig.host}") // 会去smtpConfig这个JavaBean里面找
    private String smtpHost;

    @Value("#{smtpConfig.port}")
    private int smtpPort;
}

// 定义了这么个JavaBean
@Component
public class SmtpConfig {
    @Value("${smtp.host}")
    private String host;

    @Value("${smtp.port:25}")
    private int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
```



### 条件装配

```
1.设置好Profile可以决定在什么profile环境下创建Bean
    @Bean
    @Profile("") 

2.@Conditional决定是否创建这个Bean
	@Component
	@Conditional(OnSmtpEnvCondition.class)
```





## AOP

```
Aspect Oriented Programming，面向切面编程
```

```
业务流程：
	1.核心逻辑 Service
	2.切面逻辑 Aspect
	3.把Aspect以Proxy“织入”到Service
```

### 原理

```
java平台的“织入”的方式
	1.编译期，关键词aspect实现
	2.类加载器，目标类被装载到JVM的时候，用类加载器做“增强”
	3.运行期，目标对象和切面都是普通java类，JVM动态代理/第三方库“织入”
```

```
！！
Spring的AOP实现==>基于JVM的动态代理
```

### 装配AOP

```
(平时不用，只是了解思路)
```

```
相关概念：
	Aspect: 切面，横跨多个核心逻辑的功能
	Joinpoint: 连接点，应用程序流程什么地方执行插入切面
	PointCut: 切入点，连接点的集合
	Advice: 增强，特定连接点上的操作
	Introduction: 引介，为一个现有Java对象动态增加新的接口
	Weaving: 织入，切面整合到程序的执行流程中
	Interceptor: 拦截器， 实现增强的一种方式
	Target Object: 目标对象，真正执行业务的核心逻辑对象
	AOP Proxy: AOP代理对象，客户端持有的增强后的对象引用
```

```java
// 分析：
//		@Aspect注解表示这个类里面的方法需要注入到指定的位置
//		@Component 这是个Bean
@Aspect
@Component
public class LoggingAspect {
    // 在执行UserService的每个方法前执行:
    @Before("execution(public * com.itranswarp.learnjava.service.UserService.*(..))")
    public void doAccessCheck() {
        System.err.println("[Before] do access check...");
    }

    // 在执行MailService的每个方法前后执行:
    @Around("execution(public * com.itranswarp.learnjava.service.MailService.*(..))")
    public Object doLogging(ProceedingJoinPoint pjp) throws Throwable {
        System.err.println("[Around] start " + pjp.getSignature());
        Object retVal = pjp.proceed();
        System.err.println("[Around] done " + pjp.getSignature());
        return retVal;
    }
}
```

#### 容器怎么设置呢？

```java
@Configuration
@CompotentScan
@EnableAspectJAtuoProxy // 这个注解会自动查找有@Aspect的Bean
public class AppConfig {
...
}
```

#### 如何注入呢？

```java
// 编写一个子类，持有原始实例的引用
public UserServiceAopProxy extends UserService {
    private UserService target; // UserService这个实例被隐藏在里面了
    private LoggingAspect aspect;

    public UserServiceAopProxy(UserService target, LoggingAspect aspect) {
        this.target = target;
        this.aspect = aspect;
    }

    public User login(String email, String password) {
        // 先执行Aspect的代码:
        aspect.doAccessCheck();
        // 再执行UserService的逻辑:
        return target.login(email, password);
    }

    public User register(String email, String password, String name) {
        aspect.doAccessCheck();
        return target.register(email, password, name);
    }
    ...
}
```



#### AOP实际的使用

```
1.定义并执行方法，方法上通过AspectJ的注解标注什么地方调用这个方法
2.标记@Compotent和@Aspect
3.@Configuration类上标注@EnableAspectJAutoProxy
```



#### 拦截器的类型

```
@Before： 先执行拦截器代码，再执行目标代码

@After： 先目标代码。再拦截器代码

@AfterReturning： 目标代码正常返回的时候，执行拦截器代码

@AfterThrowing： 目标代码异常时，执行拦截器代码
 
@Around： 包含上面的全部
```



### 使用注解装配AOP

```
// 被装配的Bean最好知道自己被安排了
//		@Transactional： 数据库事务中调用的时候用到
```

```
1.使用注解实现AOP需要先定义注解
2.使用@Around("@annotation(name)")实现装配
```

```java
// 定义注解
@Target(METHOD)
@Retention(RUNTIME)
public @interface MetricTime {
    String value();
}

//标记注解
@Component
public class UserService {
    // 监控register()方法性能:
    @MetricTime("register")
    public User register(String email, String password, String name) {
        ...
    }
    ...
}

//实现装配
@Aspect
@Component
public class MetricAspect {
    @Around("@annotation(metricTime)")
    public Object metric(ProceedingJoinPoint joinPoint, MetricTime metricTime) throws Throwable {
        String name = metricTime.value();
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long t = System.currentTimeMillis() - start;
            // 写入日志或发送至JMX:
            System.err.println("[Metrics] " + name + ": " + t + "ms");
        }
    }
}
```



### 注意点

```
Spring通过CGLIB创建的代理类，不会初始化代理类自身继承的任何成员变量，包括final类型的成员变量！
```

```
方案：	
	1.访问被注入的Bean时，总是调用方法而非直接访问字段；
	2.编写Bean时，如果可能会被代理，就不要编写public final方法。
```



## 数据库访问

```
Spring针对JDBC做了几件事情：
	1.简化的JDBC模板，不需要手动释放资源
	2.提供统一的DAO类来实现Data Access Object模式
	3.SQLException封装成DataAccessException，是RuntimeException，可以区分SQL异常的原因
	4.可以继承Hibernate、JPA、MyBatis这些框架
```

### JDBC的使用

#### JDBC常规使用

```
JDBC的常规流程：
	1.创建全局 DataSource 实例，表示数据库连接池
	2.读写数据库的步骤：
		-从 DataSource 获取 Connection 实例
		-用 Connection 创建 PrepareStatement 实例
		-执行SQL语句，查询=>ResultSet数据集，修改=>int结果

关键在于 try...finally释放资源，涉及事务的代码需要正确提交或者回滚
```

#### Spring的简化使用

```
Spring怎么做？
	1.IoC容器去创建 DataSource 实例
	2.有JdbcTemplate，可以直接实例化
```

```java
// 核心点 AppConfig必须做的工作
// 1. @PropertySource("jdbc.properties") 读取配置文件
// 2. @Value("${jdbc.url}") 注入配置文件里的配置
// 3. 创建 DataSource 实例，顺便注入配置
// 4. 创建 JdbcTemplate 实例， 注入 DataSource

@Configuration
@ComponentScan
@PropertySource("jdbc.properties") // 第一个点
public class AppConfig {

    @Value("${jdbc.url}") // 第二个点
    String jdbcUrl;

    @Value("${jdbc.username}")
    String jdbcUsername;

    @Value("${jdbc.password}")
    String jdbcPassword;

    @Bean
    DataSource createDataSource() { // 第三个点
        HikariConfig config = new HikariConfig(); 
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);
        config.addDataSourceProperty("autoCommit", "true");
        config.addDataSourceProperty("connectionTimeout", "5");
        config.addDataSourceProperty("idleTimeout", "60");
        return new HikariDataSource(config);
    }

    // 第四个点
    @Bean
    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

//======
// 配置文件案例
# 数据库文件名为testdb:
jdbc.url=jdbc:hsqldb:file:testdb

# Hsqldb默认的用户名是sa，口令是空字符串:
jdbc.username=sa
jdbc.password=
```



#### JdbcTemplate用法

```
查询： query(), queryForObject(); 
	只要 sql,参数，RowMapper(把结果映射为JavaBean)
更新：update();
	sql,参数
其他：excute(ConnectionCallback)
	connection可以做所有JDBC操作
```



```java
T execute(ConnectionCallback<T> action);

T execute(String sql, PreparedStatementCallback<T> action);

T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper);
	sql,sql参数,RowMapper是把 ResultSet当前行映射为JavaBean输出；
	
T query(String sql, Object[] args, RowMapper<T> rowMapper);

T update();

T insert(); // 遇到自增列的时候，用 KeyHolder 获取自增值
```



```java
// T execute(ConnectionCallback<T> action) 使用案例

public User getUserById(long id) {
    // 注意传入的是ConnectionCallback:
    return jdbcTemplate.execute((Connection conn) -> {
        // 可以直接使用conn实例，不要释放它，回调结束后JdbcTemplate自动释放:
        // 在内部手动创建的PreparedStatement、ResultSet必须用try(...)释放:
        try (var ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setObject(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User( // new User object:
                            rs.getLong("id"), // id
                            rs.getString("email"), // email
                            rs.getString("password"), // password
                            rs.getString("name")); // name
                }
                throw new RuntimeException("user not found by id.");
            }
        }
    });
}

// ====================
// T execute(String sql, PreparedStatementCallback<T> action)使用案例
public User getUserByName(String name) {
    // 需要传入SQL语句，以及PreparedStatementCallback:
    return jdbcTemplate.execute("SELECT * FROM users WHERE name = ?", (PreparedStatement ps) -> {
        // PreparedStatement实例已经由JdbcTemplate创建，并在回调后自动释放:
        ps.setObject(1, name);
        try (var rs = ps.executeQuery()) {
            if (rs.next()) {
                return new User( // new User object:
                        rs.getLong("id"), // id
                        rs.getString("email"), // email
                        rs.getString("password"), // password
                        rs.getString("name")); // name
            }
            throw new RuntimeException("user not found by name.");
        }
    });
}
//==========================
//T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper)使用
public User getUserByEmail(String email) {
    // 传入SQL，参数和RowMapper实例:
    return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email = ?", new Object[] { email },
            (ResultSet rs, int rowNum) -> {
                // 将ResultSet的当前行映射为一个JavaBean:
                return new User( // new User object:
                        rs.getLong("id"), // id
                        rs.getString("email"), // email
                        rs.getString("password"), // password
                        rs.getString("name")); // name
            });
}
```



### 使用声明式事务

```
Spring有操作事务的高级接口
	PlatformTransactionManager： 事务管理器
	TransactionStatus： 事务
```

#### 常规事务操作

```java
// 开启事务，JDBC，提交事务
TransactionStatus tx = null;
try {
    // 开启事务:
    tx = txManager.getTransaction(new DefaultTransactionDefinition());
    // 相关JDBC操作:
    jdbcTemplate.update("...");
    jdbcTemplate.update("...");
    // 提交事务:
    txManager.commit(tx);
} catch (RuntimeException e) {
    // 回滚事务:
    txManager.rollback(tx);
    throw e;
}
```

#### 事务管理器意义 PlatformTransactionManager

```
JavaEE 除了 JDBC 还有分布式事务JTA(Java Transaction API)，所以提取一个抽象的事务管理器
```

```java
// 只处理JDBC事务的时候，先定义PlatformTransactionManager的Bean，是DataSourceTransactionManager类型
@Configuration
@ComponentScan
@PropertySource("jdbc.properties")
public class AppConfig {
    ...
    @Bean
    PlatformTransactionManager createTxManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
```



#### 声明式事务！

```
声明了@EnableTransactionManagement后，不必额外添加@EnableAspectJAutoProxy。
	原因：AOP代理会自动创建Bean的代理实现
```

```java
// 用 @EnableTransationManagement 注解启动
@Configuration
@ComponentScan
@EnableTransactionManagement // 启用声明式
@PropertySource("jdbc.properties")
public class AppConfig {
    ...
}

// 需要事务支持的就 @Transactional 注解
@Component
@Transational // 在class上面，所有的public方法都可以支持事务
public class UserService {
    // 此public方法自动具有事务支持:
    @Transactional
    public User register(String email, String password, String name) {
       ...
    }
}
```



#### 回滚事务

```
业务异常 最好 extends RuntimeException
```

```java
// RuntimeException / IOException 异常就回滚
@Transactional
// 也可以针对性注解
// @Transactional(rollbackFor = {RuntimeException.class, IOException.class})
public buyProducts(long productId, int num) {
    ...
    if (store < num) {
        // 库存不够，购买失败:
        throw new IllegalArgumentException("No enough products");
    }
    ...
}
```

#### 事务传播

```
没有事务的时候，创建一个事务
有事务的时候，直接加入当前的事务

传播级别：
	REQUIRED: 常用
	SUPPORTS：有事务就加入，没有就不开
	MANDATORY：
	REQUIRES_NEW: 直接开新的，旧的会挂起
	NOT_SUPPORTED:
	NEVER:
	NESTED:
```

#### Spring使用事务

```
使用 ThreadLocal 去做JDBC的事务
Connection 和 TransactionStatus 绑定到 ThreadLocal上

## 所以事务传播必须要在 一个线程内！！
```



### DAO使用

```
编写数据访问层
```

```java
// Spring有JdbcDaoSupport类
public abstract class JdbcDaoSupport extends DaoSupport {
	
    // 核心就是这个template，但是没有AutoWired注解哦！
    private JdbcTemplate jdbcTemplate;

    public final void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initTemplateConfig();
    }
	
    // 直接调用jdbcTemplate实例
    public final JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    ...
}
```

```java
// 专门写个注入JdbcTemplate的Dao
public abstract class AbstractDao extends JdbcDaoSupport {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        super.setJdbcTemplate(jdbcTemplate);
    }
}
```



### 集成Hibernate

```
ORM (Object Relational Mapping) 的框架
	目的： 数据库表记录 --> java对象
	作用： 替代JdbcTemplate
	实现步骤： 
		1.Maven内引入JDBC驱动、连接池、Hibernate
		2.AppConfig 创建DataSource、引入JDBC配置文件、启用声明式事务
		3.AppConfig 创建 LocalSessionFactoryBean
```

```java
// LocalSessionFactoryBean 会创建一个SessionFactory
// Hibernate的 Session封装了 JDBC Connection实例
// SessionFactory 封装的是 DataSource实例、持有连接池
//		每次操作数据库就创建一个新的Session
@Configuration
@ComponentScan
@EnableTransactionManagement
@PropertySource("jdbc.properties")
public class AppConfig {
    @Bean
    DataSource createDataSource() {
        ...
    }
    
    @Bean
    LocalSessionFactoryBean createSessionFactory(@Autowired DataSource dataSource) {
        var props = new Properties();
        // 自动创建表结构
        props.setProperty("hibernate.hbm2ddl.auto", "update");
        // 指定用的是什么数据库
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        // 打印SQL语句
        props.setProperty("hibernate.show_sql", "true");
        var sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        // 扫描指定的package获取所有entity class:
        sessionFactoryBean.setPackagesToScan("com.itranswarp.learnjava.entity");
        sessionFactoryBean.setHibernateProperties(props);
        return sessionFactoryBean;
    }
    
    // 创建 HibernateTemplate 和 HibernateTransactionManager
    // HibernateTemplate 方便用工具类
    // HibernateTransactionManager 声明式事务
    @Bean
    HibernateTemplate createHibernateTemplate(@Autowired SessionFactory sessionFactory) {
        return new HibernateTemplate(sessionFactory);
    }

    @Bean
    PlatformTransactionManager createTxManager(@Autowired SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }
}
```

```
常用注解：
	@Entity ： JavaBean用于映射了
	
	@Table(name="表名") ： 对应实际表名
	
	@Id ： 主键
	@GenerateValue ： 自增
	@Column(nullable= , updatable= , length= ): 列，空？，可更新？，长度
	
	@MappedSuperclass ： 标注这是用于继承的抽象实体类
	
	@Transient ： 这个属性是计算的，不是数据库读取的
	
	@PrePersist ： 
```



#### CRUD的操作

```java
// insert ==> save() 方法
public User register(String email, String password, String name) {
    // 创建一个User对象:
    User user = new User();
    // 设置好各个属性:
    user.setEmail(email);
    user.setPassword(password);
    user.setName(name);
    // 不要设置id，因为使用了自增主键
    // 保存到数据库:
    hibernateTemplate.save(user);
    // 现在已经自动获得了id:
    System.out.println(user.getId());
    return user;
}

//===========================
// delete ==> delete(),基本是删除id来实现
public boolean deleteUser(Long id) {
    User user = hibernateTemplate.get(User.class, id);
    if (user != null) {
        hibernateTemplate.delete(user);
        return true;
    }
    return false;
}

// ==============================
// update ==> update()
public void updateUser(Long id, String name) {
    User user = hibernateTemplate.load(User.class, id);
    user.setName(name);
    hibernateTemplate.update(user);
}
```

#### Example查询

```java
// 1. findByExample()
public User login(String email, String password) {
    User example = new User();
    example.setEmail(email);
    example.setPassword(password);
    // 基本类型字段都会加入到where条件里面
    List<User> list = hibernateTemplate.findByExample(example);
    return list.isEmpty() ? null : list.get(0);
}

// 2. Criteria查询
// DetachedCriteria用链式语句去添加AND条件
public User login(String email, String password) {
    DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
    criteria.add(Restrictions.eq("email", email))
            .add(Restrictions.eq("password", password));
    List<User> list = (List<User>) hibernateTemplate.findByCriteria(criteria);
    return list.isEmpty() ? null : list.get(0);
}

// 3. HQL查询
// 和SQL一个意思
List<User> list = (List<User>) hibernateTemplate.find("FROM User WHERE email=? AND password=?", email, password);
```

#### 小结

```
在Spring中集成Hibernate需要配置的Bean如下：
    DataSource；
    LocalSessionFactory；
    HibernateTransactionManager；
    HibernateTemplate（推荐）。
推荐使用Annotation配置所有的Entity Bean。
```



### JPA集成

```
Java Persistence API
	javax.persistence包
	
增删改方法：
	persist()
	remove()
	merge()
```

#### 不同接口对比

| JDBC       | Hibernate      | JPA                  |
| :--------- | :------------- | :------------------- |
| DataSource | SessionFactory | EntityManagerFactory |
| Connection | Session        | EntityManager        |



### 集成MyBatis

```
DataSource(源) --> SqlSessionFactory(ORM的源) --> DataSourceTransactionManager(声明式事务) --> Mapper(实现映射)
```

| JDBC       | Hibernate      | JPA                  | MyBatis           |
| :--------- | :------------- | :------------------- | :---------------- |
| DataSource | SessionFactory | EntityManagerFactory | SqlSessionFactory |
| Connection | Session        | EntityManager        | SqlSession        |



#### Mapper接口

```java
// 类和表之间 用Mapper这个 ！接口！ 来实现映射
public interface UserMapper {
	@Select("SELECT * FROM users WHERE id = #{id}")
	User getById(@Param("id") long id);
    
    @Insert("INSERT INTO users (email, password, name, createdAt) VALUES (#{user.email}, #{user.password}, #{user.name}, #{user.createdAt})")
	void insert(@Param("user") User user);
}
```

```java
// MapperFactoryBean 自动创建所有Mapper的实现类
//		用注解来启动
@MapperScan("com.itranswarp.learnjava.mapper")
...其他注解...
public class AppConfig {
    ...
}
```



## WEB开发

```
1.Servlet规范定义了标准组件 : Servlet, JSP, Filter, Listener
	Servlet：能处理HTTP请求并将HTTP响应返回
	JSP：一种嵌套Java代码的HTML，将被编译为Servlet
	Filter：能过滤指定的URL以实现拦截功能
	Listener：监听指定的事件，如ServletContext、HttpSession的创建和销毁

2.标准组件运行在Servlet容器中， Tomcat、 jetty、 WebLogic

MVC框架的意义：
	不需要接触Servlet API
```



### Spring MVC 的使用

#### Spring MVC的特点

```
Spring 提供一个 IoC 容器， 所有的Bean(包含Controller) 都在容器里被初始化
Servlet容器由 JavaEE服务器提供(tomcat)
```



#### Spring MVC 配置

```java
@Configuration
@ComponentScan
@EnableWebMvc // 启用Spring MVC ！！！
@EnableTransactionManagement
@PropertySource("classpath:/jdbc.properties")
public class AppConfig {
    ...
    // 为了 Spring MVC 专门创建的几个Bean
    // 1.
    @Bean
    WebMvcConfigurer createWebMvcConfigurer() { // 不是必须，可以自动处理静态文件
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/static/**").addResourceLocations("/static/");
            }
        };
    }
    
    // 2. 用什么模板引擎， 实例化什么 ViewResolver
    @Bean
    ViewResolver createViewResolver(@Autowired ServletContext servletContext) {
        PebbleEngine engine = new PebbleEngine.Builder().autoEscaping(true)
                .cacheActive(false)
                .loader(new ServletLoader(servletContext))
                .extension(new SpringExtension())
                .build();
        PebbleViewResolver viewResolver = new PebbleViewResolver();
        // 指定prefix和suffix来确定如何查找View
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix("");
        viewResolver.setPebbleEngine(engine);
        return viewResolver;
    }
}
```



#### 创建的相关问题

```
1.Spring 容器 谁来创建？
	解析：-Listener启动
		 -Servlet启动
		 -XML配置
		 -注解配置

2.什么时候创建？
	解析：xml配置初始化DispatcherServlet后，会按照AppConfig配置初始化
3.容器里的Controller是怎么通过Servlet调用的？
	解析：通过ServletContext对象？不是很确定熬
```

问题1代码

```
配置文件里初始化Spring MVC的DispatcherServlet 
--> DispatcherServlet 根据AppConfig配置创建IoC容器，初始化Bean，绑定到ServletContext
--> DisPatcherServlet 接受所有HTTP请求，按照Controller配置的路径转发到指定方法，按照返回的ModelAndView渲染界面
```

```xml
// web.xml 配置DispatcherServlet
<web-app>
    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        // 1. 初始化参数contextClass指定使用注解配置的xxx
        <init-param>
            <param-name>contextClass</param-name>
            <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
        </init-param>
        // 2. 配置文件的位置参数
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>com.itranswarp.learnjava.AppConfig</param-value>
        </init-param>
        <load-on-startup>0</load-on-startup>
    </servlet>

    // 3. Servlet映射到 /* 可以处理所有URL
    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/*</url-pattern>
    </servlet-mapping>
</web-app>
```

```java
// AppConfig 启动嵌入式Tomcat
public static void main(String[] args) throws Exception {
    Tomcat tomcat = new Tomcat();
    tomcat.setPort(Integer.getInteger("port", 8080));
    tomcat.getConnector();
    Context ctx = tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());
    WebResourceRoot resources = new StandardRoot(ctx);
    resources.addPreResources(
            new DirResourceSet(resources, "/WEB-INF/classes", new File("target/classes").getAbsolutePath(), "/"));
    ctx.setResources(resources);
    tomcat.start();
    tomcat.getServer().await();
}
```



#### Controller的编写

```java
// 1. @Controller 注解 不是@Compotent注解了

// 2. @GetMapping / @PostMapping 表示 GET / POST请求
//	  @RequestParam 表示需要接收的参数
	@PostMapping("/signin")
    public ModelAndView doSignin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session) {
        ...
    }

// 3. 返回值 包含View的路径+Map 作为Model
	return new ModelAndView("signin.html"); // 仅View，没有Model
	new ModelAndView("redirect:/signin"); // 重定向
	return "redirect:/signin";

// 4. Controller对URL可以分组，可以在Controller的class定义URL前缀
	@Controller
    @RequestMapping("/user")
    public class UserController {
        // 注意实际URL映射是/user/profile
        @GetMapping("/profile")
        public ModelAndView profile() {
            ...
        }

        // 注意实际URL映射是/user/changePassword
        @GetMapping("/changePassword")
        public ModelAndView changePassword() {
            ...
        }
    }
```



### REST 的使用

```
输入输出都是 JSON， 便于第三方调用/使用页面JS交互 
```

```java
// 在Controller中可以直接处理JSON
// @GetMapping / @PostMapping 可以指定输入输出的格式
//	consumes：接受的类型
//	produces：输出的类型
//  @ResponseBody：
// 	返回的String不需要额外处理，直接作为输出内容写入HttpServletResponse
// 	输入的JSON直接被反序列化位JavaBean
@PostMapping(value = "/rest",
             consumes = "application/json;charset=UTF-8",
             produces = "application/json;charset=UTF-8")
@ResponseBody 
public String rest(@RequestBody User user) {
    return "{\"restSupport\":true}";
}
```

#### @RestController

```java
// 可以替代@COntroller
// 定义了@RestController之后，每个方法都是一个API接口
@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    UserService userService;

    @GetMapping("/users")
    public List<User> users() {
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public User user(@PathVariable("id") long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/signin")
    public Map<String, Object> signin(@RequestBody SignInRequest signinRequest) {
        try {
            User user = userService.signin(signinRequest.email, signinRequest.password);
            return Map.of("user", user);
        } catch (Exception e) {
            return Map.of("error", "SIGNIN_FAILED", "message", e.getMessage());
        }
    }

    public static class SignInRequest {
        public String email;
        public String password;
    }
}

// JavaBean里面有些不想被JSON获取到的 比如 password，用@JsonIgnore来注解JavaBean里的get方法
// @JsonIgnore的缺点是进出全屏蔽
// @JsonProperty(access = Access.WRITE_ONLY) 可以实现只进不出，注册的时候也不会被影响到了
```



### Filter的使用

```xml
# 1.解决编码问题
#	给全局范围类HttpServletRequest 和 HttpServletResponse强制设置编码
#	或者设置一个Filter，在web.xml声明就ok
<web-app>
    <filter>
        <filter-name>encodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
        <init-param>
            <param-name>forceEncoding</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>

    <filter-mapping>
        <filter-name>encodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    ...
</web-app>
```

```xml
# 2. AuthFilter作为Bean，Servlet容器不会识别。在web.xml中声明，就不会被Spring容器实例化，就没有@AutoWired的注入
#	方案：Servlet容器实例化的Filter，间接引用Spring容器实例化的AuthFilter。
#		Spring MVC里叫做 DelegatingFilterProxy
<web-app>
    <filter>
        <filter-name>authFilter</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>authFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    ...
</web-app>

# Servlet容器从web.xml中读取配置，实例化 DelegatingFilterProxy
# Spring容器扫描@Compotent实例化AuthFilter
# DelegatingFilterProxy 会查找ServletContext上的Spring的容器
# 找到名字是authFilter的Bean，上下的filter-name最好一样，减少配置
```

![image-20220428231522462](../../../../../A_Lab/Note/MD_Pic/image-20220428231522462.png)



### Interceptor的使用

```
针对Controller的拦截
Spring容器直接管理的
```

```java
// 必须实现HandlerInterceptor接口
// 选择实现 preHandle()： Controller执行前
//		   postHanlder()： Controller正常返回后
//		   afterHandler()： 无论有无异常，结束一定执行
//================
// 拦截器要生效，要在 WebMvcConfigurer里注册所有的Interceptor
@Bean
WebMvcConfigurer createWebMvcConfigurer(@Autowired HandlerInterceptor[] interceptors) {
    return new WebMvcConfigurer() {
        public void addInterceptors(InterceptorRegistry registry) {
            for (var interceptor : interceptors) {
                registry.addInterceptor(interceptor);
            }
        }
        ...
    };
}
```

#### 异常处理

```java
// @ExceptionHandler 针对异常处理方法
// 可以传入Exception、HttpServlet、代码里的设置等
// 只针对当前Controller
@Controller
public class UserController {
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleUnknowException(Exception ex) {
        return new ModelAndView("500.html", Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
    }
    ...
}
```



### CORS的处理

```
Cross-Origin Resource Sharing，处理跨域问题
	1.@CorssOrigin：
		在@RestController里定义，
		# @CrossOrigin(origins = "http://local.liaoxuefeng.com:8080")
	
	2.CorsRegistry：
		WebMvcConfigurer 中定义CORS配置
		@Bean
        WebMvcConfigurer createWebMvcConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addCorsMappings(CorsRegistry registry) {
                    registry.addMapping("/api/**")
                    .allowedOrigins("http://local.liaoxuefeng.com:8080")
                    .allowedMethods("GET", "POST")
                    .maxAge(3600);
                    // 可以继续添加其他URL规则:
                    // registry.addMapping("/rest/v2/**")...
                }
            };
        }
	
	3.CorsFilter：
		Spring提供
```



### 国际化

```
MessageFormat + Locale实现
	1.获取Locale
		按浏览器语言顺序，
		Spring MVC有LocaleResolver从HttpServletRequest中获取Locale
				CookieLocaleResolver从HttpServletRequest中获取Locale
	2.提取资源文件
		把写死在模板中的字符串以资源文件的方式存储在外部，.properties文件
		
	3.创建MessageSource
		创建一个Spring提供的MessageSource实例，它自动读取所有的.properties文件
		
```



### 异步处理

```
在Servlet模型中，每个请求都是由某个线程处理，然后，将响应写入IO流，发送给客户端。从开始处理请求，到写入响应完成，都是在同一个线程中处理的。

实现Servlet容器的时候，只要每处理一个请求，就创建一个新线程处理它，就能保证正确实现了Servlet线程模型。
```

```
web.xml配置
Controller编写async逻辑
    法一：返回一个Callable，Spring MVC把返回的放到线程池执行
    法二：返回一个DeferredResult对象，在另一个线程里处理
```



### WebSocket使用

```
一种基于HTTP的长链接技术
```

```
1.创建一个WebSocketConfigurer实例
	实例在内部通过WebSocketHandlerRegistry注册能处理WebSocket的WebSocketHandler以及可选的WebSocket拦截器HandshakeInterceptor
```



## 第三方组件

```
可以集成JavaMail发送邮件；
	核心是定义一个JavaMailSender的Bean，然后调用其send()方法。
	
可以集成JMS消息服务；
	可以通过JMS服务器实现消息的异步处理。
	消息服务主要解决Producer和Consumer生产和处理速度不匹配的问题
可以集成Quartz实现定时任务；
可以集成Redis等服务。
```

