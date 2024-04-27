package com.whispercppdemo.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.whispercppdemo.R

class ListViewAdapter_DownloadTranslationModel(context: Context,
    items: ArrayList<ListViewItem_DownloadTranslationModel>) : BaseAdapter() {
    private val context: Context
    private val items: ArrayList<ListViewItem_DownloadTranslationModel>

    init {
        this.context = context
        this.items = items
    }

    override fun getItemId(position: Int): Long {
        //TODO("Not yet implemented")
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        //TODO("Not yet implemented")
        return items[position]
    }

    override fun getCount(): Int {
        //TODO("Not yet implemented")
        return items.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //TODO("Not yet implemented")

        var convertView: View? = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.listview_item_translation_model, parent, false)
        }

        val currentItem = getItem(position) as ListViewItem_DownloadTranslationModel

        val textview_locale = convertView
            ?.findViewById(R.id.TextView_Locale) as TextView
        val textview_locale_code = convertView
            ?.findViewById(R.id.TextView_LocaleCode) as TextView
        val textview_locale_code_all = convertView
            ?.findViewById(R.id.TextView_LocaleCodeAll) as TextView
        val textview_downloaded = convertView
            ?.findViewById(R.id.TextView_Downloaded) as TextView

        textview_locale.text = currentItem.item_locale
        textview_locale_code.text = currentItem.item_locale_code
        textview_locale_code_all.text = currentItem.item_locale_code_all
        textview_downloaded.text = currentItem.item_downloaded

        return convertView
    }
}