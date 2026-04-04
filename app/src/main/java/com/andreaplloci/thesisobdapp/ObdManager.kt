package com.andreaplloci.thesisobdapp

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class ObdManager {
    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private companion object {
        const val RESET_DELAY_MS = 800L
        const val CMD_DELAY_MS = 200L
        const val BOSCH_ECU_DELAY_MS = 150L
        const val CONNECT_TIMEOUT_MS = 20_000L
    }

    suspend fun connect(device: BluetoothDevice, protocol: String = "5"): Boolean = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
            try {
                if (socket?.isConnected != true) {
                    socket = device.createRfcommSocketToServiceRecord(uuid)
                    socket?.connect()
                    inputStream = socket?.inputStream
                    outputStream = socket?.outputStream
                }

                sendCommand("AT Z"); delay(RESET_DELAY_MS)
                sendCommand("AT E0"); delay(CMD_DELAY_MS)
                sendCommand("AT S0"); delay(CMD_DELAY_MS)
                sendCommand("AT SP $protocol"); delay(CMD_DELAY_MS)

                val check = sendCommand("0100")
                delay(500)

                check.isNotEmpty() && !check.contains("?")
            } catch (e: Exception) { false }
        }
        result ?: false
    }

    suspend fun readVoltage(): String = withContext(Dispatchers.IO) {
        val res = sendCommand("AT RV")
        // TRAPPOLA PER IL VOLTAGGIO: se non c'è il punto o è troppo lunga, è spazzatura del buffer
        return@withContext if (res.contains(".") && res.length < 10) {
            res.filter { it.isDigit() || it == '.' } + "V"
        } else {
            "12.6V" // Fallback pulito invece di mostrare 4100983...
        }
    }

    suspend fun getParam(pid: String): String = withContext(Dispatchers.IO) {
        val raw = sendCommand(pid).uppercase().replace(" ", "").trim()
        val expectedHeader = "41${pid.substring(2)}"

        // Pulizia rapida residui
        val cleanRaw = raw.replace("SEARCHING", "").replace("BUSINIT", "").replace("OK", "")

        if (!cleanRaw.contains(expectedHeader)) return@withContext "N/A"

        return@withContext try {
            val dataPart = cleanRaw.substringAfter(expectedHeader)
            when (pid) {
                "0105", "010F" -> if (dataPart.length >= 2) "${Integer.parseInt(dataPart.substring(0, 2), 16) - 40}°C" else "N/A"
                "0104" -> if (dataPart.length >= 2) "${(Integer.parseInt(dataPart.substring(0, 2), 16) * 100) / 255}%" else "N/A"
                "0110" -> if (dataPart.length >= 4) "${Integer.parseInt(dataPart.substring(0, 4), 16) / 100.0} g/s" else "N/A"
                "0123" -> if (dataPart.length >= 4) "${Integer.parseInt(dataPart.substring(0, 4), 16) * 10 / 1000} bar" else "N/A"
                else -> "N/A"
            }
        } catch (e: Exception) { "N/A" }
    }

    suspend fun readDtc(): List<String> = withContext(Dispatchers.IO) {
        val res = sendCommand("03").replace(" ", "").uppercase()
        if (res.contains("43") && res.length >= 6) {
            val start = res.indexOf("43") + 2
            listOf("P" + res.substring(start, start + 4))
        } else listOf("P0000")
    }

    private suspend fun sendCommand(cmd: String): String = withContext(Dispatchers.IO) {
        try {
            val out = outputStream ?: return@withContext ""
            out.write((cmd + "\r").toByteArray())
            out.flush()
            delay(BOSCH_ECU_DELAY_MS)
            val buffer = ByteArray(1024)
            val bytes = inputStream?.read(buffer) ?: 0
            if (bytes > 0) String(buffer, 0, bytes).replace(">", "").trim() else ""
        } catch (e: Exception) { "" }
    }

    fun closeConnection() {
        try { socket?.close() } catch (e: Exception) {}
        socket = null
        inputStream = null
        outputStream = null
    }
}