<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="renderer" content="webkit|ie-comp|ie-stand">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <title>${context.name?default('Ratel')}-运行状态</title>
    <#include "./inc.ftl" />
    <style type="text/css">
        .h200 {
            height: 200px;
        }

        .h300 {
            height: 300px;
        }

        .h400 {
            height: 400px;
        }
    </style>
</head>
<body class="fixed-sidebar full-height-layout gray-bg">

<#include "./header.ftl" />

<section class="Hui-article-box" style="overflow-y: scroll">

    <nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i>
        <a class="maincolor" href="${context.domain!''}/">状态</a>
        <span class="c-999 en">&gt;</span><span class="c-666">节点状态</span>
    </nav>

    <#if cluster??>
    <div id="search_bar" class="pd-20">
        <div class="row cl">
            <label class="form-label col-xs-2 text-r">节点:</label>
            <div class="formControls col-xs-4">
                <select id="node" name="node" size="1" class="input-text">

                </select>
            </div>
        </div>

    </div>
    </#if>

    <div class="cl row pd-10">
        <span>已运行</span>
        <span id="day_show">0天</span>
        <strong id="hour_show">0时</strong>
        <strong id="minute_show">0分</strong>
        <strong id="second_show">0秒</strong>
    </div>


    <div id="request" class="col-xs-5 h300 mt-30"></div>
    <div id="request_time" class="col-xs-5 h300 ml-40 mt-30"></div>

    <div id="memory" class="col-xs-5 h300 mt-30"></div>
    <div id="cpu" class="col-xs-5 h300 ml-40 mt-30"></div>

    <!--<div id="base" class="col-xs-5 h300"></div>-->
    <div id="netio" class="col-xs-5 h300 mt-30"></div>

</section>
<script type="text/javascript" src="js/map.js"></script>
<script type="text/javascript" src="js/echarts.common.min.js"></script>

</body>
<script type="text/javascript">
    var intDiff = parseInt(60); //倒计时总秒数量
    function timer(intDiff) {
        window.setInterval(function () {
            var day = 0, hour = 0, minute = 0, second = 0; //时间默认值
            if (intDiff > 0) {
                day = Math.floor(intDiff / (60 * 60 * 24));
                hour = Math.floor(intDiff / (60 * 60)) - (day * 24);
                minute = Math.floor(intDiff / 60) - (day * 24 * 60) - (hour * 60);
                second = Math.floor(intDiff) - (day * 24 * 60 * 60) - (hour * 60 * 60) - (minute * 60);
            }
            if (minute <= 9) minute = '0' + minute;
            if (second <= 9) second = '0' + second;
            $('#day_show').html(day + "天");
            $('#hour_show').html('<s id="h"></s>' + hour + '时');
            $('#minute_show').html('<s></s>' + minute + '分');
            $('#second_show').html('<s></s>' + second + '秒');
            intDiff++;
        }, 1000);
    }


    var x = new Array();
    //x=['周一','周二','周三','周四','周五','周六','周日']

    var request = new Array();
    var netIo = new Map();
    var memory = new Array();
    var cpu = new Array();

    function drawRequest() {
        var tag = ['总数', '错误', '失败', 'https', '处理中']
        var total = new Array();
        var error = new Array();
        var fail = new Array();
        var https = new Array();
        var processing = new Array();
        request.forEach(function (r) {
            total.push(r.total);
            error.push(r.error);
            fail.push(r.fail);
            https.push(r.https);
            processing.push(r.processing);
        });
        var series = [
            {
                name: '总数',
                type: 'line',
                showSymbol: false,
                smooth: true,
                lineStyle: {
                    color: '#48bdd7',
                    width: 1
                },
                data: total
            },
            {
                name: '错误',
                type: 'line',
                showSymbol: false,
                smooth: true,
                lineStyle: {
                    color: '#d70701',
                    width: 1
                },
                data: error
            },
            {
                name: '失败',
                type: 'line',
                showSymbol: false,
                smooth: true,
                lineStyle: {
                    color: '#d7c908',
                    width: 1
                },
                data: fail
            },
            {
                name: 'https',
                type: 'line',
                showSymbol: false,
                smooth: true,
                lineStyle: {
                    color: '#32d715',
                    width: 1
                },
                data: https
            },
            {
                name: '处理中',
                type: 'line',
                showSymbol: false,
                smooth: true,
                lineStyle: {
                    color: '#d210d7',
                    width: 1
                },
                data: processing
            }
        ];
        var option = {
            title: {
                text: '请求数'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: tag
            },
            toolbox: {
                feature: {
                    saveAsImage: {}
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: x
            },
            yAxis: {
                type: 'value',
                formatter: function (v) {
                    if (v > 10000)
                        return (v / 10000.0).toFixed(2) + "(w)";
                    else if (v > 1000)
                        return (v / 1000.0).toFixed(2) + "(k)";
                    return v;
                }
            },
            series: series
        };

        var chart = echarts.init(document.getElementById('request'));
        chart.setOption(option);
    }

    function drawNetio() {
        var tag = new Array();
        //tag = ['in', 'out'];


        var series = new Array();

        netIo.each(function (key, value) {
            if (!value)
                return;
            console.log(key);
            var si = {};
            si.name = key + '-in';
            tag.push(si.name);
            si.type = 'line';
            si.showSymbol = false;
            si.smooth = true;
            si.lineStyle = {
                color: '#ff2b23',
                width: 1
            };
            var so = {};
            so.name = key + '-out';
            tag.push(so.name);
            so.type = 'line';
            so.showSymbol = false;
            so.smooth = true;
            so.lineStyle = {
                color: '#3c72ff',
                width: 1
            };
            var nin = new Array();
            var nout = new Array();
            value.forEach(function (d) {
                console.debug(d.inBytes + "-" + d.outBytes)
                nin.push((d.inBytes / 2014).toFixed(2));
                nout.push((d.outBytes / 1024).toFixed(2));
            });

            si.data = nin;
            so.data = nout;
            series.push(si);
            series.push(so);
        });

        var option = {
            title: {
                text: 'netio使用'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: tag
            },
            toolbox: {
                feature: {
                    saveAsImage: {}
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: x
            },
            yAxis: {
                type: 'value',
                axisLabel: {
                    formatter: function (v) {
                        if (v > 1024 * 1024)
                            return (v / 1024 * 1024.0).toFixed(2) + "(GB)";
                        else if (v > 1024)
                            return (v / 1024.0).toFixed(2) + "(MB)";
                        return v + "(KB)";
                    }
                }
            },
            series: series
        };

        var chart = echarts.init(document.getElementById('netio'));
        chart.setOption(option);

    }

    function drawMemory() {
        var tag = new Array();
        tag = ['已使用', '最大可用'];
        var total = new Array();
        var free = new Array();
        var max = new Array();
        for (var i = 0; i < memory.length; i++) {
            var e = memory[i];
//            alert(e.total/1048576 + "," + (e.free/1048576).toFixed(0) + "," + e.max/1048576);
            total.push((e.total / 1048576).toFixed(0));
            free.push((e.free / 1048576).toFixed(0));
            max.push((e.max / 1048576).toFixed(0));
        }

        var series = new Array();
        series = [
            {
                name: '已使用',
                type: 'line',
                showSymbol: false,
                smooth: true,
                lineStyle: {
                    width: 1
                },
                areaStyle: {
                    normal: {
                        color: '#f39d78'
                    }
                },
                data: total
            },
//            {
//                name:'空闲',
//                type:'line',
//                showSymbol:false,
//                smooth:true,
//                areaStyle: {
//                    normal : {
//                        color:'#ffd912'
//                    }
//                },
//                data:free
//            },
            {
                name: '最大可用',
                type: 'line',
                showSymbol: false,
                smooth: true,
                lineStyle: {
                    color: '#0040f3',
                    width: 1
                },
                data: max
            }
        ];

        var option = {
            title: {
                text: 'memory'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: tag
            },
            toolbox: {
                feature: {
                    saveAsImage: {}
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: x
            },
            yAxis: {
                type: 'value',
                axisLabel: {
                    formatter: function (v) {
                        if (v > 1024)
                            return (v / 1024.0).toFixed(2) + "(GB)";
                        return v + "(MB)";
                    }
                }
            },
            series: series
        };
        var chart = echarts.init(document.getElementById('memory'));
        chart.setOption(option);
    }

    function drawCpu() {
        var tag = new Array();
        tag = ['cpu'];
        var series = new Array();
        series = [
            {
                name: 'cpu',
                type: 'line',
                lineStyle: {
                    width: 1
                },
                showSymbol: false,
                smooth: true,
                data: cpu
            }
        ];

        var option = {
            title: {
                text: 'cpu'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: tag
            },
            toolbox: {
                feature: {
                    saveAsImage: {}
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: x
            },
            yAxis: {
                type: 'value',
                splitNumber: 5,
                min: 0,
                max: 100,
                axisLabel: {
                    formatter: '{value} (%)'
                }
            },
            series: series
        };

        var chart = echarts.init(document.getElementById('cpu'));
        chart.setOption(option);

    }

    var reqtData = new Array();
    reqtData = ['响应时长'];
    var reqtSeries = new Array();
    reqtSeries = [
        {
            name: '响应时长',
            type: 'line',
            stack: '总量',
            lineStyle: {
                color: '#bc68ff',
                width: 1
            },
            data: [0.24, 0.08, 0.33, 0.15, 1.22, 1.88, 0.56]
        }];

    var reqtOption = {
        title: {
            text: '响应时长'
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: reqtData
        },
        toolbox: {
            feature: {
                saveAsImage: {}
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: x
        },
        yAxis: {
            type: 'value'
        },
        series: reqtSeries
    };

    var timeline = '';
    var node = '';
    function getStatus(additional) {

        $.ajax({
            url: '${context.domain!''}/status?timestamp=' + timeline + '&node=' + node + "&size=50",
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                timer(data.runtime);

                var nodes = data.nodeInfo;
                if (!additional) {
                    x = new Array();

                    request = new Array();
                    netIo = new Map();
                    memory = new Array();
                    cpu = new Array();
                }

                for (var i = nodes.length - 1; i > 0; i--) {
                    var node = nodes[i];
                    if (i == (nodes.length - 1))
                        timeline = node.createTime.replace("T", ' ').replace("Z", '');

                    x.push(node.createTime.replace("T", ' ').replace("Z", ''));

                    var status = JSON.parse(node.status);

                    var m = {};
                    m.total = status.system.total;
                    m.free = status.system.free;
                    m.max = status.system.max;
                    memory.push(m);

                    cpu.push((status.system.cpu * 100).toFixed(2));

                    var n = status.system.netIo;
                    for (var key in n) {
                        var data = netIo.get(key);
                        if (data == undefined) {
                            data = new Array();
                        }
                        data.push(n[key]);
                        netIo.put(key, data);
                    }

                    var r = {};
                    r.total = status.gate.request;
                    r.error = status.gate.error;
                    r.fail = status.gate.fail;
                    r.https = status.gate.httpsRequest;
                    r.processing = status.gate.processing;
                    request.push(r);
                }

                if (additional) {

                }

                drawRequest();
                drawNetio();
                drawMemory();
                drawCpu();
            },
            error: function () {

            }
        });
    }

    function getNodes() {
        $.ajax({
            url: '${context.domain!''}/cluster/node',
            dataType: 'json',
            type: 'GET',
            async: false,
            success: function (data) {
                if (data == undefined || data.length == 0)
                    return;
                $('#node').html("");
                var rows = '';
                var i = 0;
                var options = '';
                for (; i < data.length; i++) {
                    var v = data[i];

                    var node = v.nodeId;
                    var host = v.hostname;
                    var selected = '';
                    if (v.isLocal != undefined)
                        selected = " selected='selected'";
                    options += "<option value='" + node + "'" + selected + ">" + host + "</option>";
                }
                $('#node').html(options);
            },
            error: function () {

            }
        });
    }

    $(function () {

        <#if context.cluster??>
        getNodes();
        </#if>

        getStatus();

        $('#node').change(function () {
            node = $(this).val();
            getStatus();
        });

//        var baseChart = echarts.init(document.getElementById('base'));
//        baseChart.setOption(baseOption);

        var reqtChart = echarts.init(document.getElementById('request_time'));
        reqtChart.setOption(reqtOption);

    });


</script>
</html>