package com.worlddominationsummit.wdsandroid

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.*
import com.nostra13.universalimageloader.core.ImageLoader
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.HashMap
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager


/**
 * Created by nicky on 7/11/17.
 */
class ChatNameDialog : DialogFragment() {

    var view: NamerView? = null
    var thisDialog: DialogFragment = this

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        if (view == null) {
            view = NamerView(activity.applicationContext)
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        view!!.focusOnInp()
    }



    inner class NamerView: LinearLayout {

        lateinit var input: EditText

        constructor(context: android.content.Context) : super(context) {
            create()
        }
        constructor(context: android.content.Context, attrs: android.util.AttributeSet) : super(context, attrs) {
            create()
        }
        constructor(context: android.content.Context, attrs: android.util.AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            create()
        }

        fun focusOnInp() {
            Handler().postDelayed({
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }, 140)
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

                    textView("Name Your Group!") {
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
                textView("Set a name for your group! You and everyone in it will see the name that you set.") {
                    textColor = Color.DarkGray()
                    textSize = 15f
                    typeface = Font.use("Karla")
                    backgroundColor = Color.White()
                }.lparams {
                    margin = dip(22)
                }
                input = editText {
                    textColor = Color.DarkGray()
                    hintTextColor = Color.Gray()
                    hint = "Set your group's name"
                    textSize = 15f
                    typeface = Font.use("Karla")
                }.lparams {
                    leftMargin = dip(22)
                    rightMargin = dip(22)
                    width = matchParent
                    height = dip(40)
                    padding = dip(8)
                }
                button("Let's Go!") {
                    backgroundColor = Color.Orange()
                    textColor = Color.White()
                    textSize = 15f
                    typeface = Font.use("Karla_Bold")
                    setAllCaps(false)
                    onClick {
                        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
                        MainActivity.self.chatEditFragment.setName(input.text.toString())
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