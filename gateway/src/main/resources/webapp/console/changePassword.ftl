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
        <span class="c-999 en">&gt;</span><span class="c-666">修改密码</span>
    </nav>

    <form id="mainForm" class="form form-horizontal responsive" action="#" method="post">
        <div class="panel panel-default mt-20">
            <div class="panel-header">变更密码</div>
            <div class="panel-body">
                <div class="row cl">
                    <label class="form-label col-xs-3" for="originPassword">旧密码:</label>
                    <div class="formControls col-xs-5">
                        <input class="input-text" id="originPassword" name="originPassword" type="password"/>
                    </div>
                    <div class="formControls col-xs-4"><i class="c-red">*</i> 输入旧密码</div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3" for="newPassword">新密码:</label>
                    <div class="formControls col-xs-5">
                        <input class="input-text" id="newPassword" name="newPassword" type="password"/>
                    </div>
                    <div class="formControls col-xs-4"><i class="c-red">*</i> 输入新密码</div>
                </div>
                <div class="row cl">
                    <label class="form-label col-xs-3" for="confirmPassword">确认密码:</label>
                    <div class="formControls col-xs-5">
                        <input class="input-text" id="confirmPassword" name="confirmPassword" type="password"/>
                    </div>
                    <div class="formControls col-xs-4"><i class="c-red">*</i> 再次输入新密码</div>
                </div>
                <div class="row cl">
                    <div class="col-xs-12 text-c">
                        <input id='btn_login' class="btn btn-primary" type="submit" value="&nbsp;&nbsp;修改&nbsp;&nbsp;">
                    </div>
                </div>
            </div>
        </div>
    </form>

</section>

</body>
<script type="text/javascript">

    $(document).ready(function () {
        $('#mainForm').submit(function () {
            $('#btn_login').attr("disabled", true);
            var newPassword = $('#newPassword').val();
            var confirmPassword=$('#confirmPassword').val();
            if(newPassword != confirmPassword){
                $('#btn_login').removeAttr("disabled");
                $.Huimodalalert('两次输入新密码不相同，请检查', 1500);
                return;
            }
            var originPassword = $('#originPassword').val();
            var d={};
            d.originPassword=$.md5(originPassword);
            d.newPassword=$.md5(newPassword);
            d.confirmPassword=$.md5(confirmPassword);

            $.ajax({
                url:"${context.domain!''}/user/changePassword",
                data:JSON.stringify(d),
                type:'post',
                async:false,
                dataType:"json",
                success:function (data) {
                    $('#btn_login').removeAttr("disabled");
                    if(data.success){
                        $.Huimodalalert(data.data, 1500, function () {
                            window.location = window.location;
                        });
                    }else{
                        $('#newPassword').val('');
                        $('#confirmPassword').val('');
                        $.Huimodalalert(data.data, 2000, function(){
                            $('#password').val("");
                        });
                    }

                },
                error:function (err) {
                    $('#btn_login').removeAttr("disabled");
                }
            });
            return false;
        });

    });
</script>
</html>