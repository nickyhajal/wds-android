package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.Visibility
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by nicky on 6/12/17.
 */
class DaySelector : LinearLayout {

    var selected: String = "2019-06-27"
    var mAdapter: Adapter;
    var onSelect: ((String) -> Unit)? = null

    constructor(context: Context) : super(context) {
        mAdapter = Adapter(Assets.days)
        create()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mAdapter = Adapter(Assets.days)
        create()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mAdapter = Adapter(Assets.days)
        create()
    }

    fun setDaysFromEvents(events: JSONArray) {
        val len = events.length()
        var days: ArrayList<String> = ArrayList<String>();
        for(i in 0..(len-1)) {
            val evDay: String = events.optJSONObject(i).optString("startDay", "");
            if (!days.contains(evDay) && evDay.isNotEmpty()) {
                days.add(evDay)
            }
        }
        mAdapter.update(days)
    }

    fun setSelectedDay(day: String) {
        selected = day
        mAdapter.notifyDataSetChanged()
    }

    fun create() {
        relativeLayout {
            lparams {
                width = matchParent
                height = matchParent
                centerVertically()
            }
            recyclerView {
                val orientation = LinearLayoutManager.HORIZONTAL
                val layout = LinearLayoutManager(context, orientation, true)
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

    class Holder(val cell: DaySelectorCell) : RecyclerView.ViewHolder(cell)

    inner class Adapter(val arrayList: ArrayList<String> = ArrayList<String>()) : RecyclerView.Adapter<Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder? {
            val cell: DaySelectorCell = DaySelectorCell(parent.context);
            cell.onSelect = { it: String -> setSelectedDay(it) }
            return Holder(cell)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val cell: DaySelectorCell= holder.cell
            val day = arrayList.get(position)
            cell.setDay(day)
            cell.setStatus(selected.compareTo(day) == 0)
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        fun setSelectedDay(day: String): Unit {
            selected = day;
            onSelect?.invoke(day)
            notifyDataSetChanged()
        }

        fun update(days: ArrayList<String>) {
            arrayList.clear();
            Collections.sort(days);
            Collections.reverse(days);
            arrayList.addAll(days);
            notifyDataSetChanged()
        }

        public fun push(text: String) {
            arrayList.add(0, text)
            notifyItemInserted(0)
        }

        public fun pop() {
            arrayList.remove(arrayList.last())
            notifyItemRemoved(arrayList.size)
        }

    }

    class DaySelectorCell : LinearLayout {

        val DAY_WEEK = 1
        val DAY_MONTH = 2
        val LAYOUT = 3
        val CIRCLE = 4
        var dayRaw = "2019-06-27T12:00:00.000"
        var onSelect: ((String) -> Unit)? = null
        lateinit var dayOfWeek: TextView
        lateinit var dayOfMonth: TextView
        lateinit var circle: Circle
        inline fun ViewManager.circleView() = circleView() {}

        inline fun ViewManager.circleView(init: Circle.() -> Unit): Circle {
            return ankoView({ Circle(it) }, 0, init)
        }
        constructor(context: Context) : super(context) {
            create()
        }
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            create()
        }
        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            create()
        }

        fun setDay(date: String) {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            dayRaw = date
            try {
                val tz = TimeZone.getDefault()
                val now = Date()
                val offsetFromUtc = tz.getOffset(now.time).toLong()
                val start = parser.parse("${date}T00:00:00.000Z").time
                val dOWFormatter = SimpleDateFormat("E")
                val dOMFormatter = SimpleDateFormat("dd")
                dayOfWeek.text = dOWFormatter.format(start)
                dayOfMonth.text = dOMFormatter.format(start)

            } catch (e: ParseException) {
                Log.e("WDS", "Parse Exception", e)
            }
        }
        fun setStatus(selected: Boolean) {
            circle.visibility = View.GONE
            if (selected) {
                circle.visibility = View.VISIBLE
                dayOfMonth.textColor = context.resources.getColor(R.color.bright_tan)
            } else {
                dayOfMonth.textColor = context.resources.getColor(R.color.gray)
            }
        }
        fun create() {
            relativeLayout {
                id = LAYOUT;
                isClickable
                onClick {
                    onSelect?.invoke(dayRaw)
                }
                lparams {
                    width = dip(55)
                    height = matchParent
                    centerInParent()
                }
                dayOfWeek = textView("Mon") {
                    id = DAY_WEEK
                    typeface = Font.use("Vitesse")
                    textColor = context.resources.getColor(R.color.gray)
                }.lparams {
                    topMargin = dip(6)
                    alignParentTop();
                    centerHorizontally()
                }
                circle = circleView {
                    id = CIRCLE
                    visibility = View.VISIBLE
                    backgroundColor = context.resources.getColor(R.color.bright_tan)
                }.lparams {
                    centerHorizontally()
                    topMargin = dip(0)
                    width = dip(30)
                    height = dip(30)
                    below(dayOfWeek)
                }
                dayOfMonth = textView("28") {
                    id = DAY_MONTH
                    typeface = Font.use("Vitesse")
                    textColor = context.resources.getColor(R.color.gray)
                }.lparams {
                    topMargin = dip(5)
                    below(dayOfWeek)
                    centerHorizontally()
                }
            }
        }
    }
    class Circle : View {
        val paint: Paint
        var circleColor: Int

        constructor(context: Context) : super(context) {
            paint = Paint()
            circleColor = context.resources.getColor(R.color.cyan)
            paint.color = circleColor
        }

        override fun onDraw(canvas: Canvas): Unit {
            canvas.drawColor(Color.TRANSPARENT)
            val radius: Float = (height/2).toFloat()
            val x = (width / 2)
            val y = (height / 2)
            canvas.drawCircle(x.toFloat(), y.toFloat(), radius, paint);
        }
    }
}