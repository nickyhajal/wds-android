package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.loopj.android.image.SmartImageView
import com.nostra13.universalimageloader.core.ImageLoader

import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap


/**
 * Created by nicky on 5/19/15.
 */
class AttendeeSearchAdapter(context: Context, users: ArrayList<HashMap<String, String>>) : ArrayAdapter<HashMap<String, String>>(context, 0, users) {
    var mImageLoader = ImageLoader.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        val user = Attendee.fromHashMap(getItem(position))
        convertView = LayoutInflater.from(context).inflate(R.layout.attendee_search_row, parent, false)
        holder = ViewHolder()
        holder.name = convertView!!.findViewById(R.id.name)
        holder!!.name!!.typeface = Font.use("Karla")
        holder.avatar = convertView.findViewById(R.id.avatar)
        convertView.tag = holder
        mImageLoader.displayImage(user.getPic(120), holder.avatar)
        holder!!.name!!.text = user.full_name
        convertView.setOnClickListener { MainActivity.self.open_profile_from_search(user) }
        return convertView
    }

    private inner class ViewHolder {
        var name: TextView? = null
        var avatar: ImageView? = null
    }

}

