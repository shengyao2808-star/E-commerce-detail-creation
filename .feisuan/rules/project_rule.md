
# 开发规范指南

为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、基础项目信息

- **操作系统**：Windows 11
- **工作区路径**：`C:\Users\Administrator\IdeaProjects\E-commerce detail creation`
- **项目作者**：Administrator
- **当前时间**：2026-05-24 03:00:34

## 二、技术栈与构建工具

- **语言版本**：JDK 21.0.11
- **构建工具**：Maven
- **主框架**：Spring Boot 3.x (基于 JDK 21 推荐版本)
- **核心依赖**：
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `lombok`

## 三、目录结构规范

项目遵循标准的 Maven 目录结构，根目录为 `src`。

```text
E-commerce detail creation
└── src
    ├── main
    │   ├── java
    │   │   └── com.example.ecommerce (示例包名，需根据实际包结构调整)
    │   │       ├── controller
    │   │       ├── service
    │   │       │   └── impl
    │   │       ├── repository
    │   │       ├── entity
    │   │       ├── dto
    │   │       ├── config
    │   │       └── EcommerceApplication.java
    │   └── resources
    │       ├── application.yml
    │       └── static
    └── test
        └── java
```

> **注意**：请确保所有 Java 源文件位于 `src/main/java` 下，资源文件位于 `src/main/resources` 下。

## 四、分层架构规范

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|----------------------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 不得直接访问数据库，必须通过 Service 层调用                  |
| **Service**    | 实现业务逻辑、事务管理与数据校验   | 必须通过 Repository 层访问数据库；返回 DTO 而非 Entity（除非必要） |
| **Repository** | 数据库访问与持久化操作             | 继承 `JpaRepository`；使用 `@EntityGraph` 避免 N+1 查询问题     |
| **Entity**     | 映射数据库表结构                   | 不得直接返回给前端（需转换为 DTO）；包名统一为 `entity`         |

### 接口与实现分离

- 所有接口实现类需放在接口所在包下的 `impl` 子包中。

## 五、安全与性能规范

### 输入校验

- 使用 `@Valid` 与 JSR-303 校验注解（如 `@NotBlank`, `@Size` 等）
  - 注意：Spring Boot 3.x 中校验注解位于 `jakarta.validation.constraints.*`

- 禁止手动拼接 SQL 字符串，防止 SQL 注入攻击。

### 事务管理

- `@Transactional` 注解仅用于 **Service 层**方法。
- 避免在循环中频繁提交事务，影响性能。

## 六、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `UserServiceImpl`     |
| 方法/变量  | lowerCamelCase       | `saveUser()`          |
| 常量       | UPPER_SNAKE_CASE     | `MAX_LOGIN_ATTEMPTS`  |

### 注释规范

- **所有类、方法、字段需添加注释，注释语言必须使用用户的第一语言（中文）**。
- 使用 Javadoc 格式进行详细注释。

### 类型命名规范（阿里巴巴风格）

| 后缀 | 用途说明                     | 示例         |
|------|------------------------------|--------------|
| DTO  | 数据传输对象                 | `UserDTO`    |
| DO   | 数据库实体对象               | `UserDO`     |
| BO   | 业务逻辑封装对象             | `UserBO`     |
| VO   | 视图展示对象                 | `UserVO`     |
| Query| 查询参数封装对象             | `UserQuery`  |

### 实体类简化工具

- 使用 Lombok 注解替代手动编写 getter/setter/构造方法：
  - `@Data`
  - `@NoArgsConstructor`
  - `@AllArgsConstructor`

## 七、扩展性与日志规范

### 接口优先原则

- 所有业务逻辑通过接口定义（如 `UserService`），具体实现放在 `impl` 包中（如 `UserServiceImpl`）。

### 日志记录

- 使用 `@Slf4j` 注解代替 `System.out.println`

## 八、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |
