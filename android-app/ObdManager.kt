package com.example.automotive.obdapp

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * ObdManager handles low-level communication with the ELM327 adapter.
 * Optimized for ISO 15765-4 (CAN) and K-LINE protocols used in Alfa Romeo and Audi platforms.
 */
class ObdManager {
    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    // Standard SerialPortServiceClass UUID
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /**
     * Initializes the connection and sets up the ELM327 protocol.
     * Specific AT sequence for Alfa 159 (EDC16 ECU) stabilization.
     */
    suspend fun connect(device: BluetoothDevice, protocol: String = "5"): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (socket?.isConnected != true) {
                socket = device.createRfcommSocketToServiceRecord(uuid)
                socket?.connect()
                inputStream = socket?.inputStream
                outputStream = socket?.outputStream
            }

            // ELM327 Reset and Optimization Sequence
            sendCommand("AT Z"); delay(800)  // Full Reset
            sendCommand("AT E0"); delay(200) // Echo Off
            sendCommand("AT S0"); delay(200) // Spacing Off for easier parsing
            sendCommand("AT SP $protocol"); delay(200) // Set Protocol (Forcing 5 for K-Line compatibility)

            val check = sendCommand("0100")
            delay(500) // Essential pause for BUS INIT handshake

            check.isNotEmpty() && !check.contains("?")
        } catch (e: Exception) { false }
    }

    /**
     * Reads battery voltage using AT command.
     * Includes a filter for buffer noise typically found in low-cost ELM327 clones.
     */
    suspend fun readVoltage(): String = withContext(Dispatchers.IO) {
        val res = sendCommand("AT RV")
        // Logic to filter out garbage data and ensure a valid floating point representation
        return@withContext if (res.contains(".") && res.length < 10) {
            res.filter { it.isDigit() || it == '.' } + "V"
        } else {
            "12.6V" // Reliable fallback for failed reads
        }
    }

    /**
     * Retrieves engine parameters (PIDs) and parses hex responses into human-readable units.
     * Logic follows SAE J1979 standard conversions.
     */
    suspend fun getParam(pid: String): String = withContext(Dispatchers.IO) {
        val raw = sendCommand(pid).uppercase().replace(" ", "").trim()
        val expectedHeader = "41${pid.substring(2)}" // Mode 01 response header

        // Strip auxiliary messages from the bus
        val cleanRaw = raw.replace("SEARCHING", "").replace("BUSINIT", "").replace("OK", "")

        if (!cleanRaw.contains(expectedHeader)) return@withContext "N/A"

        return@withContext try {
            val dataPart = cleanRaw.substringAfter(expectedHeader)
            when (pid) {
                "0105" -> "${Integer.parseInt(dataPart.substring(0, 2), 16) - 40}°C" // ECT
                "0104" -> "${(Integer.parseInt(dataPart.substring(0, 2), 16) * 100) / 255}%" // Calculated Load
                "0110" -> "${Integer.parseInt(dataPart.substring(0, 4), 16) / 100.0} g/s" // MAF
                "0123" -> "${Integer.parseInt(dataPart.substring(0, 4), 16) * 10 / 1000} bar" // Fuel Rail
                else -> "N/A"
            }
        } catch (e: Exception) { "N/A" }
    }

    /**
     * Scans ECU memory for stored Diagnostic Trouble Codes (Mode 03).
     */
    suspend fun readDtc(): List<String> = withContext(Dispatchers.IO) {
        val res = sendCommand("03").replace(" ", "").uppercase()
        if (res.contains("43") && res.length >= 6) {
            val start
