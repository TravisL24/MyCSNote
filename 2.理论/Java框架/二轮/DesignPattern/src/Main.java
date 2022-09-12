import java.lang.reflect.Proxy;
// // 代理模式的Main (Programmer, Worker, Proxyer)
//public class Main {
//    public static void main(String[] args) {
//        Worker worker = new Worker();
//
////        // 1. 静态代理的方式
////        Proxyer proxyer = new Proxyer(worker);
////        // 2. 透明代理的方式
////        Proxyer proxyer = new Proxyer();
////
////        proxyer.coding();
//
////        // 3. 动态代理
////        // 利用Proxy类的newInsatance方法去生成某个对象的代理对象
////        //  ClassLoader loader, 类装载器
////        //  @NotNull Class<?>[] interfaces, 指定被代理类的接口
////        //  @NotNull reflect.InvocationHandler h, 实现handler接口，自定义环节
////
////        Programmer dynamicProxyer =
////                (Programmer) Proxy.newProxyInstance(worker.getClass().getClassLoader(),
////                        worker.getClass().getInterfaces(),
////                (proxy, method, args1) -> {
////            // 调用了worker的方法的时候，开始做一些操作， 通过动态代理的实现方法使用invoke调用
////            if(method.getName().equals("coding")) {
////                method.invoke(worker, args1);
////                System.out.println("调用了worker的方法后，做了点反馈");
////            } else {
////                return method.invoke(worker, args1);
////            }
////            return null;
////        });
////        dynamicProxyer.coding();
//
//        // 区别：
//        // 静态代理： 自己写代理类，代理类要实现相同接口，方法多就要一一去实现，很麻烦
//        // 动态代理： JDK的API去实现，动态的在内存中构建对象，默认实现接口的全部方法
//
//    }
//}

// 包装模式， 对原有对象的一个增强
// 用组合的方式（构造函数传递）接受最简单的实现类，再去扩展对应的方法
// 优点：
//      装饰类和被装饰类是独立的，装饰模式是继承的一种替代方案；继承装饰器 就可以动态扩展想要的功能
// 缺点： 不好调试
public class Main {
    public static void main(String[] args) {
        // 最原始的实现类
        Phone phone = new MiPhone();
        // 装饰成对应的功能

        phone = new MusicPhone(phone);

        phone.call();
    }
}
