package com.worlddominationsummit.wdsandroid

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * Created by nicky on 7/15/17.
 */

class TermsDialog : DialogFragment() {

    var view: TermView? = null
    var thisDialog: DialogFragment = this

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        if (view == null) {
            view = TermView(activity.applicationContext)
        }

        view!!.setTerms(MainActivity.self.cartFragment.mProdTerms)

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
    }



    inner class TermView: LinearLayout {

        lateinit var title: TextView
        lateinit var termStr: TextView

        constructor(context: android.content.Context) : super(context) {
            create()
        }
        constructor(context: android.content.Context, attrs: android.util.AttributeSet) : super(context, attrs) {
            create()
        }
        constructor(context: android.content.Context, attrs: android.util.AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            create()
        }

        fun setTerms(str: String) {
            termStr.text = str
        }


        fun create() {
            verticalLayout {
                backgroundColor = Color.White()
                lparams {
                    width = matchParent
                    height = wrapContent
                    padding = 0
                    margin = dip(-8)
                }
                linearLayout {
                    backgroundColor = Color.Blue()

                    textView("Ticket Terms") {
                        textColor = Color.White()
                        backgroundColor = Color.Blue()
                        typeface = Font.use("Vitesse_Bold")
                        textSize = 18f
                        gravity = Gravity.CENTER
                    }.lparams {
                        padding = dip(14)
                        width = matchParent
                        height = wrapContent
                    }
                }.lparams {
                    padding = dip(14)
                    width = matchParent
                    height = wrapContent
                }
                termStr = textView("") {
                    textColor = Color.DarkGray()
                    textSize = 15f
                    typeface = Font.use("Karla")
                    backgroundColor = Color.White()
                }.lparams {
                    margin = dip(22)
                }
                button("Got it!") {
                    backgroundColor = Color.Orange()
                    textColor = Color.White()
                    textSize = 17f
                    typeface = Font.use("Karla_Bold")
                    setAllCaps(false)
                    onClick {
                        thisDialog.dismiss()
                    }
                }.lparams {
                    width = matchParent
                    height = wrapContent
                    padding = dip(6)
                    topMargin = dip(22)
                }
            }
        }
    }
}