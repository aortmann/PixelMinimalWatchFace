package com.benoitletondor.pixelminimalwatchfacecompanion.view.main

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import org.koin.android.viewmodel.ext.android.viewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.stateEventStream.observe(this, Observer { state ->
            when(state) {
                is MainViewModel.State.Loading -> {
                    main_activity_not_premium_view.visibility = View.GONE
                    main_activity_loading_view.visibility = View.VISIBLE
                    main_activity_premium_view.visibility = View.GONE
                    main_activity_syncing_view.visibility = View.GONE
                    main_activity_error_view.visibility = View.GONE
                }
                is MainViewModel.State.NotPremium -> {
                    main_activity_not_premium_view.visibility = View.VISIBLE
                    main_activity_loading_view.visibility = View.GONE
                    main_activity_premium_view.visibility = View.GONE
                    main_activity_syncing_view.visibility = View.GONE
                    main_activity_error_view.visibility = View.GONE
                }
                is MainViewModel.State.Syncing -> {
                    main_activity_not_premium_view.visibility = View.GONE
                    main_activity_loading_view.visibility = View.GONE
                    main_activity_premium_view.visibility = View.GONE
                    main_activity_syncing_view.visibility = View.VISIBLE
                    main_activity_error_view.visibility = View.GONE
                }
                is MainViewModel.State.Premium -> {
                    main_activity_not_premium_view.visibility = View.GONE
                    main_activity_loading_view.visibility = View.GONE
                    main_activity_premium_view.visibility = View.VISIBLE
                    main_activity_syncing_view.visibility = View.GONE
                    main_activity_error_view.visibility = View.GONE
                }
                is MainViewModel.State.Error -> {
                    main_activity_not_premium_view.visibility = View.GONE
                    main_activity_loading_view.visibility = View.GONE
                    main_activity_premium_view.visibility = View.GONE
                    main_activity_syncing_view.visibility = View.GONE
                    main_activity_error_view.visibility = View.VISIBLE
                    main_activity_error_view_text_2.text = getString(R.string.premium_error, state.error.message)
                }
            }
        })

        main_activity_not_premium_view_not_premium_view_pager.adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getItem(position: Int): Fragment = Fragment(when(position) {
                0 -> R.layout.fragment_premium_1
                1 -> R.layout.fragment_premium_2
                2 -> R.layout.fragment_premium_3
                else -> throw IllegalStateException("invalid position: $position")
            })

            override fun getCount(): Int = 3

        }

        main_activity_not_premium_view_not_premium_view_pager_indicator.setViewPager(main_activity_not_premium_view_not_premium_view_pager)

        viewModel.errorSyncingEvent.observe(this, Observer { syncingError ->
            AlertDialog.Builder(this)
                .setTitle(R.string.error_syncing_title)
                .setMessage(getString(R.string.error_syncing_message, syncingError.message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        })

        viewModel.errorPayingEvent.observe(this, Observer { paymentError ->
            AlertDialog.Builder(this)
                .setTitle(R.string.error_syncing_title)
                .setMessage(getString(R.string.error_syncing_message, paymentError.message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        })

        viewModel.syncSucceedEvent.observe(this, Observer {
            Toast.makeText(this, R.string.sync_succeed_message, Toast.LENGTH_LONG).show()
        })

        main_activity_error_view_retry_button.setOnClickListener {
            viewModel.retryPremiumStatusCheck()
        }

        main_activity_premium_view_sync_button.setOnClickListener {
            viewModel.triggerSync()
        }

        main_activity_not_premium_view_buy_button.setOnClickListener {
            viewModel.launchPremiumBuyFlow(this)
        }

        main_activity_not_premium_view_promocode_button.setOnClickListener {
            showRedeemVoucherUI()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        menu.findItem(R.id.copyright_button).title = getString(R.string.copyright, BuildConfig.VERSION_NAME)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if( item.itemId == R.id.send_feedback_button ) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SENDTO
            sendIntent.data = Uri.parse("mailto:") // only email apps should handle this
            sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(resources.getString(R.string.feedback_email)))
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.feedback_send_subject))

            if ( sendIntent.resolveActivity(packageManager) != null) {
                startActivity(sendIntent)
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showRedeemVoucherUI() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_redeem_voucher, null)
        val voucherEditText: EditText = dialogView.findViewById(R.id.voucher)

        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.voucher_redeem_dialog_title)
            .setMessage(R.string.voucher_redeem_dialog_message)
            .setView(dialogView)
            .setPositiveButton(R.string.voucher_redeem_dialog_cta) { dialog, _ ->
                dialog.dismiss()

                val voucher = voucherEditText.text.toString()
                if (voucher.trim { it <= ' ' }.isEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.voucher_redeem_error_dialog_title)
                        .setMessage(R.string.voucher_redeem_error_code_invalid_dialog_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()

                    return@setPositiveButton
                }

                if ( !launchRedeemVoucherFlow(voucher) ) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.iab_purchase_error_title)
                        .setMessage(R.string.iab_purchase_error_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)

        val dialog = builder.show()

        // Directly show keyboard when the dialog pops
        voucherEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // Check if the device doesn't have a physical keyboard
            if (hasFocus && resources.configuration.keyboard == Configuration.KEYBOARD_NOKEYS) {
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
    }

    private fun launchRedeemVoucherFlow(voucher: String): Boolean {
        return try {
            val url = "https://play.google.com/redeem?code=" + URLEncoder.encode(voucher, "UTF-8")
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            true
        } catch (e: Exception) {
            false
        }
    }
}
