package com.example.menu_app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.menu_app.R
import com.example.menu_app.data.PlaceOrderResult
import com.example.menu_app.utils.Money
import com.example.menu_app.utils.applySystemBarInsets

class OrderConfirmationActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ORDER_ID = "order_id"
        private const val EXTRA_TOTAL_CENTS = "total_cents"
        private const val EXTRA_ETA = "eta_minutes"
        private const val EXTRA_ITEMS = "items_summary"
        private const val EXTRA_ADDRESS = "address_summary"

        fun start(context: Context, order: PlaceOrderResult, itemsSummary: String, addressSummary: String) {
            context.startActivity(
                Intent(context, OrderConfirmationActivity::class.java)
                    .putExtra(EXTRA_ORDER_ID, order.orderId)
                    .putExtra(EXTRA_TOTAL_CENTS, order.totals.totalCents)
                    .putExtra(EXTRA_ETA, order.etaMinutes)
                    .putExtra(EXTRA_ITEMS, itemsSummary)
                    .putExtra(EXTRA_ADDRESS, addressSummary)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order_confirmation)
        applySystemBarInsets()

        val eta = intent.getIntExtra(EXTRA_ETA, 0)
        findViewById<TextView>(R.id.tvOrderId).text =
            "Order ID: #${intent.getStringExtra(EXTRA_ORDER_ID).orEmpty()}"
        findViewById<TextView>(R.id.tvConfirmItems).text = intent.getStringExtra(EXTRA_ITEMS).orEmpty()
        findViewById<TextView>(R.id.tvConfirmAddress).text =
            "Delivering to: ${intent.getStringExtra(EXTRA_ADDRESS).orEmpty()}"
        findViewById<TextView>(R.id.tvConfirmEta).text =
            if (eta > 0) "Estimated delivery: about $eta minutes" else ""
        findViewById<TextView>(R.id.tvConfirmTotal).text =
            Money.format(intent.getLongExtra(EXTRA_TOTAL_CENTS, 0L))

        findViewById<Button>(R.id.btnViewOrders).setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
            finish()
        }
        findViewById<Button>(R.id.btnBackHome).setOnClickListener { goHome() }

        // Back should never return to the finished checkout.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })
    }


    private fun goHome() {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launch)
        }
        finish()
    }
}
