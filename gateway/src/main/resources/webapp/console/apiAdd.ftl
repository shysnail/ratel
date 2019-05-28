<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-编辑API</title>
    <#include "./inc.ftl" />
</head>
<body class="fixed-sidebar full-height-layout gray-bg">

<#include "./header.ftl" />

<section class="Hui-article-box" style="overflow-y: scroll">
    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">应用</a>
        <span class="c-999 en">&gt;</span><span class="c-666">添加API</span>
    </nav>

    <form id="mainForm" class="form form-horizontal responsive" action="#" method="post">
        <div class="panel panel-default mt-20">
            <div class="panel-header">基本设置</div>
            <div class="panel-body">
                <div class="row cl">
                    <label class="form-label col-xs-3" for="name">名称:</label>
                    <div class="formControls col-xs-8">
                        <input class="input-text" id="name" name="name" placeholder="为便于管理，请给api命名"/>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3" for="path">path:</label>
                    <div class="formControls col-xs-8">
                        <input class="input-text" id="path" name="path"
                               placeholder="uri，支持正则表达式。如/demo、^demo[a-z]+/image/[a-z0-9]+$等等"/>
                    </div>
                </div>
                <div class="row cl" style="display: none;">
                    <label class="form-label col-xs-3" for="name">vhost:</label>
                    <div class="formControls col-xs-8">
                        <input class="input-text" id="vhost" name="vhost" placeholder="请输入vhost，如 x.test.com，多个以 空格 拼接"
                               value="*" disabled/>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3"></label>
                    <div class="formControls col-xs-8">
                        1.路径配置区分大小写，支持 精确(/user)/模糊匹配(/common/*)、路径捕获、正则表达式 <br/>
                        2. 非全正则路径 必须以'/' 开始，如 /test、/test/* 等 <br/>
                        3. 路径捕获可以使用正则捕获和预定义参数，正则捕获如/(\w+)/(\+), 使用'$参数序号 '来提取，如 /$1_$2.html;
                        预定义参数格式为':变量名' 如 /product/:pid.html 使用 ':变量名'提取 <br/>
                        4.固定便捷用法规则路径以'/*'结尾，转发路径任意位置包含'$1'，则表示需要替换请求路径中'*'的部分到转发路径'$1'位置中去
                    </div>
                </div>

                <div class="row cl">
                    <label class="form-label col-xs-3">允许的请求METHOD:</label>
                    <div class="formControls skin-minimal col-xs-8">
                        <input type="checkbox" id="methodAll"/>&nbsp;所有
                        <input type="checkbox" name="methods" value="GET" checked/>&nbsp;GET
                        <input type="checkbox" name="methods" value="POST" checked/>&nbsp;POST
                        <input type="checkbox" name="methods" value="PUT" checked/>&nbsp;PUT
                        <input type="checkbox" name="methods" value="DELETE" checked/>&nbsp;DELETE
                        <input type="checkbox" name="methods" value="PATCH"/>&nbsp;PATCH
                        <input type="checkbox" name="methods" value="OPTION"/>&nbsp;OPTION
                        <input type="checkbox" name="methods" value="TRACE"/>&nbsp;TRACE
                        <input type="checkbox" name="methods" value="CONNECT"/>&nbsp;CONNECT
                        <input type="checkbox" name="methods" value="OTHER"/>&nbsp;OTHER
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">自动启动:</label>
                    <div class="formControls skin-minimal col-xs-1">
                        <input type="checkbox" id="autoRun"/>
                    </div>
                    <div class="col-xs-7 text-l">
                        如开启自动启动，则所属应用启动时，该api自动运行。否则，需要手动启动<br/>
                        变更此项设定，下次所属应用重启时生效
                    </div>
                </div>

                <div class="e-p-i-normal panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">服务类型设定<b style="float:right;margin-right: 10px">+</b></div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">服务类型:</label>
                            <div class="formControls col-xs-8">
                                <div class="radio-box">
                                    <input type="radio" id="upstreamOption.proxyType-1" name="upstreamOption.proxyType"
                                           value="UPSTREAM" checked>
                                    <label for="upstreamOption.proxyType-1">反响代理请求</label>
                                </div>
                                <div class="check-box">
                                    <input type="radio" id="upstreamOption.proxyType-2" name="upstreamOption.proxyType"
                                           value="REDIRECT">
                                    <label for="upstreamOption.proxyType-2">重定向</label>
                                </div>
                                <div class="check-box">
                                    <input type="radio" id="upstreamOption.proxyType-3" name="upstreamOption.proxyType"
                                           value="ECHO">
                                    <label for="upstreamOption.proxyType-3">直接返回信息</label>
                                </div>
                            </div>
                        </div>

                        <div class="e-p-i-i panel panel-default mt-20 upstreamOption" id="upstreamOption_UPSTREAM">
                            <div class="e-p-i-i-h panel-header cl">转发设置</div>
                            <div class="e-p-i-i-b panel-body">
                                <div class="row cl">
                                    <label class="form-label col-xs-3">api目标服务方法:</label>
                                    <div class="formControls col-xs-8">
                                        <div class="radio-box">
                                            <input type="radio" name="methodForward" value="" checked/>&nbsp;沿用请求方法
                                            <input type="radio" name="methodForward" value="GET"/>&nbsp;GET
                                            <input type="radio" name="methodForward" value="POST"/>&nbsp;POST
                                            <input type="radio" name="methodForward" value="PUT"/>&nbsp;PUT
                                            <input type="radio" name="methodForward" value="DELETE"/>&nbsp;DELETE
                                            <input type="radio" name="methodForward" value="PATCH"/>&nbsp;PATCH
                                            <input type="radio" name="methodForward" value="OPTION"/>&nbsp;OPTION
                                            <input type="radio" name="methodForward" value="TRACE"/>&nbsp;TRACE
                                            <input type="radio" name="methodForward" value="CONNECT"/>&nbsp;CONNECT
                                            <input type="radio" name="methodForward" value="OTHER"/>&nbsp;OTHER
                                        </div>
                                    </div>

                                </div>
                                <div class="row cl">
                                    <label class="form-label col-xs-3">负载策略:</label>
                                    <div class="formControls col-xs-3">
                                        <select name="proxyPolicy" size="1" class="select-box">
                                            <option value="RANDOM">随机</option>
                                            <option value="POLLING_AVAILABLE">权重轮训</option>
                                            <option value="IP_HASH">IP分配</option>
                                            <option value="LEAST_ACTIVE">最小活跃数(暂不可用)</option>
                                        </select>(目标url存在多个时，策略生效)
                                    </div>
                                    <div class="formControls col-xs-5 text-r">

                                    </div>
                                </div>

                                <div class="row cl">
                                    <label class="form-label col-xs-3">目标:</label>
                                    <div class="col-xs-9" id="upstream_targets">
                                        <div class="upstream_target">
                                            <div class="formControls col-xs-6">
                                                <input class="input-text" type="text" name="target.url">
                                            </div>
                                            <div class="formControls col-xs-3">
                                                <span class="f-l">权重：</span>&nbsp;<input class="input-text f-l"
                                                                                         style="width: 50%" type="text"
                                                                                         name="target.weight" value="1"
                                                                                         placeholder="1">
                                            </div>
                                            <div class="formControls col-xs-3" name="upstream_target_ops">
                                                <button class="btn btn-secondary radius f-r" type="button"
                                                        onclick="addUpstream()">增加
                                                </button>
                                            </div>
                                        </div>
                                    </div>

                                </div>

                                <div class="row cl">
                                    <label class="form-label col-xs-3">转发线程池类型:</label>
                                    <div class="formControls col-xs-8">
                                        <div class="radio-box">
                                            <input type="radio" id="upstreamOption.threadType-1"
                                                   name="upstreamOption.threadType" value="APP" checked>
                                            <label for="upstreamOption.threadType-1">整个应用公用线程池</label>
                                        </div>
                                        <div class="radio-box">
                                            <input type="radio" id="upstreamOption.threadType-2"
                                                   name="upstreamOption.threadType" value="API">
                                            <label for="upstreamOption.threadType-2">接口自用线程池</label>
                                        </div>
                                    </div>
                                </div>
                                <div class="row cl" id="upstream_threadpool" style="display: none;">
                                    <label class="form-label col-xs-3">最大转发线程数:</label>
                                    <div class="formControls col-xs-3">
                                        <div id="upstreamOption_maxPoolSize"></div>
                                    </div>
                                    <div class="formControls col-xs-3">
                                        如果这个应用经常应对高并发场景，这个值可以设置稍微大一点。可以理解为同时连接的数量
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="row cl upstreamOption" id="upstreamOption_REDIRECT" style="display: none;">
                            <div class="row cl">
                                <label class="form-label col-xs-3">跳转到:</label>
                                <div class="formControls col-xs-8">
                                    <input type="text" class="input-text" name="redirectOption.url"
                                           placeholder="如参数中带有中文以及特殊字符，需先uriencode"/>
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">附加请求字符串:</label>
                                <div class="formControls col-xs-8">
                                    <input type="checkbox" name="redirectOption.passQueryString"/>
                                </div>
                            </div>
                        </div>
                        <div class="row cl upstreamOption" id="upstreamOption_ECHO" style="display: none;">
                            <div class="row cl">
                                <label class="form-label col-xs-3">返回码:</label>
                                <div class="formControls col-xs-8">
                                    <input type="text" class="input-text" name="echo.code" value="200"
                                           style="width: 15%"/>
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">信息类型:</label>
                                <div class="formControls col-xs-8">
                                    <input type="text" class="input-text" name="echo.contentType"
                                           value="application/json"/>
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">内容:</label>
                                <div class="formControls col-xs-8">
                                    <textarea class="textarea" name="echo.content">{"msg":"这是一个结果"}</textarea>

                                </div>
                            </div>
                        </div>
                    </div>
                </div>


            </div>
        </div>

        <div class="panel panel-default mt-20" id="extend-panel">
            <div class="e-p-h panel-header">扩展设置<b style="float:right;margin-right: 10px">+</b></div>
            <div class="e-p-b panel-body">

                <div class="e-p-i panel panel-default mt-20" id="upstream_panel">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">反向代理服务设置</label>
                        <div class="col-xs-5">
                            <input type="radio" class="item_trigger" name="option.upstream" value="0" checked> 使用应用配置
                            &nbsp;
                            <input type="radio" class="item_trigger" name="option.upstream" value="1"> 自定义
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">超时时长:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="upstreamOption.timeout" style="width: 15%"
                                       placeholder="请求后端服务最大时长" value="10000"/> 毫秒
                            </div>
                        </div>

                        <div class="row cl">
                            <label class="form-label col-xs-3">失败重试:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="upstreamOption.retry" style="width: 15%"
                                       placeholder="失败后重试次数" value="1"/> 次
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">转发url中的请求字符串:</label>
                            <div class="formControls col-xs-5">
                                <div class="check-box">
                                    <input type="checkbox" name="upstreamOption.passQueryString" checked/>
                                </div>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">透传请求体:</label>
                            <div class="formControls col-xs-8">
                                <div class="radio-box">
                                    <input type="radio" id="passBody_passBodyType-1" name="passBody.passBodyType"
                                           value="ALL_PASS" checked>
                                    <label for="passBody_passBodyType-1">透传所有</label>
                                </div>
                                <div class="check-box">
                                    <input type="radio" id="passBody_passBodyType-2" name="passBody.passBodyType"
                                           value="ALL_PASS">
                                    <label for="passBody_passBodyType-2">不透传</label>
                                </div>
                                <div class="check-box">
                                    <input type="radio" id="passBody_passBodyType-3" name="passBody.passBodyType"
                                           value="PASS_BY_METHODS">
                                    <label for="passBody_passBodyType-3">仅透传指定方法</label>
                                </div>
                            </div>
                        </div>
                        <div class="row cl" id="passBody_methods" style="display: none;">
                            <label class="form-label col-xs-3">&nbsp;</label>
                            <div class="formControls skin-minimal col-xs-8" id="passBodyMethodChosePanel">
                                <input type="checkbox" name="passBody.option" value="GET"/>&nbsp;GET
                                <input type="checkbox" name="passBody.option" value="POST" checked/>&nbsp;POST
                                <input type="checkbox" name="passBody.option" value="PUT" checked/>&nbsp;PUT
                                <input type="checkbox" name="passBody.option" value="DELETE"/>&nbsp;DELETE
                                <input type="checkbox" name="passBody.option" value="PATCH"/>&nbsp;PATCH
                                <input type="checkbox" name="passBody.option" value="OPTION"/>&nbsp;OPTION
                                <input type="checkbox" name="passBody.option" value="TRACE"/>&nbsp;TRACE
                                <input type="checkbox" name="passBody.option" value="CONNECT"/>&nbsp;CONNECT
                                <input type="checkbox" name="passBody.option" value="OTHER"/>&nbsp;OTHER
                            </div>
                        </div>
                        <div class="row cl proxy_sets">
                            <label class="form-label col-xs-3">最大内容长度:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxContentLength"
                                       placeholder="-1为不限制" value="-1"/>
                            </div>
                        </div>
                        <div class="row cl proxy_sets">
                            <label class="form-label col-xs-3">url最大请求参数长度:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxInitialLineLength"
                                       placeholder="-1为不限制" value="4096"/>
                            </div>
                        </div>
                        <div class="row cl proxy_sets">
                            <label class="form-label col-xs-3">最大请求头:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxHeaderSize"
                                       placeholder="-1为不限制" value="8192"/>
                            </div>
                        </div>
                        <div class="row cl proxy_sets">
                            <label class="form-label col-xs-3">keepalive保持:</label>
                            <div class="formControls col-xs-3 ">
                                <div class="check-box">
                                    <input type="checkbox" id="upstreamOption_keepAlive" name="upstreamOption.keepAlive" checked/>
                                </div>
                            </div>
                            <div class="formControls col-xs-5">
                                如果使用应用线程池，这个选项无效。
                                如果经常应对瞬间并发，推荐开启；保持时长视瞬间持续时长而定:保持时长>=并发持续时长.
                            </div>
                        </div>
                        <div class="row cl proxy_sets">
                            <label class="form-label col-xs-3">keepalive保持时长(秒):</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" id="upstreamOption_keepAliveTimeout" name="upstreamOption.keepAliveTimeout"
                                       value="60"/>
                            </div>
                        </div>

                        <div class="row cl proxy_sets">
                            <label class="form-label col-xs-3">连接最大闲置时长:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxIdleTimeout"
                                       placeholder="-1为不限制" value="5000"/>
                            </div>
                        </div>
                        <div class="row cl proxy_sets">
                            <label class="form-label col-xs-3">拥堵时等待队列最大大小:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxWaitQueueSize"
                                       placeholder="默认为-1，不限制" value="-1"/>
                            </div>
                        </div>

                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20" id="header_panel">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">请求头设定</label>
                        <div class="col-xs-5">
                            <input type="radio" class="item_trigger" name="option.headers" value="0" checked> 使用应用配置
                            &nbsp;
                            <input type="radio" class="item_trigger" name="option.headers" value="-1"> 禁用 &nbsp;
                            <input type="radio" class="item_trigger" name="option.headers" value="1"> 自定义
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body" id="proxyHeaders">
                        <div class="row cl text-r pr-30">
                            <!--<div class="formControls col-xs-12">-->
                            <button class="btn btn-secondary radius" type="button" onclick="addHeader()">增加</button>
                            <!--</div>-->
                        </div>
                        <div class="row cl header-set" style="display: none">
                            <div class="formControls col-xs-2 text-r">
                                <select name="header.type" size="1" onchange="switchHeader(this)">
                                    <option value="ADD">附加</option>
                                    <option value="REMOVE">移除</option>
                                </select>
                            </div>
                            <div class="formControls col-xs-3">
                                <input type="text" class="input-text" name="header.name" placeholder="header 项的 name"/>
                            </div>
                            <div class="formControls col-xs-1 text-c" name="eqs">
                                =
                            </div>
                            <div class="formControls col-xs-4">
                                <input type="text" class="input-text col-xs-2" name="header.value"
                                       placeholder="对应的值，可使用系统提供的变量值，使用方式 $remoteIp"/>
                            </div>
                            <div class="formControls col-xs-1">
                                <button class="btn btn-warning radius" type="button" onclick="removeHeader(this)">删除
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">静态WEB服务</label>
                        <div class="col-xs-5">
                            <input type="radio" class="item_trigger" name="option.openStaticServer" value="0" checked>
                            使用应用配置 &nbsp;
                            <!--<input type="radio" class="item_trigger" name="option.openStaticServer" value="-1"> 禁用 &nbsp;-->
                            <input type="radio" class="item_trigger" name="option.openStaticServer" value="1"> 自定义
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">静态网页根目录:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="docRoot"
                                       placeholder="相对于网关服务homedir的目录，或者绝对路径" value="wwwroot"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">流控</label>
                        <div class="col-xs-5">
                            <input type="radio" class="item_trigger" name="option.limit" value="0" checked> 使用应用配置
                            &nbsp;
                            <input type="radio" class="item_trigger" name="option.limit" value="-1"> 禁用 &nbsp;
                            <input type="radio" class="item_trigger" name="option.limit" value="1"> 自定义
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">统计周期:</label>
                            <div class="formControls col-xs-8 text-l">
                                <input type="text" class="input-text" name="limit.interval" style="width: 15%"
                                       value="1"/>
                                <select name="limit.timeUnit" size="1">
                                    <option value="SECOND">秒</option>
                                    <option value="MINUTE">分钟</option>
                                    <option value="HOUR">小时</option>
                                </select>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">总数:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="limit.limit" style="width: 15%" value="-1"/>
                                次(小于零即不限制)
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">每IP:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="limit.limitPerIp" value="-1"/> 次(小于零即不限制)
                            </div>
                            <label class="form-label col-xs-2"><input type="checkbox" name="limit.ipFromHeader"/>从header获取ip:</label>
                            <div class="formControls col-xs-5">
                                <input type="text" class="input-text" name="limit.ipHeaderKey"
                                       placeholder="填写请求头中的ip字段"/> 次(小于零即不限制)
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">每用户:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="limit.limitPerClient" style="width: 15%"
                                       value="-1"/> 次(小于零即不限制)
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">对用户做次数控制时，请填写请求头中的用户标识:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="limit.keys"
                                       placeholder="如 uuid，如header中的多项组合才能标记一个用户，用 ',' 拼接即可"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">超出返回</label>
                            <div class="formControls col-xs-8">&nbsp;
                            </div>

                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">返回码:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="limit.overloadedReturn.code" value="403"
                                       style="width: 15%"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">信息类型:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="limit.overloadedReturn.contentType"
                                       value="application/json"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">内容:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="limit.overloadedReturn.content"
                                       value="请求过于频繁"/>
                            </div>
                        </div>

                    </div>
                </div>

                <!--<div class="e-p-i panel panel-default mt-20">-->
                <!--<div class="e-p-i-h panel-header cl">-->
                <!--<label class="col-xs-3 text-l pl-5">熔断策略</label>-->
                <!--<div class="col-xs-5"><input type="radio" name="option.fusing" value="0" checked> 使用应用配置 &nbsp; <input type="radio" name="option.fusing" value="-1"> 禁用 &nbsp;<input type="radio" name="option.fusing" value="1"> 自定义</div>-->
                <!--</div>-->
                <!--<div class="e-p-i-b panel-body">-->
                <!--<div class="row cl">-->
                <!--<label class="form-label col-xs-3">流控:</label>-->
                <!--<div class="formControls col-xs-8">-->
                <!--<input type="text" class="input-text" name="fusing.name" placeholder="权限认证"/>-->
                <!--</div>-->
                <!--</div>-->
                <!--</div>-->
                <!--</div>-->

                <!--<div class="e-p-i panel panel-default mt-20">-->
                <!--<div class="e-p-i-h panel-header cl">-->
                <!--<label class="col-xs-3 text-l pl-5">缓存</label>-->
                <!--<div class="col-xs-5"><input type="radio" name="option.fusing" value="0" checked> 使用应用配置 &nbsp; <input type="radio" name="option.fusing" value="-1"> 禁用 &nbsp;<input type="radio" name="option.fusing" value="1"> 自定义</div>-->
                <!--</div>-->
                <!--<div class="e-p-i-b panel-body">-->
                <!--<div class="row cl">-->
                <!--<label class="form-label col-xs-3">匹配规则:</label>-->
                <!--<div class="formControls col-xs-8">-->
                <!--<input type="text" class="input-text" name="cache.rule" placeholder="路径，支持正则表达式，多个用 ',' 拼接"/>-->
                <!--</div>-->
                <!--</div>-->
                <!--<div class="row cl">-->
                <!--<label class="form-label col-xs-3">生命周期(秒):</label>-->
                <!--<div class="formControls col-xs-8">-->
                <!--<input type="text" class="input-text" name="cache.interval" value="1800" style="width: 15%"/>-->
                <!--</div>-->
                <!--</div>-->
                <!--</div>-->
                <!--</div>-->

                <div class="e-p-i panel panel-default mt-20" style="display: none">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">IP黑名单</label>
                        <div class="col-xs-5">
                            <input type="radio" class="item_trigger" name="option.ipBlacklist" value="0" checked> 使用应用配置
                            &nbsp;
                            <input type="radio" class="item_trigger" name="option.ipBlacklist" value="-1"> 禁用 &nbsp;
                            <input type="radio" class="item_trigger" name="option.ipBlacklist" value="1"> 自定义
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">ip:</label>
                            <div class="formControls col-xs-8">
                                <textarea class="textarea" id="ipBlacklist" name="ipBlacklist" rows="3"
                                          placeholder="ip地址（支持v4 v6），用 英文 ',' 拼接"></textarea>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">租户认证</label>
                        <div class="col-xs-5">
                            <input type="radio" class="item_trigger" name="option.auth" value="0" checked> 使用应用配置 &nbsp;
                            <input type="radio" class="item_trigger" name="option.auth" value="-1"> 禁用 &nbsp;
                            <input type="radio" class="item_trigger" name="option.auth" value="1"> 自定义
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">名称:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="auth.name" value="默认auth2.0认证"
                                       placeholder="默认auth2.0认证"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">实现类:</label>
                            <div class="formControls col-xs-8">
                                <select id="auth_instance" name="auth.instance" class="col-xs-8 input-text">
                                </select>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">用法:</label>
                            <div class="formControls col-xs-8">
                                <p class="textarea" style="overflow-y: scroll" id="auth.usage" placeholder=""></p>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">认证不通过返回</label>
                            <div class="formControls col-xs-8">&nbsp;
                            </div>

                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">返回码:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="auth.failReturn.code" value="401"
                                       style="width: 15%"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">信息类型:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="auth.failReturn.contentType"
                                       value="application/json"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">内容:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="auth.failReturn.content" value="使用的key不正确"/>
                            </div>
                        </div>

                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">前置处理器</label>
                        <div class="col-xs-5">
                            <input type="radio" class="item_trigger" name="option.preHandler" value="0" checked> 使用应用配置
                            &nbsp;
                            <input type="radio" class="item_trigger" name="option.preHandler" value="-1"> 禁用 &nbsp;
                            <input type="radio" class="item_trigger" name="option.preHandler" value="1"> 自定义
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">名称/简介:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="preHandler.name" value="默认前置处理器"
                                       placeholder="默认前置处理器"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">实现类:</label>
                            <div class="formControls col-xs-8">
                                <select id="preHandler_instance" name="preHandler.instance" class="col-xs-8 input-text">
                                    <option value="">暂无可用处理器</option>
                                </select>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">用法:</label>
                            <div class="formControls col-xs-8">
                                <p class="textarea" style="overflow-y: scroll" id="preHandler.usage" placeholder=""></p>
                            </div>
                        </div>

                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">后置处理器</label>
                        <div class="col-xs-5">
                            <input type="radio" class="item_trigger" name="option.postHandler" value="0" checked> 使用应用配置
                            &nbsp;
                            <input type="radio" class="item_trigger" name="option.postHandler" value="-1"> 禁用 &nbsp;
                            <input type="radio" class="item_trigger" name="option.postHandler" value="1"> 自定义
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <div class="row cl">
                                <label class="form-label col-xs-3">名称/简介:</label>
                                <div class="formControls col-xs-8">
                                    <input type="text" class="input-text" name="postHandler.name" value="默认后置处理器"
                                           placeholder="默认后置处理器"/>
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">实现类:</label>
                                <div class="formControls col-xs-8">
                                    <select id="postHandler_instance" name="postHandler.instance"
                                            class="col-xs-8 input-text">
                                        <option value="">暂无可用处理器</option>
                                    </select>
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">用法:</label>
                                <div class="formControls col-xs-8">
                                    <p class="textarea" style="overflow-y: scroll" id="postHandler.usage" placeholder=""></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

        </div>
    </form>

    <div class="row cl mt-30 mb-40 text-c">
        <button class="btn btn-primary radius" type="button" onclick="submit();">提 交</button>
        <button class="btn btn-default radius ml-50" type="button" onclick="javascript:window.history.go(-1);">返回</button>
    </div>

</section>

<div class="modal fade" id="waitModal" tabindex="-1" role="dialog">
    <div class="modal-dialog text-c va-m" style="">
        <div class="modal-body">
            <img alt="" src="${context.domain!''}/image/wait.gif"/>
        </div>
    </div><!-- /.modal-dialog -->
</div>
<script type="text/javascript">

    var appId = getParameter('appId');
    var submitURI = '${context.domain!''}/app/' + appId + '/api';
    var headLine = $('#proxyHeaders').find(".header-set:last");

    function addHeader(k, v) {
        var newHeadLine = headLine.clone();
        if (k != undefined) {
            $(newHeadLine).find(":input[name=header\\.name]").val(k);
        }
        if (v != undefined) {
            $(newHeadLine).find(":input[name=header\\.value]").val(v);
        } else if (k != undefined) {
            $(newHeadLine).find(":input[name=header\\.type]").val('REMOVE').change();

        }
        $(newHeadLine).appendTo($('#proxyHeaders'));
        $(newHeadLine).show();
    }

    function removeHeader(obj) {
        var headLine = $(obj).parent().parent();
//        var headLines = $('#proxyHeaders').find(".header-set");
//        if(headLines.length == 1){
//            $.Huimodalalert('亲，再删就啥也没有了！',1000);
//            return;
//        }
        headLine.remove();
    }

    var targetLine = $('#upstream_targets').find('.upstream_target:last');
    function addUpstream(name, weight) {
        var newTargetLine = targetLine.clone();
        if (name != undefined)
            $(newTargetLine).find("input[name=target\\.url]").val(name);
        if (weight != undefined)
            $(newTargetLine).find("input[name=target\\.weight]").val(weight);
        $(newTargetLine).find("div[name=upstream_target_ops]").html('<button class="btn btn-warning radius" type="button" onclick="removeUpstream(this)">删除</button>');
        $(newTargetLine).appendTo($('#upstream_targets'));
        $(newTargetLine).show();
    }

    function removeUpstream(obj) {
        var headLine = $(obj).parent().parent();
        headLine.remove();
    }

    function switchHeader(obj) {
        if ($(obj).val() == 'ADD') {
            $(obj).parent().parent().find("div[name=eqs]").css('visibility', 'visible');
            $(obj).parent().parent().find("input[name=header\\.value]").css('visibility', 'visible');
        } else {
            $(obj).parent().parent().find("div[name=eqs]").css('visibility', 'hidden');
            $(obj).parent().parent().find("input[name=header\\.value]").css('visibility', 'hidden');
        }
    }

    function getItem(id) {
        $.ajax({
            url: "${context.domain!''}/app/" + appId + "/api/" + id,
            type: 'GET',
            dataType: 'json',
            async: false,
            success: function (api) {
                $('#name').val(api.name);
                $('#path').val(api.path);
                $('#vhost').val(api.vhost);
                $('#autoRun').attr('checked', api.running==1?true:false);
                var extendOption = JSON.parse(api.parameter);
                var preference = extendOption.preferenceOption;
                var method = preference.method;
                if (method == undefined || $.trim(method) == '') {
                    $('#methodAll').prop('checked');
                    $('#methodAll').click();
                } else {
                    $(':checkbox[name=methods]').each(function () {
                        if (method.indexOf($(this).val()) >= 0) {
                            $(this).prop('checked', true);
                        }
                    });
                }

                var upstream = extendOption.upstreamOption;
                var redirect = extendOption.redirectOption;
                var echoOption = extendOption.echoOption;

                var proxyType = '';
                if (upstream != undefined) {
                    proxyType = 'UPSTREAM';
                    var methodForward = upstream.methodForward;
                    $(":radio[name=methodForward]").each(function () {
                        if ($(this).val == methodForward) {
                            $(this).prop('checked', true);
                            return;
                        }
                    });

                    $(":input[name=proxyPolicy]").val(upstream.loadBalance);
                    $(":radio[name=upstreamOption\\.threadType]").each(function () {
                        if ($(this).val() == upstream.threadType) {
                            $(this).prop('checked', true);
                            $(this).click();
                        }
                    });
                    if (upstream.threadType == "API") {
                        $("#upstreamOption_maxPoolSize").find(":input").val(upstream.maxPoolSize);
                    }
                    var targets = upstream.targets;
                    for (var i = 0; i < targets.length; i++) {
                        var t = targets[i];
                        addUpstream(t.url, t.weight);
                    }

                    $('input:text[name=upstreamOption\\.timeout]').val(upstream.timeout);
                    $('input:text[name=upstreamOption\\.retry]').val(upstream.retry);
                    $(':checkbox[name=upstreamOption\\.passQueryString]').prop('checked', upstream.passQueryString);
                    $('input:text[name=upstreamOption\\.maxContentLength]').val(upstream.maxContentLength);
                    $('input:text[name=upstreamOption\\.maxInitialLineLength]').val(upstream.maxInitialLineLength);
                    $('input:text[name=upstreamOption\\.maxHeaderSize]').val(upstream.maxHeaderSize);
                    $('input:checkbox[name=upstreamOption\\.keepAlive]').prop('checked', upstream.keepAlive);
                    if(upstream.keepAlive)
                        $('input:text[name=upstreamOption\\.keepAliveTimeout]').val(upstream.keepAliveTimeout);
                    else
                        $('input:text[name=upstreamOption\\.keepAliveTimeout]').attr('disabled', true);

                    $('input:text[name=upstreamOption\\.maxIdleTimeout]').val(upstream.maxIdleTimeout);
                    $('input:text[name=upstreamOption\\.maxWaitQueueSize]').val(upstream.maxWaitQueueSize);

                    var passBody = upstream.passBody;

                    $(":radio[name=passBody\\.passBodyType]").each(function () {
                        if ($(this).val() == passBody.passBodyType) {
                            $(this).prop("checked", true);
                        }
                    });

                    if (passBody.passBodyType == 'PASS_BY_METHODS') {
                        $('input[name=passBody\\.option]').each(function () {
                            if (passBody.option.indexOf($(this).val()) >= 0) {
                                $(this).prop('checked', true);
                            }
                        });
                        $('#passBody_methods').show();
                    }

                    $(':radio[name=option\\.upstream]').each(function () {
                        if ($(this).val() == 1) {
//                            $(this).prop('checked');
                            $(this).click();
                        }
                    });

                    var appendHeaders = upstream.appendHeaders;
                    for (var key in appendHeaders) {
                        addHeader(key, appendHeaders[key]);
                    }

                    var removeHeaders = upstream.removeHeaders;
                    if (removeHeaders != undefined) {
                        for (var i = 0; i < removeHeaders.length; i++) {
                            addHeader(removeHeaders[i]);
                        }
                    }

                    $(':radio[name=option\\.headers]').each(function () {
                        if ($(this).val() == 1 && (appendHeaders != undefined || removeHeaders != undefined)) {
                            $(this).click();
                        } else if ($(this).val() == -1 && (appendHeaders == undefined && removeHeaders == undefined)) {
                            $(this).click();
                        }
                    });


                } else if (redirect != undefined) {
                    proxyType = 'REDIRECT';
                    $('#upstreamOption_REDIRECT').find(":input[name=redirectOption\\.url]").val(redirect.url);
                    $('#upstreamOption_REDIRECT').find(":checkbox[name=redirectOption\\.passQueryString]").prop('checked', redirect.passQueryString);
                } else if (echoOption != undefined) {
                    proxyType = 'ECHO';
                    var echo = echoOption.echo;
                    $('#upstreamOption_ECHO').find(":input[name=echo\\.code]").val(echo.code);
                    $('#upstreamOption_ECHO').find(":input[name=echo\\.contentType]").val(echo.contentType);
                    $('#upstreamOption_ECHO').find(":input[name=echo\\.content]").val(echo.content);
                }
                $(":radio[name=upstreamOption\\.proxyType]").each(function () {
                    if ($(this).val() == proxyType) {
//                        $(this).prop('checked', true);
                        $(this).click();
                    }
                });

                var root = preference.root;
                $(':radio[name=option\\.openStaticServer]').each(function () {
                    if ($(this).val() == 1 && root != undefined && $.trim(root).length > 0) {
//                            $(this).prop('checked');
                        $('#docRoot').text(root);
                        $(this).click();
                    } else if ($(this).val() == -1 && (root == undefined || $.trim(root).length == 0)) {
                        $(this).click();
                    }
                });

                var ipBlackList = preference.ipBlacklist;
                $(':radio[name=option\\.ipBlacklist]').each(function () {
                    if ($(this).val() == 1 && ipBlackList != undefined && $.trim(ipBlackList).length > 0) {
                        $('#ipBlackList').text(ipBlackList);
                        $(this).click();
                    } else if ($(this).val() == -1 && (ipBlackList == undefined || $.trim(ipBlackList).length == 0)) {
                        $(this).click();
                    }
                });

                var limit = preference.accessLimitOption;
                $(':radio[name=option\\.limit]').each(function () {
                    if ($(this).val() == 1 && limit != undefined) {
                        $(":input[name=limit\\.limit]").val(limit.limit);
                        $(":input[name=limit\\.limitPerIp]").val(limit.limitPerIp);

                        $(":input[name=limit\\.ipFromHeader]").prop('checked', limit.ipHeaderKey != undefined);
                        $(":input[name=limit\\.ipHeaderKey]").val(limit.ipHeaderKey);

                        $(":input[name=limit\\.limitPerClient]").val(limit.limitPerClient);
                        if (limit.limitPerClient > 0) {
                            $(":input[name=limit\\.keys]").val(limit.keys);
                        }

                        $(":input[name=limit\\.interval]").val(limit.interval);

                        $(":input[name=limit\\.timeUnit]").val(limit.timeUnit);
                        $(":input[name=limit\\.timeUnit]").change();
                        var overloadedReturn = {};
                        overloadedReturn = limit.overloadedReturn;
                        $(":input[name=limit\\.overloadedReturn\\.code]").val(overloadedReturn.code);
                        $(":input[name=limit\\.overloadedReturn\\.contentType]").val(overloadedReturn.contentType);
                        $(":input[name=limit\\.overloadedReturn\\.content]").val(overloadedReturn.content);
                        $(this).click();
                    } else if ($(this).val() == -1 && limit == undefined) {
                        $(this).click();
                    }
                });


                var auth = preference.authOption;
                $(':radio[name=option\\.auth]').each(function () {
                    if ($(this).val() == 1 && auth != undefined) {
                        $(":input[name=auth\\.name]").val(auth.name);
                        $(":input[name=auth\\.instance]").val(auth.instance);
                        $(":input[name=auth\\.instance]").change();
                        var authFailReturn = auth.failReturn;
                        $(":input[name=auth\\.failReturn\\.code]").val(authFailReturn.code);
                        $(":input[name=auth\\.failReturn\\.contentType]").val(authFailReturn.contentType);
                        $(":input[name=auth\\.failReturn\\.content]").text(authFailReturn.content);
//                        $('#openAuth').click();
                        $(this).click();
                    } else if ($(this).val() == -1 && auth == undefined) {
                        $(this).click();
                    }
                });

                var preHandlers = preference.preProcessors;
                if (preHandlers != undefined && preHandlers.length > 0) {
                    var preHandler = preHandlers[0];
                    $(':radio[name=option\\.preHandler]').each(function () {
                        if ($(this).val() == 1 && preHandler != undefined) {
                            $(":input[name=preHandler\\.name]").val(auth.name);
                            $(":input[name=preHandler\\.instance]").val(auth.instance);
                            $(":input[name=preHandler\\.instance]").change();
                            $(this).click();
                        } else if ($(this).val() == -1 && auth == undefined) {
                            $(this).click();
                        }
                    });
                }

                var postHandlers = preference.postProcessors;
                if (postHandlers != undefined && postHandlers.length > 0) {
                    var postHandler = postHandlers[0];
                    $(':radio[name=option\\.postHandler]').each(function () {
                        if ($(this).val() == 1 && postHandler != undefined) {
                            $(":input[name=postHandler\\.name]").val(auth.name);
                            $(":input[name=postHandler\\.instance]").val(auth.instance);
                            $(":input[name=postHandler\\.instance]").change();
                            $(this).click();
                        } else if ($(this).val() == -1 && auth == undefined) {
                            $(this).click();
                        }
                    });
                }

            },
            error: function (err) {

            }
        });
    }

    function submit() {
        $('#waitModal').modal('show');
        var api = {};
        api.name = $('#name').val();
        api.path = $('#path').val();
        api.vhost = $('#vhost').val();
        api.appId = appId;
        api.running=$('#autoRun').prop('checked')?1:0;
        var parameter = {};
        api.extendOption = parameter;
        var preference = {};
        parameter.preferenceOption = preference;
        var cm = new Array();
        $(':checkbox[name=methods]:checked').each(function () {
            cm.push($(this).val());
        });
        if (cm.length > 0)
            preference.method = cm.join(',');
        else
            preference.method = undefined;


        var proxyType = $(":radio[name=upstreamOption\\.proxyType]:checked").val();
        var upstreamOption = {};
        if (proxyType == 'UPSTREAM') {
            parameter.upstreamOption = upstreamOption;
            upstreamOption.proxyType = proxyType;
            upstreamOption.methodForward = $(":radio[name=methodForward]:checked").val();
            if (upstreamOption.methodForward == '')
                upstreamOption.methodForward = undefined;
            upstreamOption.loadBalance = $(":input[name=proxyPolicy]").val();
            upstreamOption.threadType = $(":radio[name=upstreamOption\\.threadType]").val();
            if (upstreamOption.threadType == "API") {
                upstreamOption.maxPoolSize = $("#upstreamOption_maxPoolSize").find(":input").val();
            }

            var targets = new Array();
            var targetsBox = $("#upstream_targets").find(".upstream_target");
            targetsBox.each(function () {
                var target = {};
                target.url = $(this).find(":input[name=target\\.url]").val();
                if (target.url == undefined || $.trim(target.url) == '') {
                    return;
                }
                target.weight = $(this).find(":input[name=target\\.weight]").val();
                if (target.weight == undefined || $.trim(target.weight) == '') {
                    target.weight = 1;
                }
                targets.push(target);
            })
            if (targets.length < 1) {
                $.Huimodalalert('必须指定转发目标url！', 2000, function(){
                    $('#waitModal').modal('hide');
                });
                return;
            }
            upstreamOption.targets = targets;
        } else if (proxyType == 'REDIRECT') {
            var redirectOption = {};
            redirectOption.proxyType = 'REDIRECT';
            parameter.redirectOption = redirectOption;
            redirectOption.url = $('#upstreamOption_REDIRECT').find(":input[name=redirectOption\\.url]").val();
            redirectOption.passQueryString = $('#upstreamOption_REDIRECT').find(":checkbox[name=redirectOption\\.passQueryString]").prop('checked');
            if (redirectOption.url == undefined || $.trim(redirectOption.url) == '') {
                $.Huimodalalert('必须指定重定向目标 url！', 2000, function(){
                    $('#waitModal').modal('hide');
                });
                return;
            }
        } else if (proxyType == 'ECHO') {
            var echoOption = {};
            parameter.echoOption = echoOption;
            echoOption.proxyType = 'ECHO';
            var echo = {};
            echo.code = $('#upstreamOption_ECHO').find(":input[name=echo\\.code]").val();
            echo.contentType = $('#upstreamOption_ECHO').find(":input[name=echo\\.contentType]").val();
            echo.content = $('#upstreamOption_ECHO').find(":input[name=echo\\.content]").val();
            echoOption.echo = echo;
        }

        var proxyUseType = $(":radio[name=option\\.upstream]:checked").val();
        if (proxyUseType == undefined) {
            upstreamOption.upstreamOptionType = "NONE";
        } else if (proxyUseType > 0) {
            upstreamOption.timeout = $('input:text[name=upstreamOption\\.timeout]').val();
            upstreamOption.retry = $('input:text[name=upstreamOption\\.retry]').val();
            upstreamOption.passQueryString = $(':checkbox[name=upstreamOption\\.passQueryString]').prop('checked');
            upstreamOption.maxContentLength = $('input:text[name=upstreamOption\\.maxContentLength]').val();
            upstreamOption.maxInitialLineLength = $('input:text[name=upstreamOption\\.maxInitialLineLength]').val();
            upstreamOption.maxHeaderSize = $('input:text[name=upstreamOption\\.maxHeaderSize]').val();
            upstreamOption.keepAlive = $(':checkbox[name=upstreamOption\\.keepAlive]').prop('checked');
            upstreamOption.keepAliveTimeout = $('input:text[name=upstreamOption\\.keepAliveTimeout]').val();
            upstreamOption.maxIdleTimeout = $('input:text[name=upstreamOption\\.maxIdleTimeout]').val();
            upstreamOption.maxWaitQueueSize = $('input:text[name=upstreamOption\\.maxWaitQueueSize]').val();

            var passBody = {};
            upstreamOption.passBody = passBody;
            passBody.passBodyType = $(":radio[name=passBody\\.passBodyType]:checked").val();
            if (passBody.passBodyType == 'PASS_BY_METHODS') {
                var cm = new Array();
                $('input[name=passBody\\.option]:checked').each(function () {
                    cm.push($(this).val());
                });
                passBody.option = cm.join(',');
            }
        } else {
            upstreamOption.upstreamOptionType = "APP";
        }

        var headerType = $(":radio[name=option\\.headers]:checked").val();

        if (headerType < 0) {
            upstreamOption.headerType = 'NONE';
        } else if (headerType == 0) {
            upstreamOption.headerType = 'APP';
        } else {
            var appendHeaders = {};
            upstreamOption.appendHeaders = appendHeaders;
            var removeHeaders = new Array();
            upstreamOption.removeHeaders = removeHeaders;

            var headers = $('#proxyHeaders').find(".header-set");
            headers.each(function () {
                var type = $(this).find(":input[name=header\\.type]").val();
                if (type == 'ADD') {
                    var key = $(this).find(":input[name=header\\.name]").val();
                    if (key == undefined || $.trim(key) == '')
                        return;
                    appendHeaders[key] = $(this).find(":input[name=header\\.value]").val();
                } else {
                    var rmHeaders = $(this).find(":input[name=header\\.name]").val();
                    if (rmHeaders != undefined && $.trim(rmHeaders) != '') {
                        var rm = rmHeaders.split(',');
                        for (var i = 0; i < rm.length; i++) {
                            removeHeaders.push(rm[i]);
                        }
                    }
                }
            });
        }

        var staticServer = $(':radio[name=option\\.openStaticServer]:checked').val();
        if (staticServer < 0) {
            preference.staticServerType = 'NONE';
        } else if (staticServer == 0) {
            preference.staticServerType = 'APP';
        } else {
            preference.staticServer = true;
            preference.root = $(":input[name=docRoot]").val();
        }

        var limit = $(':radio[name=option\\.limit]:checked').val();
        if (limit < 0) {
            preference.accessLimitType = 'NONE';
        } else if (limit == 0) {
            preference.accessLimitType = 'APP';
        } else {
            var accessLimit = {};
            preference.accessLimitOption = accessLimit;
            accessLimit.limit = $(":input[name=limit\\.limit]").val();
            accessLimit.limitPerIp = $(":input[name=limit\\.limitPerIp]").val();
            if ($(":input[name=limit\\.ipFromHeader]").prop('checked')) {
                accessLimit.ipHeaderKey = $(":input[name=limit\\.ipHeaderKey]").val();
            }
            accessLimit.limitPerClient = $(":input[name=limit\\.limitPerClient]").val();
            var keys = $(":input[name=limit\\.keys]").val();
            keys = keys == undefined ? '' : keys;
            accessLimit.keys = keys.split(',');
            accessLimit.interval = $(":input[name=limit\\.interval]").val();
            accessLimit.timeUnit = $(":input[name=limit\\.timeUnit]").val();
            var overloadedReturn = {};
            accessLimit.overloadedReturn = overloadedReturn;
            overloadedReturn.code = $(":input[name=limit\\.overloadedReturn\\.code]").val();
            overloadedReturn.contentType = $(":input[name=limit\\.overloadedReturn\\.contentType]").val();
            overloadedReturn.content = $(":input[name=limit\\.overloadedReturn\\.content]").val();
        }

        var ipBlacklistType = $(':radio[name=option\\.ipBlacklist]:checked').val();
        if (ipBlacklistType < 0) {
            preference.ipBlacklistType = 'NONE';
        } else if (ipBlacklistType == 0) {
            preference.ipBlacklistType = 'APP';
        } else {
            var ipBlacklist = $('#ipBlacklist').val();
            if (ipBlacklist != undefined) {
                preference.ipBlacklist = ipBlacklist.split(',');
            }
            preference.ipBlacklist = ipBlacklist.split(',');
        }

        var auth = $(':radio[name=option\\.auth]:checked').val();
        if (auth < 0) {
            preference.authType = 'NONE';
        } else if (auth == 0) {
            preference.authType = 'APP';
        } else {
            var authOption = {};
            preference.authOption = authOption;
            authOption.name = $(":input[name=auth\\.name]").val();
            authOption.instance = $(":input[name=auth\\.instance]").val();
            authOption.usage = $("#auth\\.usage").text();
            var authFailReturn = {};
            authOption.failReturn = authFailReturn;
            authFailReturn.code = $(":input[name=auth\\.failReturn\\.code]").val();
            authFailReturn.contentType = $(":input[name=auth\\.failReturn\\.contentType]").val();
            authFailReturn.content = $(":input[name=auth\\.failReturn\\.content]").val();
        }

        var openPreHandler = $(':radio[name=option\\.preHandler]:checked').val();
        if (openPreHandler < 0) {
            preference.preHandlerType = "NONE";
        } else if (openPreHandler == 0) {
            preference.preHandlerType = "APP";
        } else {
            var preHandler = {};
            preference.preProcessors = new Array();
            preHandler.name = $(":input[name=preHandler\\.name]").val();
            preHandler.instance = $(":input[name=preHandler\\.instance]").val();
            preHandler.usage = $("#preHandler\\.usage").text();
            preference.preProcessors.push(preHandler);
        }

        var openPostHandler = $(':radio[name=option\\.postHandler]:checked').val();
        if (openPostHandler < 0) {
            preference.postHandlerType = "NONE";
        } else if (openPostHandler == 0) {
            preference.postHandlerType = "APP";
        } else {
            var postHandler = {};
            preference.postProcessors = new Array();
            postHandler.name = $(":input[name=postHandler\\.name]").val();
            postHandler.instance = $(":input[name=postHandler\\.instance]").val();
            ;
            postHandler.usage = $("#postHandler\\.usage").text();
            preference.postProcessors.push(postHandler);
        }

        $.ajax({
            url: submitURI,
            data: JSON.stringify(api),
            type: 'POST',
            dataType: 'json',
            success: function (data) {
                if(processData(data))
                $.Huimodalalert('API已创建！', 2000, function () {
                    $('#waitModal').modal('hide');
                    window.location = 'api.html?appId=' + appId;
                });
            },
            error: function (err) {
                $.Huimodalalert(err.responseText, 2000, function(){
                    $('#waitModal').modal('hide');
                });
            }
        });

    }

    var authUsage = new Array();
    var preHandlerUsage = new Array();
    var postHandlerUsage = new Array();

    function loadEnv() {
        $.ajax({
            url: '${context.domain!''}/env',
            type: 'GET',
            async: false,
            dataType: 'json',
            success: function (data) {
                var auths = data.auths;

                authUsage = new Array();
                var avauths = "";
                for (var i = 0; auths != undefined && i < auths.length; i++) {
                    var auth = auths[i];

                    authUsage[auth.instance] = auth.usage;
                    avauths += "<option value='" + auth.instance + "'>" + auth.instance + "</option>";
                    if (i == 0) {
                        $('#auth\\.usage').html(auth.usage);
                    }
                }
                $('#auth_instance').html(avauths);

                var preHandlers = data.preHandlers;
                preHandlerUsage = new Array();
                var avpreHandlers = "";
                for (var i = 0; preHandlers != undefined && i < preHandlers.length; i++) {
                    var preHandler = preHandlers[i];
                    preHandlerUsage[preHandler.instance] = preHandler.usage;
                    avpreHandlers += "<option value='" + preHandler.instance + "'>" + preHandler.instance + "</option>";
                }
                $('#preHandler_instance').html(avpreHandlers);
                $('#preHandler_instance').change();


                var postHandlers = data.postHandlers;
                postHandlerUsage = new Array();
                var avpostHandlers = "";
                for (var i = 0; postHandlers != undefined && i < postHandlers.length; i++) {
                    var postHandler = postHandlers[i];
                    postHandlerUsage[postHandler.instance] = postHandler.usage;
                    avpostHandlers += "<option value='" + postHandler.instance + "'>" + postHandler.instance + "</option>";
                }
                $('#postHandler_instance').html(avpostHandlers);
                $('#postHandler_instance').change();

            },
            error: function (err) {

            }
        });
    }

    $(function () {

        $(".input-text,.textarea").Huifocusblur();

        $("#extend-panel").Huifold({
            titCell: '.e-p-h',
            mainCell: '.e-p-b',
            type: 1,
            trigger: 'click',
            className: "selected",
            speed: "first",
        });

        $("#methodAll").click(function () {
            $(":checkbox[name=methods]").attr('checked', false);
            if (this.checked) {
                $(":checkbox[name=methods]").attr('disabled', true);
            } else {
                $(":checkbox[name=methods]").attr('disabled', false);
            }
        });

        $("#upstreamOption_keepAlive").change(function () {
            var checked = $(this).prop('checked');
            if (checked) {
                $("#upstreamOption_keepAliveTimeout").attr('disabled', false);
            } else {
                $("#upstreamOption_keepAliveTimeout").attr('disabled', true);
            }
        });

        $(":radio[id*=passBody_passBodyType]").click(function () {
            if ($(this).val() == 'PASS_BY_METHODS') {
                $('#passBody_methods').show();
            } else {
                $('#passBody_methods').hide();
            }
        });

        $(":radio[name=upstreamOption\\.threadType]").click(function () {
            if ($(this).val() == 'APP') {
                $("#upstream_threadpool").hide();
                $(".proxy_sets").hide();
            } else {
                $("#upstream_threadpool").show();
                $(".proxy_sets").show();
            }
        });

        $(".e-p-i-normal").Huifold({
            titCell: '.e-p-i-h',
            mainCell: '.e-p-i-b',
            type: 1,
            trigger: 'click',
            className: "selected",
            speed: "first",
        });

        $(".item_trigger").click(function () {
            var value = $(this).val();
            var panel = $(this).parent().parent().parent().find('div.panel-body');
            if (value > 0) {
                panel.slideDown('first').end().addClass('selected');
            } else {
                panel.slideUp('first').end().removeClass('selected');
            }
        });

        $(":radio[name=upstreamOption\\.proxyType]").click(function () {
            var value = $(this).val();
            var box = $(this).parents('.e-p-i-b');
            box.children(".upstreamOption[id!=upstreamOption_" + value + "]").hide();
            box.children(".upstreamOption[id=upstreamOption_" + value + "]").show();
            if (value == 'UPSTREAM') {
                $('#upstream_panel').show();
                $('#header_panel').show();
            } else {
                $('#upstream_panel').hide();
                $('#header_panel').hide();
            }
        });

        $("#upstreamOption_maxPoolSize").Huispinner({
            value: 5,
            minValue: 1,
            maxValue: 65535,
            dis: 1
        });

        $('#auth_instance').change(function () {
            var instance = $(this).val();
            $('#auth\\.usage').html(authUsage[instance]);
        });

        $('#preHandler_instance').change(function () {
            var instance = $(this).val();
            $('#preHandler\\.usage').html(preHandlerUsage[instance]);
        });

        $('#postHandler_instance').change(function () {
            var instance = $(this).val();
            $('#postHandler\\.usage').html(postHandlerUsage[instance]);
        });

        loadEnv();

        appId = getParameter("appId");
        var id = getParameter('id');
        if (id != undefined) {
            $('#action_tag').text('编辑应用');
            submitURI = '${context.domain!''}/app/' + appId + '/api/' + id;
            getItem(id);
        }
    });

</script>
</body>
</html>