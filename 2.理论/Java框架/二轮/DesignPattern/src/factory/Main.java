package factory;

/*
*  1. 静态工厂模式
*  2. 工厂方法模式
*  3. 抽象工厂模式
*
* */



/*
*       1. 工厂方法模式
*           -不需要负责对象创建，明确每个类的职责
*           -有新对象需求，增加一个具体的类和工厂类就行
*           -方便维护
*
*           -需要额外编码
* */
interface AnimalFactory {
    Animal createAnimal();
}

class CatFactory implements AnimalFactory {
    @Override
    // 创建猫
    public Animal createAnimal() {
        return new Cat();
    }

}

class DogFactory implements AnimalFactory {

    // 创建狗
    @Override
    public Animal createAnimal() {
        return new Dog();
    }

}

abstract class Animal {

    // 抽象方法 eat
    public abstract void eat();
}

class Cat extends Animal {
    @Override
    public void eat() {
        System.out.println("猫吃鱼");
    }
}

class Dog extends Animal {
    @Override
    public void eat() {
        System.out.println("狗吃肉");
    }
}


/*
*       2. 静态工厂模式
*           对工厂方法模式做一种削减
*           只有一个具体的工厂去创建对象
* */

class StaticAnimalFactory {
    public static Dog createDog() {
        return new Dog();
    }

    public static Cat createCat() {
        return new Cat();
    }


    // 静态的选择
    public static Animal createAnimal(String type) {
        if ("dog".equals(type)) {
            return new Dog();
        } else if ("cat".equals(type)) {
            return new Cat();
        } else {
            return null;
        }
    }
}

/*
*       3. 抽象工厂模式
*           多加一层抽象，减少工厂的数量
*
*
* */



public class Main {

    public static void main(String[] args) {

    }

}
