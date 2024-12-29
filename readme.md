# B站视频下载器 (Bilibili Video Downloader)

一个基于JavaFX开发的B站视频下载工具,支持多种清晰度下载和音视频分离下载。主要参考了 https://github.com/yutto-dev/yutto

## 功能特点

<img src="https://github.com/user-attachments/assets/99b221ef-1e37-4006-97f2-2858af71337a" width="300">

- 支持BV号和完整链接解析
- 支持多种视频清晰度选择
- 支持SESSDATA配置,可下载高清视频
- 实时显示下载进度
- 自动合并音视频
- 详细的日志输出

## 系统要求

- Windows 7/8/10/11
- Java 17 或更高版本 ([下载地址](https://adoptium.net/))

## 安装说明

1. 下载release发布包并解压
2. 确保已安装Java 17
3. 运行 BilibiliDownloader.exe

注: release发布包中包含使用说明(readme.txt)

## 源码目录结构

```
BilibiliDownloader/
├── src/                           # 源代码目录
│   └── main/
│       ├── java/                 # Java源代码
│       │   ├── module-info.java  # 模块定义文件
│       │   └── com/bilibili/
│       │       ├── api/         # B站API相关代码
│       │       │   └── BiliAPI.java        # B站API接口封装
│       │       ├── gui/         # 图形界面相关代码
│       │       │   └── MainController.java  # 主窗口控制器
│       │       ├── model/       # 数据模型
│       │       │   ├── VideoInfo.java      # 视频信息模型
│       │       │   ├── AudioStream.java    # 音频流模型
│       │       │   └── VideoStream.java    # 视频流模型
│       │       ├── util/        # 工具类
│       │       │   ├── Config.java         # 配置管理
│       │       │   ├── Downloader.java     # 下载器实现
│       │       │   └── MediaMerger.java    # 音视频合并工具
│       │       └── Main.java    # 程序入口类
│       └── resources/           # 资源文件目录
│           └── com/bilibili/gui/
│               ├── MainWindow.fxml  # 主窗口布局
│               ├── styles.css       # 界面样式
│               └── favicon.png      # 应用图标
└── README.md                    # 项目说明文档
```



## 技术栈

- Java 17
- JavaFX 17
- Maven
- OkHttp
- mp4parser
- Launch4j

## 开发环境搭建

1. 开发环境要求
   - JDK 17 ([下载地址](https://adoptium.net/))
   - IntelliJ IDEA ([下载地址](https://www.jetbrains.com/idea/))
   - Maven 3.x

2. 克隆项目
```bash
git https://github.com/judydark13/Bilibili-Video-Downloader.git
```

3. IDEA配置
   - 打开IDEA，选择 `File -> Open`，选择项目目录
   - 等待IDEA自动导入Maven项目
   - 确保Project Structure (`File -> Project Structure`) 中：
     - Project SDK 设置为 JDK 17
     - Language Level 设置为 17
   - 确保Maven设置正确，等待依赖下载完成

4. 运行项目
   - 在IDEA中找到 `src/main/java/com/bilibili/Main.java`
   - 右键选择 `Run 'Main'` 即可运行项目

## 主要类说明

- `Main.java`: 程序入口
- `MainController.java`: 主界面控制器
- `BiliAPI.java`: B站API接口封装
- `Downloader.java`: 下载功能实现
- `MediaMerger.java`: 音视频合并实现



## 开源协议

MIT License

