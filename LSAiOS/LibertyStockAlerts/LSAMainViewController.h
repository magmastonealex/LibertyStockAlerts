//
//  LSAMainViewController.h
//  LibertyStockAlerts
//
//  Created by Alex Roth on 2014-07-24.
//  Copyright (c) 2014 MagmaStone. All rights reserved.
//

#import "LSAFlipsideViewController.h"
#import "LSAAppDelegate.h"
@interface LSAMainViewController : UIViewController <LSAFlipsideViewControllerDelegate, UIPopoverControllerDelegate, UITextFieldDelegate>

@property (strong, nonatomic) UIPopoverController *flipsidePopoverController;
@property (strong, nonatomic) LSAAppDelegate* appDelegate;
@end
