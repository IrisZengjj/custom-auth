# Keycloak自定义认证模块 (Custom Authenticator)
## 📖项目简介
本项目custom-auth是一个基于Keycloak平台开发的自定义认证器。核心是Keycloak提供的标准身份验证流程增加一层额外的安全校验机制，即基于移动终端设备属性的凭证认证，满足跨主体数据鉴权系统的安全要求。本模块在Keycloak认证流中充当协调者，将认证决策转交给外部的Credential-Server服务。
#### 核心认证流程
**触发**: Keycloak 认证流在运行时调用本模块。  
**数据交互**: 本模块负责与外部Credential-Server通信，获取比对结果。  
**决策**: 若设备凭证比对成功，用户直接通过设备认证；若失败，认证流程将导向传统的账号密码登录界面。
## 🏗️环境依赖与Keycloak实例构建
### 环境依赖
本项目依赖于 Keycloak 24.0.5 版本的源码进行编译和部署。需从 https://github.com/keycloak/keycloak/releases/tag/24.0.5 下载 Source code (zip)。本地构建实例生成 keycloak-999.0.0-SNAPSHOT。推荐使用 JDK 17 或更高版本。
### 开发实例构建步骤
要运行本项目，要先完成 Keycloak 服务器的构建和启动：  
· **源码构建**: 下载 Keycloak 24.0.5 源码，并在本地完成构建，将生成 keycloak-999.0.0-SNAPSHOT 版本的服务器发行包。  
· **启动服务器**: 进入本地构建的 Keycloak 服务器 bin 目录（例如：D:\keycloak\keycloak-main\keycloak-999.0.0-SNAPSHOT\bin），执行以下命令，即可启动开发模式服务器：
```bash
kc.bat start-dev --hostname=localhost
```
## 🎨自定义认证模块的部署&配置
### 第一阶段：自定义模块编译与部署
**编译模块:**   
导航至认证模块custom-authenticator根目录，执行 Maven 编译和打包命令，生成 JAR 包：
```bash
mvn clean install
mvn clean compile
mvn clean package
```
生成的jar包将存储在custom-authenticator\target目录下。  
将jar包复制到keycloak-999.0.0-SNAPSHOT\providers\文件夹下之后，在keycloak-999.0.0-SNAPSHOT\bin目录下运行：
```bash
kc.bat build  //识别新加入的Provider
kc.bat start-dev --hostname=localhost  //启动服务器
```
**部署验证:**  
JAR 包安装成功后，会被 Keycloak 服务器自动加载。此时可以访问后台（登录账号：admin，密码：admin）
```bash
http://localhost:8080/admin
```
### 第二阶段：Keycloak 管理控制台配置
在 Keycloak 服务器启动后，如果是**初次构建**，则需要完成以下自定义配置：  
**配置领域(Realm)**: 创建或选择目标领域。  
**创建新的认证流（Authentication Flow）**: 导航至 Authentication -> Flows 选项卡，创建一个新流（例如命名为 My Authentication Flow）。  
**添加自定义执行器（Authenticator）**: 在新流中，添加认证执行器（例如命名为 My Custom Authenticator）。  
**绑定认证流**: 将 Browser Flow（或相应的客户端认证流程）设置为刚创建的 My Authentication Flow。  

如果**已创建过**自定义领域，则执行以下操作导入领域：  
· 在左侧导航栏，点击 Add realm。  
· 选择 Import，上传 realm-export.json 文件（在config文件夹下）。  
这会自动创建包含自定义认证流和所需客户端的领域。

### 第三阶段：界面显示与自定义主题
自定义主题文件位于仓库的 /themes/mycustomtheme 目录下。在完成 Keycloak 认证模块的部署后，需要执行以下操作以应用自定义界面：  
· **复制主题文件：** 将项目 Git 仓库中的 /themes/ 目录完整复制到本地 Keycloak 服务器的安装目录下，确保存在 keycloak-999.0.0-SNAPSHOT\themes\mycustomtheme 文件夹。  
· **在控制台启用：**
登录 Keycloak 管理界面。  
导航至 Realm Settings -> Themes 选项卡。  
在 Login Theme（登录主题）下拉菜单中，确保选择 mycustomtheme。
