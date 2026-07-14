package com.example.javademo.basic;

/**
 * Java面向对象编程（OOP）三大特性演示
 *
 * 【面试常问】
 * - 封装：用private隐藏内部状态，通过public方法访问
 * - 继承：子类复用父类代码，Java单继承（extends）
 * - 多态：父类引用指向子类对象，同一个方法不同表现
 * - 抽象类 vs 接口：抽象类可以有构造器和普通方法，接口Java8+可以有default方法
 *
 * 【常见坑】
 * - 多态时父类引用无法调用子类特有方法
 * - 静态方法不能被重写（override），只能被隐藏（hide）
 * - 接口中的变量默认是 public static final
 */
public class OOPDemo {

    // ==================== 封装 ====================
    // private 属性 + public getter/setter = 封装
    static class Person {
        private String name;  // 外部不能直接访问
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        // 通过方法控制访问，可以加入校验逻辑
        public void setAge(int age) {
            if (age < 0 || age > 150) {
                throw new IllegalArgumentException("年龄不合法: " + age);
            }
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }

        public void sayHello() {
            System.out.println("我是 " + name + "，今年 " + age + " 岁");
        }
    }

    // ==================== 继承 ====================
    // Student 继承 Person，复用 name/age 属性和方法
    static class Student extends Person {
        private String school;

        public Student(String name, int age, String school) {
            super(name, age);  // 调用父类构造器，必须放在第一行
            this.school = school;
        }

        // 重写（Override）父类方法 — 多态的基础
        @Override
        public void sayHello() {
            System.out.println("我是学生 " + getName() + "，在 " + school + " 上学");
        }

        // 子类特有方法
        public void study() {
            System.out.println(getName() + " 正在学习...");
        }
    }

    // ==================== 抽象类 ====================
    // 抽象类不能被实例化，可以有构造器、普通方法和抽象方法
    static abstract class Animal {
        protected String name;

        public Animal(String name) {
            this.name = name;
        }

        // 普通方法 — 有实现
        public void sleep() {
            System.out.println(name + " 在睡觉");
        }

        // 抽象方法 — 没有实现，子类必须重写
        public abstract void makeSound();
    }

    static class Dog extends Animal {
        public Dog(String name) { super(name); }

        @Override
        public void makeSound() {
            System.out.println(name + "：汪汪汪！");
        }
    }

    // ==================== 接口 ====================
    // Java8+ 接口可以有 default 方法和 static 方法
    interface Flyable {
        // 接口中的变量默认 public static final
        String TYPE = "飞行生物";

        // 抽象方法
        void fly();

        // default方法 — 有默认实现，子类可选择性重写
        default void land() {
            System.out.println("降落中...");
        }
    }

    static class Bird extends Animal implements Flyable {
        public Bird(String name) { super(name); }

        @Override
        public void makeSound() {
            System.out.println(name + "：叽叽喳喳！");
        }

        @Override
        public void fly() {
            System.out.println(name + " 展开翅膀飞翔");
        }
    }

    // ==================== 演示入口 ====================
    public static void main(String[] args) {
        System.out.println("========== 1. 封装演示 ==========");
        Person p = new Person("张三", 25);
        p.sayHello();
        // p.name = "xxx";  // ❌ 编译错误，private不能直接访问

        System.out.println("\n========== 2. 继承演示 ==========");
        Student s = new Student("李四", 20, "清华大学");
        s.sayHello();   // 调用重写后的方法
        s.study();      // 调用子类特有方法

        System.out.println("\n========== 3. 多态演示 ==========");
        // 父类引用指向子类对象 — 这是多态的核心
        Person ps = new Student("王五", 22, "北京大学");
        ps.sayHello();  // 调用的是Student的sayHello（动态绑定）
        // ps.study();  // ❌ 编译错误！父类引用无法调用子类特有方法

        // 向下转型才能调用子类方法
        if (ps instanceof Student) {
            ((Student) ps).study();
        }

        System.out.println("\n========== 4. 抽象类演示 ==========");
        // Animal a = new Animal("xxx");  // ❌ 抽象类不能实例化
        Dog dog = new Dog("旺财");
        dog.makeSound();
        dog.sleep();

        System.out.println("\n========== 5. 接口演示 ==========");
        Bird bird = new Bird("小蓝");
        bird.makeSound();
        bird.fly();
        bird.land();  // 接口的default方法
    }
}
