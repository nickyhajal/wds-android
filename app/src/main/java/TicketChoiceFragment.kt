package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.*

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Created by nicky on 08/07/16.
 */
class TicketChoiceFragment: Fragment() {
    var mView: ChoiceView? = null


    // Views
    var mListview: ListView? = null
    var mMessage: TextView? = null
    var mDoubleBtn: Button? = null
    var mSingleBtn: Button? = null
    var mDoubleSoldOut = false
    var mSingleSoldOut = false
    var mDoubleCard: LinearLayout? = null
    var mSingleCard: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onResume() {
        super.onResume()
        MainActivity.self.tabsFragment.showTabs()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView == null) {
            mView = ChoiceView(getContext())
            mDoubleBtn = mView!!.doubleBtn
            mSingleBtn = mView!!.singleBtn
            mDoubleCard = mView!!.doubleCard
            mSingleCard = mView!!.singleCard
        }
        syncUI()
        return mView
    }

    fun updateItems() {
        val pre = MainActivity.state["pre"] as HashMap<*, *>
        mDoubleSoldOut = pre["double_soldout"] as Long > 0
        mSingleSoldOut = pre["single_soldout"] as Long > 0
        syncUI()
    }

    fun syncUI() {
        if (mDoubleBtn != null) {
            mView!!.doubleSoldOut = mDoubleSoldOut
            mView!!.singleSoldOut = mSingleSoldOut
            if (mDoubleSoldOut) {
                mDoubleBtn!!.setText("Sold Out");
                mDoubleCard!!.alpha = 0.4f
            } else {
                mDoubleBtn!!.setText("Join us both years!");
                mDoubleCard!!.alpha = 1.0f
            }
            if (mSingleSoldOut) {
                mSingleBtn!!.setText("Sold Out");
                mSingleCard!!.alpha = 0.4f
            } else {
                mSingleBtn!!.setText("Join us next year!");
                mSingleCard!!.alpha = 1.0f
            }
        }

    }


    class ChoiceView : RelativeLayout {
        lateinit var doubleBtn: Button
        lateinit var singleBtn: Button
        lateinit var doubleCard: LinearLayout
        lateinit var singleCard: LinearLayout
        var doubleSoldOut: Boolean = false
        var singleSoldOut: Boolean = false

        constructor(context: Context) : super(context) {
            create()
        }
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            create()
        }
        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            create()
        }

        fun create() {
            verticalLayout {
                backgroundColor = Color.Tan()

                lparams {
                    gravity = Gravity.TOP
                    width = matchParent
                    height = matchParent
                    padding = dip(4)
                }
                scrollView {
                    verticalLayout {

                        textView {
                            textColor = Color.Blue()
                            typeface = Font.use("Vitesse_Medium")
                            backgroundColor = Color.Tan()
                            textSize = 24f
                            text = "Which is right for you?"
                            gravity = Gravity.CENTER
                        }.lparams {
                            topMargin = dip(40)
                            bottomMargin = dip(16)
                            height = wrapContent
                            width = matchParent
                        }

                        // Card
                        doubleCard = verticalLayout {
                            backgroundColor = Color.White()

                            verticalLayout {
                                padding = dip(18)
                                bottomPadding = 0
                                linearLayout {
                                    textView {
                                        text = "WDS 2019 & 2020"
                                        textColor = Color.Coffee()
                                        textSize = 22f
                                        typeface = Font.use("Vitesse_Medium")
                                    }.lparams {
                                        width = wrapContent
                                        height = wrapContent
                                        bottomMargin = dip(10)
                                    }
                                    textView {
                                        text = "$997"
                                        textColor = Color.BrightGreen()
                                        textSize = 22f
                                        typeface = Font.use("Vitesse_Medium")
                                        gravity = Gravity.RIGHT
                                    }.lparams {
                                        width = matchParent
                                        height = wrapContent
                                    }
                                }.lparams {
                                    height = wrapContent
                                    width = matchParent
                                }
                                textView {
                                    text = "A special offer to join us for the last 2 years of WDS at an incredible price. Just 200 tickets available."
                                    textColor = Color.DarkGray()
                                    textSize = 16f
                                    typeface = Font.use("Karla_Bold")
                                }.lparams {
                                    width = matchParent
                                    height = wrapContent
                                    bottomMargin = dip(10)
                                }
                                textView {
                                    text = "• WDS 2019 will be from June 25th to July 1st, 2019.\n" +
                                            "\n" +
                                            "• The dates for WDS 2020 will be between June-August 2020 and announced in mid-2019.\n" +
                                            "\n" +
                                            "• You'll assign your 2019 ticket(s) from an email confirmation after purchase. Your 2020 ticket(s) will be assigned before the end of 2019.\n" +
                                            "\n" +
                                            "• Your pre-order includes 1 free Insider Access Academy for 2019 & 2020\n" +
                                            "\n" +
                                            "• Tickets can be transferred for \$100 up to 30 days before the event (2019 & 2020).\n"
                                    textColor = Color.DarkGray()
                                    textSize = 14f
                                    typeface = Font.use("Karla")
                                }.lparams {
                                    width = matchParent
                                    height = wrapContent
                                    bottomMargin = dip(2)
                                }
                            }.lparams {
                                width = matchParent
                                height = wrapContent
                            }
                            doubleBtn = button {
                                backgroundColor = Color.Orange()
                                text = "Join us both years!"
                                typeface = Font.use("Vitesse_Bold")
                                textSize = 18f
                                textColor = Color.White()
                                onClick {
                                    if (!doubleSoldOut) {
                                        var prod: HashMap<String, String> = HashMap();
                                        prod.put("name", "WDS 2019 & 2020");
                                        prod.put("descr", "360 Ticket to WDS 2019");
                                        MainActivity.self.open_cart("wdsDouble", prod);
                                    }
                                }
                            }.lparams {
                                width = matchParent
                                height = wrapContent
                            }

                        }.lparams {
                            bottomMargin = dip(20)
                            width = matchParent
                            height = wrapContent
                        }

                        // Card
                        singleCard = verticalLayout {
                            backgroundColor = Color.White()

                            verticalLayout {
                                padding = dip(18)
                                bottomPadding = 0
                                linearLayout {
                                    textView {
                                        text = "WDS 2019"
                                        textColor = Color.Coffee()
                                        textSize = 22f
                                        typeface = Font.use("Vitesse_Medium")
                                    }.lparams {
                                        width = wrapContent
                                        height = wrapContent
                                        bottomMargin = dip(10)
                                    }
                                    textView {
                                        text = "$597"
                                        textColor = Color.BrightGreen()
                                        textSize = 22f
                                        typeface = Font.use("Vitesse_Medium")
                                        gravity = Gravity.RIGHT
                                    }.lparams {
                                        width = matchParent
                                        height = wrapContent
                                    }
                                }.lparams {
                                    height = wrapContent
                                    width = matchParent
                                }
                                textView {
                                    text = "Join us again next year! Pre-order now for a significantly discounted cost over the standard ticket."
                                    textColor = Color.DarkGray()
                                    textSize = 16f
                                    typeface = Font.use("Karla_Bold")
                                }.lparams {
                                    width = matchParent
                                    height = wrapContent
                                    bottomMargin = dip(10)
                                }
                                textView {
                                    text = "• WDS 2019 will be from June 25th to July 1st, 2019.\n" +
                                            "\n" +
                                            "• You'll assign your 2019 ticket(s) from an email confirmation after purchase.\n" +
                                            "\n" +
                                            "• Your pre-order includes 1 free Insider Access Academy\n" +
                                            "\n" +
                                            "• Tickets can be transferred for \$100 up to 30 days before the event.\n"
                                    textColor = Color.DarkGray()
                                    textSize = 14f
                                    typeface = Font.use("Karla")
                                }.lparams {
                                    width = matchParent
                                    height = wrapContent
                                    bottomMargin = dip(2)
                                }
                            }.lparams {
                                width = matchParent
                                height = wrapContent
                            }
                            singleBtn = button {
                                backgroundColor = Color.Orange()
                                text = "Join us next year!"
                                typeface = Font.use("Vitesse_Bold")
                                textSize = 18f
                                textColor = Color.White()
                                onClick {
                                    if (!singleSoldOut) {
                                        var prod: HashMap<String, String> = HashMap();
                                        prod.put("name", "WDS 2019");
                                        prod.put("descr", "360 Ticket to WDS 2019");
                                        MainActivity.self.open_cart("wds2019", prod);
                                    }
                                }
                            }.lparams {
                                width = matchParent
                                height = wrapContent
                            }

                        }.lparams {
                            bottomMargin = dip(20)
                            width = matchParent
                            height = wrapContent
                        }
                    }.lparams {
                        width = matchParent
                        height = wrapContent
                    }}.lparams {
                    width = matchParent
                    height = wrapContent
                }
            }
        }
    }
}
