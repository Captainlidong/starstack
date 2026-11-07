# 贡献指南 CONTRIBUTING

感谢关注星栈始动 StarStack BootUp！欢迎通过 Issue 反馈问题、通过 PR 提交改进。

## 环境搭建

1. **要求**：JDK 8、Maven 3.6+、Node 14+（前端）、MySQL 8.0
2. **数据库**：创建 `xm-blog` 库（utf8mb4），导入根目录 `xm-blog.sql`；连接配置在 `springboot/src/main/resources/application.yml`
3. **后端**：`springboot/` 下 `mvn spring-boot:run`（或根目录 `start-backend.bat`），端口 9090
4. **前端开发模式**：`vue/` 下 `npm install && npm run serve`（8080，代理到 9090）；生产构建 `npm run build` 后把 `dist/` 复制到 `springboot/src/main/resources/static/` 重新打包
5. 默认管理员：admin / admin（首次登录请立即改密）

## 提交规范

使用 Conventional Commits（与仓库现有历史保持一致）：

```
feat: 新功能        fix: 缺陷修复      perf: 性能优化
docs: 文档         refactor: 重构     security: 安全加固
```

示例：`feat: 私信升级 WebSocket 实时推送`、`perf: 图片链路三级优化`

## 代码安全规约（必须遵守）

### SQL
- **禁止 `${}`**：Mapper XML 一律使用 `#{}` 预编译占位
- 模糊查询必须 `like concat('%', #{kw}, '%')`，禁止 `'%${kw}%'`
- 动态排序必须用 `<choose>` 白名单分支（参考 BlogMapper.xml selectAll 的 sort 处理），禁止把前端参数拼进 `order by`
- 不要在 JDBC URL 重新加上 `allowMultiQueries=true`

### XSS
- **富文本字段**（blog.content / activity.content）入库前必须过 `XssCleanService.sanitizeHtml()`（jsoup 白名单）
- **纯文本字段**（评论/私信/提问/举报/昵称/公告等）入库前必须过 `XssCleanService.escapeText()`
- 新增用户输入字段时同步在对应 Service 入口补净化调用；**密码字段永不处理**
- 新增前端渲染点：禁止对未消毒的用户内容使用 `v-html`

### 限流
- 新增写操作/公开高频接口时，在 Controller 方法上加 `@RateLimit(name = "唯一名", limit = n, window = 60)`
- 新错误码加入 `ResultCodeEnum`（下一个可用编号从 5020 起）并在 docs/API.md 错误码表登记

## 开发流程

1. Fork（或从 main 拉分支）→ 新建特性分支 `feat/xxx`
2. 开发并自测：后端 `mvn compile` 通过；核心页面手工回归（首页/详情/发布/评论/管理端）
3. 提交 PR：说明改动动机、影响面与验证方式
4. 涉及数据库变更需同步提供增量升级 SQL（参考根目录 `xm-blog-upgrade-v*.sql` 的编号惯例）并更新 `xm-blog.sql`

## 项目结构速览

详见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)（架构设计）与 [docs/API.md](docs/API.md)（接口清单）。
