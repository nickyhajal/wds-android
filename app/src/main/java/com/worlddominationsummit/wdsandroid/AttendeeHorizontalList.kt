package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader;
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by nicky on 7/8/17.
 */
class AttendeeHorizontalList : LinearLayout {

    var mAdapter: Adapter;
    var onSelect: ((String) -> Unit)? = null
    val theOrientation = LinearLayoutManager.HORIZONTAL
    val layout = LinearLayoutManager(context, theOrientation, true)

    constructor(context: Context) : super(context) {
        mAdapter = Adapter(ArrayList<Attendee>())
        create()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mAdapter = Adapter(ArrayList<Attendee>())
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mAdapter = Adapter(ArrayList<Attendee>())
        create()
    }
    fun setAttendees(atns: ArrayList<Attendee>) {
        if (mAdapter == null) {
            mAdapter = Adapter(atns)
        } else {
            mAdapter.update(atns)
        }
        if (atns.isEmpty()) {
            resizeWithAnimation(this, 150, 0)
        } else {
            resizeWithAnimation(this, 150, dip(62))
        }
    }

    fun create() {
        relativeLayout {

            backgroundColor = Color.YellowCanvas()

            lparams {
                width = matchParent
                height = matchParent
                topPadding = dip(7)
                centerVertically()
            }
            recyclerView {
                layout.reverseLayout = true
                layout.stackFromEnd = true
                layoutManager = layout
                overScrollMode = View.OVER_SCROLL_ALWAYS
                adapter = mAdapter
            }
                    .lparams {
                        width = matchParent
                        height = matchParent
                        centerVertically()
                        alignParentLeft()
                    }
        }
    }

    private fun resizeWithAnimation(view: View, duration: Int, targetHeight: Int): Animation {
        val initialHeight = view.measuredHeight
        val distance = targetHeight - initialHeight

        val a = object : Animation() {

            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f && targetHeight == 0) {
                } else {
                    view.layoutParams.height = (initialHeight + distance * interpolatedTime).toInt()
                    view.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        a.duration = duration.toLong()
        view.startAnimation(a)
        return a
    }

    class Holder(val cell: Cell) : RecyclerView.ViewHolder(cell)

    inner class Adapter(val arrayList: ArrayList<Attendee> = ArrayList<Attendee>()) : RecyclerView.Adapter<Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder? {
            val cell: Cell = Cell(parent.context);

            return Holder(cell)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val cell: Cell= holder.cell
            val atn = arrayList.get(position)
            cell.setAtn(atn)
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        fun update(atns: ArrayList<Attendee>) {
            arrayList.clear();
            arrayList.addAll(atns);
            notifyDataSetChanged()
            layout.scrollToPosition(arrayList.count() - 1)
        }

    }

    class Cell: LinearLayout {

        val IMAGE = 198128
        val NAME = 91281
        var user_id: String? = ""
        var nameView: TextView? = null
        var avatarView: ImageView? = null
        var mImageLoader: ImageLoader = ImageLoader.getInstance()
        var onSelect: ((String) -> Unit)? = null
        constructor(context: Context) : super(context) {
            create()
        }
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            create()
        }
        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            create()
        }

        fun setAtn(atn: Attendee) {
            user_id = atn.user_id
            var firstName = atn.first_name
            mImageLoader.displayImage(atn.getPic(120), avatarView)
            Handler().postDelayed({
                mImageLoader.displayImage(atn.getPic(120), avatarView)
                nameView!!.text = "${firstName} ${atn.last_name[0]}."
            }, 170)
            nameView!!.text = "${firstName} ${atn.last_name[0]}."
        }

        fun create() {
            relativeLayout {
                isClickable
                onClick {
                    onSelect?.invoke(user_id!!)
                }
                lparams {
                    width = dip(68)
                    height = matchParent
                    centerInParent()
                }
                nameView = textView {
                    id = NAME
                    typeface = Font.use("Karla")
                    textSize = 13f
                    textColor = context.resources.getColor(R.color.gray)
                    singleLine = true
                    ellipsize = TextUtils.TruncateAt.MIDDLE
                }.lparams {
                    topMargin = dip(4)
                    leftPadding = dip(3)
                    rightPadding = dip(3)
                    centerHorizontally()
                    below(IMAGE)
                }
                avatarView = imageView{
                    id = IMAGE
                    visibility = View.VISIBLE
                    backgroundColor = context.resources.getColor(R.color.bright_tan)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = true
                }.lparams {
                    centerHorizontally()
                    alignParentTop();
                    topMargin = dip(4)
                    width = dip(30)
                    height = dip(30)
                }
            }
        }
    }
}