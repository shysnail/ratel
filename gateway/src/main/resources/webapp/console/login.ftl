<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-登录</title>
    <#include "./inc.ftl" />
</head>
<body class="fixed-sidebar full-height-layout gray-bg">
<header class="navbar-wrapper">
    <div class="navbar navbar-black">
        <div class="container-fluid cl">
            <a class="logo navbar-logo hidden-xs" href="#"><img class="logo" alt="${context.name?default('Ratel')}" src="image/ratel.jpg"><span>${context.name?default('Ratel')}</span></a>
            <a class="logo navbar-logo-m visible-xs" href="#"><img class="logo" alt="${context.name?default('Ratel')}" src="image/ratel.jpg"></a>
        </div>
    </div>
</header>

<div class="container text-c">
    <div id="apps" class="container ui-sortabl col-5 text-l box-shadow" style="margin-top: 7rem;">

        <div class="panel panel-default">
            <div class="panel-header">登录</div>
            <div class="panel-body">
                <form action="" method="post" class="form form-horizontal responsive" id="loginform">
                    <input id="target" type="hidden" name="target" >
                    <div class="row cl">
                        <label class="form-label col-xs-3">用户名：</label>
                        <div class="formControls col-xs-8">
                            <input type="text" class="input-text" placeholder="4~16个字符，字母/中文/数字/下划线" name="username" id="username" value="admin">
                        </div>
                    </div>
                    <div class="row cl">
                        <label class="form-label col-xs-3">密码：</label>
                        <div class="formControls col-xs-8">
                            <input type="password" class="input-text" autocomplete="off" placeholder="密码" name="password" id="password" value="123456">
                        </div>
                    </div>
                    <div class="row cl">
                        <div class="col-xs-12 text-c">
                            <input id='btn_login' class="btn btn-primary" type="submit" value="&nbsp;&nbsp;登录&nbsp;&nbsp;">
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
<script type="text/javascript">
    var target = getParameter('target');
    $('#target').val(target);
    $('#loginform').attr("action", "${context.domain!''}/login?r="+Math.random());

    $(document).ready(function(){
        $('#loginform').submit(function () {
            $('#btn_login').attr("disabled", true);
            var pwd = $('#password').val();
            $('#password').val($.md5(pwd));
            $.ajax({
                url:"${context.domain!''}/login",
                data:$('#loginform').serialize(),
                type:'post',
                async:false,
                dataType:"json",
                success:function (data) {
                    $('#btn_login').removeAttr("disabled");
                    $('#password').val(pwd);
                    if(data.success){
                        $.Huimodalalert('登陆验证通过', 1500, function () {
                            window.location = 'index.html';
                        });
                    }else{
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