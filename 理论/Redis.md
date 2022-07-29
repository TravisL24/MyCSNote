# 数据结构

![](https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/redis%E5%91%BD%E4%BB%A4%E6%8F%90%E7%BA%B2.png)

## 1. String

```
k-v基本结构，value最多容纳数据长度是 512M
```

### 内部实现

```
    底层数据类型： int + SDS(简单动态字符串)

    SDS： 有一个 len 属性的值去记录长度
        1.不仅可以保存文本数据，还可以保存二进制数据。
        2.获取字符串长度的时间复杂度是 O(1)
        3.Redis 的 SDS API 是安全的，拼接字符串不会造成缓冲区溢出
```

<img title="" src="https://raw.githubusercontent.com/TravisL24/pic-repo/main/picGo/2022/07/28/20220728155514.webp" alt="string结构.webp" data-align="center" width="616">

```
String对象内部编码3种：

    int ：整数，long类型保存，保存在ptr
    embstr：字符串 <= 32字节    
    raw ：字符串 > 32字节
    
embstr 和 raw编码都用 SDS 来保存值
    embstr ==> 一次内存分配函数，分配一块连续的内存空间保存redisObject + SDS
    raw    ==> 两次内存分配函数分配两个空间保存redisObject + SDS

embstr的优势：
    1.内存分配减少为1次
    2.释放字符串对象也只要1次
    3.数据在连续内存可以更好的利用缓存
缺点:
    字符串长度增加需要重新分配空间，所以embstr 只读！
```

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/int.png" alt="" data-align="center" width="335">

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/embstr.png" alt="" data-align="center" width="659">

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/raw.png" alt="" data-align="center" width="659">

### 应用场景

```
1.缓存对象
    1.1 直接缓存整个对象的 JSON 
        SET user:1 '{"name":"xiaolin", "age":18}'
    1.2 采用将 key 进行分离为 user:ID:属性，采用 MSET 存储，用 MGET 获取各属性值
        MSET user:1:name xiaolin user:1:age 18 user:2:name xiaomei user:2:age 20

2.常规计数
    Redis 处理命令是单线程，所以执行命令的过程是原子的。
    如计算访问次数、点赞、转发、库存数量等等

3.分布式锁
    3.1 SET 命令有个 NX 参数可以实现「key不存在才插入」，可以用它来实现分布式锁：
        key 不存在 ==> 插入成功，加锁成功；
        key 存在   ==> 插入失败，加锁失败。
    还会对分布式锁加上过期时间
        SET lock_key unique_value NX PX 10000
          lock_key 就是 key 键；
          unique_value == 客户端生成的唯一的标识；
          PX 10000 == 设置 lock_key 的过期时间为 10s，这是为了避免客户端发生异常而无法释放锁。

    3.2 解锁 ==> 删除lock_key，unique_value也需要匹配才行
        （Lua脚本保证解锁的原子性）
        // 释放锁时，先比较 unique_value 是否相等，避免锁的误释放
        if redis.call("get",KEYS[1]) == ARGV[1] then
            return redis.call("del",KEYS[1])
        else
            return 0
        end

4. 共享session信息
    服务器都去同一个 Redis 获取相关的 Session 信息，解决了分布式系统下 Session 存储的问题。
```

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/Session2.png" alt="" data-align="center" width="298">

## 2. List

```
简单的字符串列表，按照插入顺序排序，可以从头部或尾部向 List 列表添加元素。

列表的最大长度为 2^32 - 1，也即每个列表支持超过 40 亿个元素。
```

### 内部实现

```
双向链表/压缩列表
    压缩链表：元素 < 512, 元素值 < 64字节
    双向链表：反之。
Redis3.2+，quicklist 取代双向链表/压缩列表
```

<img src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/list.png" title="" alt="" data-align="center">

### 应用场景

```
消息队列
    3个需求：
        1.消息保序
            -list本身就是先进先出的顺序，lpush添加消息，rpop获取消息
                缺点：rpop需要轮询访问list，有性能损失
                    ==> 用brpop（阻塞式读取）替代rpop，没东西就自己阻塞
        2.处理重复的消息
            要求：
                -每个消息都有一个全局的 ID
                -消费者要记录已经处理过的消息的 ID
            实际操作：
                自行为每个消息生成一个全局唯一ID
        3.保证消息的可靠性
            要求：没处理好的消息，如何重新找回
            实操：brpoplpush命令，
                让消费者程序从一个 List 中读取消息，
                同时，Redis 会把这个消息再插入到另一个 List（可以叫作备份 List）留存

总结：
    消息保序：使用 LPUSH + RPOP；
    阻塞读取：使用 BRPOP；
    重复消息处理：生产者自行实现全局唯一 ID；
    消息的可靠性：使用 BRPOPLPUSH

    缺陷：
        List 不支持多个消费者消费同一条消息，读取后直接删除了
```

## 3.Hash

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/hash.png" alt="" data-align="center" width="413">

```
Hash 是一个键值对（key - value）集合，
其中 value 的形式如： value=[{field1，value1}，...{fieldN，valueN}]。
Hash 特别适合用于存储对象
```

### 内部实现

```
压缩列表/hash表
    压缩列表：hash类型元素 < 512 && 值 < 64字节
    hash表： 反之。
Redis 7+，交给listpack数据结构来做了
```

### 应用场景

```
缓存对象
    一般对象用 String + Json 存储，对象中某些频繁变化的属性可以考虑抽出来用 Hash 类型存储。
```

## 4.Set

```
一个无序并唯一的键值集合，它的存储顺序不会按照插入的先后顺序进行存储。
一个集合最多可以存储 2^32-1 个元素。

Set 和 List的区别；
    List ： 元素可重复，先后顺序存储
    Set ：  元素不可重，无序方式存储
```

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/set.png" alt="" data-align="center" width="425">

### 内部实现

```
hash表/整数集合
    整数集合：元素都是整数 && 元素个数 < 512
    hash表： 反之
```

### 应用场景

```
1. 点赞
    Set 类型可以保证一个用户只能点一个赞
2. 共同关注
    交集运算，
    Set 的差集、并集和交集的计算复杂度较高，在数据量较大的情况下，如果直接执行这些计算，会导致 Redis 实例阻塞
        在主从集群中，为了避免主库因为 Set 做聚合计算（交集、差集、并集）时导致主库被阻塞，
        我们可以选择一个从库完成聚合统计，或者把数据返回给客户端，由客户端来完成聚合统计
3. 抽奖活动
    存储某活动中中奖的用户名 ，Set 类型因为有去重功能，可以保证同一个用户不会中奖两次。
```

## 5.Zset

```
有序集合类型，比 Set 类型多了一个排序属性 score（分值）
每个存储元素相当于有两个值组成的，一个是有序结合的元素值，一个是排序值。
```

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/zset.png" alt="" data-align="center" width="504">

### 内部实现

```
压缩列表/跳表
    压缩列表：元素个数 < 64 && 元素值 < 64
    跳表 ：反之
Redis 7，listpack替换压缩列表
```

### 应用场景

```
需要展示最新列表、排行榜等场景时，如果数据更新频繁或者需要分页显示，可以优先考虑使用ZSet

1.排行榜
2.电话、姓名排序(ZRANGEBYLEX / ZREVRANGEBYLEX)
```

## BitMap

```
位图，一串连续的二进制数组（0和1），可以通过偏移量（offset）定位元素。
BitMap通过最小的单位bit来进行0|1的设置，表示某个元素的值或者状态，时间复杂度为O(1)。
特别适合一些数据量大且使用二值统计的场景。
```

### 内部实现

```
用 String 类型作为底层数据结构实现的一种统计二值状态的数据类型
```

### 应用场景

```
1.签到打卡
    每一天只要一位就可以表示
2.判断用户登录状态
3.连续签到用户总数
    连续天数的bitmap做与运算
```

## 6.HyperLogLog

```
一种用于「统计基数」的数据集合类型
    基数统计就是指统计一个集合中不重复的元素个数
HyperLogLog 是统计规则是基于概率完成的，不是非常准确，标准误算率是 0.81%。

HyperLogLog 提供不精确的去重计数。

优点：
    -计算基数需要的内存空间是固定的，而且很小
    每个 HyperLogLog 键只需要花费 12 KB 内存，就可以计算接近 2^64 个不同元素的基数
```

### 应用场景

```
百万级网页UV计数    
```

## 7. GEO

```
主要用于存储地理位置信息，并对存储的信息进行操作。
```

### 内部实现

```
直接使用了 Sorted Set 集合类型。
使用 GeoHash 编码方法实现了经纬度到 Sorted Set 中元素权重分数的转换
```

## 8.Stream

```
专门为消息队列设计的数据类型。
支持消息的持久化、支持自动生成全局唯一 ID、支持 ack 确认消息的模式、支持消费组模式
```

### 常见命令

```
-XADD ： 插入消息，有序，自动生成全局唯一ID
-XLEN ： 查询消息长度
-XREAD ： 读取消息，可以按ID读取
-XDEL ： 根据ID删除消息
-DEL ：删除整个Stream
-XRANGE ： 读取区间消息
-XREADGROUP ： 按消费组形式读取消息
-XPENDING ： 查询每个消费组内所有消费者[已读取，未确认的消息]
-XACK ： 向消息队列确认消息处理完成
```

###应用场景

```
消息队列
    1. XADD -- XREAD (BLOCK 实现阻塞读)

    2. XGROUP创建消费组，XREADGROUP消费组内的消费者读取消息
        同一个消费组里的消费者不能消费同一条消息。
        不同消费组的消费者可以消费同一条消息

    3. 基于 Stream 实现的消息队列，如何保证消费者在发生故障或宕机再次重启后，仍然可以读取未处理完的消息？
        XPENDING -- XACK
        Streams 会自动使用内部队列（也称为 PENDING List）留存消费组里每个消费者读取的消息，
        直到消费者使用 XACK 命令通知 Streams“消息已经处理完成”
```

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/%E6%B6%88%E6%81%AF%E7%A1%AE%E8%AE%A4.png" alt="" width="474" data-align="center">

### 与专业消息队列的差距

```
专业的需求：
    -消息不丢
    -消息可堆积

1.Redis Stream消息会丢吗？
    一个消息队列 = 生产者 + 队列中间件 + 消费者
    生产者：异常处理合理，恰当重发消息就不会丢失
    消费者：不会丢失
    中间件：会丢失，
            -AOF持久化是异步操作，宕机的时候存在数据丢失可能
            -主从复制也是异步的，主从切换可能会丢
2. Redis Stream 消息可堆积吗？
    Stream有最大长度限制，超过限制删除就消息
```

![](https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97%E4%B8%89%E4%B8%AA%E9%98%B6%E6%AE%B5.png)

## 总结

```
常用基本类型：
    String（字符串），Hash（哈希），List（列表），Set（集合）及 Zset(sorted set：有序集合)
    底层有所改变
应用场景：
    String：缓存对象、常规计数、分布式锁、共享session信息
    List ： 消息队列(1.自己实现全局唯一ID 2.没有消费组)
    Hash ： 缓存对象，购物车
    Set ： 聚合计算，点赞，共同关注，抽奖
    Zset ： 排序场景；排行榜，电话姓名排序
    BitMap ： 二值状态统计；签到，判断用户登录状态，连续签到用户总数
    HyperLogLog ： 海量数据基数统计；百万级网页UV计数
    GEO ： 存储地理位置信息
    Stream ： 消息队列
```

![](https://img-blog.csdnimg.cn/img_convert/9fa26a74965efbf0f56b707a03bb9b7f.png)
