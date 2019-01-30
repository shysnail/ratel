<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-查看应用</title>
    <#include "./inc.ftl" />
    <style type="text/css">
        .panel-body {
            display: block
        }
    </style>
</head>
<body class="fixed-sidebar full-height-layout gray-bg">

<#include "./header.ftl" />

<section class="Hui-article-box" style="overflow-y: scroll">
    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">应用</a>
        <span class="c-999 en">&gt;</span><span class="c-666">查看应用</span>
    </nav>

    <div class="check-div form-inline pt-10 pl-10">
        <input class="btn btn-primary-outline radius" type="button" value="启动" id='action_start'
               onclick="appAction(1)"/>
        <input class="btn btn-primary-outline radius" type="button" value="停止" id='action_stop' onclick="appAction(0)"/>

        <input class="btn btn-primary-outline radius" type="button" value="编辑" onclick="edit();"/>
    </div>

    <form id="mainForm" class="form form-horizontal responsive" action="#" method="post">
        <div class="panel panel-default mt-20">
            <div class="panel-header">基本设置</div>
            <div class="panel-body">
                <div class="row cl">
                    <label class="form-label col-xs-3" for="name">APP名称:</label>
                    <div class="formControls col-xs-8" id="name">
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3" for="description">简介:</label>
                    <div class="formControls col-xs-8" id="description">
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">服务类型:</label>
                    <div class="formControls skin-minimal col-xs-8" id="protocol">
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">使用端口:</label>
                    <div class="formControls col-xs-8" id="port">
                    </div>
                </div>
                <div class="row cl" style="display: none;">
                    <label class="form-label col-xs-3">vhost:</label>
                    <div class="formControls col-xs-8" id="vhost">
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">发布到网关组:</label>
                    <div class="formControls col-xs-8" id="deployGroup">
                    </div>
                </div>
            </div>
        </div>

        <div class="panel panel-default mt-20" id="extend-panel">
            <div class="e-p-h panel-header">扩展设置<b style="float:right;margin-right: 10px">+</b></div>
            <div class="e-p-b panel-body">

                <div class="e-p-i panel panel-default" id="ssl_panel">
                    <div class="e-p-i-h panel-header cl">HTTPS</div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">HTTPS端口:</label>
                            <div class="formControls col-xs-2" id="ssl.port">
                            </div>
                        </div>
                        <div class="row cl" style="margin-top:0px">
                            <label class="form-label col-xs-3">证书类型:</label>
                            <div class="formControls col-xs-8" id="ssl.certType">
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">证书key路径或密码:</label>
                            <div class="formControls col-xs-8" id="ssl.keyPath">
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">证书路径:</label>
                            <div class="formControls col-xs-8" id="ssl.certPath">
                            </div>
                        </div>
                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header">反向代理服务设置<b style="float:right;margin-right: 10px">+</b></div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">超时时长:</label>
                            <div class="formControls col-xs-8" id="upstreamOption.timeout">
                            </div>
                        </div>

                        <div class="row cl">
                            <label class="form-label col-xs-3">失败重试:</label>
                            <div class="formControls col-xs-8" id="upstreamOption.retry">
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">转发url中的请求字符串:</label>
                            <div class="formControls col-xs-8" id="upstreamOption.passQueryString">
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">透传请求体:</label>
                            <div class="formControls col-xs-8" id="upstreamOption.passBody">
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">最大内容长度:</label>
                            <div class="formControls col-xs-8" id="upstreamOption.maxContentLength">
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">url最大请求参数长度:</label>
                            <div class="formControls col-xs-8" id="upstreamOption.maxInitialLineLength">
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">最大请求头:</label>
                            <p class="formControls col-xs-8" id="upstreamOption.maxHeaderSize">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">最大线程池:</label>
                        <div class="formControls col-xs-8" id="upstreamOption.maxPoolSize">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">keepalive保持:</label>
                        <div class="formControls col-xs-8" id="upstreamOption.keepAlive">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">keepalive保持时长(秒):</label>
                        <div class="formControls col-xs-8" id="upstreamOption.keepAliveTimeout">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">连接最大闲置时长:</label>
                        <div class="formControls col-xs-8" id="upstreamOption.maxIdleTimeout">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">拥堵时等待队列最大大小:</label>
                        <div class="formControls col-xs-8" id="upstreamOption.maxWaitQueueSize">
                        </div>
                    </div>
                </div>
            </div>

            <div class="e-p-i panel panel-default mt-20" id="header_panel">
                <div class="e-p-i-h panel-header">请求头设定</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl">
                        <label class="form-label col-xs-3">附加的请求头:</label>
                        <div class="formControls col-xs-8" id="upstreamOption.appendHeaders">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">移除的请求头:</label>
                        <div class="formControls col-xs-8" id="upstreamOption.removeHeaders">
                        </div>
                    </div>
                </div>
            </div>

            <div class="e-p-i panel panel-default mt-20" id="staticServer">
                <div class="panel-header cl">静态WEB服务</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl">
                        <label class="form-label col-xs-3">静态网页根目录:</label>
                        <div class="formControls col-xs-8" id="docRoot">
                        </div>
                    </div>
                </div>
            </div>

            <div class="e-p-i panel panel-default mt-20" id="sessionOption_panel">
                <div class="e-p-i-h panel-header cl">SESSION</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl">
                        <label class="form-label col-xs-3">interval:</label>
                        <div class="formControls col-xs-8" id="openSession.interval">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">SessionName:</label>
                        <div class="formControls col-xs-8" id="openSession.name">
                        </div>
                    </div>
                </div>
            </div>

            <div class="e-p-i panel panel-default mt-20" id="crossDomain_panel">
                <div class="e-p-i-h panel-header cl">跨域</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl">
                        <label class="form-label col-xs-3">允许请求源:</label>
                        <div class="formControls col-xs-8" id="crossDomain.allowedOrigin">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">允许的请求METHOD:</label>
                        <div class="formControls skin-minimal col-xs-8" id="crossDomain.allowedMethods">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">最大缓存时长(秒):</label>
                        <div class="formControls col-xs-8" id="crossDomain.maxAgeSeconds">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">允许发送cookie:</label>
                        <div class="formControls col-xs-8" id="crossDomain.allowCredentials">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">允许的headers:</label>
                        <div class="formControls col-xs-8" id="crossDomain.allowedHeaders">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">暴露的headers:</label>
                        <div class="formControls col-xs-8" id="crossDomain.exposedHeaders">
                        </div>
                    </div>

                </div>
            </div>

            <div class="e-p-i panel panel-default mt-20" id="limit_panel">
                <div class="panel-header cl">流控</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl">
                        <label class="form-label col-xs-3">统计周期:</label>
                        <div class="formControls col-xs-8 text-l" id="limit.interval">

                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">总数:</label>
                        <div class="formControls col-xs-8" id="limit.limit">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">每IP:</label>
                        <div class="formControls col-xs-8" id="limit.limitPerIp">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">每用户:</label>
                        <div class="formControls col-xs-8" id="limit.limitPerClient">
                        </div>
                    </div>

                    <div class="row cl">
                        <label class="form-label col-xs-3">超出返回</label>
                        <div class="formControls col-xs-8" id="limit.overloadedReturn">&nbsp;
                        </div>

                    </div>

                </div>
            </div>

            <!--<div class="e-p-i panel panel-default mt-20">-->
            <!--<div class="e-p-i-h panel-header cl">-->
            <!--<label class="col-xs-3 text-l pl-5">熔断策略</label>-->
            <!--<div class="col-xs-2"><input type="checkbox" id="fusing" name="fusing" value="1"> <b class='openOrClose'>启用</b></div>-->
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
            <!--<div class="col-xs-2"><input type="checkbox" id="openCache" name="openCache" value="1"> <b class='openOrClose'>启用</b></div>-->
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

            <div class="e-p-i panel panel-default mt-20" id="ipbl_panel">
                <div class="e-p-i-h panel-header">IP黑名单</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl">
                        <label class="form-label col-xs-3">ip:</label>
                        <div class="formControls col-xs-8" id="ipBlacklist">
                        </div>
                    </div>
                </div>
            </div>

            <div class="e-p-i panel panel-default mt-20" id="auth_panel">
                <div class="e-p-i-h panel-header cl">租户认证</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl">
                        <label class="form-label col-xs-3">名称:</label>
                        <div class="formControls col-xs-8" id="auth.name">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">实现类:</label>
                        <div class="formControls col-xs-8" id="auth.instance">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">用法:</label>
                        <div class="formControls col-xs-8" id="auth.usage">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">认证不通过返回</label>
                        <div class="formControls col-xs-8" id="auth.failReturn">&nbsp;
                        </div>

                    </div>

                </div>
            </div>

            <div class="e-p-i panel panel-default mt-20" id="preHandler_panel">
                <div class="e-p-i-h panel-header">前置处理器</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl">
                        <label class="form-label col-xs-3">名称:</label>
                        <div class="formControls col-xs-8" id="preHandler.name">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">实现类:</label>
                        <div class="formControls col-xs-8" id="preHandler.instance">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">用法:</label>
                        <div class="formControls col-xs-8" id="preHandler.usage">
                        </div>
                    </div>
                </div>
            </div>

            <div class="e-p-i panel panel-default mt-20" id="postHandler_panel">
                <div class="e-p-i-h panel-header">后置处理器</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl">
                        <label class="form-label col-xs-3">名称:</label>
                        <div class="formControls col-xs-8" id="postHandler.name">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">实现类:</label>
                        <div class="formControls col-xs-8" id="postHandler.instance">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">用法:</label>
                        <div class="formControls col-xs-8" id="postHandler.usage">
                        </div>
                    </div>
                </div>
            </div>

            <div class="e-p-i panel panel-default mt-20" id="code_panel">
                <div class="e-p-i-h panel-header">响应状态码定义</div>
                <div class="e-p-i-b panel-body">
                    <div class="row cl code" style="display: none">
                        <label class="form-label col-xs-3">状态码:</label>
                        <div class="formControls col-xs-8" name="code">

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

<script type="text/javascript">
    var codeLine = $('#code_panel').find(".code:first");
    function addCode(code) {
        var newCodeLine = codeLine.clone();
        if (code) {
            $(newCodeLine).find("div[name=code]").html(code.code + " &nbsp; " + code.contentType + " &nbsp; <br/>" + code.content);
        }
        $(newCodeLine).appendTo($('#code_panel'));
        $(newCodeLine).show();
    }
    var id;
    function edit() {
        window.location = 'appAdd.html?id=' + id;
    }

    function appAction(action) {
        var url = "${context.domain!''}/app/" + id;
        if (action == 0)
            url += "/stop";
        else if(action == 1)
            url += "/start";
        else if(action == 2)
            url += "/pause";
        else if(action == 3)
            url += "/restart";
        else if(action == 4)
            url += "/resume";
        $.ajax({
            url: url,
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                if(processData(data))
                $.Huimodalalert('应用已' + (action == 0 ? '停止' : '启动'), 1000, function () {
                    window.location = window.location;
                });
            },
            error: function (err) {

            }
        });
    }

    function getAPP(id) {
        $.ajax({
            url: "${context.domain!''}/app/" + id,
            type: 'GET',
            dataType: 'json',
            async: false,
            success: function (app) {
                $('#name').text(app.name);
                $('#description').text(app.description);
                $('#protocol').text(app.protocol);
                $('#vhost').text(app.vhost);
                $('#port').text(app.port);
                $('#deployGroup').text(app.deployGroup);
                var running = app.running;
                if (running == 0) {
                    $('#action_start').show();
                    $('#action_stop').hide();
                }else if(running == 1){
                    $('#action_start').hide();
                    $('#action_stop').show();
                }
                var extendOption = JSON.parse(app.parameter);

                var ssl = extendOption.ssl;
                if (ssl == undefined) {
                    $('#ssl_panel').hide();
                } else {
                    $('#ssl\\.port').text(ssl.port);
                    $('#ssl\\.certType').text(ssl.certType);
                    $('#ssl\\.keyPath').text(ssl.keyPath);
                    $('#ssl\\.certPath').text(ssl.certPath);
                }

                var upstreamOption = extendOption.upstreamOption;
                $('#upstreamOption\\.timeout').text(upstreamOption.timeout + '毫秒');
                $('#upstreamOption\\.retry').text(upstreamOption.retry + '次');
                $('#upstreamOption\\.passQueryString').text(upstreamOption.passQueryString ? '是' : '否');
                var passBody = upstreamOption.passBody;
                var passBodyStr = '';
                if ("ALL_PASS" == passBody.passBodyType) {
                    passBodyStr = "透传所有";
                } else if ("ALL_HOLD" == passBody.passBodyType) {
                    passBodyStr = "不透传所有";
                } else if ("PASS_BY_METHODS" == passBody.passBodyType) {
                    passBodyStr = "透传指定方法请求体。";
                    passBodyStr += "方法：" + passBody.option;
                }
                $('#upstreamOption\\.passBody').text(passBodyStr);
                $('#upstreamOption\\.maxContentLength').text(upstreamOption.maxContentLength < 0 ? '不限' : upstreamOption.maxContentLength);
                $('#upstreamOption\\.maxInitialLineLength').text(upstreamOption.maxInitialLineLength < 0 ? '不限' : upstreamOption.maxInitialLineLength);
                $('#upstreamOption\\.maxHeaderSize').text(upstreamOption.maxHeaderSize < 0 ? '不限' : upstreamOption.maxHeaderSize);
                $('#upstreamOption\\.maxPoolSize').text(upstreamOption.maxPoolSize);
                $('#upstreamOption\\.keepAlive').text(upstreamOption.keepAlive ? ('是，保持' + upstreamOption.keepAliveTimeout + '秒' ) : '否');
                $('#upstreamOption\\.maxIdleTimeout').text(upstreamOption.maxIdleTimeout + '秒');
                $('#upstreamOption\\.maxWaitQueueSize').text(upstreamOption.maxWaitQueueSize);

                var appendHeaders = upstreamOption.appendHeaders;
                var appendHeaderStr = '';
                for (var key in appendHeaders) {
                    appendHeaderStr += (key + "=" + appendHeaders[key] + ',&nbsp;&nbsp;');
                }
                var removeHeaders = upstreamOption.removeHeaders;
                if ($.trim(appendHeaderStr) == '' && (removeHeaders == undefined || $.trim(removeHeaders) == '')) {
                    $('#header_panel').hide();
                } else {
                    $('#upstreamOption\\.appendHeaders').html(appendHeaderStr);
                    $('#upstreamOption\\.removeHeaders').text(removeHeaders);
                }
                var sessionOption = extendOption.sessionOption;
                if (sessionOption == undefined) {
                    $('#sessionOption_panel').hide();
                } else {
                    $('#openSession\\.interval').text(sessionOption.interval + '秒');
                    $('#openSession\\.name').text(sessionOption.name);
                }

                var preferenceOption = extendOption.preferenceOption;

                var crossDomain = extendOption.crossDomain;
                if (crossDomain == undefined) {
                    $('#crossDomain_panel').hide();
                } else {
                    $('#crossDomain\\.allowedOrigin').text(crossDomain.allowedOrigin);
                    $('#crossDomain\\.allowedMethods').text(crossDomain.allowedMethods);
                    $('#crossDomain\\.maxAgeSeconds').text(crossDomain.maxAgeSeconds);
                    $('#crossDomain\\.allowCredentials').text(crossDomain.allowCredentials);
                    $('#crossDomain\\.allowedHeaders').text(crossDomain.allowedHeaders);
                    $('#crossDomain\\.exposedHeaders').text(crossDomain.exposedHeaders);
                }

                var root = preferenceOption.root;
                if (root != undefined && $.trim(root).length > 0) {
                    $('#docRoot').text(root);
                } else {
                    $('#staticServer').hide();
                }

                var ipBlackList = preferenceOption.ipBlacklist;
                if (ipBlackList != undefined && ipBlackList.length > 0) {
                    $('#ipBlackList').text(ipBlackList);
                } else {
                    $('#ipbl_panel').hide();
                }

                var limit = preferenceOption.accessLimitOption;
                if (limit != undefined) {
                    $('#limit\\.interval').text("每" + limit.interval + limit.timeUnit);
                    $('#limit\\.limit').text(limit.limit < 0 ? '不限' : limit.limit);
                    $('#limit\\.limitPerIp').text(limit.limitPerIp < 0 ? '不限' : limit.limitPerIp);
                    var limitClient = ''
                    if (limit.limitPerClient < 0) {
                        limitClient = '不限'
                    } else {
                        limitClient = limit.limitPerClient;
                        limitClient += ",用户标识：" + limit.keys;
                    }
                    $('#limit\\.limitPerClient').text(limitClient);

                    var overloadedReturn = '';
                    overloadedReturn = 'code:' + limit.overloadedReturn.code + ",Content-Type:" + limit.overloadedReturn.contentType + ", 返回信息:" + limit.overloadedReturn.content;
                    $('#limit\\.overloadedReturn').text(overloadedReturn);
                } else {
                    $('#limit_panel').hide();
                }

                var auth = preferenceOption.authOption;
                if (auth != undefined) {
                    $("#auth\\.name").text(auth.name);
                    $("#auth\\.instance").text(auth.instance);
                    $("#auth\\.usage").text(auth.usage);
                    var failReturn = '';
                    failReturn = 'code:' + auth.failReturn.code + ",Content-Type:" + auth.failReturn.contentType + ", 返回信息:" + auth.failReturn.content;
                    $("#auth\\.failReturn").text(failReturn);
                } else {
                    $('#auth_panel').hide();
                }

                var preHandlers = preferenceOption.preProcessors;
                if (preHandlers != undefined && preHandlers.length > 0) {
                    var preHandler = preHandlers[0];
                    $("#preHandler\\.name").text(preHandler.name);
                    $("#preHandler\\.instance").text(preHandler.instance);
                    $("#preHandler\\.usage").text(preHandler.usage);
                } else {
                    $('#preHandler_panel').hide();
                }

                var postHandlers = preferenceOption.postProcessors;
                if (postHandlers != undefined && postHandlers.length > 0) {
                    var postHandler = postHandlers[0];
                    $("#postHandler\\.name").text(postHandler.name);
                    $("#postHandler\\.instance").text(postHandler.instance);
                    $("#postHandler\\.usage").text(postHandler.usage);
                } else {
                    $('#postHandler_panel').hide();
                }

                var customCodes = preferenceOption.customCodes;
                if (customCodes && customCodes.length > 0) {
                    for (var i = 0; customCodes && i < customCodes.length; i++) {
                        addCode(customCodes[i]);
                    }
                    $('#code_panel').show();
                }
            },
            error: function (err) {

            }
        });
    }
    $(document).ready(function () {
        id = getParameter('id');
        getAPP(id);
    });
</script>
</body>
</html>