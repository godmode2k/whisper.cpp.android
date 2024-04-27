package com.whispercppdemo

import android.app.Application
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class App : Application() {
    private val TAG = "App"

    private var m_englishToLangTranslator: Translator? = null
    private var m_options: TranslatorOptions? = null
    private var m_source_lang = ""
    private var m_target_lang = ""
    private var m_status = ""

    //private var m_tts: TextToSpeech? = null

    private var m_locale_info: HashMap<String, String> = HashMap<String, String>()


    override fun onCreate() {
        super.onCreate()

        init()
    }

    override fun onTerminate() {
        super.onTerminate()

        release()
    }

    fun init() {
        Log.w( TAG, "init()" )

        set_locale_info()
    }

    fun release() {
        Log.w( TAG, "release()" )

        if (m_englishToLangTranslator != null ) {
            m_englishToLangTranslator!!.close()
        }

        //tts_stop()
    }

    // ------------------------------------------------------------------
    // Translator: ML-Kit
    // ------------------------------------------------------------------
    fun set_translator(source_lang: String, target_lang: String) : Translator? {
        m_source_lang = source_lang
        m_target_lang = target_lang

        // Create an English-Lang translator:
        //val options = TranslatorOptions.Builder()
        m_options = TranslatorOptions.Builder()
            //.setSourceLanguage(TranslateLanguage.ENGLISH)
            //.setTargetLanguage(TranslateLanguage.KOREAN)
            .setSourceLanguage( source_lang )
            .setTargetLanguage( target_lang )
            .build()
        m_englishToLangTranslator = Translation.getClient(m_options!!)
        return m_englishToLangTranslator
    }

    fun get_translator() : Translator? {
        return m_englishToLangTranslator
    }

    fun get_source_lang(): String {
        return m_source_lang
    }

    fun get_target_lang(): String {
        return m_target_lang
    }

    fun set_status(status: String) {
        m_status = status
    }

    fun get_status(): String {
        return m_status
    }

    fun get_status_message(): String {
        val status = "Translate to: " + get_status()
        return status
    }

    fun resume_translator() {
        set_translator( get_source_lang(), get_target_lang() )
    }

/*
    // ------------------------------------------------------------------
    // TTS
    // ------------------------------------------------------------------
    //private fun tts_init() {
    private fun tts_init(context: android.content.Context) {
        //m_tts = TextToSpeech(this, TextToSpeech.OnInitListener {
        m_tts = TextToSpeech(context, TextToSpeech.OnInitListener {
                status ->
            if ( status != TextToSpeech.ERROR ) {
                //m_tts!!.setLanguage( Locale.getDefault() )
                //m_tts!!.setLanguage( Locale.ENGLISH )
                //m_tts!!.setLanguage( Locale.KOREAN )

                val target_lang = Locale(get_target_lang())
                if ( target_lang != null ) {
                    m_tts!!.setLanguage(target_lang)
                    Log.w( LOG_TAG, "TRANSLATE: [+] translate to: " + target_lang )
                }
                else {
                    m_tts!!.setLanguage( Locale.getDefault() )
                    Log.w( LOG_TAG, "TRANSLATE: [+] translate to: " + Locale.getDefault() )
                }
            }
        })
    }

    //fun tts_speak(text: String) {
    fun tts_speak(context: android.content.Context, text: String) {
        //if ( m_tts == null ) {
        //    tts_init(context)
        //}
        tts_init(context)
        if ( m_tts == null ) {
            Toast.makeText( this, "Error: Cannot use TTS (Text-To-Speech)", Toast.LENGTH_SHORT).show()
            return
        }

        //var text: CharSequence
        if ( m_tts!!.isSpeaking ) {
            m_tts!!.stop()
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
 */

    private fun set_locale_info() {
        if ( m_locale_info == null ) {
            m_locale_info = HashMap<String, String>()
        }

        m_locale_info.put( "gl", "gl_ES"); // 갈리시아어
        m_locale_info.put( "gu", "gu_IN"); // 구자라트어
        m_locale_info.put( "el", "el_CY"); // 그리스어
        m_locale_info.put( "nl", "nl_NL"); // 네덜란드어

        // no_??
        //m_locale_info.put( "no", "no_??"); // 노르웨이어

        m_locale_info.put( "da", "da_DK"); // 덴마크어
        m_locale_info.put( "de", "de_DE"); // 독일어
        m_locale_info.put( "lv", "lv_LV"); // 라트비아어
        m_locale_info.put( "ru", "ru_RU"); // 러시아어
        m_locale_info.put( "ro", "ro_RO"); // 루마니아어
        m_locale_info.put( "lt", "lt_LT"); // 리투아니아어
        m_locale_info.put( "mr", "mr_IN"); // 마라티어
        m_locale_info.put( "mk", "mk_MK"); // 마케도니아어
        m_locale_info.put( "ms", "ms_MY"); // 말레이어
        m_locale_info.put( "mt", "mt_MT"); // 몰타어
        m_locale_info.put( "vi", "vi_VN"); // 베트남어
        m_locale_info.put( "be", "be_BY"); // 벨라루스어
        m_locale_info.put( "bn", "bn_BD"); // 벵골어
        m_locale_info.put( "bg", "bg_BG"); // 불가리아어
        m_locale_info.put( "sw", "sw_KE"); // 스와힐리어
        m_locale_info.put( "sv", "sv_SE"); // 스웨덴어
        m_locale_info.put( "es", "es_ES"); // 스페인어
        m_locale_info.put( "sk", "sk_SK"); // 슬로바키아어
        m_locale_info.put( "sl", "sl_SI"); // 슬로베니아어
        m_locale_info.put( "ar", "ar_AE"); // 아랍어
        m_locale_info.put( "is", "is_IS"); // 아이슬란드어

        // ht_??
        //m_locale_info.put( "ht", "ht_??"); // 아이티어

        m_locale_info.put( "ga", "ga_IE"); // 아일랜드어
        m_locale_info.put( "af", "af_NA"); // 아프리칸스어
        m_locale_info.put( "sq", "sq_AL"); // 알바니아어
        m_locale_info.put( "et", "et_EE"); // 에스토니아어
        m_locale_info.put( "eo", "eo_001"); // 에스페란토어
        m_locale_info.put( "en", "en_US"); // 영어
        m_locale_info.put( "ur", "ur_PK"); // 우르두어
        m_locale_info.put( "uk", "uk_UA"); // 우크라이나어
        m_locale_info.put( "cy", "cy_GB"); // 웨일스어
        m_locale_info.put( "it", "it_IT"); // 이탈리아어

        m_locale_info.put( "id", "in_ID"); // 인도네시아어

        m_locale_info.put( "ja", "ja"); // 일본어
        m_locale_info.put( "ka", "ka_GE"); // 조지아어

        //m_locale_info.put( "zh", "zh_CN_#Hans"); // 중국어
        m_locale_info.put( "zh", "zh_CN"); // 중국어

        m_locale_info.put( "cs", "cs_CZ"); // 체코어
        m_locale_info.put( "ca", "ca_AD"); // 카탈로니아어
        m_locale_info.put( "kn", "kn_IN"); // 칸나다어
        m_locale_info.put( "hr", "hr_HR"); // 크로아티아어

        // tl_??
        //m_locale_info.put( "tl", "tl_??"); // 타칼로그어

        m_locale_info.put( "ta", "ta_IN"); // 타밀어
        m_locale_info.put( "th", "th_TH"); // 태국어
        m_locale_info.put( "tr", "tr_TR"); // 터키어
        m_locale_info.put( "te", "te_IN"); // 텔루구어
        m_locale_info.put( "fa", "fa_IR"); // 페르시아어
        m_locale_info.put( "pt", "pt_PT"); // 포르투갈어
        m_locale_info.put( "pl", "pl_PL"); // 폴란드어
        m_locale_info.put( "fr", "fr_FR"); // 프랑스어
        m_locale_info.put( "fi", "fi_FI"); // 핀란드어
        m_locale_info.put( "ko", "ko_KR"); // 한국어
        m_locale_info.put( "hu", "hu_HU"); // 헝가리어

        // he_?? or iw_IL ?
        m_locale_info.put( "he", "iw_IL"); // 히브리어

        m_locale_info.put( "hi", "hi_IN"); // 힌디어
    }

    fun get_locale_info(lang_code: String) : String {
        if ( m_locale_info == null ) {
            set_locale_info()
        }

        return "" + m_locale_info.get( lang_code )
    }

}