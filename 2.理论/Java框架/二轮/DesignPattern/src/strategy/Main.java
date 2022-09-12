package strategy;

/*
*   策略模式
*       定义一组算法，将每个算法封装起来并且可以互相切换
*       有一个上下文角色---> 去和策略接口对接，整体的工作全部由上下文角色去完成就好了
* */
interface IncreaseFanStrategy {
    void action();
}

class WaterArmy implements IncreaseFanStrategy {

    @Override
    public void action() {
        System.out.println("具体实现的 WaterArmy 方法");
    }
}

class OriginalArticle implements IncreaseFanStrategy{
    @Override
    public void action() {
        System.out.println("另一种具体实现的策略方法");
    }
}

class Person {
    private IncreaseFanStrategy strategy;

    public Person(IncreaseFanStrategy strategy) {
        this.strategy = strategy;
    }

    public void exec() {
        strategy.action();
    }
}

public class Main {
    public static void main(String[] args) {
        Person person = new Person(new WaterArmy());
        person.exec();

        Person person1 = new Person(new OriginalArticle());
        person1.exec();
    }

}
