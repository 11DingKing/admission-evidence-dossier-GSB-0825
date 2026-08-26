# 投档来源卷宗系统（Admission Evidence Dossier）

围绕**一条投档事实**建立卷宗，把最多 **5 个独立来源**的投档最高分记录并排核验，并按 `as_of` **封存不可变卷宗**，解决“公开数据只剩一个被转述的数字、找不到原始来源/修订记录/分数口径”的问题。

- 后端：Java 21 · Spring Boot 3.5 · PostgreSQL 16 · Flyway · Gradle Wrapper（`backend/`）
- 前端：React 19 · TypeScript · Vite · Vitest · pnpm 单仓库（`apps/web`）
- 不要求 Docker；API 与 Web 用环境变量分别启动。

## 一、核心概念与一致性判定规则

**fact_key（一条事实）**：`province_code + admission_year + subject_category + school_code + major_group_code`。
每份卷宗（`dossier_id`）只对应一个 fact_key；前端内置两个固定 seed 卷宗：

| 卷宗 | fact_key | 期望量表 |
|---|---|---|
| 深圳职业技术大学（`11111111-…`） | 44 / 2025 / PHYSICS 普通物理类 / 11113 / 专业组 203 | GAOKAO_750_TENTH（750 分制，0.1 精度） |
| 武汉职业技术大学（`22222222-…`） | 42 / 2025 / ART_COMPOSITE 艺术综合 / 10834 / 专业组 Y01 | ART_COMPOSITE_TENTH（艺术综合分，0.1 精度） |

**分数口径**
- 分数一律以**最小 0.1 分的整数** `score_tenths` 保存（609.0 分 → `6090`），展示值 `score_display` = tenths/10 一位小数。
- 每条记录保留**原始量表** `score_scale`；来源量表与 fact_key 期望量表不一致时，卷宗标记 `SCALE_CONFLICT`，**系统不做任何换算**。

**revision（来源修订）规则**
- 同一来源只接受**更大的** `source_revision` 作为当前值。
- 迟到的旧修订**仍写入证据链**（`superseded=true`），但永远不能抢回当前值。
- 某时刻的当前值：在 `recorded_at ≤ as_of` 且 `effective_from ≤ as_of（UTC 日期）` 的记录中取 `source_revision` 最大者；因此 `as_of` 可以重放历史。

**卷宗状态判定（优先级从高到低）**
1. `SCALE_CONFLICT`：任一来源当前值的量表 ≠ fact_key 期望量表；
2. `CONFLICT`：≥2 个来源有当前值且分数不一致；
3. `CONSISTENT`：≥2 个来源当前值分数一致且量表一致；
4. `SINGLE_SOURCE`：只有 1 个来源有当前值；
5. `NO_EVIDENCE`：尚无任何来源记录。

**freshness（有效期）**：相对 `as_of`（UTC 日期）——`effective_from` 未到 → `UPCOMING`；`effective_to` 已过 → `EXPIRED`；否则 `FRESH`。

**封存（seal）**
- `POST /dossiers/{id}/seals {asOf}` 把该时刻的视图（状态、各来源当前值、证据链）序列化为 jsonb 快照并算内容哈希；快照**只增不改**，没有更新/删除接口。
- 同一 `(dossier, as_of)` 重复封存返回 **409 SEAL_CONFLICT**。
- `hasNewerEvidence` 实时计算：封存后若出现 `recorded_at > as_of` 的新证据，列表/快照显示“之后有新证据”（前端提示**过期卷宗**），但快照内容与哈希永不变。

**抓取与部分失败**
- `POST /dossiers/{id}/sources/fetch` 逐来源抓取，HTTP 始终 **200**，`results` 逐项给出：
  - `LOADED`：新记录成为当前值；`SUPERSEDED`：该修订已在链中/为旧修订；
  - `ERROR`：该来源失败（`FACT_KEY_MISMATCH` fact_key 不匹配且**不落库**、`SOURCE_UNAVAILABLE`、`NO_PUBLICATION_AS_OF`），错误逐项返回，其他来源正常入库。
- API 返回每个来源的值、口径、有效期、`freshness` 与内容哈希（`sha256:` 前缀）；前端保留已加载的证据链，只把失败来源标为“缺口”。

**固定 seed 剧本**（V2 迁移写入）
- 来源 A `GD_EDU_EXAM` 广东省教育考试院：rev1 = 610.0（粤教考函〔2025〕37号，2025-07-19）→ rev2 更正为 **609.0**（粤教考函〔2025〕41号，2025-07-25）。
- 来源 B `SZPU_ADMISSION` 深圳职业技术大学本科招生网：609.0（深职大招〔2025〕18号）。
- 来源 C `CHSI_GGKG` 阳光高考信息平台：只有**武汉** fact_key 的 604.4 艺术综合分——抓取深圳卷宗时返回 `FACT_KEY_MISMATCH`，**不能挂到深圳卷宗**。
- 来源 D `WHPU_ADMISSION` 武汉职业技术大学招生网：武汉 fact 的 604.4。
- 操作：深圳卷宗以 `as_of=2025-07-20T12:00:00Z` 核验 → `CONFLICT`（610.0 vs 609.0）；以 `as_of=2025-07-26` 再核验 → `CONSISTENT`（rev1 留在证据链并标记已被取代）；武汉卷宗核验 → `CONSISTENT`。

## 二、环境要求

- JDK 21（Gradle toolchain 会自动探测/按需下载）
- Node.js ≥ 20.19、pnpm ≥ 10（仓库已含 `pnpm-lock.yaml`）
- PostgreSQL 16（本机服务即可，无需 Docker）

## 三、完整运行方法

```bash
# 0. 准备数据库（首次）
createdb admission_dossier

# 1. 启动后端（Flyway 首次启动自动建表 + 写入固定 seed）
cd backend
./gradlew bootJar
DB_URL=jdbc:postgresql://localhost:5432/admission_dossier \
DB_USERNAME=postgres DB_PASSWORD=postgres \
API_PORT=8080 \
java -jar build/libs/admission-dossier-backend-0.0.1-SNAPSHOT.jar
#   开发时也可用：./gradlew bootRun（同样读取上述环境变量）

# 2. 启动前端（另一个终端，仓库根目录）
pnpm install
VITE_API_BASE_URL=http://localhost:8080/api/v1 pnpm dev
#   打开 http://localhost:5173 ；生产构建：pnpm build 后 pnpm preview
#   （同源/反向代理部署时 VITE_API_BASE_URL 可留空，默认 /api/v1）
```

环境变量样例见 [.env.example](.env.example)。后端 Swagger UI：<http://localhost:8080/swagger-ui.html>，OpenAPI JSON：<http://localhost:8080/v3/api-docs>；静态契约见 [openapi/openapi.yaml](openapi/openapi.yaml)。

## 四、验收命令

```bash
# 后端（在 backend/ 下）
./gradlew test       # 21 个 JUnit 测试
./gradlew bootJar    # 产物 build/libs/admission-dossier-backend-0.0.1-SNAPSHOT.jar

# 前端（在仓库根目录）
pnpm install --frozen-lockfile
pnpm test            # Vitest：键盘核验/证据抽屉/部分失败保留/状态切换/360px/过期卷宗
pnpm build           # tsc --noEmit 严格类型检查 + Vite 构建
```

后端测试覆盖：revision 乱序（迟到旧修订不抢回当前值）、fact_key 不匹配零落库、量表冲突 `SCALE_CONFLICT` 不换算、`as_of` 重放（CONFLICT→CONSISTENT）、旧卷宗封存不可变 + 重复封存 409、部分来源失败时其他来源与证据链保留。测试为纯 JUnit/Mockito，不依赖数据库。

## 五、前端工作台说明

第一屏即紧凑**卷宗核验工作台**（无营销 Hero、无装饰渐变、无嵌套卡片）：

- **来源队列**：每来源一行（当前值/量表、修订数、有效期、✓已核验 / ✗失败-缺口 / ○未抓取、“原文”按钮），失败但已有旧值时标“已保留”。
- **当前事实**：fact_key 五字段、期望量表、状态徽章、一致分。
- **来源原文抽屉**：`role="dialog"`，含文号、内容哈希、有效期、原文 `<pre>`、修订列表与版本差异（Δ 分差）；Esc 关闭，焦点自动进入/归还。
- **封存**：“封存卷宗”按当前 `as_of` 封存；封存记录可点选只读查看，之后有新证据时显示“过期卷宗”横幅。
- 全键盘可操作（Tab/Enter/Esc，原生控件 + 可见焦点轮廓）；<720px 单列布局，360px 宽度可用。
- 四种状态：加载中骨架（`aria-busy`）、空（尚未抓取任何来源）、部分失败（`role="alert"`，证据链不清空）、过期卷宗（`role="status"` 横幅 + 只读快照）。

## 六、目录结构

```
openapi/openapi.yaml          # API 静态契约（运行时 /v3/api-docs、/swagger-ui.html）
backend/                      # Spring Boot 3.5 + Gradle Wrapper
  src/main/resources/db/migration/V1__schema.sql   # 表结构
  src/main/resources/db/migration/V2__seed.sql     # 固定 seed（4 来源 / 2 卷宗 / 5 公告）
  src/main/java/com/gsb/admission/dossier/         # domain / model / repo / service / web
  src/test/java/…                                  # 21 个单元测试
apps/web/                     # React 19 + Vite + Vitest
  src/api/                    # 类型（镜像 OpenAPI）与 fetch 客户端（读 VITE_API_BASE_URL）
  src/components/             # 工作台、来源队列、当前事实、原文抽屉、版本差异、状态组件
  src/hooks/                  # useWorkbench（状态编排）、useViewport（<720px 单列）
```
