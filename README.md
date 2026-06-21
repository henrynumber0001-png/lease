

https://github.com/user-attachments/assets/d2264cf0-5f26-4567-aeb0-356b744df919



# Lease

项目按后端和前端分层组织：

```text
lease/
├── backend/   # Spring Boot + Maven 多模块后端
└── frontend/  # Vue 前端应用
```

## Backend

后端入口位于 `backend/`，包含以下 Maven 模块：

- `model`: 实体、枚举等领域模型
- `common`: 公共配置、工具、异常、返回结果封装
- `web`: Web 服务聚合模块
- `web/web-admin`: 管理端后端服务
- `web/web-app`: 用户端后端服务

常用命令：

```bash
cd backend
mvn clean verify
```

## Frontend

前端入口位于 `frontend/`：

- `rentHouseAdmin`: 管理端前端
- `rentHouseH5`: 用户端 H5 前端

常用命令：

```bash
cd frontend/rentHouseAdmin
npm install
npm run dev

cd ../rentHouseH5
npm install
npm run dev
```
