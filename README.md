# net-game-core

## 高性能的微服务游戏框架

## 简介

~~~
* 基于SpringBoot Netty Grpc实现
* 注册中心,配置中心Redission
* 微服务之间采用grpc异步调用
* 封装了常用的工具类,全局异常处理,多数据源
* 搭建的一套分布式微服务架构,代码干净整洁,注释清晰,适合新项目开发
~~~

## 目录结构

~~~
net-game-core
├── cluster    --集群
│       └── battle-server    --战斗服
│       └── game-server      --游戏服
│       └── gate-server      --网关服
│       └── gm-server        --Gm服
│       └── login-server     --登录服
├── common     --公共
│       └── cluster          --集群模块
│       └── internal         --内部模块
│              └── base      --基础数据
│              └── cache         --缓存
│              └── db            --数据库
│              └── delay         --延迟任务
│              └── loader        --扫描
│              └── proxy         --代理
│              └── random        --随机概率
│              └── ranklist      --排行榜
│              └── script        --热更
│              └── thread        --线程池
│       └── network          --网络模块
│              └── grpc          --tcp2.0
│              └── jetty         --http
│              └── netty         --websocket
│       └── util             --工具模块
├── proto      --协议
├── pom.xml    --依赖
~~~

- 关于游戏服务架构
    - 游戏架构详情：项目采用微服务架构，包含登录服务、游戏服务、网关服、GM 服务等节点，玩家与网关通过长连接 Netty 封装长连接，游戏服务和玩家交互通过网关负载均衡和路由转发，游戏服与网关通过 GRPC 进行数据同步，GM 和游戏服务用 jetty 发 HTTP，后台用 JIT。
    - 注册中心与保活机制：注册中心用 Redis 分布式缓存存储服务器配置信息，client 从 Redis 获取服务提供者 IP 和端口号。通过心跳机制保活，网关作为客户端调游戏服时，若游戏服断连则踢掉玩家对应的 session，游戏服断连需重启。
    - 节点状态特点：游戏服节点有状态，玩家登录数据存于游戏服内存，若节点出问题，用户需等节点恢复或选择其他服，除非热更或停服解决问题。
- 游戏服务的相关技术与策略
    - 游戏服务高可用：认为游戏服务出现 bug 解 bug 即可，若一服中断不影响其他服，还可通过补偿邮件或奖励解决问题。
    - Netty 应用：客户端与网关通过 Netty 进行交互，封装消息对象，通过配置或注解将消息号路由到业务。
    - 线程模型：线程池采用 actor 模型，每个线程有自己的消息队列，实现并行处理，避免竞争阻塞。
    - 网关策略：根据网关连接数和权重计算最小连接数，为用户提供负载较小的网关，用户重连时重新获取网关 IP。
    - 鉴权逻辑：登录验证成功后在分布式 Redis 中存放 token，用户连接网关时用 token 鉴权。
- 游戏项目的技术架构与数据存储
    - 加密工具类与算法选择：未明确加密方式，可能用雪花算法，未细看 token 实现
    - Redis 的用途与缓存策略：用作分布式缓存，部分数据永久不过期，token 过期时间一般为几分钟并加随机毫秒
    - MySQL 的数据存储情况：存玩家数据，数据量不大，通过玩家 ID 存储，以 JSON 形式存字段
    - 行为记录的处理方式：部分上报日志平台，部分存 log 数据库或 ES，邮件日志存 MySQL 并压缩
