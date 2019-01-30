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
        <span class="c-999 en">&gt;</span><span class="c-666">节点</span>
        <span class="c-999 en">&gt;</span><span class="c-666">应用列表</span>
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
        <div class="panel panel-default mt-20">
            <div class="panel-header cl">
                <div class="formControls col-xs-2">
                    名称
                </div>
                <div class="col-xs-4">
                    描述
                </div>
                <div class="col-xs-2 hidden-xs">
                    创建时间
                </div>
                <div class="col-xs-1">
                    状态
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
                    %{item.port}:%{item.name}
                </div>
                <div class="col-xs-4">
                    %{item.description}
                </div>
                <div class="col-xs-2 hidden-xs">
                    %{item.createTime}
                </div>
                <div class="article-status col-xs-1">
                    %{item.status}
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

    var tmpAppRow = $('div.tmp_item_row');
    function apps() {
        var runningApps="";
        $.ajax({
            url: '${context.domain!''}/cluster/node/' + node + '/app',
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
                    rowHtml = rowHtml.replace(/\%\{item.createTime}/g, v.createTime.replace("T", ' ').replace("Z", ''));
                    rowHtml = rowHtml.replace(/\%\{item.group}/g, v.groupName == undefined ? v.deployGroup : v.groupName);

                    var running = v.realRunning;
                    var status = '<img src="${context.domain!''}/image/stop.png" title="stopped" />';
                    if(v.running){
                        status = '<img src="${context.domain!''}/image/running.png" title="running" />';
                        runningApps += v.id + ",";
                    }
                    else
                        status = '<img src="${context.domain!''}/image/pause.png" title="pause" />';

                    rowHtml = rowHtml.replace(/\%\{item.status}/g, status);

                    var options = new Array();

                    options.push("<a href='${context.domain!''}/nodeApi.html?appId=" + v.id + "&nodeId="+node+"' ><i class='iconfont icon-zitiyulan' title='查看APIs'></i></a>");

//                    if (running == 0){
//                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 1)'><i class='iconfont icon-play-circle' title='启动'></i></a>");
//                    }
//                    else if (running == 1){ //暂停
//                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 0)'><i class='iconfont icon-stop' title='停止'></i></a>");
//                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 3)'><i class='iconfont icon-redo' title='重启'></i></a>");
//                    }
//                    else if(running == 2){
//                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 0)'><i class='iconfont icon-stop' title='停止'></i></a>");
////                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 2)'><i class='iconfont icon-timeout' title='暂停'></i></a>");
//                        options.push("<a href='javascript:void(0)' onclick='appAction(" + v.id + ", 3)'><i class='iconfont icon-redo' title='重启'></i></a>");
//                    }

                    <#--options.push("<a href='${context.domain!''}/appAdd.html?id=" + v.id + "' ><i class='iconfont icon-tianxie' title='编辑'></i></a>");-->
                    <#--options.push("<a href='javascript:void(0)' onclick='deleteApp(" + v.id + ")'><i class='iconfont icon-delete' title='删除'></i></a>");-->

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

    }

    var node = getParameter('node');

    $(document).ready(function () {
        apps();
    });
</script>

</html>