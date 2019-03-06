package com.jacquessmuts.positivitea

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jacquessmuts.positivitea.database.TeaService
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {

    override val kodein by org.kodein.di.android.kodein()

    val teaService: TeaService by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        teaService.getTeabags()

    }

}
