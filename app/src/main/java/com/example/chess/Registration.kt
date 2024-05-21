package com.example.chess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.widget.Toast


class Registration : AppCompatActivity() {
    private lateinit var Email: EditText
    private lateinit var Passw: EditText
    private lateinit var Name: EditText

    private var email: String = ""
    private var password: String = ""
    private var name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
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
            if(email!=""&&password!=""&&name!="") {
                Log.d("RegistrationActivity", "Email: $email")
                Log.d("RegistrationActivity", "Password: $password")
                Log.d("RegistrationActivity", "Name: $name")
                val intent = Intent(this@Registration, Login::class.java)
                startActivity(intent)
            }
            else{
                Log.d("RegistrationActivity", "Email: $email")
                Log.d("RegistrationActivity", "Password: $password")
                Log.d("RegistrationActivity", "Name: $name")
                Toast.makeText(this@Registration, "Completa tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
