// Letakkan di: app/src/main/java/<package-anda>/MainActivity.kt
// Ganti baris package di bawah sesuai nama package proyek Anda.
package com.example.tebakangka

import android.content.res.Configuration
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvResult: TextView
    private lateinit var progressScore: LinearProgressIndicator
    private lateinit var cardResult: MaterialCardView
    private lateinit var tilGuess: TextInputLayout
    private lateinit var etGuess: TextInputEditText
    private lateinit var btnGuess: MaterialButton
    private lateinit var btnReset: MaterialButton
    private lateinit var btnTheme: MaterialButton

    private var secretNumber = 0
    private var score = 100
    private var gameOver = false
    private var lastMessage = "Ayo mulai menebak!"
    private var lastStyle = Style.NEUTRAL

    private enum class Style { NEUTRAL, SUCCESS, ERROR }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Terapkan mode tema yang tersimpan sebelum tampilan dibuat
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvScore = findViewById(R.id.tvScore)
        tvResult = findViewById(R.id.tvResult)
        progressScore = findViewById(R.id.progressScore)
        cardResult = findViewById(R.id.cardResult)
        tilGuess = findViewById(R.id.tilGuess)
        etGuess = findViewById(R.id.etGuess)
        btnGuess = findViewById(R.id.btnGuess)
        btnReset = findViewById(R.id.btnReset)
        btnTheme = findViewById(R.id.btnTheme)

        // Pulihkan permainan setelah ganti tema (activity dibuat ulang)
        if (savedInstanceState != null) {
            secretNumber = savedInstanceState.getInt("secret")
            score = savedInstanceState.getInt("score")
            gameOver = savedInstanceState.getBoolean("gameOver")
            lastMessage = savedInstanceState.getString("message") ?: lastMessage
            lastStyle = Style.values()[savedInstanceState.getInt("style")]
            updateScore()
            showResult(lastMessage, lastStyle)
            btnGuess.isEnabled = !gameOver
            tilGuess.isEnabled = !gameOver
        } else {
            startNewGame()
        }

        updateThemeIcon()

        btnTheme.setOnClickListener {
            val newMode = if (isNightMode()) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
            prefs.edit().putInt("night_mode", newMode).apply()
            AppCompatDelegate.setDefaultNightMode(newMode)
        }
        btnGuess.setOnClickListener { checkGuess() }
        btnReset.setOnClickListener { startNewGame() }
        etGuess.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkGuess()
                true
            } else false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("secret", secretNumber)
        outState.putInt("score", score)
        outState.putBoolean("gameOver", gameOver)
        outState.putString("message", lastMessage)
        outState.putInt("style", lastStyle.ordinal)
    }

    private fun isNightMode(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    private fun updateThemeIcon() {
        // Mode gelap -> tampilkan matahari (ketuk untuk terang), sebaliknya bulan
        btnTheme.setIconResource(
            if (isNightMode()) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
        )
    }

    private fun startNewGame() {
        secretNumber = Random.nextInt(0, 101) // acak 0 sampai 100
        score = 100
        gameOver = false
        etGuess.text?.clear()
        tilGuess.error = null
        tilGuess.isEnabled = true
        btnGuess.isEnabled = true
        showResult("Ayo mulai menebak!", Style.NEUTRAL)
        updateScore()
    }

    private fun checkGuess() {
        if (gameOver) return

        val guess = etGuess.text.toString().trim().toIntOrNull()
        if (guess == null || guess !in 0..100) {
            tilGuess.error = "Masukkan angka 0 - 100"
            return
        }
        tilGuess.error = null

        if (guess == secretNumber) {
            showResult("Benar! Angkanya adalah $secretNumber.\nSkor akhir: $score", Style.SUCCESS)
            endGame()
        } else {
            score -= 10
            updateScore()

            if (score <= 0) {
                showResult("Game over! Angka yang benar adalah $secretNumber.", Style.ERROR)
                endGame()
            } else {
                val hint = if (guess < secretNumber) "terlalu kecil" else "terlalu besar"
                showResult("Tebakan $guess $hint.\nSkor dikurangi 10.", Style.ERROR)
            }
        }
        etGuess.text?.clear()
    }

    private fun endGame() {
        gameOver = true
        btnGuess.isEnabled = false
        tilGuess.isEnabled = false
    }

    private fun updateScore() {
        tvScore.text = score.toString()
        progressScore.setProgressCompat(score, true)
    }

    private fun showResult(message: String, style: Style) {
        lastMessage = message
        lastStyle = style
        tvResult.text = message
        val (bgAttr, fgAttr) = when (style) {
            Style.NEUTRAL -> com.google.android.material.R.attr.colorSurfaceVariant to
                    com.google.android.material.R.attr.colorOnSurfaceVariant
            Style.SUCCESS -> com.google.android.material.R.attr.colorTertiaryContainer to
                    com.google.android.material.R.attr.colorOnTertiaryContainer
            Style.ERROR -> com.google.android.material.R.attr.colorErrorContainer to
                    com.google.android.material.R.attr.colorOnErrorContainer
        }
        cardResult.setCardBackgroundColor(MaterialColors.getColor(cardResult, bgAttr))
        tvResult.setTextColor(MaterialColors.getColor(tvResult, fgAttr))
    }
}