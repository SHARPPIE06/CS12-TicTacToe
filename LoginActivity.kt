package com.example.tictactoe

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class LoginActivity : Activity() {
    private lateinit var database: UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = UserDatabase(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.rgb(245, 235, 255))
        }

        val title = TextView(this).apply {
            text = "TIC TAC TOE"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(105, 55, 180))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "CS 12 Final Project — Please Login"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, 8, 0, 32)
        }

        val username = EditText(this).apply {
            hint = "Username"
            setSingleLine(true)
        }

        val password = EditText(this).apply {
            hint = "Password"
            inputType = 129
            setSingleLine(true)
        }

        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 20, 0, 0)
        }

        val login = Button(this).apply {
            text = "LOGIN"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(105, 55, 180))
        }

        val register = Button(this).apply {
            text = "CREATE NEW ACCOUNT"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(255, 105, 180))
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(username)
        layout.addView(password)
        layout.addView(login, btnParams)
        layout.addView(register, btnParams)
        setContentView(layout)

        login.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString()

            if (user.isBlank() || pass.isBlank()) {
                toast("Please enter username and password")
                return@setOnClickListener
            }

            val userId = database.loginUser(user, pass)
            if (userId != -1L) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("USER_ID", userId)
                    putExtra("USERNAME", user)
                }
                startActivity(intent)
                finish()
            } else {
                toast("Invalid username or password")
            }
        }

        register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }
}
