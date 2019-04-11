View code [here](https://github.com/OE-FET/OutputCurve/blob/master/src/Main.kt)

![](https://i.imgur.com/YlXnUTz.png)

# OutputCurve
Here's a breakdown of the code

The first few lines are `import` statements. These are needed because you need to import a class to use it:
```kotlin
import JISA.Control.ConfigStore
import JISA.Experiment.ResultStream
import JISA.GUI.*
import JISA.Util
```
```
We then move on to defining some global variables. The first is a `ConfigStore` object:
```
```kotlin
// This stores any configurations to a file
val config = ConfigStore("FET-Output")
```
```
This acts to save any configuration data we might generate to a file so that it's remembered 
next time we use the program

Next we create a `ConfigGrid`. This is the grid of instrument connection options that allows 
the user to choose which driver and address to use to connect to up to two SMUs:
```
```kotlin
// This creates an instrument connection config grid (store connection configs in config file)
val connections = ConfigGrid("Instruments", config).apply {
    addSMU("SMU 1")
    addSMU("SMU 2")
}
```
```
As you can see, we use Kotlin's `apply {...}` structure, which lets us call methods on 
the `ConfigGrid` immediately after assigning it

Next we create two SMU config panels, these allow the user to select which SMU from our 
`ConfigGrid` and, further to that, which channel on that SMU should be used for Source-Drain 
and Source-Gate. These are then both added to a `Grid` to hold them together:
```
```kotlin
// These are panels that let us configure SMU channels based on the SMUs in the connection grid
val sdSMU   = SMUConfig("Source-Drain", "sd", config, connections)
val sgSMU   = SMUConfig("Source-Gate", "sg", config, connections)
val smuGrid = Grid("Channels", sdSMU, sgSMU).apply { setGrowth(false, false) }
```
```
Finally, we make the `Plot` and `Table` used to display the results:
```
```kotlin
// GUI elements
val plot  = Plot("Output Curve", "SD Voltage [V]", "Drain Current [A]")
val table = Table("Table of Results")
```
```
These are all "global" variables, so they are accessible from any part of our code in `Main.kt`.

Next, we define the main method. This is the code that will run when the program starts 
(at least after creating the global variables anyway). In it, we create the user-input 
fields (ie the panels that have the text-boxes to let us specify what voltages to use etc)
```
```kotlin
fun main() {

    parameters = Fields("Parameters")

    val minSD = parameters.addDoubleField("Start SD [V]", minSDV)
    val maxSD = parameters.addDoubleField("Stop SD [V]", maxSDV)
    val numSD = parameters.addIntegerField("No. Steps", numSDV)

    parameters.addSeparator()

    val minSG = parameters.addDoubleField("Start SG [V]", minSGV)
    val maxSG = parameters.addDoubleField("Stop SG [V]", maxSGV)
    val numSG = parameters.addIntegerField("No. Steps", numSGV)

    val settings = Fields("Settings")

    val intTime = settings.addDoubleField("Integration Time [s]", integrationTime)
    val delTime = settings.addDoubleField("Delay Time [s]", delayTime / 1000.0)    // seconds
    val file    = settings.addFileSave("Output File", outputFile)   
 ```
 ```
Each `add...Field(...)` method returns a handle on the field it just created. These can be 
then be used (by calling `get()` on them) to retrive what is currently typed in them.
Next, we add these two panels plus the plot and table to a `Grid` (with 2 columns) to hold 
them together. We then create and add everything to our main window (which is a `Tabs` element):
```
```kotlin
    val measurement = Grid("Measurement", 2, parameters, settings, table, plot)
    val mainWindow  = Tabs("FET Output Characterisation", connections, smuGrid, measurement) 
```
```
We tell our main window that we want it to be maximised and for the program to terminate if it is closed:
```
```kotlin
    mainWindow.setMaximised(true)
    mainWindow.setExitOnClose(true)
```
```
We then add the "Start" button to the measurement grid, and tell it what to do when pressed:
```
```kotlin
    measurement.addToolbarButton("Start") {

        // Disable all input fields during measurement
        parameters.setFieldsDisabled(true)
        settings.setFieldsDisabled(true)

        // Set parameters to equal what is currently written in the GUI input fields
        minSDV          = minSD.get()
        maxSDV          = maxSD.get()
        numSDV          = numSD.get()
        minSGV          = minSG.get()
        maxSGV          = maxSG.get()
        numSGV          = numSG.get()
        integrationTime = intTime.get()
        delayTime       = (delTime.get() * 1000).toLong() // convert to milliseconds
        outputFile      = file.get()

        // Run the experiment
        runMeasurement()

        // Re-enable input fields
        parameters.setFieldsDisabled(false)
        settings.setFieldsDisabled(false)

    }
```
```
Finally, we make the main window show itself:
```
```kotlin
    mainWindow.show()
    
}
```
 
