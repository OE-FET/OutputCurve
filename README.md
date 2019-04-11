![](https://i.imgur.com/YlXnUTz.png)

# JISA Output Curve Example Code
This repository contains three Kotlin programs that use JISA to perform an output measurement on a transistor using SMUs.

1. Basic, no GUI [code](https://github.com/OE-FET/OutputCurve/blob/master/src/basic/Basic.kt)
2. Simple GUI [code](https://github.com/OE-FET/OutputCurve/blob/master/src/basicGUI/BasicGUI.kt)
3. Full GUI [code](https://github.com/OE-FET/OutputCurve/blob/master/src/fullGUI/FullGUI.kt)

## 1. Basic Program
This sweeps two SMUs through a range then dumps the output into `data.csv`. Running the program looks like:
```
Connecting to instruments...
Performing Measurement...
SG: 0.0 V, SD: 0.0 V
SG: 0.0 V, SD: 30.0 V
SG: 0.0 V, SD: 60.0 V
SG: 10.0 V, SD: 0.0 V
SG: 10.0 V, SD: 30.0 V
...
SG: 60.0 V, SD: 30.0 V
SG: 60.0 V, SD: 60.0 V
Measurement Complete. Outputting to 'data.csv'...
All done. Goodbye.
```

## 2. Simple GUI
This adds to the basic program by adding in basic GUI elements and dialogue boxes to allow the user to specify measurement parameters and see results plotted in real time. First displaying a `Fields` element:

![](https://i.imgur.com/db0oieF.png)

then moving onto a `Plot`:

![](https://i.imgur.com/1xdGSff.png)

finally telling the user it has completed with a info-alert:

![](https://i.imgur.com/t5fDub7.png)

## 3. Full GUI
This add considerably to the previous examples, creating many GUI elements and combining them `Grid` objects and finally combining those `Grid` objects into a single `Tabs` object to act as the main window. This program offers the user means to change SMU connection and configuration options and displays both a plot and table of results

![](https://i.imgur.com/uimIdth.png)

![](https://i.imgur.com/6EV9puS.png)

![](https://i.imgur.com/A1xIXif.png)
