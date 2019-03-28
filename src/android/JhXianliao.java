package com.jiahua.xianliao;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.xianliao.im.sdk.api.ISGAPI;
import org.xianliao.im.sdk.api.SGAPIFactory;
import org.xianliao.im.sdk.constants.SGConstants;
import org.xianliao.im.sdk.modelbase.BaseReq;
import org.xianliao.im.sdk.modelmsg.SGImageObject;
import org.xianliao.im.sdk.modelmsg.SGLinkObject;
import org.xianliao.im.sdk.modelmsg.SGMediaMessage;
import org.xianliao.im.sdk.modelmsg.SendAuth;
import org.xianliao.im.sdk.modelmsg.SendMessageToSG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class JhXianliao extends CordovaPlugin{
    public static final String TAG = "Cordova.Plugin.Xianliao";

    public static final String PREFS_NAME = "Cordova.Plugin.Xianliao";
    public static final String XLAPPID_PROPERTY_KEY = "xlappid";

    public static final String EXTERNAL_STORAGE_IMAGE_PREFIX = "external://";
    public static final int REQUEST_CODE_ENABLE_PERMISSION = 55433;
    public static final String ANDROID_WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static final String KEY_ARG_MESSAGE = "message";
    public static final String KEY_ARG_SCENE = "scene";
    public static final String KEY_ARG_TEXT = "text";
    public static final String KEY_ARG_MESSAGE_TITLE = "title";
    public static final String KEY_ARG_MESSAGE_DESCRIPTION = "description";
    public static final String KEY_ARG_MESSAGE_THUMB = "thumb";
    public static final String KEY_ARG_MESSAGE_MEDIA = "media";
    public static final String KEY_ARG_MESSAGE_MEDIA_TYPE = "type";
    public static final String KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL = "webpageUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_IMAGE = "image";
    public static final String KEY_ARG_MESSAGE_MEDIA_TEXT = "text";
    public static final String KEY_ARG_MESSAGE_MEDIA_MUSICURL = "musicUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_MUSICDATAURL = "musicDataUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_VIDEOURL = "videoUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_FILE = "file";
    public static final String KEY_ARG_MESSAGE_MEDIA_EMOTION = "emotion";
    public static final String KEY_ARG_MESSAGE_MEDIA_EXTINFO = "extInfo";
    public static final String KEY_ARG_MESSAGE_MEDIA_URL = "url";
    public static final String KEY_ARG_MESSAGE_MEDIA_USERNAME = "userName";
    public static final String KEY_ARG_MESSAGE_MEDIA_MINIPROGRAMTYPE = "miniprogramType";
    public static final String KEY_ARG_MESSAGE_MEDIA_MINIPROGRAM = "miniProgram";
    public static final String KEY_ARG_MESSAGE_MEDIA_PATH = "path";
    public static final String KEY_ARG_MESSAGE_MEDIA_WITHSHARETICKET = "withShareTicket";
    public static final String KEY_ARG_MESSAGE_MEDIA_HDIMAGEDATA = "hdImageData";

    public static final String ERROR_XIANLIAO_NOT_INSTALLED = "未安装闲聊";
    public static final String ERROR_INVALID_PARAMETERS = "参数格式错误";
    public static final String ERROR_SEND_REQUEST_FAILED = "发送请求失败";
    public static final String ERROR_XIANLIAO_RESPONSE_COMMON = "普通错误";
    public static final String ERROR_XIANLIAO_RESPONSE_USER_CANCEL = "用户点击取消并返回";
    public static final String ERROR_XIANLIAO_RESPONSE_SENT_FAILED = "发送失败";
    public static final String ERROR_XIANLIAO_RESPONSE_AUTH_DENIED = "授权失败";
    public static final String ERROR_XIANLIAO_RESPONSE_UNSUPPORT = "闲聊不支持";
    public static final String ERROR_XIANLIAO_RESPONSE_UNKNOWN = "未知错误";

    public static final int TYPE_WECHAT_SHARING_APP = 1;
    public static final int TYPE_WECHAT_SHARING_EMOTION = 2;
    public static final int TYPE_WECHAT_SHARING_FILE = 3;
    public static final int TYPE_WECHAT_SHARING_IMAGE = 4;
    public static final int TYPE_WECHAT_SHARING_MUSIC = 5;
    public static final int TYPE_WECHAT_SHARING_VIDEO = 6;
    public static final int TYPE_WECHAT_SHARING_WEBPAGE = 7;
    public static final int TYPE_WECHAT_SHARING_MINI = 8;

    public static final int SCENE_SESSION = 0;
    public static final int SCENE_TIMELINE = 1;
    public static final int SCENE_FAVORITE = 2;

    public static final int MAX_THUMBNAIL_SIZE = 320;

    protected static CallbackContext currentCallbackContext;
    protected static ISGAPI xlAPI;
    protected static String appId;
    protected static CordovaPreferences xl_preferences;


    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        String id = getAppId(preferences);
        // save app id
        saveAppId(cordova.getActivity(), id);
        initXlAPI();
        Log.d(TAG, "plugin initialized.");
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, String.format("%s is called. Callback ID: %s.", action, callbackContext.getCallbackId()));

        if (action.equals("share")) {
            return share(args, callbackContext);
        } else if (action.equals("sendAuthRequest")) {
            return sendAuthRequest(args, callbackContext);
        }else if (action.equals("isXlAppInstalled")) {
            return isInstalled(callbackContext);
        }
        return false;
    }

    /**
     * 分享
     * @param args
     * @param callbackContext
     * @return
     * @throws JSONException
     */
    protected boolean share(CordovaArgs args, final CallbackContext callbackContext)
            throws JSONException {
        final ISGAPI api = getXlAPI(cordova.getActivity());

        // check if installed
        if (!api.isSGAppInstalled()) {
            callbackContext.error(ERROR_XIANLIAO_NOT_INSTALLED);
            return true;
        }

        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        // run in background
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BaseReq req = null;
                    JSONObject msgObj = params.getJSONObject(KEY_ARG_MESSAGE);
                    if (msgObj.has(KEY_ARG_MESSAGE_MEDIA)) {
                        int mediaType = msgObj.getJSONObject(KEY_ARG_MESSAGE_MEDIA).getInt(KEY_ARG_MESSAGE_MEDIA_TYPE);
                        switch (mediaType) {//
                            case TYPE_WECHAT_SHARING_IMAGE://分享图片
                                req = createImageReq(msgObj);
                                break;
                            default://分享卡片
                                req = createCardReq(msgObj);
                                break;
                        }
                    } else {
                        req = createCardReq(params);
                    }
                    if (api.sendReq(req)) {
                        Log.i(TAG, "Message has been sent successfully.");
                    } else {
                        Log.i(TAG, "Message has been sent unsuccessfully.");
                        // clear callback context
                        currentCallbackContext = null;
                        // send error
                        callbackContext.error(ERROR_SEND_REQUEST_FAILED);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to build sharing message.", e);

                    // clear callback context
                    currentCallbackContext = null;
                    // send json exception error
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
                }

            }
        });

        // send no result
        sendNoResultPluginResult(callbackContext);

        return true;
    }

    /**
     * 图片分享
     * @param message
     * @return
     */
    private BaseReq createImageReq(JSONObject message) throws JSONException{
            String title = message.getString(KEY_ARG_MESSAGE_TITLE);
            String describe = message.getString(KEY_ARG_MESSAGE_DESCRIPTION);

            Bitmap image = getBitmap(message.getJSONObject(KEY_ARG_MESSAGE_MEDIA), KEY_ARG_MESSAGE_MEDIA_IMAGE,0);
            SGImageObject imageObject = new SGImageObject(image);
            SGMediaMessage msg = new SGMediaMessage();
            msg.mediaObject = imageObject;
            msg.title = title;
            msg.description = describe;
            SendMessageToSG.Req req = new SendMessageToSG.Req();
            req.transaction = SGConstants.T_IMAGE;
            req.mediaMessage = msg;
            req.scene = SendMessageToSG.Req.SGSceneSession;
            return req;
    }

    /**
     * 卡片分享
     * @param message
     * @return
     */
    private BaseReq createCardReq(JSONObject message) throws JSONException{
        String title = message.getString(KEY_ARG_MESSAGE_TITLE);
        String describe = message.getString(KEY_ARG_MESSAGE_DESCRIPTION);

        Bitmap thumb = getThumbnail(message,KEY_ARG_MESSAGE_THUMB);
        String shareUrl = message.getJSONObject(KEY_ARG_MESSAGE_MEDIA).getString(KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL);
        SGLinkObject linkObject;
        if(thumb != null){
            linkObject = new SGLinkObject(thumb);
        }else linkObject = new SGLinkObject();
        linkObject.shareUrl = shareUrl;

        SGMediaMessage msg = new SGMediaMessage();
        msg.mediaObject = linkObject;
        msg.title = title;
        msg.description = describe;

        SendMessageToSG.Req req = new SendMessageToSG.Req();
        req.transaction = SGConstants.T_LINK;
        req.mediaMessage = msg;
        req.scene = SendMessageToSG.Req.SGSceneSession;
        return req;
    }
    /**
     * 授权
     * @param args
     * @param callbackContext
     * @return
     */
    protected boolean sendAuthRequest(CordovaArgs args, CallbackContext callbackContext) {
        final ISGAPI api = getXlAPI(cordova.getActivity());

        final SendAuth.Req req = new SendAuth.Req();
        try {
        //    req.scope = args.getString(0);
            req.state = args.getString(1);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            req.state = "none";
        }

        if (api.sendReq(req)) {
            Log.i(TAG, "Auth request has been sent successfully.");
            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Auth request has been sent unsuccessfully.");
            // send error
            callbackContext.error(ERROR_SEND_REQUEST_FAILED);
        }

        return true;
    }

    /**
     * 是否安装闲聊
     * @param callbackContext
     * @return
     */
    protected boolean isInstalled(CallbackContext callbackContext) {
        final ISGAPI api = getXlAPI(cordova.getActivity());
        if (!api.isSGAppInstalled()) {
            callbackContext.success(0);
        } else {
            callbackContext.success(1);
        }
        return true;
    }


    /**
     * 初始化闲聊的API
     */
    protected void initXlAPI() {
        ISGAPI api = getXlAPI(cordova.getActivity());
        if(xl_preferences == null) {
            xl_preferences = preferences;
        }
        if (api != null) {
            api.registerApp(getAppId(preferences));
        }
    }

    /**
     * 获取闲聊API
     * @param ctx
     * @return
     */
    public static ISGAPI getXlAPI(Context ctx) {
        if (xlAPI == null) {
            String appId = getSavedAppId(ctx);

            if (!appId.isEmpty()) {
                xlAPI = SGAPIFactory.createSGAPI(ctx,appId);
            }
        }

        return xlAPI;
    }

    /**
     * 获取appid
     * @param f_preferences
     * @return
     */
    public static String getAppId(CordovaPreferences f_preferences) {
        if (appId == null) {
            if(f_preferences != null) {
                appId = f_preferences.getString(XLAPPID_PROPERTY_KEY, "");
            }else if(xl_preferences != null){
                appId = xl_preferences.getString(XLAPPID_PROPERTY_KEY, "");
            }
        }
        return appId;
    }

    /**
     * 保存 app id 到 SharedPreferences
     * @param ctx
     * @param id
     */
    public static void saveAppId(Context ctx, String id) {
        if (id!=null && id.isEmpty()) {
            return ;
        }

        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(XLAPPID_PROPERTY_KEY, id);
        editor.commit();
    }

    /**
     * Get saved app id
     * @param ctx
     * @return
     */
    public static String getSavedAppId(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(XLAPPID_PROPERTY_KEY, "");
    }

    public static CallbackContext getCurrentCallbackContext() {
        return currentCallbackContext;
    }

    private void sendNoResultPluginResult(CallbackContext callbackContext) {
        // save current callback context
        currentCallbackContext = callbackContext;

        // send no result and keep callback
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    protected Bitmap getThumbnail(JSONObject message, String key) {
        return getBitmap(message, key, MAX_THUMBNAIL_SIZE);
    }

    protected Bitmap getBitmap(JSONObject message, String key, int maxSize) {
        Bitmap bmp = null;
        String url = null;

        try {
            if (!message.has(key)) {
                return null;
            }

            url = message.getString(key);

            // get input stream
            InputStream inputStream = getFileInputStream(url);
            if (inputStream == null) {
                return null;
            }

            // decode it
            // @TODO make sure the image is not too big, or it will cause out of memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            bmp = BitmapFactory.decodeStream(inputStream, null, options);

            // scale
            if (maxSize > 0 && (options.outWidth > maxSize || options.outHeight > maxSize)) {

                Log.d(TAG, String.format("Bitmap was decoded, dimension: %d x %d, max allowed size: %d.",
                        options.outWidth, options.outHeight, maxSize));

                int width = 0;
                int height = 0;

                if (options.outWidth > options.outHeight) {
                    width = maxSize;
                    height = width * options.outHeight / options.outWidth;
                } else {
                    height = maxSize;
                    width = height * options.outWidth / options.outHeight;
                }

                Bitmap scaled = Bitmap.createScaledBitmap(bmp, width, height, true);
                bmp.recycle();

                int length = scaled.getRowBytes() * scaled.getHeight();

                if(length > (maxSize/10)*1024) {
                    scaled = compressImage(scaled,(maxSize/10));
                }

                bmp = scaled;
            }

            inputStream.close();

        } catch (JSONException e) {
            bmp = null;
            e.printStackTrace();
        } catch (IOException e) {
            bmp = null;
            e.printStackTrace();
        }

        return bmp;
    }


    /**
     * compress bitmap by quility
     */
    protected  Bitmap compressImage(Bitmap image,Integer maxSize) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 90;

        while (baos.toByteArray().length / 1024 > maxSize) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * Get input stream from a url
     */
    protected InputStream getFileInputStream(String url) {
        InputStream inputStream = null;
        try {

            if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    if (!cordova.hasPermission(ANDROID_WRITE_EXTERNAL_STORAGE)) {
                        cordova.requestPermission(this, REQUEST_CODE_ENABLE_PERMISSION, ANDROID_WRITE_EXTERNAL_STORAGE);
                    }
                }

                File file = Util.downloadAndCacheFile(webView.getContext(), url);

                if (file == null) {
                    Log.d(TAG, String.format("File could not be downloaded from %s.", url));
                    return null;
                }

                // url = file.getAbsolutePath();
                inputStream = new FileInputStream(file);

                Log.d(TAG, String.format("File was downloaded and cached to %s.", file.getAbsolutePath()));

            } else if (url.startsWith("data:image")) {  // base64 image

                String imageDataBytes = url.substring(url.indexOf(",") + 1);
                byte imageBytes[] = Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT);
                inputStream = new ByteArrayInputStream(imageBytes);

                Log.d(TAG, "Image is in base64 format.");

            } else if (url.startsWith(EXTERNAL_STORAGE_IMAGE_PREFIX)) { // external path

                url = Environment.getExternalStorageDirectory().getAbsolutePath() + url.substring(EXTERNAL_STORAGE_IMAGE_PREFIX.length());
                inputStream = new FileInputStream(url);

                Log.d(TAG, String.format("File is located on external storage at %s.", url));

            } else if (!url.startsWith("/")) { // relative path

                inputStream = cordova.getActivity().getApplicationContext().getAssets().open(url);

                Log.d(TAG, String.format("File is located in assets folder at %s.", url));

            } else {

                inputStream = new FileInputStream(url);

                Log.d(TAG, String.format("File is located at %s.", url));

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputStream;
    }
}
