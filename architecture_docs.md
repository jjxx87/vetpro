# 报销单管理系统架构与时序图

本文档使用 Mermaid 语法绘制了当前报销单管理系统的整体架构框架图以及核心业务（如提交报销单）的时序图。

---

## 1. 系统架构框架图

本系统采用前后端分离的架构设计。前端基于 Vue 3 体系，后端基于 Spring Boot 3 + MyBatisPlus，数据库使用 MySQL。

```mermaid
graph TD
    subgraph "前端展示层（Vue 3 + Element Plus）"
        UI[页面组件 UI]
        List["ReimbursementList.vue<br/>列表页"]
        Detail["ReimbursementDetail.vue<br/>详情/编辑页"]
        UI --> List
        UI --> Detail
    end

    subgraph "前端逻辑层（Pinia + Vue Router）"
        Router["Vue Router<br/>路由跳转"]
        Store["Pinia Store<br/>状态/数据字典管理"]
        API_JS["apis/reimbursement.js<br/>接口封装"]
        
        List -.-> Router
        Detail -.-> Router
        Detail -.-> Store
        List -.-> API_JS
        Detail -.-> API_JS
    end

    subgraph "网络通信层（Vite Proxy / Axios）"
        Axios[Axios 请求拦截与封装]
        API_JS --> Axios
        Axios -- HTTP/JSON --> Nginx[网关/反向代理]
    end

    subgraph "后端应用层（Spring Boot 3）"
        Controller["Controller 层<br/>接收请求与参数校验"]
        Service["Service 层<br/>业务逻辑处理与事务管理"]
        Mapper["Mapper 层（MyBatisPlus）<br/>数据持久化操作"]
        
        Nginx --> Controller
        Controller --> Service
        Service --> Mapper
    end

    subgraph "数据存储层（MySQL 8）"
        DB[("vetech_db<br/>数据库")]
        Table1["reimbursement<br/>主表"]
        Table2["reimbursement_itinerary<br/>行程子表"]
        Table3["reimbursement_subsidy<br/>补助子表"]
        Table4["reimbursement_apportionment<br/>分摊子表"]
        
        Mapper --> DB
        DB --- Table1
        DB --- Table2
        DB --- Table3
        DB --- Table4
    end
```

---

## 2. 核心业务时序图

### 2.1 新增/编辑并提交报销单时序图

该时序图展示了用户在详情页填写完所有信息后，点击“提交”按钮，前后端交互并最终落库的完整流程。

```mermaid
sequenceDiagram
    autonumber
    actor User as 用户
    participant Detail as 前端(ReimbursementDetail)
    participant API as 前端(Axios/API)
    participant Ctrl as 后端(Controller)
    participant Svc as 后端(Service)
    participant DB as 数据库(MySQL)

    User->>Detail: 填写基础信息/补录行程/补助/分摊
    User->>Detail: 点击提交（或保存草稿）
    
    Detail->>Detail: 前置校验（必填、金额、分摊比例等）
    alt 校验未通过且用户选择保存草稿
        Detail-->>User: 提示是否保存为草稿
        User->>Detail: 确认保存草稿
        Detail->>Detail: status = 0
    else 校验通过（直接提交）
        Detail->>Detail: status = 1
    end
    
    Detail->>Detail: 组装 Payload（主表字段+子表数组）
    Detail->>API: POST/PUT /api/reimbursement
    
    API->>Ctrl: 接收请求并校验参数
    Ctrl->>Svc: 调用保存逻辑
    
    rect rgb(240, 248, 255)
        Note right of Svc: 开启事务（@Transactional）
        alt 更新（payload.id 有值）
            Svc->>DB: 更新主表 reimbursement
            Svc->>DB: 删除旧子表数据（行程/补助/分摊）
        else 新增（payload.id 为空）
            Svc->>DB: 插入主表 reimbursement（生成 id）
        end
        
        par 保存行程
            Svc->>DB: 批量插入 reimbursement_itinerary
        and 保存补助
            Svc->>DB: 批量插入 reimbursement_subsidy
        and 保存分摊
            Svc->>DB: 批量插入 reimbursement_apportionment
        end
    end
    
    Svc-->>Ctrl: 返回保存结果（id）
    Ctrl-->>API: 返回统一响应（code=200）
    
    API-->>Detail: 返回结果
    Detail-->>User: 提示成功（提交/草稿）
    Detail->>Detail: 跳转/回退列表页
```

### 2.2 查看报销单详情时序图

该时序图展示了用户从列表页点击进入详情页时，前端如何通过 ID 获取并回显复杂数据的过程。

```mermaid
%%{init: {'themeVariables': {'fontSize': '18px'}}}%%
sequenceDiagram
    autonumber
    actor User as 用户
    participant List as 列表页(ReimbursementList)
    participant Detail as 详情页(ReimbursementDetail)
    participant API as 前端(Axios/API)
    participant Ctrl as 后端(Controller)
    participant Svc as 后端(Service)
    participant DB as 数据库(MySQL)

    User->>List: 点击某条记录的编辑/详情
    List->>Detail: 路由跳转（携带 id）
    
    Detail->>Detail: onMounted 触发
    Detail->>API: GET /api/reimbursement/{id}
    
    API->>Ctrl: 接收请求
    Ctrl->>Svc: 调用查询逻辑
    Svc->>DB: 查询主表 reimbursement
    Svc->>DB: 查询子表 itinerary/subsidy/apportionment
    DB-->>Svc: 返回数据集
    
    Svc->>Svc: 组装成详情对象（主表+子表）
    Svc-->>Ctrl: 返回详情对象
    Ctrl-->>API: 返回统一响应（code=200, data）
    
    API-->>Detail: 返回结果
    
    Detail->>Detail: 字段映射（兼容前后端命名差异）
    Detail->>Detail: 计算衍生字段（补助合计/分摊合计等）
    Detail->>Detail: 写入 formData 并触发渲染
    
    Detail-->>User: 页面渲染完成
```

---

## 3. 业务流程图

### 3.1 报销单全流程（列表→详情→保存/提交→返回）

```mermaid
flowchart TD
    Entry[进入系统] --> ListPage[报销单列表页<br/>ReimbursementList.vue]
    ListPage -->|刷新/查询| ListApi[GET /api/reimbursement/list]
    ListApi --> ListPage

    ListPage -->|新增| NewDetail[详情页（新增）<br/>ReimbursementDetail.vue]
    ListPage -->|编辑/详情| OpenDetail[详情页（回显）<br/>ReimbursementDetail.vue]
    OpenDetail --> DetailApi[GET /api/reimbursement/{id}]
    DetailApi --> OpenDetail

    ListPage -->|删除| DeleteApi[DELETE /api/reimbursement/{id}]
    DeleteApi --> ListPage

    subgraph DetailFlow[详情页编辑流程]
        Base[填写基础信息] --> Trip[补录行程]
        Trip --> Subsidy[补助信息（按天勾选/录入）]
        Subsidy --> Apportion[费用归属及分摊]
        Apportion --> Validate{前置校验}
        Validate -->|校验通过| Submit[提交<br/>status=1]
        Validate -->|校验未通过| AskDraft{是否保存草稿}
        AskDraft -->|是| Draft[保存草稿<br/>status=0]
        AskDraft -->|否| Fix[返回修改必填项/比例/金额]
        Fix --> Base
    end

    NewDetail --> Base
    OpenDetail --> Base

    Submit --> SaveApi[POST/PUT /api/reimbursement]
    Draft --> SaveApi
    SaveApi --> Back[返回列表页]
    Back --> ListPage
```
