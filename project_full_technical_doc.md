# Vetech 项目完整功能技术文档（前后端）

本文档面向开发/运维人员，覆盖本项目当前实现的完整功能与技术实现细节，包含：系统架构、数据库、后端接口与事务、前端页面与交互逻辑、构建与部署（含 Docker）、以及关键业务规则。  
要求：不使用文件链接，直接给出关键实现代码与配置。

---

## 0. 项目概览

### 0.1 功能范围（当前已实现）
- 报销单列表：查看、搜索过滤（前端本地过滤）、分页（前端本地分页）、删除、作废（status=2）、进入编辑/详情
- 报销单详情：新增、编辑、回显、保存草稿（status=0）、提交（status=1）
- 详情子模块：
  - 补录行程：新增/编辑/删除/复制；日期重叠校验；自动生成补助日历
  - 补助信息：按“日历天”选择餐补/交补/通讯补；合计金额回写；标准总额展示
  - 费用归属与分摊：多行分摊比例与金额；均分；合计校验
- 只读控制：已完成/已作废（status=1/2）在详情页禁用全部输入与按钮（包含关闭/提交）

### 0.2 技术栈
- 前端：Vue 3 + Vue Router + Pinia + Element Plus + Axios + Vite
- 后端：Spring Boot 3 + MyBatis-Plus + MySQL
- 部署：前端 Docker（多阶段构建）+ Nginx 静态托管与 /api 反代

---

## 1. 数据库设计（MySQL）

### 1.1 初始化 SQL（serve/schema.sql）

```sql
CREATE DATABASE IF NOT EXISTS `vetech_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `vetech_db`;

DROP TABLE IF EXISTS `reimbursement`;
CREATE TABLE `reimbursement` (
  `id` varchar(32) NOT NULL COMMENT '主键 ID',
  `creation_time` varchar(32) DEFAULT NULL COMMENT '创建时间',
  `reimbursement_title` varchar(255) DEFAULT NULL COMMENT '报销标题',
  `reimburser_id` varchar(32) DEFAULT NULL COMMENT '报销人 ID',
  `reimburser_no` varchar(32) DEFAULT NULL COMMENT '报销人工号',
  `reimburser_name` varchar(50) DEFAULT NULL COMMENT '报销人姓名',
  `reim_department_id` varchar(32) DEFAULT NULL COMMENT '报销部门 ID',
  `reim_department_no` varchar(32) DEFAULT NULL COMMENT '报销部门编号',
  `reim_department_name` varchar(50) DEFAULT NULL COMMENT '报销部门名称',
  `reim_company_id` varchar(32) DEFAULT NULL COMMENT '费用归属公司 ID',
  `reim_company_no` varchar(32) DEFAULT NULL COMMENT '费用归属公司编号',
  `reim_company_name` varchar(50) DEFAULT NULL COMMENT '费用归属公司名称',
  `business_type_id` varchar(32) DEFAULT NULL COMMENT '业务类型 ID',
  `business_type_no` varchar(32) DEFAULT NULL COMMENT '业务类型编号',
  `business_type_name` varchar(50) DEFAULT NULL COMMENT '业务类型名称',
  `business_trip_reason` text DEFAULT NULL COMMENT '出差事由',
  `subsidy_total` decimal(10,2) DEFAULT '0.00' COMMENT '补助总金额',
  `meal_allowance` decimal(10,2) DEFAULT '0.00' COMMENT '餐费补助',
  `transportation_allowance` decimal(10,2) DEFAULT '0.00' COMMENT '交通补助',
  `phone_allowance` decimal(10,2) DEFAULT '0.00' COMMENT '通讯补助',
  `remarks` text DEFAULT NULL COMMENT '备注信息',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态：0草稿 1已完成 2已作废',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单主表';

DROP TABLE IF EXISTS `reimbursement_itinerary`;
CREATE TABLE `reimbursement_itinerary` (
  `id` varchar(32) NOT NULL COMMENT '主键 ID',
  `reimbursement_id` varchar(32) DEFAULT NULL COMMENT '关联主表 ID',
  `employee_id` varchar(32) DEFAULT NULL COMMENT '出行人 ID',
  `start_city` varchar(20) DEFAULT NULL COMMENT '出发城市编号',
  `end_city` varchar(20) DEFAULT NULL COMMENT '到达城市编号',
  `start_date` varchar(20) DEFAULT NULL COMMENT '出发日期',
  `end_date` varchar(20) DEFAULT NULL COMMENT '到达日期',
  `reason` varchar(500) DEFAULT NULL COMMENT '行程说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单-补录行程表';

DROP TABLE IF EXISTS `reimbursement_subsidy`;
CREATE TABLE `reimbursement_subsidy` (
  `id` varchar(32) NOT NULL COMMENT '主键 ID',
  `reimbursement_id` varchar(32) DEFAULT NULL COMMENT '关联主表 ID',
  `employee_id` varchar(32) DEFAULT NULL COMMENT '出行人 ID',
  `start_date` varchar(20) DEFAULT NULL COMMENT '开始日期',
  `end_date` varchar(20) DEFAULT NULL COMMENT '结束日期',
  `start_city` varchar(20) DEFAULT NULL COMMENT '出发城市编号',
  `end_city` varchar(20) DEFAULT NULL COMMENT '到达城市编号',
  `days` int(11) DEFAULT NULL COMMENT '补助天数',
  `meal_amount` varchar(20) DEFAULT NULL COMMENT '餐费金额',
  `traffic_amount` varchar(20) DEFAULT NULL COMMENT '交通金额',
  `comm_amount` varchar(20) DEFAULT NULL COMMENT '通讯金额',
  `calendar` text DEFAULT NULL COMMENT '补助日历明细JSON',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单-补助信息表';

DROP TABLE IF EXISTS `reimbursement_apportionment`;
CREATE TABLE `reimbursement_apportionment` (
  `id` varchar(32) NOT NULL COMMENT '主键 ID',
  `reimbursement_id` varchar(32) DEFAULT NULL COMMENT '关联主表 ID',
  `company_id` varchar(32) DEFAULT NULL COMMENT '费用归属公司 ID',
  `project_id` varchar(32) DEFAULT NULL COMMENT '项目 ID',
  `percent` decimal(5,2) DEFAULT '0.00' COMMENT '分摊比例(%)',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '分摊金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单-费用分摊表';
```

### 1.2 表关系与数据流
- reimbursement（主表）1:N reimbursement_itinerary（行程）
- reimbursement（主表）1:N reimbursement_subsidy（补助）
- reimbursement（主表）1:N reimbursement_apportionment（分摊）
- 删除主表时：后端显式删除三张子表数据，再删主表（事务内）
- 更新主表时：先更新主表，再删除旧子表，最后写入新子表（事务内）

---

## 2. 后端（Spring Boot + MyBatis-Plus）

### 2.1 依赖（serve/pom.xml 关键项）

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.6</version>
  </dependency>
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
  </dependency>
</dependencies>
```

### 2.2 配置（serve/src/main/resources/application.yml）

说明：数据库密码属于敏感信息，生产环境应使用环境变量/密钥管理；本文档用占位符展示。

```yml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/vetech_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: "******"

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  global-config:
    db-config:
      id-type: assign_uuid
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl
```

### 2.3 启动入口（serve/src/main/java/com/vetech/serve/ServeApplication.java）

```java
@SpringBootApplication
public class ServeApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServeApplication.class, args);
    }
}
```

### 2.4 领域模型（Entity）

#### 2.4.1 主表实体（serve/src/main/java/.../entity/Reimbursement.java）

```java
@Data
@TableName("reimbursement")
public class Reimbursement {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String creationTime;
    private String reimbursementTitle;
    private String reimburserId;
    private String reimburserNo;
    private String reimburserName;
    private String reimDepartmentId;
    private String reimDepartmentNo;
    private String reimDepartmentName;
    private String reimCompanyId;
    private String reimCompanyNo;
    private String reimCompanyName;
    private String businessTypeId;
    private String businessTypeNo;
    private String businessTypeName;
    private String businessTripReason;
    private String subsidyTotal;
    private String mealAllowance;
    private String transportationAllowance;
    private String phoneAllowance;
    private String remarks;
    private Integer status;

    @TableField(exist = false)
    private List<ReimbursementItinerary> itineraries;

    @TableField(exist = false)
    private List<ReimbursementSubsidy> subsidies;

    @TableField(exist = false)
    private List<ReimbursementApportionment> apportionments;
}
```

#### 2.4.2 行程子表实体（ReimbursementItinerary）

```java
@Data
@TableName("reimbursement_itinerary")
public class ReimbursementItinerary {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String reimbursementId;
    private String employeeId;
    private String startCity;
    private String endCity;
    private String startDate;
    private String endDate;
    private String reason;
}
```

#### 2.4.3 补助子表实体（ReimbursementSubsidy，日历明细 JSON）

```java
@Data
@TableName(value = "reimbursement_subsidy", autoResultMap = true)
public class ReimbursementSubsidy {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String reimbursementId;
    private String employeeId;
    private String startDate;
    private String endDate;
    private String startCity;
    private String endCity;
    private Integer days;
    private String mealAmount;
    private String trafficAmount;
    private String commAmount;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> calendar;
}
```

#### 2.4.4 分摊子表实体（ReimbursementApportionment）

```java
@Data
@TableName("reimbursement_apportionment")
public class ReimbursementApportionment {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String reimbursementId;
    private String companyId;
    private String projectId;
    private BigDecimal percent;
    private BigDecimal amount;
}
```

### 2.5 数据访问层（Mapper）

MyBatis-Plus BaseMapper 直接提供 CRUD 能力（本项目未额外写 XML）。

```java
@Mapper
public interface ReimbursementMapper extends BaseMapper<Reimbursement> {}

@Mapper
public interface ReimbursementItineraryMapper extends BaseMapper<ReimbursementItinerary> {}

@Mapper
public interface ReimbursementSubsidyMapper extends BaseMapper<ReimbursementSubsidy> {}

@Mapper
public interface ReimbursementApportionmentMapper extends BaseMapper<ReimbursementApportionment> {}
```

### 2.6 业务层（Service）与事务策略

核心目标：主表与三张子表“同一事务写入/更新”，避免主子不一致。

#### 2.6.1 接口（IReimbursementService）

```java
public interface IReimbursementService extends IService<Reimbursement> {}
```

#### 2.6.2 实现（ReimbursementServiceImpl）

关键点：
- save：若无 id 生成 UUID；保存主表后保存子表
- updateById：更新主表；删除旧子表；写入新子表
- removeById：先删子表；再删主表
- getById：查询主表，并查询并挂载 itineraries/subsidies/apportionments

```java
@Service
public class ReimbursementServiceImpl extends ServiceImpl<ReimbursementMapper, Reimbursement>
        implements IReimbursementService {

    @Autowired
    private ReimbursementItineraryMapper itineraryMapper;
    @Autowired
    private ReimbursementSubsidyMapper subsidyMapper;
    @Autowired
    private ReimbursementApportionmentMapper apportionmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Reimbursement entity) {
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        boolean result = super.save(entity);
        saveNested(entity);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Reimbursement entity) {
        boolean result = super.updateById(entity);
        deleteNested(entity.getId());
        saveNested(entity);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        deleteNested((String) id);
        return super.removeById(id);
    }

    @Override
    public Reimbursement getById(Serializable id) {
        Reimbursement reimbursement = super.getById(id);
        if (reimbursement != null) {
            reimbursement.setItineraries(itineraryMapper.selectList(
                    new LambdaQueryWrapper<ReimbursementItinerary>()
                            .eq(ReimbursementItinerary::getReimbursementId, id)
            ));
            reimbursement.setSubsidies(subsidyMapper.selectList(
                    new LambdaQueryWrapper<ReimbursementSubsidy>()
                            .eq(ReimbursementSubsidy::getReimbursementId, id)
            ));
            reimbursement.setApportionments(apportionmentMapper.selectList(
                    new LambdaQueryWrapper<ReimbursementApportionment>()
                            .eq(ReimbursementApportionment::getReimbursementId, id)
            ));
        }
        return reimbursement;
    }

    private void saveNested(Reimbursement entity) {
        String id = entity.getId();
        if (entity.getItineraries() != null) {
            for (ReimbursementItinerary item : entity.getItineraries()) {
                item.setReimbursementId(id);
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                itineraryMapper.insert(item);
            }
        }
        if (entity.getSubsidies() != null) {
            for (ReimbursementSubsidy item : entity.getSubsidies()) {
                item.setReimbursementId(id);
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                subsidyMapper.insert(item);
            }
        }
        if (entity.getApportionments() != null) {
            for (ReimbursementApportionment item : entity.getApportionments()) {
                item.setReimbursementId(id);
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                apportionmentMapper.insert(item);
            }
        }
    }

    private void deleteNested(String reimbursementId) {
        itineraryMapper.delete(new LambdaQueryWrapper<ReimbursementItinerary>()
                .eq(ReimbursementItinerary::getReimbursementId, reimbursementId));
        subsidyMapper.delete(new LambdaQueryWrapper<ReimbursementSubsidy>()
                .eq(ReimbursementSubsidy::getReimbursementId, reimbursementId));
        apportionmentMapper.delete(new LambdaQueryWrapper<ReimbursementApportionment>()
                .eq(ReimbursementApportionment::getReimbursementId, reimbursementId));
    }
}
```

### 2.7 控制器层（Controller）与 REST API

#### 2.7.1 Controller 代码（serve/src/main/java/.../controller/ReimbursementController.java）

功能：
- `POST /api/reimbursement` 新增（含子表）
- `PUT /api/reimbursement` 更新（含子表，覆盖式更新）
- `GET /api/reimbursement/{id}` 详情（主表+子表）
- `GET /api/reimbursement/list` 列表（按 creationTime 倒序）
- `DELETE /api/reimbursement/{id}` 删除（主表+子表）

```java
@RestController
@RequestMapping("/api/reimbursement")
public class ReimbursementController {
    @Autowired
    private IReimbursementService reimbursementService;

    @PostMapping
    public boolean save(@RequestBody Reimbursement reimbursement) {
        return reimbursementService.save(reimbursement);
    }

    @GetMapping("/{id}")
    public Reimbursement getById(@PathVariable String id) {
        return reimbursementService.getById(id);
    }

    @GetMapping("/list")
    public List<Reimbursement> list() {
        return reimbursementService.lambdaQuery()
                .orderByDesc(Reimbursement::getCreationTime)
                .list();
    }

    @PutMapping
    public boolean update(@RequestBody Reimbursement reimbursement) {
        return reimbursementService.updateById(reimbursement);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable String id) {
        return reimbursementService.removeById(id);
    }
}
```

#### 2.7.2 请求/响应约定（实际代码行为）
- 请求体：JSON，直接映射到 `Reimbursement`（包含 `itineraries/subsidies/apportionments` 三个数组）
- 响应：当前接口直接返回 `boolean` 或 `Reimbursement` 对象（未封装统一响应结构）

示例：新增/提交（POST /api/reimbursement）
```json
{
  "title": "示例标题(前端别名字段)",
  "reimbursementTitle": "示例标题",
  "reimburserId": "13AB3A3F72409002",
  "reimDepartmentId": "13AB8D7B52A9B002",
  "reimCompanyId": "1C61686865DA8000",
  "businessTypeId": "1B5FEB7DD4396000",
  "businessTripReason": "出差事由",
  "creationTime": "2026-05-28 10:11:12",
  "status": 1,
  "itineraries": [
    {
      "employeeId": "13AB3A3F72409002",
      "startCity": "10458",
      "endCity": "10119",
      "startDate": "2026-05-20",
      "endDate": "2026-05-22",
      "reason": "行程说明"
    }
  ],
  "subsidies": [
    {
      "employeeId": "13AB3A3F72409002",
      "startDate": "2026-05-20",
      "endDate": "2026-05-22",
      "startCity": "10458",
      "endCity": "10119",
      "days": 3,
      "calendar": [
        {
          "date": "2026-05-20",
          "weekday": "星期三",
          "city": "10119",
          "mealSelected": true,
          "mealStandard": 100,
          "mealAmount": 100,
          "trafficSelected": true,
          "trafficStandard": 40,
          "trafficAmount": 40,
          "commSelected": true,
          "commStandard": 40,
          "commAmount": 40
        }
      ]
    }
  ],
  "apportionments": [
    { "companyId": "1C61686865DA8000", "projectId": "", "percent": 100, "amount": 540 }
  ]
}
```

---

## 3. 前端（Vue 3 + Element Plus）

### 3.1 工程与构建（package.json）

```json
{
  "name": "vetech",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@element-plus/icons-vue": "^2.3.2",
    "axios": "^1.7.9",
    "element-plus": "^2.14.0",
    "pinia": "^3.0.4",
    "vue": "^3.5.32",
    "vue-router": "^5.0.7"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^6.0.6",
    "vite": "^8.0.8",
    "vite-plugin-vue-devtools": "^8.1.1"
  }
}
```

### 3.2 Vite 开发代理（vite.config.js）

开发环境 `/api` 代理到本机后端 `8080`（生产由 nginx.conf 处理）。

```js
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### 3.3 入口与全局注册（src/main.js）

```js
import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
```

### 3.4 路由（src/router/index.js）

```js
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'list', component: List },
    { path: '/detail/:id?', name: 'detail', component: Detail }
  ]
})
```

### 3.5 请求封装（src/utils/request.js）

```js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '',
  timeout: 10000
})

request.interceptors.request.use(
  config => config,
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => response.data,
  error => {
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request
```

### 3.6 API 封装（src/apis/reimbursement.js）

```js
import request from '../utils/request'

export const getReimbursementList = () => request({ url: '/api/reimbursement/list', method: 'get' })
export const getReimbursementById = (id) => request({ url: `/api/reimbursement/${id}`, method: 'get' })
export const addReimbursement = (data) => request({ url: '/api/reimbursement', method: 'post', data })
export const updateReimbursement = (data) => request({ url: '/api/reimbursement', method: 'put', data })
export const deleteReimbursement = (id) => request({ url: `/api/reimbursement/${id}`, method: 'delete' })
```

### 3.7 字典数据（Pinia：src/stores/dict.js）

说明：本项目把公司/部门/员工/业务类型/城市/项目等字典以“静态数组”写在前端 Store 中，并提供业务类型树形转换 getter。  
字典数组内容较长且包含大量固定数据，此处重点展示结构与 getter（数组本体与项目代码保持一致）。

```js
import { defineStore } from 'pinia'

export const useDictStore = defineStore('dict', {
  state: () => ({
    companies: [/* ... */],
    departments: [/* ... */],
    employees: [/* ... */],
    businessTypes: [/* ... */],
    cities: [/* ... */],
    projects: [/* ... */]
  }),
  getters: {
    businessTypeTree(state) {
      const buildTree = (parentId) => {
        return state.businessTypes
          .filter(item => item.superiorId === parentId)
          .map(item => ({
            value: item.businessTypeId,
            label: item.businessTypeName,
            children: item.thereSubordinateNode === "1" ? buildTree(item.businessTypeId) : undefined
          }))
      }
      return buildTree("none")
    }
  }
})
```

---

## 4. 业务页面与核心逻辑

### 4.1 列表页（src/views/ReimbursementList.vue）

#### 4.1.1 功能点
- 从后端拉取列表数据 `GET /api/reimbursement/list`
- 搜索条件：单号(id片段)、标题、事由、公司、部门、报销人、业务类型（支持选中父节点后匹配所有子节点）
- 分页：前端本地分页（Element Plus Pagination）
- 操作：
  - 新增：跳 `/detail`
  - 编辑：跳 `/detail/:id`；当 status=1/2 时禁用编辑按钮
  - 删除：调用 `DELETE /api/reimbursement/:id`
  - 作废：调用 `PUT /api/reimbursement`，仅传 `{id,status:2}` 做部分更新

#### 4.1.2 核心实现代码

拉取列表与本地保存：
```js
const allData = ref([])

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getReimbursementList()
    allData.value = res || []
  } finally {
    loading.value = false
  }
}
```

业务类型过滤（父节点包含子节点的所有 businessTypeId）：
```js
const filteredList = computed(() => {
  const selectedBusinessTypeId = searchForm.value.businessTypeId
  let businessTypeIdSet = null
  if (selectedBusinessTypeId) {
    const childrenByParentId = new Map()
    dictStore.businessTypes.forEach(t => {
      const parentId = t.superiorId
      if (!childrenByParentId.has(parentId)) childrenByParentId.set(parentId, [])
      childrenByParentId.get(parentId).push(t.businessTypeId)
    })

    businessTypeIdSet = new Set()
    const stack = [selectedBusinessTypeId]
    while (stack.length) {
      const id = stack.pop()
      if (businessTypeIdSet.has(id)) continue
      businessTypeIdSet.add(id)
      const children = childrenByParentId.get(id) || []
      children.forEach(childId => stack.push(childId))
    }
  }

  return allData.value.filter(item => {
    let match = true
    if (searchForm.value.reimNo && !item.id?.includes(searchForm.value.reimNo)) match = false
    if (searchForm.value.title && !item.reimbursementTitle?.includes(searchForm.value.title)) match = false
    if (searchForm.value.reason && !item.businessTripReason?.includes(searchForm.value.reason)) match = false
    if (searchForm.value.companyId && item.reimCompanyId !== searchForm.value.companyId) match = false
    if (searchForm.value.departmentId && item.reimDepartmentId !== searchForm.value.departmentId) match = false
    if (searchForm.value.employeeId && item.reimburserId !== searchForm.value.employeeId) match = false
    if (businessTypeIdSet && !businessTypeIdSet.has(item.businessTypeId)) match = false
    return match
  })
})
```

分页切片：
```js
const tableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})
```

作废（部分字段更新）：
```js
const handleVoid = (row) => {
  ElMessageBox.confirm('确认作废该报销单吗?', '提示', { type: 'warning' }).then(async () => {
    await updateReimbursement({ id: row.id, status: 2 })
    ElMessage.success('作废成功')
    fetchList()
  })
}
```

编辑按钮禁用逻辑（模板层）：
```vue
<el-button
  :type="row.status === 1 || row.status === 2 ? 'info' : 'primary'"
  link
  @click="handleEdit(row)"
  :disabled="row.status === 1 || row.status === 2"
>
  <el-icon :size="16"><EditPen /></el-icon>
</el-button>
```

---

### 4.2 详情页（src/views/ReimbursementDetail.vue）

#### 4.2.1 页面数据模型（formData）

```js
const formData = reactive({
  id: '',
  title: '',
  reimbursementTitle: '',
  employeeId: '',
  reimburserId: '',
  departmentId: '',
  reimDepartmentId: '',
  companyId: '',
  reimCompanyId: '',
  businessTypeId: '',
  reason: '',
  businessTripReason: '',
  status: 0,
  createTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  creationTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  itineraries: [],
  subsidies: [],
  apportionments: [{ companyId: '', projectId: '', percent: 100, amount: 0 }],
  remarks: ''
})
```

#### 4.2.2 回显加载与字段映射

后端返回字段与页面绑定字段存在“别名”，回显时做兼容映射，确保表单可展示并能提交回后端字段：

```js
onMounted(async () => {
  const id = route.params.id
  if (id) {
    const res = await getReimbursementById(id)
    if (res) {
      res.title = res.reimbursementTitle
      res.employeeId = res.reimburserId
      res.departmentId = res.reimDepartmentId
      res.companyId = res.reimCompanyId
      res.reason = res.businessTripReason
      res.createTime = res.creationTime

      Object.assign(formData, res)

      formData.reimbursementTitle = res.reimbursementTitle || res.title
      formData.reimburserId = res.reimburserId || res.employeeId
      formData.reimDepartmentId = res.reimDepartmentId || res.departmentId
      formData.reimCompanyId = res.reimCompanyId || res.companyId
      formData.businessTypeId = res.businessTypeId
      formData.businessTripReason = res.businessTripReason || res.reason

      if(!formData.itineraries) formData.itineraries = []
      if(!formData.subsidies) formData.subsidies = []

      formData.subsidies.forEach(sub => {
        sub.days = sub.days || (sub.calendar ? sub.calendar.length : 0)
        let apply = 0
        let amount = 0
        if (sub.calendar && sub.calendar.length > 0) {
          sub.calendar.forEach(cal => {
            apply += (cal.mealStandard || 0) + (cal.trafficStandard || 0) + (cal.commStandard || 0)
            amount += (cal.mealSelected ? (cal.mealAmount || 0) : 0) +
                      (cal.trafficSelected ? (cal.trafficAmount || 0) : 0) +
                      (cal.commSelected ? (cal.commAmount || 0) : 0)
          })
        }
        sub.applyAmount = apply
        sub.subsidyAmount = amount
      })

      if(!formData.apportionments || formData.apportionments.length === 0) {
        formData.apportionments = [{
          companyId: formData.reimCompanyId,
          projectId: '',
          percent: 100,
          amount: Number(formData.subsidyTotal || 0)
        }]
      }
    }
  }
})
```

#### 4.2.3 只读态（已完成/已作废禁用）

```js
const isReadOnly = computed(() => formData.status === 1 || formData.status === 2)
```

模板禁用示例：
```vue
<el-form ... :disabled="isReadOnly">...</el-form>
<el-button ... :disabled="isReadOnly">提交</el-button>
<el-button ... :disabled="isReadOnly">关闭</el-button>
```

关键方法入口短路（防绕过）：
```js
const doSubmit = (status) => {
  if (isReadOnly.value) return
  // ...
}
```

#### 4.2.4 补录行程：弹窗、校验与“自动生成补助”

弹窗表单模型：
```js
const itineraryForm = reactive({
  employeeId: '',
  startCity: '',
  endCity: '',
  dateRange: [],
  startDate: '',
  endDate: '',
  reason: ''
})
```

保存行程关键校验：
- 结束日期不能早于开始日期
- 到达日期不能晚于当前日期
- 同一员工行程日期不能重叠（区间交集判断）

```js
const saveItinerary = () => {
  if (isReadOnly.value) return
  itineraryFormRef.value.validate((valid) => {
    if (!valid) return

    itineraryForm.startDate = itineraryForm.dateRange[0].split(' ')[0]
    itineraryForm.endDate = itineraryForm.dateRange[1].split(' ')[0]

    if (itineraryForm.endDate < itineraryForm.startDate) return ElMessage.error('到达日期不能早于出发日期')
    if (itineraryForm.endDate > currentDate) return ElMessage.error('到达日期不能晚于当前日期')

    const start = new Date(itineraryForm.startDate).getTime()
    const end = new Date(itineraryForm.endDate).getTime()
    const overlap = formData.itineraries.some((item, idx) => {
      if (idx === editItineraryIndex.value) return false
      if (item.employeeId !== itineraryForm.employeeId) return false
      const s = new Date(item.startDate).getTime()
      const e = new Date(item.endDate).getTime()
      return Math.max(start, s) <= Math.min(end, e)
    })
    if (overlap) return ElMessage.error('该人员行程日期存在重复，请重新选择')

    const days = Math.floor((end - start) / (1000 * 3600 * 24)) + 1

    if (editItineraryIndex.value > -1) {
      formData.itineraries[editItineraryIndex.value] = { ...itineraryForm }
      generateSubsidy(editItineraryIndex.value, { ...itineraryForm }, days)
    } else {
      formData.itineraries.push({ ...itineraryForm })
      generateSubsidy(formData.itineraries.length - 1, { ...itineraryForm }, days)
    }
    recalculateApportionment()
    itineraryVisible.value = false
  })
}
```

补助标准规则（按城市类型映射餐补；交补/通讯固定 40）：
```js
const getSubsidyStandard = (cityNo) => {
  const city = dictStore.cities.find(c => c.cityNo === cityNo)
  const type = city ? city.cityType : '3'
  const meal = type === '1' ? 100 : type === '2' ? 80 : 50
  return { meal, traffic: 40, comm: 40 }
}
```

根据行程自动生成补助日历（默认全选、金额=标准）：
```js
const generateSubsidy = (index, itinerary, days) => {
  const standard = getSubsidyStandard(itinerary.endCity)
  const calendar = []
  let current = new Date(itinerary.startDate)
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

  for (let i = 0; i < days; i++) {
    calendar.push({
      date: current.toISOString().split('T')[0],
      weekday: weekdays[current.getDay()],
      city: itinerary.endCity,
      selected: true,
      mealSelected: true, mealStandard: standard.meal, mealAmount: standard.meal,
      trafficSelected: true, trafficStandard: standard.traffic, trafficAmount: standard.traffic,
      commSelected: true, commStandard: standard.comm, commAmount: standard.comm
    })
    current.setDate(current.getDate() + 1)
  }

  const applyAmount = days * (standard.meal + standard.traffic + standard.comm)

  formData.subsidies[index] = {
    employeeId: itinerary.employeeId,
    startDate: itinerary.startDate,
    endDate: itinerary.endDate,
    startCity: itinerary.startCity,
    endCity: itinerary.endCity,
    days,
    applyAmount,
    subsidyAmount: applyAmount,
    calendar
  }
}
```

#### 4.2.5 补助日历弹窗：合计、全选/列选/单元选

补助金额合计（仅累加被选中的类型）：
```js
const currentSubsidyAmount = computed(() => {
  if (!currentSubsidy.value) return 0
  return currentSubsidy.value.calendar.reduce((sum, row) => {
    return sum
      + (row.mealSelected ? row.mealAmount : 0)
      + (row.trafficSelected ? row.trafficAmount : 0)
      + (row.commSelected ? row.commAmount : 0)
  }, 0)
})
```

标准总额（与勾选状态无关，始终合计每日标准）：
```js
const currentSubsidyStandardAmount = computed(() => {
  if (!currentSubsidy.value) return 0
  return currentSubsidy.value.calendar.reduce((sum, row) => {
    return sum + (row.mealStandard || 0) + (row.trafficStandard || 0) + (row.commStandard || 0)
  }, 0)
})
```

全选（选中/取消选中仅改变 selected 标记；金额输入是否参与合计由 mealSelected/trafficSelected/commSelected 决定）：
```js
const handleCalendarSelectAll = (val) => {
  if (isReadOnly.value) return
  colSelect.meal = val
  colSelect.traffic = val
  colSelect.comm = val
  currentSubsidy.value.calendar.forEach(row => {
    row.selected = val
    row.mealSelected = val
    row.trafficSelected = val
    row.commSelected = val
    if (val) {
      row.mealAmount = row.mealStandard
      row.trafficAmount = row.trafficStandard
      row.commAmount = row.commStandard
    }
  })
}
```

列选择与行选择逻辑会更新“全选/列选状态”，保证头部 checkbox 与明细一致：
```js
const updateCalendarSelectionState = () => {
  if (!currentSubsidy.value) return
  calendarSelectAll.value = currentSubsidy.value.calendar.every(r => r.mealSelected && r.trafficSelected && r.commSelected)
  colSelect.meal = currentSubsidy.value.calendar.every(r => r.mealSelected)
  colSelect.traffic = currentSubsidy.value.calendar.every(r => r.trafficSelected)
  colSelect.comm = currentSubsidy.value.calendar.every(r => r.commSelected)
}
```

确认保存回写到主表 subsidies（同时触发分摊重算）：
```js
const saveSubsidy = () => {
  if (isReadOnly.value) return
  currentSubsidy.value.subsidyAmount = currentSubsidyAmount.value
  currentSubsidy.value.applyAmount = currentSubsidyStandardAmount.value
  formData.subsidies[currentSubsidyIndex.value] = JSON.parse(JSON.stringify(currentSubsidy.value))
  recalculateApportionment()
  subsidyVisible.value = false
}
```

#### 4.2.6 分摊：金额联动与校验

补助总额变化触发重算：
```js
watch(totalSubsidy, () => {
  recalculateApportionment()
})
```

分摊比例联动（第 0 行兜底，保证总和=100；并按比例计算金额）：
```js
const recalculateApportionmentPercent = () => {
  if (formData.apportionments.length === 0) return
  if (formData.apportionments.length === 1) {
    formData.apportionments[0].percent = 100
    formData.apportionments[0].amount = totalSubsidy.value
    return
  }

  let pSum = 0
  for (let i = 1; i < formData.apportionments.length; i++) {
    pSum += formData.apportionments[i].percent
  }

  if (pSum > 100) {
    formData.apportionments.forEach((app, idx) => {
      if (idx > 0) app.percent = 0
    })
    pSum = 0
  }

  formData.apportionments[0].percent = Number((100 - pSum).toFixed(2))

  let aSum = 0
  for (let i = 1; i < formData.apportionments.length; i++) {
    const amount = Number((totalSubsidy.value * formData.apportionments[i].percent / 100).toFixed(2))
    formData.apportionments[i].amount = amount
    aSum += amount
  }
  formData.apportionments[0].amount = Number((totalSubsidy.value - aSum).toFixed(2))
}
```

均分（除第 0 行外平均分配，0 行补差）：
```js
const evenApportion = () => {
  if (isReadOnly.value) return
  const count = formData.apportionments.length
  if (count === 0) return
  const avgPercent = Math.floor(10000 / count) / 100
  const avgAmount = Math.floor(totalSubsidy.value * 100 / count) / 100

  let pSum = 0
  let aSum = 0
  for (let i = 1; i < count; i++) {
    formData.apportionments[i].percent = avgPercent
    formData.apportionments[i].amount = avgAmount
    pSum += avgPercent
    aSum += avgAmount
  }
  formData.apportionments[0].percent = Number((100 - pSum).toFixed(2))
  formData.apportionments[0].amount = Number((totalSubsidy.value - aSum).toFixed(2))
}
```

#### 4.2.7 提交/草稿：前端校验与 payload 组装

提交入口：先校验表单；无行程/分摊不完整/合计不匹配时提示保存草稿；否则提交 status=1。

```js
const handleSubmit = () => {
  if (isReadOnly.value) return
  formRef.value.validate((valid) => {
    if (valid) {
      if (formData.itineraries.length === 0) {
        return ElMessageBox.confirm('您还未添加补录行程，是否要先保存为草稿？', '提示', {
          confirmButtonText: '保存草稿',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => doSubmit(0))
      }

      const appValid = formData.apportionments.every(a => a.companyId)
      if (!appValid) {
        return ElMessageBox.confirm('费用归属公司有未填项，是否要先保存为草稿？', '提示', {
          confirmButtonText: '保存草稿',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => doSubmit(0))
      }

      const pSum = formData.apportionments.reduce((sum, item) => sum + item.percent, 0)
      if (Math.abs(pSum - 100) > 0.01) {
        return ElMessageBox.confirm('分摊比例合计不为100%，是否要先保存为草稿？', '提示', {
          confirmButtonText: '保存草稿',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => doSubmit(0))
      }

      const aSum = formData.apportionments.reduce((sum, item) => sum + item.amount, 0)
      if (Math.abs(aSum - totalSubsidy.value) > 0.01) {
        return ElMessageBox.confirm('分摊金额合计不等于补助总额，是否要先保存为草稿？', '提示', {
          confirmButtonText: '保存草稿',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => doSubmit(0))
      }

      doSubmit(1)
    } else {
      ElMessageBox.confirm('表单有必填项未填完，是否要先保存为草稿？', '提示', {
        confirmButtonText: '保存草稿',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => doSubmit(0))
    }
  })
}
```

payload 组装：把前端“别名字段”同步回后端字段，并补齐合计字段：
```js
const doSubmit = (status) => {
  if (isReadOnly.value) return
  formData.status = status
  formData.subsidyTotal = String(totalSubsidy.value.toFixed(2))
  formData.mealAllowance = String(totalMeal.value.toFixed(2))
  formData.transportationAllowance = String(totalTraffic.value.toFixed(2))
  formData.phoneAllowance = String(totalComm.value.toFixed(2))

  formData.reimbursementTitle = formData.title
  formData.reimburserId = formData.employeeId
  formData.reimDepartmentId = formData.departmentId
  formData.reimCompanyId = formData.companyId
  formData.businessTripReason = formData.reason
  formData.creationTime = formData.createTime || formData.creationTime

  const payload = JSON.parse(JSON.stringify(formData))
  const request = formData.id ? updateReimbursement(payload) : addReimbursement(payload)

  request.then(() => {
    ElMessage.success(status === 0 ? '保存草稿成功' : '提交成功')
    router.push('/')
  }).catch(err => console.error(err))
}
```

#### 4.2.8 关闭按钮：提示是否保存草稿（可编辑态）

```js
const handleClose = () => {
  if (isReadOnly.value) return
  ElMessageBox.confirm('是否保存为草稿？', '提示', {
    confirmButtonText: '保存草稿',
    cancelButtonText: '不保存',
    type: 'warning',
    distinguishCancelAndClose: true
  })
    .then(() => {
      doSubmit(0)
    })
    .catch((action) => {
      if (action === 'cancel') router.back()
    })
}
```

---

## 5. 部署与运行

### 5.1 本地开发

前端（开发代理到 8080）：
```bash
npm install
npm run dev
```

后端：
```bash
cd serve
mvn spring-boot:run
```

### 5.2 前端生产构建（非 Docker）

```bash
npm run build
```

产物输出到 `dist/`。

### 5.3 前端 Docker 部署（推荐给同网段访问）

#### 5.3.1 Dockerfile（项目根目录 Dockerfile）

```dockerfile
FROM dockerproxy.com/library/node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM dockerproxy.com/library/nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
```

#### 5.3.2 Nginx 配置（项目根目录 nginx.conf）

说明：
- `try_files ... /index.html` 支持 SPA 路由刷新
- `/api/` 反向代理到宿主机后端（host.docker.internal:8080）

```nginx
server {
  listen 80;
  server_name _;

  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }

  location /api/ {
    proxy_pass http://host.docker.internal:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }
}
```

#### 5.3.3 构建与启动命令（示例对外端口 8081）

```bash
docker build -t vetech-web:latest .
docker rm -f vetech-web
docker run -d --name vetech-web -p 8081:80 vetech-web:latest
```

#### 5.3.4 校园网访问要点
- 访问地址：`http://<服务器电脑IPv4>:8081/`
- 需要服务器电脑的 Windows 防火墙放行入站端口（例如 8081）
- 若同一校园网仍无法访问，通常是网络策略对“终端入站”做了限制，需要换可入站的网络/设备或使用内网组网方案

---

## 6. 已知限制与改进方向（基于当前实现）

- 后端接口返回值未统一封装（boolean / entity 混用），缺少统一错误码与 message
- 列表分页与搜索在前端本地完成：数据量大时应改为后端分页与条件查询
- ReimbursementSubsidy 中 `mealAmount/trafficAmount/commAmount` 为 String，数据库主表金额字段为 decimal：类型一致性可优化
- 缺少权限鉴权、审计日志与敏感字段脱敏策略（建议按上线要求补齐）

