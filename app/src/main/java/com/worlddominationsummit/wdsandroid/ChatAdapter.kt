package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.os.Handler
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.joooonho.SelectableRoundedImageView

import com.android.volley.Response
import com.android.volley.VolleyError
import com.github.curioustechizen.ago.RelativeTimeTextView
import com.nostra13.universalimageloader.core.ImageLoader

import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap


/**
 * Created by nicky on 08/07/16.
 */
class ChatAdapter(context: Context, items: ArrayList<HashMap<String, Any>>) : ArrayAdapter<HashMap<String, Any>>(context, 0, items) {
    var mImageLoader: ImageLoader
    var isLoading: Boolean? = false
    var mContext: ChatFragment? = null

    init {
        mImageLoader = ImageLoader.getInstance()
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (item!!["user_id"].toString() == Me.atn.user_id) {
            return 1
        } else {
            return 0
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val item = getItem(position)
        val viewType = getItemViewType(position)
        if (viewType == 1) {
            val holder: ViewHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.chat_msg_me, parent, false)
                holder = buildHolder(convertView)
                convertView!!.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            syncHolder(holder, item)
            return convertView
        } else {
            val holder: ViewHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.chat_msg, parent, false)
                holder = buildHolder(convertView)
                convertView!!.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            syncHolder(holder, item)
            return convertView
        }
    }

    fun buildHolder(convertView: View): ViewHolder {
        val holder = ViewHolder()
        holder.card = convertView.findViewById(R.id.card) as? LinearLayout
        holder.name = convertView.findViewById(R.id.name) as? TextView
        holder.name!!.typeface = Font.use("Karla_Bold")
        holder.timestamp = convertView.findViewById(R.id.timestamp) as? RelativeTimeTextView
        holder.timestamp!!.typeface = Font.use("Karla_Italic")
        holder.avatar = convertView.findViewById(R.id.avatar) as? SelectableRoundedImageView
        holder.content = convertView.findViewById(R.id.content) as? TextView
        holder.content!!.typeface = Font.use("Karla")
        holder.avatar!!.scaleType = ImageView.ScaleType.CENTER_CROP
        holder.avatar!!.setCornerRadiiDP(19f, 19f, 19f, 19f)
        holder.avatar!!.setBorderWidthDP(0f)
        return holder
    }

    fun syncHolder(holder: ViewHolder, item: HashMap<String, Any>) {
        mImageLoader.displayImage("https://avatar.wds.fm/" + item["user_id"] + "?width=78", holder.avatar!!)
        holder.name!!.text = item["author"] as String
        holder.content!!.text = item["msg"] as String
        Linkify.addLinks(holder.content, Linkify.WEB_URLS)
        holder.timestamp!!.setReferenceTime((item["created_at"].toString().toLong())- 10000)
    }

    inner class ViewHolder {
        var name: TextView? = null
        var channel: TextView? = null
        var card: LinearLayout? = null
        var buttons: LinearLayout? = null
        var timestamp: RelativeTimeTextView? = null
        var avatar: SelectableRoundedImageView? = null
        var content: TextView? = null
    }

}

