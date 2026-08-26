# 投档来源卷宗系统 (Admission Evidence Dossier)

围绕一条投档事实（`fact_key`）把最多 5 个独立来源放在一起核验，并按 `as_of` 封存不可变卷宗。

- **fact_key** = `province_code` + `admission_year` + `subject_category` + `school_code` + `major_group_code`
- 后端：Java 21、Spring Boot 3.5、PostgreSQL 16、Flyway、Gradle Wrapper
- 前端：React 19、TypeScript、Vite、Vitest、pnpm 单仓库

## 一致性判定规则

每个卷宗对当前**有效来源**（每个 `source_id` 取最大 `source_revision`）进行判定：

| 状态 | 判定条件 |
|------|----------|
| `PENDING` | 没有任何来源 |
| `SCALE_CONFLICT` | 任一来源的 `score_scale` 与卷宗 `expected_scale` 不一致，或来源之间量表不一致。**系统不自行换算分数。** |
| `CONFLICT` | 量表一致，但有效来源的 `score_value` 存在不同值 |
| `CONSISTENT` | 量表一致，且所有有效来源的 `score_value` 相同 |

### Revision 规则

- 同一 `source_id` 的当前值始终是**最大的 `source_revision`**。
- 迟到的旧 revision 会被收入证据链（可在版本差异中查看），但**不能抢回当前值**。
- 唯一约束：`(fact_key, source_id, source_revision)`，重复提交返回 `DUPLICATE_REVISION`。

### 分数存储

- 分数以 **0.1 分为最小单位的整数**保存：`609.0` → `6090`，`604.4` → `6044`。
- 同时保留 `original_score_text`（原始量表上的文本）与 `score_scale`（口径）。

### 卷宗封存

- `POST /api/dossiers/{id}/seal` 传入 `asOf`，系统截取 `recorded_at <= asOf` 的每个来源最大 revision，快照写入 `dossier_entry`。
- 封存后的卷宗**不可变**：不能再次封存，不能追加来源。
- 若封存后有新的来源修订到达，卷宗标记 `expired: true`（前端显示过期横幅，但封存内容不变）。

### 部分失败

- 批量提交（最多 5 条）返回 HTTP `207 Multi-Status`。
- `accepted` 包含成功来源，`errors` 包含逐项错误（`FACT_KEY_MISMATCH`、`DUPLICATE_REVISION` 等）。
- 前端保留已加载的证据链，仅将失败项标为缺口。

## 验证场景

### 深圳职业技术大学 · 普通物理类

fact_key: `44 / 2025 / PHYSICS / 11113 / 001`，期望量表 `PHYSICS_750_T1`

1. 来源 A revision 1 = **6100**（610.0）
2. 来源 B revision 1 = **6090**（609.0）
3. 卷宗状态 → `CONFLICT`
4. 来源 A revision 2 更正为 **6090**（609.0）
5. 卷宗状态 → `CONSISTENT`

### 武汉职业技术大学 · 数字媒体艺术

fact_key: `42 / 2025 / ART / 13796 / 018`，量表 `ART_COMPREHENSIVE_T1`，分数 6044（604.4）。
该来源的 fact_key 与深圳卷宗不同，提交到深圳卷宗时返回 `FACT_KEY_MISMATCH`，**不会挂到深圳卷宗**。

固定 seed 由 `DataSeeder` 在应用启动时自动写入（可通过 `APP_SEED_ENABLED=false` 关闭）。

## 运行方法

### 前置条件

- Java 21
- PostgreSQL 16（创建数据库 `admission_dossier`）
- pnpm 9+
- Node.js 20+

### 1. 启动 PostgreSQL 并创建数据库

```sql
CREATE DATABASE admission_dossier;
```

### 2. 配置环境变量

```bash
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env.local
```

按需修改数据库连接和 API 地址。后端也可直接用环境变量：

```bash
export DB_URL=jdbc:postgresql://localhost:5432/admission_dossier
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

### 3. 启动后端（API + Swagger UI）

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS 示例
./gradlew :backend:bootRun
```

- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

Flyway 会自动执行 `db/migration/V1__init.sql` 建表，`DataSeeder` 自动写入验证场景数据。

### 4. 启动前端（另一个终端）

```bash
cd frontend
pnpm install
pnpm dev
```

打开 http://localhost:5173 ，点击「深圳职业技术大学示例」即可加载 seed 卷宗。

### 5. 前端构建预览

```bash
cd frontend
pnpm build
pnpm preview
```

## 验收命令

在仓库根目录依次执行：

```bash
# 后端测试
./gradlew test

# 后端打包
./gradlew bootJar

# 前端依赖安装（使用 lockfile）
pnpm install --frozen-lockfile

# 前端测试
pnpm --filter frontend test

# 前端构建
pnpm --filter frontend build
```

## 主要 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/dossiers` | 创建卷宗 |
| GET | `/api/dossiers/{id}` | 获取卷宗（含来源/封存条目） |
| GET | `/api/dossiers?...` | 按 fact_key 查询卷宗列表 |
| POST | `/api/dossiers/{id}/seal` | 按 as_of 封存卷宗 |
| POST | `/api/dossiers/{id}/sources/batch` | 向卷宗批量提交来源（最多 5 条） |
| GET | `/api/sources?...` | 查询当前有效来源 |
| GET | `/api/sources/history?...` | 查询全部修订历史 |

每个来源返回：`scoreValue`、`scoreScale`、`effectiveFrom/To`、`freshness`（FRESH/STALE/EXPIRED）、`contentHash`（SHA-256）。

## 前端工作台

第一屏为紧凑卷宗核验工作台，包含：

- **来源队列**：方向键导航，Enter 打开原文抽屉
- **当前事实**：fact_key 明细 + 当前核验结论
- **来源原文抽屉**：Esc 或按钮关闭，展示文号、哈希、口径等
- **版本差异**：按 source_id 分组展示 revision 变更

支持加载、空、部分失败、过期卷宗四种状态；适配 360px 宽度（720px 以下单列堆叠）；无营销 Hero、装饰性渐变或嵌套卡片。

## 项目结构

```
.
├── backend/
│   ├── build.gradle.kts
│   ├── gradlew
│   └── src/main/
│       ├── java/com/dossier/admission/
│       │   ├── domain/          # FactKey, SourceRecord, Dossier, DossierEntry, 枚举
│       │   ├── repository/      # JPA Repository
│       │   ├── service/         # 来源提交、卷宗封存、一致性判定
│       │   ├── web/             # REST 控制器 + DTO
│       │   ├── config/          # CORS、OpenAPI
│       │   └── seed/            # 固定 seed 数据
│       └── resources/db/migration/V1__init.sql
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── App.tsx              # 工作台主界面
│       ├── api.ts               # API 客户端
│       ├── types.ts
│       ├── components/          # SourceQueue, CurrentFact, SourceDrawer, VersionDiff
│       └── test/                # Vitest + Testing Library
├── openapi.yaml
└── pnpm-workspace.yaml
```
