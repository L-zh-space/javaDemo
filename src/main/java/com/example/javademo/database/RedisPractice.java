package com.example.javademo.database;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Response;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.params.SetParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 手写练习 — 后端面试高频考点
 *
 * 【面试常问 — Redis 为什么快？】
 * - 基于内存操作（纯内存，无磁盘IO瓶颈）
 * - 单线程模型（避免多线程上下文切换和锁竞争，Redis 6.0+ 引入多线程IO但执行命令仍是单线程）
 * - IO多路复用（epoll，一个线程处理多个连接）
 * - 高效的数据结构（SDS、ziplist、skiplist等）
 *
 * 【面试常问 — 5种基本数据类型】
 * - String: 字符串，可以存JSON、数字等
 * - Hash: 哈希表，适合存对象
 * - List: 链表，可以用作队列/栈
 * - Set: 无序集合，去重、交并差集
 * - ZSet: 有序集合，带score排序，适合排行榜
 *
 * 【面试常问 — 缓存三大问题】
 * - 缓存穿透: 查不存在的数据 → 布隆过滤器 / 空值缓存
 * - 缓存击穿: 热点key过期 → 互斥锁 / 永不过期
 * - 缓存雪崩: 大量key同时过期 → 过期时间加随机 / 高可用集群
 *
 * 【面试常问 — 过期策略 & 淘汰策略】
 * - 惰性删除: 访问key时检查是否过期
 * - 定期删除: 每隔一段时间随机抽查一批key删除
 * - 淘汰策略: 内存满时如何淘汰（allkeys-lru、volatile-lru等）
 *
 * 【面试常问 — 持久化】
 * - RDB: 快照，某一时刻全量数据（bgsave fork子进程）
 * - AOF: 追加写命令日志，数据更安全
 * - 混合持久化（Redis 4.0+）
 *
 * 【面试常问 — 分布式锁】
 * - SETNX + 过期时间 + 唯一标识（防误删）
 * - Redisson 的 RedLock 算法
 */
public class RedisPractice {

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6383;

    // 使用连接池（生产环境推荐，避免频繁创建连接）
    private static final JedisPool pool = new JedisPool(REDIS_HOST, REDIS_PORT);

    public static void main(String[] args) {
        // System.out.println("\n========== 1. String 操作 ==========");
        // stringOps();

        // System.out.println("\n========== 2. Hash 操作 ==========");
        // hashOps();

        System.out.println("\n========== 3. List 操作 ==========");
        listOps();

        // System.out.println("\n========== 4. Set 操作 ==========");
        // setOps();

        // System.out.println("\n========== 5. ZSet 操作 ==========");
        // zsetOps();

        // System.out.println("\n========== 6. 过期策略 & TTL ==========");
        // expiryDemo();

        // System.out.println("\n========== 7. Pipeline 批量操作 ==========");
        // pipelineDemo();

        // System.out.println("\n========== 8. 事务 ==========");
        // transactionDemo();

        // System.out.println("\n========== 9. Pub/Sub 发布订阅 ==========");
        // pubSubDemo();
    }

    // ==================== 1. String 操作 ====================

    static void stringOps() {
        try (Jedis jedis = pool.getResource()) {

            // // 基础 SET/GET
            // jedis.set("name", "张三");
            // jedis.set("user:1:name", "张三");
            // System.out.println("GET → " + jedis.get("user:1:name"));

            // // SETNX — 不存在才设置（分布式锁基础）
            // long result = jedis.setnx("lock:order:1001", "thread-1");
            // System.out.println("SETNX（首次） → " + result); // 1=成功
            // result = jedis.setnx("lock:order:1001", "thread-2");
            // System.out.println("SETNX（重复） → " + result); // 0=失败

            // // SET 带参数（替代 SETEX + SETNX）
            // jedis.set("counter", "0", SetParams.setParams().nx().ex(60));
            // System.out.println("\nSET NX EX → counter=" + jedis.get("counter"));

            // // INCR / DECR — 原子增减（计数器、限流）
            // jedis.incr("counter");
            // jedis.incrBy("counter", -5);
            // System.out.println("INCR两次后 counter → " + jedis.get("counter"));

            // // MSET / MGET — 批量读写
            // jedis.mset("k1", "v1", "k2", "v2", "k3", "v3");
            // List<String> values = jedis.mget("k1", "k2", "k3");
            // System.out.println("MGET → " + values);

            // // 编码对象存JSON
            // jedis.set("user:1", "{\"id\":1,\"name\":\"张三\",\"age\":20}");
            // System.out.println("User JSON → " + jedis.get("user:1"));

            // GETSET — 返回旧值并设新值
            String old = jedis.getSet("counter", "0");
            System.out.println("GETSET 旧值 → " + old);

            // // 清理
            // jedis.del("user:1:name", "lock:order:1001", "counter", "k1", "k2", "k3", "user:1");
        }
    }

    // ==================== 2. Hash 操作 ====================

    static void hashOps() {
        try (Jedis jedis = pool.getResource()) {

            // // HSET — 设置单个字段
            // jedis.hset("user:1001", "name", "张三");
            // jedis.hset("user:1001", "age", "20");
            // jedis.hset("user:1001", "city", "北京");

            // // HMSET — 批量设置字段
            // Map<String, String> fields = new HashMap<>();
            // fields.put("email", "zhangsan@test.com");
            // fields.put("phone", "13800138000");
            // jedis.hset("user:1001", fields);

            // // HGET / HGETALL — 获取字段
            // System.out.println("HGET name → " + jedis.hget("user:1001", "name"));
            // System.out.println("HGETALL → " + jedis.hgetAll("user:1001"));

            // // HEXISTS / HDEL
            // System.out.println("HEXISTS email → " + jedis.hexists("user:1001", "email"));
            // jedis.hdel("user:1001", "phone");

            // // HINCRBY — 原子增减（计数器场景）
            // jedis.hset("article:1", "views", "0");
            // jedis.hincrBy("article:1", "views", 1);
            // System.out.println("文章浏览量 → " + jedis.hget("article:1", "views"));

            // // HKEYS / HVALS — 所有字段名/值
            // System.out.println("HKEYS → " + jedis.hkeys("user:1001"));
            // System.out.println("HVALS → " + jedis.hvals("user:1001"));

            // // 清理
            // jedis.del("user:1001", "article:1");
        }
    }

    // ==================== 3. List 操作 ====================

    static void listOps() {
        try (Jedis jedis = pool.getResource()) {

            // // LPUSH / RPUSH — 左/右插入
            // jedis.lpush("tasks", "任务A", "任务B"); // 左边插入 → [B, A]
            // jedis.rpush("tasks", "任务C", "任务D"); // 右边插入 → [B, A, C, D]

            // // LRANGE — 范围查询（0 -1 查全部）
            // System.out.println("LRANGE → " + jedis.lrange("tasks", 0, -1));

            // LINDEX — 按索引获取
            // System.out.println("LINDEX[0] → " + jedis.lindex("tasks", 0));

            // // LLEN — 长度
            // System.out.println("LLEN → " + jedis.llen("tasks"));
            
            // LPOP / RPOP — 弹出元素（可做队列/栈）
            // System.out.println("LPOP → " + jedis.lpop("tasks")); // B（最先弹出）
            // System.out.println("RPOP → " + jedis.rpop("tasks")); // D

            // // 消息队列模式：生产者-消费者
            // System.out.println("\n--- 消息队列模拟 ---");
            // jedis.del("mq:orders");
            // // 生产者
            // jedis.lpush("mq:orders", "order:1", "order:2", "order:3");
            // System.out.println("生产了3个订单");
            // // 消费者（阻塞式 — 实际生产用BRPOP）
            // while (jedis.llen("mq:orders") > 0) {
            //     System.out.println("消费 → " + jedis.brpop(1, "mq:orders"));
            // }

            // // 清理
            // jedis.del("tasks", "mq:orders");
        }
    }

    // ==================== 4. Set 操作 ====================

    static void setOps() {
        try (Jedis jedis = pool.getResource()) {

            // SADD — 添加元素（自动去重）
            jedis.sadd("tags:java", "Spring", "MyBatis", "Redis", "Spring"); // Spring重复不会加入
            jedis.sadd("tags:python", "Django", "Flask", "Redis");

            // SMEMBERS — 查看所有成员
            System.out.println("java标签 → " + jedis.smembers("tags:java"));

            // SCARD — 元素个数
            System.out.println("SCARD → " + jedis.scard("tags:java"));

            // SISMEMBER — 判断是否存在
            System.out.println("Spring 存在? → " + jedis.sismember("tags:java", "Spring"));

            // 集合运算（交、并、差）
            System.out.println("\n--- 集合运算 ---");
            System.out.println("交集 SINTER → " + jedis.sinter("tags:java", "tags:python"));
            System.out.println("并集 SUNION → " + jedis.sunion("tags:java", "tags:python"));
            System.out.println("差集 SDIFF java-python → " + jedis.sdiff("tags:java", "tags:python"));

            // 共同关注场景
            jedis.sadd("follow:user1", "userA", "userB", "userC");
            jedis.sadd("follow:user2", "userB", "userC", "userD");
            System.out.println("\n共同关注 → " + jedis.sinter("follow:user1", "follow:user2"));

            // SPOP — 随机弹出一个元素（抽奖场景）
            jedis.del("lottery:pool");
            jedis.sadd("lottery:pool", "奖品1", "奖品2", "奖品3", "参与奖");
            System.out.println("抽到 → " + jedis.spop("lottery:pool"));

            // 清理
            jedis.del("tags:java", "tags:python", "follow:user1", "follow:user2", "lottery:pool");
        }
    }

    // ==================== 5. ZSet 操作 ====================

    static void zsetOps() {
        try (Jedis jedis = pool.getResource()) {

            // ZADD — 添加元素（带score）
            jedis.zadd("leaderboard", 85, "张三");
            jedis.zadd("leaderboard", 92, "李四");
            jedis.zadd("leaderboard", 78, "王五");
            jedis.zadd("leaderboard", 88, "赵六");

            // ZRANGE — 按score正序排列
            System.out.println("排名（低→高）: " + jedis.zrange("leaderboard", 0, -1));

            // ZREVRANGE — 按score倒序排列
            System.out.println("排名（高→低）: " + jedis.zrevrange("leaderboard", 0, -1));

            // ZRANK — 获取排名（从0开始）
            System.out.println("李四排名 → " + jedis.zrank("leaderboard", "李四"));

            // ZSCORE — 获取分数
            System.out.println("张三分数 → " + jedis.zscore("leaderboard", "张三"));

            // ZINCRBY — 增减分数
            jedis.zincrby("leaderboard", 5, "王五"); // 加5分
            System.out.println("王五加5分后 → " + jedis.zscore("leaderboard", "王五"));

            // ZRANGEBYSCORE — 按分数范围查询
            System.out.println("80~90分段 → " + jedis.zrangeByScore("leaderboard", 80, 90));

            // 带分数的查询
            System.out.println("\n排行榜（带分数）:");
            List<redis.clients.jedis.resps.Tuple> tuples = jedis.zrevrangeWithScores("leaderboard", 0, -1);
            for (int i = 0; i < tuples.size(); i++) {
                redis.clients.jedis.resps.Tuple t = tuples.get(i);
                System.out.printf("  第%d名: %s (%.0f分)%n", i + 1, t.getElement(), t.getScore());
            }

            // 清理
            jedis.del("leaderboard");
        }
    }

    // ==================== 6. 过期策略 & TTL ====================

    static void expiryDemo() {
        try (Jedis jedis = pool.getResource()) {

            // 设置key并指定过期时间
            jedis.setex("code:13800138000", 60, "123456"); // 60秒过期（验证码场景）
            System.out.println("验证码 → " + jedis.get("code:13800138000"));
            System.out.println("TTL → " + jedis.ttl("code:13800138000") + "秒");

            // EXPIRE — 给已有key设过期
            jedis.set("session:abc", "user-data");
            jedis.expire("session:abc", 30);
            System.out.println("session TTL → " + jedis.ttl("session:abc"));

            // PERSIST — 移除过期时间
            jedis.persist("session:abc");
            System.out.println("PERSIST后 TTL → " + jedis.ttl("session:abc") + "（-1=永不过期）");

            // 缓存三大问题说明
            System.out.println("\n--- 缓存三大问题 ---");
            System.out.println("1. 缓存穿透: 查不存在的数据 → 布隆过滤器 / 空值缓存(短过期)");
            System.out.println("     示例: nullValueCache(jedis);");
            System.out.println("2. 缓存击穿: 热点key过期 → 互斥锁 / 逻辑过期");
            System.out.println("     示例: mutexLock(jedis, \"hot:item:1\");");
            System.out.println("3. 缓存雪崩: 大量key同时过期 → 过期时间+随机 / 高可用");

            // 缓存穿透应对：缓存空值
            nullValueCache(jedis);

            // 清理
            jedis.del("code:13800138000", "session:abc", "user:99999");
        }
    }

    /**
     * 缓存穿透应对 — 空值缓存
     * 查询不存在的key时，缓存一个空值（短过期时间），避免每次都打到DB
     */
    static void nullValueCache(Jedis jedis) {
        String cacheKey = "user:99999";
        String cacheValue = jedis.get(cacheKey);

        if (cacheValue == null) {
            // 模拟查DB也不存在
            System.out.println("  查DB: user:99999 不存在");
            // 缓存空值，过期时间设短一些（如60秒）
            jedis.setex(cacheKey, 60, "NULL");
            System.out.println("  缓存空值: " + cacheKey + " = NULL (60秒过期)");
        } else if ("NULL".equals(cacheValue)) {
            System.out.println("  命中空值缓存，直接返回不存在");
        } else {
            System.out.println("  缓存命中 → " + cacheValue);
        }
    }

    // ==================== 7. Pipeline 批量操作 ====================

    static void pipelineDemo() {
        try (Jedis jedis = pool.getResource()) {

            // 不使用 Pipeline — 每个命令一次 RTT
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                jedis.set("pipe:no:" + i, "value" + i);
                jedis.get("pipe:no:" + i);
                jedis.del("pipe:no:" + i);
            }
            long noPipeTime = System.currentTimeMillis() - start;
            System.out.println("无Pipeline 100次SET+GET+DEL → " + noPipeTime + "ms");

            // 使用 Pipeline — 批量发送，一次RTT
            start = System.currentTimeMillis();
            Pipeline pipe = jedis.pipelined();
            for (int i = 0; i < 100; i++) {
                pipe.set("pipe:yes:" + i, "value" + i);
                pipe.get("pipe:yes:" + i);
                pipe.del("pipe:yes:" + i);
            }
            pipe.sync(); // 批量执行
            long pipeTime = System.currentTimeMillis() - start;
            System.out.println("Pipeline 100次SET+GET+DEL → " + pipeTime + "ms");
            System.out.println("提速 → " + (noPipeTime / Math.max(pipeTime, 1)) + "x");

            System.out.println("\nPipeline 特点:");
            System.out.println("  1. 减少RTT（网络往返时间）");
            System.out.println("  2. 不保证原子性（不像事务）");
            System.out.println("  3. 适合批量写入/读取，不适合有依赖关系的操作");
        }
    }

    // ==================== 8. 事务 ====================

    static void transactionDemo() {
        try (Jedis jedis = pool.getResource()) {

            System.out.println("Redis 事务特点:");
            System.out.println("  1. MULTI 开启事务 → 命令入队 → EXEC 执行");
            System.out.println("  2. 不支持回滚（语法错误整体不执行，运行时错误继续执行）");
            System.out.println("  3. 配合 WATCH 实现乐观锁");

            // 基本事务
            System.out.println("\n--- 基本事务 ---");
            Transaction tx = jedis.multi();
            tx.set("tx:name", "张三");
            tx.incr("tx:counter");
            tx.get("tx:name");
            List<Object> results = tx.exec();
            System.out.println("事务结果: " + results);

            // WATCH 乐观锁 — 模拟转账
            System.out.println("\n--- WATCH 乐观锁（转账） ---");
            jedis.set("account:A", "100");
            jedis.set("account:B", "50");

            jedis.watch("account:A", "account:B");
            int amount = 30;

            // 模拟并发：另一个线程在此时修改了account:A
            // jedis.set("account:A", "200"); // 模拟并发修改

            tx = jedis.multi();
            tx.decrBy("account:A", amount);
            tx.incrBy("account:B", amount);
            results = tx.exec(); // 如果被WATCH的key被修改过，exec返回null

            if (results != null) {
                System.out.println("转账成功！A余额=" + jedis.get("account:A") + ", B余额=" + jedis.get("account:B"));
            } else {
                System.out.println("转账失败（乐观锁冲突），可重试");
                jedis.unwatch();
            }

            // 清理
            jedis.del("tx:name", "tx:counter", "account:A", "account:B");
        }
    }

    // ==================== 9. Pub/Sub 发布订阅 ====================

    static void pubSubDemo() {
        System.out.println("Pub/Sub 模式：发布者 → Channel → 订阅者");

        // 启动订阅者线程
        Thread subThread = new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        System.out.println("  [订阅者] 收到消息: channel=" + channel + ", msg=" + message);
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        System.out.println("  [订阅者] 已订阅: " + channel);
                    }
                }, "news:tech", "news:sports");
            } catch (Exception e) {
                // 订阅者在 unsubscribe 时会抛出异常
            }
        });
        subThread.setDaemon(true);
        subThread.start();

        // 等待订阅完成
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        // 发布消息
        try (Jedis jedis = pool.getResource()) {
            System.out.println("\n--- 发布消息 ---");
            jedis.publish("news:tech", "Redis 7.0 发布啦！");
            jedis.publish("news:sports", "中国队夺得冠军！");
        }

        // 等待消息消费
        try { Thread.sleep(300); } catch (InterruptedException e) {}

        System.out.println("\nPub/Sub 注意:");
        System.out.println("  1. 消息不会持久化，订阅者不在线就丢失");
        System.out.println("  2. 适合实时通知，不适合可靠消息（可靠消息用Stream或MQ）");
    }

    // ==================== 面试常见考点总结 ====================

    static void interviewSummary() {
        System.out.println("========== Redis 面试高频考点汇总 ==========\n");

        System.out.println("【数据结构应用场景】");
        System.out.println("  String: 缓存对象/计数器/分布式锁/验证码");
        System.out.println("  Hash: 用户信息/购物车/文章属性");
        System.out.println("  List: 消息队列/最新列表/时间线");
        System.out.println("  Set: 标签/共同好友/抽奖/去重");
        System.out.println("  ZSet: 排行榜/延时队列/带权重的集合");

        System.out.println("\n【缓存三大问题 + 解决方案】");
        System.out.println("  穿透: 查不存在 → 布隆过滤器 + 空值缓存");
        System.out.println("  击穿: 热点过期 → 互斥锁 + 逻辑过期");
        System.out.println("  雪崩: 大量过期 → 随机TTL + 多级缓存 + 限流");

        System.out.println("\n【数据一致性】");
        System.out.println("  先更新DB再删缓存（Cache Aside 模式）");
        System.out.println("  延迟双删: 删缓存→更新DB→延迟再删");
        System.out.println("  最终一致性 vs 强一致性");

        System.out.println("\n【内存淘汰策略（8种）】");
        System.out.println("  noeviction: 不淘汰，满了报错");
        System.out.println("  allkeys-lru: 所有key中LRU淘汰（常用）");
        System.out.println("  volatile-lru: 有过期时间的key中LRU淘汰");
        System.out.println("  allkeys-lfu: 所有key中LFU淘汰（Redis 4.0+）");
        System.out.println("  volatile-ttl: 即将过期的key优先淘汰");

        System.out.println("\n【持久化 RDB vs AOF】");
        System.out.println("  RDB: 快照、恢复快、可能丢数据");
        System.out.println("  AOF: 日志、数据安全、文件大恢复慢");
        System.out.println("  推荐: 混合持久化（Redis 4.0+）");

        System.out.println("\n【高可用架构】");
        System.out.println("  主从复制: 读写分离，数据冗余");
        System.out.println("  哨兵模式: 自动故障转移");
        System.out.println("  集群模式: 数据分片，横向扩展（16384个slot）");

        System.out.println("\n【分布式锁要点】");
        System.out.println("  1. 互斥: SET NX + 唯一标识");
        System.out.println("  2. 防死锁: 加过期时间 EX");
        System.out.println("  3. 防误删: Lua脚本判断标识再删除");
        System.out.println("  4. 可重入: Redisson的看门狗机制");
    }
}
