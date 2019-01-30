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

<section class="Hui-article-box" style="overflow-y: scroll">

    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">系统</a>
        <span class="c-999 en">&gt;</span><span class="c-666">设定</span>
    </nav>

    <div class="panel panel-default mt-20">
        <div class="panel-header">基本设置</div>
        <div class="panel-body">
            <div class="row cl">
                <label class="form-label col-xs-3" for="name">名称:</label>
                <div class="formControls col-xs-8">
                    <input class="input-text" id="name" name="name" placeholder="为便于管理，请给api命名"/>
                </div>
            </div>
            <div class="row cl">
                <label class="form-label col-xs-3" for="path">path:</label>
                <div class="formControls col-xs-8">
                    <input class="input-text" id="path" name="path"
                           placeholder="uri，支持正则表达式。如/demo、^demo[a-z]+/image/[a-z0-9]+$等等"/>
                </div>
            </div>
        </div>
    </div>


</section>

</body>
<script type="text/javascript">
    $(function () {

    });


</script>
</html>