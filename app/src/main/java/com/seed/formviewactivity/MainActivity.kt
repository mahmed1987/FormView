package com.seed.formviewactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
            "country" -> {
                delay(1000)
                listOf(
                    Pair("1", "United States"),
                    Pair("2", "United Kingdom"),
                    Pair("3", "Canada"),
                    Pair("4", "Mexico")
                )
            }
            else -> listOf(Pair("a", "a"))
        }//To change body of created functions use File | Settings | File Templates.
    }

    override fun stitchedResult(tag: String) {
        Toast.makeText(
            this, tag, Toast
                .LENGTH_LONG
        ).show()

    }

}
