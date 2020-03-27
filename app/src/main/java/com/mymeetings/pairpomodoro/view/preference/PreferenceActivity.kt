package com.mymeetings.pairpomodoro.view.preference

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.mymeetings.pairpomodoro.R


class PreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        supportFragmentManager.beginTransaction().add(R.id.container, PreferenceFragment())
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Preferences"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun open(context: Context) {
            val intent = Intent(context, PreferenceActivity::class.java)
            context.startActivity(intent)
        }
    }
}