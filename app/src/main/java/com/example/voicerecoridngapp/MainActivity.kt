package com.example.voicerecoridngapp


import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException


const val REQUEST_CODE = 200
class MainActivity : AppCompatActivity(),Timer.OnTimerTickListener{

    private lateinit var amps: ArrayList<Float>
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private lateinit var recorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer
    private var dirPath = ""
    //private var filename = ""
    private var isRecord = false
    private var isPause = false
    private var a : Float = 0F

    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val actionBar = supportActionBar
        actionBar!!.title = "VisualizeYourVoice"
        val colorDrawable = ColorDrawable(Color.parseColor("#4f617d"))
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(colorDrawable)
        };
        btnRecord.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        btnReplay.setImageResource(R.drawable.ic_replay)
        btnStop.setImageResource(R.drawable.ic_stop)


        permissionGranted = ActivityCompat.checkSelfPermission(this,permissions[0]) == PackageManager.PERMISSION_GRANTED
        if(!permissionGranted)
            ActivityCompat.requestPermissions(this,permissions, REQUEST_CODE)


        timer = Timer(this)
        btnRecord.setOnClickListener{

            when{
                isRecord -> pauseRecord()
                isPause -> resumeRecord()
                else -> startRecording()
            }


        }
        btnStop.setOnClickListener{
            stopRecord()
        }
        btnReplay.setOnClickListener {
            replay()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED

    }




    private fun pauseRecord()
    {
        Toast.makeText(this, "Recording paused", Toast.LENGTH_LONG).show()
        recorder.pause()
        isRecord = false
        isPause = true
        btnRecord.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        timer.pause()


    }


    private fun resumeRecord(){
        Toast.makeText(this, "Recording resumed", Toast.LENGTH_LONG).show()

        recorder.resume()
        isRecord = true
        isPause = false
        btnRecord.setImageResource(R.drawable.icpause)

        timer.start()



    }
    private fun startRecording() {
        btnRecord.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"

        //filename = "audio_record_latest"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setOutputFile(getRecordingFilePath())
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            //setOutputFile("$dirPath$filename.mp3")

            try {
                prepare()

            }
            catch (e:IOException){
                e.printStackTrace()
            }
            start()
        }
        btnRecord.setImageResource(R.drawable.icpause)
        isRecord = true
        isPause = false

        timer.start()
        Toast.makeText(this, "Recording started", Toast.LENGTH_LONG).show()
        a=recorder.maxAmplitude.toFloat()

    }

    private fun stopRecord()
    {
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_LONG).show()
        timer.stop()
        recorder.apply {
            stop()
            release()
        }

        isPause = false
        isRecord = false
        btnRecord.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        tvTimer.text = "00:00:00"
        amps = waveformView.clearWave()

    }

    private fun replay()
    {
        Toast.makeText(this, "Recording playing", Toast.LENGTH_LONG).show()

        isPause = false
        isRecord = false
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(getRecordingFilePath())
            mediaPlayer.prepare()
            mediaPlayer.start()


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onTimerTick(duration: String) {
        tvTimer.text = duration
        //waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
        waveformView.addAmp(recorder.maxAmplitude.toFloat())



    }
    private fun getRecordingFilePath(): String? {
        val contextWrapper = ContextWrapper(applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file = File(musicDirectory, "testRecordingFile" + ".mp3")
        return file.path
    }


}