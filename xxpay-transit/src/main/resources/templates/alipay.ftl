<script src="http://code.jquery.com/jquery-1.10.1.min.js"></script>
<script src="https://gw.alipayobjects.com/as/g/h5-lib/alipayjsapi/3.1.1/alipayjsapi.inc.min.js"></script>

<button id="J_btn" class="btn btn-default" style="display: none">支付</button>
<script>

    var btn = document.querySelector('#J_btn');
    var payUrl = '${payUrl}'
    btn.addEventListener('click', function () {
        ap.tradePay({
            orderStr: payUrl
        }, function (res) {
            ap.alert(res.resultCode);
        });
    });

    $().ready(function () {
        $("#J_btn").click();
    });

</script>