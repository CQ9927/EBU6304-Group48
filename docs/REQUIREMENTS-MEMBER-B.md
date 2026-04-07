# Member B (zzzskl) 代码依赖要求文档

## 概述
本文件定义了 Member B (zzzskl, 231226772) 负责的模块所需的依赖接口和数据结构。其他成员需要实现这些依赖以使 Member B 的代码正常工作。

## Member B 负责的模块

| 模块 | Servlet | JSP | 数据文件 |
|------|---------|-----|----------|
| TA Profile | `TaProfileServlet` | `ta/profile.jsp` | `profiles.json` |
| TA CV | `TaCvServlet` | `ta/cv.jsp` | `profiles.json`, CV文件 |
| TA Jobs | `TaJobsServlet` | `ta/jobs.jsp` | `jobs.json`, `applications.json` |

## 1. 数据模型 (Model Classes) 要求

### 1.1 Profile 类 (`com.ebu6304.group48.model.Profile`)
```java
package com.ebu6304.group48.model;

import java.util.List;

public class Profile {
    private String profileId;        // 如: "P001"
    private String userId;           // 关联的user ID
    private String name;             // TA姓名
    private String email;            // 邮箱
    private String major;            // 专业
    private List<String> skills;     // 技能列表
    private List<String> availability; // 可用时间段
    private String notes;            // 备注
    private String cvFileName;       // CV文件名 (新增字段)
    private String updatedAt;        // ISO 8601格式
    private String createdAt;        // ISO 8601格式
    
    // 必须实现：无参构造器、全字段构造器、getter/setter方法
    // 必须实现：toString()方法（便于调试）
}
```

### 1.2 Job 类 (`com.ebu6304.group48.model.Job`)
```java
package com.ebu6304.group48.model;

import java.util.List;

public class Job {
    private String jobId;            // 如: "J001"
    private String title;            // 岗位标题
    private String type;             // "MODULE" 或 "INVIGILATION"
    private String semester;         // 如: "2026_SPRING"
    private String schedule;         // 时间安排
    private Integer capacity;        // 容量
    private List<String> requiredSkills; // 所需技能
    private String postedByUserId;   // 发布者ID
    private String status;           // "OPEN" 或 "CLOSED"
    private String createdAt;        // ISO 8601格式
    
    // 必须实现：无参构造器、全字段构造器、getter/setter方法
}
```

### 1.3 Application 类 (`com.ebu6304.group48.model.Application`)
```java
package com.ebu6304.group48.model;

import java.util.List;

public class Application {
    private String applicationId;    // 如: "A001"
    private String jobId;            // 关联的job ID
    private String applicantUserId;  // 申请人ID
    private Integer matchScore;      // 匹配分数 (0-100)
    private List<String> missingSkills; // 缺失技能列表
    private String status;           // "SUBMITTED", "UNDER_REVIEW", "SELECTED", "REJECTED"
    private String note;             // 备注
    private String createdAt;        // ISO 8601格式
    private String updatedAt;        // ISO 8601格式
    
    // 必须实现：无参构造器、全字段构造器、getter/setter方法
}
```

### 1.4 User 类 (`com.ebu6304.group48.model.User`)
```java
package com.ebu6304.group48.model;

public class User {
    private String userId;           // 如: "U001"
    private String username;         // 用户名
    private String passwordHash;     // 密码哈希
    private String role;             // "TA", "MO", "ADMIN"
    private String createdAt;        // ISO 8601格式
    
    // 必须实现：无参构造器、全字段构造器、getter/setter方法
}
```

## 2. 数据仓库 (Repository Classes) 要求

### 2.1 ProfileRepository (`com.ebu6304.group48.repository.ProfileRepository`)
```java
package com.ebu6304.group48.repository;

import com.ebu6304.group48.model.Profile;
import javax.servlet.ServletContext;
import java.util.List;

public class ProfileRepository {
    
    public ProfileRepository(ServletContext context) {
        // 构造函数，接收ServletContext用于获取数据目录
    }
    
    /**
     * 通过userId查找profile
     * @param userId 用户ID
     * @return 对应的Profile对象，未找到时返回null
     */
    public Profile findByUserId(String userId) {
        // 实现要求：从profiles.json读取数据，查找匹配的profile
        // 返回第一个匹配的profile或null
    }
    
    /**
     * 保存profile（创建或更新）
     * @param profile 要保存的profile对象
     * @return 保存成功返回true，失败返回false
     */
    public boolean save(Profile profile) {
        // 实现要求：
        // 1. 如果profileId已存在：更新现有profile
        // 2. 如果profileId不存在：创建新profile
        // 3. 将更新后的列表写入profiles.json文件
        // 4. 处理文件读写异常
    }
    
    /**
     * 获取所有profiles（可选，用于调试）
     * @return 所有profile的列表
     */
    public List<Profile> findAll() {
        // 从profiles.json读取所有profiles
    }
}
```

### 2.2 JobRepository (`com.ebu6304.group48.repository.JobRepository`)
```java
package com.ebu6304.group48.repository;

import com.ebu6304.group48.model.Job;
import javax.servlet.ServletContext;
import java.util.List;

public class JobRepository {
    
    public JobRepository(ServletContext context) {
        // 构造函数，接收ServletContext
    }
    
    /**
     * 获取所有OPEN状态的岗位
     * @return OPEN状态的Job列表
     */
    public List<Job> findAllOpenJobs() {
        // 实现要求：从jobs.json读取，过滤status为"OPEN"的岗位
    }
    
    /**
     * 通过jobId查找岗位
     * @param jobId 岗位ID
     * @return 对应的Job对象，未找到时返回null
     */
    public Job findById(String jobId) {
        // 实现要求：从jobs.json查找匹配的job
    }
    
    /**
     * 获取所有岗位（包括OPEN和CLOSED）
     * @return 所有Job的列表
     */
    public List<Job> findAll() {
        // 从jobs.json读取所有jobs
    }
}
```

### 2.3 ApplicationRepository (`com.ebu6304.group48.repository.ApplicationRepository`)
```java
package com.ebu6304.group48.repository;

import com.ebu6304.group48.model.Application;
import javax.servlet.ServletContext;
import java.util.List;

public class ApplicationRepository {
    
    public ApplicationRepository(ServletContext context) {
        // 构造函数，接收ServletContext
    }
    
    /**
     * 通过申请人userId查找所有申请
     * @param userId 申请人ID
     * @return 该用户的所有申请列表
     */
    public List<Application> findByApplicantUserId(String userId) {
        // 实现要求：从applications.json过滤applicantUserId匹配的记录
    }
    
    /**
     * 通过jobId查找所有申请
     * @param jobId 岗位ID
     * @return 该岗位的所有申请列表
     */
    public List<Application> findByJobId(String jobId) {
        // 实现要求：从applications.json过滤jobId匹配的记录
    }
    
    /**
     * 保存申请
     * @param application 要保存的申请对象
     * @return 保存成功返回true，失败返回false
     */
    public boolean save(Application application) {
        // 实现要求：类似ProfileRepository的save方法
    }
}
```

## 3. Maven 依赖要求

### 必须添加到 pom.xml：
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.15.2</version>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
    <version>2.15.2</version>
</dependency>
```

**或者使用 Gson（二选一）：**
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

## 4. 会话 (Session) 要求

登录成功后必须在Session中设置以下属性：

```java
HttpSession session = request.getSession();
session.setAttribute("userId", "U001");      // 用户ID，必须
session.setAttribute("username", "zzzskl");  // 用户名，建议
session.setAttribute("role", "TA");         // 用户角色，必须
```

## 5. 测试数据要求

### 5.1 用户数据 (`data/users.json`)
```json
[
  {
    "userId": "U001",
    "username": "zzzskl",
    "passwordHash": "sha256:samplehash123",
    "role": "TA",
    "createdAt": "2026-03-27T10:00:00Z"
  },
  {
    "userId": "U010",
    "username": "mo_user",
    "passwordHash": "sha256:samplehash456",
    "role": "MO",
    "createdAt": "2026-03-27T10:05:00Z"
  }
]
```

### 5.2 岗位数据 (`data/jobs.json`)
```json
[
  {
    "jobId": "J001",
    "title": "CS101 Tutorial Assistant",
    "type": "MODULE",
    "semester": "2026_SPRING",
    "schedule": "WED_18_20",
    "capacity": 2,
    "requiredSkills": ["Java", "Teaching", "Algorithms"],
    "postedByUserId": "U010",
    "status": "OPEN",
    "createdAt": "2026-03-27T11:00:00Z"
  },
  {
    "jobId": "J002",
    "title": "Final Exam Invigilator",
    "type": "INVIGILATION",
    "semester": "2026_SPRING",
    "schedule": "FRI_09_12",
    "capacity": 5,
    "requiredSkills": ["Proctoring", "Attention to detail"],
    "postedByUserId": "U010",
    "status": "OPEN",
    "createdAt": "2026-03-27T11:30:00Z"
  }
]
```

### 5.3 Profile 数据 (`data/profiles.json`)
```json
[
  {
    "profileId": "P001",
    "userId": "U001",
    "name": "Zhang San",
    "email": "ta48@school.edu",
    "major": "Software Engineering",
    "skills": ["Java", "Python", "Web Development"],
    "availability": ["MON_14_16", "WED_18_20", "FRI_10_12"],
    "notes": "Experienced TA for programming courses",
    "cvFileName": null,
    "updatedAt": "2026-03-27T10:30:00Z",
    "createdAt": "2026-03-27T10:30:00Z"
  }
]
```

## 6. 文件目录结构要求

```
data/
├── users.json           # 用户数据（必须包含至少一个TA用户）
├── profiles.json        # Profile数据（可以为空数组）
├── jobs.json           # 岗位数据（必须包含至少一个OPEN岗位）
├── applications.json    # 申请数据（可以为空数组）
└── selection.json      # 选择数据（可以为空数组）

src/main/java/com/ebu6304/group48/
├── model/              # 上述4个Model类
│   ├── Profile.java
│   ├── Job.java
│   ├── Application.java
│   └── User.java
└── repository/         # 上述3个Repository类
    ├── ProfileRepository.java
    ├── JobRepository.java
    └── ApplicationRepository.java
```

## 7. 接口调用示例

### 7.1 ProfileRepository 调用示例
```java
// 在Servlet中初始化
ProfileRepository repo = new ProfileRepository(getServletContext());

// 查询当前用户的profile
String userId = (String) session.getAttribute("userId");
Profile profile = repo.findByUserId(userId);

// 保存profile
boolean success = repo.save(profile);
```

### 7.2 JobRepository 调用示例
```java
JobRepository jobRepo = new JobRepository(getServletContext());
List<Job> openJobs = jobRepo.findAllOpenJobs();
```

### 7.3 ApplicationRepository 调用示例
```java
ApplicationRepository appRepo = new ApplicationRepository(getServletContext());
List<Application> myApplications = appRepo.findByApplicantUserId(userId);
```

## 8. 错误处理要求

所有Repository方法必须：
1. 处理文件不存在的情况（返回空列表或null）
2. 处理JSON解析错误（返回空列表或null）
3. 处理IO异常（返回false或空列表）
4. 不能抛出未检查异常到Servlet层

## 9. 验收标准

Member B 的代码可以正常工作当以下条件满足：

1. ✅ Model 类存在且字段与文档一致
2. ✅ Repository 类存在且方法签名与文档一致  
3. ✅ pom.xml 包含JSON处理依赖
4. ✅ data/ 目录包含测试数据文件
5. ✅ 登录后Session包含userId和role属性
6. ✅ 编译无错误：`mvn clean compile`
7. ✅ 可以部署到Tomcat并访问 `/ta/profile`, `/ta/cv`, `/ta/jobs`

## 10. 联系人

- **Member B (zzzskl)**: 231226772
- **职责**: TA Profile, CV Management, Job Browsing
- **文件位置**: `src/main/java/com/ebu6304/group48/servlet/Ta*.java`
- **JSP位置**: `src/main/webapp/WEB-INF/jsp/ta/*.jsp`

---
*最后更新: 2026-04-07*
*版本: 1.0*