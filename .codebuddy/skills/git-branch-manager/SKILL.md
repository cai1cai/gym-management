---
name: git-branch-manager
description: 当用户需要创建新分支、推送代码或创建合并请求(PR/MR)时使用此技能。适用于本地代码修改后的 Git 工作流程操作。支持 GitHub 和 GitLab 平台。
---

# Git 分支管理器

## 概述

此技能帮助用户完成从本地代码修改到创建合并请求的完整 Git 工作流程，包括创建分支、推送代码和生成 PR/MR 创建链接。自动识别 GitHub 和 GitLab 平台。

## 使用时机

- 用户说"创建分支"、"新建分支"
- 用户说"创建合并请求"、"创建PR"、"创建MR"、"提交PR"
- 用户说"推送代码到远程"
- 用户需要完成本地修改后的 Git 操作流程

## 默认配置

| 配置项 | 默认值 |
|--------|--------|
| 默认目标分支 | `trunk` |
| 提交人 | cai1cai |
| 审核人 | cai1cai |
| 远程仓库名 | `origin` |

## 平台识别

通过远程仓库地址自动识别平台：

```bash
git config --get remote.origin.url
```

| 平台 | URL 格式 |
|------|----------|
| GitHub | `github.com` 或 `git@github.com` |
| GitLab | `gitlab.com` 或 `git@gitlab.com` 或私有 GitLab 地址 |

## 工作流程

### 场景一：创建新分支并创建合并请求

当用户请求创建分支和合并请求时，按以下步骤执行：

#### 步骤 1：确认参数

向用户确认以下信息（未提供的需要询问）：
- **新分支名称**：必须由用户指定
- **目标分支**：未指定则使用默认值 `trunk`

> 注意：PR/MR 的标题和描述由用户在浏览器中手动填写，无需提前确认

#### 步骤 2：创建分支

```bash
# 从目标分支创建新分支
git checkout <目标分支>
git pull origin <目标分支>
git checkout -b <新分支名称>
```

#### 步骤 3：推送分支到远程

```bash
git push -u origin <新分支名称>
```

#### 步骤 4：生成合并请求链接

先获取远程仓库地址：
```bash
git config --get remote.origin.url
```

根据平台生成对应的链接：

##### GitHub 链接格式
```
https://github.com/<用户名>/<仓库名>/compare/<目标分支>...<新分支>?expand=1
```

##### GitLab 链接格式
```
https://<gitlab地址>/<用户名>/<仓库名>/-/merge_requests/new?merge_request[source_branch]=<新分支>&merge_request[target_branch]=<目标分支>
```

将链接提供给用户，用户点击链接在浏览器中：
1. 填写 PR/MR 标题
2. 填写 PR/MR 描述
3. 选择提交人：cai1cai
4. 选择审核人：cai1cai

#### 步骤 5：确认结果

向用户报告：
- 创建的分支名称
- 目标分支
- 平台类型（GitHub/GitLab）
- PR/MR 创建链接

### 场景二：仅创建分支

如果用户只需要创建分支，不需要创建 PR/MR：

```bash
git checkout <目标分支>
git pull origin <目标分支>
git checkout -b <新分支名称>
```

### 场景三：推送已存在的分支并创建 PR/MR

如果分支已存在，只需推送并生成链接：

```bash
git push -u origin <分支名称>
```

然后根据平台生成 PR/MR 创建链接。

## 常用命令参考

| 操作 | 命令 |
|------|------|
| 查看当前分支 | `git branch` |
| 查看远程分支 | `git branch -r` |
| 切换分支 | `git checkout <分支名>` |
| 创建并切换分支 | `git checkout -b <新分支名>` |
| 推送到远程 | `git push -u origin <分支名>` |
| 获取远程仓库地址 | `git config --get remote.origin.url` |

## URL 解析规则

从远程地址提取仓库信息：

| 远程地址格式 | 解析方式 |
|--------------|----------|
| `git@github.com:user/repo.git` | 平台=GitHub, 用户=user, 仓库=repo |
| `https://github.com/user/repo.git` | 平台=GitHub, 用户=user, 仓库=repo |
| `git@gitlab.com:user/repo.git` | 平台=GitLab, 用户=user, 仓库=repo |
| `https://gitlab.com/user/repo.git` | 平台=GitLab, 用户=user, 仓库=repo |
| `git@gitlab.xxx.com:user/repo.git` | 平台=GitLab(私有), 地址=gitlab.xxx.com |

## 输出格式

完成操作后，使用以下格式报告结果：

```
## Git 操作完成

**操作类型**: [创建分支 / 推送代码 / 创建PR链接 / 创建MR链接]
**平台**: [GitHub / GitLab]

### 详情
- **新分支**: <分支名>
- **目标分支**: <目标分支>

### 合并请求创建链接
<链接>

> 点击上方链接在浏览器中创建合并请求：
> 1. 填写标题和描述
> 2. 选择提交人：cai1cai
> 3. 选择审核人：cai1cai
```

## 示例

**用户**: "创建一个分支叫 feature/login，然后创建合并请求到 trunk"

**响应**:
1. 确认参数：新分支=feature/login，目标分支=trunk
2. 执行创建分支命令
3. 推送到远程
4. 检测平台，生成对应链接
5. 报告结果

---

**用户**: "创建分支 bugfix/crash-fix"

**响应**:
1. 确认参数：新分支=bugfix/crash-fix，目标分支=trunk（默认）
2. 执行创建分支命令
3. 询问是否需要推送和创建合并请求

---

**用户**: "在 GitLab 上创建分支 hotfix/issue-123，创建 MR"

**响应**:
1. 确认参数和平台
2. 执行创建分支命令
3. 推送到远程
4. 生成 GitLab MR 链接
5. 报告结果
