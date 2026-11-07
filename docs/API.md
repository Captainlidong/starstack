# 星栈始动 StarStack BootUp — API 接口文档

Base URL：`http://<host>:9090`（前后端同源部署）。
除标注「公开」外，所有接口需在请求头携带 `token: <JWT>`（登录/注册接口在 body 内）。

## 统一返回结构

```json
{ "code": "200", "msg": "成功", "data": {} }
```
分页接口 `data` 为 `PageInfo`：`{ list: [...], total: n }`。

## 错误码

| code | 含义 | | code | 含义 |
|---|---|---|---|---|
| 200 | 成功 | | 5011 | 电话格式不正确 |
| 400 | 参数异常 | | 5012 | 邮箱格式不正确 |
| 401 | Token 无效/过期 | | 5013 | 内容包含违禁词 |
| 403 | 角色权限不足 | | 5014 | 已举报过该内容 |
| 500 | 系统异常 | | 5015 | 博客不存在或未公开 |
| 4001 | 参数缺失 | | 5016 | 不能关注自己 |
| 5001 | 用户名已存在 | | 5017 | 评论内容不能为空 |
| 5002 | 用户未登录 | | 5018 | 仅支持图片与 PDF 文件 |
| 5003 | 账号或密码错误 | | 5019 | 操作过于频繁（限流） |
| 5004 | 用户不存在 | | 5008 | 密码连续错误 5 次锁定（内联） |
| 5005 | 原密码错误 | | 5006 | 活动已报名 |
| 5007 | 验证码错误或过期 | | 5009 | 密码强度不足 |
| 5010 | 账号格式不正确 | | | |

## 限流策略

`@RateLimit` 按「接口维度 + 客户端 IP」固定窗口计数，超限返回 5019：

| 接口 | 阈值 | | 接口 | 阈值 |
|---|---|---|---|---|
| POST /login | 10 次/60s | | POST /message/send | 20 次/60s |
| POST /register | 5 次/60s | | POST /blog/add | 5 次/60s |
| GET /captcha | 30 次/60s | | POST /files/upload | 10 次/60s |
| POST /comment/add | 5 次/60s | | POST /files/editor/upload | 10 次/60s |
| POST /report/add | 5 次/60s | | PUT /blog/updateReadCount | 30 次/60s |
| POST /question/add | 3 次/60s | | PUT /activity/updateReadCount | 30 次/60s |
| PUT /question/answer | 10 次/60s | | POST /likes/set | 30 次/60s |
| POST /follow/{id} | 20 次/60s | | POST /collect/set | 30 次/60s |

---

## 1. 认证（/login /register /captcha）

| 动词 | 路径 | 功能 | 参数 | 权限 |
|---|---|---|---|---|
| POST | /login | 登录（验证码+错误 5 次锁 5 分钟） | body: username/password/role/captchaKey/captchaCode | 公开 |
| POST | /register | 注册（账号 4-16 位、密码强度、验证码） | body: 同上 | 公开 |
| PUT | /updatePassword | 修改密码（改密即全端下线） | body: username/password/newPassword/role | 登录 |
| GET | /captcha | 图形验证码 `{key, image(base64)}`，2 分钟一次性 | - | 公开 |

## 2. 博客（/blog）

| 动词 | 路径 | 功能 | 参数 | 权限 |
|---|---|---|---|---|
| GET | /selectPage | 分页列表（scope=public/all/own；sort=composite/date/reads/likes） | query: Blog 字段+pageNum/pageSize | 公开 |
| GET | /selectById/{id} | 详情 | path: id | 公开 |
| GET | /selectTop | 热榜前 20（综合分+时间衰减） | - | 公开 |
| GET | /selectRecommend/{blogId} | 相关博客推荐 | path: blogId | 公开 |
| PUT | /updateReadCount/{blogId} | 阅读量+1 并记录行为 | path: blogId | 公开 |
| POST | /add | 发表（状态机：草稿直存，发布需审核） | body: Blog | 登录 |
| PUT | /update | 修改（重新提审） | body: Blog | 登录 |
| PUT | /audit/{id}/{pass} | 审核（pass=1 通过/0 驳回，可带 rejectReason） | path | ADMIN |
| PUT | /setTop/{id}/{state} | 置顶/取消 | path: id, state(0/1) | ADMIN |
| DELETE | /delete/{id}、/delete/batch | 删除（作者本人或管理员） | path/body: id | 登录 |
| GET | /selectAll /selectUserBlog /selectUserLike /selectUserCollect /selectUserComment | 管理与个人维度查询 | query+分页 | 登录 |

## 3. 分类（/category）

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| GET | /selectAll | 全部分类 | 公开 |
| POST /add、PUT /update、DELETE /delete/{id}、/delete/batch | | 增删改 | 登录 |
| GET | /selectById/{id}、/selectPage | 查询 | 登录 |

## 4. 活动（/activity /activitySign）

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| GET | /selectPage /selectAll /selectById/{id} /selectTop | 活动查询 | 公开 |
| PUT | /updateReadCount/{activityId} | 阅读量+1 | 公开 |
| POST /add、PUT /update、DELETE /delete/{id}、/delete/batch | | 活动增删改（content 富文本） | 登录 |
| POST | /activitySign/add | 报名（重复报名 5006） | 登录 |
| DELETE | /activitySign/delete/user/{activityId}/{userId} | 取消报名 | 登录 |
| GET | /activitySign/selectPage | 报名记录分页 | 登录 |
| GET | /selectUser /selectUserLike /selectUserCollect /selectUserComment | 个人维度查询 | 登录 |

## 5. 评论（/comment）

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| GET | /selectForUser | 评论树（fid+module，两级结构） | 公开 |
| GET | /selectCount | 评论数（fid+module） | 公开 |
| POST | /add | 发表（USER，敏感词+XSS 转义+限流） | 登录 |
| PUT | /update、DELETE /delete/{id}、/delete/batch、GET /selectById /selectAll /selectPage | 管理 | 登录 |

## 6. 点赞 / 收藏 / 关注

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| POST | /likes/set | 点赞切换（fid+module） | 登录 |
| POST | /collect/set | 收藏切换（fid+module） | 登录 |
| POST | /follow/{userId} | 关注（50016 不能关注自己） | 登录 |
| DELETE | /follow/{userId} | 取消关注 | 登录 |

## 7. 私信（/message，WebSocket 推送）

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| POST | /send | 发送 `{receiverId, content}`（敏感词+转义+20 次/分） | 登录 |
| GET | /conversations | 会话列表（最后一条+未读数） | 登录 |
| GET | /history/{otherUserId} | 最近 50 条记录 | 登录 |
| PUT | /read/{otherUserId} | 标记已读 | 登录 |
| GET | /unreadCount | 未读总数 | 登录 |

**WebSocket**：`ws(s)://host:9090/ws/message?token=JWT`——握手鉴权后接收 `{type:"new-message", message:{...}, senderName}` 实时推送；客户端发 `{"type":"ping"}` 收 `pong` 心跳。

## 8. 通知（/notification）

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| GET | /selectPage | 我的通知分页 | 登录 |
| GET | /unreadCount | 未读数 | 登录 |
| PUT | /read/{id} /readAll | 标记已读/全部已读 | 登录 |

类型：LIKE/COLLECT/COMMENT/FOLLOW/AUDIT/REPORT/SYSTEM/ASK/ANSWER。

## 9. 问答（/question）

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| POST | /add | 向作者提问 `{authorId, title, content}`（敏感词+3 次/分） | 登录 |
| GET | /myAsk /myReceived | 我提出/我收到的 | 登录 |
| PUT | /answer | 作者回答 `{id, answer}`（仅被提问本人） | 登录 |

## 10. 个人空间（/space，全部公开）

| 动词 | 路径 | 功能 |
|---|---|---|
| GET | /info/{userId} | 头部信息+统计（博客/粉丝/关注/获赞） |
| GET | /blogs/{userId} | 公开文章分页 |
| GET | /fans/{userId} /following/{userId} | 粉丝/关注列表分页 |
| GET | /isFollowed/{userId} | 是否已关注（游客 false） |

## 11. 举报（/report）

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| POST | /add | 举报 `{targetType, targetId, reason}`（重复 5014） | 登录 |
| GET | /selectPage | 处理队列 | ADMIN |
| PUT | /handle | 处理（RESOLVED 下架 / DISMISSED 驳回+通知） | ADMIN |

## 12. 管理端（/notice /dashboard /sensitiveWord /admin /user）

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| GET | /notice/selectAll /selectPage /selectById/{id} | 公告查询（登录弹窗数据源） | 公开 |
| POST/PUT/DELETE | /notice/add /update /delete/{id} | 公告维护（推送即触发全员弹窗） | 登录 |
| GET | /dashboard/stats | 仪表盘统计（卡片+图表+待办，30s 缓存） | ADMIN |
| GET/POST/DELETE | /sensitiveWord/selectPage /add /delete/{id} | 敏感词管理（内存缓存即时生效） | ADMIN |
| CRUD | /admin/*、/user/* | 账号管理（查询脱敏密码） | 登录 |

## 13. 文件（/files）

| 动词 | 路径 | 功能 | 权限 |
|---|---|---|---|
| POST | /upload | 上传（白名单 jpg/jpeg/png/gif/webp/bmp/pdf，返回 URL；上传即预热缩略图） | 公开+限流 |
| POST | /editor/upload | wangEditor 上传 `{errno:0, data:[{url}]}` | 公开+限流 |
| GET | /{flag} | 原图流式输出（immutable 一年缓存） | 公开 |
| GET | /thumb/{flag} | 缩略图懒生成（WebP 三级缓存） | 公开 |
| DELETE | /{flag} | 删除原图并联动清理缩略图 | 公开（风险点：无鉴权，部署建议收紧） |
