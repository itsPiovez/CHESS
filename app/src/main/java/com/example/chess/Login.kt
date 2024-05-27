package com.example.chess

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Login : AppCompatActivity() {
    private lateinit var Email: EditText
    private lateinit var Passw: EditText

    private var email: String = ""
    private var password: String = ""
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this)

        // Adjust window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Email = findViewById(R.id.editTextName)
        Passw = findViewById(R.id.editTextPassword)

        val button2 = findViewById<Button>(R.id.buttonregistration)
        button2.setOnClickListener {
            val intent = Intent(this@Login, Registration::class.java)
            startActivity(intent)
        }

        val button3 = findViewById<Button>(R.id.button4)
        button3.setOnClickListener {
            val intent = Intent(this@Login, Game::class.java)
            val TypeGame = "Offline"
            intent.putExtra("TypeGame", TypeGame)
            startActivity(intent)
        }

        val buttonAccess = findViewById<Button>(R.id.buttonLogin)
        buttonAccess.setOnClickListener {
            email = Email.text.toString()
            password = Passw.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                authenticateUser(email, password)
            } else {
                Toast.makeText(this@Login, "Completa tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticateUser(email: String, password: String) {
        val url = "https://scacchi.5cimarcopiovesan.barsanti.edu.it/login"

        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        Log.d("Login", "Sending login request with email: $email and password: $password")

        val loginRequest = JsonObjectRequest(Request.Method.POST, url, json,
            Response.Listener { response ->
                try {
                    val message = response.getString("message")
                    if (message == "Login successful") {
                        val intent = Intent(this@Login, Dashboard::class.java)
                        intent.putExtra("userData", response.toString())
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Login, "Credenziali non valide", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@Login, "Errore nella risposta del server", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("Login", "Login error: ${error.message}")
                Toast.makeText(this@Login, "Errore nella richiesta di autenticazione", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(loginRequest)
    }
}
