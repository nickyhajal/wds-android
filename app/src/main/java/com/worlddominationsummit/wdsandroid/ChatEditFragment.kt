package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.attendee_search_bar.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*

/**
 * Created by nicky on 7/8/17.
 */
class ChatEditFragment : Fragment() {
    var mView: ChatEditView? = null
    var mChatId: String? = null
    var mName: String? = null
    var selectedAtns = ArrayList<Attendee>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView == null) {
            mView = ChatEditView(getContext())
        }
        return mView as View
    }

    fun startChat() {
        if (selectedAtns.count() == 1) {
            MainActivity.self.open_chat(selectedAtns.get(0))
        } else if (mName == null) {
            val dialog = ChatNameDialog()
            dialog.show(MainActivity.self.fragmentManager, "chatnamedialog")
        }
    }

    fun setName(name: String) {
        // Check if no view has focus:
        if (mChatId == null) {
            mView!!.hideKb({
                MainActivity.self.open_chat(selectedAtns, name)
            })
        } else {
            mView!!.hideKb()
            mName = name
        }
    }

    fun setChat(chatId: String) {
        mChatId = chatId
    }

    fun clearChat() {
        mChatId = null
    }

    fun getTitle() : String {
        if (mChatId != null) {
            return "Chat Admin"
        } else {
            return "Start a Chat"
        }
    }

    fun toggleAttendee(atn: Attendee) {
        val tmp = ArrayList<Attendee>()
        var found = false
        selectedAtns.forEach {
            if (it.user_id == atn.user_id) {
                found = true
            } else {
                tmp.add(it)
            }
        }
        if (found) {
            selectedAtns = tmp
        } else {
            selectedAtns.add(atn)
        }
        mView!!.searchInput.setText("")
        mView!!.updateAttendees(selectedAtns)
        MainActivity.self.updateChatStartBtn(selectedAtns.count())
    }

    fun runSearch(q: String) {
        val results = Assets.searchAttendees(q)
        mView!!.updateAdapter(results)

    }

    inner class ChatEditView: LinearLayout {

        var mImageLoader: ImageLoader
        var chat: HashMap<String, Any>? = null
        lateinit var name: TextView
        lateinit var content: TextView
        lateinit var avatar: ImageView
        lateinit var searchInput: EditText
        lateinit var pList: AttendeeHorizontalList
        lateinit var searchList: ListView
//        inline fun ViewManager.attendeeHorizontalList () = AttendeeHorizontalList() {}

        inline fun ViewManager.attendeeHorizontalList (init: AttendeeHorizontalList.() -> Unit): AttendeeHorizontalList {
            return ankoView({ AttendeeHorizontalList(it) }, 0, init)
        }
        constructor(context: android.content.Context) : super(context) {
            create()
            mImageLoader = ImageLoader.getInstance()
        }
        constructor(context: android.content.Context, attrs: android.util.AttributeSet) : super(context, attrs) {
            create()
            mImageLoader = ImageLoader.getInstance()
        }
        constructor(context: android.content.Context, attrs: android.util.AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            create()
            mImageLoader = ImageLoader.getInstance()
        }

        fun updateAttendees(atns: ArrayList<Attendee>) {
            pList.setAttendees(atns)
        }
        fun updateAdapter() {
            searchList.adapter = SearchAdapter(getContext(), ArrayList<HashMap<String, String>>())
        }
        fun updateAdapter(list: ArrayList<HashMap<String, String>>) {
            if (list.isNotEmpty()) {
                searchList.adapter = SearchAdapter(getContext(), list)
            }
        }

        fun hideKb (onHide: (() -> Unit)? = null) {
            Handler().postDelayed({
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
                onHide?.invoke()
            }, 120)
        }
        fun create() {
            verticalLayout {
                lparams {
                    gravity = android.view.Gravity.TOP
                    backgroundColor = Color.DarkYellowTan()
                    width = matchParent
                    height = matchParent
                }
                pList = attendeeHorizontalList {
                    backgroundColor = Color.LightTan()
                }.lparams {
                    width = matchParent
                    height = dip(0)
                }
                searchInput = editText {
                    textColor = Color.DarkGray()
                    hintTextColor = Color.Gray()
                    backgroundColor = Color.White()
                    hint = "Search attendees to chat with"
                    textSize = 15f
                    typeface = Font.use("Karla")
                }.lparams {
                    width = matchParent
                    height = dip(40)
                    padding = dip(6)
                    topMargin = dip(3)
                    bottomMargin = dip(1)

                }
                searchList = listView {
                    dividerHeight = dip(0)
                }.lparams {
                    width = matchParent
                    height = matchParent
                    padding = 0
                    margin = 0
                }

            }
            searchInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    runSearch(s.toString())
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })
        }
    }

    inner class SearchAdapter(context: Context, users: ArrayList<HashMap<String, String>>) : ArrayAdapter<HashMap<String, String>>(context, 0, users) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val user = Attendee.fromHashMap(getItem(position))
            var convertedView: SearchRow? = null
            if (convertView != null) {
                convertedView = convertView as SearchRow
            }
            if (convertedView == null) {
                convertedView = SearchRow(context)
                convertedView.onSelect = { it: Attendee -> toggleAttendee(it) }
            }
            convertedView.setAttendee(user, position)
            return convertedView
        }

        private inner class ViewHolder {
            var name: TextView? = null
            var avatar: ImageView? = null
        }

    }

    inner class SearchRow: RelativeLayout {

        val AVATAR = 819821
        lateinit var atn: Attendee
        lateinit var name: TextView
        lateinit var avatar: ImageView
        lateinit var row: RelativeLayout
        var inx: Int = 0
        lateinit var status: ImageView
        var onSelect: ((Attendee) -> Unit)? = null
        var mImageLoader = ImageLoader.getInstance()

        inline fun ViewManager.attendeeHorizontalList (init: AttendeeHorizontalList.() -> Unit): AttendeeHorizontalList {
            return ankoView({ AttendeeHorizontalList(it) }, 0, init)
        }
        constructor(context: android.content.Context) : super(context) {
            create()
        }
        constructor(context: android.content.Context, attrs: android.util.AttributeSet) : super(context, attrs) {
            create()
        }
        constructor(context: android.content.Context, attrs: android.util.AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            create()
        }

        fun setAttendee(user: Attendee, position: Int) {
            avatar.setImageResource(R.drawable.default_avatar)
            mImageLoader.displayImage(user.getPic(120), avatar)
            name.text = user.full_name
            atn = user
            if (inSelected(user.user_id)) {
                status.setImageResource(R.drawable.checked_circle)
            } else {
                status.setImageResource(R.drawable.unchecked_circle)
            }

            inx = position
            row.setBackgroundColor(if (position % 2 > 0) {
               Color.DarkCanvas()
            } else {
                Color.WhiteCanvas()
            });

        }

        fun inSelected(user_id: String): Boolean {
            selectedAtns.forEach {
                if (it.user_id.equals(user_id)) {
                    return true
                }
            }
            return false
        }


        fun create() {
            row = relativeLayout {
                isClickable = true
                onClick {
                    if (atn != null) {
                        onSelect?.invoke(atn)
                        setAttendee(atn, inx)
                    }
                }
                lparams {
                    backgroundColor = Color.LightTan()
                    width = matchParent
                    height = dip(42)
                    padding = 0
                }
                avatar = imageView(R.drawable.default_avatar) {
                    id = AVATAR
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = true
                }.lparams {
                    width = dip(42)
                    height = matchParent
                    gravity = Gravity.LEFT
                    alignParentStart()
                    alignParentStart()
                }
                name = textView {
                    textColor = Color.DarkGray()
                    textSize = 16f
                    typeface = Font.use("Karla_Bold")
                    gravity = Gravity.CENTER_VERTICAL
                }.lparams {
                    centerVertically()
                    gravity = Gravity.CENTER_VERTICAL
                    rightOf(AVATAR)
                    leftMargin = dip(16)
                    width = matchParent
                    height = matchParent
                }
                status = imageView(R.drawable.unchecked_circle) {
                }.lparams {
                    gravity = Gravity.CENTER_VERTICAL
                    alignParentRight()
                    width = dip(30)
                    height = dip(30)
                    rightMargin = dip(8)
                    centerVertically()
                }
            }
        }
    }


}