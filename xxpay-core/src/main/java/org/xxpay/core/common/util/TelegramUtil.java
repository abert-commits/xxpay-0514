package org.xxpay.core.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class TelegramUtil {

    private static final String chat_id = "-431173381";//预警消息群

    private static final String yingxiao_chat_id = "-389831914";

    private static final String requestUrl = "https://api.telegram.org/bot1267413179:AAEPu8ViTWPy9MkVyI01wapSnRIeozhftuI/sendmessage";

    public static void SendMsg(String msg) {
        try {
            String myUrl = MessageFormat.format("{0}?chat_id={1}&text={2}", requestUrl, chat_id, msg);
            XXPayUtil.doPostQueryCmd(myUrl, "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    //公告消息发送
    //内部盘口 1380701165:AAFWXYQOPYIeEjS01s22MpQUfBHo7i4lvuk
    //外包盘口 1493048387:AAFXXnt98LHM9ep_9i0pusBKi-bYkH4JGfQ

    /**
     * 公告消息发送
     *
     * @param type 消息类型：1:内部盘口，2:外包盘口
     * @param msg  消息内容
     */
    public static String PushMsg(String type, String msg) {

        try {
            String myChatId = "";
            String nbChat_id = "bot1380701165:AAFWXYQOPYIeEjS01s22MpQUfBHo7i4lvuk";
            String wbChat_id = "bot1462073537:AAEpYWBCCKl5rtC3-W4PSrADx6h5w28zx6g";
            if (type.equals("1")) {
                myChatId = nbChat_id;
            } else {
                myChatId = wbChat_id;
            }
            String myRequestUrl = MessageFormat.format("https://api.telegram.org/{0}/sendmessage", myChatId);
            String getUpdatesUrl = MessageFormat.format("https://api.telegram.org/{0}/getUpdates", myChatId);
            String result = XXPayUtil.doPostQueryCmd(getUpdatesUrl, "");
            JSONObject jsonObject = JSONObject.parseObject(result);
            String code = jsonObject.getString("ok");
            if (!code.equals("true")) {
                return "发送失败,请求Telegram机器人接口失败";
            }

            Map<String, String> mapKeys = new HashMap<>();
            JSONArray jsonArray = jsonObject.getJSONArray("result");

            if (jsonArray==null || jsonArray.size()==0)
            {
                return "发送失败,未找到群组信息！";
            }
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i).getJSONObject("message").getJSONObject("chat");
                String key = object.getString("id");
                if (mapKeys.containsKey(key)) {
                    continue;
                }
                String sendUrl = MessageFormat.format("{0}", myRequestUrl);
                String sendMsg = MessageFormat.format("chat_id={0}&text={1}", key, msg);
                mapKeys.put(key, key);

                String sendResult = XXPayUtil.doPostQueryCmd(sendUrl, sendMsg);
                String a = "";
            }




            return "发送成功";
        } catch (Exception ex) {
            return "发送失败，请求异常:" + TelegramUtil.getExceptionInfo(ex);
        }
    }


    public static String getExceptionInfo(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        return baos.toString();
    }

}
