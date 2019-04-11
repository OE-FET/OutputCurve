import JISA.Control.ConfigStore
import JISA.Experiment.ResultStream
import JISA.GUI.*
import JISA.Util

// This stores any configurations to a file
val config = ConfigStore("FET-Output")

// This creates an instrument connection config grid (store connection configs in config file)
val connections = ConfigGrid("Instruments", config).apply {
    addSMU("SMU 1")
    addSMU("SMU 2")
}

// These are panels that let us configure SMU channels based on the SMUs in the connection grid
val sdSMU   = SMUConfig("Source-Drain", "sd", config, connections)
val sgSMU   = SMUConfig("Source-Gate", "sg", config, connections)
val smuGrid = Grid("Channels", sdSMU, sgSMU).apply { setGrowth(false, false) }

// GUI elements
val plot  = Plot("Output Curve", "SD Voltage [V]", "Drain Current [A]")
val table = Table("Table of Results")

// Experiment parameters
var minSDV = 0.0
var maxSDV = 60.0
var numSDV = 61

var minSGV = 0.0
var maxSGV = 60.0
var numSGV = 7

var integrationTime = 20e-3
var delayTime       = 500L // milliseconds
var outputFile      = ""

fun main() {

    val parameters = Fields("Parameters")

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

    val measurement = Grid("Measurement", 2, parameters, settings, table, plot)
    val mainWindow  = Tabs("FET Output Characterisation", connections, smuGrid, measurement)

    mainWindow.setMaximised(true)
    mainWindow.setExitOnClose(true)

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

    mainWindow.show()

}

fun runMeasurement() {

    // Clear the table and plot from any old data
    table.clear()
    plot.clear()

    // Get the configured SMU channels
    val sourceDrain = sdSMU.getSMU()
    val sourceGate = sgSMU.getSMU()

    // If either of them is null, then we can't continue
    if (sourceDrain == null || sourceGate == null) {
        GUI.errorAlert("Error", "Not Configured", "Source-drain and/or source-gate not configured.")
        return
    }

    // Create the values we will use (sweep up and down in sd-V, and just once in sg-V)
    val sdVoltages = Util.symArray(Util.makeLinearArray(minSDV, maxSDV, numSDV))
    val sgVoltages = Util.makeLinearArray(minSGV, maxSGV, numSGV)

    // Create object to hold results, output directly to file
    val results = ResultStream(outputFile, "SD Voltage", "SD Current", "SG Voltage (Set)", "SG Voltage", "SG Current")
    results.setUnits("V", "A", "V", "V", "A")

    // Plot SD Voltage (0) on x, SD Current (1) on y, split series by set SG voltage (2)
    plot.watchList(results, 0, 1, 2)
    table.watchList(results)

    // Configure source-drain SMU
    sourceDrain.setVoltage(minSDV)
    sourceDrain.useAutoRanges()
    sourceDrain.setIntegrationTime(integrationTime)
    sourceDrain.useFourProbe(false)
    sourceDrain.turnOn()

    // Configure source-gate SMU
    sourceGate.setVoltage(minSGV)
    sourceGate.useAutoRanges()
    sourceGate.setIntegrationTime(integrationTime)
    sourceGate.useFourProbe(false)
    sourceGate.turnOn()

    // The measurement sweep itself
    for (sgVoltage in sgVoltages) {

        sourceGate.setVoltage(sgVoltage)

        for (sdVoltage in sdVoltages) {

            sourceDrain.setVoltage(sdVoltage)

            Util.sleep(delayTime)

            results.addData(
                sourceDrain.getVoltage(),
                sourceDrain.getCurrent(),
                sgVoltage,
                sourceGate.getVoltage(),
                sourceGate.getCurrent()
            )

        }

    }

    // Turn off the SMUs now we're done
    sourceDrain.turnOff()
    sourceGate.turnOff()

    // We're done with these now
    results.finalise()

}
