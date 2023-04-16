package com.ooober.user.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.ooober.user.R
import android.widget.Spinner
import java.util.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        var op = 0
        //get current language
        val currentLocale = resources.configuration.locales.toString()

        //debug test
        Log.d("current LANG", currentLocale)

        if(currentLocale.contains("en"))
            op = 1

        //config options for the language select spinner
        val languageSelector: Spinner = findViewById(R.id.spin_langSelect)
        val options = arrayOf(resources.getString(R.string.lang_es), resources.getString(R.string.lang_en))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSelector.adapter = adapter

        languageSelector.setSelection(op)

        //set listener to change language on ap
        languageSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val langOption = if (position == 0) "es" else "en"

                // create a Locale object for the desired locale
                val locale = Locale(langOption)

                // set the app's locale to the desired locale
                Locale.setDefault(locale)

                // create a configuration object and set the app's locale to it
                val config = baseContext.resources.configuration
                config.setLocale(locale)

                // update the app's configuration with the new locale
                baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

                //debug test
                Log.d("new LANG", resources.configuration.locales.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
}