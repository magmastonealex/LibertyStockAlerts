//
//  LSAAppDelegate.h
//  LibertyStockAlerts
//
//  Created by Alex Roth on 2014-07-24.
//  Copyright (c) 2014 MagmaStone. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface LSAAppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (nonatomic) float lastPrice;
@property (nonatomic) float prevPrice;
@property (nonatomic) unsigned long long totalShares;
@property (nonatomic) NSInteger tradePlatform;
@property (strong, nonatomic) NSString* userName;
@property (strong, nonatomic) NSString* email;
-(void)makeTrade;
-(void)fakeSave;
@end
