package com.example.tictactoe

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class RegisterActivity : Activity() {
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
            text = "CREATE ACCOUNT"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(105, 55, 180))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        val username = EditText(this).apply {
            hint = "Enter Username (min 3 chars)"
            setSingleLine(true)
        }

        val password = EditText(this).apply {
            hint = "Enter Password (min 8 chars)"
            inputType = 129
            setSingleLine(true)
        }

        val confirmPassword = EditText(this).apply {
            hint = "Confirm Password"
            inputType = 129
            setSingleLine(true)
        }

        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 20, 0, 0)
        }

        val register = Button(this).apply {
            text = "REGISTER"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(255, 105, 180))
        }

        val backBtn = Button(this).apply {
            text = "BACK TO LOGIN"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(105, 55, 180))
        }

        layout.addView(title)
        layout.addView(username)
        layout.addView(password)
        layout.addView(confirmPassword)
        layout.addView(register, btnParams)
        layout.addView(backBtn, btnParams)
        setContentView(layout)

        register.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString()
            val confirm = confirmPassword.text.toString()

            when {
                user.length < 3 -> toast("Username must have at least 3 characters")
                pass.length < 8 -> toast("Password must have at least 8 characters")
                pass != confirm -> toast("Passwords do not match")
                database.registerUser(user, pass) -> {
                    toast("Account created successfully!")
                    finish()
                }
                else -> toast("Username already exists")
            }
        }

        backBtn.setOnClickListener {
            finish()
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
