```
标准化的Java项目管理和构建工具
```

```ascii
a-maven-project
├── pom.xml  // 项目描述文件，定义项目内容
├── src 
│   ├── main
│   │   ├── java  // 源码
│   │   └── resources // 资源文件
│   └── test
│       ├── java
│       └── resources // 测试资源
└── target // 编译和打包的文件
```

## 依赖管理

```
Maven 会根据当前申明需要的包，自动导入其他前置的包
```

### 依赖关系

| scope    | 说明                       | 示例              |
|:-------- |:------------------------ |:--------------- |
| compile  | 编译时需要用到该jar包（默认）         | commons-logging |
| test     | 编译Test时需要用到该jar包         | junit           |
| runtime  | 编译时不需要，但运行时需要用到          | mysql           |
| provided | 编译时需要用到，但运行时由JDK或某个服务器提供 | servlet-api     |

```
compile类型的依赖会被直接放入classpath
```

## 构建流程

### Lifecycle和Phase

```
生命周期(Lifecycle) 是一系列阶段(phase)组成的。
mvn + phase maven会根据生命周期运行到指定的phase
也可以多个 phase配合使用
常见指令：
    mvn clean: 清理所有生成的class和jar
    mvn clean compile: 先清理后执行到compile
    mvn clean test:  先清理后执行到test
    mvn clean package: 先清理再执行到package
```

### Goal

| 执行的Phase | 对应执行的Goal                          |
|:-------- |:---------------------------------- |
| compile  | compiler:compile                   |
| test     | compiler:testCompile surefire:test |

```
lifecycle ==> java的package，包含一个或者多个phase
phase ==> java的class，包含一个或者多个goal
goal ==> java的method，真正做事的
```

## 使用插件 (plugin)

```
goal是最小的任务执行单元
执行phase，都是通过某个插件(plugin)执行的，插件回去执行对应的goal
```

## 模块管理

```
Maven可以管理多个模块，每一个模块可以当作一个独立的Maven项目，都有独自的pom.xml

pom.xml可以提取出共同的部分作为parent，
他的<packaging>pom</packaging>和常规的是不一样的，可以减少每个模块中重复的配重

multiple-project
├── pom.xml // 用于统一编译的，多一个<modules></modules>模块
├── parent
│   └── pom.xml
├── module-a
│   ├── pom.xml
│   └── src
├── module-b
│   ├── pom.xml
│   └── src
└── module-c
    ├── pom.xml
    └── src
// 每个模块都可以从parent直接继承
```

## 使用mvnw

```
mvnw是Maven Wrapper缩写，可以执行特定的maven版本，不和其他的冲突

my-project
├── .mvn
│   └── wrapper
│       ├── MavenWrapperDownloader.java
│       ├── maven-wrapper.jar
│       └── maven-wrapper.properties
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   └── resources
    └── test
        ├── java
        └── resources

// mvnw、mvnw.cmd、.mvn提交到库之中可以统一版本
```
