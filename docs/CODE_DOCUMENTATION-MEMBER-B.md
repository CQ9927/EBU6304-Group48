# TA Recruitment System - Member B (zzzskl) 代码说明文档

## 概述
本文档详细说明Member B (zzzskl, 231226772)负责开发的所有代码文件，包括核心业务逻辑和测试辅助代码。

## 代码分类

### 🎯 核心业务代码 (Production Code)
这些是实现实际业务功能的代码，包含完整的业务逻辑：

1. **Servlet层** - HTTP请求处理
   - `TaProfileServlet.java` - TA个人资料管理
   - `TaCvServlet.java` - CV文件上传管理
   - `TaJobsServlet.java` - 岗位查看与筛选

2. **视图层** - 用户界面
   - `profile.jsp` - 个人资料表单页面
   - `cv.jsp` - CV管理页面
   - `jobs.jsp` - 岗位列表页面

3. **数据模型层** - 业务实体定义
   - `Profile.java` - 个人资料实体
   - `Job.java` - 岗位实体
   - `Application.java` - 申请实体
   - `User.java` - 用户实体

### 🔧 测试/占位符代码 (Test/Placeholder Code)
这些是为了使系统能够编译和运行而创建的占位符实现，需要其他成员完成：

1. **数据访问层** - JSON持久化占位符
   - `ProfileRepository.java` - 需要实现真实的JSON读写
   - `JobRepository.java` - 需要实现真实的JSON读写
   - `ApplicationRepository.java` - 需要实现真实的JSON读写

### 📦 配置与依赖
- `pom.xml` - 项目配置，JSON 使用 Gson（与 `UserRepository` 一致）

## 核心文件详细说明

### 1. TaProfileServlet.java
**文件路径**: `src/main/java/com/ebu6304/group48/servlet/TaProfileServlet.java`
**类型**: 🎯 核心业务代码

#### 📋 功能描述
- TA个人资料的创建和更新
- 提供RESTful接口：GET(显示表单)、POST(保存数据)
- 用户认证检查
- 数据验证和错误处理

#### 🔄 输入/输出
**输入 (HTTP Request)**:
- `GET /ta/profile` - 显示个人资料表单
- `POST /ta/profile` - 保存个人资料
  - 参数: `name`, `email`, `major`, `skills[]`, `availability[]`, `notes`
  - Session要求: `userId` (用户ID)

**输出 (HTTP Response)**:
- 成功: 重定向到 `/ta/profile` 并显示成功消息
- 失败: 重定向到 `/ta/profile` 并显示错误消息
- 数据流: 通过 `ProfileRepository` 读写 `profiles.json`

#### ⚠️ 约束条件
1. **用户认证**: 必须先登录并设置Session `userId`
2. **数据验证**: 必填字段检查在客户端和服务器端
3. **文件依赖**: 需要 `ProfileRepository` 的真实实现
4. **数据格式**: 遵循 `DATA_SCHEMA.md` 规范

#### 🔧 未来修改建议
1. **增强验证**: 添加邮箱格式验证、技能数量限制
2. **批量操作**: 支持导入/导出个人资料
3. **审计日志**: 记录个人资料修改历史
4. **API扩展**: 添加JSON API接口供前端应用调用

---

### 2. TaCvServlet.java
**文件路径**: `src/main/java/com/ebu6304/group48/servlet/TaCvServlet.java`
**类型**: 🎯 核心业务代码

#### 📋 功能描述
- CV文件上传、查看、删除
- 文件类型和大小验证
- Active CV标记管理
- 多文件版本控制

#### 🔄 输入/输出
**输入**:
- `GET /ta/cv` - 显示CV管理页面
- `POST /ta/cv?action=upload` - 上传CV文件
  - 参数: `cvFile` (multipart/form-data)
  - 支持格式: PDF, DOC, DOCX
  - 大小限制: 5MB
- `POST /ta/cv?action=delete` - 删除CV文件
  - 参数: `filename` (要删除的文件名)

**输出**:
- 文件保存到: `{dataDir}/cvs/CV_{userId}_{timestamp}.{ext}`
- 数据库更新: `profiles.json` 中的 `cvFileName` 字段

#### ⚠️ 约束条件
1. **文件格式**: 仅支持PDF和Word文档
2. **文件大小**: 最大5MB
3. **存储位置**: CV文件保存在服务器本地目录
4. **依赖关系**: 需要先完成个人资料 (`ProfileRepository`)

#### 🔧 未来修改建议
1. **文件预览**: 添加PDF预览功能
2. **云存储**: 集成云存储服务 (AWS S3, Azure Blob)
3. **版本控制**: 完整的CV版本历史管理
4. **OCR支持**: 自动提取CV文本内容进行分析

---

### 3. TaJobsServlet.java
**文件路径**: `src/main/java/com/ebu6304/group48/servlet/TaJobsServlet.java`
**类型**: 🎯 核心业务代码

#### 📋 功能描述
- 显示所有OPEN状态的岗位
- 支持多维度筛选 (类型、学期、技能)
- 计算技能匹配度
- 显示申请状态

#### 🔄 输入/输出
**输入**:
- `GET /ta/jobs` - 显示所有岗位
- 筛选参数: `type`, `semester`, `skill`

**输出**:
- 岗位列表 (通过 `JobRepository` 获取)
- 申请状态 (通过 `ApplicationRepository` 获取)
- 用户资料 (通过 `ProfileRepository` 获取)
- 匹配度计算: (用户技能 ∩ 岗位需求技能) / 岗位需求技能 × 100%

#### ⚠️ 约束条件
1. **数据依赖**: 需要三个Repository都实现
2. **性能考虑**: 大量岗位时可能需要分页
3. **匹配算法**: 当前使用简单技能交集算法

#### 🔧 未来修改建议
1. **高级匹配**: 加权技能匹配算法
2. **分页支持**: 大数据量分页显示
3. **排序功能**: 按匹配度、创建时间等排序
4. **推荐系统**: 基于用户历史申请的智能推荐

---

### 4. JSP 页面文件

#### 4.1 profile.jsp
**路径**: `src/main/webapp/WEB-INF/jsp/ta/profile.jsp`
**功能**: 个人资料编辑表单
**特性**:
- 响应式表单布局
- 技能多选框组
- 时间段选择器
- 客户端验证
- 实时保存状态显示

#### 4.2 cv.jsp  
**路径**: `src/main/webapp/WEB-INF/jsp/ta/cv.jsp`
**功能**: CV文件管理界面
**特性**:
- 文件上传组件
- 文件类型验证 (JavaScript)
- Active CV标记
- 文件列表显示
- 操作确认对话框

#### 4.3 jobs.jsp
**路径**: `src/main/webapp/WEB-INF/jsp/ta/jobs.jsp`
**功能**: 岗位浏览和筛选界面
**特性**:
- 多条件筛选表单
- 匹配度可视化显示
- 申请状态标签
- 技能匹配高亮
- 自动刷新机制

---

### 5. 数据模型文件

#### 5.1 Profile.java
**类型**: 🎯 核心业务代码
**功能**: 个人资料数据模型
**字段说明**:
- `profileId`: 唯一标识符 (格式: P001)
- `userId`: 关联用户ID
- `name`, `email`, `major`: 基本信息
- `skills`, `availability`: 列表字段
- `cvFileName`: CV文件引用
- `createdAt`, `updatedAt`: 时间戳

#### 5.2 Job.java
**类型**: 🎯 核心业务代码  
**功能**: 岗位数据模型
**字段说明**:
- `jobId`: 岗位ID (格式: J001)
- `title`, `type`, `semester`: 岗位信息
- `schedule`, `capacity`: 时间和容量
- `requiredSkills`: 需求技能列表
- `status`: 状态 (OPEN/CLOSED)

#### 5.3 Application.java
**类型**: 🎯 核心业务代码
**功能**: 申请记录数据模型
**字段说明**:
- `applicationId`: 申请ID (格式: A001)
- `jobId`, `applicantUserId`: 关联ID
- `matchScore`: 匹配分数 (0-100)
- `missingSkills`: 缺失技能列表
- `status`: 申请状态

#### 5.4 User.java
**类型**: 🎯 核心业务代码
**功能**: 用户账户数据模型
**字段说明**:
- `userId`: 用户ID (格式: U001)
- `username`: 登录用户名
- `passwordHash`: 密码哈希
- `role`: 用户角色 (TA/MO/ADMIN)

---

### 6. 占位符Repository文件

#### 6.1 ProfileRepository.java
**类型**: 🔧 测试/占位符代码
**状态**: ⚠️ 需要实现
**当前实现**: 返回空值/默认值
**需要实现的方法**:
- `findByUserId()`: 从 `profiles.json` 读取数据
- `save()`: 写入 `profiles.json`
- `findAll()`: 读取所有profiles

#### 6.2 JobRepository.java
**类型**: 🔧 测试/占位符代码
**状态**: ⚠️ 需要实现
**需要实现的方法**:
- `findAllOpenJobs()`: 筛选status="OPEN"的岗位
- `findById()`: 按ID查找岗位
- `findAll()`: 读取所有岗位

#### 6.3 ApplicationRepository.java
**类型**: 🔧 测试/占位符代码
**状态**: ⚠️ 需要实现
**需要实现的方法**:
- `findByApplicantUserId()`: 按申请人查找申请
- `findByJobId()`: 按岗位查找申请
- `save()`: 保存申请记录

## 实现注意事项

### 编译与运行
1. **编译命令**: `mvn clean compile`
2. **打包命令**: `mvn package`
3. **运行要求**: Tomcat 9.x, JDK 17+

### 数据文件位置
1. **运行时数据目录**: `{user.home}/ebu6304-group48-data/`
2. **CV存储目录**: `{dataDir}/cvs/`
3. **JSON文件**: `users.json`, `profiles.json`, `jobs.json`, `applications.json`

### 会话管理要求
以下Session属性必须在登录后设置:
```java
session.setAttribute("userId", "U001");
session.setAttribute("username", "username");
session.setAttribute("role", "TA");
```

## 测试指南

### 单元测试 (建议)
```java
// ProfileServlet 测试示例
@Test
public void testProfileServlet_GetWithoutSession_RedirectsToLogin() {
    // 测试未登录时重定向到登录页
}

@Test  
public void testProfileServlet_PostValidData_SavesProfile() {
    // 测试有效数据保存
}
```

### 集成测试步骤
1. 部署WAR到Tomcat
2. 访问 `http://localhost:8080/ta-recruitment/ta/profile`
3. 完成个人资料表单
4. 上传CV文件
5. 浏览和筛选岗位

## 故障排除

### 常见问题
1. **编译错误**: 检查 Gson 依赖是否存在于 pom.xml
2. **404错误**: 检查Tomcat部署路径
3. **空指针异常**: 检查Session属性是否设置
4. **文件上传失败**: 检查目录权限和磁盘空间

### 日志记录
建议添加日志记录到关键方法:
```java
import java.util.logging.Logger;

private static final Logger LOG = Logger.getLogger(TaProfileServlet.class.getName());

LOG.info("Profile saved for user: " + userId);
```

## 版本历史

### v1.0 (2026-04-07) - 初始版本
- 完成所有核心Servlet和JSP
- 创建数据模型和占位符Repository
- 添加详细文档说明
- 确保项目编译通过

## 联系人
- **开发者**: zzzskl (231226772)
- **负责模块**: TA个人资料、CV管理、岗位浏览
- **文档版本**: 1.0
- **最后更新**: 2026-04-07