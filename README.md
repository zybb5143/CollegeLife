# CollegeLife 校园生活平台

面向校园场景的二手交易 + 社区 + AI 应用后端，Spring Boot 3 / Spring Cloud Alibaba 多模块工程，前端 Vue 3 + Element Plus。

## 技术栈

- 后端：Spring Boot 3.2.3、Spring Cloud 2023.0.3、Spring Cloud Alibaba 2023.0.1.0（Nacos 服务发现、OpenFeign、LoadBalancer）
- 持久层：MyBatis（XML Mapper）+ MySQL 8
- 认证：Spring Security + JWT（HS512，无状态），方法级 `@PreAuthorize` 区分普通用户与管理员
- 缓存/并发：Redis（登录凭证缓存、在线状态、红包金额原子扣减、Redis 分布式锁）
- 实时通信：Spring WebSocket + STOMP（私信推送）
- AI：豆包 / DeepSeek 双服务商，用于发帖内容审核与作业 AI 批改
- 前端：Vue 3 + Vite + Element Plus + axios
- 压测：自研并发压测模块，对比 MySQL 行锁与 Redis 原子操作两种抢红包实现

## 模块划分

| 模块 | 说明 | 端口 |
| --- | --- | --- |
| `shengda-app-10010` | 主应用，聚合 `shengda-common` 的全部接口 | 10010 |
| `shengda-common` | 业务代码主体：Controller / Service / Mapper / Security / WebSocket | - |
| `shengda-ai` | AI 服务商抽象层（Doubao、DeepSeek），普通 jar，不注册到 Nacos | - |
| `shengda-tie-service` | 帖子子服务，从主应用按绞杀者模式拆出，注册进 Nacos | 10020 |
| `red-packet-load-test` | 抢红包并发压测，输出吞吐与成功率报告 | 19999 |
| `vue-frontend` | Vue 3 前端 | 5173 |

## 主要功能

- 用户：注册登录、JWT 鉴权、资料修改、每日签到与连续签到
- 二手交易：发布闲置（图片上传）、列表 / 详情 / 我的闲置、下单、买卖双向订单
- 社区：发帖带图、回帖、帖子列表与详情、AI 审核（判定与学习无关的帖子转为待审核并从列表隐藏）
- 私信：WebSocket 实时收发、好友列表与关注
- 红包：发红包 / 抢红包，Redis Lua 原子扣减防超发，配套并发压测模块对比 MySQL 与 Redis 实现
- 作业：教师发布题目、学生提交、AI 自动批改并给出评分
- 网盘：文件上传下载、管理端文件管理
- 管理端：用户与内容管理、数据导出

## 微服务拆分方式

主应用不注册进 Nacos，只开启服务发现；帖子的读写通过 `@FeignClient("shengda-tie-service")` 转发到子服务，
接口路径保持不变，主应用不再持有帖子 Mapper。子服务实例可在 Nacos 控制台查看，子服务停机时主应用直接报错而不是返回空数据。

## 快速开始

1. 建库并初始化数据（含表结构与种子账号）：

   ```bash
   mysql -uroot -p < SQL/database_schema.sql
   ```

2. 启动中间件：MySQL 8、Redis（可选，未启动时相关逻辑自动降级）、Nacos standalone（帖子子服务需要）。

3. 配置以环境变量注入，不给就用文件里的本地默认值：

   | 变量 | 用途 |
   | --- | --- |
   | `MYSQL_USER` / `MYSQL_PASSWORD` | 数据库账号，示例工程用的是本机演示口令 |
   | `JWT_SECRET` | JWT 签名密钥，HS512 要求不少于 64 字节 |
   | `DEEPSEEK_API_KEY` / `DOUBAO_API_KEY` | AI 服务商密钥，为空时审核直接放行 |

4. 编译并启动：

   ```bash
   ./mvnw clean install -DskipTests
   java -jar shengda-tie-service/target/shengda-tie-service-0.0.1-SNAPSHOT.jar
   java -jar shengda-app-10010/target/shengda-app-10010-0.0.1-SNAPSHOT.jar
   cd vue-frontend && npm install && npm run dev
   ```

   前端 `/api` 代理到 `http://localhost:10010`。

种子账号：`admin / admin123`（管理员）、`test / test123`（普通用户）。密码字段为明文存储、登录时直接比对，属于演示项目遗留写法，生产应改为 BCrypt。

## 目录速览

```
shengda-common/src/main/java/com/example/demo/
  ├── controller/   REST 接口
  ├── service/      业务逻辑
  ├── mapper/       MyBatis 接口（XML 在 resources/mapper）
  ├── config/       Security、Redis、WebSocket、静态资源映射
  └── websocket/    私信实时推送
shengda-tie-service/          帖子子服务（独立端口，注册 Nacos）
shengda-ai/                   AI 服务商抽象（Doubao / DeepSeek）
SQL/database_schema.sql       建表脚本 + 种子账号
doc/                          微服务迁移实验讲义
```
