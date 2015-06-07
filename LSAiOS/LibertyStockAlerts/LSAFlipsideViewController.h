//
//  LSAFlipsideViewController.h
//  LibertyStockAlerts
//
//  Created by Alex Roth on 2014-07-24.
//  Copyright (c) 2014 MagmaStone. All rights reserved.
//

#import <UIKit/UIKit.h>

@class LSAFlipsideViewController;

@protocol LSAFlipsideViewControllerDelegate
- (void)flipsideViewControllerDidFinish:(LSAFlipsideViewController *)controller;
@end

@interface LSAFlipsideViewController : UIViewController<UITextFieldDelegate>

@property (weak, nonatomic) id <LSAFlipsideViewControllerDelegate> delegate;

- (IBAction)done:(id)sender;

@end
