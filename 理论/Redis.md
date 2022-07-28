# 数据结构

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
    
```

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/int.png" alt="" data-align="center" width="335">

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/embstr.png" alt="" data-align="center" width="659">

<img title="" src="https://cdn.xiaolincoding.com/gh/xiaolincoder/redis/%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/raw.png" alt="" data-align="center" width="659">
