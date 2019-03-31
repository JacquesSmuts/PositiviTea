package com.jacquessmuts.positivitea.activity

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.jacquessmuts.positivitea.R
import com.jacquessmuts.positivitea.service.TeaRepository
import com.jacquessmuts.positivitea.util.subscribeAndLogE
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.content_add.*
import org.kodein.di.generic.instance
import java.util.concurrent.TimeUnit

class AddActivity : BaseActivity() {

    private val teaRepository: TeaRepository by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()

        rxSubs.add(RxView.clicks(button)
            .throttleFirst(1, TimeUnit.SECONDS)
            .filter { validateInputs() }
            .subscribeAndLogE {
                progress.visibility = View.VISIBLE
                teaRepository.saveUnapprovedTeaBagToServer(editTitle.text.toString(),
                    editMessage.text.toString()) { success ->
                    progress.visibility = View.GONE

                    val message = getString(if (success) {
                        R.string.add_success
                    } else {
                        R.string.add_failure
                    })
                    val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
                    toast.show()

                    if (success) {
                        Handler().postDelayed({
                            onBackPressed()
                        }, 3000)
                    }
                }
            })
    }

    private fun validateInputs(): Boolean {
        val valid = editTitle.text.toString().isNotBlank() && editMessage.text.toString().isNotBlank()

        if (!valid) {
            val toast = Toast.makeText(this, "no", Toast.LENGTH_LONG)
            toast.show()
        }

        return valid
    }
}
