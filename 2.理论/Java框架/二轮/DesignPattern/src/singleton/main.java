package singleton;

/* 单例的步骤
     1. 构造函数私有化
     2. 类的内部创建实例
     3. 提供获取唯一实例的办法
 */

/*
    1. 懒汉式， 线程不安全版本
    多线程不能正常工作
*/
class Singleton1 {
    private static Singleton1 instance;
    private Singleton1() {
        System.out.println("懒汉线程不安全");
    };

    public static Singleton1 getInstance() {
        if(instance == null) {
            instance = new Singleton1();
        }
        return instance;
    }
}

/*
    2. 懒汉式， 线程安全版本
    效率低，正常情况下不需要同步
*/
class Singleton2 {
    private static Singleton2 instance;
    private Singleton2() {
        System.out.println("懒汉式，线程安全");
    };

    // 多一个synchronized
    public static synchronized Singleton2 getInstance() {
        if(instance == null) {
            instance = new Singleton2();
        }
        return instance;
    }
}

/*
    3. 饿汉式
    类装载的时候就实例化
*/
class Singleton3 {
    private static Singleton3 instance = new Singleton3();
    private Singleton3() {
        System.out.println("饿汉模式");
    }
    public static Singleton3 getInstance() {
        return instance;
    }
}
/*
    4. 静态内部类
    只有调用getInstance方法的时候才会装载对应的Holder类的时候才实例化
*/
class Singleton4 {
    private static class SingletonHolder {
        private static final Singleton4 INSTANCE = new Singleton4();
    }
    private Singleton4() {
        System.out.println("静态内部类");
    }
    public static final Singleton4 getInstance() {
        return SingletonHolder.INSTANCE;
    }
}

/*
    5. 枚举
    避免多线程同步问题，防止反序列化创建新对象
*/
enum Singleton5 {
    INSTANCE;
    public void whateverMethod() {

    }
}
/*
    6. 双重校验锁
    对象用volatile去修饰了
*/
class Singleton6 {
    private volatile static Singleton6 singleton6;
    private Singleton6() {}

    public static Singleton6 getInstance() {
        if(singleton6 == null) {
            synchronized (Singleton6.class) {
                if(singleton6 == null) {
                    singleton6 = new Singleton6();
                }
            }
        }
        return singleton6;
    }
}




public class main {

    public static void main(String[] args) {
        new Thread(() -> {

            // 创建单例对象
            Singleton4 singleton = Singleton4.getInstance();
            System.out.println(singleton.hashCode());

        }).start();
        new Thread(() -> {

            // 创建单例对象
            Singleton4 singleton = Singleton4.getInstance();
            System.out.println(singleton.hashCode());

        }).start();
    }

}
