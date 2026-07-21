# 学习记录：@ControllerAdvice 全局异常处理器

## 学习日期
2026-07-21

## 解决的问题
项目中异常散落在各 Service 和 Controller，直接抛给前端，响应格式不统一，且可能泄露内部信息。

## 核心思路
- 用 `@RestControllerAdvice` 拦截所有 Controller 抛出的异常
- 统一包装成项目已有的 `Result<T>` 格式返回
- 分层处理：业务异常 → 参数校验 → 权限异常 → 兜底

## 处理链路

```
Controller 执行
    │
    ▼ 抛出异常
GlobalExceptionHandler 拦截
    │
    ├─ BusinessException          → Result.error(resultCode)
    ├─ MethodArgumentNotValidException → Result.error(400)
    ├─ AccessDeniedException      → Result.error(403)
    └─ Exception (兜底)            → Result.error(500)，不暴露堆栈
```

## 涉及的项目已有组件

| 组件 | 文件 | 作用 |
|------|------|------|
| `Result<T>` | [Result.java](../common/src/main/java/net/togogo/common/Result.java) | 统一响应体 `{code, message, data}` |
| `ResultCode` | [ResultCode.java](../common/src/main/java/net/togogo/common/ResultCode.java) | 业务状态码枚举 |
| `BusinessException` | [BusinessException.java](../common/src/main/java/net/togogo/common/BusinessException.java) | 携带 ResultCode 的业务异常 |


## 关键点
1. 用 `@RestControllerAdvice` 而非 `@RestController`
2. 返回 `Result<T>` 而非 `ResponseEntity`，和项目现有格式一致
3. BusinessException 直接复用 `Result.error(resultCode)`，自动映射状态码和消息
4. 兜底异常不暴露 `ex.getMessage()`，只返回固定的"系统错误"
