这是一个api网关
基于 vertx(base on netty) 开发的一款轻量级网关。

# 部署

### step 1 环境准备
    os:linux /windows /osx
    数据库：mysql:5.0+ / postgresql
    jdk:1.8+

### step 2 部署
#### way 1.构建部署
 > JDK: 1.8 + (如使用openjdk 请自行匹配版本，暂时未评测) <br/>
 > build: maven <br/>
```html
mvn clean package -Dmaven.test.skip=true
cd gateway/target/ratel
将conf目录下 ratel.sql导入至数据库
修改conf目录ratel.yml配置，包括集群模式，数据库配置等等
使用 sh bin/start.sh 启动
```
#### way 2.下载发布包
```html
wget https://github.com/shysnail/ratel/releases/download/x.x.x/ratel.zip
tar xvf 
cd ratel-xxx
将conf目录ttart.sh 启动
```

#### 扩展
lib目录下有siga不同环境的包，是监控采样cpu，网卡数据的依赖包，放入你的jdk文件夹的bin目录下（或放入你的path环境变量里配置的任何一个地址下）

# 配置说明
配置文件位于 conf目录下，使用yaml编写
```html
mode 运行模式，如设置为 DEV，会开启一些调试设置，如禁用缓存等等
port 控制台使用的端口
domainName 本服务发布的域名

upload 当前版本只提供

cluster 集群选项
    enabled true|false 你懂的
    host 本机的ip，其余节点可访问的本机ip
    port 集群通信端口
    pingInterval 健康检查间隔
    pingRetryInterval 其余节点ping不通时，重试间隔

    zookeeperHosts 目前版本使用zk做集群托管
    rootPath 在zk中的节点路径
```
## 目录说明
#### module
    整个项目由两个模块构成：
    gateway --网关主要功能实现模块
    share   --基础依赖模块，封装了基础类，如需要扩展某些功能如实现一个特定的前置或者后置处理器，可以依赖此模块开发。
    
## 组件介绍

#### 反向代理

#### 请求转发

#### 黑名单

#### 限流

#### 租户认证

#### 可扩展前置/后置处理

## 操作手册

