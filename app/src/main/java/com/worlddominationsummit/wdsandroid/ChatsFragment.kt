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
class ChatsFragment : Fragment() {
    var mView: ChatsView? = null
    lateinit var mAdapter: ChatRowAdapter
    lateinit var mChats: ArrayList<HashMap<String, Any>>
    var mListener: ChildEventListener? = null
    var mState = "loading"
    var processed = 0
    lateinit var mLastReads: HashMap<String, Long>
    var mWatchingKeys: ArrayList<String>? = null
    val mChatsHandler = Handler()
    val mChatListHandler = Handler()
    lateinit var mListeners: ArrayList<Any>
    val mChatsRunnable = {
        val saveArray = JSONArray()
        mChats.forEach {
            try {
                val str = (JSONObject(it).toString())
                saveArray.put(str)
            } catch (e: JSONException) {

            }
        }
        Store.set("chats", saveArray.toString())
    }
    val mChatListRunnable = {
        updateChatList()
    }

    // Views
    var mListview: ListView? = null
    var mMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mChats = ArrayList<HashMap<String, Any>>()
        mLastReads = HashMap<String, Long>()
        mListeners = ArrayList<Any>()
        var processed = false
        val ord = HashMap<String, String>()
        ord.put("type", "orderKey")
        val query = ArrayList<HashMap<String, String>>()
        query.add(ord)
        val cached = Store.getArray("chats") as ArrayList<String>
        if (cached.isNotEmpty()) {
            cached.forEach {
                try {
                    val obj: JSONObject = JSONObject(it)
                    val chat = JsonHelper.toMap(obj) as HashMap<String, Any>
                    mChats.add(chat)
                    if (mState !== "loaded") {
                        setState("loaded")
                    }
                } catch (e: JSONException) {
//                    Puts.i(e.toString())
                    Puts.i("Json Error parsing chats")
                }
            }
            if (mChats.isNotEmpty()) {
                updateChatList()
            }
        }

        Fire.querySingle("/chats_by_user/" + Me.atn.user_id, query, object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount < 1) {
                    setState("null")
                }
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    setState("loaded")
                    for (c in dataSnapshot.children) {
                        watchChat(c.key.toString(), java.lang.Long.parseLong(c.value.toString()))
                    }
                } else {
                    setState("null")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Puts.i(databaseError.toString())
            }
        })

        mListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
                    watchChat(dataSnapshot.key.toString(), java.lang.Long.parseLong(dataSnapshot.value.toString()))
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        Fire.watch("/chats_by_user/" + Me.atn.user_id, mListener)
    }

    override fun onResume() {
        super.onResume()
        MainActivity.self.tabsFragment.showTabs()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView == null) {
            mView = ChatsView(getContext())
            mListview = mView!!.list
            mMessage = mView!!.msg
            mAdapter = ChatRowAdapter(activity, mChats)
            mListview!!.adapter = mAdapter
            syncState()
        }
        return mView
    }

    fun watchChat(chatId: String, last_read: Long) {
//        Puts.i(">>> CHAT ID: $chatId")
        mLastReads.put(chatId, last_read)
        val readListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    mLastReads.put(chatId, java.lang.Long.parseLong(dataSnapshot.value.toString()))
                    updateChatList()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        val chatListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    updateChat(dataSnapshot.key.toString(), dataSnapshot.value as HashMap<String, Any>?)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

        Fire.watch("/chats_by_user/" + Me.atn.user_id + "/" + chatId, readListener)
        Fire.watch("/chats/" + chatId, chatListener)
        mListeners.add(readListener)
        mListeners.add(chatListener)
    }

    fun setState(state: String) {
        mState = state
        syncState()
    }

    fun syncState() {
        if (mMessage != null) {
            when (mState) {
                "loading" -> {
                    mMessage!!.text = "Loading..."
                    mMessage!!.visibility = View.VISIBLE
                    mListview!!.visibility = View.GONE
                }
                "null" -> {
                    mMessage!!.text = "Your messages will appear here."
                    mMessage!!.visibility = View.VISIBLE
                    mListview!!.visibility = View.GONE
                }
                "loaded" -> {
                    mMessage!!.visibility = View.GONE
                    mListview!!.visibility = View.VISIBLE
                }
            }
        }
    }

    fun processReads() {
        if (mChats.size > 0) {
            var i: Int
            i = 0
            while (i < mChats.size) {
                val chat = mChats[i]
                val lastStamp = chat["lastStamp"].toString().toLong()
                var lastRead: Long = 0
                if (mLastReads.containsKey(chat["id"])) {
                    lastRead = mLastReads[chat["id"]]!!
                    lastRead += 500
                }
                val read = lastRead >= lastStamp
                chat.put("read", read)
                mChats[i] = chat
                i++
            }
        }
    }

    fun updateChatList() {
        if (mChats.size > 1) {
            Collections.sort<HashMap<String, Any>>(mChats) { h1, h2 ->
                val h1: String = h1["lastStamp"].toString()
                val h2: String = h2["lastStamp"].toString()
                h2.compareTo(h1)
            }
        }
        processReads()
        updateItems()
    }

    fun updateChatListDebounced() {
        mChatListHandler.removeCallbacksAndMessages(null)
        mChatListHandler.postDelayed(mChatListRunnable, 100)
    }

    fun updateItems() {
        if (this.activity != null) {
            mAdapter = ChatRowAdapter(this.activity, mChats)
            if (mListview != null) {
                mListview!!.adapter = mAdapter
            }
        } else {
            Handler().postDelayed({ updateItems() }, 1000)
        }
    }

    fun updateChat(key: String, chat: HashMap<String, Any>?, save: Boolean = true) {
        val with = ArrayList<String>()
        var withStr = ""
        var av_id = ""
        var lastStamp: Long = 0
        var lastMsgContent = ""

        if (chat != null && chat["participants"] != null && chat["last_msg"] != null) {
            val lastMsg = chat["last_msg"] as HashMap<String, Any>

            var pre = ""
            for (p in chat["participants"] as ArrayList<HashMap<*, *>>) {
                if (p["user_id"] === lastMsg["user_id"]) {
                    pre = p["first_name"].toString() + ": "
                }
                if (p["user_id"].toString() != Me.atn.user_id) {
                    with.add(p["first_name"].toString() + " " + p["last_name"])
                    if (av_id.length < 1) {
                        av_id = p["user_id"].toString()
                    }
                }
            }
            if (chat["name"] != null) {
                withStr = chat!!["name"].toString()
            } else {
                withStr = TextUtils.join(", ", with)
            }
            if (withStr.length > 65) {
                withStr = withStr.substring(0, 65) + "..."
            }
            if (lastMsg["user_id"].toString() == Me.atn.user_id) {
                pre = "You: "
            } else {
                av_id = lastMsg["user_id"].toString()
            }
            lastMsgContent = pre + lastMsg["msg"]
            if (lastMsgContent.length > 95) {
                lastMsgContent = lastMsgContent.substring(0, 95) + "..."
            }
            lastStamp = lastMsg["created_at"].toString().toLong()
            val c = HashMap<String, Any>()
            c.put("id", key)
            if (chat["name"] != null) {
                c.put("pid", key)
                c.put("group", "true")
            } else {
                c.put("pid", chat!!["pid"]!!)
            }
            c.put("lastMsg", lastMsgContent)
            c.put("lastStamp", lastStamp)
            c.put("with", withStr)
            c.put("av_id", av_id)
            if (mChats.size > 0) {
                var found = -1
                var i: Int
                i = 0
                while (i < mChats.size) {
                    if (mChats[i]["id"]!!.toString().compareTo(key) == 0) {
                        found = i
                    }
                    i++
                }
                if (found > -1) {
                    mChats[found] = c
                } else {
                    mChats.add(c)
                }
            } else {
                mChats.add(c)
            }
            if (save) {
                saveChatsDebounced()
            }
            updateChatListDebounced()
        }
    }

    fun saveChatsDebounced() {
        mChatsHandler.removeCallbacksAndMessages(null)
        mChatsHandler.postDelayed(mChatsRunnable, 400)
    }


    class ChatsView : RelativeLayout {

        lateinit var msg: TextView
        lateinit var list: ListView
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
            relativeLayout {
                backgroundColor = Color.Tan()

                lparams {
                    gravity = Gravity.TOP
                    width = matchParent
                    height = matchParent
                    padding = 0
                }
                msg = textView {
                    backgroundColor = Color.Tan()
                    typeface = Font.use("Vitesse_Medium")
                    backgroundColor = Color.Tan()
                    textSize = 16f
                    gravity = Gravity.CENTER
                }.lparams {
                    alignParentTop()
                    alignParentLeft()
                    gravity = Gravity.CENTER_VERTICAL
                    width
                }
                list = listView {
                    backgroundColor = Color.Tan()
//                    divider = Color.Tan()
                    dividerHeight = dip(1)
                }.lparams {
                    alignParentTop()
                    alignParentLeft()
                    width = matchParent
                    height = wrapContent
                    leftMargin = 0
                    rightMargin = 0
                }
                relativeLayout {
//                    imageView(R.drawable.floating_shadow) {
//                    }.lparams {
//                        width = matchParent
//                        height = matchParent
//                        alignParentBottom()
//                        alignParentLeft()
//                    }
                    linearLayout {
                        backgroundDrawable = context.resources.getDrawable(R.drawable.rounded_shadow)
                    }.lparams {
                        width = matchParent
                        height = matchParent
                        alignParentBottom()
                        alignParentLeft()
                    }
                    button {
                        backgroundDrawable = context.resources.getDrawable(R.drawable.left_button_rounded)
                        onClick {
                            MainActivity.self.open_chat_edit()
                        }

                    }.lparams {
                        width = dip(57)
                        height = dip(57)
                        alignParentTop()
                        alignParentLeft()
                    }
                    button {
                        backgroundDrawable = context.resources.getDrawable(R.drawable.right_button_rounded)
                        onClick {
                            MainActivity.self.open_chat(Me.atn.user_id+"_support")
                        }

                    }.lparams {
                        width = dip(57)
                        height = dip(57)
                        alignParentTop()
                        alignParentRight()
                    }
                    imageView(R.drawable.plus) {
                    }.lparams {
                        width = dip(14)
                        height = dip(14)
                        alignParentLeft()
                        alignParentTop()
                        leftMargin = dip(24)
                        topMargin = dip(22)
                    }
                    imageView(R.drawable.help) {
                    }.lparams {
                        width = dip(18)
                        height = dip(18)
                        alignParentRight()
                        alignParentTop()
                        rightMargin = dip(24)
                        topMargin = dip(20)
                    }
                } .lparams {
                    width = dip(114)
                    height = dip(90)
                    alignParentBottom()
                    alignParentRight()
                    rightMargin = dip(16)
                    bottomMargin = dip(16)
                }
            }
        }
    }
}
