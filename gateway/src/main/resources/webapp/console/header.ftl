<header class="navbar-wrapper">
    <div class="navbar navbar-black">
        <div class="container-fluid cl">
            <a class="logo navbar-logo hidden-xs" href="#"><img class="logo" alt="${context.name?default('Ratel')}" src="${context.domain!''}/image/ratel.jpg"><span>${context.name?default('Ratel')}</span></a>
            <a class="logo navbar-logo-m visible-xs" href="#"><img class="logo" alt="${context.name?default('Ratel')}" src="${context.domain!''}/image/ratel.jpg"></a>

            <div class="opbox hidden-xs f-r mr-50 mt-10">
            <#if context.curUser?exists>
                <span class="user f-l">${context.curUser.name}<a href="${context.domain!''}/logout"><i class="iconfont icon-tuichu" title="注销"></i></a></span>
            <#else>
                <span class="user f-l"><a href='${context.domain!''}/login.html'>登录</a></span>
            </#if>
            </div>

            <div class="opbox visible-xs f-r mr-50 mt-10">
            <#if context.curUser?exists>
                <span class="user f-l">${context.curUser.name}<a href="${context.domain!''}/logout"><i class="iconfont icon-tuichu" title="注销"></i></a></span>
            <#else>
                <span class="user f-l"><a href='${context.domain!''}/login.html'>登录</a></span>
            </#if>
                <a aria-hidden="false" class="ml-50 nav-toggle iconfont visible-xs JS-nav-toggle icon-category" href="javascript:;"></a>
            </div>
        </div>
    </div>
</header>

<aside class="Hui-aside">
    <div class="menu_dropdown bk_2">
        <ul>
            <li <#if (context.uri!"") == "index.html">class="cursor"</#if>><a href="${context.domain!''}/index.html">应用<i class="iconfont icon-yingyongguanli"></i></a></li>
        </ul>
        <#if context.cluster??>
        <dl id="cluster">
            <dt class="<#if (context.uri!"") == "node.html" || (context.uri!"") == "group.html" || (context.uri!"") == "nodeApp.html" || (context.uri!"") == "nodeApi.html">selected</#if>">集群<i class="iconfont menu_dropdown-arrow icon-shenjing"></i></dt>
            <dd>
                <ul>
                    <li <#if (context.uri!"") == "node.html" || (context.uri!"") == "nodeApp.html" || (context.uri!"") == "nodeApi.html">class="cursor"</#if>><a href="${context.domain!''}/node.html">节点</a><i class="iconfont icon-jiedianguanli"></i></li>
                    <li <#if (context.uri!"") == "group.html">class="cursor"</#if>><a href="${context.domain!''}/group.html">集群组</a><i class="iconfont icon-zuzhiqunzu"></i></li>
                </ul>
            </dd>
        </dl>
        </#if>
        <ul>
            <li <#if (context.uri!"") == "status.html">class="cursor"</#if>><a href="${context.domain!''}/status.html">状态<i class="iconfont icon-shujukanban"></i></a></li>
        </ul>
        <#if context.curUser??>
        <dl id="menu_2">
            <dt class="<#if (context.uri!"") == "user.html" || (context.uri!"") == "changePassword.html">selected</#if>">系统<i class="iconfont menu_dropdown-arrow icon-icon_shezhi"></i></dt>
            <dd>
                <ul>
                    <#if context.curUser.role=="admin">
                    <li <#if (context.uri!"") == "user.html">class="cursor"</#if>><a href="${context.domain!''}/user.html">用户<i class="iconfont icon-icon_zhanghao"></i></a></li>
                    </#if>
                    <li <#if (context.uri!"") == "changePassword.html">class="cursor"</#if>><a href="${context.domain!''}/changePassword.html">更改密码<i class="iconfont icon-mima"></i></a></li>
                </ul>
            </dd>
        </dl>
        </#if>
    </div>
</aside>
