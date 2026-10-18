```kotlin
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

    // Komponen UI yang digunakan dalam permainan
    private lateinit var tvScore: TextView
    private lateinit var tvResult: TextView
    private lateinit var progressScore: LinearProgressIndicator
    private lateinit var cardResult: MaterialCardView
    private lateinit var tilGuess: TextInputLayout
    private lateinit var etGuess: TextInputEditText
    private lateinit var btnGuess: MaterialButton
    private lateinit var btnReset: MaterialButton
    private lateinit var btnTheme: MaterialButton

    // Variabel untuk menyimpan kondisi permainan
    private var secretNumber = 0
    private var score = 100
    private var gameOver = false
    private var lastMessage = "Ayo mulai menebak!"
    private var lastStyle = Style.NEUTRAL

    // Inisialisasi tampilan pesan berdasarkan hasil tebakan
    private enum class Style { NEUTRAL, SUCCESS, ERROR }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Mengambil pengaturan tema yang tersimpan
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Menghubungkan variabel dengan komponen pada layout
        tvScore = findViewById(R.id.tvScore)
        tvResult = findViewById(R.id.tvResult)
        progressScore = findViewById(R.id.progressScore)
        cardResult = findViewById(R.id.cardResult)
        tilGuess = findViewById(R.id.tilGuess)
        etGuess = findViewById(R.id.etGuess)
        btnGuess = findViewById(R.id.btnGuess)
        btnReset = findViewById(R.id.btnReset)
        btnTheme = findViewById(R.id.btnTheme)

        // Memulihkan kondisi permainan jika Activity dibuat ulang
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
            // Memulai permainan baru saat aplikasi pertama dibuka
            startNewGame()
        }

        updateThemeIcon()

        // Mengganti tema terang dan gelap sekaligus menyimpan pilihannya
        btnTheme.setOnClickListener {
            val newMode = if (isNightMode()) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES

            prefs.edit().putInt("night_mode", newMode).apply()
            AppCompatDelegate.setDefaultNightMode(newMode)
        }

        // Mengatur aksi tombol tebakan dan mulai ulang
        btnGuess.setOnClickListener { checkGuess() }
        btnReset.setOnClickListener { startNewGame() }

        // Memeriksa tebakan ketika tombol Done pada keyboard ditekan
        etGuess.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkGuess()
                true
            } else false
        }
    }

    // Menyimpan kondisi permainan saat Activity dihentikan sementara atau dibuat ulang
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("secret", secretNumber)
        outState.putInt("score", score)
        outState.putBoolean("gameOver", gameOver)
        outState.putString("message", lastMessage)
        outState.putInt("style", lastStyle.ordinal)
    }

    // Memeriksa apakah aplikasi sedang menggunakan tema gelap
    private fun isNightMode(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    // Mengubah ikon tombol tema sesuai mode yang sedang digunakan
    private fun updateThemeIcon() {
        btnTheme.setIconResource(
            if (isNightMode()) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
        )
    }

    // Mengatur ulang skor, angka rahasia, dan tampilan untuk permainan baru
    private fun startNewGame() {
        secretNumber = Random.nextInt(0, 101) // Angka acak dari 0 sampai 100
        score = 100
        gameOver = false

        etGuess.text?.clear()
        tilGuess.error = null
        tilGuess.isEnabled = true
        btnGuess.isEnabled = true

        showResult("Ayo mulai menebak!", Style.NEUTRAL)
        updateScore()
    }

    // Memvalidasi dan memeriksa angka yang dimasukkan oleh pemain
    private fun checkGuess() {
        if (gameOver) return

        // Mengubah input menjadi angka dan memastikan nilainya berada pada rentang 0–100
        val guess = etGuess.text.toString().trim().toIntOrNull()
        if (guess == null || guess !in 0..100) {
            tilGuess.error = "Masukkan angka 0 - 100"
            return
        }
        tilGuess.error = null

        if (guess == secretNumber) {
            // Pemain menang jika tebakan sama dengan angka rahasia
            showResult("Benar! Angkanya adalah $secretNumber.\nSkor akhir: $score", Style.SUCCESS)
            endGame()
        } else {
            // Skor berkurang 10 setiap kali tebakan salah
            score -= 10
            updateScore()

            if (score <= 0) {
                // Permainan berakhir jika skor mencapai nol
                showResult("Game over! Angka yang benar adalah $secretNumber.", Style.ERROR)
                endGame()
            } else {
                // Memberikan petunjuk apakah tebakan terlalu kecil atau terlalu besar
                val hint = if (guess < secretNumber) "terlalu kecil" else "terlalu besar"
                showResult("Tebakan $guess $hint.\nSkor dikurangi 10.", Style.ERROR)
            }
        }

        etGuess.text?.clear()
    }

    // Mengakhiri permainan dan menonaktifkan input tebakan
    private fun endGame() {
        gameOver = true
        btnGuess.isEnabled = false
        tilGuess.isEnabled = false
    }

    // Memperbarui nilai skor dan indikator progres
    private fun updateScore() {
        tvScore.text = score.toString()
        progressScore.setProgressCompat(score, true)
    }

    // Menampilkan pesan hasil dengan warna sesuai status permainan
    private fun showResult(message: String, style: Style) {
        lastMessage = message
        lastStyle = style
        tvResult.text = message

        // Memilih warna latar dan teks berdasarkan jenis pesan
        val (bgAttr, fgAttr) = when (style) {
            Style.NEUTRAL -> com.google.android.material.R.attr.colorSurfaceVariant to
                    com.google.android.material.R.attr.colorOnSurfaceVariant

            Style.SUCCESS -> com.google.android.material.R.attr.colorTertiaryContainer to
                    com.google.android.material.R.attr.colorOnTertiaryContainer

            Style.ERROR -> com.google.android.material.R.attr.colorErrorContainer to
                    com.google.android.material.R.attr.colorOnErrorContainer
        }

        // Menerapkan warna berdasarkan tema Material yang aktif
        cardResult.setCardBackgroundColor(MaterialColors.getColor(cardResult, bgAttr))
        tvResult.setTextColor(MaterialColors.getColor(tvResult, fgAttr))
    }
}
```
