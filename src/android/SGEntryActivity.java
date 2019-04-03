package __PACKAGE_NAME__;

import android.app.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.jiahua.xianliao.JhXianliao;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.xianliao.im.sdk.api.ISGAPI;
import org.xianliao.im.sdk.api.ISGAPIEventHandler;
import org.xianliao.im.sdk.api.SGAPIFactory;
import org.xianliao.im.sdk.constants.SGConstants;
import org.xianliao.im.sdk.modelbase.BaseReq;
import org.xianliao.im.sdk.modelbase.BaseResp;
import org.xianliao.im.sdk.modelmsg.InvitationResp;
import org.xianliao.im.sdk.modelmsg.SendAuth;


/**
 * Created by nickyang on 2017/1/18.
 *
 * 此类用于接收从闲聊返回到应用的返回值
 *
 * 注意： "sgapi" 目录名和 "SGEntryActivity" 类名都不能改动
 *
 */

public class SGEntryActivity extends Activity implements ISGAPIEventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Constants.SG_APPID 修改成自己申请的appId
        ISGAPI api = JhXianliao.getXlAPI(this);
        if(api == null){
            startMainActivity();
        }else api.handleIntent(getIntent(),this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ISGAPI api = JhXianliao.getXlAPI(this);
        if(api == null){
            startMainActivity();
        }else api.handleIntent(getIntent(),this);
    }

    @Override
    public void onReq(BaseReq req) {
        finish();
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.i(JhXianliao.TAG, resp.toString());
        CallbackContext ctx = JhXianliao.getCurrentCallbackContext();
        if (ctx == null) {
            startMainActivity();
            return ;
        }
        switch (resp.errCode){
            case BaseResp.ErrCode.ERR_OK:
                switch (resp.getType()){
                    case SGConstants.COMMAND_AUTH: {  //授权登陆
                        auth(resp);
                        break;
                    }
                    case SGConstants.COMMAND_SHARE: {  //分享文本，图片，邀请
                        ctx.success();
                        break;
                    }
                    default:
                        ctx.success();
                        break;
                }
                break;

            case BaseResp.ErrCode.ERR_USER_CANCEL:
                ctx.error(JhXianliao.ERROR_XIANLIAO_RESPONSE_USER_CANCEL);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                ctx.error(JhXianliao.ERROR_XIANLIAO_RESPONSE_AUTH_DENIED);
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                ctx.error(JhXianliao.ERROR_XIANLIAO_RESPONSE_SENT_FAILED);
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                ctx.error(JhXianliao.ERROR_XIANLIAO_RESPONSE_UNSUPPORT);
                break;
            case BaseResp.ErrCode.ERR_COMM:
                ctx.error(JhXianliao.ERROR_XIANLIAO_RESPONSE_COMMON);
                break;
            default:
                ctx.error(JhXianliao.ERROR_XIANLIAO_RESPONSE_UNKNOWN);
                break;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    protected void startMainActivity() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getApplicationContext().getPackageName());
        getApplicationContext().startActivity(intent);
    }

    /**
     * 授权
     * @param resp
     */
    protected void auth(BaseResp resp) {
        SendAuth.Resp res = ((SendAuth.Resp) resp);
        Log.i(JhXianliao.TAG, res.toString());

        // get current callback context
        CallbackContext ctx = JhXianliao.getCurrentCallbackContext();
        if (ctx == null) {
            return ;
        }

        JSONObject response = new JSONObject();
        try {
            response.put("code", res.code);
            response.put("state", res.state);
            response.put("country", res.country);
            response.put("lang", res.lang);
        } catch (JSONException e) {
            Log.e(JhXianliao.TAG, e.getMessage());
        }
        ctx.success(response);
    }

}
