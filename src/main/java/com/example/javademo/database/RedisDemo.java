package com.example.javademo.database;

import redis.clients.jedis.Jedis;

/**
 * Redis 基础连接与操作入门
 *
 * Redis 是一个基于内存的键值存储数据库，常用作缓存、消息队列、分布式锁等。
 *
 * 1、本机需要安装 Redis（默认端口 6379），或连接远程 Redis 服务。
 *
 * 2、使用 Jedis 客户端连接 Redis（Jedis 是 Redis 官方推荐的 Java 客户端之一）。
 *
 * 3、Redis 默认有 16 个数据库（db0 ~ db15），默认使用 db0。
 *
 * RedisDemo
 */
public class RedisDemo {

    // Redis 服务器地址
    static final String REDIS_HOST = "localhost";
    // Redis 端口
    static final int REDIS_PORT = 6379;
    // Redis 密码（默认无密码，若设置了密码则填写）
    static final String REDIS_PASSWORD = null;

    public static void main(String[] args) {
        System.out.println("========== 1. 连接 Redis ==========");
        connectRedis();
        System.out.println("\n========== 2. 基础操作 ==========");
        basicOps();
    }

    static void connectRedis() {
        // 使用 try-with-resources 自动关闭连接
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            // 如果设置了密码
            if (REDIS_PASSWORD != null) {
                jedis.auth(REDIS_PASSWORD);
            }
            // 测试连接 — PING 命令
            System.out.println("PING → " + jedis.ping());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void basicOps() {
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            System.out.println("========== 3. String 操作 ==========");
            // SET — 设置键值
            jedis.set("name", "张三");
            // GET — 获取键值
            System.out.println("GET name → " + jedis.get("name"));

            // SETEX — 设置带过期时间的键（秒）
            jedis.setex("temp_key", 10, "10秒后过期");
            System.out.println("GET temp_key → " + jedis.get("temp_key"));
            // TTL — 查看剩余过期时间
            System.out.println("TTL temp_key → " + jedis.ttl("temp_key"));

            System.out.println("========== 4. Key 操作 ==========");
            // EXISTS — 判断键是否存在
            System.out.println("EXISTS name → " + jedis.exists("name"));
            // DEL — 删除键
            jedis.del("temp_key");
            System.out.println("DEL temp_key 后 EXISTS → " + jedis.exists("temp_key"));
            // KEYS — 模糊匹配键（生产环境慎用！）
            System.out.println("KEYS n* → " + jedis.keys("n*"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
