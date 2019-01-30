<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-API管理</title>
    <#include "./inc.ftl" />
</head>
<body class="fixed-sidebar full-height-layout gray-bg">

<#include "./header.ftl" />

<section class="Hui-article-box">
    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">首页</a>
        <span class="c-999 en">&gt;</span><span class="c-666">节点代理</span>
        <span class="c-999 en">&gt;</span><span class="c-666" id="appName">应用</span>
        <span class="c-999 en">&gt;</span><span class="c-666">API管理</span>
    </nav>

    <footer class="footer">
        <ul class="pagination">
            <li class="gray">
                共
                <x id="totalSize">0</x>
                条记录
            </li>
        </ul>
    </footer>

    <div id="apps" class="Hui-article pd-20" style="display: block">
        <div class="check-div form-inline">
            <#--<input class="btn btn-primary-outline radius" type="button" value="添加API" onclick="createApi();"/>-->
            <#--<input class="btn btn-primary-outline radius" type="button" value="删除选中" onclick="deleteApi();"/>-->
            <#--<input class="btn btn-primary-outline radius" type="button" value="启动选中" onclick="startApi();"/>-->
            <#--<input class="btn btn-primary-outline radius" type="button" value="停止选中" onclick="stopApi();"/>-->
            <#--<input class="btn btn-primary-outline radius" type="button" value="暂停选中" onclick="pauseApi();"/>-->
            <#--<input class="btn btn-primary-outline radius" type="button" value="恢复选中" onclick="resumeApi();"/>-->
        </div>

        <div class="panel panel-default mt-20">
            <div class="panel-header" style="height: 24px">
                <#--<div class="col-xs-1">-->
                    <#--<input type="checkbox" id="selectAll"/>-->
                <#--</div>-->
                <div class="col-xs-3">
                    名称
                </div>
                <div class="col-xs-3">
                    path
                </div>
                <div class="col-xs-3 hidden-xs">
                    创建时间
                </div>
                <div class="col-xs-3">
                    状态
                </div>
            </div>
            <div class="row cl tmp_item_row pt-5" style="display: none;">
                <#--<div class="col-xs-1">-->
                    <#--<input type="checkbox" name="ids" value="%{item.id}"/>-->
                <#--</div>-->
                <div class="article-title col-xs-3">
                    %{item.name}
                </div>
                <div class="col-xs-3">
                    %{item.path}
                </div>
                <div class="col-xs-3 hidden-xs">
                    %{item.createTime}
                </div>
                <div class="article-status col-xs-3">
                    %{item.status}
                </div>
            </div>
            <div class="panel-body" id="table_items">
                暂无API
            </div>

        </div>


    </div>

</section>

</body>
<script type="text/javascript">

    var appId = getParameter('appId');
    var node = getParameter('nodeId');

    $(document).ready(function () {
        var tmpAppRow = $('div.tmp_item_row');
        var runningApis="";

        $.ajax({
            url: '${context.domain!''}/cluster/node/' + node + '/app/'+appId+'/api',
            dataType: 'json',
            type: 'GET',
            async:false,
            success: function (data) {
                if (data == undefined || data.length == 0)
                    return;
                $('#table_items').html("");
                var rows = '';
                var i = 0;
                var app = data.app;
                var apis = data.apis;
                if(app == undefined)
                    return;

                $('#appName').html("<a class='maincolor' href='${context.domain!''}/nodeApp.html?node=" + node + "'>" + app.name + "</a>");

                if(apis == undefined || apis.length == 0)
                    return;

                for (; i < apis.length; i++) {
                    var v = apis[i];
                    var row = tmpAppRow.clone();
                    var rowHtml = row.html();
                    rowHtml = rowHtml.replace(/\%\{item.id}/g, v.id);
                    rowHtml = rowHtml.replace(/\%\{item.appId}/g, v.appId);
                    rowHtml = rowHtml.replace(/\%\{item.name}/g, v.name);
                    rowHtml = rowHtml.replace(/\%\{item.path}/g, v.path);
                    rowHtml = rowHtml.replace(/\%\{item.createTime}/g, v.createTime.replace("T", ' ').replace("Z", ''));
                    var running = v.realRunning;
                    var status = '<img src="image/stop.png" title="stopped" />';
                    if(v.running){
                        status = '<img src="image/running.png" title="running" />';
                        runningApis += v.id+",";
                    }
                    else
                        status = '<img src="image/pause.png" title="pause" />';

                    rowHtml = rowHtml.replace(/\%\{item.status}/g, status);
//                    var options = new Array();
                    <#--if (running == 0){-->
                        <#--options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 1)'><i class='iconfont icon-play-circle' title='启动'></i></a>");-->
                    <#--}-->
                    <#--else if (running == 1){ //运行中-->
                        <#--options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 2)'><i class='iconfont icon-zanting' title='暂停'></i></a>");-->
                        <#--options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 0)'><i class='iconfont icon-stop' title='停止'></i></a>");-->
                        <#--options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 3)'><i class='iconfont icon-redo' title='重启'></i></a>");-->
                    <#--}-->
                    <#--else if(running == 2){-->
                        <#--options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 0)'><i class='iconfont icon-stop' title='停止'></i></a>");-->
                        <#--options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 4)'><i class='iconfont icon-bofang' title='恢复运行'></i></a>");-->
                        <#--options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 3)'><i class='iconfont icon-redo' title='重启'></i></a>");-->
                    <#--}-->

                    <#--options.push("<a href='${context.domain!''}/apiAdd.html?id=" + v.id + "' ><i class='iconfont icon-tianxie' title='编辑'></i></a>");-->
                    <#--options.push("<a href='javascript:void(0)' onclick='deleteApi(" + v.id + ")'><i class='iconfont icon-delete' title='删除'></i></a>");-->

//                    var optionHtml = "";
//                    for(var x=0; x<options.length; x ++){
//                        if( x < options.length - 1)
//                            optionHtml += options[x] + "&nbsp;&nbsp;|&nbsp;&nbsp;";
//                        else
//                            optionHtml += options[x];
//                    }

//                    rowHtml = rowHtml.replace(/\%\{item.option}/g, optionHtml);
                    row.prop('id', 'item_' + v.id);
                    if (i % 2 == 1) {
                        row.css({"background": "#f9f9f9"});
                    }
                    row.html(rowHtml);
                    row.show();
                    $('#table_items').append(row);
                }
                $('#totalSize').text(i);
            },
            error: function (err) {

            }
        });

    });
</script>
</html>