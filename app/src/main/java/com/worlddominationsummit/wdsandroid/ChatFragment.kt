package com.worlddominationsummit.wdsandroid

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView

import com.android.volley.VolleyError

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.lang.reflect.Array
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask

import android.os.Handler

/**
 * Created by nicky on 08/07/16.
 */
class ChatFragment : Fragment(), Runnable {
    var mView: View? = null
    var mAdapter: ChatAdapter? = null
    var mMsgs: ArrayList<HashMap<String, Any>>? = ArrayList()
    var mChat: Chat? = null
    var mChatName: String? = null
    lateinit var mState: String
    var mReadyToReceive: Boolean? = null
    var mAtn: HashMap<String, Any>? = null
    var mAtns: ArrayList<Attendee>? = null
    var mOldPid: String? = null
    var mTypingAnim: Animation? = null
    private var mTimer: Handler? = Handler()

    // View
    var mMessage: TextView? = null
    lateinit var mSubMessage: TextView
    var mTyping: TextView? = null
    var mListview: ListView? = null
    lateinit var mMessages: LinearLayout
    lateinit var mSubmit: Button
    lateinit var mNewMsg: EditText

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView == null) {
            mView = inflater!!.inflate(R.layout.chat, container, false)
            mListview = mView!!.findViewById(R.id.chatMsgList) as ListView
            mMessage = mView!!.findViewById(R.id.msg) as TextView
            mTyping = mView!!.findViewById(R.id.typingShell) as TextView
            mMessages = mView!!.findViewById(R.id.messages) as LinearLayout
            mSubMessage = mView!!.findViewById(R.id.submsg) as TextView
            mSubmit = mView!!.findViewById(R.id.submitBtn) as Button
            mNewMsg = mView!!.findViewById(R.id.newMsg) as EditText
            Font.applyTo(mView)
            setState("loading")
            mSubmit.setOnClickListener {
                mChat!!.send(mNewMsg.text.toString())
                mNewMsg.setText("")
                mChat!!.setTyping(false)
            }
            val self = this
            mNewMsg.setOnClickListener {
                if (!mNewMsg.hasFocus()) {
                    mNewMsg.requestFocus()
                    val imm = MainActivity.self.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(mNewMsg, InputMethodManager.SHOW_IMPLICIT)
                }
            }
            mNewMsg.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                }

                override fun afterTextChanged(editable: Editable) {
                    if (mTimer != null) {
                        mTimer!!.removeCallbacks(self)
                        mTimer!!.postDelayed(self, 2000)
                    }
                    if (mChat != null) {
                        mChat!!.setTyping(true)
                    }
                }
            })
        }
        return mView
    }

    override fun run() {
        if (mChat != null) {
            mChat!!.setTyping(false)
            if (mChat!!.mGroup) {
                setState("group")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mOldPid = mChat!!.mPid
        mChat!!.setTyping(false)
        mChat!!.unwatch()
        mChat = null
        mAtn = null
        mMsgs!!.clear()
        MainActivity.self.tabsFragment.showTabs()
        updateItems()
    }

    override fun onResume() {
        super.onResume()
        MainActivity.self.tabsFragment.hideTabs()
        mTimer = Handler()
        if (mOldPid != null && mAtn == null && mChat == null) {
            setPid(mOldPid!!)
        }
        updateAdapter()
    }

    fun setName(name: String) {
        mChatName = name
    }

    fun getTitle(): String {
        var title: String = ""
        if (mChatName != null) {
            title = mChatName!!
        } else {
            if (mAtn != null) {
                title += "Chat with "+ mAtn!!.get("first_name");
            }
        }
        return title
    }
    fun updateAdapter() {
        Handler().postDelayed({
            if (mAdapter != null) {
                mAdapter!!.notifyDataSetChanged()
            }
            updateAdapter()
        }, 37000)
    }


    // We use this to set a group
    fun setAttendees(atns: ArrayList<Attendee>) {
        setState("loading")
        MainActivity.self.updateTitle()
        if (mChat == null && mChatName !== null) {
            mAtns = atns
            mAtns!!.add(Me.atn)
            mChat = Chat(mAtns!!, true, mChatName!!)
            startChat()
        }
    }

    fun setAttendee(atn: Attendee) {
        MainActivity.self.updateTitle()
        setState("loading")
        if (mChat == null) {
            mAtn = atn.toSimpleHashMap() as HashMap<String, Any>
            atn.readyForMessages {
                if (atn.receivesMsgs) {
                    mReadyToReceive = true
                } else {
                    mReadyToReceive = false
                }
                syncState()
            }
            val atns = ArrayList<Attendee>()
            atns.add(atn)
            atns.add(Me.atn)
            mChat = Chat(atns)
            startChat()
        }

    }

    fun setPid(pid: String) {
        setState("loading")
        mChatName = null
        if (mChat == null) {
            clearMsgs()
            mChat = Chat(pid)
            startChat()
        }
    }
    fun setPid(pid: String, group: Boolean, name: String) {
        setState("loading")
        mChatName = name
        if (mChat == null) {
            clearMsgs()
            mChat = Chat(pid, group, name)
            startChat()
        }
    }

    fun startChat() {
        val runnable = Runnable {
            setState("null")
            if (mChat!!.mGroup) {
                setState("group")
            }
            mChat!!.watch()
            mChat!!.watchTyping()
            for (atn in mChat!!.mParticipants) {
                if (atn["user_id"].toString() != Me.atn.user_id) {
                    mAtn = atn as HashMap<String, Any>
                    break
                }
            }
            MainActivity.self.updateTitle()
        }
        mChat!!.whenReady(runnable)

    }

    fun updateMessages(msgs: ArrayList<HashMap<String, Any>>) {
        if (msgs.size > 0) {
            setState("loaded")
        } else {
            setState("null")
        }
        mMsgs = msgs
        updateItems()
    }

    fun updateItems() {
        if (this.activity != null) {
            mAdapter = ChatAdapter(this.activity, mMsgs!!)
            if (mListview != null) {
                mListview!!.adapter = mAdapter
                Handler().postDelayed({
                    if (mAdapter != null) {
                        mAdapter!!.notifyDataSetChanged()
                    }
                    updateAdapter()
                }, 1500)
            }
        } else {
            Handler().postDelayed({ updateItems() }, 500)
        }
    }

    fun updateTyping(typing: String) {
        if (mTyping != null && (mTypingAnim == null || mTypingAnim!!.hasEnded())) {
            if (typing.length == 0) {
                mTypingAnim = resizeWithAnimation(mTyping!!, 150, 1)
                mTyping!!.animate().alpha(0.0f)
                Handler().postDelayed({ mTyping!!.text = typing }, 250)
            } else {
                mTyping!!.text = typing
                mTypingAnim = resizeWithAnimation(mTyping!!, 100, 75)
                mTyping!!.animate().alpha(1.0f)
            }
        }
    }

    fun clearMsgs() {
        if (mMsgs != null) {
            mMsgs!!.clear()
        } else {
            mMsgs = ArrayList<HashMap<String, Any>>()
        }
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
                    mMessages.visibility = View.VISIBLE
                    mListview!!.visibility = View.GONE
                    mSubMessage.visibility = View.GONE
                }
                "null" ->
                    // mAtn would really only be null here if we are opening an existing
                    // chat from a pid and therefore don't have an attendee
                    // that means that the chat wouldn't be null anyway, so we can
                    // just ignore
                    if (mAtn != null) {
                        mMessages.visibility = View.VISIBLE
                        mMessage!!.text = "Send a Message to " + mAtn!!["first_name"] + " below."
                        if (mReadyToReceive != null) {
                            mSubMessage.visibility = View.VISIBLE
                            if (mReadyToReceive!!) {
                                mSubMessage.text = mAtn!!["first_name"].toString() + " has the latest WDS App installed and will receive your messages immediately."
                            } else {
                                mSubMessage.text = mAtn!!["first_name"].toString() + " hasn't installed the latest WDS App but will receive your messages once they do."
                            }
                        } else {
                            mSubMessage.visibility = View.GONE
                        }
                        mListview!!.visibility = View.GONE
                    } else if (mChat!!.mPid!!.contains("support")){
                        mReadyToReceive = true
                        mSubMessage.visibility = View.VISIBLE
                        mMessages.visibility = View.VISIBLE
                        mListview!!.visibility = View.GONE
                        mMessage!!.text = "Send a message to the WDS Concierge below."
                        mSubMessage.text = "We'll reply back as soon as possible and can chat through your situation."
                    }
                "group" -> {
                    mMessages.visibility = View.VISIBLE
                    mMessage!!.text = "Woohoo! Your group is ready to chat!"
                }
                "loaded" -> {
                    mMessages.visibility = View.GONE
                    mListview!!.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun resizeWithAnimation(view: View, duration: Int, targetHeight: Int): Animation {
        val initialHeight = view.measuredHeight
        val distance = targetHeight - initialHeight

        val a = object : Animation() {

            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f && targetHeight == 0) {
                    //                    view.setVisibility(View.GONE);
                } else {
                    //                    view.setVisibility(View.VISIBLE);
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

}
