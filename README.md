# Keycloak自定义认证模块 (Custom Authenticator)
## 📖项目简介
本项目custom-auth是一个基于Keycloak平台开发的自定义认证器。核心是Keycloak提供的标准身份验证流程增加一层额外的安全校验机制，即基于移动终端设备属性的凭证认证，满足跨主体数据鉴权系统的安全要求。本模块在Keycloak认证流中充当协调者，将认证决策转交给外部的Credential-Server服务。
### 核心认证流程
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
## 🚀自定义认证模块的部署&配置
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
kc.bat build //识别新加入的Provider
kc.bat start-dev --hostname=localhost //启动服务器
```
**部署验证:**  
JAR 包安装成功后，会被 Keycloak 服务器自动加载。此时访问后台
```bash
http://localhost:8080/admin
```
即为配置好的领域（登录账号：admin，密码：admin）。
