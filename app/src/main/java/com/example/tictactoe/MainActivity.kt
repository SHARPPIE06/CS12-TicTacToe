package com.example.tictactoe

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var userDatabase: UserDatabase
    private var userId: Long = -1L
    private var username = ""

    private val board = Array(9) { "" }
    private val cells = ArrayList<Button>()
    private var currentPlayer = "X"
    private var gameOver = false
    private var gameMode = "TWO_PLAYER"

    private var scoreX = 0
    private var scoreO = 0
    private var draws = 0

    private lateinit var status: TextView
    private lateinit var scoreText: TextView

    private val purple = Color.rgb(105, 55, 180)
    private val pink = Color.rgb(255, 105, 180)
    private val blue = Color.rgb(45, 170, 230)
    private val backgroundColor = Color.rgb(245, 240, 255)

    private val winPatterns = arrayOf(
        intArrayOf(0, 1, 2),
        intArrayOf(3, 4, 5),
        intArrayOf(6, 7, 8),
        intArrayOf(0, 3, 6),
        intArrayOf(1, 4, 7),
        intArrayOf(2, 5, 8),
        intArrayOf(0, 4, 8),
        intArrayOf(2, 4, 6)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userDatabase = UserDatabase(this)

        userId = intent.getLongExtra("USER_ID", -1L)
        username = intent.getStringExtra("USERNAME") ?: "Player"

        if (userId == -1L) {
            finish()
            return
        }

        showModeSelection()
    }

    private fun showModeSelection() {
        val record = userDatabase.getUserRecord(userId, username)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(backgroundColor)
        }

        val title = TextView(this).apply {
            text = "TIC TAC TOE"
            textSize = 32f
            setTextColor(purple)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val welcome = TextView(this).apply {
            text = "Welcome, $username!"
            textSize = 20f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(10, 12, 10, 8)
        }

        val recordBadge = TextView(this).apply {
            text = "Lifetime Record: ${record.wins}W - ${record.losses}L - ${record.draws}D"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(purple)
            gravity = Gravity.CENTER
            setPadding(10, 4, 10, 28)
        }

        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 14, 0, 0)
        }

        val twoPlayer = Button(this).apply {
            text = "TWO PLAYER GAME"
            setTextColor(Color.WHITE)
            setBackgroundColor(pink)
        }

        val singlePlayer = Button(this).apply {
            text = "SINGLE PLAYER (VS COMPUTER)"
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
        }

        val userRecordBtn = Button(this).apply {
            text = "INDIVIDUAL USER RECORD"
            setTextColor(Color.WHITE)
            setBackgroundColor(purple)
        }

        val history = Button(this).apply {
            text = "MY GAME HISTORY"
        }

        val logout = Button(this).apply {
            text = "LOGOUT"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.DKGRAY)
        }

        layout.addView(title)
        layout.addView(welcome)
        layout.addView(recordBadge)
        layout.addView(twoPlayer, btnParams)
        layout.addView(singlePlayer, btnParams)
        layout.addView(userRecordBtn, btnParams)
        layout.addView(history, btnParams)
        layout.addView(logout, btnParams)
        setContentView(layout)

        twoPlayer.setOnClickListener {
            gameMode = "TWO_PLAYER"
            scoreX = 0
            scoreO = 0
            draws = 0
            createGameScreen()
        }

        singlePlayer.setOnClickListener {
            gameMode = "SINGLE_PLAYER"
            scoreX = 0
            scoreO = 0
            draws = 0
            createGameScreen()
        }

        userRecordBtn.setOnClickListener {
            showIndividualUserRecord()
        }

        history.setOnClickListener {
            showHistory()
        }

        logout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun createGameScreen() {
        cells.clear()

        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            setBackgroundColor(backgroundColor)
        }

        val title = TextView(this).apply {
            text = "TIC TAC TOE"
            textSize = 28f
            setTextColor(purple)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        main.addView(title)

        val mode = TextView(this).apply {
            text = if (gameMode == "SINGLE_PLAYER")
                "$username (X) vs Computer (O)"
            else
                "$username (X) vs Player O (O)"
            textSize = 17f
            setTextColor(purple)
            gravity = Gravity.CENTER
            setPadding(5, 8, 5, 8)
        }
        main.addView(mode)

        scoreText = TextView(this).apply {
            textSize = 16f
            setTextColor(purple)
            gravity = Gravity.CENTER
            setPadding(5, 8, 5, 8)
        }
        main.addView(scoreText)
        updateScore()

        status = TextView(this).apply {
            text = "$username (X)'s Turn"
            textSize = 20f
            setTextColor(purple)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(5, 10, 5, 16)
        }
        main.addView(status)

        val grid = GridLayout(this).apply {
            columnCount = 3
            rowCount = 3
        }

        for (i in 0 until 9) {
            val cell = Button(this).apply {
                text = ""
                textSize = 36f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                setBackgroundColor(purple)
                isAllCaps = false
            }

            val params = GridLayout.LayoutParams().apply {
                width = 95.dp()
                height = 95.dp()
                setMargins(5, 5, 5, 5)
            }

            grid.addView(cell, params)
            cells.add(cell)

            cell.setOnClickListener {
                playMove(i)
            }
        }
        main.addView(grid)

        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(24, 12, 24, 0)
        }

        val newGame = Button(this).apply {
            text = "NEW GAME"
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            setOnClickListener { resetGame() }
        }

        val recordBtn = Button(this).apply {
            text = "MY RECORD"
            setTextColor(Color.WHITE)
            setBackgroundColor(purple)
            setOnClickListener { showIndividualUserRecord() }
        }

        val history = Button(this).apply {
            text = "GAME HISTORY"
            setOnClickListener { showHistory() }
        }

        val menu = Button(this).apply {
            text = "MAIN MENU"
            setOnClickListener { showModeSelection() }
        }

        main.addView(newGame, btnParams)
        main.addView(recordBtn, btnParams)
        main.addView(history, btnParams)
        main.addView(menu, btnParams)

        setContentView(main)
        resetGame()
    }

    private fun playMove(index: Int) {
        if (gameOver || board[index].isNotEmpty()) return

        board[index] = currentPlayer
        cells[index].text = currentPlayer
        cells[index].setTextColor(if (currentPlayer == "X") pink else Color.CYAN)
        cells[index].isEnabled = false

        if (checkWinner()) {
            gameOver = true
            val opponentName = if (gameMode == "SINGLE_PLAYER") "Computer" else "Player O"
            val winnerName = if (currentPlayer == "X") username else opponentName

            if (currentPlayer == "X") {
                scoreX++
            } else {
                scoreO++
            }

            status.text = if (gameMode == "SINGLE_PLAYER") {
                if (currentPlayer == "X") "YOU WIN!" else "COMPUTER WINS!"
            } else {
                "$winnerName ($currentPlayer) Wins!"
            }

            saveGame(winnerName)
            disableBoard()
            updateScore()
        } else if (board.all { it.isNotEmpty() }) {
            gameOver = true
            draws++
            status.text = "IT'S A DRAW!"
            saveGame("Draw")
            disableBoard()
            updateScore()
        } else {
            currentPlayer = if (currentPlayer == "X") "O" else "X"
            if (gameMode == "SINGLE_PLAYER" && currentPlayer == "O") {
                status.text = "Computer's Turn..."
                computerMove()
            } else {
                status.text = if (gameMode == "SINGLE_PLAYER") {
                    "Your Turn - $username (X)"
                } else {
                    val turnOwner = if (currentPlayer == "X") username else "Player O"
                    "$turnOwner ($currentPlayer)'s Turn"
                }
            }
        }
    }

    private fun checkWinner(): Boolean {
        for (pattern in winPatterns) {
            val a = pattern[0]
            val b = pattern[1]
            val c = pattern[2]
            if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c]) {
                pattern.forEach { cells[it].setBackgroundColor(Color.rgb(50, 190, 120)) }
                return true
            }
        }
        return false
    }

    private fun computerMove() {
        if (gameOver) return

        // 1. Win if possible
        var move = findWinningMove("O")
        // 2. Block player's winning move
        if (move == -1) move = findWinningMove("X")
        // 3. Take center
        if (move == -1 && board[4].isEmpty()) move = 4
        // 4. Take a corner
        if (move == -1) move = listOf(0, 2, 6, 8).firstOrNull { board[it].isEmpty() } ?: -1
        // 5. Take any open cell
        if (move == -1) move = board.indices.firstOrNull { board[it].isEmpty() } ?: -1

        if (move != -1) {
            playMove(move)
        }
    }

    private fun findWinningMove(symbol: String): Int {
        for (pattern in winPatterns) {
            val empty = pattern.filter { board[it].isEmpty() }
            val matching = pattern.count { board[it] == symbol }
            if (matching == 2 && empty.size == 1) {
                return empty[0]
            }
        }
        return -1
    }

    private fun disableBoard() {
        cells.forEach { it.isEnabled = false }
    }

    private fun resetGame() {
        for (i in 0 until 9) {
            board[i] = ""
            cells[i].text = ""
            cells[i].isEnabled = true
            cells[i].setBackgroundColor(purple)
        }
        currentPlayer = "X"
        gameOver = false
        status.text = "$username (X)'s Turn"
    }

    private fun updateScore() {
        scoreText.text = "Session Score — X: $scoreX  |  O: $scoreO  |  Draws: $draws"
    }

    private fun saveGame(winner: String) {
        val opponent = if (gameMode == "SINGLE_PLAYER") "Computer" else "Player O"
        userDatabase.saveGame(userId, username, opponent, winner, gameMode)
    }

    private fun showIndividualUserRecord() {
        val record = userDatabase.getUserRecord(userId, username)
        val winRateFormatted = String.format("%.1f%%", record.winRate)

        val summary = """
            Player: ${record.username}
            Registered: ${record.dateRegistered}

            Total Matches Played: ${record.totalGames}
            Wins: ${record.wins}
            Losses: ${record.losses}
            Draws: ${record.draws}
            Win Rate: $winRateFormatted
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("INDIVIDUAL USER RECORD")
            .setMessage(summary)
            .setPositiveButton("CLOSE", null)
            .show()
    }

    private fun showHistory() {
        val history = userDatabase.getUserHistory(userId)
        val message = if (history.isEmpty()) {
            "No games played yet!"
        } else {
            history.joinToString("\n\n----------------\n\n")
        }

        AlertDialog.Builder(this)
            .setTitle("MY GAME HISTORY ($username)")
            .setMessage(message)
            .setPositiveButton("CLOSE", null)
            .show()
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        userDatabase.close()
        super.onDestroy()
    }
}
