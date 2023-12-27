package com.example.sumativa4

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class MainActivity : AppCompatActivity() {

    private var clienteID: String = ""

    // CONEXION AL SERVIDOR
    private val MQTTHOST = "tcp://sumativa-4.cloud.shiftr.io:1883"
    private val MQTTUSER = "gg9ReOYVouMTCVTb"
    private val MQTTPASS = "sumativa-4"

    private val TOPIC = "LED"
    private val TOPIC_MSG_ON = "ENCENDER"
    private val TOPIC_MSG_OFF = "APAGAR"

    private var permisoPublicar: Boolean = false

    private lateinit var cliente: MqttAndroidClient
    private lateinit var opciones: MqttConnectOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getNombreCliente()
        connectBroker()
        suscribirseTopic()

        val btnON: Button = findViewById(R.id.btnON)
        btnON.setOnClickListener {
            enviarMensaje(TOPIC, TOPIC_MSG_ON)
        }

        val btnOFF: Button = findViewById(R.id.btnOFF)
        btnOFF.setOnClickListener {
            enviarMensaje(TOPIC, TOPIC_MSG_OFF)
        }
    }

    private fun enviarMensaje(topic: String, msg: String) {
        if (checkConnection()) {
            try {
                val qos = 0
                cliente.publish(topic, msg.toByteArray(), qos, false)
                Toast.makeText(baseContext, "$topic: $msg", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun connectBroker() {
        cliente = MqttAndroidClient(applicationContext, MQTTHOST, clienteID)
        opciones = MqttConnectOptions()
        opciones.userName = MQTTUSER
        opciones.password = MQTTPASS.toCharArray()

        try {
            val token = cliente.connect(opciones)
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Toast.makeText(baseContext, "Conectado", Toast.LENGTH_SHORT).show()
                    permisoPublicar = true
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(baseContext, "Conexión fallida", Toast.LENGTH_SHORT).show()
                    permisoPublicar = false
                }
            }

        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun suscribirseTopic() {
        try {
            cliente.subscribe(TOPIC, 0)
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        cliente.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                Toast.makeText(baseContext, "Conexión perdida", Toast.LENGTH_SHORT).show()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                // Maneja los mensajes que llegan al topic
                if (topic == TOPIC) {
                    val payload = message?.toString()
                    Toast.makeText(baseContext, "Mensaje recibido en $topic: $payload", Toast.LENGTH_SHORT).show()
                }
            }

            override fun deliveryComplete(token: IMqttToken?) {
            }
        })
    }

    private fun getNombreCliente() {
        val manufacturer = Build.MANUFACTURER
        val modelName = Build.MODEL
        clienteID = "$manufacturer $modelName"

        val txtIdCliente = findViewById<TextView>(R.id.txtIdCliente)
        txtIdCliente.text = clienteID
    }

    private fun checkConnection(): Boolean {
        return permisoPublicar
    }
}
