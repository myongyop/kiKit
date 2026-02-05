package com.myongyop.kikit

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import app.tauri.annotation.Command
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.Invoke
import app.tauri.plugin.Plugin
import app.tauri.plugin.JSObject
import app.tauri.plugin.JSArray

@TauriPlugin
class PrinterPlugin(private val activity: Activity) : Plugin(activity) {
    private val manager: UsbManager by lazy {
        activity.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    @Command
    fun listPrinters(invoke: Invoke) {
        val printerList = JSArray()
        val devices = manager.deviceList
        for (device in devices.values) {
            val json = JSObject()
            json.put("deviceName", device.deviceName)
            json.put("deviceId", device.deviceId)
            json.put("vendorId", device.vendorId)
            json.put("productId", device.productId)
            json.put("manufacturerName", device.manufacturerName ?: "Unknown")
            json.put("productName", device.productName ?: "Unknown")
            printerList.put(json)
        }
        val result = JSObject()
        result.put("printers", printerList)
        invoke.resolve(result)
    }

    @Command
    fun connectPrinter(invoke: Invoke) {
        val args = invoke.parseArgs(ConnectPrinterArgs::class.java)
        val deviceName = args.deviceName
        if (deviceName == null) {
            invoke.reject("Device name is required")
            return
        }

        val device = manager.deviceList.values.find { it.deviceName == deviceName }
        if (device == null) {
            invoke.reject("Device not found")
            return
        }

        if (manager.hasPermission(device)) {
            // Already has permission, proceed to connect (simulated for now)
            val ret = JSObject()
            ret.put("success", true)
            ret.put("message", "Already had permission")
            invoke.resolve(ret)
        } else {
            val permissionIntent = PendingIntent.getBroadcast(
                activity, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
            )
            
            manager.requestPermission(device, permissionIntent)
            
            val ret = JSObject()
            ret.put("success", false)
            ret.put("message", "Permission requested")
            invoke.resolve(ret)
        }
    }

    @Command
    fun testPrint(invoke: Invoke) {
        val args = invoke.parseArgs(TestPrintArgs::class.java)
        val value = args.value
        println("PrinterPlugin: invoke received with value: $value")
        invoke.resolve()
    }

    @Command
    fun printRaw(invoke: Invoke) {
        val args = invoke.parseArgs(PrintRawArgs::class.java)
        val deviceName = args.deviceName
        val data = args.data

        if (deviceName == null || data == null) {
            invoke.reject("Device name and data are required")
            return
        }

        val device = manager.deviceList.values.find { it.deviceName == deviceName }
        if (device == null) {
            invoke.reject("Device not found")
            return
        }

        if (!manager.hasPermission(device)) {
            invoke.reject("Permission denied")
            return
        }

        val connection = manager.openDevice(device)
        if (connection == null) {
            invoke.reject("Failed to open connection")
            return
        }

        try {
            // Find interface and endpoint
            // Simplified: find first Interface with Bulk Out endpoint
            var endpoint: android.hardware.usb.UsbEndpoint? = null
            var iface: android.hardware.usb.UsbInterface? = null

            for (i in 0 until device.interfaceCount) {
                val intf = device.getInterface(i)
                for (j in 0 until intf.endpointCount) {
                    val ep = intf.getEndpoint(j)
                    if (ep.type == android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_BULK &&
                        ep.direction == android.hardware.usb.UsbConstants.USB_DIR_OUT) {
                        endpoint = ep
                        iface = intf
                        break
                    }
                }
                if (endpoint != null) break
            }

            if (endpoint == null || iface == null) {
                invoke.reject("Bulk out endpoint not found")
                return
            }

            if (!connection.claimInterface(iface, true)) {
                invoke.reject("Failed to claim interface")
                return
            }

            // Convert List<Int> to ByteArray
            // data is List<Int> because JSON array of numbers comes as integers
            // Should be careful about signed/unsigned.
            val bytes = ByteArray(data.size)
            for (i in data.indices) {
                bytes[i] = data[i].toByte()
            }

            val transferred = connection.bulkTransfer(endpoint, bytes, bytes.size, 1000)
            if (transferred >= 0) {
                val ret = JSObject()
                ret.put("success", true)
                ret.put("bytesWritten", transferred)
                invoke.resolve(ret)
            } else {
                invoke.reject("Transfer failed")
            }

        } finally {
            connection.close() // claimInterface is released on close
        }
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.myongyop.kikit.USB_PERMISSION"
    }
}

@app.tauri.annotation.InvokeArg
class TestPrintArgs {
    var value: String? = null
}

@app.tauri.annotation.InvokeArg
class ConnectPrinterArgs {
    var deviceName: String? = null
}

@app.tauri.annotation.InvokeArg
class PrintRawArgs {
    var deviceName: String? = null
    var data: List<Int>? = null
}
