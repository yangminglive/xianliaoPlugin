var exec = require('cordova/exec');

module.exports = {
    Scene: {
        SESSION:  0, // 聊天界面
    },

    Type: {
        APP:     1,// 已废弃
        EMOTION: 2,// 已废弃
        FILE:    3,// 已废弃
        IMAGE:   4,
        MUSIC:   5,
        VIDEO:   6,
        WEBPAGE: 7,
        MINI:    8
    },


    isInstalled: function (onSuccess, onError) {
        exec(onSuccess, onError, "JhXianliao", "isXlAppInstalled", []);
    },

    /**
     * Share a message to xianliao app
     *
     * @example
     * <code>
     * JhXianliao.share({
     *     message: {
     *        title: "Message Title",
     *        description: "Message Description(optional)",
     *        mediaTagName: "Media Tag Name(optional)",
     *        thumb: "http://YOUR_THUMBNAIL_IMAGE",
     *        media: {
     *            type: JhXianliao.Type.WEBPAGE,   // webpage
     *            webpageUrl: "https://github.com/xu-li/cordova-plugin-wechat"    // webpage
     *        }
     *    },
     *    scene: JhXianliao.Scene.TIMELINE   // share to Timeline
     * }, function () {
     *     alert("Success");
     * }, function (reason) {
     *     alert("Failed: " + reason);
     * });
     * </code>
     */
    share: function (message, onSuccess, onError) {
        exec(onSuccess, onError, "JhXianliao", "share", [message]);
    },

    /**
     * Sending an auth request to JhXianliao
     *
     * @example
     * <code>
     * JhXianliao.auth(function (response) { alert(response.code); });
     * </code>
     */
    auth: function (scope, state, onSuccess, onError) {
        if (typeof scope == "function") {
            // JhXianliao.auth(function () { alert("Success"); });
            // JhXianliao.auth(function () { alert("Success"); }, function (error) { alert(error); });
            return exec(scope, state, "JhXianliao", "sendAuthRequest");
        }

        if (typeof state == "function") {
            // JhXianliao.auth("snsapi_userinfo", function () { alert("Success"); });
            // JhXianliao.auth("snsapi_userinfo", function () { alert("Success"); }, function (error) { alert(error); });
            return exec(state, onSuccess, "JhXianliao", "sendAuthRequest", [scope]);
        }

        return exec(onSuccess, onError, "JhXianliao", "sendAuthRequest", [scope, state]);
    },

};
