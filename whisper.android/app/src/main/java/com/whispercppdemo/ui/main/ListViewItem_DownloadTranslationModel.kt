package com.whispercppdemo.ui.main

class ListViewItem_DownloadTranslationModel(
    val item_locale: String, val item_locale_code: String, val item_locale_code_all: String,
    val item_downloaded: String) {
    override fun toString(): String {
        //return super.toString()

        return this.item_locale
    }
}