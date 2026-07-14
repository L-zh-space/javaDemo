package com.example.javademo.designpattern;

/**
 * 工厂模式：简单工厂、工厂方法、抽象工厂
 *
 * 【面试常问】
 * - 简单工厂：一个工厂类根据参数创建不同产品（违反开闭原则）
 * - 工厂方法：每个产品对应一个工厂子类（符合开闭原则，但类爆炸）
 * - 抽象工厂：创建产品族（一组相关产品）
 *
 * - Spring 中 BeanFactory 是工厂方法的体现
 *
 * 【常见坑】
 * - 简单工厂添加新产品需要修改工厂类
 * - 工厂方法导致类数量膨胀
 */
public class FactoryDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. 简单工厂 ==========");
        simpleFactory();

        System.out.println("\n========== 2. 工厂方法 ==========");
        factoryMethod();

        System.out.println("\n========== 3. 抽象工厂 ==========");
        abstractFactory();
    }

    // ==================== 产品接口 ====================
    interface Animal {
        void speak();
    }

    static class Dog implements Animal {
        @Override
        public void speak() {
            System.out.println("  旺旺！我是Dog");
        }
    }

    static class Cat implements Animal {
        @Override
        public void speak() {
            System.out.println("  喵喵！我是Cat");
        }
    }

    // ==================== 1. 简单工厂 ====================
    // 一个工厂根据参数创建不同产品 — 违反开闭原则
    static class SimpleAnimalFactory {
        public static Animal create(String type) {
            switch (type) {
                case "dog": return new Dog();
                case "cat": return new Cat();
                default: throw new IllegalArgumentException("未知类型: " + type);
            }
        }
    }

    static void simpleFactory() {
        Animal dog = SimpleAnimalFactory.create("dog");
        Animal cat = SimpleAnimalFactory.create("cat");
        dog.speak();
        cat.speak();
        System.out.println("缺点: 新增动物需要修改工厂类的switch");
    }

    // ==================== 2. 工厂方法 ====================
    // 每个产品对应一个工厂 — 符合开闭原则
    interface AnimalFactory {
        Animal create();
    }

    static class DogFactory implements AnimalFactory {
        @Override
        public Animal create() { return new Dog(); }
    }

    static class CatFactory implements AnimalFactory {
        @Override
        public Animal create() { return new Cat(); }
    }

    static void factoryMethod() {
        AnimalFactory dogFactory = new DogFactory();
        AnimalFactory catFactory = new CatFactory();
        dogFactory.create().speak();
        catFactory.create().speak();
        System.out.println("优点: 新增动物只需要添加新工厂类，不修改旧代码");
        System.out.println("缺点: 类数量翻倍");
    }

    // ==================== 3. 抽象工厂 ====================
    // 创建产品族 — 比如不同风格的UI组件
    interface Button {
        void render();
    }
    interface TextField {
        void render();
    }

    // Windows风格
    static class WindowsButton implements Button {
        @Override
        public void render() { System.out.println("  [Windows按钮]"); }
    }
    static class WindowsTextField implements TextField {
        @Override
        public void render() { System.out.println("  [Windows文本框]"); }
    }

    // Mac风格
    static class MacButton implements Button {
        @Override
        public void render() { System.out.println("  [Mac按钮-圆角]"); }
    }
    static class MacTextField implements TextField {
        @Override
        public void render() { System.out.println("  [Mac文本框-圆角]"); }
    }

    // 抽象工厂 — 创建产品族
    interface UIFactory {
        Button createButton();
        TextField createTextField();
    }

    static class WindowsFactory implements UIFactory {
        @Override
        public Button createButton() { return new WindowsButton(); }
        @Override
        public TextField createTextField() { return new WindowsTextField(); }
    }

    static class MacFactory implements UIFactory {
        @Override
        public Button createButton() { return new MacButton(); }
        @Override
        public TextField createTextField() { return new MacTextField(); }
    }

    static void abstractFactory() {
        System.out.println("Windows风格UI:");
        renderUI(new WindowsFactory());

        System.out.println("Mac风格UI:");
        renderUI(new MacFactory());

        System.out.println("抽象工厂适合: 需要创建一组相关/依赖的对象");
        System.out.println("Spring的FactoryBean类似于工厂方法模式");
    }

    static void renderUI(UIFactory factory) {
        factory.createButton().render();
        factory.createTextField().render();
    }
}
