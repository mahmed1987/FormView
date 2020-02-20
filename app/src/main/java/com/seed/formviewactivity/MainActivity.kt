package com.seed.formviewactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val newFragment= FormViewFragment()
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.add(R.id.container, newFragment).commit()

    }


//    override suspend fun requestData(tag: String): List<Pair<String, String>> {
//        return when (tag) {
//            "country" -> {
//                delay(1000)
//                listOf(
//                    Pair("1", "United States"),
//                    Pair("2", "United Kingdom"),
//                    Pair("3", "Canada"),
//                    Pair("4", "Mexico")
//                )
//            }
//            else -> listOf(Pair("a", "a"))
//        }//To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun stitchedResult(tag: String) {
//        Toast.makeText(
//            this, tag, Toast
//                .LENGTH_LONG
//        ).show()
//
//    }

}
