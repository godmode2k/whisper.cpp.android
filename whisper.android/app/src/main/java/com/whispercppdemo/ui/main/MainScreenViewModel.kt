package com.whispercppdemo.ui.main

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.whispercppdemo.media.decodeWaveFile
import com.whispercppdemo.recorder.Recorder
import com.whispercpp.whisper.WhisperContext
import com.whispercppdemo.Activity_DownloadTranslationModel
import com.whispercppdemo.App
import com.whispercppdemo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private const val LOG_TAG = "MainScreenViewModel"

class MainScreenViewModel(private val application: Application) : ViewModel() {
    var canTranscribe by mutableStateOf(false)
        private set
    var dataLog by mutableStateOf("")
        private set
    var isRecording by mutableStateOf(false)
        private set

    private val modelsPath = File(application.filesDir, "models")
    private val samplesPath = File(application.filesDir, "samples")
    private var recorder: Recorder = Recorder()
    private var whisperContext: com.whispercpp.whisper.WhisperContext? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordedFile: File? = null


    // TTS
    private var m_tts: TextToSpeech? = null


    init {
        viewModelScope.launch {
            printSystemInfo()
            loadData()

            // TTS
            tts_init()
            //val app = application as App
            //app.tts_init(application.applicationContext)
        }
    }

    private suspend fun printSystemInfo() {
        printMessage(String.format("System Info: %s\n", com.whispercpp.whisper.WhisperContext.getSystemInfo()))
    }

    private suspend fun loadData() {
        printMessage("Loading data...\n")
        try {
            copyAssets()
            loadBaseModel()
            canTranscribe = true
        } catch (e: Exception) {
            Log.w(LOG_TAG, e)
            printMessage("${e.localizedMessage}\n")
        }
    }

    private suspend fun printMessage(msg: String) = withContext(Dispatchers.Main) {
        dataLog += msg
    }

    private suspend fun copyAssets() = withContext(Dispatchers.IO) {
        modelsPath.mkdirs()
        samplesPath.mkdirs()
        //application.copyData("models", modelsPath, ::printMessage)
        application.copyData("samples", samplesPath, ::printMessage)
        printMessage("All data copied to working directory.\n")
    }

    private suspend fun loadBaseModel() = withContext(Dispatchers.IO) {
        printMessage("Loading model...\n")
        val models = application.assets.list("models/")
        if (models != null) {
            whisperContext = com.whispercpp.whisper.WhisperContext.createContextFromAsset(application.assets, "models/" + models[0])
            printMessage("Loaded model ${models[0]}.\n")
        }

        //val firstModel = modelsPath.listFiles()!!.first()
        //whisperContext = WhisperContext.createContextFromFile(firstModel.absolutePath)
    }

    fun benchmark() = viewModelScope.launch {
        runBenchmark(6)
    }

    fun transcribeSample() = viewModelScope.launch {
        transcribeAudio(getFirstSample())
    }

    private suspend fun runBenchmark(nthreads: Int) {
        if (!canTranscribe) {
            return
        }

        canTranscribe = false

        printMessage("Running benchmark. This will take minutes...\n")
        whisperContext?.benchMemory(nthreads)?.let{ printMessage(it) }
        printMessage("\n")
        whisperContext?.benchGgmlMulMat(nthreads)?.let{ printMessage(it) }

        canTranscribe = true
    }

    private suspend fun getFirstSample(): File = withContext(Dispatchers.IO) {
        samplesPath.listFiles()!!.first()
    }

    private suspend fun readAudioSamples(file: File): FloatArray = withContext(Dispatchers.IO) {
        stopPlayback()
        startPlayback(file)
        return@withContext decodeWaveFile(file)
    }

    private suspend fun stopPlayback() = withContext(Dispatchers.Main) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private suspend fun startPlayback(file: File) = withContext(Dispatchers.Main) {
        mediaPlayer = MediaPlayer.create(application, file.absolutePath.toUri())
        mediaPlayer?.start()
    }

    private suspend fun transcribeAudio(file: File) {
        if (!canTranscribe) {
            return
        }

        canTranscribe = false

        try {
            printMessage("\nReading wave samples... ")
            val data = readAudioSamples(file)
            printMessage("${data.size / (16000 / 1000)} ms\n")
            printMessage("Transcribing data...\n")
            val start = System.currentTimeMillis()
            val text = whisperContext?.transcribeData(data)
            val elapsed = System.currentTimeMillis() - start
            printMessage("Done ($elapsed ms): $text\n")

            // text-to-text translation
            text_to_text_translation( text.toString() )
        } catch (e: Exception) {
            Log.w(LOG_TAG, e)
            printMessage("${e.localizedMessage}\n")
        }

        canTranscribe = true
    }

    fun toggleRecord() = viewModelScope.launch {
        try {
            if (isRecording) {
                recorder.stopRecording()
                isRecording = false
                recordedFile?.let { transcribeAudio(it) }
            } else {
                stopPlayback()
                val file = getTempFileForRecording()
                recorder.startRecording(file) { e ->
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            printMessage("${e.localizedMessage}\n")
                            isRecording = false
                        }
                    }
                }
                isRecording = true
                recordedFile = file
            }
        } catch (e: Exception) {
            Log.w(LOG_TAG, e)
            printMessage("${e.localizedMessage}\n")
            isRecording = false
        }
    }

    private suspend fun getTempFileForRecording() = withContext(Dispatchers.IO) {
        File.createTempFile("recording", "wav")
    }

    override fun onCleared() {
        runBlocking {
            whisperContext?.release()
            whisperContext = null
            stopPlayback()
        }
    }

    companion object {
        fun factory() = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                MainScreenViewModel(application)
            }
        }
    }


    // ------------------------------------------------------------------
    // Text-to-Text Translation
    // ------------------------------------------------------------------
    fun text_to_text_translation_model_download() = viewModelScope.launch {
/*
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.KOREAN)
            .build()
        val englishKoreanTranslator = Translation.getClient(options)

        var conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()


//        val modelManager = RemoteModelManager.getInstance()
//        // Get translation models stored on the device.
//        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
//            .addOnSuccessListener { models ->
//                // ...
//            }
//            .addOnFailureListener {
//                // Error.
//            }


        englishKoreanTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start translating.
                // (Set a flag, unhide the translation UI, etc.)

                Log.w( LOG_TAG, "TRANSLATE: [+] downloaded model...")
                viewModelScope.launch {
                    printMessage("\n[+] Downloaded Translation Model\n")
                }

                englishKoreanTranslator.close()
            }
            .addOnFailureListener { exception ->
                // Model couldn’t be downloaded or other internal error.
                // ...

                Log.w( LOG_TAG, "TRANSLATE: [-] downloaded model...")
                viewModelScope.launch {
                    printMessage("\n[-] Downloaded Translation Model\n")
                }

                englishKoreanTranslator.close()
            }
 */


        var intent = Intent(application, Activity_DownloadTranslationModel::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK )
        application.applicationContext.startActivity( intent )
    }
    private suspend fun text_to_text_translation(text: String) {
        var translate_text = ""

        Log.w( LOG_TAG, "TRANSLATE: source text = " + text)

        val translate_start = System.currentTimeMillis()

        try {
/*
            // Create an English-Lang translator:
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.KOREAN)
                .build()
            val englishToLangTranslator = Translation.getClient(options)

            var conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            englishToLangTranslator!!.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    // Model downloaded successfully. Okay to start translating.
                    // (Set a flag, unhide the translation UI, etc.)

                    Log.w( LOG_TAG, "TRANSLATE: [+] downloaded model...")

                    englishToLangTranslator.translate(text)
                        .addOnSuccessListener { translatedText ->
                            val translate_elapsed = System.currentTimeMillis() - translate_start

                            // Translation successful.
                            translate_text = translatedText
                            Log.w( LOG_TAG, "TRANSLATE: [+] translate: " + translate_text)

                            // translated text to TextView (result)
                            viewModelScope.launch {
                                printMessage("\ntranslation Done ($translate_elapsed ms): $translate_text\n")
                            }

                            englishToLangTranslator!!.close()
                        }
                        .addOnFailureListener { exception ->
                            // Error.
                            // ...

                            Log.w( LOG_TAG, "TRANSLATE: [-] translate: " + translate_text)
                            englishToLangTranslator!!.close()
                        }
                }
                .addOnFailureListener { exception ->
                    // Model couldn’t be downloaded or other internal error.
                    // ...

                    Log.w( LOG_TAG, "TRANSLATE: [-] downloaded model...")
                    englishToLangTranslator!!.close()
                }


            englishToLangTranslator!!.translate(text)
                .addOnSuccessListener { translatedText ->
                    // Translation successful.

                    val translate_elapsed = System.currentTimeMillis() - translate_start
                    translate_text = translatedText
                    Log.w( LOG_TAG, "TRANSLATE: [+] translate: " + translate_text)

                    viewModelScope.launch {
                        printMessage("\n[+] translation ($translate_elapsed ms): $translate_text\n")
                        printMessage("\n[+] TTS now...\n")
                        tts_speak( translate_text )
                    }

                    englishToLangTranslator!!.close()
                }
                .addOnFailureListener { exception ->
                    // Error.
                    // ...

                    val translate_elapsed = System.currentTimeMillis() - translate_start
                    Log.w( LOG_TAG, "TRANSLATE: [-] translate: " + translate_text)
                    viewModelScope.launch {
                        printMessage("\n[-] translation ($translate_elapsed ms): $translate_text\n")
                    }

                    englishToLangTranslator!!.close()
                }


            // If you are using a Translator in a Fragment or AppCompatActivity,
            // one easy way to do that is call LifecycleOwner.getLifecycle() on the Fragment or AppCompatActivity,
            // and then call Lifecycle.addObserver. For example:
            //
            //val options = ...
            //val translator = Translation.getClient(options)
            //getLifecycle().addObserver(translator)

            //Log.w( LOG_TAG, "TRANSLATE: close...")
            //englishKoreanTranslator.close()
*/



            val app = application as App
            val englishToLangTranslator = app.get_translator()

            englishToLangTranslator!!.translate(text)
                .addOnSuccessListener { translatedText ->
                    // Translation successful.

                    val translate_elapsed = System.currentTimeMillis() - translate_start
                    translate_text = translatedText
                    Log.w( LOG_TAG, "TRANSLATE: [+] translate: " + translate_text)

                    viewModelScope.launch {
                        printMessage("\n[+] translation ($translate_elapsed ms):\n$translate_text\n")
                        printMessage("\n[+] TTS now...\n")
                        tts_speak( translate_text )
                        //app.tts_speak( application.applicationContext, translate_text )
                    }

                    //englishToLangTranslator!!.close()
                }
                .addOnFailureListener { exception ->
                    // Error.
                    // ...

                    val translate_elapsed = System.currentTimeMillis() - translate_start
                    Log.w( LOG_TAG, "TRANSLATE: [-] translate: " + translate_text)
                    viewModelScope.launch {
                        printMessage("\n[-] translation ($translate_elapsed ms): $translate_text\n")
                    }

                    //englishToLangTranslator!!.close()
                }


        } catch (e: Exception) {
            Log.w(LOG_TAG, e)
            printMessage("${e.localizedMessage}\n")
        }
    }


    // ------------------------------------------------------------------
    // TTS
    // ------------------------------------------------------------------
    private fun tts_init() {
        m_tts = TextToSpeech(application.applicationContext, TextToSpeech.OnInitListener {
            status ->
            if ( status != TextToSpeech.ERROR ) {
                //! Note: Locale.[LANG]: (e.g.,) ko_KR, ...
                //m_tts!!.setLanguage( Locale.getDefault() )
                //m_tts!!.setLanguage( Locale.ENGLISH )
                //m_tts!!.setLanguage( Locale.KOREAN )

                val app = application as App
                val target_lang = Locale(app.get_target_lang())
                if ( target_lang != null ) {
                    m_tts!!.setLanguage(target_lang)
                    Log.w( LOG_TAG, "tts_init: TRANSLATE: [+] translate to: " + target_lang )
                }
                else {
                    m_tts!!.setLanguage( Locale.getDefault() )
                    Log.w( LOG_TAG, "tts_init: TRANSLATE: [+] translate to: " + Locale.getDefault() )
                }
            }
        })
    }
    fun tts_speak(text: String) {
        if ( m_tts == null ) {
            tts_init()
        }

        //var text: CharSequence
        if ( m_tts!!.isSpeaking ) {
            m_tts!!.stop()
        }

        val app = application as App
        val target_lang = Locale(app.get_target_lang())
        if ( target_lang != null ) {
            m_tts!!.setLanguage(target_lang)
            Log.w( LOG_TAG, "TRANSLATE: [+] translate to: " + target_lang )
        }
        else {
            m_tts!!.setLanguage( Locale.getDefault() )
            Log.w( LOG_TAG, "TRANSLATE: [+] translate to: " + Locale.getDefault() )
        }

        //m_tts!!.setPitch( 1.0f )
        //m_tts!!.setSpeechRate( 1.0f )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            m_tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        else {
            m_tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }
    fun tts_stop() {
        if ( m_tts != null ) {
            m_tts!!.stop()
            m_tts!!.shutdown()
        }
        m_tts = null
    }


}

private suspend fun Context.copyData(
    assetDirName: String,
    destDir: File,
    printMessage: suspend (String) -> Unit
) = withContext(Dispatchers.IO) {
    assets.list(assetDirName)?.forEach { name ->
        val assetPath = "$assetDirName/$name"
        Log.v(LOG_TAG, "Processing $assetPath...")
        val destination = File(destDir, name)
        Log.v(LOG_TAG, "Copying $assetPath to $destination...")
        printMessage("Copying $name...\n")
        assets.open(assetPath).use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Log.v(LOG_TAG, "Copied $assetPath to $destination")
    }
}
