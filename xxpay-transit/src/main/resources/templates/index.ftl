<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta http-equiv="Content-Language" content="zh-cn">
    <meta name="apple-mobile-web-app-capable" content="no"/>
    <meta name="apple-touch-fullscreen" content="yes"/>
    <meta name="format-detection" content="telephone=no,email=no"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="white">
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1">
    <meta http-equiv="Expires" content="0">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Cache-control" content="no-cache">
    <meta http-equiv="Cache" content="no-cache">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <title>支付宝</title>
    <link href="/css/gepay.css?t=20181115" rel="stylesheet" media="screen">
    <script type="text/javascript" src="https://cdn.staticfile.org/jquery/1.11.1/jquery.min.js"></script>
    <script type="text/javascript">
        var _hmt = _hmt || [];
        _hmt.push(['_trackEvent', 'pay', 'show1']);
    </script>
</head>

<body>
<div class="body">
    <h1 class="mod-title" style="line-height: normal;">
        <span class="ico_log ico-1"></span>
    </h1>

    <div class="mod-ct">
        <div class="order">
        </div>
        <div class="amount" id="money"><span>订单金额：${amount}</div>
        <div class="qrcode-img-wrapper" data-role="qrPayImgWrapper">
            <div data-role="qrPayImg" class="qrcode-img-area">
                <div class="ui-loading qrcode-loading" data-role="qrPayImgLoading"></div>
                <div style="position: relative;display: none;">
                    <img id="show_qrcode" width="300" height="300" src="/images/qrcode_loading.png"
                         style="display: block;"
                         title="本二维码仅可支付一次,请勿重复使用,本二维码仅可支付一次,请勿重复使用,本二维码仅可支付一次,请勿重复使用,本二维码仅可支付一次,请勿重复使用">
                    <img onclick="" id="use" src="/images/logo_alipay.png"
                         style="display:none; position: absolute;top: 50%;left: 50%;width:32px;height:32px;margin-left: -16px;margin-top: -30px">
                    <img id="loading" src="/images/loading.gif"
                         style="position: absolute;top: 50%;left: 50%;width:32px;height:32px;margin-left: -16px;margin-top: -15px">
                </div>
            </div>
        </div>
        <div id="doDiv" style="display:block;">
            <!--其他手机浏览器+支付宝支付-->
            <!--isiOS-->
            <div class="paybtn" id="paybtn" style="padding-top: 10px;"><a href="${payUrl}" id="alipaybtn" class="btn btn-primary"
                                                              target="_blank" style="font-size: 26px;">点击启动支付宝支付</a>
            </div>
        </div>
        <div class="time-item" style="padding-top: 10px;">
            <div class="time-item"><span style="font-size: 20px"> 订单号</span>
                <h1>${mchOrderId}</h1>
                <h1 style="color: red">${msg}</h1></div>
            <!--其他手机浏览器+支付宝支付-->
            <div class="time-item" id="msg">
                <h1><h1>支付完成后，请返回此页</h1>
            </div>

        </div>
        <div class="time-item">
            <p>
                <br/>
                <#--<a href="https://www.wenjuan.com/s/bmuyUb/?user=2a43bb261e64d2c996f3cfea&repeat=1" id="alipayerror" target="_blank">支付宝支付遇到问题,点此反馈</a>-->
            </p>
        </div>
        <div class="tip-text">
        </div>

    </div>

    <#--    <div class="foot">-->
    <#--        <div class="inner" style="display:none;">-->
    <#--            <p>手机用户可保存上方二维码到手机中</p>-->
    <#--            <p>在微信扫一扫中选择“相册”即可</p>-->
    <#--            <p></p>-->
    <#--        </div>-->
    <#--    </div>-->

</div>

<script type="text/javascript">
    $().ready(function () {
        var msg ='${msg}';
        if (msg!='')
        {
            $("#paybtn").attr("style","display:none");
        }
    });
</script>
</body>
</html>