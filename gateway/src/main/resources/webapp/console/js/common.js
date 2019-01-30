/**
 * Created by frog.w on 2018/8/26.
 */

// String.prototype.trim = function () {
//     return this.replace(/(^\s*)|(\s*$)/g, "");
// }
// String.prototype.ltrim = function () {
//     return this.replace(/(^\s*)/g, "");
// }
// String.prototype.rtrim = function () {
//     return this.replace(/(\s*$)/g, "");
// }

function getParameter(name) {
    var url = location.search; //获取url中"?"符后的字串
    var theRequest = new Object();
    if (url.indexOf('?') < 0)
        return undefined;


    var str = url.substr(1);
    var strs = str.split("&");
    for (var i = 0; i < strs.length; i++) {
        var kv = strs[i].split("=");
        // if(kv[0] == undefined || kv[0].trim() == '')
        //     continue;

        if (kv[0] == name)
            return kv[1];

        // theRequest[strs[i].split("=")[0]]=unescape(strs[i].split("=")[1]);
    }

    return undefined;
}


function leftDropdown(obj) {
    var host = window.location.host,
        url = location.href,
        domain = "http://" + host + "/",
        menu = url.replace(domain, "");
    $(obj).each(function () {
        var current = $(this).find("a");
        $(this).removeClass("current");
        if (menu == $(current[0]).attr("href")) {
            $(this).addClass("current");
            $(this).parent("dl").find("dt").addClass("selected");
            $(this).parent("dl").find("dd").show();
        }
    });
}


function processError(e){
    if(e.showType == 1){
        eval(e.content);
    }else{
        if(e.show == 1){
            $.Huimodalalert(e.content, 2000);
        }
    }
}


function processData(data) {
    if(!data.success){
        var info = data.data;
        if(info != undefined){
            if(info.code == 401){
                if(info.show){
                    $.Huimodalalert(info.content, 1000, function () {
                        window.location = 'login.html';
                    });
                }else{
                    window.location = 'login.html';
                }
            }else{
                if(info.show){
                    $.Huimodalalert(info.content, 1500);
                }
            }
        }else{
            $.Huimodalalert("执行出错！", 1500);
        }

        return false;
    }
    return true;
}

function loadUser() {
    var ustr = $.cookie("session.user");
    // $(".user").css('height', $(".container-fluid").height);
    if(ustr){
        var user = JSON.parse(decodeURIComponent(ustr));
        $('.opbox .user').each(function (){
            $(this).text(user.name);

        });

        $('.opbox').each(function(){
           $(this).append("<a href='/logout'><i class='iconfont icon-tuichu'></i></a>");
        });
        // $('.opbox .icon-tuichu').show();
    }else{
        $('.user').html("<a href='/login.html'>登录</a>");
    }

    var stat = $.cookie("server.stat");
    if(stat && stat.cluster){
        $('#cluster').hide();
    }

}

$(document).ready(function () {
    $('.menu_dropdown dl dt').each(function () {
        if ($(this).hasClass("selected")) {
            $(this).parent().find("dd").show();
        } else {
            $(this).parent().find("dd").hide();
        }
    });
    $('.menu_dropdown dl dt').click(function () {
        if ($(this).hasClass("selected")) {
            $(this).removeClass("selected");
            $(this).parent().find("dd").hide();
        } else {
            $(this).addClass("selected");
            $(this).parent().find("dd").show();
        }
    });
});