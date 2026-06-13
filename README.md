<div align="center">
    <img src="./folia.png" alt="Crelia" width="720">
    <h1>Crelia</h1>
    <p>在 Folia 区域多线程架构上整合 NeoForge 服务端能力的 Minecraft 服务端实验项目。</p>
</div>

> [!WARNING]
> 没做完！他现在有无数问题！你不会想用它正式开服的！

## 项目简介

Crelia 基于 [Folia](https://github.com/PaperMC/Folia) 开发。

初衷是为了使机械动力服务器利用folia的多核心方案以缓解卡顿。
## 当前状态

- 以folia作为基础叠加neoforge功能，理论上性能十分接近folia。理论上！
- 适配中，目前大多模组都会与其不兼容

### 12345678

- folia怎么来他就怎么来
- 构建出来是一个服务端核心jar

##不好意思，这里存储的有一点问题：本仓库目前只包含 NeoForge 模块(neoforge/),尚未包含"把 NeoForge 钩子注入 Minecraft 原版类"的那部分源码补丁——该缝合目前仍在本地源码树中,未固化为补丁文件(它位于被 gitignore 的 folia-server/src/minecraft)。
因此,直接从本仓库 applyPatches 构建出的不是完整的缝合服务端(NeoForge 框架在、但与原版的钩子未接上)。该缝合将在后续通过 rebuildPatches 固化为 minecraft-patches/ 补丁后补全。

## 上游项目

- [PaperMC/Folia](https://github.com/PaperMC/Folia)
- [PaperMC/Paper](https://github.com/PaperMC/Paper)
- [NeoForged/NeoForge](https://github.com/neoforged/NeoForge)

## 许可证

本项目基于多个上游开源项目开发。不同目录中的代码可能适用不同许可证和版权声明。

Folia 和 Paper 相关补丁请参阅 [`PATCHES-LICENSE`](./PATCHES-LICENSE)；NeoForge 相关代码请同时保留并遵守对应文件中的上游版权与许可证声明。







我是个新手
