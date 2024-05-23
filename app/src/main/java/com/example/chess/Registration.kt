package com.example.chess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Registration : AppCompatActivity() {
    private lateinit var Email: EditText
    private lateinit var Passw: EditText
    private lateinit var Name: EditText

    private var email: String = ""
    private var password: String = ""
    private var name: String = ""
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this)

        // Adjust window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Email = findViewById(R.id.editTextEmail)
        Passw = findViewById(R.id.editTextPassword)
        Name = findViewById(R.id.editTextName)

        val buttonRegistration = findViewById<Button>(R.id.buttonregistration)
        buttonRegistration.setOnClickListener {
            email = Email.text.toString()
            password = Passw.text.toString()
            name = Name.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                registerUser(email, password, name)
            } else {
                Toast.makeText(this@Registration, "Completa tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }
    }private fun registerUser(email: String, password: String, name: String) {
        val url = "http://172.20.10.3:3000/register"

        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("username", name)  // Assicurati di usare "name" qui
        }

        Log.d("Registration", "JSON Request: $json")

        val registerRequest = JsonObjectRequest(Request.Method.POST, url, json,
            Response.Listener { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val intent = Intent(this@Registration, Login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Registration, "Registrazione fallita", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@Registration, "Errore nella risposta del server", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("Registration", "Registration error: ${error.message}")
                Toast.makeText(this@Registration, "Errore nella richiesta di registrazione", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(registerRequest)
    }

}
