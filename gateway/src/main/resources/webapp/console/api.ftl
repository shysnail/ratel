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
            <input class="btn btn-primary-outline radius" type="button" value="添加API" onclick="createApi();"/>
            <input class="btn btn-primary-outline radius" type="button" value="删除选中" onclick="deleteApi();"/>
            <input class="btn btn-primary-outline radius" type="button" value="启动选中" onclick="startApi();"/>
            <input class="btn btn-primary-outline radius" type="button" value="停止选中" onclick="stopApi();"/>
            <#--<input class="btn btn-primary-outline radius" type="button" value="暂停选中" onclick="pauseApi();"/>-->
            <#--<input class="btn btn-primary-outline radius" type="button" value="恢复选中" onclick="resumeApi();"/>-->
        </div>

        <div class="panel panel-default mt-20">
            <div class="panel-header" style="height: 24px">
                <div class="col-xs-1">
                    <input type="checkbox" id="selectAll"/>
                </div>
                <div class="col-xs-2">
                    名称
                </div>
                <div class="col-xs-2">
                    path
                </div>
                <div class="col-xs-2 hidden-xs">
                    创建时间
                </div>
                <div class="col-xs-1">
                    状态
                </div>
                <div class="col-xs-4 hidden-xs">
                    操作
                </div>
                <div class="col-xs-5 visible-xs">
                    操作
                </div>
            </div>
            <div class="row cl tmp_item_row pt-5" style="display: none;">
                <div class="col-xs-1">
                    <input type="checkbox" name="ids" value="%{item.id}"/>
                </div>
                <div class="article-title col-xs-2">
                    <a href="apiView.html?appId=%{item.appId}&id=%{item.id}">%{item.name}</a>
                </div>
                <div class="col-xs-2">
                    %{item.path}
                </div>
                <div class="col-xs-2 hidden-xs">
                    %{item.createTime}
                </div>
                <div class="article-status col-xs-1">
                    %{item.status}
                </div>
                <div class="col-xs-4 hidden-xs">
                    %{item.option}
                </div>
                <div class="col-xs-5 visible-xs">
                    %{item.option}
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

    function viewApi(id) {
        window.location = '${context.domain!''}/apiView.html?appId=' + appId + "&id=" + id;
    }

    function createApi() {
        window.location = '${context.domain!''}/apiAdd.html?appId=' + appId;
    }

    function selectIds() {
        var ids = '';
        $('#table_items').find(':checkbox[name=ids]:checked').each(function () {
            ids += $(this).val() + ",";
        });
        if (ids.length > 0 && ids.charAt(ids.length - 1) == ',')
            ids = ids.substring(0, ids.length - 1);
        return ids;
    }

    function deleteApi(id) {
        if (id == undefined) {
            id = selectIds();
        }
        $.ajax({
            url: '${context.domain!''}/app/' + appId + '/api/' + id,
            type: 'DELETE',
            dataType: 'json',
            success: function (data) {
                if(processData(data))
                $.Huimodalalert(data.data, 1000, function () {
                    if(data.success){
                        window.location = window.location;
                    }
                });
            },
            error: function (err) {

            }
        });
    }

    function startApi() {
        apiAction(selectIds(), 1);
    }

    function stopApi(id) {
        apiAction(selectIds(), 0);
    }

    function pauseApi(id) {
        apiAction(selectIds(), 2);
    }

    function resumeApi(id) {
        apiAction(selectIds(), 4);
    }

    function apiAction(id, action) {
        if(id==undefined){
            $.Huimodalalert('请选择需要操作的api', 1000);
            return;
        }
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
                $.Huimodalalert(data.data, 1000, function () {
                    if(data.success){
                        window.location = window.location;
                    }
                });
            },
            error: function (err) {

            }
        });
    }

    $(document).ready(function () {

        $('#selectAll').click(function () {
            if ($(this).prop('checked')) {
                $(':checkbox[name=ids]').each(function () {
                    $(this).prop("checked", true);
                })
            } else {
                $(':checkbox[name=ids]').each(function () {
                    $(this).prop("checked", false);
                })
            }
        });

        var tmpAppRow = $('div.tmp_item_row');
        var runningApis="";

        $.ajax({
            url: '${context.domain!''}/app/' + appId + '/api',
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

                $('#appName').html("<a class='maincolor' href='${context.domain!''}/appView.html?id="+appId+"'>" + app.name + "</a>");

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
                    rowHtml = rowHtml.replace(/\%\{item.createTime}/g, v.createTime ? v.createTime.replace("T", ' ').replace("Z", '') : "");
                    var running = v.running;
                    var status = '<img src="image/stop.png" title="stopped" />';
                    if(v.running == 1){
                        status = '<img src="image/running.png" title="running" />';
                        runningApis += v.id+",";
                    }
                    else if(v.running == 2)
                        status = '<img src="image/pause.png" title="pause" />';

                    rowHtml = rowHtml.replace(/\%\{item.status}/g, status);
                    var options = new Array();
                    if (running == 0){
                        options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 1)'><i class='iconfont icon-play-circle' title='启动'></i></a>");
                    }
                    else if (running == 1){ //运行中
                        options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 2)'><i class='iconfont icon-zanting' title='暂停'></i></a>");
                        options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 0)'><i class='iconfont icon-stop' title='停止'></i></a>");
                        options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 3)'><i class='iconfont icon-redo' title='重启'></i></a>");
                    }
                    else if(running == 2){
                        options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 0)'><i class='iconfont icon-stop' title='停止'></i></a>");
                        options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 4)'><i class='iconfont icon-bofang' title='恢复运行'></i></a>");
                        options.push("<a href='javascript:void(0)' onclick='apiAction(" + v.id + ", 3)'><i class='iconfont icon-redo' title='重启'></i></a>");
                    }

                    options.push("<a href='${context.domain!''}/apiAdd.html?appId="+v.appId+"&id=" + v.id + "' ><i class='iconfont icon-tianxie' title='编辑'></i></a>");
                    options.push("<a href='javascript:void(0)' onclick='deleteApi(" + v.id + ")'><i class='iconfont icon-delete' title='删除'></i></a>");

                    var optionHtml = "";
                    for(var x=0; x<options.length; x ++){
                        if( x < options.length - 1)
                            optionHtml += options[x] + "&nbsp;&nbsp;|&nbsp;&nbsp;";
                        else
                            optionHtml += options[x];
                    }

                    rowHtml = rowHtml.replace(/\%\{item.option}/g, optionHtml);
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

        if(runningApis == "")
            return;

        runningApis = runningApis.substring(0, runningApis.length-1);
        $.ajax({
            url:'${context.domain!''}/app/'+appId+'/api/status/onNode?&apiIds='+runningApis+'&r='+Math.random(),
            type:'get',
            dataType:'json',
            success:function (data) {
                if(data.success){
                    var unhealthy=data.data;
                    for(var appId in unhealthy){//遍历json对象的每个key/value对,p为key
                        var unHealthNodes = unhealthy[appId];
                        if(unHealthNodes.length == 0)
                            continue;

                        var statusCell = $('#item_'+appId).find(".article-status")[0];
                        var nodesHtml = "";
                        for (var i=0; i < unHealthNodes.length; i++) {
                            nodesHtml+= "<a class='warning' href='"+unHealthNodes[i].nodeId+"'>"+unHealthNodes[i].hostname+"</a></br>";
                        }
                        var warn="<img id='warn_"+appId+"' style='margin-left:0.5rem;' src='image/warning.png' title='存在节点状态异常' data-toggle='popover' data-placement='bottom' />";
                        $(warn).appendTo(statusCell);
                        $('#warn_'+appId).attr("data-content", nodesHtml);
                        $('#warn_'+appId).popover({
                            html:true
                        });
                    }

                }else{//标记无法获取集群运行状态
                    var warn="<img style='margin-left:0.5rem;' src='image/warning1.png' title='无法获取节点状态' />";
                    var appIds=runningApps.split(",");
                    for(var i = 0; i < appIds.length; i ++){
                        var statusCell = $('#'+appId).find(".article-status");
                        $(warn).appendTo(statusCell);
                    }
                }
            },
            error:function (err) {

            }
        });

    });
</script>
</html>