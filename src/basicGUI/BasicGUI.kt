package basicGUI

import JISA.Addresses.GPIBAddress
import JISA.Devices.K2450
import JISA.Devices.SMU
import JISA.Experiment.ResultList
import JISA.GUI.Fields
import JISA.GUI.GUI
import JISA.GUI.Plot
import JISA.Util
import kotlin.system.exitProcess

val SD_ADDRESS = GPIBAddress(0, 10)
val SG_ADDRESS = GPIBAddress(0, 20)

const val MIN_SD_V = 0.0
const val MAX_SD_V = 60.0
const val NUM_SD_V = 61

const val MIN_SG_V = 0.0
const val MAX_SG_V = 60.0
const val NUM_SG_V = 7

fun main() {

    // Connect to SMUs
    val sourceDrain: SMU
    val sourceGate: SMU
    try {
        sourceDrain = K2450(SD_ADDRESS)
        sourceGate  = K2450(SG_ADDRESS)
    } catch (e: Exception) {
        GUI.errorAlert("Error", "Connection Error", e.message)
        exitProcess(1)
    }

    val params = Fields("Parameters")
    val minSDV = params.addDoubleField("Start SD [V]", MIN_SD_V)
    val maxSDV = params.addDoubleField("Stop SD [V]", MAX_SD_V)
    val numSDV = params.addIntegerField("No. Steps", NUM_SD_V)

    params.addSeparator()

    val minSGV = params.addDoubleField("Start SG [V]", MIN_SG_V)
    val maxSGV = params.addDoubleField("Stop SG [V]", MAX_SG_V)
    val numSGV = params.addIntegerField("No. Steps", NUM_SG_V)

    params.addSeparator()

    val file   = params.addFileSave("Output File")

    val okay = params.showAndWait()

    if (!okay) {
        exitProcess(0)
    }

    // Create arrays of voltages to use
    val sdVoltages = Util.makeLinearArray(minSDV.get(), maxSDV.get(), numSDV.get())
    val sgVoltages = Util.makeLinearArray(minSGV.get(), maxSGV.get(), numSGV.get())

    // Create result storage
    val results = ResultList("SD Voltage", "SD Current", "SG Voltage (set)", "SG Voltage", "SG Current")
    results.setUnits("V", "A", "V", "V", "A")

    val plot   = Plot("Output Curve", "SD Voltage [V]", "Drain Current [A]")
    val series = plot.watchList(results, 0, 1, 2)
    series.showMarkers(false)

    plot.setExitOnClose(true)
    plot.show()

    // Configure source-drain SMU
    sourceDrain.setVoltage(MIN_SD_V)
    sourceDrain.useAutoRanges()
    sourceDrain.setCurrentLimit(10e-3)
    sourceDrain.useFourProbe(false)
    sourceDrain.turnOn()

    // Configure source-gate SMU
    sourceGate.setVoltage(MIN_SG_V)
    sourceGate.useAutoRanges()
    sourceGate.setCurrentLimit(10e-3)
    sourceGate.useFourProbe(false)
    sourceGate.turnOn()

    // Loop over all source-gate and source-drain values, taking measurements
    for (sgV in sgVoltages) {

        sourceGate.setVoltage(sgV)

        for (sdV in sdVoltages) {

            sourceDrain.setVoltage(sdV)

            Util.sleep(500)               // Wait 0.5 seconds before taking measurement

            results.addData(
                sourceDrain.getVoltage(),
                sourceDrain.getCurrent(),
                sgV,
                sourceGate.getVoltage(),
                sourceGate.getCurrent()
            )

        }

    }

    sourceDrain.turnOff()
    sourceGate.turnOff()

    // Output results as CSV file
    results.output(file.get())

    GUI.infoAlert("Complete", "Measurement Complete", "The measurement completed successfully.")

}