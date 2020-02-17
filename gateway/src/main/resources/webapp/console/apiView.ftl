<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-查看API</title>
    <#include "./inc.ftl" />
    <style type="text/css">
        .panel-body {
            display: block
        }
        .hide{
            display: none;
        }
    </style>
</head>
<body class="fixed-sidebar full-height-layout gray-bg">

<#include "./header.ftl" />

<section class="Hui-article-box" style="overflow-y: scroll">
    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">应用</a>
        <span class="c-gray en">&gt;</span>
        <a href="${context.domain!''}/appView.html?id=%{app.id}" id="curApp"></a>
        <span class="c-999 en">&gt;</span><span class="c-666">查看API</span>
    </nav>

    <div class="check-div form-inline pt-10 pl-10">
        <input class="btn btn-primary-outline radius hide" type="button" value="启动" id='action_start' onclick="apiAction(1)"/>
        <input class="btn btn-primary-outline radius hide" type="button" value="停止" id='action_stop' onclick="apiAction(0)"/>
        <input class="btn btn-primary-outline radius hide" type="button" value="重启" id='action_restart' onclick="appAction(3)"/>
        <input class="btn btn-primary-outline radius hide" type="button" value="暂停" id='action_pause' onclick="appAction(2)"/>
        <input class="btn btn-primary-outline radius hide" type="button" value="恢复运行" id='action_resume' onclick="appAction(4)"/>
        <input class="btn btn-primary-outline radius" type="button" value="编辑" onclick="edit();"/>
    </div>

    <form id="mainForm" class="form form-horizontal responsive" action="#" method="post">
        <div class="panel panel-default mt-20">
            <div class="panel-header">基本设置</div>
            <div class="panel-body">
                <div class="row cl">
                    <label class="form-label col-xs-3" for="name">名称:</label>
                    <div class="formControls col-xs-8" id="name">
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3" for="path">path:</label>
                    <div class="formControls col-xs-8" id="path">
                    </div>
                </div>
                <div class="row cl" style="display: none;">
                    <label class="form-label col-xs-3">vhost:</label>
                    <div class="formControls col-xs-8" id="vhost">
                    </div>
                </div>

                <div class="row cl">
                    <label class="form-label col-xs-3">允许的请求METHOD:</label>
                    <div class="formControls skin-minimal col-xs-8" id="methodAll">

                    </div>
                </div>

                <div class="e-p-i panel panel-default mt-20">
                    <div class="e-p-i-h panel-header cl">服务类型设定<b style="float:right;margin-right: 10px">+</b></div>
                    <div class="e-p-i-b panel-body">
                        <div class="row cl">
                            <label class="form-label col-xs-3">服务类型:</label>
                            <div class="formControls col-xs-8" id="proxyType">

                            </div>
                        </div>

                        <div class="row cl proxy_upstream">
                            <label class="form-label col-xs-3">api目标服务方法:</label>
                            <div class="formControls col-xs-8" id="upstreamOption.methodForward">

                            </div>

                        </div>
                        <div class="row cl proxy_upstream">
                            <label class="form-label col-xs-3">负载策略:</label>
                            <div class="formControls col-xs-3">
                                <select id="upstreamOption.proxyPolicy" size="1" class="select-box" readonly>
                                    <option value="RANDOM">随机</option>
                                    <option value="POLLING_AVAILABLE">权重轮训</option>
                                    <option value="IP_HASH">IP分配</option>
                                    <option value="LEAST_ACTIVE">最小活跃数(暂不可用)</option>
                                </select>(目标url存在多个时，策略生效)
                            </div>
                            <div class="formControls col-xs-5 text-r">

                            </div>
                        </div>

                        <div class="row cl proxy_upstream">
                            <label class="form-label col-xs-3">负载均衡目标:</label>
                            <div class="formControls col-xs-9" id="upstreamOption.targets">

                            </div>
                        </div>

                        <div class="row cl proxy_upstream">
                            <label class="form-label col-xs-3">转发线程池类型:</label>
                            <div class="formControls col-xs-8" id="upstreamOption.threadType">

                            </div>
                        </div>

                        <div class="row cl proxyoption" id="proxyOption_REDIRECT" style="display: none;">
                            <div class="row cl">
                                <label class="form-label col-xs-3">跳转到:</label>
                                <div class="formControls col-xs-8" id="redirectOption.url">
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">附加请求字符串:</label>
                                <div class="formControls col-xs-8" id="redirectOption.passQueryString">
                                </div>
                            </div>
                        </div>
                        <div class="row cl proxyoption" id="proxyOption_ECHO" style="display: none;">
                            <div class="row cl">
                                <label class="form-label col-xs-3">返回码:</label>
                                <div class="formControls col-xs-8" id="echoOption.echo.code">
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">信息类型:</label>
                                <div class="formControls col-xs-8" id="echoOption.echo.contentType">
                                </div>
                            </div>
                            <div class="row cl">
                                <label class="form-label col-xs-3">内容:</label>
                                <div class="formControls col-xs-8" id="echoOption.echo.content">

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
                    <div class="e-p-i-h panel-header">反向代理服务设置</div>
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
                            <div class="formControls col-xs-8" id="upstreamOption.maxHeaderSize">
                            </div>
                        </div>
                        <div class="row cl">
                            <label class="form-label col-xs-3">keepalive保持:</label>
                            <div class="formControls col-xs-8" id="upstreamOption.keepAlive">
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
                    <div class="e-p-i-b panel-body" id="proxyHeaders">
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
                <!--<div class="e-p-i-h panel-header cl">熔断策略</div>-->
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
                <!--<div class="e-p-i-h panel-header cl">缓存</div>-->
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

            </div>

        </div>
    </form>

    <div class="row cl mt-30 mb-40 text-c">
        <#--<button class="btn btn-primary radius" type="button" onclick="submit();">提 交</button>-->
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
    var id = getParameter('id');

    function apiAction(action) {
        var url = "${context.domain!''}/app/" + appId + "/api/" + id;
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
            async: false,
            success: function (data) {
                if(processData(data))
                $.Huimodalalert('API已' + (action == 0 ? '停止' : '启动'), 2000, function () {
                    window.location.reload();
                });
            },
            error: function (err) {

            }
        });
    }
    function edit() {
        window.location = 'apiAdd.html?appId=' + appId + '&id=' + id;
    }

    function getAPI(appId, id) {
        $.ajax({
            url: "${context.domain!''}/app/" + appId + "/api/" + id,
            type: 'GET',
            dataType: 'json',
            async: false,
            success: function (api) {
                $('#name').text(api.name);
                $('#path').text(api.path);
                $('#vhost').text(api.vhost);

                var running = api.running;

                if (running == 0) {
                    $('#action_start').show();
                } else if(running == 1){
                    $('#action_stop').show();
                    $('#action_pause').show();
                    $('#action_restart').show();
                }else if(running == 2){
                    $('#action_resume').show();
                    $('#action_stop').show();
                    $('#action_restart').show();
                }

                var parameter = JSON.parse(api.parameter);

                var preferenceOption = parameter.preferenceOption;
                $('#methodAll').text(preferenceOption.method == undefined ? '所有' : preferenceOption.method);

                var upstreamOption = parameter.upstreamOption;
                var redirectOption = parameter.redirectOption;
                var echoOption = parameter.echoOption;

                var proxyType = '';
                if (upstreamOption != undefined) {
                    proxyType = upstreamOption.proxyType;
                    $('#proxyType').text('反响代理请求');

                    $('#upstreamOption\\.methodForward').text(upstreamOption.methodForward == undefined ? '使用请求的方法' : upstreamOption.methodForward);
                    $('#upstreamOption\\.proxyPolicy').val(upstreamOption.loadBalance);
                    $('#upstreamOption\\.proxyPolicy').change();

                    var targets = "";
                    for (var i = 0; i < upstreamOption.targets.length; i++) {
                        var t = upstreamOption.targets[i];
                        targets += "SERVER: " + t.url + " 权重：" + t.weight + "<br/>"
                    }

                    $('#upstreamOption\\.targets').html(targets);
                    var threadType = upstreamOption.threadType == 'APP' ? ('整个应用公用线程池') : ('接口自用线程池');
                    if(upstreamOption.threadType != 'APP')
                        threadType += '&nbsp;&nbsp;大小:' + (upstreamOption.maxPoolSize ? upstreamOption.maxPoolSize : "不限");
                    $('#upstreamOption\\.threadType').html(threadType);

                    $('#proxyOption_REDIRECT').hide();
                    $('#proxyOption_ECHO').hide();
                    $('div.proxy_upstream').show();

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
                    $('#upstreamOption\\.keepAlive').text(upstreamOption.keepAlive ? ('是，最大保持' + upstreamOption.keepAliveTimeout + '秒' ) : '否');
                    $('#upstreamOption\\.maxIdleTimeout').text(upstreamOption.maxIdleTimeout?(upstreamOption.maxIdleTimeout + '秒') : "不清理闲置连接");
                    $('#upstreamOption\\.maxWaitQueueSize').text(upstreamOption.maxWaitQueueSize);

                    $('#upstream_panel').show();

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

                } else if (redirectOption != undefined) {
                    proxyType = proxyType.proxyType;
                    $('#proxyType').text('重定向');
                    $('#redirectOption\\.url').text(redirectOption.url);
                    $('#redirectOption\\.passQueryString').text(redirectOption.passQueryString ? '是' : '否');
                    $('#upstream_panel').hide();
                    $('#header_panel').hide();
                    $('#proxyOption_ECHO').hide();
                    $('div.proxy_upstream').hide();
                    $('#proxyOption_REDIRECT').show();
                } else if (echoOption != undefined) {
                    proxyType = echoOption.proxyType;
                    $('#proxyType').text('直接返回信息');
                    var echo = echoOption.echo;
                    $('#echoOption\\.echo\\.code').text(echo.code);
                    $('#echoOption\\.echo\\.contentType').text(echo.contentType);
                    $('#echoOption\\.echo\\.content').text(echo.content);

                    $('#header_panel').hide();
                    $('#upstream_panel').hide();
                    $('#proxyOption_REDIRECT').hide();
                    $('#proxyOption_ECHO').show();
                    $('div.proxy_upstream').hide();
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
                    $('#limit\\.interval').text("每" + limit.interval + ' ' + limit.timeUnit);
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
                    overloadedReturn = 'code:' + limit.overloadedReturn.code + ",Content-Type:" + limit.overloadedReturn.contentType + ",content:" + limit.overloadedReturn.content;
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

                var preHandlers = preferenceOption.preHttpProcessors;
                if (preHandlers != undefined && preHandlers.length > 0) {
                    var preHandler = preHandlers[0];
                    $("#preHandler\\.name").text(preHandler.name);
                    $("#preHandler\\.instance").text(preHandler.instance);
                    $("#preHandler\\.usage").text(preHandler.usage);
                } else {
                    $('#preHandler_panel').hide();
                }

                var postHandlers = preferenceOption.postHttpProcessors;
                if (postHandlers != undefined && postHandlers.length > 0) {
                    var postHandler = postHandlers[0];
                    $("#postHandler\\.name").text(postHandler.name);
                    $("#postHandler\\.instance").text(postHandler.instance);
                    $("#postHandler\\.usage").text(postHandler.usage);
                } else {
                    $('#postHandler_panel').hide();
                }

            },
            error: function (err) {

            }
        });
    }

    $(function () {
        getAPI(appId, id);
    });

</script>
</body>
</html>