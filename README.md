# 在线考试系统 - 后端 API

## 项目说明

基于 Spring Boot 3.5.7 + MyBatis Plus 的在线考试系统后端服务。

## 技术栈

- Spring Boot 3.5.7
- MyBatis Plus 3.5.5
- MySQL 8.0
- Lombok
- Fastjson2

## 快速开始

### 1. 导入数据库

```bash
# 使用项目根目录下的 online_exam_complete.sql
mysql -u root -p < ../../online_exam_complete.sql
```

### 2. 配置数据库

配置文件：`src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/online_exam
spring.datasource.username=root
spring.datasource.password=123456
```

### 3. 启动项目

```bash
# 方式一：使用 Maven
mvn spring-boot:run

# 方式二：使用 IDE 直接运行 OnlineexamApplication.java
```

### 4. 测试接口

访问测试接口：
```
GET http://localhost:8080/api/test/hello
```

## 已实现的接口

### 用户认证模块
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/current-user` - 获取当前用户信息

### 学生端接口
- `GET /api/student/home/stats` - 首页统计
- `GET /api/student/exams` - 考试列表
- `GET /api/student/scores` - 成绩列表

### 教师端接口
- `GET /api/teacher/home/stats` - 首页统计
- `GET /api/teacher/questions` - 题目列表（支持筛选）

### 公共接口
- `GET /api/common/classes` - 班级列表
- `GET /api/common/subjects` - 科目列表

## 测试账号

### 教师
- 用户名：`teacher01`
- 密码：`123456`
- 角色：`teacher`

### 学生
- 用户名：`student01`
- 密码：`123456`
- 角色：`student`

## 登录示例

```json
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "teacher01",
  "password": "123456",
  "role": "teacher"
}
```

响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "xxx-xxx-xxx",
    "userId": 2,
    "username": "teacher01",
    "role": "teacher"
  }
}
```

## 项目结构

```
src/main/java/com/dxd/onlineexam/
├── common/              # 通用类
│   ├── Result.java      # 统一返回结果
│   └── ResultCode.java  # 状态码枚举
├── config/              # 配置类
│   └── CorsConfig.java  # 跨域配置
├── controller/          # 控制器层
│   ├── AuthController.java
│   ├── StudentController.java
│   ├── TeacherController.java
│   └── CommonController.java
├── dto/                 # 数据传输对象
│   └── LoginRequest.java
├── entity/              # 实体类
│   ├── User.java
│   ├── Exam.java
│   ├── Question.java
│   └── ...
├── exception/           # 异常处理
│   └── GlobalExceptionHandler.java
├── mapper/              # MyBatis Mapper
│   ├── UserMapper.java
│   ├── ExamMapper.java
│   └── ...
├── service/             # 业务逻辑层
│   ├── AuthService.java
│   ├── StudentService.java
│   ├── TeacherService.java
│   └── CommonService.java
└── vo/                  # 视图对象
    ├── LoginResponse.java
    ├── ExamVO.java
    └── ...
```

## 注意事项

1. **密码存储**：当前使用明文存储，仅供实训使用。生产环境请使用加密
2. **Token管理**：当前使用简单的UUID，生产环境建议使用JWT
3. **权限控制**：当前未实现严格的权限控制，可根据需要添加拦截器
4. **数据校验**：已添加基本的参数校验，可根据需要扩展

## 开发建议

### 添加新接口的步骤：
1. 在 `entity` 中定义实体类
2. 在 `mapper` 中创建 Mapper 接口
3. 在 `service` 中实现业务逻辑
4. 在 `controller` 中暴露 API 接口
5. 测试接口功能

### 常见问题：

**Q: 启动报错找不到数据库？**
A: 检查 MySQL 是否启动，数据库 `online_exam` 是否已创建

**Q: 跨域问题？**
A: 已配置 CORS，支持 localhost:5173/5174/3000

**Q: MyBatis 日志看不到？**
A: 检查 application.properties 中的日志配置

## 联系方式

如有问题，请查看API接口文档.md或联系开发团队。

