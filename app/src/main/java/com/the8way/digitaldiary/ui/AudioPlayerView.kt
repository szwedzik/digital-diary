package com.the8way.digitaldiary.ui

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.the8way.digitaldiary.R

class AudioPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mediaPlayer: MediaPlayer? = null
    private var playPauseButton: ImageButton
    private var audioDuration: TextView
    private var isPlaying = false
    private var audioPath: String? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var originalDuration: String = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.audio_player_layout, this, true)
        playPauseButton = findViewById(R.id.playPauseButton)
        audioDuration = findViewById(R.id.audioDuration)

        playPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
            } else {
                playAudio()
            }
        }
    }

    fun setAudioPath(path: String?) {
        audioPath = path
        updateAudioDuration()
    }

    private fun playAudio() {
        audioPath?.let {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(it)
                prepare()
                start()
                setOnCompletionListener {
                    this@AudioPlayerView.isPlaying = false
                    playPauseButton.setImageResource(R.drawable.ic_play)
                    updateRunnable?.let { it1 -> handler.removeCallbacks(it1) }
                    audioDuration.text = originalDuration
                }
            }
            isPlaying = true
            playPauseButton.setImageResource(R.drawable.ic_pause)
            startUpdatingTimer()
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        playPauseButton.setImageResource(R.drawable.ic_play)
        updateRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun startUpdatingTimer() {
        updateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    val remainingTime = (it.duration - it.currentPosition) / 1000
                    val minutes = remainingTime / 60
                    val seconds = remainingTime % 60
                    audioDuration.text = String.format("%d:%02d", minutes, seconds)
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(updateRunnable as Runnable)
    }

    private fun updateAudioDuration() {
        audioPath?.let {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(it)
                prepare()
                val duration = duration / 1000 // duration in seconds
                val minutes = duration / 60
                val seconds = duration % 60
                originalDuration = String.format("%d:%02d", minutes, seconds)
                audioDuration.text = originalDuration
                release()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mediaPlayer?.release()
        mediaPlayer = null
        updateRunnable?.let { handler.removeCallbacks(it) }
    }
}
