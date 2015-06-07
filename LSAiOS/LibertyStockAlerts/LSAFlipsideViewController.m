//
//  LSAFlipsideViewController.m
//  LibertyStockAlerts
//
//  Created by Alex Roth on 2014-07-24.
//  Copyright (c) 2014 MagmaStone. All rights reserved.
//

#import "LSAFlipsideViewController.h"
#import "LSAAppDelegate.h"

static void * XXContext = &XXContext;
static void * XXContext2 = &XXContext2;


@interface LSAFlipsideViewController ()
{
    float curPrice;
    float prevPrice;
    unsigned long long totalShares;
    float diff;
    LSAAppDelegate *appDelegate;
    UIView * chanView;
}
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;

@end



@implementation LSAFlipsideViewController
@synthesize scrollView;

- (void)awakeFromNib
{
    self.preferredContentSize = CGSizeMake(320.0, 480.0);
    
    [super awakeFromNib];
    //scrollView.contentSize;
}
- (void)viewWillAppear:(BOOL)animated{
 
}
-(void)viewWillDisappear:(BOOL)animated{
    NSLog(@"Saving values...");
    appDelegate.email=((UITextField*)[chanView viewWithTag:203]).text;
    appDelegate.userName=((UITextField*)[chanView viewWithTag:1]).text;
}
-(void)openLink{
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"http://www.mobileappcity.com"]];
}
- (void)viewDidLoad
{
    [super viewDidLoad];
    chanView = [[[NSBundle mainBundle] loadNibNamed:@"settingsView" owner:self options:nil] objectAtIndex:0];
        [scrollView addSubview:chanView];
    UISegmentedControl* seg = (UISegmentedControl*)[chanView viewWithTag:80];
    [seg addTarget:self action:@selector(MySegmentControlAction:) forControlEvents: UIControlEventValueChanged];
    scrollView.contentSize=chanView.frame.size;
    appDelegate = (LSAAppDelegate*)[[UIApplication sharedApplication] delegate];
    curPrice = [appDelegate lastPrice];
    prevPrice = [appDelegate prevPrice];
    totalShares = [appDelegate totalShares];
    [seg setSelectedSegmentIndex:[appDelegate tradePlatform]];
    [self recalculate];
    ((UITextField*)[chanView viewWithTag:203]).text = [appDelegate email];
    ((UITextField*)[chanView viewWithTag:1]).text = [appDelegate userName];
    [(UITextField*)[chanView viewWithTag:1] setDelegate:self];
    [(UITextField*)[chanView viewWithTag:203] setDelegate:self];
    //[chanView viewWithTag:201] - Get App
    //[chanView viewWithTag:202] - Save
    UIButton * saveButton = (UIButton*)[chanView viewWithTag:202];
    UIButton * getButton = (UIButton*)[chanView viewWithTag:201];
    [saveButton addTarget:appDelegate action:@selector(fakeSave) forControlEvents:UIControlEventTouchUpInside];
    [getButton addTarget:self action:@selector(openLink) forControlEvents:UIControlEventTouchUpInside];
    [appDelegate addObserver:self forKeyPath:@"lastPrice" options:0 context:XXContext];
    [appDelegate addObserver:self forKeyPath:@"totalShares" options:0 context:XXContext2];
    // Do any additional setup after loading the view, typically from a nib.
}
-(void)dealloc
{
    NSLog(@"Removed");
    [appDelegate removeObserver:self forKeyPath:@"lastPrice"];
    [appDelegate removeObserver:self forKeyPath:@"totalShares"];
}
-(BOOL) textFieldShouldReturn:(UITextField *)textField{
    [textField resignFirstResponder];
    return YES;
}
- (void)MySegmentControlAction:(UISegmentedControl *)segment
{
    appDelegate.tradePlatform=segment.selectedSegmentIndex;
}

-(void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if ([keyPath isEqualToString:@"lastPrice"]){
        curPrice = [object lastPrice];
        prevPrice = [object prevPrice];
        totalShares = [object totalShares];
        diff = curPrice-prevPrice;
        NSLog(@"%f,%f,%f",curPrice,prevPrice,diff);
        if(diff > 0.0f){
            [self recalculate];
        }
    } else {
        totalShares = [object totalShares];
        [self recalculate];
    }
}
-(void)recalculate{
    
    NSNumber *numberObj;
    if (totalShares < 1000) {
        numberObj = [NSNumber numberWithDouble:totalShares * curPrice];
    } else {
        NSInteger curPriceIntVal = [[NSString stringWithFormat:@"%.0f", (curPrice * 1000.0)] integerValue];
        numberObj = [NSNumber numberWithUnsignedLongLong:(totalShares * curPriceIntVal)/1000];
    }

    NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
    [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
    NSString *currencyString = [formatter stringFromNumber:numberObj];
    numberObj = [NSNumber numberWithDouble:(curPrice-prevPrice)/prevPrice];
    [formatter setNumberStyle:NSNumberFormatterPercentStyle];
    [formatter setRoundingMode:NSNumberFormatterRoundHalfUp];
    [formatter setMaximumFractionDigits:2];
    [formatter setMinimumFractionDigits:0];
    [formatter setMultiplier:@100];
    NSString *percentString = [formatter stringFromNumber:numberObj];
    
    
    ((UITextField*)[chanView viewWithTag:2]).text = currencyString;//[NSString stringWithFormat:@"$ %.0f", totalShares*curPrice];
    ((UITextField*)[chanView viewWithTag:3]).text = [NSString stringWithFormat:@"$%g Per Share", prevPrice];
    ((UITextField*)[chanView viewWithTag:4]).text = [NSString stringWithFormat:@"$%g Per Share", curPrice];
    ((UITextField*)[chanView viewWithTag:5]).text = percentString;//[NSString stringWithFormat:@"%.02f %%", (prevPrice/curPrice)*100];
    
    //[chanView viewWithTag:2]; total value
    //[chanView viewWithTag:3]; last price
    //[chanView viewWithTag:4]; current price
    //[chanView viewWithTag:5]; percent change
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Actions

- (IBAction)done:(id)sender
{
    [self.delegate flipsideViewControllerDidFinish:self];
}

@end
