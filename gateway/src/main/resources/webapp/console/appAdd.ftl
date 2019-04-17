<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-编辑应用</title>
    <#include "./inc.ftl" />

</head>
<body class="fixed-sidebar full-height-layout gray-bg">

<#include "./header.ftl" />

<section class="Hui-article-box" style="overflow-y: scroll">
    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">应用</a>
        <span class="c-999 en">&gt;</span><span class="c-666" id="action_tag">创建应用</span>
    </nav>

    <form id="mainForm" class="form form-horizontal responsive" action="#" method="post">
        <div class="panel panel-default mt-20">
            <div class="panel-header">基本设置</div>
            <div class="panel-body">
                <div class="row cl">
                    <label class="form-label col-xs-3" for="name">APP名称:</label>
                    <div class="formControls col-xs-8">
                        <input class="input-text" id="name" name="name" placeholder="请输入项目名，如 project"/>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3" for="description">简介:</label>
                    <div class="formControls col-xs-8">
                        <textarea class="textarea" id="description" name="description" rows="3"></textarea>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">服务类型:</label>
                    <div class="formControls skin-minimal col-xs-8">
                        <div class="radio-box">
                            <input type="radio" id="protocol-1" name="protocol" value="HTTP_HTTPS" checked>
                            <label for="protocol-1">HTTP(/HTTPS)</label>
                        </div>
                        <#--<div class="radio-box">-->
                            <#--<input type="radio" id="protocol-3" name="protocol" value="WEB_SOCKET" disabled>-->
                            <#--<label for="protocol-3">WEB_SOCKET</label>-->
                        <#--</div>-->
                        <div class="radio-box">
                            <input type="radio" id="protocol-4" name="protocol" value="TCP" disabled>
                            <label for="protocol-4">TCP</label>
                        </div>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">使用端口:</label>
                    <div class="formControls col-xs-8">
                        <input class="input-text" id="port" name="port" placeholder="8765" value="8765"/>
                    </div>
                </div>
                <div class="row cl" style="display: none;">
                    <label class="form-label col-xs-3" for="name">vhost:</label>
                    <div class="formControls col-xs-8">
                        <input class="input-text" id="vhost" name="vhost" placeholder="请输入vhost，如 x.test.com，多个以 空格 拼接"
                               value="*"/>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">发布到网关组:</label>
                    <div class="formControls col-xs-8">
                        <select id="deployGroup" class="input-text" style="width: 50%" name="deployGroup" size="1">

                        </select>
                    </div>
                </div>
            </div>
        </div>

        <div class="panel panel-default mt-20" id="extend-panel">
            <div class="e-p-h panel-header">扩展设置<b style="float:right;margin-right: 10px">+</b></div>
            <div class="e-p-b panel-body">

                <div class="e-p-i panel panel-default">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">HTTPS</label>
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openHTTPS"
                                                     name="openHTTPS"> <b class='openOrClose'>启用</b></div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">HTTPS端口:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="ssl.port" placeholder="8443" value="8443"/>
                            </div>
                        </div>
                        <div class="row cl" style="margin-top:0px">
                            <label class="form-label col-xs-3">证书类型:</label>
                            <div class="formControls col-xs-8">
                                <select name="ssl.certType" size="1">
                                    <option value="JKS">JKS</option>
                                    <option value="PFX">PFX</option>
                                    <option value="PEM">PEM</option>
                                </select>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">证书key路径或密码:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="ssl.keyPath"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">证书路径:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="ssl.certPath"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i-normal panel panel-default mt-20">
                    <div class="e-p-i-h panel-header">反向代理服务设置<b style="float:right;margin-right: 10px">+</b></div>
                    <div class="e-p-i-b panel-body">
                        <!--<div class="row cl">-->
                        <!--<label class="form-label col-xs-3">转发线程池类型:</label>-->
                        <!--<div class="formControls col-xs-8">-->
                        <!--<div class="radio-box">-->
                        <!--<input type="radio" id="upstreamOption.threadType-1" name="upstreamOption.threadType" value="APP" checked>-->
                        <!--<label for="upstreamOption.threadType-1">整个应用公用线程池</label>-->
                        <!--</div>-->
                        <!--<div class="check-box">-->
                        <!--<input type="radio" id="upstreamOption.threadType-2" name="upstreamOption.threadType" value="API">-->
                        <!--<label for="upstreamOption.threadType-2">接口自用线程池</label>-->
                        <!--</div>-->
                        <!--</div>-->
                        <!--</div>-->
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
                        <div class="row cl">
                            <label class="form-label col-xs-3">最大内容长度:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxContentLength"
                                       placeholder="-1为不限制" value="-1"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">url最大请求参数长度:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxInitialLineLength"
                                       placeholder="-1为不限制" value="4096"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">最大请求头:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxHeaderSize"
                                       placeholder="-1为不限制" value="8192"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">最大线程池:</label>
                            <div class="formControls col-xs-3">
                                <div id="upstreamOption_maxPoolSize"></div>
                            </div>
                            <div class="formControls col-xs-3">
                                如果这个应用经常应对高并发场景，这个值可以设置稍微大一点。可以理解为同时连接的数量
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">keepalive保持:</label>
                            <div class="formControls col-xs-5">
                                <div class="check-box">
                                    <input type="checkbox" name="upstreamOption.keepAlive" checked/>
                                </div>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">keepalive保持时长(秒):</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.keepAliveTimeout"
                                       value="60"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">连接最大闲置时长:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxIdleTimeout"
                                       placeholder="-1为不限制" value="5000"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">拥堵时等待队列最大大小:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="upstreamOption.maxWaitQueueSize"
                                       placeholder="默认为-1，不限制" value="-1"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i-normal panel panel-default mt-20">
                    <div class="e-p-i-h panel-header">请求头设定<b style="float:right;margin-right: 10px">+</b></div>
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
                    <div class="panel-header cl">
                        <label class="col-xs-3 text-l pl-5">静态WEB服务</label>
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openStaticServer"
                                                     name="openStaticServer" value="1"> 启用
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
                        <label class="col-xs-3 text-l pl-5">SESSION</label>
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openSession"
                                                     name="openSession" value="1"> <b class='openOrClose'>启用</b></div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">interval:</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="openSession.interval" value="1800"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">SessionName:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="openSession.name" value="RATEL_SESSION"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">跨域</label>
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openCrossDomain"
                                                     name="openCrossDomain" value="1"> <b class='openOrClose'>启用</b>
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">允许请求源:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="crossDomain.allowedOrigin"
                                       placeholder="host，支持正则。如(http|https)://some.com"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">允许的请求METHOD:</label>
                            <div class="formControls skin-minimal col-xs-8">
                                <input type="checkbox" id="crossMethodAll"/>&nbsp;所有
                                <input type="checkbox" name="crossDomain.allowedMethods" value="GET" checked/>&nbsp;GET
                                <input type="checkbox" name="crossDomain.allowedMethods" value="POST" checked/>&nbsp;POST
                                <input type="checkbox" name="crossDomain.allowedMethods" value="PUT" checked/>&nbsp;PUT
                                <input type="checkbox" name="crossDomain.allowedMethods" value="DELETE" checked/>&nbsp;DELETE
                                <input type="checkbox" name="crossDomain.allowedMethods" value="PATCH"/>&nbsp;PATCH
                                <input type="checkbox" name="crossDomain.allowedMethods" value="OPTION"/>&nbsp;OPTION
                                <input type="checkbox" name="crossDomain.allowedMethods" value="TRACE"/>&nbsp;TRACE
                                <input type="checkbox" name="crossDomain.allowedMethods" value="CONNECT"/>&nbsp;CONNECT
                                <input type="checkbox" name="crossDomain.allowedMethods" value="OTHER"/>&nbsp;OTHER
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">最大缓存时长(秒):</label>
                            <div class="formControls col-xs-2">
                                <input type="text" class="input-text" name="crossDomain.maxAgeSeconds"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">允许发送cookie:</label>
                            <div class="formControls col-xs-8">
                                <input type="checkbox" name="crossDomain.allowCredentials"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">允许的headers:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="crossDomain.allowedHeaders"
                                       placeholder="多个使用，拼接"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">暴露的headers:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="crossDomain.exposedHeaders"
                                       placeholder="多个使用，拼接"/>
                            </div>
                        </div>

                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">流控</label>
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openAccessLimit"
                                                     name="openAccessLimit" value="1"> 启用
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

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">IP黑名单</label>
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openIpFilter"
                                                     name="openIpFilter" value="1"> <b class='openOrClose'>启用</b></div>
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
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openAuth" name="openAuth"
                                                     value="1"> <b class='openOrClose'>启用</b></div>
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
                                <p class="textarea" id="auth.usage" placeholder=""></p>
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

                <div class="e-p-i panel panel-default mt-20" style="display: none">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">自动降级/熔断</label>
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openBlow"
                                                     name="openBlow" value="1" disabled> <b class='openOrClose'>启用</b>
                        </div>
                    </div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">开启自动熔断:</label>
                            <div class="formControls col-xs-8">
                                <input type="checkbox" class="input-text" name="blow.auto" disabled/>
                            </div>
                        </div>
                        <div class="row cl">
                            <div class="row cl">
                                <label class="form-label col-xs-3">熔断阈值:</label>
                                <div class="formControls col-xs-8">
                                    <input type="text" class="input-text" name="blow.errorCycle" value="5"
                                           style="width: 3rem;"/> 分钟 接口错误率超过 <input type="text" class="input-text" name="blow.errorRate" value="5"
                                                                                    style="width: 3rem;"/>%
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">超时占比:</label>
                                <div class="formControls col-xs-8">
                                    响应超过<input type="text" class="input-text" name="blow.timeout" value="30000"
                                               style="width: 4rem;"/> 秒的响应超过<input type="text" class="input-text" name="blow.timeoutRate" value="10"
                                                                                   style="width: 3rem;"/>%
                                </div>
                            </div>
                        </div>
                        <div class="row cl">
                            <div class="row cl">
                                <label class="form-label col-xs-3">熔断阈值:</label>
                                <div class="formControls col-xs-8">
                                    <input type="text" class="input-text" name="blow.errorCycle" value="5"
                                           style="width: 3rem;"/> 分钟 接口错误率小于 <input type="text" class="input-text" name="blow.errorRate" value="5"
                                                                                    style="width: 2rem;"/>%
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">超时占比:</label>
                                <div class="formControls col-xs-8">
                                    响应超过<input type="text" class="input-text" name="blow.timeout" value="30000"
                                               style="width: 4rem;"/> 秒的响应小于<input type="text" class="input-text" name="blow.timeoutRate" value="10"
                                                                                   style="width: 3rem;"/>%
                                </div>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">返回码:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="blow.result.code" value="200" style="width: 3rem;"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">信息类型:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="blow.result.contentType"
                                       value="application/json"/>
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">内容:</label>
                            <div class="formControls col-xs-8">
                                <input type="text" class="input-text" name="blow.result.content" value='{"success":true,"msg":"服务暂时停止了"}'/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">前置处理器</label>
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openPreHandler"
                                                     name="openPreHandler" value="1"> <b class='openOrClose'>启用</b>
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
                                <p class="textarea" name="preHandler.usage" id="preHandler.usage" placeholder=""></p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">
                        <label class="col-xs-3 text-l pl-5">后置处理器</label>
                        <div class="col-xs-2"><input type="checkbox" class="item_trigger" id="openPostHandler"
                                                     name="openPostHandler" value="1"> <b class='openOrClose'>启用</b>
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
                                    <p class="textarea" id="postHandler.usage" placeholder=""></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i-normal panel panel-default mt-20" style="display: none;">
                    <div class="e-p-i-h panel-header">响应状态码定义<b style="float:right;margin-right: 10px">+</b></div>
                    <div class="e-p-i-b panel-body" id="code_panel">
                        <div class="row cl">
                            <div class="formControls col-xs-9"></div>
                            <div class="formControls col-xs-3 text-r">
                                <button class="btn btn-secondary radius" type="button" onclick="addCode()">增加</button>
                            </div>
                        </div>
                        <div class="code-set cl" style="display:none">
                            <div class="row cl">
                                <label class="form-label col-xs-3">状态码:</label>
                                <div class="formControls col-xs-6">
                                    <input type="text" class="input-text" style="width: 100px;" name="code"
                                           value="400"/>
                                </div>
                                <div class="formControls col-xs-3 text-r">
                                    <button class="btn btn-warning radius" type="button" onclick="removeCode(this)">删除
                                    </button>
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">Content-Type</label>
                                <div class="formControls col-xs-8">
                                    <input type="text" class="input-text" name="contentType" value="application/json"/>
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">内容:</label>
                                <div class="formControls col-xs-8">
                                    <input type="text" class="input-text" name="content" value="使用的key不正确"/>
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
            <img alt="" src="image/wait.gif"/>
        </div>
    </div><!-- /.modal-dialog -->
</div>

<script type="text/javascript">
    var appId;
    var submitURI = '${context.domain!''}/app';

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
        var headLines = $('#proxyHeaders').find(".header-set");
//        if(headLines.length == 1){
//            $.Huimodalalert('亲，再删就啥也没有了！',1000);
//            return;
//        }
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

    var codeLine = $('#code_panel').find(".code-set:first");
    function addCode(code) {
        var newCodeLine = codeLine.clone();
        if (code) {
            $(newCodeLine).find(":input[name=code]").val(code.code);
            $(newCodeLine).find(":input[name=contentType]").val(code.contentType);
            $(newCodeLine).find(":input[name=content]").val(code.content);
        }
        $(newCodeLine).appendTo($('#code_panel'));
        $(newCodeLine).show();
    }

    function removeCode(obj) {
        var codeLine = $(obj).parent().parent().parent();
        codeLine.remove();
    }

    function getItem(id) {
        $.ajax({
            url: "${context.domain!''}/app/" + id,
            type: 'GET',
            dataType: 'json',
            async: false,
            success: function (app) {
                $('#name').val(app.name);
                $('#description').val(app.description);
                $(':radio[name=protocol]').val(app.protocol);
                $('#vhost').val(app.vhost);
                $('#port').val(app.port);
                $(":input[name=deployGroup]").val(app.deployGroup);

                var extendOption = JSON.parse(app.parameter);

                var ssl = extendOption.ssl;
                if (ssl != undefined) {
                    $(':input[name=ssl\\.port]').val(ssl.port);
                    $(':input[name=ssl\\.certType]').val(ssl.certType);
                    $(':input[name=ssl\\.keyPath]').val(ssl.keyPath);
                    $(':input[name=ssl\\.certPath]').val(ssl.certPath);
                    $('#openHTTPS').click();
                }

                var upstreamOption = extendOption.upstreamOption;
                $('input:text[name=upstreamOption\\.timeout]').val(upstreamOption.timeout);
                $('input:text[name=upstreamOption\\.retry]').val(upstreamOption.retry);
                $('input:checkbox[name=upstreamOption\\.passQueryString]').prop('checked', upstreamOption.passQueryString);

                var passBody = upstreamOption.passBody;

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

                $('input:text[name=upstreamOption\\.threadType]').val(upstreamOption.threadType);
                $('input:text[name=upstreamOption\\.maxContentLength]').val(upstreamOption.maxContentLength);
                $('input:text[name=upstreamOption\\.maxInitialLineLength]').val(upstreamOption.maxInitialLineLength);
                $('input:text[name=upstreamOption\\.maxHeaderSize]').val(upstreamOption.maxHeaderSize);
                $("#upstreamOption_maxPoolSize").find(":input").val(upstreamOption.maxPoolSize);
                $('input:text[name=upstreamOption\\.keepAlive]').prop('checked', upstreamOption.keepAlive);
                $('input:text[name=upstreamOption\\.keepAliveTimeout]').val(upstreamOption.keepAliveTimeout);
                $('input:text[name=upstreamOption\\.maxIdleTimeout]').val(upstreamOption.maxIdleTimeout);
                $('input:text[name=upstreamOption\\.maxWaitQueueSize]').val(upstreamOption.maxWaitQueueSize);

                var appendHeaders = upstreamOption.appendHeaders;
                for (var key in appendHeaders) {
                    addHeader(key, appendHeaders[key]);
                }

                var removeHeaders = upstreamOption.removeHeaders;
                if (removeHeaders != undefined) {
                    for (var i = 0; i < removeHeaders.length; i++) {
                        addHeader(removeHeaders[i]);
                    }
                }

                var sessionOption = extendOption.sessionOption;
                if (sessionOption != undefined) {
                    $('#openSession').click();
                    $(":input[name=openSession\\.interval]").val(sessionOption.interval);
                    $(":input[name=openSession\\.name]").val(sessionOption.name);
                }

                var preferenceOption = extendOption.preferenceOption;

                var crossDomain = extendOption.crossDomain;
                if (crossDomain != undefined) {
                    $('#openCrossDomain').click();
                    $(":input[name=crossDomain\\.allowedOrigin]").val(crossDomain.allowedOrigin);
                    $(":input[name=crossDomain\\.allowCredentials]").prop('checked', crossDomain.allowCredentials);
                    $(":input[name=crossDomain\\.maxAgeSeconds]").val(crossDomain.maxAgeSeconds);
                    $(':input[name=crossDomain\\.allowedMethods]:checked').val(crossDomain.allowedMethods);
                    $(":input[name=crossDomain\\.allowedHeaders]").val(crossDomain.allowedHeaders);
                    $(":input[name=crossDomain\\.exposedHeaders]").val(crossDomain.exposedHeaders);
                }

                var root = preferenceOption.root;
                if (root != undefined && $.trim(root).length > 0) {
                    $('#openStaticServer').click();
                    $('#docRoot').text(root);
                }

                var ipBlackList = preferenceOption.ipBlacklist;
                if (ipBlackList != undefined && ipBlackList.length > 0) {
                    $('#ipBlackList').text(ipBlackList);
                    $('#openIpFilter').click();
                }

                var limit = preferenceOption.accessLimitOption;
                if (limit != undefined) {
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
                    var overloadedReturn = {};
                    overloadedReturn = limit.overloadedReturn;
                    $(":input[name=limit\\.overloadedReturn\\.code]").val(overloadedReturn.code);
                    $(":input[name=limit\\.overloadedReturn\\.contentType]").val(overloadedReturn.contentType);
                    $(":input[name=limit\\.overloadedReturn\\.content]").val(overloadedReturn.content);
                    $('#openAccessLimit').click();
                }

                var auth = preferenceOption.authOption;
                if (auth != undefined) {
                    $(":input[name=auth\\.name]").val(auth.name);
                    $(":input[name=auth\\.instance]").val(auth.instance);
                    $(":input[name=auth\\.instance]").change();
                    var authFailReturn = auth.failReturn;
                    $(":input[name=auth\\.failReturn\\.code]").val(authFailReturn.code);
                    $(":input[name=auth\\.failReturn\\.contentType]").val(authFailReturn.contentType);
                    $(":input[name=auth\\.failReturn\\.content]").text(authFailReturn.content);
                    $('#openAuth').click();
                }

                var preHandlers = preferenceOption.preProcessors;
                if (preHandlers != undefined && preHandlers.length > 0) {
                    var preHandler = preHandlers[0];
                    $(":input[name=preHandler\\.name]").val(preHandler.name);
                    $(":input[name=preHandler\\.instance]").val(preHandler.instance);
                    $(":input[name=preHandler\\.instance]").change();
                    $('#openPreHandler').click();
                }

                var postHandlers = preferenceOption.postProcessors;
                if (postHandlers != undefined && postHandlers.length > 0) {
                    var postHandler = postHandlers[0];
                    $(":input[name=postHandler\\.name]").val(postHandler.name);
                    $(":input[name=postHandler\\.instance]").val(postHandler.instance);
                    $(":input[name=postHandler\\.instance]").change();
                    $('#openPostHandler').click();
                }

                var customCodes = preferenceOption.customCodes;
                for (var i = 0; customCodes && i < customCodes.length; i++) {
                    addCode(customCodes[i]);
                }

            },
            error: function (err) {

            }
        });
    }

    function submit() {
        var bv = $('#mainForm').data('bootstrapValidator');

        $('#waitModal').modal('show');
        var app = {};
        if (appId != undefined && $.trim(appId) != '') {
            app.id = appId;
        }
        app.name = $('#name').val();
        app.description = $('#description').val();
        app.protocol = $(':radio[name=protocol]').val();
        app.vhost = $('#vhost').val();
        app.port = $('#port').val();

        app.deployGroup = $(":input[name=deployGroup]").val();

        var extendOption = {};
        app.extendOption = extendOption;

        if ($('#openHTTPS').prop('checked')) {//开启https
            var ssl = {};
            extendOption.ssl = ssl;
            ssl.port = $(':input[name=ssl\\.port]').val();
            ssl.certType = $(':input[name=ssl\\.certType]').val();
            ssl.keyPath = $(':input[name=ssl\\.keyPath]').val();
            ssl.certPath = $(':input[name=ssl\\.certPath]').val();
        }

        var preference = {};
        extendOption.preferenceOption = preference;

        var upstreamOption = {};
        extendOption.upstreamOption = upstreamOption;
        upstreamOption.passQueryString = $('input:checkbox[name=upstreamOption\\.passQueryString]').prop('checked');
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
        upstreamOption.timeout = $('input:text[name=upstreamOption\\.timeout]').val();
        upstreamOption.retry = $('input:text[name=upstreamOption\\.retry]').val();
        upstreamOption.threadType = $('input:text[name=upstreamOption\\.threadType]').val();
        upstreamOption.maxContentLength = $('input:text[name=upstreamOption\\.maxContentLength]').val();
        upstreamOption.maxInitialLineLength = $('input:text[name=upstreamOption\\.maxInitialLineLength]').val();
        upstreamOption.maxHeaderSize = $('input:text[name=upstreamOption\\.maxHeaderSize]').val();
        upstreamOption.maxPoolSize = $("#upstreamOption_maxPoolSize").find(":input").val();
        upstreamOption.keepAlive = $('input:text[name=upstreamOption\\.keepAlive]').prop('checked');
        upstreamOption.keepAliveTimeout = $('input:text[name=upstreamOption\\.keepAliveTimeout]').val();
        upstreamOption.maxIdleTimeout = $('input:text[name=upstreamOption\\.maxIdleTimeout]').val();
        upstreamOption.maxWaitQueueSize = $('input:text[name=upstreamOption\\.maxWaitQueueSize]').val();

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

        if ($('#openStaticServer').prop('checked')) {
            preference.staticServer = true;
            preference.root = $(":input[name=docRoot]").val();
        }
        if ($('#openSession').prop('checked')) {
            var openSession = {};
            extendOption.sessionOption = openSession;
            openSession.interval = $(":input[name=openSession\\.interval]").val();
            openSession.name = $(":input[name=openSession\\.name]").val();
        }

        if ($('#openCrossDomain').prop('checked')) {
            var crossDomain = {};
            extendOption.crossDomain = crossDomain;
            crossDomain.allowedOrigin = $(":input[name=crossDomain\\.allowedOrigin]").val();
            crossDomain.allowCredentials = $(":input[name=crossDomain\\.allowCredentials]").prop('checked');
            crossDomain.maxAgeSeconds = $(":input[name=crossDomain\\.maxAgeSeconds]").val();
            crossDomain.allowedMethods = new Array();
            $(':input[name=crossDomain\\.allowedMethods]:checked').each(function () {
                crossDomain.allowedMethods.push($(this).val());
            });
            crossDomain.allowedHeaders = $(":input[name=crossDomain\\.allowedHeaders]").val().split(',');
            crossDomain.exposedHeaders = $(":input[name=crossDomain\\.exposedHeaders]").val().split(',');
        }

        if ($('#openAccessLimit').prop('checked')) {
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

        var ipBlacklist = $('#ipBlacklist').val();
        if (ipBlacklist != undefined && $.trim(ipBlacklist) != '') {
            preference.ipBlacklist = ipBlacklist.split(',');
        }

        var authInstance = $(":input[name=auth\\.instance]").val();

        if ($('#openAuth').prop('checked') && $.trim(authInstance) != '') {
            var authOption = {};
            preference.authOption = authOption;
            authOption.name = $(":input[name=auth\\.name]").val();
            authOption.instance = authInstance;
            authOption.usage = $("#auth\\.usage").text();
            var authFailReturn = {};
            authOption.failReturn = authFailReturn;
            authFailReturn.code = $(":input[name=auth\\.failReturn\\.code]").val();
            authFailReturn.contentType = $(":input[name=auth\\.failReturn\\.contentType]").val();
            authFailReturn.content = $(":input[name=auth\\.failReturn\\.content]").val();
        }

        var preHandlerInstance = $(":input[name=preHandler\\.instance]").val();
        if ($('#openPreHandler').prop('checked') && $.trim(preHandlerInstance) != '') {
            var preHandler = {};
            preference.preProcessors = new Array();
            preHandler.name = $(":input[name=preHandler\\.name]").val();
            preHandler.instance = preHandlerInstance;
            preHandler.usage = $("#preHandler\\.usage").text();
            preference.preProcessors.push(preHandler);
        }

        var postHandlerInstance = $(":input[name=postHandler\\.instance]").val();
        if ($('#openPostHandler').prop('checked') && $.trim(postHandlerInstance) != '') {
            var postHandler = {};
            preference.postProcessors = new Array();
            postHandler.name = $(":input[name=postHandler\\.name]").val();
            postHandler.instance = postHandlerInstance;
            postHandler.usage = $("#postHandler\\.usage").text();
            preference.postProcessors.push(postHandler);
        }

        var customCodes = new Array();
        var codes = $('#code_panel').find(".code-set:visible");
        codes.each(function () {
            var code = {};
            code.code = $(this).find(":input[name=code]").val();
            code.contentType = $(this).find(":input[name=contentType]").val();
            code.content = $(this).find(":input[name=content]").val();
            customCodes.push(code);
        });
        preference.customCodes = customCodes;


        $.ajax({
            url: submitURI,
            type: 'POST',
            async: true,
            dataType: 'json',
            data: JSON.stringify(app),
            success: function (data) {
                if(processData(data))
                $.Huimodalalert('应用已创建！', 2000, function () {
                    $('#waitModal').modal('hide');
                    window.location = 'index.html';
                });
            },
            error: function (err) {
                $.Huimodalalert(err.responseText, 2000);
                $('#waitModal').modal('hide');
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
                var groups = data.groups;
                var deployGroups = "";
                for (var i = 0; groups != undefined && i < groups.length; i++) {
                    var group = groups[i];
                    deployGroups += "<option value='" + group.id + "'>" + group.name + "-" + group.description + "</option>";
                }
                $('#deployGroup').html(deployGroups);

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

        $(":radio[id*=passBody_passBodyType]").click(function () {
            if ($(this).val() == 'PASS_BY_METHODS') {
                $('#passBody_methods').show();
            } else {
                $('#passBody_methods').hide();
            }
        });

        $("#crossMethodAll").click(function () {
            if (this.checked) {
                $(":checkbox[name=allowedMethods]").attr('disabled', true);
                $(":checkbox[name=allowedMethods]").attr('checked', true);
            } else {
                $(":checkbox[name=allowedMethods]").attr('checked', false);
                $(":checkbox[name=allowedMethods]").attr('disabled', false);
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
            var checked = $(this).prop('checked');
            var panel = $(this).parent().parent().parent().find('div.panel-body');
            if (checked) {
                panel.slideDown('first').end().addClass('selected');
            } else {
                panel.slideUp('first').end().removeClass('selected');
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

        appId = getParameter('id');
        if (appId != undefined) {
            $('#action_tag').text('编辑应用');
            submitURI = 'app/' + appId;
            getItem(appId);
        }
    });

</script>
</body>
</html>