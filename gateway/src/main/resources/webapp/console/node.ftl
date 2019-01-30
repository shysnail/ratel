<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-节点</title>
    <#include "./inc.ftl" />
</head>
<body class="fixed-sidebar full-height-layout gray-bg">

<#include "./header.ftl" />

<section class="Hui-article-box">
    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">首页</a>
        <span class="c-999 en">&gt;</span><span class="c-666">应用清单</span>
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
        <div class="check-div form-inline" id="group_option">
            <input class="btn btn-primary-outline radius" type="button" value="分配组" onclick="doGrouping();"/>
        </div>

        <div class="panel panel-default mt-20">
            <div class="panel-header cl">
                <#--<div class="col-xs-1">-->
                    <#--<input type="checkbox" id="selectAll"/>-->
                <#--</div>-->
                <div class="col-xs-3">
                    hostname
                </div>
                <div class="col-xs-3">
                    加入时间
                </div>
                <div class="col-xs-1">
                    状态
                </div>
                <div class="col-xs-2">
                    所属组
                </div>
                <div class="col-xs-3">
                    操作
                </div>
            </div>
            <div class="row cl tmp_item_row" style="display: none">
                <#--<div class="col-xs-1">-->
                    <#--<input type="checkbox" name="ids" value="%{item.nodeId}"/>-->
                <#--</div>-->
                <div class="col-xs-3">
                    %{item.hostname}
                </div>
                <div class="col-xs-3">
                    %{item.createTime}
                </div>
                <div class="col-xs-1 article-status">
                    %{item.status}
                </div>
                <div class="col-xs-2">
                    %{item.groupId}
                </div>
                <div class="col-xs-3">
                    %{item.option}
                </div>
            </div>
            <div class="panel-body" id="table_items">
                暂无节点
            </div>

        </div>

    </div>

</section>


<div id="main_modal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content radius">
            <div class="modal-header">
                <h3 class="modal-title">分配 组</h3>
                <a class="close" data-dismiss="modal" aria-hidden="true" href="#">×</a>
            </div>
            <div class="modal-body">
                <div class="row cl">
                    <label class="form-label col-xs-3">名称:</label>
                    <div class="formControls col-xs-8">
                        <select class="select-box" id="groupTo">
                        </select>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="doGrouping()">确定</button>
                <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
            </div>
        </div>
    </div>
</div>

<div id="apps_modal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content radius">
            <div class="modal-header">
                <h3 class="modal-title">节点内应用</h3>
                <a class="close" data-dismiss="modal" aria-hidden="true" href="#">×</a>
            </div>
            <div class="modal-body">
                <div class="row cl">
                    <label class="form-label col-xs-3">名称:</label>
                    <div class="formControls col-xs-8">
                        <select class="select-box" id="groupTo">
                        </select>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="doGrouping()">确定</button>
                <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
            </div>
        </div>
    </div>
</div>

</body>
<script type="text/javascript">

    var groupId = '';
    var nodeIds;
    function grouping(id) {
        nodeIds = id;
        $.ajax({
            url: '${context.domain!''}/cluster/group',
            dataType: 'json',
            type: 'GET',
            success: function (data) {
                if (data == undefined || data.length == 0)
                    return;
                var options = '';
                for (var i = 0; i < data.length; i++) {
                    var v = data[i];
                    options += "<option value='" + v.id + "'>" + v.name + "</option>";
                }

                $('#groupTo').html(options);
                $("#main_modal").modal("show");
            },
            error: function (err) {
            }
        });
    }
    
    function nodeControl(id, action) {
        var url='${context.domain!''}/cluster/node/'+id+"/halt";
        if(action == 1)
            url='${context.domain!''}/cluster/node/'+id+"/restart";
        else if(action == 2)
            url='${context.domain!''}/cluster/node/'+id+"/expel";

        $.ajax({
            url: url,
            type: 'GET',
            dataType: 'json',
            async: false,
            success: function (data) {
                if(processData(data))
                    $.Huimodalalert(data.data, 2000, function () {
//                        if(data.success){
                            window.location = window.location;
//                        }
                    });
            },
            error: function (err) {

            }
        });
    }

    function selectIds() {
        var ids = '';
        $('#table_items').find(':checkbox[name=ids]:checked').not(".tmp_item_row").each(function () {
            ids += $(this).val() + ",";
        });
        if (ids.length > 0 && ids.charAt(ids.length - 1) == ',')
            ids = ids.substring(0, ids.length - 1);
        return ids;
    }

    function doGrouping() {
        if (nodeIds == undefined) {
            nodeIds = selectIds();
        }

        var g = {};
        g.nodeIds = nodeIds;
        g.groupId = $('#groupTo').val();

        $.ajax({
            url: '${context.domain!''}/cluster/grouping',
            type: 'POST',
            data: JSON.stringify(g),
            dataType: 'json',
            async: false,
            success: function (data) {
                nodeIds = undefined;
                if(processData(data))
                $.Huimodalalert(data.data, 2000, function () {
                    hideModal();
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

    function nodes() {
        var url = '${context.domain!''}/cluster/node';
        if (groupId != undefined)
            url += "?groupId=" + groupId+ "&r="+Math.random();
        else
            url += "?r="+Math.random();
        $.ajax({
            url: url,
            dataType: 'json',
            type: 'GET',
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
                    rowHtml = rowHtml.replace(/\%\{item.nodeId}/g, v.nodeId);
                    rowHtml = rowHtml.replace(/\%\{item.hostname}/g, v.hostname);
                    rowHtml = rowHtml.replace(/\%\{item.groupId}/g, v.groupName == undefined ? v.groupId : v.groupName);
                    rowHtml = rowHtml.replace(/\%\{item.createTime}/g, v.addTime.replace("T", ' ').replace("Z", ''));
                    rowHtml = rowHtml.replace(/\%\{item.status}/g, v.online ? '<img src="${context.domain!''}/image/running.png" title="running" />' : '<img src="${context.domain!''}/image/stop.png" title="stopped" />');


                    var options = new Array();
                    options.push("<a href='#' onclick='grouping(\"" + v.nodeId + "\")'><i class='iconfont icon-zuzhiqunzu' title='分组'></i></a>");
                    if(v.online){
                        options.push("<a href='#' onclick='viewApp(\"" + v.nodeId + "\")'><i class='iconfont icon-yingyongguanli' title='查看应用'></i></a>");
                        options.push("<a href='#' onclick='nodeControl(\"" + v.nodeId + "\", 0)'><i class='iconfont icon-stop' title='关闭'></i></a>");
                        options.push("<a href='#' onclick='nodeControl(\"" + v.nodeId + "\", 1)'><i class='iconfont icon-redo' title='重启'></i></a>");
                    }
                    options.push("<a href='#' onclick='nodeControl(\"" + v.nodeId + "\", 2)'><i class='iconfont icon-tuichu' title='逐出'></i></a>");

                    var optionHtml = "";
                    for(var x=0; x<options.length; x ++){
                        if( x < options.length - 1)
                            optionHtml += options[x] + "&nbsp;&nbsp;|&nbsp;&nbsp;";
                        else
                            optionHtml += options[x];
                    }

                    rowHtml = rowHtml.replace(/\%\{item.option}/g, optionHtml);
                    if (i % 2 == 1) {
                        row.css({"background": "#f9f9f9"});
                    }
                    row.html(rowHtml);
                    row.show();
                    $('#table_items').append(row);
                }
                $('#totalSize').text(i);
                //                setInterval(apps, 10000); //自动刷新

            },
            error: function (err) {
                setInterval(apps, 10000);
            }
        });
    }

    function viewApp(node) {
        window.location.href='${context.domain!''}/nodeApp.html?node=' + node;
    }

    $(document).ready(function () {

        groupId = getParameter("groupId");
        if (groupId != undefined) {
            $('#group_option').show();
        } else {
            $('#group_option').hide();
        }

        nodes();

        $('#menu_1').click();

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
    });
</script>
</html>