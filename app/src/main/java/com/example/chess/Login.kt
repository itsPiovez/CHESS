package com.example.chess

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject


class Login : AppCompatActivity() {
    private lateinit var Email: EditText
    private lateinit var Passw: EditText

    private var email: String = ""
    private var password: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
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
            if(email!=""&&password!="") {
                Log.d("RegistrationActivity", "Email: $email")
                Log.d("RegistrationActivity", "Password: $password")
                val intent = Intent(this@Login, Dashboard::class.java)
                startActivity(intent)
            }
            else{
                Log.d("RegistrationActivity", "Email: $email")
                Log.d("RegistrationActivity", "Password: $password")
                Toast.makeText(this@Login, "Completa tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticateUser(email: String, password: String) {
        // Configura l'URL dell'API di autenticazione
        val url = "https://example.com/api/authenticate"

        // Crea il corpo della richiesta con le credenziali
        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)
        // Esegui la richiesta HTTP POST all'API di autenticazione

    }
}
