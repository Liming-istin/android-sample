package cn.edu.nju.istin.fenghuo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.net.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.name
    private  val BLUETOOTH_DEVICE_NAME = "bt-pan"
    private  val PORT = 5005
    private  val SEND_SERVER_IP = "225.0.0.1"

    var isRunning: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (findViewById(R.id.sendButton) as Button).setOnClickListener {
            var toSend = (findViewById(R.id.sendEditText) as EditText).text.toString().trim()
            if(toSend.isEmpty()) return@setOnClickListener
            Thread({
                try {
                    val sendSocket = DatagramSocket()
                    val msg = toSend + Calendar.getInstance().time.toString()
                    val buf: ByteArray = msg.toByteArray()
                    val sendPacket = DatagramPacket(buf, buf.size, Inet4Address.getByName(SEND_SERVER_IP), PORT)
                    sendSocket.send(sendPacket)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }).start()
        }

        var myIp: Inet4Address? = getMyIpAddress() as Inet4Address
        Thread(ReceiveListenThread(myIp)).start()
    }

    override fun onRestart() {
        super.onRestart()
        isRunning = true

    }

    inner class ReceiveListenThread(address: Inet4Address?) : Runnable {
        private var addr = address
        override fun run() {
            if (addr == null) {
                Log.e(TAG, "Ip address is null")
                return@run
            }

            while (isRunning) {
                try {
                    val socket = DatagramSocket(PORT, addr)
                    val data = ByteArray(1024)
                    val packet = DatagramPacket(data, data.size)
                    socket.receive(packet)
                    val msgReceived = String(packet.data, packet.offset, packet.length)
                    Log.i(TAG, "set $msgReceived")
                    runOnUiThread({
                        (findViewById(R.id.receiveDisplay) as TextView).text = msgReceived
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun getMyIpAddress(): InetAddress? {
        var netInterface: NetworkInterface? = NetworkInterface.getByName(BLUETOOTH_DEVICE_NAME)
        if (netInterface == null) {
            Log.e(TAG, "Unable to find a device named $BLUETOOTH_DEVICE_NAME")
            return null
        }
        val ipAddresses: Enumeration<InetAddress> = netInterface.inetAddresses
        while (ipAddresses.hasMoreElements()) {
            val addr: InetAddress = ipAddresses.nextElement()
            if (addr is Inet4Address) {
                Log.i(TAG, "get ip " + addr.toString())
                return addr
            }
        }
        return null
    }

    override fun onStop() {
        isRunning = false
        super.onStop()
    }
}
