package com.example.menu_app.activities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.menu_app.R
import com.example.menu_app.data.Address
import com.example.menu_app.data.CartManager
import com.example.menu_app.repository.AddressRepository
import com.example.menu_app.repository.OrderRepository
import com.example.menu_app.utils.Money
import com.example.menu_app.utils.PriceEstimator
import com.example.menu_app.utils.Validators
import com.example.menu_app.utils.applySystemBarInsets
import java.util.UUID

class CheckoutActivity : AppCompatActivity() {

    private val addressRepository = AddressRepository()
    private val orderRepository = OrderRepository()

    private var savedAddress: Address? = null
    private var clientRequestId: String = UUID.randomUUID().toString()
    private var placing = false

    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPincode: EditText
    private lateinit var etState: EditText
    private lateinit var etCity: EditText
    private lateinit var etHouse: EditText
    private lateinit var etRoadArea: EditText
    private lateinit var etDeliveryInstructions: EditText
    private lateinit var etOrderNotes: EditText
    private lateinit var tvOrderTotal: TextView
    private lateinit var btnSaveNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)
        applySystemBarInsets()
        CartManager.init(applicationContext)

        // Keep the same request id if the screen is recreated, so a retry can never create two orders.
        savedInstanceState?.getString("client_request_id")?.let { clientRequestId = it }

        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etPincode = findViewById(R.id.etPincode)
        etState = findViewById(R.id.etState)
        etCity = findViewById(R.id.etCity)
        etHouse = findViewById(R.id.etHouse)
        etRoadArea = findViewById(R.id.etRoadArea)
        etDeliveryInstructions = findViewById(R.id.etDeliveryInstructions)
        etOrderNotes = findViewById(R.id.etOrderNotes)
        tvOrderTotal = findViewById(R.id.tvOrderTotal)
        btnSaveNext = findViewById(R.id.btnSaveNext)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnEditAddress).setOnClickListener { fillFormFromSaved() }
        btnSaveNext.setOnClickListener { onSaveAndNext() }

        if (CartManager.getItems().none { it.available }) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadSavedAddress()
        loadServerTotal()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("client_request_id", clientRequestId)
    }



    private fun loadSavedAddress() {
        val card = findViewById<View>(R.id.cardSavedAddress)
        card.visibility = View.GONE
        addressRepository.list { result ->
            result.onSuccess { list ->
                val first = list.firstOrNull() ?: return@onSuccess
                savedAddress = first
                findViewById<TextView>(R.id.tvAddressLabel).text = first.label
                findViewById<TextView>(R.id.tvAddressPhone).text = first.phone
                findViewById<TextView>(R.id.tvAddressLine).text =
                    "${first.fullName}\n${first.summary()}"
                card.visibility = View.VISIBLE
            }
            // On failure we simply show the empty form.
        }
    }

    /** The bottom total comes from the SERVER. The local estimate is only a placeholder. */
    private fun loadServerTotal() {
        val items = CartManager.getItems()
        tvOrderTotal.text = Money.format(PriceEstimator.estimate(items).totalCents) + " (est.)"
        orderRepository.quote(items) { result ->
            result.onSuccess { quote ->
                tvOrderTotal.text = Money.format(quote.totals.totalCents)
            }.onFailure {
                Toast.makeText(this, OrderRepository.describe(it), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fillFormFromSaved() {
        val a = savedAddress ?: return
        etFullName.setText(a.fullName)
        etPhone.setText(a.phone)
        etPincode.setText(a.pincode)
        etState.setText(a.state)
        etCity.setText(a.city)
        etHouse.setText(a.house)
        etRoadArea.setText(a.roadArea)
        etFullName.requestFocus()
    }

    // ------------------------------------------------------------ submit

    private fun onSaveAndNext() {
        if (placing) return

        val address: Address = if (formIsEmpty() && (savedAddress != null)) {
            savedAddress!!
        } else {
            readAndValidateForm() ?: return
        }

        val items = CartManager.getItems()
        if (items.any { !it.available }) {
            Toast.makeText(this, "Remove unavailable items from your cart first", Toast.LENGTH_LONG).show()
            return
        }

        setPlacing(value = true)
        orderRepository.placeOrder(
            items = items,
            address = address,
            deliveryInstructions = etDeliveryInstructions.text.toString().trim(),
            orderNotes = etOrderNotes.text.toString().trim(),
            clientRequestId = clientRequestId,
        ) { result ->
            result.onSuccess { order ->
                val summary = items.joinToString(", ") { "${it.quantity} x ${it.name}" }
                // Remember a newly typed address for next time.
                if (address.id.isBlank()) addressRepository.save(address) { }
                CartManager.clear()
                com.example.menu_app.model.CartManager.clearCart()
                clientRequestId = UUID.randomUUID().toString()
                OrderConfirmationActivity.start(this, order, summary, address.summary())
                finish()
            }.onFailure {
                setPlacing(value = false)
                Toast.makeText(this, OrderRepository.describe(it), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setPlacing(value: Boolean) {
        placing = value
        btnSaveNext.isEnabled = !value
        btnSaveNext.alpha = if (value) 0.5f else 1f
        btnSaveNext.text = if (value) "Placing order..." else "Save and Next"
    }

    private fun formIsEmpty(): Boolean = listOf(
        etFullName, etPhone, etPincode, etState, etCity, etHouse, etRoadArea,
    ).all { it.text.toString().trim().isEmpty() }

    private fun readAndValidateForm(): Address? {
        val name = etFullName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val pincode = etPincode.text.toString().trim()
        val state = etState.text.toString().trim()
        val city = etCity.text.toString().trim()
        val house = etHouse.text.toString().trim()
        val road = etRoadArea.text.toString().trim()

        var firstError: EditText? = null
        fun check(field: EditText, ok: Boolean, message: String) {
            if (!ok) {
                field.error = message
                if (firstError == null) firstError = field
            }
        }
        check(etFullName, name.length >= 2, "Enter your full name")
        check(etPhone, Validators.isValidPhone(phone), "Enter a valid phone number")
        check(etPincode, Validators.isValidPincode(pincode), "Enter a valid pincode")
        check(etState, state.isNotEmpty(), "Required")
        check(etCity, city.isNotEmpty(), "Required")
        check(etHouse, house.isNotEmpty(), "Required")
        check(etRoadArea, road.isNotEmpty(), "Enter road, area or building")

        firstError?.let {
            it.requestFocus()
            return null
        }
        return Address(
            fullName = name, phone = phone, pincode = pincode,
            state = state, city = city, house = house, roadArea = road
        )
    }
}
