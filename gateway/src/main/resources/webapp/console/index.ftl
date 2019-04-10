<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-首页</title>
    <#include "./inc.ftl" />
</head>
<body class="fixed-sidebar full-height-layout gray-bg">
<#include "./header.ftl" />
<section class="Hui-article-box">
    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="/">首页</a>
        <span class="c-999 en">&gt;</span><span class="c-666">应用清单</span>
    </nav>

    <div class="footer cl">
        <ul class="pagination">
            <li class="gray">
                共
                <x id="totalSize">0</x>
                条记录
            </li>
        </ul>
    </div>

    <div id="apps" class="Hui-article pd-20">
        <div class="check-div form-inline">
            <input class="btn btn-primary-outline radius" type="button" value="添加应用"
                   onclick="window.location='${context.domain!''}/appAdd.html';"/>
        </div>

        <div class="panel panel-default mt-20">
            <div class="panel-header cl">
                <div class="formControls col-xs-2">
                    名称
                </div>
                <div class="col-xs-3">
                    描述
                </div>
                <div class="col-xs-2 hidden-xs">
                    创建时间
                </div>
                <div class="col-xs-1">
                    状态
                </div>
                <div class="col-xs-1">
                    节点
                </div>
                <div class="col-xs-3 hidden-xs">
                    操作
                </div>
                <div class="col-xs-5 visible-xs">
                    操作
                </div>
            </div>
            <div class="row cl tmp_item_row pt-5" style="display: none">
                <div class="article-title col-xs-2">
                    <a href="${context.domain!''}/appView.html?id=%{item.id}">%{item.port}:%{item.name}</a>
                </div>
                <div class="col-xs-3">
                    %{item.description}
                </div>
                <div class="col-xs-2 hidden-xs">
                    %{item.createTime}
                </div>
                <div class="article-status col-xs-1">
                    %{item.status}
                </div>
                <div class="col-xs-1">
                    %{item.group}
                    <!--%{item.nodes}-->
                </div>
                <div class="col-xs-3 hidden-xs">
                    %{item.option}
                </div>
                <div class="col-xs-5 visible-xs">
                    %{item.option}
                </div>
            </div>
            <div class="panel-body" id="table_items">
                暂无应用
            </div>

        </div>


    </div>

</section>

</body>
<script type="text/javascript">

    function deleteApp(id) {
        if (!confirm("删除后不可恢复，点击取消终止操作，点击确定继续删除")) {
            return;
        }
        $.ajax({
            url: '${context.domain!''}/app/' + id,
            type: 'DELETE',
            dataType: 'json',
            success: function (data) {
                if(processData(data))
                $.Huimodalalert('应用已删除！', 1000, function () {
                    window.location = window.location;
                });
            },
            error: function (err) {

            }
        });
    }

    function appAction(id, action) {
        var url = "${context.domain!''}/app/" + id;
        if (action == 0)
            url += "/stop";
        else if(action == 2)
            url += "/pause";
        else if(action == 1){
            url += "/start";
        }else if(action == 3){
            url += "/restart";
        }else if(action == 4){
            url += "/resume";
        }
        $.ajax({
            url: url,
            type: 'GET',
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

    var tmpAppRow = $('div.tmp_item_row');
    function apps() {
        var runningApps="";
        $.ajax({
            url: '${context.domain!''}/app',
            dataType: 'json',
            type: 'GET',
            async:false,
            success: function (data) {
                if (data == undefined || data.length == 0)
                    return;
                $('#table_items').html("");
                var rows = '';
                var i = 0;
                for (; i < data.length; i++) {
                    var v = data[i];
                    var row = tmpAppRow.clone();

                    var rowHtml = row.html();
                    rowHtml = rowHtml.replace(/\%\{item.id}/g, v.id);
                    rowHtml = rowHtml.replace(/\%\{item.port}/g, v.port);
                    rowHtml = rowHtml.replace(/\%\{item.name}/g, v.name);
                    rowHtml = rowHtml.replace(/\%\{item.description}/g, v.description);
                    rowHtml = rowHtml.replace(/\%\{item.createTime}/g, v.createTime ? v.createTime.replace("T", ' ').replace("Z", '') : "");
                    rowHtml = rowHtml.replace(/\%\{item.group}/g, v.groupName ? v.deployGroup : v.groupName);

                    var running = v.running;
                    var status = '<img src="${context.domain!''}/image/stop.png" title="stopped" />';
                    if(v.running == 1){
                        status = '<img src="${context.domain!''}/image/running.png" title="running" />';
                        runningApps+=v.id+",";
                    }
                    else if(v.running == 2)
                        status = '<img src="${context.domain!''}/image/pause.png" title="pause" />';

                    rowHtml = rowHtml.replace(/\%\{item.status}/g, status);

                    var options = new Array();

                    options.push("<a href='${context.domain!''}/api.html?appId=" + v.id + "' ><i class='iconfont icon-zitiyulan' title='查看APIs'></i></a>");

                    if (running == 0){
                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 1)'><i class='iconfont icon-play-circle' title='启动'></i></a>");
                    }
                    else if (running == 1){ //暂停
                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 0)'><i class='iconfont icon-stop' title='停止'></i></a>");
                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 3)'><i class='iconfont icon-redo' title='重启'></i></a>");
                    }
//                    else if(running == 2){
//                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 0)'><i class='iconfont icon-stop' title='停止'></i></a>");
////                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 2)'><i class='iconfont icon-timeout' title='暂停'></i></a>");
//                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 3)'><i class='iconfont icon-redo' title='重启'></i></a>");
//                    }

                    options.push("<a href='${context.domain!''}/appAdd.html?id=" + v.id + "' ><i class='iconfont icon-tianxie' title='编辑'></i></a>");
                    options.push("<a href='javascript:void(0)' onclick='deleteApp(" + v.id + ")'><i class='iconfont icon-delete' title='删除'></i></a>");

                    var optionHtml = "";
                    for(var x=0; x<options.length; x ++){
                        if( x < options.length - 1)
                            optionHtml += options[x] + "&nbsp;&nbsp;|&nbsp;&nbsp;";
                        else
                            optionHtml += options[x];
                    }

                    rowHtml = rowHtml.replace(/\%\{item.option}/g, optionHtml)
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

        if(runningApps == "")
            return;

        runningApps = runningApps.substring(0, runningApps.length-1);
        $.ajax({
            url:'${context.domain!''}/app/status/onNode?appIds='+runningApps+'&r='+Math.random(),
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
                        var warn="<img id='warn_"+appId+"' style='margin-left:0.5rem;' src='${context.domain!''}/image/tixing.png' title='存在节点状态异常' data-toggle='popover' data-placement='bottom' />";
                        $(warn).appendTo(statusCell);
                        $('#warn_'+appId).attr("data-content", nodesHtml);
                        $('#warn_'+appId).popover({
                            html:true
                        });
                    }

                }else{//标记无法获取集群运行状态
                    var warn="<img style='margin-left:0.5rem;' src='${context.domain!''}/image/warning1.png' title='无法获取节点状态' />";
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
    }

    $(document).ready(function () {
        apps();
    });
</script>

</html>