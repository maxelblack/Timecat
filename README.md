# Timecat

一个基于无障碍服务的 Android 应用使用时间控制，使用 Compose for Android 构建。

目前处于初期开发阶段，已经基本完成 UI 部分。

## 使用

### 直接安装

直接下载最新的 [Release](https://github.com/maxelblack/Timecat/releases/latest) 安装即可，后续功能完善后可能会上架各大应用商店。

上架 Google Play 是不可能的，穷逼开发者用不起 25 美金的 Play 开发者账户。本项目完全开源，若有兴趣可以在遵循 [Apache License v2.0](https://github.com/maxelblack/Timecat/blob/main/LICENSE) 并注明来源（本仓库地址）的情况下随意发布到任何地方（参考下面的[手动构建](#手动构建)）。

### 手动构建

首先你需要一个 JDK 环境，这个 JVM 系开发者应该很熟悉了。若不熟悉的话这里推荐用 [Temurin 11](https://adoptium.net/temurin/releases?version=11) ，安装时记得选上配置环境变量。

克隆本仓库到本地：

```shell
git clone https://github.com/maxelblack/Timecat.git
```

创建一个 Keystore 用于给 APK 签名，各大搜索引擎能找到很多教程（为此可能需要下载一个 Android Studio），这里不再赘述。

然后将 Keystore 文件重命名为 `key.jks` 并放在 `keystore` 目录中，同时新建一个 `keystore.properties` ，内容如下：

```properties
# Keystore 的密码
storePassword = ******
# 用于签名的 Key 的密码
keyPassword = ******
# 用于签名的 Key 的名字
keyAlias = Key for Timecat
# Keystore 文件路径，一般不需更改
storeFile = ../keystore/key.jks
```

保存文件后打开 `version.properties` 并更改 `versionName` 为你想要的版本名（这个版本名会显示在 Timecat 的应用信息中），如果不清楚后果请不要随意更改 `versionCode` 。

最后切换到项目目录开始构建（中国大陆建议使用全局代理）：

```shell
./gradlew assembleRelease
```

构建跑完之后，在 `项目目录/app/build/outputs/apk/release` 可以找到安装包，名为 `app-release.apk` 。

## 贡献代码

Fork 这个仓库修改后提交 Pull request 即可，请尽可能遵循项目的文件布局规范。

若需本地运行请使用 debug 构建，没有配置 Keystore 时 release 构建会报错。

## 参与翻译

步骤基本同贡献代码：

- Fork 这个仓库
- 在 `项目目录/app/src/main/res/values-语言代码` 目录中创建 `strings.xml` 这个文件
- 将 `项目目录/app/src/main/res/values/strings.xml` 的内容复制过去并删除 `app_name` 所在行（即第二行）
- 翻译 XML 标签之间的文本内容
- 提交 Pull request 。