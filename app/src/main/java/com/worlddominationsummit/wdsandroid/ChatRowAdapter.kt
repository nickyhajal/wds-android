package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.text.util.Linkify
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.android.volley.Response
import com.android.volley.VolleyError
import com.github.curioustechizen.ago.RelativeTimeTextView
import com.nostra13.universalimageloader.core.ImageLoader
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onClick

import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by nicky on 08/07/16.
 */
class ChatRowAdapter(context: Context, items: ArrayList<HashMap<String, Any>>) : ArrayAdapter<HashMap<String, Any>>(context, 0, items) {
    var isLoading: Boolean? = false
    var mContext: Context? = null

    init {
        mContext = context
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertedView: ChatRow? = null
        if (convertView != null) {
            convertedView = convertView as ChatRow
        }
        val item = getItem(position)
        if (convertedView == null) {
            convertedView = ChatRow(mContext as Context)
        }
        convertedView.setActiveChat(item)
        return convertedView
    }

    class ChatRow : LinearLayout {

        var mImageLoader: ImageLoader
        var chat: HashMap<String, Any>? = null
        lateinit var name: TextView
        lateinit var content: TextView
        lateinit var time: RelativeTimeTextView
        lateinit var avatar: ImageView
        inline fun ViewManager.relativeTimeView() = relativeTimeView() {}

        inline fun ViewManager.relativeTimeView (init: RelativeTimeTextView.() -> Unit): RelativeTimeTextView{
            return ankoView({ RelativeTimeTextView(it, null) }, 0, init)
        }
        constructor(context: Context) : super(context) {
            create()
            mImageLoader = ImageLoader.getInstance()
        }
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            create()
            mImageLoader = ImageLoader.getInstance()
        }
        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            create()
            mImageLoader = ImageLoader.getInstance()
        }

        fun setActiveChat(sentChat: HashMap<String, Any>): Unit {
            chat = sentChat
            mImageLoader.displayImage("https://avatar.wds.fm/" + chat!!["av_id"] + "?width=78", avatar!!)
            name!!.text = chat!!["with"] as String
            content!!.text = chat!!["lastMsg"] as String
            Linkify.addLinks(content, Linkify.WEB_URLS)
            time!!.setReferenceTime(chat!!["lastStamp"].toString().toLong())
        }

        fun create() {
            linearLayout {
                isClickable
                onClick {
                    if (chat!!.containsKey("group")) {
                        MainActivity.self.open_chat(chat!!["pid"].toString(), true, chat!!["with"].toString())
                    } else {
                        MainActivity.self.open_chat(chat!!["pid"].toString())
                    }
                }
                lparams {
                    gravity = Gravity.TOP
                    backgroundColor = Color.LightTan()
                    width = matchParent
                    height = wrapContent
                }
                avatar = imageView() {
                }.lparams {
                    width = dip(48)
                    height = dip(48)
                    gravity = Gravity.TOP
                    topMargin = dip(4)
                    leftMargin = dip(4)
                }
                verticalLayout {
                    backgroundColor = Color.LightTan()
                    padding = 0

                    name = textView {
                        ellipsize = TextUtils.TruncateAt.END
                        textSize = 14f
                        textColor = Color.Orange()
                        typeface = Font.use("Vitesse_Bold")
                    }.lparams {
                        width = matchParent
                        height = wrapContent
                        topMargin = dip(4)
                        rightPadding = dip(12)
//                        leftPadding = dip(12)
                    }
                    content = textView {
                        ellipsize = TextUtils.TruncateAt.END
                        textSize = 13f
                        textColor = Color.Coffee()
                        typeface = Font.use("Karla")
                    }.lparams {
                        width = matchParent
                        height = wrapContent
                        topMargin = 0
                        bottomMargin = dip(-1)
                    }
                    time = relativeTimeView {
                        textColor = Color.DarkGray()
                        alpha = 0.5f
                        textSize = 12f
                        typeface = Font.use("Karla")

                        lparams {
                            width = wrapContent
                            height = wrapContent
                            bottomMargin = dip(8)
                        }
                    }

                }.lparams {
                    width = matchParent
                    height = wrapContent
                    leftMargin = dip(6)
                    bottomMargin = 0
                    rightMargin = 0
                    topMargin = 0
                }
            }
        }
    }

}

