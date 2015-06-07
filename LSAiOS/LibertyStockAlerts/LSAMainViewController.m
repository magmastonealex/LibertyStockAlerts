//
//  LSAMainViewController.m
//  LibertyStockAlerts
//
//  Created by Alex Roth on 2014-07-24.
//  Copyright (c) 2014 MagmaStone. All rights reserved.
//

#import "LSAMainViewController.h"
#import "LSAAppDelegate.h"

static void * XXContext = &XXContext;

@interface LSAMainViewController ()
{
    float curPrice;
    float diff;
    CGPoint originalCenter;
}
@property (weak, nonatomic) IBOutlet UILabel *nameLabel;
@property (weak, nonatomic) IBOutlet UITextField *sharesCount;
@property (weak, nonatomic) IBOutlet UILabel *madeLabel;
@property (weak, nonatomic) IBOutlet UITextField *madeAmount;
@end

@implementation LSAMainViewController

@synthesize flipsidePopoverController;
@synthesize appDelegate;


- (void)viewDidLoad
{
    [super viewDidLoad];
    appDelegate.window.backgroundColor = [UIColor whiteColor];
    [self.navigationController.navigationBar setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys: [UIFont fontWithName:@"HelveticaNeue" size:20.0f], NSFontAttributeName, nil]];
    self.navigationController.navigationBar.tintColor = [UIColor colorWithRed:12/255.f green:54/255.f blue:140/255.f alpha:1.0];
    
    
    self.appDelegate = (LSAAppDelegate*)[[UIApplication sharedApplication] delegate];
    UIToolbar* keyboardDoneButtonView = [[UIToolbar alloc] init];
    [keyboardDoneButtonView sizeToFit];
    UIBarButtonItem* doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self
                                                                  action:@selector(doneClicked:)];
    UIBarButtonItem *flexibleItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    [keyboardDoneButtonView setItems:[NSArray arrayWithObjects:flexibleItem, doneButton, nil]];
    self.sharesCount.inputAccessoryView = keyboardDoneButtonView;
    if([appDelegate totalShares] > 0){
        NSNumberFormatter *formatter = [NSNumberFormatter new];
        [formatter setGroupingSeparator:@","];
        [formatter setNumberStyle:NSNumberFormatterDecimalStyle];
        [formatter setUsesGroupingSeparator:TRUE];
        NSNumber *num = [NSNumber numberWithLongLong:[appDelegate totalShares]];
        [formatter setGroupingSize:3];
        if([appDelegate totalShares]<1){
            self.sharesCount.text=@"";
        }else{
            self.sharesCount.text = [NSString stringWithFormat:@"%@ Shares",[formatter stringFromNumber:num]];
        }
    }else{
        self.sharesCount.text=@"";
    }
    NSString * uName = [appDelegate userName];
    if(uName == nil || uName.length < 1){
        self.nameLabel.text = @"You have";
    }else{
        self.nameLabel.text = [NSString stringWithFormat:@"%@, You have", uName];
    }
    
    [appDelegate addObserver:self forKeyPath:@"lastPrice" options:0 context:XXContext];
    [appDelegate addObserver:self forKeyPath:@"userName" options:0 context:XXContext];
    [_sharesCount addTarget:self action:@selector(textChanged:) forControlEvents:UIControlEventEditingChanged];
    [_sharesCount setDelegate:self];
    
    curPrice = [appDelegate lastPrice];
    float prevPrice = [appDelegate prevPrice];
    diff = curPrice-prevPrice;
    NSLog(@"%f,%f,%f",curPrice,prevPrice,diff);
    if(diff > 0.0f){
        [self recalculate];
    }

    
    originalCenter = self.view.center;
    
    
	// Do any additional setup after loading the view, typically from a nib.
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillShow:)
                                                 name:UIKeyboardWillShowNotification
                                               object:nil];
}
- (void)keyboardWillShow:(NSNotification *)notification
{
    if([appDelegate totalShares]< 1){
        self.sharesCount.text = @"";
    }else{
        self.sharesCount.text = [NSString stringWithFormat:@"%llu",[appDelegate totalShares]];
    }
    // Get the size of the keyboard.
    CGSize keyboardSize = [[[notification userInfo] objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    
    //Given size may not account for screen rotation
    CGFloat height = MIN(keyboardSize.height, keyboardSize.width);
    //int width = MAX(keyboardSize.height,keyboardSize.width);
    
    [UIView animateWithDuration:0.3 animations:^{
        
        self.view.center=CGPointMake(originalCenter.x, originalCenter.y-ceil(height/2) - 10.f);
        
    } completion:^(BOOL finished) {
        
    }];
    
}
- (IBAction)doneClicked:(id)sender
{
    NSLog(@"Done Clicked.");
    
    if ([self.sharesCount.text length] == 0 || [self.sharesCount.text integerValue] == 0) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Please enter correct shares"
                                                        message:@""
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    NSNumberFormatter *formatter = [NSNumberFormatter new];
    [formatter setGroupingSeparator:@","];
    [formatter setNumberStyle:NSNumberFormatterDecimalStyle];
    [formatter setUsesGroupingSeparator:TRUE];
    unsigned long long value = strtoull([self.sharesCount.text UTF8String], NULL, 0);
    NSNumber *num = [NSNumber numberWithLongLong:value];
    [formatter setGroupingSize:3];
    self.sharesCount.text = [NSString stringWithFormat:@"%@ Shares",[formatter stringFromNumber:num]];
    [self.view endEditing:YES];
}
- (void)textFieldDidEndEditing:(UITextField *)textField
{

    [UIView animateWithDuration:0.3 animations:^{
        self.view.center=CGPointMake(originalCenter.x, originalCenter.y);
        
    } completion:^(BOOL finished) {
        
    }];
    
    
    /* resign first responder, hide keyboard, move views */
}

-(void)viewWillUnload{
    NSLog(@"Removed");
    [appDelegate removeObserver:self forKeyPath:@"lastPrice"];
    [appDelegate removeObserver:self forKeyPath:@"userName"];
}


-(void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if([keyPath isEqualToString:@"lastPrice"]) {
        curPrice = [object lastPrice];
        float prevPrice = [object prevPrice];
        diff = curPrice-prevPrice;
        
        NSLog(@"%f,%f,%f",curPrice,prevPrice,diff);
            [self recalculate];
    } else {
        NSString * uName = [object userName];
        self.nameLabel.text = [NSString stringWithFormat:@"%@, you have", uName];
        NSNumberFormatter *formatter = [NSNumberFormatter new];
        [formatter setGroupingSeparator:@","];
        [formatter setNumberStyle:NSNumberFormatterDecimalStyle];
        [formatter setUsesGroupingSeparator:TRUE];
        //unsigned long long value = strtoull([textField.text UTF8String], NULL, 0);
        NSNumber *num = [NSNumber numberWithLongLong:[object totalShares]];
        [formatter setGroupingSize:3];
        if([appDelegate totalShares]<1){
            self.sharesCount.text=@"";
        }else{
            self.sharesCount.text = [NSString stringWithFormat:@"%@ Shares",[formatter stringFromNumber:num]];
        }

    }
}
-(BOOL) textFieldShouldReturn:(UITextField *)textField{
    
    [textField resignFirstResponder];
    return YES;
}
-(BOOL) textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if (textField.text.length + string.length > 19) {
        return NO;
    }
    
    return YES;
}
-(void)recalculate{
    unsigned long long numShares = [appDelegate totalShares];
    NSNumber *numberObj;
    if (numShares < 1000) {
        numberObj = [NSNumber numberWithDouble:numShares*diff];
        NSLog(@"%f, %f", diff, diff);
    } else {
        NSInteger diffIntVal = [[NSString stringWithFormat:@"%.0f", (diff * 1000.0)] integerValue];
        numberObj = [NSNumber numberWithUnsignedLongLong:(numShares*diffIntVal)/1000];
        NSLog(@"%ld, %f", (long)diffIntVal, diff);
    }
    
    
    NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
    [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
    NSString *currencyString = [formatter stringFromNumber:numberObj];
    
    //NSUInteger madeAmount = numShares*diff;
    //NSString* madeAmountS=[NSString stringWithFormat:@"$ %.0f", madeAmount];
    NSLog(@"Made: %@", currencyString);
    if([numberObj longLongValue]<1){
        self.madeAmount.text=@"";
    }else{
        self.madeAmount.text = currencyString;
    }
    if(diff < 0){
        self.madeAmount.text=@"";
        self.madeLabel.text=@"";
    }else{
        self.madeLabel.text=@"You just made!";
    }
}

-(void)textChanged:(UITextField *)textField {
    
    NSLog(@"current totalShares %llu", appDelegate.totalShares);
    unsigned long long value = strtoull([textField.text UTF8String], NULL, 0);
    NSLog(@"new totalShares %llu", value);
    appDelegate.totalShares = value;
    [self recalculate];
}
-(void)viewWillAppear:(BOOL)animated{
    NSNumberFormatter *formatter = [NSNumberFormatter new];
    [formatter setGroupingSeparator:@","];
    [formatter setNumberStyle:NSNumberFormatterDecimalStyle];
    [formatter setUsesGroupingSeparator:TRUE];
    NSNumber *num = [NSNumber numberWithLongLong:[appDelegate totalShares]];
    [formatter setGroupingSize:3];
    if([appDelegate totalShares]<1){
        self.sharesCount.text=@"";
    }else{
        self.sharesCount.text = [NSString stringWithFormat:@"%@ Shares",[formatter stringFromNumber:num]];
    }
    
}

- (IBAction)makeTrade:(id)sender {
    [appDelegate makeTrade];
    /**/
    
/*    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"--Beta Software--"
                                                    message:@"Created by Alex Roth at MagmaStone!"
                                                   delegate:nil
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];*/
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Flipside View Controller

- (void)flipsideViewControllerDidFinish:(LSAFlipsideViewController *)controller
{
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        [self dismissViewControllerAnimated:YES completion:nil];
    } else {
        [self.flipsidePopoverController dismissPopoverAnimated:YES];
    }
    NSNumberFormatter *formatter = [NSNumberFormatter new];
    [formatter setGroupingSeparator:@","];
    [formatter setNumberStyle:NSNumberFormatterDecimalStyle];
    [formatter setUsesGroupingSeparator:TRUE];
    NSNumber *num = [NSNumber numberWithLongLong:[appDelegate totalShares]];
    [formatter setGroupingSize:3];
    self.sharesCount.text = [NSString stringWithFormat:@"%@ Shares",[formatter stringFromNumber:num]];

}

- (void)popoverControllerDidDismissPopover:(UIPopoverController *)popoverController
{
    self.flipsidePopoverController = nil;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    
    [self.sharesCount resignFirstResponder];
    
    if ([[segue identifier] isEqualToString:@"showAlternate"]) {
        [[segue destinationViewController] setDelegate:self];
        
        if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
            UIPopoverController *popoverController = [(UIStoryboardPopoverSegue *)segue popoverController];
            self.flipsidePopoverController = popoverController;
            popoverController.delegate = self;
        }
    }
}

- (IBAction)togglePopover:(id)sender
{
    if (self.flipsidePopoverController) {
        [self.flipsidePopoverController dismissPopoverAnimated:YES];
        self.flipsidePopoverController = nil;
    } else {
        [self performSegueWithIdentifier:@"showAlternate" sender:sender];
    }
}

@end
