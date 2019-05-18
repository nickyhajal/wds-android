package com.worlddominationsummit.wdsandroid

import android.os.Handler
import android.renderscript.Sampler
import android.text.TextUtils
import android.util.Log
import android.view.View

import com.android.volley.*
import com.android.volley.Response
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.concurrent.RunnableFuture

/**
 * Created by nicky on 8/6/16.
 */
class Chat {
    var mCreating = false
    var mReady = false
    var mParticipants: ArrayList<HashMap<*, *>>
    var mAdmins: ArrayList<String>? = null
    private val mReadies = ArrayList<Runnable>()
    lateinit var mMeta: HashMap<String, Any>
    var last_read: String? = null
    var mListener: ChildEventListener? = null
    var mTypingListener: ValueEventListener? = null
    var mLastRead: Long = 0
    var mChatId: String? = null
    var mPid: String? = null
    var mName: String? = null
    var mGroup: Boolean = false
    var mMsgs = ArrayList<HashMap<String, Any>>()

    constructor(participants: ArrayList<Attendee>) {
        mParticipants = ArrayList<HashMap<*, *>>()
        for (p in participants) {
            addParticipant(p)
        }
        createIfNotExists()
    }

    constructor(participants: ArrayList<Attendee>, isGroup: Boolean, name: String) {
        mGroup = isGroup
        mParticipants = ArrayList<HashMap<*, *>>()
        for (p in participants) {
            addParticipant(p)
        }
        mName = name
        createIfNotExists()
    }

    constructor(p_id: String) {
        mParticipants = ArrayList<HashMap<*, *>>()
        mPid = p_id
        createIfNotExists()
    }

    constructor(p_id: String, isGroup: Boolean, name: String) {
        mParticipants = ArrayList<HashMap<*, *>>()
        mPid = p_id
        mChatId = mPid
        mGroup = isGroup
        mName = name
        createIfNotExists()
    }

    fun init() {

    }

    fun whenReady(r: Runnable) {
        if (mReady) {
            r.run()
        } else {
            mReadies.add(r)
        }
    }

    fun createIfNotExists() {
        val metaListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    mMeta = dataSnapshot.value as HashMap<String, Any>
                    var ps: ArrayList<HashMap<*, *>> = ArrayList()
                    if (mMeta["participants"] != null) {
                        ps = mMeta["participants"] as ArrayList<HashMap<*, *>>;
                    }

                    for (p in ps) {
                        var found = false
                        for (existing in mParticipants) {
                            if (existing.get("user_id") == p.get("user_id")) {
                                found = true
                            }
                        }
                        if (!found) {
                            mParticipants.add(p)
                        }
                    }
                    mReady = true
                    if (mReadies.size > 0) {
                        for (r in mReadies) {
                            r.run()
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        val existingListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    // This is weird but with a group, you pass the ChatID
                    // so you know it exists and the value at this path is the
                    // chat object so we don't want to set it to the ChatID
                    if (!mGroup) {
                        mChatId = dataSnapshot.value as String?
                    }
                } else {
                    create()
                }
                initLastRead()
                Fire.get("/chats/" + mChatId, metaListener)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        var path = if (mGroup) {
            "/chats/" + (if (mPid == null) { "DOESNOTEXISTBUCKO" } else { mPid })
        } else {
            "/chat_participants/$mPid"
        }
        Fire.get(path, existingListener)
    }

    fun create() {
        if (!mCreating) {
            mCreating = true
            mChatId = Fire.createAt("/chats")
            if (mGroup) {
                mPid = mChatId!!
            }
            Fire.set("/chat_participants/" + mPid, mChatId)
            set("last_msg", HashMap<String, Any>())
            set("participants", mParticipants)
            set("pid", mPid!!)
            if (mGroup) {
                val admins = ArrayList<Int>()
                admins.add(Me.atn.user_id.toInt())
                set("name", mName!!)
                set("creator", Me.atn.user_id.toInt())
                set("admins", admins)
            }
            for (p in mParticipants) {
                Fire.set("/chats_by_user/" + p["user_id"] + "/" + mChatId, "1")
            }
        }
    }

    operator fun set(key: String, `val`: Any) {
        Fire.set("/chats/$mChatId/$key", `val`)
    }

    fun initLastRead() {
        Fire.get("/chats_by_user" + Me.atn.user_id + "/" + mChatId, object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    mLastRead = (dataSnapshot.value as Int).toLong()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    fun addParticipantAndUpdate(atn: Attendee) {
        addParticipant(atn)
        set("participants", mParticipants)
    }
    fun addParticipantToFire(atn: Attendee) {
        if (atn.user_id != null) {
            Fire.set("/chats_by_user${atn.user_id}/$mChatId", "1")
        }
    }
    fun addParticipant(atn: String) {
        val msgAtn = HashMap<String, Any>()
        msgAtn.put("first_name", "WDS")
        msgAtn.put("last_name", "Team")
        msgAtn.put("user_id", "support")
        if (!mParticipants.contains(msgAtn)) {
            mParticipants.add(msgAtn)
        }
        if (!mGroup) {
            val ids = ArrayList<String>()
            Collections.sort(mParticipants) { a1, a2 -> (a1["user_id"].toString().compareTo(a2["user_id"].toString())) }
            for (p in mParticipants) {
                ids.add(p.get("user_id").toString())
            }
            mPid = TextUtils.join("_", ids)
        }
    }
    fun addParticipant(atn: Attendee) {
        val msgAtn = HashMap<String, Any>()
        msgAtn.put("first_name", atn.first_name)
        msgAtn.put("last_name", atn.last_name)
        msgAtn.put("user_id", atn.user_id)
        if (!mParticipants.contains(msgAtn)) {
            mParticipants.add(msgAtn)
        }
        if (!mGroup) {
            val ids = ArrayList<String>()
            Collections.sort(mParticipants) { a1, a2 -> (a1["user_id"].toString().compareTo(a2["user_id"].toString())) }
            for (p in mParticipants) {
                ids.add(p.get("user_id").toString())
            }
            mPid = TextUtils.join("_", ids)
        }
    }

    fun now(): Long {
        return Calendar.getInstance().timeInMillis
    }

    fun send(msg: String) {
        val from = Me.atn.user_id
        val msg_id = Fire.createAt("/chat_messages/" + mChatId!!)
        val now = now()
        val msgObj = HashMap<String, Any>()
        msgObj.put("msg", msg)
        msgObj.put("user_id", from.toString().toLong())
        msgObj.put("created_at", now.toString().toLong())
        Fire.set("/chat_messages/$mChatId/$msg_id", msgObj)
        Fire.set("/chats_by_user/" + Me.atn.user_id + "/" + mChatId, now)
        set("last_msg", msgObj)
        var summary = msg
        if (summary.length > 100) {
            summary = summary.substring(0, 100) + "..."
        }
        val to = JSONArray()
        for (p in mParticipants) {
            if (p["user_id"].toString() != Me.atn.user_id) {
                to.put(p["user_id"].toString())
            }
        }
        val post = JSONObject()
        try {
            if (mPid!!.contains("support")) {
                val user = JSONObject()
                user.put("first_name", Me.atn.first_name)
                user.put("last_name", Me.atn.last_name)
                user.put("user_id", Me.atn.user_id)
                post.put("url", "https://concierge.wds.fm/")
                post.put("user", user)
                post.put("message", msg);
                Api.post("message/toSlack", post, { }) { }

            } else {
                post.put("chat_id", mPid)
                post.put("user_id", to)
                post.put("summary", summary)
                Api.post("message", post, { }) { }
            }
        } catch (e: JSONException) {
            Log.e("WDS", "Json Exception", e)
        }

    }

    fun watch() {
        if (mChatId != null && mChatId!!.length > 0 && mListener == null) {
            val now = Calendar.getInstance().timeInMillis + 200
            val ord = HashMap<String, String>()
            ord.put("type", "orderChild")
            ord.put("val", "created_at")
            val small = HashMap<String, String>()
            small.put("type", "limitLast")
            small.put("val", "15")
            val all = HashMap<String, String>()
            all.put("type", "limitLast")
            all.put("val", "500")
            val start = HashMap<String, String>()
            start.put("type", "startChildAt")
            start.put("child", "created_at")
            start.put("val", now.toString())
            val smallQ = ArrayList<HashMap<String, String>>()
            smallQ.add(small)
            val allQ = ArrayList<HashMap<String, String>>()
            allQ.add(all)
            val startQ = ArrayList<HashMap<String, String>>()
            startQ.add(ord)
            startQ.add(start)
//            val cached = Store.getJsonArray("chat-$mChatId")
//            if (cached != null && cached.length() > 0) {
//                val len = cached.length()
//                for (i in 0..len - 1) {
//                    val msg: JSONObject = cached.optJSONObject(i)
//                    addMsg(JsonHelper.toMap(msg) as HashMap<String, Any>)
//                }
//            }
//            Fire.querySingle("/chat_messages/" + mChatId!!, smallQ, object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
//                        for (child in dataSnapshot.children) {
//                            addMsg(child.value as HashMap<String, Any>)
//                        }
//                    }
//                    drawMessages()
//                }
//
//                override fun onCancelled(databaseError: DatabaseError) {
//
//                }
//            })
//            Handler().postDelayed({
//                Fire.querySingle("/chat_messages/" + mChatId!!, allQ, object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
//                            clear()
//                            for (child in dataSnapshot.children) {
//                                addMsg(child.value as HashMap<String, Any>)
//                            }
//                        }
//                        drawMessages()
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {
//
//                    }
//                })
//            }, 3000)
            mListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    if (dataSnapshot.exists()) {
                        addMsg(dataSnapshot.value as HashMap<String, Any>)
                    }
                    drawMessages()
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
            Fire.query("/chat_messages/" + mChatId!!, startQ, mListener)
        }
    }
    fun addMsg(msg: HashMap<String, Any>) {
        val last = mMsgs.size - 1
        var author = ""

        // Me getting added twice to participants?
        for (p in mParticipants) {
            if (p["user_id"].toString() == msg["user_id"].toString()) {
                if (p["first_name"] != null) {
                    author += p["first_name"]
                }
                if (p["last_name"] != null) {
                    author += " " + (p["last_name"] as String).substring(0, 1)
                }
            }
        }
        msg.put("author", author)
        val last_msg: HashMap<String, Any>
        if (mMsgs.size < 1) {
            msg.put("created_at", (msg["created_at"].toString().toLong()) + 4000)
            mMsgs.add(msg)
        } else {
            last_msg = mMsgs[last]
            val msgCreated = msg["created_at"].toString().toLong()
            val lastMsgCreated = (last_msg["created_at"].toString().toLong())
            val diff = (msgCreated - lastMsgCreated) / 1000
            if (last_msg["user_id"].toString() == msg["user_id"].toString() && diff < 120) {
                var text = last_msg["msg"] as String
                text += "\n\n" + msg["msg"]
                last_msg.put("created_at", msg!!["created_at"]!!)
                last_msg.put("msg", text)
                mMsgs[last] = last_msg
            } else {
                mMsgs.add(msg)
            }
        }
        if (mLastRead < msg["created_at"].toString().toLong()) {
            mLastRead = now()
            Fire.set("/chats_by_user/" + Me.atn.user_id + "/" + mChatId, mLastRead)
        }
    }

    fun setTyping(isTyping: Boolean) {
        if (isTyping) {
            val now = now() / 1000
            Fire.set("/chats/" + mChatId + "/typing/" + Me.atn.user_id, now)
        } else {
            Fire.remove("/chats/" + mChatId + "/typing/" + Me.atn.user_id)
        }
    }

    fun watchTyping() {
        mTypingListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var writing = ""
                var count = 0
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (child in dataSnapshot.children) {
                        val now = (now() / 1000).toDouble()
                        val diff = now - java.lang.Double.valueOf(child.value!!.toString())

                        if (diff < 10.0 && child.key != Me.atn.user_id) {
                            val writer = getParticipant(child.key.toString())
                            if (writer != null) {
                                if (count == 1) {
                                    writing += " & "
                                }
                                if (writer != null) {
                                    writing += writer["first_name"]
                                    count += 1
                                }
                            }
                        }
                    }
                    if (count == 1) {
                        writing += " is typing..."
                    } else if (count == 2) {
                        writing += " are typing..."
                    } else if (count > 2) {
                        writing = count.toString() + " WDSers are typing..."
                    }
                }
                MainActivity.self.chatFragment.updateTyping(writing)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        Fire.watch("/chats/$mChatId/typing", mTypingListener)
    }

    fun addParticipantToFire(user_id: String) {
        Fire.set("/chats_by_user/$user_id/$mChatId", "1")
    }
    fun removeParticipant(user_id: String) {
        val tmp = ArrayList<HashMap<*, *>>();
        Fire.remove("/chats_by_user/$user_id/$mChatId")
        mParticipants.forEach {
            if (it.get("user_id") != user_id) {
                tmp.add(it)
            }
        }
        mParticipants = tmp
        set("participants", mParticipants)
    }

    fun addAdmin(user_id: String) {
        mAdmins!!.add(user_id)
        set("admins", mAdmins!!);
    }

    fun removeAdmin(user_id: String) {
        mAdmins!!.remove(user_id)
        set("admins", mAdmins!!)
    }

    fun unwatch() {
        Fire.unwatch(mListener)
        Fire.unwatch(mTypingListener)
        mListener = null
        mTypingListener = null
    }

    fun getParticipant(user_id: String): HashMap<*, *>? {
        for (a in mParticipants) {
            if (a["user_id"].toString() == user_id) {
                return a
            }
        }
        return null
    }

    fun clear() {
        mMsgs.clear()
        drawMessages()
    }

    fun drawMessages() {
        MainActivity.self.chatFragment.updateMessages(mMsgs)
    }

}
