<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-用户</title>
    <#include "./inc.ftl" />
</head>
<body class="fixed-sidebar full-height-layout gray-bg">

<#include "./header.ftl" />

<section class="Hui-article-box">
    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">首页</a>
        <span class="c-gray en">&gt;</span>系统管理
        <span class="c-999 en">&gt;</span><span class="c-666">用户清单</span>
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
            <input class="btn btn-primary-outline radius" type="button" value="添加用户" onclick="addItem()"/>
        </div>
        <div class="panel panel-default mt-20">
            <div class="panel-header cl">
                <div class="col-xs-2">
                    姓名
                </div>
                <div class="col-xs-3">
                    部门
                </div>
                <div class="col-xs-2">
                    创建时间
                </div>
                <div class="col-xs-2">
                    身份
                </div>
                <div class="col-xs-1">
                    状态
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
                    %{item.department}
                </div>
                <div class="col-xs-2">
                    %{item.createTime}
                </div>
                <div class="col-xs-2">
                    %{item.role}
                </div>
                <div class="article-status col-xs-1">
                    %{item.status}
                </div>
                <div class="col-xs-2">
                    %{item.option}
                </div>
            </div>
            <div class="panel-body" id="table_items">
                暂无用户
            </div>

        </div>

    </div>

</section>

<div id="main_modal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content radius">
            <div class="modal-header">
                <h3 class="modal-title">添加/编辑 用户</h3>
                <a class="close" data-dismiss="modal" aria-hidden="true" href="#">×</a>
            </div>
            <input type="hidden" name="item.id" />
            <div class="modal-body">
                <div class="row cl">
                    <label class="form-label col-xs-3">姓名:</label>
                    <div class="formControls col-xs-8">
                        <input type="text" class="input-text" name="item.name" placeholder="中英文，不能包含特殊字符"/>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">登录名:</label>
                    <div class="formControls col-xs-8">
                        <input type="text" class="input-text" name="item.account" placeholder="建议用邮箱"/>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">部门:</label>
                    <div class="formControls col-xs-8">
                        <input type="text" class="input-text" name="item.department" placeholder="中英文，不能包含特殊字符"/>
                    </div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3">角色:</label>
                    <div class="formControls col-xs-8">
                        <select name="item.role" class="col-xs-8 input-text">
                            <option value="admin">管理员</option>
                            <option value="guest">一般用户</option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="doAddItem();">确定</button>
                <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
            </div>
        </div>
    </div>
</div>

</body>
<script type="text/javascript">

    function addItem() {
        $("input:hidden[name=item\\.id]").val('');
        $("#main_modal").modal("show");
    }

    function editItem(id) {
        $.ajax({
            url: '${context.domain!''}/user/' + id,
            type: 'GET',
            dataType: 'json',
            async: false,
            success: function (data) {
                $("input:hidden[name=item\\.id]").val(data.id);
                $("input:text[name=item\\.name]").val(data.name);
                $("input:text[name=item\\.account]").val(data.account);
                if(id != undefined){
                    $("input:text[name=item\\.account]").prop('readonly', true)
                }
                $("input:text[name=item\\.department]").val(data.department);
                $("input:text[name=item\\.role]").val(data.role);
                $("#main_modal").modal("show");
            },
            error: function (err) {

            }
        });

    }


    function doAddItem() {
        var item = {};
        item.id = $("input:hidden[name=item\\.id]").val();
        item.name = $("input:text[name=item\\.name]").val();
        item.account = $("input:text[name=item\\.account]").val();
        item.email=item.account;
        item.password=$.md5('111111');
        item.role = $("[name=item\\.role]").val();
        item.department = $("input:text[name=item\\.department]").val();
        $.ajax({
            url: '${context.domain!''}/user?r='+Math.random(),
            type: 'POST',
            data: JSON.stringify(item),
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

    function frozenItem(id, type) {
        var url= '${context.domain!''}/user/' + id+"/unFrozen";
        if(type==1){
            url= '${context.domain!''}/user/' + id+"/frozen";
        }
        $.ajax({
            url: url,
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                if(processData(data))
                $.Huimodalalert(data.data, 1000, function () {
                    window.location.reload();
                });
            },
            error: function (err) {

            }
        });
    }

    var tmpAppRow = $('div.tmp_item_row');

    function items() {
        $.ajax({
            url: '${context.domain!''}/user',
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
                    rowHtml = rowHtml.replace(/\%\{item.account}/g, v.account);
                    rowHtml = rowHtml.replace(/\%\{item.department}/g, v.department);
                    rowHtml = rowHtml.replace(/\%\{item.role}/g, v.role);
                    rowHtml = rowHtml.replace(/\%\{item.status}/g, v.lockedOut ? '<img src="${context.domain!''}/image/dongjie.png" title="frozen" />':'');
                    rowHtml = rowHtml.replace(/\%\{item.createTime}/g, v.createTime.replace("T", ' ').replace("Z", ''));

                    var options = new Array();

                    options.push("<a href='#' onclick='editItem(\"" + v.account + "\")'><i class='iconfont icon-tianxie' title='编辑'></i></a>");

                    if (v.lockedOut)
                        options.push("<a href='javascript:void(0)' onclick='frozenItem(\"" + v.account + "\", 0)'><i class='iconfont icon-jiedong' style='color: #f37629;' title='解冻'></i></a>");
                    else
                        options.push("<a href='javascript:void(0)' onclick='frozenItem(\"" + v.account + "\", 1)'><i class='iconfont icon-dongjie2' style='color: #4297ca;' title='冻结'></i></a>");

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
        items();
    });
</script>
</html>