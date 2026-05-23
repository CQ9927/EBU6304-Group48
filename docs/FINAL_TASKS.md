# 最终验收任务清单 (Final Assessment Task List)

> 最终评估日期：**2026-05-24** | 版本：v1.0 | 分支策略：每个任务一个 feature branch → PR → main

---

## 人员分配

| GitHub | 任务数 | 任务编号 |
|--------|--------|----------|
| **CQ9927** | 4 | M1, M2, M3, M4 |
| **SpPt2FeMa** | 1 | S1 |
| **zzzskl** | 1 | Z1 |
| **yunmengdd** | 0 | 不分配 |
| **BUCOD** | 0 | 不分配 |

---

## 依赖关系

```
M1 (MatchingService 多维算法)
├── M2 (MO审核页升级) ── 依赖 M1
├── S1 (TA端接入)     ── 依赖 M1
└── Z1 (CV/Profile修复) ── 独立，但需了解 M1 的 MatchResult 结构

M3 (MO发岗页修复)     ── 独立
M4 (错误页面)         ── 独立

全部完成后 → 集成测试冒烟
```

---

---

## M1 — MatchingService 多维匹配算法

**负责人：** CQ9927
**依赖：** 无
**文件：** `src/main/java/com/ebu6304/group48/service/MatchingService.java`

### 现状

当前 MatchingService 只有一个 `buildConflictHints()` 方法（给 admin 仪表盘做规则检测）。真正的匹配评分逻辑离散在两处：
- `TaApplyServlet.buildApplication()`（第89-101行）—— 简单技能字符串精确匹配
- `ta/jobs.jsp`（第126-138行）—— JSTL 重复实现同样逻辑

两者都是：`matchScore = matchingCount * 100 / requiredSkills.size()`，仅考虑技能精确匹配。

### 改动内容

#### 1. 新增公开方法

```java
public MatchResult computeMatch(Job job, Profile profile)
```

#### 2. 新增内部类 MatchResult

```java
public static class MatchResult {
    private final int totalScore;        // 0-100 总分
    private final int skillScore;        // 技能维度得分 (满分50)
    private final int scheduleScore;     // 时间兼容维度得分 (满分25)
    private final int majorScore;        // 专业相关维度得分 (满分15)
    private final int completenessScore; // 资料完整度得分 (满分10)

    private final List<String> matchedSkills;   // 匹配的技能
    private final List<String> missingSkills;   // 缺失的技能
    private final boolean scheduleMatch;        // 时间是否匹配
    private final String detail;                // 文字说明，如 "技能高度匹配，时间兼容"

    // 全参构造器 + 全部 getter
}
```

#### 3. 四维评分算法详细设计

##### 维度一：技能匹配（满分 50 分）

```
输入：job.getRequiredSkills()（可能为空列表）, profile.getSkills()

Step 1 - 精确匹配（40 分）:
  requiredSkills 为空 → 直接得 40 分
  否则：
    遍历每个 requiredSkill，检查 profile.getSkills() 是否 contains
    matchedCount = 匹配上的技能数
    score1 = matchedCount * 40 / requiredSkills.size()  （整数除法）

Step 2 - 额外技能加分（10 分）:
  extraSkills = profile 中有但 job 不需要的技能数
  score2 = min(extraSkills * 2, 10)  // 每个额外技能加2分，最多10分

skillScore = score1 + score2  （范围 0-50）
```

##### 维度二：时间兼容性（满分 25 分）

```
输入：job.getSchedule()（单个字符串如 "WED_18_20"），profile.getAvailability()（列表如 ["MON_14_16", "WED_18_20"]）

job 无 schedule（null/blank）→ 直接得 25 分（无约束）

Step 1 - 精确匹配：
  profile.getAvailability() 包含 job.getSchedule() → 25 分

Step 2 - 同天相邻时段匹配：
  解析 schedule 和 availability 中的 DAY 部分（前3字符，如 "WED"）
  同天 → 15 分

Step 3 - 不匹配：
  0 分

scheduleScore = 0/15/25
scheduleMatch = (scheduleScore > 0)
```

##### 维度三：专业相关性（满分 15 分）

```
输入：job.getTitle()，profile.getMajor()

关键词库（与岗位标题匹配）：
  "Software" → ["Software Engineering", "Computer Science", "Information Technology"]
  "Database" → ["Computer Science", "Software Engineering", "Data Science", "Information Technology"]
  "Algorithm" → ["Computer Science", "Software Engineering", "Mathematics"]
  "Web"      → ["Computer Science", "Software Engineering", "Web Development", "Information Technology"]
  "Network"  → ["Computer Science", "Network Engineering", "Information Technology"]
  "AI" / "Machine" / "Artificial" → ["Computer Science", "Artificial Intelligence", "Data Science"]
  "Exam" / "Invigil" → 所有专业都给 15 分（监考无专业门槛）

匹配逻辑：
  将 job title 和 profile major 都转为小写
  遍历关键词库，如果 title 包含某个关键词，检查 major 是否在对应专业列表中
  匹配 → 15 分
  部分匹配（major 包含关键词但不在明确列表中）→ 10 分
  无 major → 5 分
  其他 → 5 分（给基础分）
```

##### 维度四：资料完整度（满分 10 分）

```
cvScore:
  profile.getCvFileName() 非 null 且非 blank → 5 分
  否则 → 0 分

profileScore:
  profile.getName() 非 blank
  && profile.getEmail() 非 blank
  && profile.getSkills() 非 null 且非空
  && profile.getAvailability() 非 null 且非空
  → 5 分
  以上部分满足 → 3 分
  以上几乎全空 → 0 分

completenessScore = cvScore + profileScore
```

##### 总分汇总

```java
totalScore = skillScore + scheduleScore + majorScore + completenessScore;
// 范围：0-100，不需要额外 clamp
```

#### 4. 保留原有方法

```java
public List<String> buildConflictHints(List<Job> jobs, List<Application> applications)
```

保持不变，admin 仪表盘仍然使用。

#### 5. 生成 detail 文字

```java
// 示例逻辑
if (totalScore >= 80) detail = "Excellent match: skills, schedule, and major all well-aligned";
else if (totalScore >= 60) detail = "Good match with some gaps in skills or schedule";
else if (totalScore >= 40) detail = "Partial match: consider strengthening relevant skills";
else detail = "Low match: significant gaps in required qualifications";
```

### 验收标准

- [ ] `computeMatch(job, profile)` 返回正确的四维分数
- [ ] job 无 requiredSkills 时，技能维度得 50 分（40+10 bonus）
- [ ] profile 为 null 时，返回 totalScore=0 的 MatchResult
- [ ] `buildConflictHints()` 行为不变
- [ ] 编译通过，无编译警告

---

---

## M2 — MO 审核页全面升级

**负责人：** CQ9927
**依赖：** M1（MatchingService 完成）
**文件：** `MoSelectServlet.java`、`mo/select.jsp`

### 现状问题

1. **显示 userId 而非姓名** — select.jsp `${app.applicantUserId}` 显示 "U-DEMO-TA"
2. **无申请人资料/CV 查看** — MO 完全看不到申请者的资质
3. **无容量检查** — 可以无限 SELECT 超过 capacity
4. **不排序** — 申请列表无排序
5. **无状态流转校验** — SELECTED 可以翻回 REJECTED
6. **匹配分只显示数字** — 无分项、无颜色

### 改动内容

#### A. MoSelectServlet.java 改动

##### doGet() 方法增强：

```java
// 1. 加载 ProfileRepository（需新增字段）
private ProfileRepository profileRepository;

// 2. 构建 applicantProfileMap
Map<String, Profile> applicantProfileMap = new HashMap<>();
for (Application app : filteredApplications) {
    Profile p = profileRepository.findByUserId(app.getApplicantUserId());
    if (p != null) applicantProfileMap.put(app.getApplicantUserId(), p);
}
req.setAttribute("applicantProfileMap", applicantProfileMap);

// 3. 用 MatchingService 计算匹配分（用于排序，不修改 Application 对象本身）
MatchingService matchingService = new MatchingService();
Map<String, MatchingService.MatchResult> matchResultMap = new HashMap<>();
for (Application app : filteredApplications) {
    Profile p = applicantProfileMap.get(app.getApplicantUserId());
    Job job = jobMap.get(app.getJobId());
    if (p != null && job != null) {
        matchResultMap.put(app.getApplicationId(), matchingService.computeMatch(job, p));
    }
}
req.setAttribute("matchResultMap", matchResultMap);

// 4. 按 totalScore 降序排列
List<Application> sortedApplications = new ArrayList<>(filteredApplications);
sortedApplications.sort((a, b) -> {
    int scoreA = matchResultMap.containsKey(a.getApplicationId())
        ? matchResultMap.get(a.getApplicationId()).getTotalScore() : 0;
    int scoreB = matchResultMap.containsKey(b.getApplicationId())
        ? matchResultMap.get(b.getApplicationId()).getTotalScore() : 0;
    return Integer.compare(scoreB, scoreA);
});
req.setAttribute("applications", sortedApplications);

// 5. 统计每个 job 已 SELECTED 的人数，传给 JSP
Map<String, Integer> selectedCountByJob = new HashMap<>();
for (Application app : applicationRepository.findAll()) {
    if ("SELECTED".equalsIgnoreCase(app.getStatus())) {
        selectedCountByJob.merge(app.getJobId(), 1, Integer::sum);
    }
}
req.setAttribute("selectedCountByJob", selectedCountByJob);
```

##### doPost() 方法增强 — 容量检查：

```java
// 在 SELECTED 决策前检查容量
if ("SELECTED".equals(decision)) {
    Application app = applicationRepository.findById(applicationId);
    Job job = jobRepository.findById(app.getJobId());
    int capacity = job.getCapacity() != null ? job.getCapacity() : 0;
    long alreadySelected = applicationRepository.findByJobId(app.getJobId()).stream()
        .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
        .count();
    if (alreadySelected >= capacity) {
        resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId="
            + selectedJobId + "&error=capacity");
        return;
    }
}
```

##### doPost() 方法增强 — 状态流转校验：

```java
// 终态不可变
Application existing = applicationRepository.findById(applicationId);
String currentStatus = existing.getStatus();
if ("SELECTED".equalsIgnoreCase(currentStatus) || "REJECTED".equalsIgnoreCase(currentStatus)) {
    // 终态不可再变更
    resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId="
        + selectedJobId + "&error=final");
    return;
}
// SUBMITTED → UNDER_REVIEW / SELECTED / REJECTED 允许
// UNDER_REVIEW → SELECTED / REJECTED 允许
```

#### B. mo/select.jsp 改动

**表格列改造：**

| 原列 | 改为 |
|------|------|
| Application ID | Application ID（保留） |
| Job ID | —（移除，因为在筛选器中已选定） |
| Applicant | **申请人姓名**（从 applicantProfileMap 取 name） + 资料链接 + CV下载链接 |
| Match Score | **多维评分**：总分 + 技能/时间/专业/完整度分项 |
| Missing Skills | 缺失技能（彩色标签） |
| Status | 状态徽章（保留） |
| Action | 操作按钮（加入容量判断，满时禁用 SELECT） |

**具体改动点：**

1. **申请人姓名列：**
```jsp
<td>
    <c:set var="profile" value="${applicantProfileMap[app.applicantUserId]}"/>
    <c:choose>
        <c:when test="${not empty profile}">
            <strong>${profile.name}</strong><br>
            <small>${profile.major} · ${profile.email}</small>
            <c:if test="${not empty profile.cvFileName}">
                <br><a href="${pageContext.request.contextPath}/ta/cv?download=${profile.cvFileName}"
                      class="btn btn-secondary" style="font-size:0.8rem">Download CV</a>
            </c:if>
        </c:when>
        <c:otherwise>
            <span class="text-muted">${app.applicantUserId}</span>
        </c:otherwise>
    </c:choose>
</td>
```

2. **匹配分列（多维展示）：**
```jsp
<td>
    <c:set var="mr" value="${matchResultMap[app.applicationId]}"/>
    <c:if test="${not empty mr}">
        <div class="match-score match-${mr.totalScore >= 80 ? 'high' : mr.totalScore >= 50 ? 'medium' : 'low'}">
            <strong>${mr.totalScore}%</strong>
        </div>
        <small class="text-muted">
            Skills:${mr.skillScore}/50
            Time:${mr.scheduleScore}/25
            Major:${mr.majorScore}/15
            Profile:${mr.completenessScore}/10
        </small>
        <br><small>${mr.detail}</small>
    </c:if>
</td>
```

3. **缺失技能列：**
```jsp
<td>
    <c:choose>
        <c:when test="${not empty app.missingSkills}">
            <c:forEach var="skill" items="${app.missingSkills}">
                <span class="skill-badge skill-missing">${skill} ✗</span>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <span class="text-muted">—</span>
        </c:otherwise>
    </c:choose>
</td>
```

4. **SELECT 按钮增加容量判断：**
```jsp
<c:set var="canSelect" value="true"/>
<c:if test="${app.status != 'SELECTED' && selectedCountByJob[app.jobId] >= jobCapacity}">
    <c:set var="canSelect" value="false"/>
</c:if>
<c:if test="${canSelect}">
    <!-- SELECT 表单 -->
</c:if>
<c:if test="${not canSelect}">
    <span class="text-muted">Capacity full</span>
</c:if>
```

5. **新增容量信息提示条：**
```jsp
<div class="alert alert-info">
    Job capacity: ${jobCapacity} | Already selected: ${selectedCountByJob[selectedJobId] != null ? selectedCountByJob[selectedJobId] : 0}
    | Remaining: ${jobCapacity - (selectedCountByJob[selectedJobId] != null ? selectedCountByJob[selectedJobId] : 0)}
</div>
```

6. **错误提示增强：**
```jsp
<c:if test="${param.error == 'capacity'}">
    <div class="alert alert-error">Cannot select: job capacity reached.</div>
</c:if>
<c:if test="${param.error == 'final'}">
    <div class="alert alert-error">Cannot change: application already finalized.</div>
</c:if>
```

### doGet 新增传递属性清单

| 属性名 | 类型 | 说明 |
|--------|------|------|
| `applicantProfileMap` | `Map<String, Profile>` | applicantUserId → Profile |
| `matchResultMap` | `Map<String, MatchResult>` | applicationId → MatchResult |
| `selectedCountByJob` | `Map<String, Integer>` | jobId → 已选人数 |
| `jobMap` | `Map<String, Job>` | jobId → Job（用于取 capacity） |

### 验收标准

- [ ] MO 审核页显示申请人真实姓名、专业、邮箱
- [ ] MO 可以下载申请人的 CV 文件
- [ ] 申请按匹配度总分降序排列
- [ ] 匹配分展示四维分项 + 总分 + 说明文字
- [ ] 已选人数达到 capacity 时 SELECT 按钮不可用，显示 "Capacity full"
- [ ] 尝试超容量选择时提示错误
- [ ] SELECTED/REJECTED 的申请不可再变更状态
- [ ] 缺失技能用红色标签展示

---

---

## M3 — MO 发岗页修复 + 输入校验

**负责人：** CQ9927
**依赖：** 无
**文件：** `MoPostJobServlet.java`、`mo/post-job.jsp`

### 改动内容

#### A. MoPostJobServlet.doGet() — 只展示自己的岗位

```java
// 改动前：
req.setAttribute("jobs", jobRepository.findAll());

// 改动后：
String userId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));
List<Job> myJobs = jobRepository.findAll().stream()
    .filter(j -> userId.equals(j.getPostedByUserId()))
    .collect(Collectors.toList());
req.setAttribute("jobs", myJobs);
```

#### B. MoPostJobServlet.doPost() — 增强输入校验

```java
// 1. Type 白名单校验（在现有 parseCapacity 之后）
Set<String> VALID_TYPES = Set.of("MODULE", "INVIGILATION");
if (!VALID_TYPES.contains(type.toUpperCase())) {
    req.setAttribute("error", "Job type must be MODULE or INVIGILATION.");
    // forward 回表单
    return;
}

// 2. Schedule 格式校验
// 格式：DAY_HH_HH，如 WED_18_20, MON_14_16
if (!schedule.matches("^(MON|TUE|WED|THU|FRI|SAT|SUN)_\\d{2}_\\d{2}$")) {
    req.setAttribute("error", "Schedule format must be like WED_18_20.");
    return;
}

// 3. requiredSkills 去重
List<String> requiredSkills = Arrays.stream(requiredSkillsRaw.split(","))
    .map(String::trim)
    .filter(s -> !s.isEmpty())
    .distinct()  // ← 新增去重
    .collect(Collectors.toList());
```

#### C. mo/post-job.jsp — Schedule 输入优化

```jsp
<!-- 改动前：自由文本输入 -->
<input type="text" id="schedule" name="schedule" placeholder="WED_18_20">

<!-- 改动后：改为下拉 + 格式提示 -->
<select id="schedule" name="schedule" class="form-control">
    <option value="">— Select time slot —</option>
    <option value="MON_14_16">Monday 14:00-16:00</option>
    <option value="TUE_18_20">Tuesday 18:00-20:00</option>
    <option value="WED_18_20">Wednesday 18:00-20:00</option>
    <option value="THU_18_20">Thursday 18:00-20:00</option>
    <option value="FRI_09_12">Friday 09:00-12:00</option>
    <option value="FRI_10_12">Friday 10:00-12:00</option>
</select>
```

### 验收标准

- [ ] MO 发岗页"所有岗位列表"只显示当前 MO 自己发布的岗位
- [ ] 非法 type 值被拒绝
- [ ] 非法 schedule 格式被拒绝
- [ ] 重复技能被去重

---

---

## M4 — 错误页面配置

**负责人：** CQ9927
**依赖：** 无
**文件：** `web.xml`、新建 `error/404.jsp`、`error/500.jsp`

### 改动内容

#### A. web.xml — 新增 error-page 配置

```xml
<error-page>
    <error-code>404</error-code>
    <location>/WEB-INF/jsp/error/404.jsp</location>
</error-page>
<error-page>
    <error-code>500</error-code>
    <location>/WEB-INF/jsp/error/500.jsp</location>
</error-page>
<error-page>
    <exception-type>java.lang.Throwable</exception-type>
    <location>/WEB-INF/jsp/error/500.jsp</location>
</error-page>
```

#### B. 新建 `src/main/webapp/WEB-INF/jsp/error/404.jsp`

```jsp
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Page Not Found</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<main class="site-main" style="text-align:center; padding:4rem 2rem;">
    <h1>404 — Page Not Found</h1>
    <p class="text-muted">The page you requested does not exist.</p>
    <p><a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Back to Home</a></p>
</main>
</body>
</html>
```

#### C. 新建 `src/main/webapp/WEB-INF/jsp/error/500.jsp`

```jsp
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Server Error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<main class="site-main" style="text-align:center; padding:4rem 2rem;">
    <h1>500 — Internal Server Error</h1>
    <p class="text-muted">Something went wrong. Please try again later.</p>
    <p><a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Back to Home</a></p>
</main>
</body>
</html>
```

### 验收标准

- [ ] 访问不存在的路径显示 404.jsp（而非 Tomcat 默认页面）
- [ ] 触发异常时显示 500.jsp（而非堆栈信息）

---

---

## S1 — TA 端接入新匹配 + 状态页增强

**负责人：** SpPt2FeMa
**依赖：** M1（需要 MatchingService.computeMatch() 和 MatchResult 类）
**文件：** `TaApplyServlet.java`、`TaJobsServlet.java`、`ta/jobs.jsp`、`ta/status.jsp`

### 改动内容

#### A. TaApplyServlet.java — 使用 MatchingService 替代内联计算

**需要新增的字段：**
```java
private MatchingService matchingService;

@Override
public void init() throws ServletException {
    // ... 原有初始化 ...
    matchingService = new MatchingService();
}
```

**buildApplication 方法重写：**

```java
private Application buildApplication(String userId, Job job, Profile profile, String note) {
    // 使用 MatchingService 计算多维匹配
    MatchingService.MatchResult result = matchingService.computeMatch(job, profile);

    String now = Instant.now().toString();
    Application application = new Application();
    application.setJobId(job.getJobId());
    application.setApplicantUserId(userId);
    application.setMatchScore(result.getTotalScore());  // 改为多维总分
    application.setMissingSkills(result.getMissingSkills());
    application.setStatus("SUBMITTED");
    application.setNote(note);
    application.setCreatedAt(now);
    application.setUpdatedAt(now);
    return application;
}
```

**删除：** 原有的 `missingSkills` 内联计算、`matchingCount` 内联计算代码。

#### B. TaJobsServlet.java — 传递 MatchResult 给 JSP

```java
// 新增字段
private MatchingService matchingService;

@Override
public void init() throws ServletException {
    // ... 原有初始化 ...
    matchingService = new MatchingService();
}

// doGet() 中新增：
Map<String, MatchingService.MatchResult> matchResultMap = new HashMap<>();
Map<String, Job> jobMap = new HashMap<>();
for (Job job : filteredJobs) {
    jobMap.put(job.getJobId(), job);
    if (userProfile != null) {
        matchResultMap.put(job.getJobId(), matchingService.computeMatch(job, userProfile));
    }
}
req.setAttribute("matchResultMap", matchResultMap);

// 按匹配度降序排列
List<Job> sortedJobs = new ArrayList<>(filteredJobs);
sortedJobs.sort((a, b) -> {
    int scoreA = matchResultMap.containsKey(a.getJobId())
        ? matchResultMap.get(a.getJobId()).getTotalScore() : 0;
    int scoreB = matchResultMap.containsKey(b.getJobId())
        ? matchResultMap.get(b.getJobId()).getTotalScore() : 0;
    return Integer.compare(scoreB, scoreA);
});
req.setAttribute("jobs", sortedJobs);
```

#### C. ta/jobs.jsp — 替换内联 JSTL 计算，展示多维分数

**删除（第126-139行）：**
```jsp
<!-- 删除内联匹配计算逻辑 -->
<c:set var="matchingCount" value="0" />
<c:forEach var="requiredSkill" items="${job.requiredSkills}">...</c:forEach>
<c:set var="matchScore" value="${matchingCount * 100 / job.requiredSkills.size()}" />
```

**替换为（从 matchResultMap 取）：**
```jsp
<c:set var="mr" value="${matchResultMap[job.jobId]}"/>

<!-- 匹配分列 -->
<td>
    <c:choose>
        <c:when test="${not empty mr}">
            <span class="match-score
                <c:choose>
                    <c:when test="${mr.totalScore >= 80}">match-high</c:when>
                    <c:when test="${mr.totalScore >= 50}">match-medium</c:when>
                    <c:otherwise>match-low</c:otherwise>
                </c:choose>">
                ${mr.totalScore}%
            </span>
            <br><small class="text-muted">
                S:${mr.skillScore}/50 T:${mr.scheduleScore}/25
                M:${mr.majorScore}/15 P:${mr.completenessScore}/10
            </small>
        </c:when>
        <c:otherwise>
            <span class="text-muted">N/A</span>
        </c:otherwise>
    </c:choose>
</td>
```

**技能列表列（利用 matchResultMap）：**
```jsp
<td>
    <div class="skills-list">
        <c:forEach var="skill" items="${job.requiredSkills}">
            <c:choose>
                <c:when test="${not empty mr and mr.matchedSkills.contains(skill)}">
                    <span class="skill-badge skill-match">${skill} ✓</span>
                </c:when>
                <c:otherwise>
                    <span class="skill-badge skill-missing">${skill} ✗</span>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </div>
</td>
```

#### D. ta/status.jsp — 展示技能缺口 + MO 反馈

**新增列：Missing Skills、Feedback**

```jsp
<!-- 表头新增 -->
<th>Match</th>
<th>Missing Skills</th>
<th>Status</th>
<th>Feedback</th>       <!-- 新增 -->
<th>Last Updated</th>

<!-- 每行新增 -->
<td>
    ${app.matchScore}%
    <c:set var="mr" value="${matchResultMap[app.applicationId]}"/>
    <c:if test="${not empty mr}">
        <br><small class="text-muted">${mr.detail}</small>
    </c:if>
</td>
<td>
    <c:choose>
        <c:when test="${not empty app.missingSkills}">
            <c:forEach var="skill" items="${app.missingSkills}">
                <span class="skill-badge skill-missing">${skill}</span>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <span class="text-muted">None — all skills matched</span>
        </c:otherwise>
    </c:choose>
</td>
<td>
    <!-- 状态徽章（保留原逻辑） -->
</td>
<td>
    <c:choose>
        <c:when test="${not empty app.note}">
            ${app.note}
        </c:when>
        <c:otherwise>
            <span class="text-muted">—</span>
        </c:otherwise>
    </c:choose>
</td>
<td>${app.updatedAt}</td>
```

**Job 列隐藏 jobId：**
```jsp
<!-- 改动前 -->
<td>${app.jobId} - ${jobTitles[app.jobId]}</td>

<!-- 改动后 -->
<td>
    <a href="${pageContext.request.contextPath}/ta/jobs">${jobTitles[app.jobId]}</a>
</td>
```

**TaStatusServlet 也需传入 matchResultMap：**
```java
// doGet() 中新增
Map<String, MatchingService.MatchResult> matchResultMap = new HashMap<>();
Profile profile = profileRepository.findByUserId(userId); // 需新增 profileRepository
for (Application app : applications) {
    Job job = jobRepository.findById(app.getJobId());
    if (job != null && profile != null) {
        matchResultMap.put(app.getApplicationId(), matchingService.computeMatch(job, profile));
    }
}
req.setAttribute("matchResultMap", matchResultMap);
```

### 验收标准

- [ ] TA 浏览岗位时，匹配分显示四维分数 + 总分 + 颜色
- [ ] 岗位列表按匹配度降序排列
- [ ] 申请提交时 matchScore 使用多维总分（不再只是技能百分比）
- [ ] 状态页显示缺失技能列表（彩色标签）
- [ ] 状态页显示 MO 的 note 反馈
- [ ] 状态页不显示原始 jobId（只显示岗位标题）
- [ ] `ta/jobs.jsp` 中不再有内联 JSTL 匹配计算代码

---

---

## Z1 — CV/Profile 修复 + 安全加固

**负责人：** zzzskl
**依赖：** 无（独立任务，但需了解 M1 中 MatchResult 使用 Profile 的哪些字段）
**文件：** `TaCvServlet.java`、`ta/cv.jsp`、`TaProfileServlet.java`、`RegisterServlet.java`、`LoginServlet.java`

### 改动内容

#### A. TaCvServlet.doPost() — 补充 setactive action

```java
// 在 doPost() 的 if-else 链中新增：
} else if ("setactive".equals(action)) {
    handleSetActive(req, userId, session);
}

// 新增方法：
private void handleSetActive(HttpServletRequest req, String userId, HttpSession session) {
    String filename = req.getParameter("filename");
    if (filename == null || filename.isEmpty()) {
        session.setAttribute("error", "No filename specified.");
        return;
    }
    // 安全检查：只能设置自己的 CV
    if (!filename.startsWith("CV_" + userId + "_")) {
        session.setAttribute("error", "Cannot set another user's CV as active.");
        return;
    }
    File file = new File(cvStorageDir, filename);
    if (!file.exists()) {
        session.setAttribute("error", "File not found.");
        return;
    }
    Profile profile = profileRepository.findByUserId(userId);
    if (profile == null) {
        session.setAttribute("error", "Please complete your profile first.");
        return;
    }
    profile.setCvFileName(filename);
    profile.setUpdatedAt(Instant.now().toString());
    profileRepository.save(profile);
    session.setAttribute("message", "CV set as active successfully.");
}
```

#### B. TaCvServlet.handleUpload() — 上传前检查 profile

```java
// handleUpload() 中的逻辑改为：
Profile profile = profileRepository.findByUserId(userId);
if (profile == null) {
    // 删除已上传的文件（避免孤儿文件）
    destFile.delete();
    session.setAttribute("error", "Please complete your profile first before uploading CV.");
    return;
}
// ... 更新 profile.cvFileName
```

#### C. ta/cv.jsp — 新增 CV 下载链接

```jsp
<!-- 在 CV 文件列表的每个文件名旁边，新增下载链接 -->
<c:forEach var="cv" items="${existingCvs}">
    <div class="cv-file-item">
        <span>${cv}</span>
        <a href="${pageContext.request.contextPath}/ta/cv?download=${cv}"
           class="btn btn-secondary">Download</a>
        <!-- 原有的 Set as Active 和 Delete 按钮 -->
    </div>
</c:forEach>
```

#### D. TaCvServlet.doGet() — 新增下载功能

```java
// doGet() 开头新增：
String download = req.getParameter("download");
if (download != null && !download.isEmpty()) {
    handleDownload(req, resp, download);
    return;
}

// 新增方法：
private void handleDownload(HttpServletRequest req, HttpServletResponse resp, String filename)
        throws IOException {
    String userId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));
    // 安全检查：只能下载自己的 CV
    if (!filename.startsWith("CV_" + userId + "_")) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
    File file = new File(cvStorageDir, filename);
    if (!file.exists()) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    resp.setContentType("application/octet-stream");
    resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    Files.copy(file.toPath(), resp.getOutputStream());
}
```

#### E. TaProfileServlet.doPost() — 服务端校验

```java
// 在 POST 处理中添加：
String name = trim(req.getParameter("name"));
String email = trim(req.getParameter("email"));
String major = trim(req.getParameter("major"));

if (name.isEmpty()) {
    req.setAttribute("error", "Name is required.");
    req.getRequestDispatcher("/WEB-INF/jsp/ta/profile.jsp").forward(req, resp);
    return;
}
if (email.isEmpty() || !email.contains("@")) {
    req.setAttribute("error", "Valid email is required.");
    req.getRequestDispatcher("/WEB-INF/jsp/ta/profile.jsp").forward(req, resp);
    return;
}
if (major.isEmpty()) {
    req.setAttribute("error", "Major is required.");
    req.getRequestDispatcher("/WEB-INF/jsp/ta/profile.jsp").forward(req, resp);
    return;
}
```

#### F. RegisterServlet.doPost() — 密码最小长度提升

```java
// 改动：4 → 6
if (password == null || password.length() < 6) {
    req.setAttribute("error", "Password must be at least 6 characters.");
    req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
    return;
}
```

同时更新 `auth/register.jsp` 中的前端提示文字（minlength 属性和提示文案）。

#### G. LoginServlet.doPost() — 防 session 固化

```java
// 登录成功时，在 setAttribute 之前：
HttpSession oldSession = req.getSession(false);
if (oldSession != null) {
    oldSession.invalidate();
}
HttpSession session = req.getSession(true);
// 然后继续 setAttribute...
```

### 验收标准

- [ ] CV 页 "Set as Active" 按钮功能正常
- [ ] 上传 CV 前未填 profile 时提示先填资料，不上传孤儿文件
- [ ] CV 页可以下载自己的 CV 文件
- [ ] Profile 保存时服务端校验 name/email/major 非空
- [ ] 注册密码最短 6 位
- [ ] 登录后旧 session 被销毁，新 session 创建

---

---

## 集成测试冒烟清单（全员）

### 测试流程

#### 1. 注册与登录
- [ ] 注册新 TA 账号（密码≥6位）
- [ ] 用新账号登录，验证跳转到 `/ta/dashboard`
- [ ] 验证 TA 无法访问 `/mo/dashboard`（被重定向带 forbidden 提示）

#### 2. TA 完善资料
- [ ] 进入 Profile 页，填写姓名/邮箱/专业/技能/时间
- [ ] 提交后验证保存成功，再次打开显示已保存数据
- [ ] 尝试提交空姓名，验证被拒绝

#### 3. TA 上传 CV
- [ ] 进入 CV 页，上传 PDF 文件
- [ ] 验证上传成功，CV 文件名出现在列表中
- [ ] 点击 Download 验证能下载
- [ ] 上传第二个 CV，点击 Set as Active，验证切换成功
- [ ] Delete 旧 CV，验证删除成功

#### 4. TA 浏览岗位与申请
- [ ] 进入 Jobs 页，验证岗位按匹配度降序排列
- [ ] 验证每个岗位显示：技能匹配(✓/✗)、四维匹配分(S/T/M/P+总分)、颜色标识
- [ ] 筛选功能正常（type/semester/skill）
- [ ] 点击 Apply 申请岗位
- [ ] 验证申请成功，再次打开该岗位显示 "Applied"
- [ ] 尝试重复申请，验证被拦截

#### 5. MO 发岗
- [ ] MO 登录，进入 Post job 页
- [ ] 创建新岗位（选择合法 type 和 schedule）
- [ ] 验证岗位列表只显示该 MO 自己的岗位
- [ ] 尝试非法 schedule，验证被拒绝
- [ ] 验证技能去重

#### 6. MO 审核申请（核心验收点）
- [ ] 进入 Review applications 页
- [ ] 选择岗位，验证申请列表出现
- [ ] **验证显示申请人真实姓名、专业、邮箱**
- [ ] **验证可以下载 CV**
- [ ] **验证申请按匹配度降序排列**
- [ ] **验证匹配分展示四维分项**
- [ ] 点击 Under review → 状态变更
- [ ] 点击 Select → 录取
- [ ] **验证容量上限：选满 capacity 后不能再 SELECT**
- [ ] **验证已 SELECT/REJECTED 的申请不能再次修改状态**

#### 7. TA 查看状态
- [ ] TA 登录，进入 Status 页
- [ ] **验证显示缺失技能列表**
- [ ] **验证显示岗位标题（非 jobId）**
- [ ] **验证 MO 的 note 反馈可见**
- [ ] 验证状态徽章颜色正确

#### 8. Admin 仪表盘
- [ ] Admin 登录，验证 Workload 页统计数据正确
- [ ] 验证 Quick rule signals 面板正常
- [ ] 验证 Per-job workload 表正常

#### 9. 错误页面
- [ ] 访问不存在的 URL，验证显示 404.jsp
- [ ] （可选）触发一个异常，验证显示 500.jsp

---

## 文件改动总览

```
改动文件汇总（按任务）:

M1 - MatchingService.java                   (新增方法 + MatchResult 内部类)

M2 - MoSelectServlet.java                   (加载Profile+匹配+排序+容量检查+状态校验)
   - mo/select.jsp                          (姓名/资料/CV/多维分数/容量提示)

M3 - MoPostJobServlet.java                  (仅展示自己岗位+type白名单+schedule校验+去重)
   - mo/post-job.jsp                        (schedule下拉)

M4 - web.xml                                (error-page 配置)
   - error/404.jsp                          (新建)
   - error/500.jsp                          (新建)

S1 - TaApplyServlet.java                    (使用 MatchingService 替代内联计算)
   - TaJobsServlet.java                     (传递 matchResultMap，排序)
   - TaStatusServlet.java                   (传递 matchResultMap)
   - ta/jobs.jsp                            (替换内联JSTL，展示多维分数)
   - ta/status.jsp                          (缺失技能+反馈+隐藏jobId)

Z1 - TaCvServlet.java                       (setactive+下载+孤儿文件防护)
   - ta/cv.jsp                              (下载链接)
   - TaProfileServlet.java                  (服务端校验)
   - RegisterServlet.java                   (密码6位)
   - LoginServlet.java                      (防session固化)
```
