package com.example.javademo.designpattern;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 代理模式：静态代理、JDK动态代理、CGLIB动态代理
 *
 * 【面试常问】
 * - 代理模式 vs 装饰器模式：代理是控制访问，装饰器是增强功能
 * - JDK动态代理：基于接口，Proxy.newProxyInstance()，要求目标类实现接口
 * - CGLIB动态代理：基于继承，生成目标类的子类，不能代理final类/方法
 * - Spring AOP 默认用JDK动态代理（有接口时），无接口时用CGLIB
 *
 * 【常见坑】
 * - JDK动态代理返回的对象只能用接口类型接收
 * - CGLIB不能代理final方法
 * - 代理内部方法调用不会触发代理（this.xxx 不是代理对象调用）
 */
public class ProxyDemo {

    // ==================== 目标接口和实现 ====================
    interface UserService {
        void addUser(String name);
        void deleteUser(String name);
    }

    // 目标对象（被代理的对象）
    static class UserServiceImpl implements UserService {
        @Override
        public void addUser(String name) {
            System.out.println("    执行 addUser: " + name);
        }

        @Override
        public void deleteUser(String name) {
            System.out.println("    执行 deleteUser: " + name);
        }

        // 这个方法不通过代理调用时不会打印日志
        public void selfCall() {
            System.out.println("    selfCall内部调用addUser:");
            this.addUser("内部调用");  // 注意：这里的this是原始对象，不会触发代理
        }
    }

    public static void main(String[] args) {
        System.out.println("========== 1. 静态代理 ==========");
        staticProxy();

        System.out.println("\n========== 2. JDK动态代理 ==========");
        jdkDynamicProxy();

        System.out.println("\n========== 3. 代理内部调用陷阱 ==========");
        proxyTrap();

        System.out.println("\n========== 4. JDK动态代理 vs CGLIB ==========");
        proxyComparison();
    }

    // ==================== 1. 静态代理 ====================
    // 手动编写代理类，实现相同接口，持有目标对象
    static class UserServiceStaticProxy implements UserService {
        private final UserService target;

        public UserServiceStaticProxy(UserService target) {
            this.target = target;
        }

        @Override
        public void addUser(String name) {
            System.out.println("  [静态代理] before addUser");
            target.addUser(name);
            System.out.println("  [静态代理] after addUser");
        }

        @Override
        public void deleteUser(String name) {
            System.out.println("  [静态代理] before deleteUser");
            target.deleteUser(name);
            System.out.println("  [静态代理] after deleteUser");
        }
    }

    static void staticProxy() {
        UserService target = new UserServiceImpl();
        UserService proxy = new UserServiceStaticProxy(target);
        proxy.addUser("张三");
        proxy.deleteUser("张三");
        System.out.println("缺点: 每个目标类都要写一个代理类");
    }

    // ==================== 2. JDK动态代理 ====================
    static class LogInvocationHandler implements InvocationHandler {
        private final Object target;

        public LogInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("  [JDK动态代理] before " + method.getName());
            // 反射调用目标方法
            Object result = method.invoke(target, args);
            System.out.println("  [JDK动态代理] after " + method.getName());
            return result;
        }
    }

    static void jdkDynamicProxy() {
        UserService target = new UserServiceImpl();

        // 创建动态代理 — 运行时生成代理类
        UserService proxy = (UserService) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),  // 类加载器
                target.getClass().getInterfaces(),   // 目标实现的接口
                new LogInvocationHandler(target)     // InvocationHandler
        );

        proxy.addUser("李四");
        proxy.deleteUser("李四");

        System.out.println("\n代理类名: " + proxy.getClass().getName());
        System.out.println("代理类是运行时动态生成的字节码类");
    }

    static void proxyTrap() {
        UserService target = new UserServiceImpl();
        UserService proxy = (UserService) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new LogInvocationHandler(target)
        );

        System.out.println("通过代理调用 addUser：");
        proxy.addUser("王五");  // ✅ 代理生效

        System.out.println("\n通过代理调用 selfCall：");
        // selfCall内部用this.addUser()，this是原始对象，不走代理
        if (target instanceof UserServiceImpl impl) {
            impl.selfCall();  // ❌ 代理不生效
        }
        System.out.println("结论: 方法内部调用不会触发代理（this是原始对象而非代理对象）");
    }

    static void proxyComparison() {
        System.out.println("JDK动态代理 vs CGLIB动态代理：");
        System.out.println("┌──────────┬─────────────────┬─────────────────┐");
        System.out.println("│          │ JDK动态代理      │ CGLIB           │");
        System.out.println("├──────────┼─────────────────┼─────────────────┤");
        System.out.println("│ 原理     │ 反射，基于接口    │ ASM字节码，继承  │");
        System.out.println("│ 限制     │ 目标必须实现接口  │ 不能代理final类  │");
        System.out.println("│ 性能     │ 早期JDK慢，现在优  │ 稍快（直接调用）│");
        System.out.println("│ 使用场景 │ Spring AOP默认    │ 无接口时使用    │");
        System.out.println("└──────────┴─────────────────┴─────────────────┘");
        System.out.println("\nSpring AOP 代理选择逻辑：");
        System.out.println("  有接口 → JDK动态代理（默认）");
        System.out.println("  无接口 → CGLIB");
        System.out.println("  @EnableAspectJAutoProxy(proxyTargetClass=true) → 强制CGLIB");
    }
}
