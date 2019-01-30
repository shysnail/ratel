这是一个api网关
基于 eclipse vertx(base on netty) 开发的一款轻量级网关。

[使用手册](http://www.baidu.com)

##组件介绍

#### 反向代理

#### 请求转发

#### 黑名单

#### 限流

#### 租户认证

#### 可扩展前置/后置处理


# 部署
### way 1.构建部署
 > JDK: 1.8 + (如使用openjdk 请自行匹配版本，暂时未评测) <br/>
 > build: maven <br/>
```html
mvn clean package
cd gateway/target/ratel
修改conf目录ratel.yml配置
使用 sh bin/start.sh 启动
```
### way 2.下载发布包
```html
wget https://github.
tar xvf 
cd ratel-xxx
修改conf目录ratel.yml配置
使用 sh bin/start.sh 启动
```

## 目录说明
#### module
    整个项目由两个模块构成：
    gateway --网关主要功能实现模块
    share   --基础依赖模块，封装了基础类，如需要扩展某些功能如实现一个特定的前置或者后置处理器，可以依赖此模块开发。
    
