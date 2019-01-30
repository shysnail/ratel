<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-节点组</title>
    <#include "./inc.ftl" />
</head>
<body class="fixed-sidebar full-height-layout gray-bg">

<#include "./header.ftl" />

<section class="Hui-article-box">
    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">首页</a>
        <span class="c-999 en">&gt;</span><span class="c-666">应用组</span>
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
            <input class="btn btn-primary-outline radius" type="button" value="添加组" onclick="addItem()"/>
        </div>
        <div class="panel panel-default mt-20">
            <div class="panel-header cl">
                <div class="col-xs-2">
                    名称
                </div>
                <div class="col-xs-3">
                    简介
                </div>
                <div class="col-xs-2">
                    节点数
                </div>
                <div class="col-xs-3">
                    创建时间
                </div>
                <div class="col-xs-2">
                    操作
                </div>
            </div>
            <div class="row cl tmp_item_row" style="display: none">
                <div class="col-xs-2">
                    %{item.name}
                </div>
                <div class="col-xs-3">
                    %{item.description}
                </div>
                <div class="col-xs-2">
                    %{item.nodes}
                </div>
                <div class="col-xs-3">
                    %{item.createTime}
                </div>
                <div class="col-xs-2">
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
                <h3 class="modal-title">添加/编辑 组</h3>
                <a class="close" data-dismiss="modal" aria-hidden="true" href="#">×</a>
            </div>
            <div class="modal-body">
                <div class="row cl">
                    <label class="form-label col-xs-3">名称:</label>
                    <div class="formControls col-xs-8">
                        <input type="text" class="input-text" name="group.name" placeholder="中英文，不能包含特殊字符"/>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">描述:</label>
                    <div class="formControls col-xs-8">
                        <textarea class="textarea" name="group.description" placeholder="简介"></textarea>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="doaddItem();">确定</button>
                <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
            </div>
        </div>
    </div>
</div>

</body>
<script type="text/javascript">

    var action = '';//添加组url
    function addItem() {
        action = "${context.domain!''}/cluster/group";
        $("#main_modal").modal("show");
    }

    function editGroup(id) {
        $.ajax({
            url: '${context.domain!''}/cluster/group/' + id,
            type: 'GET',
            dataType: 'json',
            async: false,
            success: function (data) {
                action = "${context.domain!''}/cluster/group/" + id;
                $("input:text[name=group\\.name]").val(data.name);
                if (id == 0) {
                    $("input:text[name=group\\.name]").prop('readonly', true);
                }
                $("textarea[name=group\\.description]").text(data.description);
                $("#main_modal").modal("show");
            },
            error: function (err) {

            }
        });

    }


    function doaddItem() {
        var g = {};
        g.name = $("input:text[name=group\\.name]").val();
        g.description = $("textarea[name=group\\.description]").val();
        $.ajax({
            url: action,
            type: 'POST',
            data: JSON.stringify(g),
            dataType: 'json',
            async: false,
            success: function (data) {
                if(processData(data))
                $.Huimodalalert('操作已完成！', 2000, function () {
                    hideModal();
                    window.location.reload();
                });
            },
            error: function (err) {

            }
        });
    }


    function hideModal() {
        $("#main_modal").modal("hide");
    }

    function deleteGroup(id) {
        if (!confirm("删除后不可恢复，点击取消终止操作，点击确定继续删除")) {
            return;
        }
        $.ajax({
            url: '${context.domain!''}/cluster/group/' + id,
            type: 'DELETE',
            dataType: 'json',
            success: function (data) {
                if(processData(data))
                $.Huimodalalert('组已删除！', 1000, function () {
                    window.location.reload();
                });
            },
            error: function (err) {

            }
        });
    }

    var tmpAppRow = $('div.tmp_item_row');

    function groups() {
        $.ajax({
            url: '${context.domain!''}/cluster/group',
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
                    rowHtml = rowHtml.replace(/\%\{item.id}/g, v.id);
                    rowHtml = rowHtml.replace(/\%\{item.name}/g, v.name);
                    rowHtml = rowHtml.replace(/\%\{item.nodes}/g, v.nodes);
                    rowHtml = rowHtml.replace(/\%\{item.description}/g, v.description);
                    rowHtml = rowHtml.replace(/\%\{item.createTime}/g, v.createTime.replace("T", ' ').replace("Z", ''));

                    var options = new Array();
                    options.push("<a href='node.html?groupId=" + v.id + "' ><i class='iconfont icon-zitiyulan' title='查看节点'></i></a>");

                    options.push("<a href='#' onclick='editGroup(" + v.id + ")'><i class='iconfont icon-tianxie' title='编辑'></i></a>");

                    if (v.nodes == undefined || v.nodes == 0)
                        options.push("<a href='javascript:void(0)' onclick='deleteGroup(" + v.id + ")'><i class='iconfont icon-delete' title='删除'></i></a>");

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
    }

    $(document).ready(function () {
        groups();
    });
</script>
</html>