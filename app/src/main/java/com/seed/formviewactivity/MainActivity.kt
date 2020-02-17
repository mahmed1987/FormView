package com.seed.formviewactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.seed.widgets.formview.FormView
import com.seed.widgets.formview.Pair
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity(), FormView.FormCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override suspend fun requestData(tag: String): List<Pair<String, String>> {
        return when (tag) {
            "countryDropDown" -> {
                delay(1000)
                listOf(Pair("1", "a"))
            }
            else -> listOf(Pair("a", "a"))
        }//To change body of created functions use File | Settings | File Templates.
    }

    override fun stitchedResult(tag: String) {
    }

}
