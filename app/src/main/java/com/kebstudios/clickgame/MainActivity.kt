package com.kebstudios.clickgame

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.kebstudios.clickgame.api.API
import com.kebstudios.clickgame.api.objects.ClickResponse
import com.kebstudios.clickgame.api.objects.User
import com.kebstudios.clickgame.recycler_view.ListAdapter
import com.plattysoft.leonids.ParticleSystem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.winners_menu.*

class MainActivity : AppCompatActivity() {

    companion object MainActivity {
        private const val USERNAME_TAG = "username"
    }

    private lateinit var mRecycler: RecyclerView
    private lateinit var mAdapter: ListAdapter
    private lateinit var mViewManager: RecyclerView.LayoutManager
    private lateinit var mFanfarePlayer: MediaPlayer
    private lateinit var mVibrator: Vibrator
    private lateinit var mPreferences: SharedPreferences

    private var lastUsername = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFanfarePlayer = MediaPlayer.create(this, R.raw.fanfare)
        mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        volumeControlStream = AudioManager.STREAM_MUSIC
        mPreferences = getPreferences(Context.MODE_PRIVATE)

        mAdapter = ListAdapter(listOf())
        mViewManager = LinearLayoutManager(this)

        mRecycler = findViewById<RecyclerView>(R.id.winners_list).apply {
            setHasFixedSize(true)

            layoutManager = mViewManager

            adapter = mAdapter
        }

        button.setOnClickListener {
            buttonClick()
        }

        winner_button.setOnClickListener {
            winnerButtonClick()
            toggleWinnersMenu()
        }

        name_input.setText(mPreferences.getString(USERNAME_TAG, ""))

        name_input.setOnFocusChangeListener { _, hasFocus ->
            name_input.isCursorVisible = hasFocus
        }

    }

    private fun buttonClick() {
        val name = name_input.text.toString()
        if (name.isEmpty()) {
            info_text.text = resources.getString(R.string.username_missing)
            showInfoBox()
        } else {
            if (lastUsername != name) {
                mPreferences.edit().putString(USERNAME_TAG, name).apply()
            }
            AsyncTask.execute {
                val user = User(name)

                val result = API.sendClickRequest(user)

                runOnUiThread {
                    handleResult(result)
                }
            }
        }
    }

    private fun handleResult(result: ClickResponse) {
        var msg =
            when (result.winAmount) {
                500 -> {
                    shootParticles(R.drawable.gold_confetti, 1000)
                    playFanfare()
                    vibrate(VibrationEffect.createWaveform(longArrayOf(0, 75, 50, 75, 50, 75, 50, 300), -1))
                    resources.getString(R.string.congratulation_string)
                        .replace("*", resources.getString(R.string.prize_3))
                }
                200 -> {
                    shootParticles(R.drawable.silver_confetti, 300)
                    playFanfare()
                    vibrate(VibrationEffect.createWaveform(longArrayOf(0, 75, 50, 300), -1))
                    resources.getString(R.string.congratulation_string)
                        .replace("*", resources.getString(R.string.prize_2))
                }
                100 -> {
                    shootParticles(R.drawable.bronze_confetti, 50)
                    playFanfare()
                    vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                    resources.getString(R.string.congratulation_string)
                        .replace("*", resources.getString(R.string.prize_1))
                }
                else -> resources.getString(R.string.no_win)
            }
        msg += resources.getString(R.string.next_win)
            .replace("*", result.clicksUntilNextWin.toString())
        info_text.text = msg

        showInfoBox()
    }

    private fun showInfoBox() {
        info_text_container.clearAnimation()
        val startAlpha = info_text_container.alpha
        info_text_container.animate()
            .setStartDelay(0)
            .setDuration(((1f - startAlpha) * 500).toLong())
            .alpha(1f)
            .withStartAction {
                info_text_container.visibility = View.VISIBLE
            }.withEndAction {
                info_text_container.animate()
                    .setStartDelay(2000)
                    .setDuration(500)
                    .alpha(0f)
                    .withEndAction {
                        info_text_container.visibility = View.GONE
                    }.start()
            }.start()
    }

    private fun shootParticles(res: Int, amount: Int) {
        ParticleSystem(this, amount, res, 3000).apply {
            setSpeedRange(0.05f, 0.2f)
            setRotationSpeedRange(100f, 200f)
            setFadeOut(200)
            oneShot(button, amount)
        }
    }

    private fun playFanfare() {
        if (mFanfarePlayer.isPlaying) {
            mFanfarePlayer.pause()
            mFanfarePlayer.seekTo(0)
        }
        mFanfarePlayer.start()
    }

    private fun vibrate(effect: VibrationEffect) {
        mVibrator.vibrate(effect)
    }

    private fun winnerButtonClick() {
        if (winners_menu_container.visibility == View.GONE) {
            wait_orb.visibility = View.VISIBLE
            winners_list.visibility = View.GONE
        }

        AsyncTask.execute {
            val result = API.sendGetWinnersRequest()

            runOnUiThread {
                mAdapter.updateData(result)

                wait_orb.animate()
                    .setDuration(250)
                    .alpha(-1f)
                    .withEndAction {
                        wait_orb.visibility = View.GONE
                        winners_list.alpha = 0f
                        winners_list.visibility = View.VISIBLE
                        winners_list.animate().alpha(1f).start()
                    }.start()
            }
        }
    }

    private fun toggleWinnersMenu() {
        if (winners_menu_container.visibility == View.GONE) {
            winners_menu_container.translationX = Utils.convertDpToPx(250f)
            winners_menu_container.clearAnimation()
            winners_menu_container
                .animate()
                .setDuration(500)
                .translationXBy(Utils.convertDpToPx(-250f))
                .withStartAction {
                    winners_menu_container.visibility = View.VISIBLE
                    winner_button.clearAnimation()
                    winner_button
                        .animate()
                        .setDuration(500)
                        .rotationBy(-360f + winner_button.rotation)
                        .start()
                }.start()
        } else {
            winners_menu_container.clearAnimation()
            winners_menu_container
                .animate()
                .setDuration(500)
                .translationXBy(Utils.convertDpToPx(250f))
                .withEndAction {
                    winners_menu_container.visibility = View.GONE
                }.start()

            winner_button.clearAnimation()
            winner_button
                .animate()
                .setDuration(500)
                .rotationBy(-winner_button.rotation)
                .start()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return super.onTouchEvent(event)

        if (name_input.isFocused && !Utils.bounds(name_input).contains(event.rawX.toInt(), event.rawY.toInt())) {
            root.requestFocus()
            hideKeyboard()
        }
        if (winners_menu_container.visibility == View.VISIBLE) {
            if (event.action == MotionEvent.ACTION_DOWN
                && !Utils.bounds(toolbar).contains(event.rawX.toInt(), event.rawY.toInt())
                && !Utils.bounds(winners_menu_container).contains(event.rawX.toInt(), event.rawY.toInt())
            ) {
                toggleWinnersMenu()
                return true
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
