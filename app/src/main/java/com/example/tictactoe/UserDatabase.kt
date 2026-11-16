package com.example.tictactoe

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class UserRecord(
    val username: String,
    val dateRegistered: String,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val winRate: Double
)

class UserDatabase(context: Context) :
    SQLiteOpenHelper(context, "TicTacToeDB", null, 3) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                salt TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                date_registered TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE game_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                player_x TEXT,
                player_o TEXT,
                winner TEXT,
                game_mode TEXT NOT NULL DEFAULT 'TWO_PLAYER',
                date_played TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    salt TEXT NOT NULL,
                    password_hash TEXT NOT NULL,
                    date_registered TEXT DEFAULT CURRENT_TIMESTAMP
                )
                """.trimIndent()
            )
            db.execSQL(
                "ALTER TABLE game_history ADD COLUMN user_id INTEGER NOT NULL DEFAULT 0"
            )
        }
        if (oldVersion < 3) {
            db.execSQL(
                "ALTER TABLE game_history ADD COLUMN game_mode TEXT NOT NULL DEFAULT 'TWO_PLAYER'"
            )
        }
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120000, 256)
        val hash = SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec).encoded
        spec.clearPassword()
        return hash.joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    fun registerUser(username: String, password: String): Boolean {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val values = ContentValues().apply {
            put("username", username)
            put("salt", salt.joinToString("") { "%02x".format(it.toInt() and 0xff) })
            put("password_hash", hashPassword(password, salt))
        }

        return writableDatabase.insert("users", null, values) != -1L
    }

    fun loginUser(username: String, password: String): Long {
        val cursor = readableDatabase.rawQuery(
            "SELECT id, salt, password_hash FROM users WHERE username = ?",
            arrayOf(username)
        )

        cursor.use {
            if (!it.moveToFirst()) return -1L

            val id = it.getLong(0)
            val saltHex = it.getString(1)
            val savedHash = it.getString(2)

            val salt = saltHex.chunked(2)
                .map { byteStr -> byteStr.toInt(16).toByte() }
                .toByteArray()

            val enteredHash = hashPassword(password, salt)

            return if (
                MessageDigest.isEqual(
                    enteredHash.toByteArray(),
                    savedHash.toByteArray()
                )
            ) id else -1L
        }
    }

    fun saveGame(
        userId: Long,
        playerX: String,
        playerO: String,
        winner: String,
        gameMode: String
    ) {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("player_x", playerX)
            put("player_o", playerO)
            put("winner", winner)
            put("game_mode", gameMode)
        }
        writableDatabase.insert("game_history", null, values)
    }

    fun getUserRecord(userId: Long, username: String): UserRecord {
        var dateRegistered = "N/A"
        readableDatabase.rawQuery(
            "SELECT date_registered FROM users WHERE id = ?",
            arrayOf(userId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                dateRegistered = cursor.getString(0) ?: "N/A"
            }
        }

        var totalGames = 0
        var wins = 0
        var draws = 0

        readableDatabase.rawQuery(
            "SELECT winner FROM game_history WHERE user_id = ?",
            arrayOf(userId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                totalGames++
                val winner = cursor.getString(0) ?: ""
                when {
                    winner.equals(username, ignoreCase = true) -> wins++
                    winner.equals("Draw", ignoreCase = true) -> draws++
                }
            }
        }

        val losses = (totalGames - wins - draws).coerceAtLeast(0)
        val winRate = if (totalGames > 0) (wins * 100.0) / totalGames else 0.0

        return UserRecord(
            username = username,
            dateRegistered = dateRegistered,
            totalGames = totalGames,
            wins = wins,
            losses = losses,
            draws = draws,
            winRate = winRate
        )
    }

    fun getUserHistory(userId: Long): List<String> {
        val history = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT player_x, player_o, winner, game_mode, date_played
            FROM game_history
            WHERE user_id = ?
            ORDER BY id DESC
            """.trimIndent(),
            arrayOf(userId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                history.add(
                    "Player X: ${it.getString(0)}\n" +
                    "Player O: ${it.getString(1)}\n" +
                    "Result/Winner: ${it.getString(2)}\n" +
                    "Mode: ${it.getString(3)}\n" +
                    "Date: ${it.getString(4)}"
                )
            }
        }
        return history
    }
}
