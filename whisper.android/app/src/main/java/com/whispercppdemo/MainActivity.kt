package com.whispercppdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.whispercppdemo.ui.main.MainScreen
import com.whispercppdemo.ui.main.MainScreenViewModel
import com.whispercppdemo.ui.theme.WhisperCppDemoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainScreenViewModel by viewModels { MainScreenViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhisperCppDemoTheme {
                MainScreen(viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        release()
    }

    override fun onStop() {
        super.onStop()

        release()
    }

    override fun onPause() {
        super.onPause()

        release()
    }

    override fun onResume() {
        super.onResume()

        val app = application as App
        if ( app != null ) {
            app.init()
            app.resume_translator()
        }
    }

    private fun release() {
        // TTS
        viewModel::tts_stop

        val app = application as App
        if ( app != null ) {
            app.release()
        }
    }

    // -----------------------------------------------------------

//    private fun _ActivityResultLauncher(): ActivityResultLauncher<Intent> {
//        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            //result: ActivityResult ->
//            result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                Toast.makeText(this, "[+] get data = ", Toast.LENGTH_SHORT).show()
//                //result.data?.getStringExtra("val")
//            }
//            else {
//                Toast.makeText(this, "[-] get data", Toast.LENGTH_SHORT).show()
//            }
//        }
//        return resultLauncher
//    }

//    fun runActivityResultLauncher() {
//        val activityLauncher = _ActivityResultLauncher()
//        var intent = Intent(this, Activity_DownloadTranslationModel::class.java)
//        activityLauncher.launch( intent )
//    }
}
