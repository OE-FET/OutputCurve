package basic

import JISA.Addresses.GPIBAddress
import JISA.Devices.K2450
import JISA.Experiment.ResultList
import JISA.Util

val SD_ADDRESS = GPIBAddress(0, 10)
val SG_ADDRESS = GPIBAddress(0, 20)

const val MIN_SD_V = 0.0
const val MAX_SD_V = 60.0
const val NUM_SD_V = 61

const val MIN_SG_V = 0.0
const val MAX_SG_V = 60.0
const val NUM_SG_V = 7

fun main() {

    println("Connecting to instruments...")

    // Connect to SMUs
    val sourceDrain = K2450(SD_ADDRESS)
    val sourceGate  = K2450(SG_ADDRESS)

    // Create arrays of voltages to use
    val sdVoltages = Util.makeLinearArray(MIN_SD_V, MAX_SD_V, NUM_SD_V)
    val sgVoltages = Util.makeLinearArray(MIN_SG_V, MAX_SG_V, NUM_SG_V)

    // Create result storage
    val results    = ResultList("SD Voltage", "SD Current", "SG Voltage (set)", "SG Voltage", "SG Current")
    results.setUnits("V", "A", "V", "V", "A")

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

    println("Performing Measurement...")

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

            println("SG: $sgV V, SD: $sdV V")

        }

    }

    sourceDrain.turnOff()
    sourceGate.turnOff()

    println("Measurement Complete. Outputting to 'data.csv'...")

    // Output results as CSV file
    results.output("data.csv")

    println("All done. Goodbye.")

}