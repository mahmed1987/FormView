package com.seed.formviewactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.seed.widgets.formview.FormView
import com.seed.widgets.formview.Pair
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay

class FormViewFragment :  Fragment(),FormView.FormCallbacks{
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_formview,container,false)
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
            context, tag, Toast
                .LENGTH_LONG
        ).show()

    }
}