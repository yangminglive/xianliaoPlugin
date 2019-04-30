//
//  CDVXianliao.h
//  cordova-plugin-jhxianliao
//
//
//

#import <Cordova/CDV.h>
#import "XianliaoApiManager.h"
#import "XianliaoApiObject.h"

enum  CDVWechatSharingType {
    CDVWXSharingTypeApp = 1,
    CDVWXSharingTypeEmotion,
    CDVWXSharingTypeFile,
    CDVWXSharingTypeImage,
    CDVWXSharingTypeMusic,
    CDVWXSharingTypeVideo,
    CDVWXSharingTypeWebPage
};

@interface CDVXianliao:CDVPlugin

@property (nonatomic, strong) NSString *currentCallbackId;
@property (nonatomic, strong) NSString *xlAppId;
@property (nonatomic, strong) NSString *xianliaoScheme;

- (void)isXlAppInstalled:(CDVInvokedUrlCommand *)command;
- (void)share:(CDVInvokedUrlCommand *)command;
- (void)sendAuthRequest:(CDVInvokedUrlCommand *)command;

@end
