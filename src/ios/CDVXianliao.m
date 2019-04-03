//
//  CDVXianliao.m
//  cordova-plugin-jhxianliao
//
//
//

#import "CDVXianliao.h"

static int const MAX_THUMBNAIL_SIZE = 320;

@implementation CDVXianliao

#pragma mark "API"
- (void)pluginInitialize {
    NSString* appId = [[self.commandDelegate settings] objectForKey:@"xlappid"];

    if (appId && ![appId isEqualToString:self.xlAppId]) {
        self.xlAppId = appId;
        self.xianliaoScheme = [NSString stringWithFormat:@"xianliao%@",appId];
        [XianliaoApiManager registerApp: appId];

        NSLog(@"cordova-plugin-jhxianliao has been initialized. Xianliao SDK Version: %@. APP_ID: %@.", [XianliaoApiManager getApiVersion], appId);
    }
}

- (void)isXlAppInstalled:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:[XianliaoApiManager isInstallXianliao]];

    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
}

- (void)share:(CDVInvokedUrlCommand *)command
{
    // if not installed
    if (![XianliaoApiManager isInstallXianliao])
    {
        [self failWithCallbackID:command.callbackId withMessage:@"未安装闲聊"];
        return ;
    }

    // check arguments
    NSDictionary *params = [command.arguments objectAtIndex:0];
    if (!params)
    {
        [self failWithCallbackID:command.callbackId withMessage:@"参数格式错误"];
        return ;
    }
	
    // save the callback id
    self.currentCallbackId = command.callbackId;
  //  XianliaoShareBaseObject* req = nil;

    // message or text?
    NSDictionary *message = [params objectForKey:@"message"];

    if (message)
    {
        // async
        [self.commandDelegate runInBackground:^{
            XianliaoShareBaseObject* req = [self buildShareMessage:message];
            [XianliaoApiManager share:req fininshBlock:^(XianliaoShareCallBackType callBackType) {
                NSLog(@"callBackType: %ld",(long)callBackType);
                switch(callBackType){
                    case XianliaoShareSuccesslType:
                        NSLog(@"分享成功，但是没有做其他处理");
                        break;
                    default:
                        [self failWithCallbackID:command.callbackId withMessage:@"发送请求失败"];
                        break;
                        
                }
                self.currentCallbackId = nil;
            }];
        }];
    }
    else
    {
            [self failWithCallbackID:command.callbackId withMessage:@"请求参数错误"];
            self.currentCallbackId = nil;
    }
}

- (void)sendAuthRequest:(CDVInvokedUrlCommand *)command
{
    self.currentCallbackId = command.callbackId;
    [XianliaoApiManager loginState:nil fininshBlock:^(XianliaoLoginCallBackType callBackType, NSString *code, NSString *state) {
        NSLog(@"callbackType:%lu, code:%@, state:%@",callBackType,code,state);
        [self onResp:callBackType code:code state:state];
    }];
    
}


#pragma mark "XlApiDelegate"

/**
 * Not implemented
 */


- (void)onResp:(XianliaoLoginCallBackType) callBackType code:(NSString *)code state:(NSString *)state
{
    BOOL success = NO;
    NSString *message = @"Unknown";
    NSDictionary *response = nil;
    switch (callBackType)
    {
        case XianliaoLoginSuccessType:
            success = YES;
            break;

        case XianliaoLoginErrorType:
            message = @"授权错误";
            break;

        case XianliaoLoginCancelType:
            message = @"用户点击取消并返回";
            break;
        default:
            message = @"未知错误";
    }

    if (success)
    {
            response = @{
                         @"code": code != nil ? code : @"",
                         @"state": state != nil ? state : @""
                         };

            CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];

            [self.commandDelegate sendPluginResult:commandResult callbackId:self.currentCallbackId];
    }
    
    else
    {
        [self failWithCallbackID:self.currentCallbackId withMessage:message];
    }

    [self pluginInitialize];
    self.currentCallbackId = nil;
}

#pragma mark "CDVPlugin Overrides"

- (void)handleOpenURL:(NSNotification *)notification
{
    NSURL* url = [notification object];

    if ([url isKindOfClass:[NSURL class]] && [url.scheme isEqualToString:self.xianliaoScheme])
    {
        [XianliaoApiManager handleOpenURL:url];
    }
}

#pragma mark "Private methods"

- (XianliaoShareBaseObject *)buildShareMessage:(NSDictionary *)message
{
    NSString *title = [message objectForKey:@"title"];
    NSString *description = [message objectForKey:@"description"];
    
    NSDictionary *media = [message objectForKey:@"media"];
    // check types
    NSInteger type = [[media objectForKey:@"type"] integerValue];
    XianliaoShareImageObject *imageMessage = [[XianliaoShareImageObject alloc] init];
    XianliaoShareLinkObject *linkMessage = [[XianliaoShareLinkObject alloc] init];
    switch (type)
    {
        case CDVWXSharingTypeImage:
            imageMessage.imageData = [self getNSDataFromURL:[media objectForKey:@"image"]];
            return imageMessage;
            
        case CDVWXSharingTypeWebPage:
        default:
            linkMessage.title = title;
            linkMessage.linkDescription = description;
            if ([message objectForKey:@"thumb"])
            {
                linkMessage.imageData = [self getNSDataFromURL:[message objectForKey:@"thumb"]];
            }
            linkMessage.url=[media objectForKey:@"webpageUrl"];
            return linkMessage;
    }
    
}

- (NSData *)getNSDataFromURL:(NSString *)url
{
    NSData *data = nil;

    if ([url hasPrefix:@"http://"] || [url hasPrefix:@"https://"])
    {
        data = [NSData dataWithContentsOfURL:[NSURL URLWithString:url]];
    }
    else if ([url hasPrefix:@"data:image"])
    {
        // a base 64 string
        NSURL *base64URL = [NSURL URLWithString:url];
        data = [NSData dataWithContentsOfURL:base64URL];
    }
    else if ([url rangeOfString:@"temp:"].length != 0)
    {
        url =  [NSTemporaryDirectory() stringByAppendingPathComponent:[url componentsSeparatedByString:@"temp:"][1]];
        data = [NSData dataWithContentsOfFile:url];
    }
    else
    {
        // local file
        url = [[NSBundle mainBundle] pathForResource:[url stringByDeletingPathExtension] ofType:[url pathExtension]];
        data = [NSData dataWithContentsOfFile:url];
    }

    return data;
}

- (UIImage *)getUIImageFromURL:(NSString *)url
{
    NSData *data = [self getNSDataFromURL:url];
    UIImage *image = [UIImage imageWithData:data];

    if (image.size.width > MAX_THUMBNAIL_SIZE || image.size.height > MAX_THUMBNAIL_SIZE)
    {
        CGFloat width = 0;
        CGFloat height = 0;

        // calculate size
        if (image.size.width > image.size.height)
        {
            width = MAX_THUMBNAIL_SIZE;
            height = width * image.size.height / image.size.width;
        }
        else
        {
            height = MAX_THUMBNAIL_SIZE;
            width = height * image.size.width / image.size.height;
        }

        // scale it
        UIGraphicsBeginImageContext(CGSizeMake(width, height));
        [image drawInRect:CGRectMake(0, 0, width, height)];
        UIImage *scaled = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();

        return scaled;
    }

    return image;
}

- (void)successWithCallbackID:(NSString *)callbackID
{
    [self successWithCallbackID:callbackID withMessage:@"OK"];
}

- (void)successWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

- (void)failWithCallbackID:(NSString *)callbackID withError:(NSError *)error
{
    [self failWithCallbackID:callbackID withMessage:[error localizedDescription]];
}

- (void)failWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}


@end
