//
//  LSAAppDelegate.m
//  LibertyStockAlerts
//
//  Created by Alex Roth on 2014-07-24.
//  Copyright (c) 2014 MagmaStone. All rights reserved.
//

#import "LSAAppDelegate.h"
#import "AFNetworking.h"
#import "LSAMainViewController.h"
#import "LSAFlipsideViewController.h"
#import <AudioToolbox/AudioToolbox.h>
#include<unistd.h>
#include<netdb.h>
@implementation LSAAppDelegate
@synthesize lastPrice,prevPrice,totalShares,tradePlatform,userName,email;
NSTimer *timer;
SystemSoundID soundID;
int isShowing=0;
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{

#ifdef __IPHONE_8_0
    if ([application respondsToSelector:@selector(registerUserNotificationSettings:)]) {
        [[UIApplication sharedApplication] registerUserNotificationSettings:[UIUserNotificationSettings settingsForTypes:(UIUserNotificationTypeAlert) categories:nil]];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    } else {
#endif
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes: (UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
#ifdef __IPHONE_8_0
    }
#endif

    //lastPrice,prevPrice,totalShares,tradePlatform,userName
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    lastPrice=[defaults floatForKey:@"lastPrice"];
    prevPrice=[defaults floatForKey:@"prevPrice"];
    totalShares=[[defaults objectForKey:@"totalShares"] unsignedLongLongValue];
    tradePlatform=[defaults integerForKey:@"tradePlatform"];
    userName=[defaults stringForKey:@"userName"];
    email=[defaults stringForKey:@"email"];
    
//    timer=[NSTimer scheduledTimerWithTimeInterval:5
//                                     target:self
//                                   selector:@selector(doUpdate:)
//                                   userInfo:nil
//                                    repeats:YES];
    [self doUpdate:nil];
    
    /*dispatch_queue_t myQueue = dispatch_queue_create("Web Queue",NULL);
    dispatch_async(myQueue, ^{
        // Perform long running process
        
        //dispatch_async(dispatch_get_main_queue(), ^{
        //
        //
        //});
    });*/
    
    return YES;
}
-(void)doUpdate:(NSTimer*)timer{
    NSLog(@"Update:");
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    manager.responseSerializer.acceptableContentTypes = [NSSet setWithObject:@"text/html"];
    NSDictionary *parameters = @{};
    
    [manager POST:@"http://sharealertapp.com/trade/api/currentprice.php" parameters:parameters success:^(AFHTTPRequestOperation *operation, id responseObject) {
        
            self.prevPrice = [responseObject[@"last_price"] floatValue];
            self.lastPrice = [responseObject[@"current_price"] floatValue];
        
        if([responseObject[@"current_price"] floatValue] > self.lastPrice){
            dispatch_async(dispatch_get_main_queue(), ^{
                if(isShowing==0){
                UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"$Cha-Ching$"
                                                                message:@"Liberty's Stock has gone up!"
                                                               delegate:self
                                                      cancelButtonTitle:@"OK"
                                                      otherButtonTitles:nil];
                isShowing=1;
                [alert show];
                }
                NSString *path = [[NSBundle mainBundle] pathForResource:@"cas" ofType:@"aiff"];
                
                AudioServicesCreateSystemSoundID((__bridge CFURLRef)[NSURL fileURLWithPath:path], &soundID);
                AudioServicesPlaySystemSound(soundID);

            });
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Error: %@", error);
    }];
}
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    isShowing=0;
}
- (void)application:(UIApplication *)app didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSString *token = [[deviceToken description] stringByTrimmingCharactersInSet: [NSCharacterSet characterSetWithCharactersInString:@"<>"]];
    token = [token stringByReplacingOccurrencesOfString:@" " withString:@""];
  //  dispatch_queue_t myQueue = dispatch_queue_create("Web Queue",NULL);
  //  dispatch_async(myQueue, ^{
       /* NSURL *aUrl = [NSURL URLWithString:@"http://sharealertapp.com/trade/api/registerdevice.php/"];
        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:aUrl cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:60.0];
        [request setHTTPMethod:@"POST"];
        NSString *postString = [NSString stringWithFormat:@"token=%@&deviceid=%@&is_android=0", token, [[[UIDevice currentDevice] identifierForVendor] UUIDString]];
        NSLog(@"Post: %@",postString);
        [request setHTTPBody:[postString dataUsingEncoding:NSUTF8StringEncoding]];
        NSHTTPURLResponse * resp;
        NSError *err;
        [NSURLConnection sendSynchronousRequest:request returningResponse:&resp error:&err];
        if(err){
            NSLog(@"Error: %@", [err localizedDescription]);
        }else{
            //[resp finalize];
            //[resp ];
        }
        //NSURLConnection *connection= [[NSURLConnection alloc] initWithRequest:request delegate:self];
        //[connection sy];
        */
        
//    });
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    manager.responseSerializer.acceptableContentTypes = [NSSet setWithObject:@"text/html"];
    NSDictionary *parameters = @{@"token": token,@"deviceid":[[[UIDevice currentDevice] identifierForVendor] UUIDString],@"is_android":@"0"};
    
    [manager POST:@"http://sharealertapp.com/trade/api/registerdevice.php" parameters:parameters success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"JSON: %@", responseObject);
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Error: %@", error);
    }];
    
}

- (void)application:(UIApplication *)app didFailToRegisterForRemoteNotificationsWithError:(NSError *)err {
    NSString *str = [NSString stringWithFormat: @"Error: %@", err];
    NSLog(@"%@",str);
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    AudioServicesDisposeSystemSoundID(soundID);
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    //lastPrice,prevPrice,totalShares,tradePlatform,userName
    [defaults setObject:userName forKey:@"userName"];
    [defaults setInteger:tradePlatform forKey:@"tradePlatform"];
    [defaults setObject:email forKey:@"email"];
    [defaults setObject:@(totalShares) forKey:@"totalShares"];
    [defaults setFloat:prevPrice forKey:@"prevPrice"];
    [defaults setFloat:lastPrice forKey:@"lastPrice"];
    [defaults synchronize];
}
- (void)makeTrade{
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    manager.responseSerializer.acceptableContentTypes = [NSSet setWithObject:@"text/html"];
    //[NSDate date];
    NSDateFormatter* format = [[NSDateFormatter alloc] init];
    [format setDateFormat:@"dd-MM-YYYY HH:mm:ss"];
    NSString * dateString = [format stringFromDate:[NSDate date]];
    
    NSNumber *numberObj;
    if (totalShares < 1000) {
        numberObj = [NSNumber numberWithDouble:totalShares * lastPrice];
    } else {
        NSInteger curPriceIntVal = [[NSString stringWithFormat:@"%.0f", (lastPrice * 1000.0)] integerValue];
        numberObj = [NSNumber numberWithUnsignedLongLong:(totalShares * curPriceIntVal)/1000];
    }
    
    
    NSString *currencyString = [NSString stringWithFormat:@"%@",numberObj];
    NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
    
    numberObj = [NSNumber numberWithDouble:(lastPrice-prevPrice)/prevPrice];
    [formatter setNumberStyle:NSNumberFormatterPercentStyle];
    [formatter setRoundingMode:NSNumberFormatterRoundHalfUp];
    [formatter setMaximumFractionDigits:2];
    [formatter setMinimumFractionDigits:0];
    [formatter setMultiplier:@100];
    NSString *percentString = [formatter stringFromNumber:numberObj];
    
    float diff = lastPrice-prevPrice;
    Boolean b=false;
    if(diff<0){
        b=true;
        diff=diff*-1;
    }
    NSNumber *numberObj_made;
    if (totalShares < 1000) {
        numberObj = [NSNumber numberWithDouble:totalShares*diff];
    } else {
        NSInteger diffIntVal = [[NSString stringWithFormat:@"%.0f", (diff * 1000.0)] integerValue];
        numberObj_made = [NSNumber numberWithLongLong:((long long)totalShares*diffIntVal)/1000];
    }
    
    //NSLog(@"RealValue %llu", totalShares*(int)diff);
    NSString *sign=@"";
    if(b){
        sign=@"-";
    }
    NSString *currencyString_made = [NSString stringWithFormat:@"%@%@", sign,numberObj_made];
    
    NSString *emailString=self.email;
    
    NSDictionary *parameters = @{@"home_shares":[NSString stringWithFormat:@"%llu", totalShares],
                                 @"home_made":currencyString_made,
                                 @"belongsto":(userName.length > 0)? userName : @"xxx",
                                 @"worthnow":currencyString,
                                 @"email_address":[NSString stringWithFormat:@"%@", emailString],
                                 @"lastprice":[NSString stringWithFormat:@"%.02f", prevPrice],
                                 @"currentprice":[NSString stringWithFormat:@"%.02f", lastPrice],
                                 @"percentchange":[NSString stringWithFormat:@"%@", percentString],
                                 @"device_token":[[[UIDevice currentDevice] identifierForVendor] UUIDString],
                                 @"tradinglink":[NSString stringWithFormat:@"%ld", (long)tradePlatform],
                                 @"date_time":dateString};
    [manager POST:@"http://sharealertapp.com/trade/api/madetrades.php" parameters:parameters success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"JSON: %@", responseObject);
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Error: %@", error);
    }];
    
    
    
    
    
    if(tradePlatform==0){
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"https://us.etrade.com/home"]];
    }else{
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"https://www.commsec.com.au/"]];
    }

}

- (void)fakeSave{
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    manager.responseSerializer.acceptableContentTypes = [NSSet setWithObject:@"text/html"];
    //[NSDate date];
    NSDateFormatter* format = [[NSDateFormatter alloc] init];
    [format setDateFormat:@"dd-MM-YYYY HH:mm:ss"];
    NSString * dateString = [format stringFromDate:[NSDate date]];
    
    NSNumber *numberObj;
    if (totalShares < 1000) {
        numberObj = [NSNumber numberWithDouble:totalShares * lastPrice];
    } else {
        NSInteger curPriceIntVal = [[NSString stringWithFormat:@"%.0f", (lastPrice * 1000.0)] integerValue];
        numberObj = [NSNumber numberWithUnsignedLongLong:(totalShares * curPriceIntVal)/1000];
    }
    
    
    NSString *currencyString = [NSString stringWithFormat:@"%@",numberObj];
    NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
    
    numberObj = [NSNumber numberWithDouble:(lastPrice-prevPrice)/prevPrice];
    [formatter setNumberStyle:NSNumberFormatterPercentStyle];
    [formatter setRoundingMode:NSNumberFormatterRoundHalfUp];
    [formatter setMaximumFractionDigits:2];
    [formatter setMinimumFractionDigits:0];
    [formatter setMultiplier:@100];
    NSString *percentString = [formatter stringFromNumber:numberObj];
    
    float diff = lastPrice-prevPrice;
    Boolean b=false;
    if(diff<0){
        b=true;
        diff=diff*-1;
    }
    NSNumber *numberObj_made;
    if (totalShares < 1000) {
        numberObj = [NSNumber numberWithDouble:totalShares*diff];
    } else {
        NSInteger diffIntVal = [[NSString stringWithFormat:@"%.0f", (diff * 1000.0)] integerValue];
        numberObj_made = [NSNumber numberWithLongLong:((long long)totalShares*diffIntVal)/1000];
    }
    
    //NSLog(@"RealValue %llu", totalShares*(int)diff);
    NSString *sign=@"";
    if(b){
        sign=@"-";
    }
    NSString *currencyString_made = [NSString stringWithFormat:@"%@%@", sign,numberObj_made];
    
    NSString *emailString=self.email;
    
    NSDictionary *parameters = @{@"home_shares":[NSString stringWithFormat:@"%llu", totalShares],
                                 @"home_made":currencyString_made,
                                 @"belongsto":(userName.length > 0)? userName : @"xxx",
                                 @"worthnow":currencyString,
                                 @"email_address":[NSString stringWithFormat:@"%@", emailString],
                                 @"lastprice":[NSString stringWithFormat:@"%.02f", prevPrice],
                                 @"currentprice":[NSString stringWithFormat:@"%.02f", lastPrice],
                                 @"percentchange":[NSString stringWithFormat:@"%@", percentString],
                                 @"device_token":[[[UIDevice currentDevice] identifierForVendor] UUIDString],
                                 @"tradinglink":[NSString stringWithFormat:@"%d", -1],
                                 @"date_time":dateString};
    [manager POST:@"http://sharealertapp.com/trade/api/madetrades.php" parameters:parameters success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"JSON: %@", responseObject);
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Error: %@", error);
    }];    
}


- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    
    application.applicationIconBadgeNumber = 0;
    
    if (![self isNetworkAvailable]) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"No Network connection!"
                                                        message:@"Connect to a network for this app to work"
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        
        [alert show];
    }
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    //lastPrice,prevPrice,totalShares,tradePlatform,userName
    [defaults setObject:userName forKey:@"userName"];
    [defaults setObject:email forKey:@"email"];
    [defaults setInteger:tradePlatform forKey:@"tradePlatform"];
    [defaults setObject:@(totalShares) forKey:@"totalShares"];
    [defaults setFloat:prevPrice forKey:@"prevPrice"];
    [defaults setFloat:lastPrice forKey:@"lastPrice"];
    [defaults synchronize];
}
-(void) application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo{
    NSLog(@"Remote");
    
    self.prevPrice=[[userInfo objectForKey:@"last_price"] floatValue];
    self.lastPrice=[[userInfo objectForKey:@"current_price"] floatValue];
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setFloat:self.prevPrice forKey:@"prevPrice"];
    [defaults setFloat:self.lastPrice forKey:@"lastPrice"];
    [defaults synchronize];
    if(isShowing==0 && (self.lastPrice-self.prevPrice)>0){
        UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"$Cha-Ching$"
                                                        message:@"Liberty's Stock has gone up!"
                                                       delegate:self
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        isShowing=1;
        [alert show];
    }
    
    if (application.applicationState == UIApplicationStateActive) {
        
        NSString *path = [[NSBundle mainBundle] pathForResource:@"cas" ofType:@"aiff"];
        if((self.lastPrice-self.prevPrice)<0){
            path=[[NSBundle mainBundle] pathForResource:@"lowdong" ofType:@"aiff"];
        }
        AudioServicesCreateSystemSoundID((__bridge CFURLRef)[NSURL fileURLWithPath:path], &soundID);
        AudioServicesPlaySystemSound(soundID);
    }
    else {
        
    }
    
}
-(BOOL)isNetworkAvailable
{
    char *hostname;
    struct hostent *hostinfo;
    hostname = "google.com";
    hostinfo = gethostbyname (hostname);
    if (hostinfo == NULL){
        NSLog(@"-> no connection!\n");
        return NO;
    }
    else{
        NSLog(@"-> connection established!\n");
        return YES;
    }
}
@end
