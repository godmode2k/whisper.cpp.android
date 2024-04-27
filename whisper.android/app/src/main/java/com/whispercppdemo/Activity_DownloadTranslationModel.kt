package com.whispercppdemo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.whispercppdemo.ui.main.ListViewAdapter_DownloadTranslationModel
import com.whispercppdemo.ui.main.ListViewItem_DownloadTranslationModel
import kotlinx.coroutines.launch
import java.util.Locale

class Activity_DownloadTranslationModel : ComponentActivity() {
    private val TAG = "Activity_DownloadTranslationModel"

    // Checks downloaded models
    val availableModels = MutableLiveData<List<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView( R.layout.activity_download_translation_model )

        // Checks downloaded models
        val modelManager = RemoteModelManager.getInstance()
        //val availableModels = MutableLiveData<List<String>>()
        // Get translation models stored on the device.
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                // ...

                availableModels.value = models.sortedBy { it.language }.map { it.language }
                Log.w( TAG, "downloaded models: " + availableModels.value )

                init_layout()
            }
            .addOnFailureListener {
                // Error.

                Log.w( TAG, "downloaded models [FAIL]" )
            }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        val app = application as App
        val status = app.get_status_message()
        val textview_status: TextView = findViewById<View>(R.id.TextView_status) as TextView
        textview_status.setText( status )
    }

    private fun init_layout() {
        val app = application as App
        val data = ArrayList<ListViewItem_DownloadTranslationModel>()

        val langs_code = TranslateLanguage.getAllLanguages()
        //Locale.getAvailableLocales().forEach { Log.w( TAG, "===== " + it + ", " + it.displayLanguage ) }
        //Locale.getISOCountries().forEach() { Log.w( TAG, "===== " + it ) }


        val langs_code_data: ArrayList<String> = ArrayList<String>()
        langs_code.forEach() {
            langs_code_data.add( Locale(it).displayLanguage + ", " + it )
            //Log.w( TAG, "===== locale: " + Locale(it).displayLanguage + ", " + it + ", " + Locale(it).getDisplayLanguage(Locale.ENGLISH))
        }
        val langs_code_data_sorted = langs_code_data.sorted()
        langs_code_data_sorted.forEach() {
            val lang_code_all = it.toString().split(",")
            val locale_language = lang_code_all[0].trim()
            val locale_code = lang_code_all[1].trim()
            val locale_code_all = app.get_locale_info( locale_code )

            Log.w( TAG, "locale code = " + locale_code + ", locale_code_all = " + locale_code_all )

            if ( locale_code_all.isEmpty() || locale_code_all.equals("null") ) {
                Log.w( TAG, "locale_code_all: empty or NULL; skip..." )
            }
            else {
                // Checks downloaded models
                var download_message = "DOWNLOAD"

                if ( availableModels != null ) {
                    availableModels.value!!.forEach {
                        if (it.trim().equals(locale_code.trim())) {
                            download_message = "DONE"
                        }
                    }
                }

                data.add( ListViewItem_DownloadTranslationModel(locale_language, locale_code, locale_code_all, download_message) )
            }
        }


        //val adapter = ListViewAdapter_DownloadTranslationModel(this@Activity_DownloadTranslationModel, data)
        val adapter = ListViewAdapter_DownloadTranslationModel(this, data)
        val listview_download_translation_model: ListView = findViewById<View>(R.id.ListView_download_translation_model) as ListView
        listview_download_translation_model.setAdapter(adapter)
        listview_download_translation_model.setOnItemClickListener { parent, view, position, id ->
            val app = application as App
            val item = adapter.getItem(position) as ListViewItem_DownloadTranslationModel
            val lang_code_all = "[" + item.item_locale + ", " + item.item_locale_code + "]"
            Log.w( TAG, "selected pos = " + position + ", data = " + item.toString() + ", " +
                    item.item_locale + ", " + ", lang_code = " + item.item_locale_code + ", locale_code_all = " + item.item_locale_code_all )

            Toast.makeText( this, "Downloading: " + lang_code_all, Toast.LENGTH_SHORT ).show()

            val textview_status: TextView = findViewById<View>(R.id.TextView_status) as TextView


            Log.w( TAG, "Downloading Translation Model...: " + lang_code_all )


            // Progress Dialog
            val dialog = Dialog(this)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(ProgressBar(this))
            dialog.setCanceledOnTouchOutside(false)
            dialog.setOnCancelListener { this.finish() } // back-button: close current Activity
            dialog.show()


            // Download Translation Model
//            val options = TranslatorOptions.Builder()
//                .setSourceLanguage(TranslateLanguage.ENGLISH)
//                //.setTargetLanguage(TranslateLanguage.KOREAN)
//                .setTargetLanguage( lang_code_all[1] )
//                .build()
//            val englishToLangTranslator = Translation.getClient(options)


            //! Note: Locale.[LANG]: (e.g.,) ko, en, ...
            //val englishToLangTranslator = app.set_translator( TranslateLanguage.ENGLISH, TranslateLanguage.KOREAN )
            val englishToLangTranslator = app.set_translator( TranslateLanguage.ENGLISH, item.item_locale_code )

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


            englishToLangTranslator!!.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    // Model downloaded successfully. Okay to start translating.
                    // (Set a flag, unhide the translation UI, etc.)

                    Log.w( TAG, "TRANSLATE: [+] downloaded model...")

                    app.set_status( lang_code_all )
                    val status = app.get_status_message()
                    textview_status.setText( status )

                    //englishToLangTranslator!!.close()
                    dialog.dismiss()

                    Toast.makeText( this, "Downloaded: " + lang_code_all + " [ OK ]", Toast.LENGTH_SHORT ).show()
                }
                .addOnFailureListener { exception ->
                    // Model couldn’t be downloaded or other internal error.
                    // ...

                    Log.w( TAG,"TRANSLATE: [-] downloaded model...")

                    //englishToLangTranslator!!.close()
                    dialog.dismiss()

                    Toast.makeText( this, "Downloaded: " + lang_code_all + " [ FAIL ]", Toast.LENGTH_SHORT ).show()
                }
        }
    }
}