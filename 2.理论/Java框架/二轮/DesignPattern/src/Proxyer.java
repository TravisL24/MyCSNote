public class Proxyer implements Programmer{
    private Worker worker;

//    // 1. 静态代理
//    // 可以主动的去选择代理某个对象，但实际的coding方法还是这个对象去做的
//    public Proxyer(Worker worker) {
//        this.worker = worker;
//    }
//

//     // 2. 普通代理
    public Proxyer() {
        this.worker = new Worker();
    }


    public void proxyMethod() {
        System.out.println("Proxyer is doing sth");
    }

    @Override
    public void coding() {
        worker.coding();
        proxyMethod();
    }


}
