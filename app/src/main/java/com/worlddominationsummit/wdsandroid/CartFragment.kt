package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.support.annotation.BoolRes
import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import info.hoang8f.android.segmented.SegmentedGroup

import com.android.volley.*
import com.android.volley.Response
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import com.stripe.android.exception.AuthenticationException
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.HashMap
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard

/**
 * Created by nicky on 5/18/15.
 */
class CartFragment : Fragment() {
    var mView: View? = null
    lateinit var mName: TextView
    lateinit var mDescr: TextView
    lateinit var mPrice: TextView
    lateinit var mFee: TextView
    lateinit var mCardDetails: TextView
    lateinit var mScanCard: Button
    lateinit var mToggleCard: Button
    lateinit var mExpiration: Button
    lateinit var mSubmit: Button
    lateinit var mQ1: Button
    lateinit var mQ2: Button
    lateinit var mQ3: Button
    lateinit var mQBtns: ArrayList<Button>
    var mTermView: TextView? = null
    var mProdPrice: Int = 0
    var mProdTerms: String = ""
    var mProdFee: Int = 0
    lateinit var mCardNum: EditText
    lateinit var mCardCVV: EditText
    lateinit var mQuantityShell: LinearLayout
    var mQuantityPicker: SegmentedGroup? = null
    var mQuantity: Int = 0
    lateinit var mExistingCardShell: LinearLayout
    lateinit var mNewCardShell: LinearLayout
    var mCode: String? = null
    var mConfirm: Boolean? = false
    var mExpMonth = 0
    var mExpYear = 0
    var mProd: HashMap<String, String>? = null
    var mSyncCard: Boolean = false
    var cardInitd = false
    var mCardExists = false
    var mUseExistingCard = true
    lateinit var mPurchData: JSONObject
    var mCharging = false

    fun setProduct(code: String, prod: HashMap<String, String>) {
        clearTerms()
        mCode = code
        mProd = prod
    }

    fun setTerms(terms: String) {
        mProdTerms = terms;
        if (mTermView != null) {
            if (mProdTerms.isNotEmpty()) {
                mTermView!!.visibility = View.VISIBLE
//                mTermView!!.text = mProdTerms
            } else {
//                mTermView!!.text = ""
                mTermView!!.visibility = View.GONE
            }
        }
    }
    fun clearTerms() {
        setTerms("")
    }

    fun updateCart() {
        if (mCode != null && mProd != null) {
            if (mCode == "academy" || mCode == "event") {
                mName.text = mProd!!["name"]
                mDescr.text = mProd!!["descr"]
                mPrice.text = mProd!!["price"]
                if (!mPrice.text.contains(("$"))) {
                    mPrice.text = "$"+mPrice.text;
                }
                mFee.visibility = View.GONE
                mQuantityShell.visibility = View.GONE
                mConfirm = false
                mPurchData = JSONObject()
                try {
                    mPurchData.put("event_id", mProd!!["event_id"])
                } catch (e: JSONException) {
                    Log.e("WDS", "Json Exception", e)
                }

            } else if (mCode == "wds2019") {
                mName.text = mProd!!["name"]
                mDescr.text = mProd!!["descr"]
                mQuantity = 1
                mProdPrice = 597
                mProdFee = 10
                mFee.visibility = View.VISIBLE
                mQuantityShell.visibility = View.VISIBLE
                mPurchData = JSONObject()
                updateQuantity()
                mConfirm = true
                try {
                    mPurchData.put("event_id", mProd!!["event_id"])
                } catch (e: JSONException) {
                    Log.e("WDS", "Json Exception", e)
                }

            }  else if (mCode == "wdsDouble") {
                mName.text = mProd!!["name"]
                mDescr.text = mProd!!["descr"]
                mQuantity = 1
                mProdPrice = 997
                mProdFee = 10
                mFee.visibility = View.VISIBLE
                mQuantityShell.visibility = View.VISIBLE
                mPurchData = JSONObject()
                updateQuantity()
                mConfirm = true
                try {
                    mPurchData.put("event_id", mProd!!["event_id"])
                } catch (e: JSONException) {
                    Log.e("WDS", "Json Exception", e)
                }


            }
        }

    }

    override fun onStart() {
        super.onStart()
        updateCart()
        updateCard()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView == null) {
            mQBtns = ArrayList<Button>()
            mView = inflater!!.inflate(R.layout.cart, container, false)
            val content = mView!!.findViewById(R.id.content) as LinearLayout
            mName = mView!!.findViewById(R.id.name) as TextView
            mDescr = mView!!.findViewById(R.id.descr) as TextView
            mPrice = mView!!.findViewById(R.id.price) as TextView
            mFee = mView!!.findViewById(R.id.fee) as TextView
            mCardDetails = mView!!.findViewById(R.id.existing_details) as TextView
            mCardNum = mView!!.findViewById(R.id.cc_number) as EditText
            mTermView = mView!!.findViewById(R.id.terms) as TextView
            mCardCVV = mView!!.findViewById(R.id.cc_cvv) as EditText
            mScanCard = mView!!.findViewById(R.id.scanCard) as Button
            mToggleCard = mView!!.findViewById(R.id.toggleCard) as Button
            mExpiration = mView!!.findViewById(R.id.cc_exp_open) as Button
            mQ1 = mView!!.findViewById(R.id.q1) as Button
            mQ2 = mView!!.findViewById(R.id.q2) as Button
            mQ3 = mView!!.findViewById(R.id.q3) as Button
            mQ1.typeface = Font.use("Karla_Bold")
            mQ2.typeface = Font.use("Karla_Bold")
            mQ3.typeface = Font.use("Karla_Bold")
            mQBtns.add(mQ1)
            mQBtns.add(mQ2)
            mQBtns.add(mQ3)

            mSubmit = mView!!.findViewById(R.id.submit) as Button
            mExistingCardShell = mView!!.findViewById(R.id.existingCardShell) as LinearLayout
            mNewCardShell = mView!!.findViewById(R.id.newCardShell) as LinearLayout
            mQuantityShell = mView!!.findViewById(R.id.quantityShell) as LinearLayout
            Font.applyTo(content)
            mScanCard.setOnClickListener { startCardScan() }
            mToggleCard.setOnClickListener { toggleCard() }
            mExpiration.setOnClickListener { showExpirationPicker() }
            mSubmit.setOnClickListener {
                if (mConfirm!!) {
                    val dialog = PurchConfirmDialog()
                    dialog.set(total, mQuantity)
                    dialog.show(MainActivity.self.fragmentManager, "rsvpdialog")
                } else {
                    startPurchaseProcess()
                }
            }
            val qBtn = View.OnClickListener { view ->
                for (b in mQBtns) {
                    if (b == view) {
                        b.setBackgroundColor(MainActivity.self.resources.getColor(R.color.dark_gray))
                        b.setTextColor(MainActivity.self.resources.getColor(R.color.light_tan))
                        mQuantity = Integer.valueOf(view.tag as String)!!
                        updateQuantity()
                    } else {
                        b.setBackgroundColor(MainActivity.self.resources.getColor(R.color.tan))
                        b.setTextColor(MainActivity.self.resources.getColor(R.color.dark_gray))
                    }
                }
            }
            mTermView!!.setOnClickListener {
                val termsDialog = TermsDialog()
                termsDialog.show(MainActivity.self.fragmentManager, "termdialog");
            }
            mQ1.setOnClickListener(qBtn)
            mQ2.setOnClickListener(qBtn)
            mQ3.setOnClickListener(qBtn)

            if (mProdTerms.isNotEmpty()) {
                mTermView!!.visibility = View.VISIBLE
//                mTermView!!.text = mProdTerms
            } else {
//                mTermView!!.text = ""
                mTermView!!.visibility = View.GONE
            }
        }
        return mView
    }

    val total: Float
        get() = (mProdPrice * mQuantity + mProdFee * mQuantity).toFloat()

    fun updateQuantity() {
        mPrice.text = "$" + (mProdPrice * mQuantity).toString()
        mFee.text = "+$" + java.lang.Float.valueOf((mProdFee * mQuantity).toFloat()).toString() + "0"
        try {
            mPurchData.put("quantity", mQuantity)
        } catch (e: JSONException) {
            Log.e("WDS", "Json Exception", e)
        }

    }

    fun startPurchaseProcess() {
        if (mCharging) {
            return
        }
        mCharging = true
        mSubmit.text = "Authorizing..."
        if (mUseExistingCard) {
            chargeCard(Me.atn.card.optString("hash"))
        } else {
            val card = Card(mCardNum.text.toString(), mExpMonth, mExpYear, mCardCVV.text.toString())
            var errBody = ""
            if (!card.validateNumber()) {
                errBody = "Hey, it seems like your credit card number isn't valid. Can you double-check it?"
            } else if (!card.validateExpiryDate()) {
                errBody = "Hmm, it looks like your credit card's expiration date isn't valid. Can you double-check it?"
            } else if (!card.validateCVC()) {
                errBody = "Hmm, it looks like your CVV isn't valid. Can you double-check it?"
            }
            if (errBody.length > 0) {
                showError(errBody)
            } else {
                try {
                    // pk_test_8WKTIWKXB6T1eFT9sFqrymCM
                    val stripe = Stripe(this.context, "pk_live_v32iH6nfQOgPmKgQiNOrnZCi")
//                    val stripe = Stripe(this.context, "pk_test_8WKTIWKXB6T1eFT9sFqrymCM")
                    stripe.createToken(card,
                            object : TokenCallback {
                                override fun onSuccess(token: Token) {
                                    chargeCard(token.id)
                                }

                                override fun onError(error: Exception) {
                                    val err = error.localizedMessage
                                    val b = "Hm, it looks like there was a problem: \n\n" + err
                                    showError(b)
                                }
                            }
                    )
                } catch (e: Exception) {
                    Log.e("WDS", "Json Exception", e)
                }

            }
        }
    }

    fun chargeCard(token: String) {
        val params = JSONObject()
        try {
            params.put("card_id", token)
            params.put("code", mCode)
            params.put("via", "and")
            params.put("purchase_data", mPurchData)
        } catch (e: JSONException) {
            Log.e("WDS", "Json Exception", e)
        }

        Api.post("product/charge", params, { response ->
            if (response.has("err") || !response.has("fire") || response.has("declined")) {
                showError()
            } else {
                val path = "/sales/$mCode/${response.optString("fire")}"
                var nullCounter = 0;
                Fire.watch(path, object: ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        println("loadPost:onCancelled ${databaseError.toException()}")
                    }
                    override fun onDataChange(changed: DataSnapshot) {

                        val value = changed.toString();
                        if (changed.getValue() != null) {
                            val data: Map<String, Object> = changed.getValue() as Map<String, Object>
                            if (data != null) {
                                val status = data.get("status").toString()
                                if (status.compareTo("done") == 0) {
                                    finishPurchase()
                                } else if (status.compareTo("pre-process") == 0) {
                                    mSubmit.text = "Processing..."
                                } else if (status.compareTo("stripe-charge") == 0) {
                                    mSubmit.text = "Charging..."
                                } else if (status.compareTo("error") == 0) {
                                    val error = data.get("error").toString()
                                    if (error.compareTo("Your card was declined.") == 0) {
                                        showError("Hm, it looks like your card was declined for some reason.\n\nCan you double-check and try again or try another card?")
                                    } else {
                                        showError()
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }) { showError() }
    }

    fun finishPurchase() {
        mSubmit.text = "Success!"
        if (mCode == "academy" || mCode == "event") {
            Me.addRsvp(mProd!!["event_id"])
            if (MainActivity.self.eventsFragment != null && MainActivity.self.eventsFragment === MainActivity.self.active) {
                MainActivity.self.eventsFragment.update_items()
            }
            if (MainActivity.self.eventFragment != null && MainActivity.self.eventFragment === MainActivity.self.active) {
                MainActivity.self.eventFragment.updateRsvpButton()
            }
        } else if (mCode.equals("wds2019") || mCode.equals("wdsDouble")) {
            Store.set("preorder19", "purchased")
            MainActivity.self.homeFragment.update_items()
        }
        Handler().postDelayed({
            mCharging = false
            fragmentManager.popBackStackImmediate()
            mSubmit.text = "Purchase"
            if (!mCode.equals("wds2019") && !mCode.equals("wdsDouble")) {
                MainActivity.self.open_events()
            }
            syncCard()
        }, 2500)
    }

    fun syncCard() {
        Me.sync({
            cardInitd = false
            updateCard()
        }) { }
    }

    private fun updateCard() {
        if (cardInitd) {
            return
        }
        cardInitd = true
        if (Me.atn.card != null && Me.atn.card.has("last4")) {
            val c = Me.atn.card
            mExistingCardShell.visibility = View.VISIBLE
            mNewCardShell.visibility = View.GONE
            mCardDetails.text = c.optString("brand") + " ending in " + c.optString("last4") + "\n" + "Exp: " + c.optString("exp_month") + "/" + c.optString("exp_year")
            mToggleCard.visibility = View.VISIBLE
            mToggleCard.text = "Use New Card"
            mCardExists = true
            mUseExistingCard = true
        } else {
            mExistingCardShell.visibility = View.GONE
            mNewCardShell.visibility = View.VISIBLE
            mToggleCard.visibility = View.GONE
            mCardExists = false
            mUseExistingCard = false
        }
    }

    private fun toggleCard() {
        if (mUseExistingCard) {
            mUseExistingCard = false
            mExistingCardShell.visibility = View.GONE
            mNewCardShell.visibility = View.VISIBLE
            mToggleCard.text = "Use Existing Card"
        } else {
            mUseExistingCard = true
            mExistingCardShell.visibility = View.VISIBLE
            mNewCardShell.visibility = View.GONE
            mToggleCard.text = "Use New Card"
        }
    }

    private fun startCardScan() {
        val scanIntent = Intent(MainActivity.self, CardIOActivity::class.java)

        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true) // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true) // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true) // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false) // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, true) // default: false

        // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
        MainActivity.self.startActivityForResult(scanIntent, MainActivity.self.SCAN_REQUEST_CODE)
    }

    fun setCard(num: String, cvv: String, m: Int, y: Int) {
        setExpiration(m - 1, y)
        mCardCVV.setText(cvv)
        mCardNum.setText(num)
    }

    fun setExpiration(month: Int, year: Int) {
        mExpMonth = month + 1
        mExpYear = year
        var m = mExpMonth.toString()
        if (m.length == 1) {
            m = "0" + m
        }
        val str = "Exp: $m/$mExpYear"
        mExpiration.text = str
        mExpiration.setTextColor(resources.getColor(R.color.blue))
    }

    fun showExpirationPicker() {
        val newFragment = ExpirationPicker()
        if (mExpYear > 0) {
            newFragment.setDefs(mExpMonth - 1, mExpYear)
        }
        newFragment.show(MainActivity.self.supportFragmentManager, "datePicker")
    }

    @JvmOverloads fun showError(errBody: String = "Looks like there was a problem. Can you try again? If you continue to have trouble, please try another card.") {
        val dialog = ContentDialog()
        dialog.setContent("There Was a Problem", errBody, "Got it!")
        dialog.show(MainActivity.self.fragmentManager, "carterrordialog")
        mSubmit.text = "There Was a Problem"
        mCharging = false
        Handler().postDelayed({ mSubmit.text = "Purchase" }, 4000)
    }
}
