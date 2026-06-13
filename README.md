<div align=center>
    <img src="./folia.png">
    <br /><br />
    <p><b>Crelia</b> —— 在 <a href="https://github.com/PaperMC/Folia">Folia</a>(区域多线程的 Paper 分支)之上缝合 <a href="https://github.com/neoforged/NeoForge">NeoForge</a> 模组加载能力的 Minecraft 服务端核心。</p>
    <p><b>这是 1.21.1 分支</b> —— 为运行<b>机械动力(Create)</b>等 NeoForge 模组而生,目标是用 Folia 的多线程换取性能。</p>
</div>

---
## 不好意思，我没太多时间所以这份readme是ai写的。但不是胡编乱造的。我想也没人会读到这句话吧。
## 是什么

Crelia = **Folia(性能)+ NeoForge(模组)**。

- **Folia** 把相邻区块编组成相互独立的"区域",每个区域有自己的 tick 线程,在线程池里并行执行——没有传统意义上的"主线程"。对于机械动力这种把大量机器分散在世界各处的玩法,这意味着成倍的性能提升。
- **NeoForge** 提供模组加载(FancyModLoader)、事件总线、注册表、capability 等一整套模组基础设施。

把两者缝在一起,就能让机械动力这类重型模组运行在 Folia 的多线程引擎上。

> **设计原则:这是"缝合"不是"创新"。** 直接复用 Folia / NeoForge 的现成代码;凡是性能机制与模组兼容冲突的地方,**一律保留 Folia,性能优先**。

### 版本

| | |
|---|---|
| Minecraft | 1.21.1 |
| 基底 | Folia(Paper 分支) |
| 模组加载器 | NeoForge 1.21.1 / FancyModLoader 4.0.42 |
| 构建 JDK | **Java 21**(必须) |

### 当前进度

- ✅ 能开服,Folia 区域多线程正常运行
- ✅ 原版玩法可玩,**NeoForge 客户端能进服**
- ✅ 可打成单文件自解压服务端 jar
- 🚧 `mods/` 目前为空,**机械动力实测尚未完成**(重型模组会暴露更多待缝合的钩子)
- 🚧 实体伤害管线仅做了临时保护,完整缝合待续


## 从源码构建 / 参与开发

> ⚠️ **核心概念:本仓库里是"补丁"不是源码。** 真正的 `net/minecraft` 源码受 Mojang 版权保护、不能上传;仓库里存的是补丁(diff),克隆后在你本地用你自己下载的 Minecraft **现场生成**完整源码。这也是 Paper / Folia 官方的做法。

开发是一个环:`补丁 → 生成源码 → 改源码 → 提交改动 → 重新生成补丁 → 提交补丁`。

### 1. 准备环境
- **Java 21**(推荐 Eclipse Temurin 21;工具链锁死 21,用更高版本会失败)
- Git

### 2. 克隆并生成源码
```bash
git clone -b 1.21.1 https://github.com/yuchendingUy/Crelia.git
cd Crelia
./gradlew applyPatches      # = patch.sh / patch.bat
```
`applyPatches` 会下载原版 1.21.1 服务端、反编译、依次打上 Paper + Folia + Crelia 的全部补丁,生成 `Folia-Server/` 与 `Folia-API/` 的真实源码。

> **首次运行较慢(下载+反编译,十几到几十分钟)且需要联网。** 已知两个网络坑:
> - 需从 `hub.spigotmc.org` 拉子模块,该服务器常中途断流——失败就重跑几次。
> - 偶发下载到损坏 jar(报 `zip END header not found`),删掉该文件重跑即可。

### 3. 改代码
用 IDE 打开项目,编辑 `Folia-Server/src/` 下的源码(这才是可编辑的真实源码)。

### 4. 运行与打包
```bash
./gradlew :folia-server:runServerFml          # 开发模式开服(带 FML + NeoForge)
./gradlew :folia-server:creliaStandaloneJar   # 打成单文件发布 jar(输出在仓库上级目录)
```

### 5. 把改动固化成补丁(关键!否则改动不会进 git)
```bash
cd Folia-Server
git add -A && git commit -m "你的改动说明"     # 先在生成的源码仓库里提交
cd ..
./gradlew rebuildPatches                       # = rb.sh / rb.bat,把提交翻译成 patches/server/*.patch
git add patches/ && git commit -m "..." && git push origin 1.21.1
```
**只有 `patches/` 里的补丁文件会进 GitHub**,`Folia-Server/src` 是生成物不上传。多人协作合并的是补丁文件。

---

## 仓库结构

| 目录 | 内容 | 是否上传 |
|---|---|---|
| `patches/` | Folia 补丁 + Crelia 的 NeoForge 缝合补丁(server 0020–0028) | ✅ 源料 |
| `neoforge/` | NeoForge 1.21.1 框架代码(client 包已去,server stub 已补) | ✅ 源料 |
| `build-data/crelia-launcher/` | 自解压单文件启动器源码 | ✅ 源料 |
| `build.gradle.kts` / `settings.gradle.kts` 等 | 构建配置与缝合接线 | ✅ 源料 |
| `Folia-Server/` `Folia-API/` | `applyPatches` 生成的真实源码(含反编译 MC) | ❌ 生成物,gitignore |
| `.gradle/` `build/` `run/` | 缓存 / 编译输出 / 测试世界 | ❌ 生成物,gitignore |

---

## 致谢与许可

Crelia 站在这些项目的肩膀上,所有底层功劳归于它们:
- [Paper](https://github.com/PaperMC/Paper) 与 [Folia](https://github.com/PaperMC/Folia) —— 服务端基底与区域多线程引擎(见 `LICENSE.txt`、`PATCHES-LICENSE`)
- [NeoForge](https://github.com/neoforged/NeoForge) 与 FancyModLoader —— 模组加载基础设施(LGPL-2.1)

本仓库**不包含、也不分发** Minecraft 的任何代码或资源——它们由 `applyPatches` 在你本地用你自己合法获得的 Minecraft 生成。使用本项目即表示你同意 [Minecraft EULA](https://aka.ms/MinecraftEULA)。
