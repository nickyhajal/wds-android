package com.worlddominationsummit.wdsandroid;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
//import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.android.volley.VolleyError;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import io.fabric.sdk.android.Fabric;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity implements Runnable {
    private static final int MY_CAMERA_REQUEST_CODE = 100;


    public static MainActivity self;
    public static HashMap state;
    public static HashMap pre;
    public static String version = "19.1.5";
    private Button search_close;
    public EditText search_inp;
    private TextView title;
    private LinearLayout titleShell;
    private RelativeLayout searchShell;
    private FrameLayout contentLayout;
    private Spinner mMeetupSpinner;
    private Spinner mExploreSpinner;
    private RelativeLayout mNotnCountShell;
    private TextView mNotnCount;
    private ImageView mLogo;
    private LinearLayout mMeetupHead;
    private RelativeLayout mExploreHead;
    private RelativeLayout mScheduleHead;
    private RelativeLayout mChatAdminHead;
    private Button mChatStartBtn;
    private AttendeeSearcher searcher;
    public Boolean tabsStarted = false;
    public LoginFragment loginFragment;
    public LoadingFragment loadingFragment;
    public WalkthroughFragment walkthroughFragment;
    public EventsFragment eventsFragment;
    public EventTypesFragment eventTypesFragment;
    public ChatEditFragment chatEditFragment;
    public AttendeeSearchFragment attendeeSearchFragment;
    public ScheduleFragment scheduleFragment;
    public HomeFragment homeFragment;
    public ProfileFragment profileFragment;
    public RegistrationFragment registrationFragment;
    public TicketChoiceFragment ticketChoiceFragment;
    public PostFragment postFragment;
    public FiltersFragment filtersFragment;
    public CommunitiesFragment communitiesFragment;
    public UserNotesFragment userNotesFragment;
    public EventFragment eventFragment;
    public CartFragment cartFragment;
    public AtnStoryFragment atnStoryFragment;
    public ChatFragment chatFragment;
    public NotificationFragment notificationFragment;
    public ChatsFragment chatsFragment;
    public ExploreFragment exploreFragment;
    public CheckinFragment checkinFragment;
    public WDSCameraFragment cameraFragment;
    public EventAttendeesFragment eventAttendeesFragment;
    public DispatchContentFragment dispatchContentFragment;
    public TabsFragment tabsFragment;
    public Boolean tabsActive = false;
    public Boolean searching = false;
    public Fragment active;
    public int SCAN_REQUEST_CODE = 19819;
    public HashMap<String, Fragment> frags = new HashMap<String, Fragment>();
    public String ntfnData = "";
    public static float density;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.self = this;
        MainActivity.density = getResources().getDisplayMetrics().density;
        MainActivity.pre = new HashMap();
        MainActivity.pre.put("fresh", new ArrayList<HashMap>());
        MainActivity.pre.put("used", new ArrayList<HashMap>());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheFileCount(1000)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
//        ImageLoaderConfiguration.createDefault(this)
        ImageLoader.getInstance().init(config);
        Fabric.with(this, new Crashlytics());
        this.contentLayout = new FrameLayout(this);
        this.contentLayout.setId(R.id.frame_layout);
        this.setContentView(this.contentLayout);
        Intent intent = getIntent();
        if (intent != null) {
            String nrsp = intent.getStringExtra("notification");
            if (nrsp != null) {
                ntfnData = nrsp;
            }
        }
        this.initApp();
        this.startExperience();
    }

    public void updateChatStartBtn(int numChatters) {
        if (numChatters > 0) {
            mChatStartBtn.setVisibility(View.VISIBLE);
        } else {
            mChatStartBtn.setVisibility(View.GONE);
        }
    }


    public void initActionBar(){
        final MainActivity ref = this;
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.attendee_search_bar);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
//        getActionBar().setHomeButtonEnabled(false);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar().setDisplayShowHomeEnabled(false);
        View bar = actionBar.getCustomView();
        bar.setVisibility(View.VISIBLE);
        this.title = (TextView) bar.findViewById(R.id.title);
        this.title.setTypeface(Font.use("Vitesse_Medium"));
        this.titleShell = (LinearLayout) bar.findViewById(R.id.titleShell);
        this.searchShell = (RelativeLayout) bar.findViewById(R.id.searchShell);
        this.search_inp = (EditText) bar.findViewById(R.id.search_inp);
        this.search_close = (Button) bar.findViewById(R.id.search_close);
        this.search_close.setTypeface(Font.use("Karla_Bold"));
        this.search_close.setVisibility(View.GONE);
        this.search_inp.setTypeface(Font.use("Karla"));
        this.searcher = new AttendeeSearcher(this, search_inp);
        this.titleShell.setVisibility(View.GONE);
        mLogo = (ImageView) bar.findViewById(R.id.logo);
        mNotnCountShell = (RelativeLayout) bar.findViewById(R.id.notnCountShell);
        mNotnCount = (TextView) bar.findViewById(R.id.notnCount);
        mNotnCount.setTypeface(Font.use("Karla_Italic"));
        mMeetupSpinner = (Spinner) bar.findViewById(R.id.meetupSpinner);
        mMeetupHead = (LinearLayout) bar.findViewById(R.id.meetups);
        mMeetupHead.setVisibility(View.GONE);
        mExploreSpinner = (Spinner) bar.findViewById(R.id.exploreSpinner);
        mExploreHead = (RelativeLayout) bar.findViewById(R.id.explore);
        mExploreHead.setVisibility(View.GONE);
        Button checkinBtn = (Button) bar.findViewById(R.id.checkin);
        checkinBtn.setTypeface(Font.use("Vitesse_Medium"));
        checkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_checkins();
            }
        });
        mScheduleHead = (RelativeLayout) bar.findViewById(R.id.scheduleHead);
        mScheduleHead.setVisibility(View.GONE);
        mChatAdminHead = (RelativeLayout) bar.findViewById(R.id.chatAdminHead);
        mChatAdminHead.setVisibility(View.GONE);
        mChatStartBtn = (Button) bar.findViewById(R.id.chatStartBtn);
        mChatStartBtn.setVisibility(View.VISIBLE);
        mChatStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatEditFragment.startChat();
            }
        });
        mChatAdminHead.setVisibility(View.GONE);
        mChatStartBtn.setTypeface(Font.use("Vitesse_Medium"));
        mChatStartBtn.setVisibility(View.GONE);
//        Button regBtn = (Button) bar.findViewById(R.id.regBtn);
//        regBtn.setTypeface(Font.use("Vitesse_Medium"));
//        regBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                open_registration();
//            }
//        });
        TextView chatAdminTitle = (TextView) bar.findViewById(R.id.chatAdminTitle);
        chatAdminTitle.setTypeface(Font.use("Vitesse_Medium"));
        TextView regTitle = (TextView) bar.findViewById(R.id.regTitle);
        regTitle.setTypeface(Font.use("Vitesse_Medium"));
        ArrayList<String> meetupSections = new ArrayList<String>();
        meetupSections.add("Browse Meetups");
        meetupSections.add("Attending Meetups");
        meetupSections.add("Suggested Meetups");
        TitleSpinner meetupSpinnerAdapter =  new TitleSpinner(this, R.layout.title_spinner, meetupSections);
        mMeetupSpinner.setAdapter(meetupSpinnerAdapter);
        mMeetupSpinner.setSelection(0);
        ArrayList<String> exploreSections = new ArrayList<String>();
        exploreSections.add("Near You");
        exploreSections.add("What's Hot");
        TitleSpinner exploreSpinnerAdapter =  new TitleSpinner(this, R.layout.title_spinner, exploreSections);
        mExploreSpinner.setAdapter(exploreSpinnerAdapter);
        mExploreSpinner.setSelection(0);
        this.search_inp.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                ref.run_search(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        this.search_inp.setOnFocusChangeListener(searchFocus);
        this.search_close.setOnClickListener(searchCloseClick);
        mExploreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                exploreFragment.changeState(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        mLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_notifications();
            }
        });
        mNotnCountShell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_notifications();
            }
        });
    }

    public void updateNotificationCount(long count) {
        if (count > 0) {
            mNotnCount.setText(String.valueOf(count));
            mNotnCountShell.setVisibility(View.VISIBLE);
        } else {
            mNotnCountShell.setVisibility(View.GONE);
        }
    }

    public void initApp() {
        Font.init(this);
        this.initActionBar();
        Store.init(this);
        Api.init(this);
        Fire.init();
        initScreens();
//        this.open_loading();
        EventTypes.init();
        Assets.INSTANCE.init(this);
        Me.init(this);
    }

    public void initScreens() {
        loadingFragment = new LoadingFragment();
        loginFragment = new LoginFragment();
        walkthroughFragment = new WalkthroughFragment();
        tabsFragment = new TabsFragment();
        eventsFragment = new EventsFragment();
        eventTypesFragment = new EventTypesFragment();
        eventFragment = new EventFragment();
        profileFragment = new ProfileFragment();
        scheduleFragment = new ScheduleFragment();
        registrationFragment = new RegistrationFragment();
        checkinFragment = new CheckinFragment();
        attendeeSearchFragment = new AttendeeSearchFragment();
        ticketChoiceFragment = new TicketChoiceFragment();
        postFragment = new PostFragment();
        eventAttendeesFragment = new EventAttendeesFragment();
        communitiesFragment = new CommunitiesFragment();
        cartFragment = new CartFragment();
        atnStoryFragment = new AtnStoryFragment();
        chatFragment = new ChatFragment();
        chatEditFragment = new ChatEditFragment();
        notificationFragment = new NotificationFragment();
        chatsFragment = new ChatsFragment();
        filtersFragment = new FiltersFragment();
        dispatchContentFragment = new DispatchContentFragment();
        homeFragment = new HomeFragment();
        exploreFragment = new ExploreFragment();
        userNotesFragment = new UserNotesFragment();
        homeFragment.init();
    }

    public void startExperience() {
//        Store.set("walkthrough", "0");
//        Store.set("user_token", "");
        int step = Me.checkWalkthrough();
        if(step < 7) {
            open_walkthrough(step);
        }
        else {
            Me.checkLoggedIn();
        }
    }

    public void showNotification() {
        JSONObject cont = new JSONObject();
        try {
            cont = new JSONObject(ntfnData);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        ntfnData = "";
        final String link = cont.optString("link");
        final String from_id = cont.optString("from_id");
        if (link.substring(0,1).equals("~")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Attendee atn = new Attendee();
                    atn.user_id = from_id;
                    open_profile(atn);
                }
            }, 100);
        } else if (link.contains("dispatch")) {
            String[] bits = link.split("/");
            String id = bits[bits.length - 1];
            JSONObject params = new JSONObject();
            try {
                params.put("channel_id", id);
                params.put("channel_type", "feed_item");
                params.put("include_author", true);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            Api.get("feed", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    try {
                        JSONObject jsonItem = rsp.getJSONArray("feed_contents").getJSONObject(0);
                        HashMap item = (HashMap) JsonHelper.toMap(jsonItem);
                        open_dispatch_item(item);
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            });
        }
    }

    public void open_tabs() {
        if(this.tabsStarted) {
            open(this.tabsFragment, "tabs");
            if (ntfnData.length() > 0) {
                showNotification();
            }
        }
        else {
            this.start_tabs();
        }
    }
    public void start_tabs() {
        final MainActivity ref = this;
//        this.open_loading();
        Assets.INSTANCE.sync(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                ref.tabsStarted = true;
                ref.open_tabs();
                open_dispatch();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ref.tabsStarted = true;
                ref.open_tabs();
                open_dispatch();
            }
        });

    }

    public void run() {
        open_dispatch();
    }

    public void updateTitle() {
        if (!searching) {
            String title = "";
            if (active == postFragment) {
                if (postFragment.mUserId != null) {
                    title = "Add a User Note";
                }
                else if (postFragment.mFeedItem != null) {
                    title = "Post a Comment";
                } else {
                    title = "Share a Post";
                }
            } else if (active == eventTypesFragment) {
                title = "Events";
            } else if (active == chatsFragment) {
                title = "Messages";
            } else if (active == chatFragment) {
                title = chatFragment.getTitle();
            } else if (active == chatEditFragment) {
                title = chatEditFragment.getTitle();
            } else if (active == eventsFragment) {
                title = EventTypes.byId.optJSONObject(eventsFragment.getMType()).optString("plural");
            } else if (active == exploreFragment) {
                title = "Explore";
            } else if (active == homeFragment) {
                if (homeFragment.activeEvent != null) {
                    title = "Dispatch: " + eventFragment.event.getWhat();
                }
            } else if (active == checkinFragment) {
                title = "Check In";
            } else if (active == ticketChoiceFragment) {
                title = "Choose Your Ticket";
            } else if (active == communitiesFragment) {
                title = "Communities";
            } else if (active == notificationFragment) {
                title = "Notifications";
            } else if (active == cartFragment) {
                title = "Let's Do This!";
            } else if (active == atnStoryFragment) {
                title = "Your Story";
            } else if (active == scheduleFragment) {
                title = "Your Schedule";
            } else if (active == registrationFragment) {
                title = "Registration";
            } else if (active == filtersFragment) {
                title = "Dispatch Filters";
            } else if (active == userNotesFragment) {
                title = "User Notes";
            } else if (active == dispatchContentFragment) {
                title = "Conversation";
            } else if (active == eventFragment) {
                title = this.eventFragment.event.getWhat();
            } else if (active == eventAttendeesFragment) {
                title = "Attendees for "+ this.eventFragment.event.getWhat();
            } else if (active == profileFragment) {
                title = " ";
            }
            this.titleShell.setVisibility(View.GONE);
            this.searchShell.setVisibility(View.GONE);
            mMeetupHead.setVisibility(View.GONE);
            mExploreHead.setVisibility(View.GONE);
            mScheduleHead.setVisibility(View.GONE);
            mChatAdminHead.setVisibility(View.GONE);
            if (title.length() > 0) {
                if (title.equals("Meetupsoeansrtoen-turnthissettinoff")) {
                    mMeetupHead.setVisibility(View.VISIBLE);
                } else if (title.equals("Explore")) {
                        mExploreHead.setVisibility(View.VISIBLE);
                } else if (title.equals("Your Schedule")) {
                    mScheduleHead.setVisibility(View.VISIBLE);
                } else if (title.equals("Start a Chat")) {
                    mChatAdminHead.setVisibility(View.VISIBLE);
                } else {
                    this.title.setText(title);
                    this.titleShell.setVisibility(View.VISIBLE);
                }
            } else {
                if (this.titleShell != null && this.searchShell != null) {
                    this.searchShell.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    public void open_walkthrough(int step) {
        open(walkthroughFragment, true, "walkthrough");
    }
    public void open_events(HashMap type) {
        eventsFragment.setType(type);
        open_events();
    }
    public void open_events() {
        open_tabs();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                eventsFragment.willDisplay();
                tabsFragment.open(eventsFragment, "eventlisting");
            }
        }, 10);
    }
    public void open_notifications() {
        notificationFragment.sync();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                open(notificationFragment, "notiffrag");
            }
        }, 10);
    }
    public void open_chat(String pid) {
        chatFragment.setPid(pid);
        open_chat();
    }
    public void open_chat(String pid, Boolean group, String name) {
        chatFragment.setPid(pid, group, name);
        open_chat();
    }
    public void open_chat(Attendee atn) {
        chatFragment.setAttendee(atn);
        open_chat();
    }
    public void open_chat(ArrayList<Attendee> atns) {
        chatFragment.setAttendees(atns);
        open_chat();
    }
    public void open_chat(ArrayList<Attendee> atns, String name) {
        chatFragment.setName(name);
        chatFragment.setAttendees(atns);
        open_chat();
    }
    public void open_chat() {
        open_tabs();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                open(chatFragment, "chat");
            }
        }, 10);
    }
    public void open_chat_edit() {
        open_tabs();
        chatEditFragment.clearChat();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                open(chatEditFragment, "chatedit");
            }
        }, 10);
    }
    public void open_chat_edit(String chatId) {
        open_tabs();
        chatEditFragment.setChat(chatId);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                open(chatEditFragment, "chatedit");
            }
        }, 10);
    }
    public void open_cart(String code, HashMap prod) {
        cartFragment.setProduct(code, prod);
        if (code.equals("wds2019") || code.equals("wdsDouble")) {
            cartFragment.setTerms("Each ticket includes 1 complimentary, non-transferable WDS Academy, priority booking at the WDS Hotel, and other discounts and benefits.\n\nTickets are non-refundable. Name changes and ticket transfers are permitted up to 30 days prior to the event for a $100 fee. A late transfer option will be available at a higher cost");
        }
        open_cart();
    }
    public void open_cart() {
        open_tabs();
//        tabsFragment.open(cartFragment, "cart");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tabsFragment.open(cartFragment, "cart");
            }
        }, 1);
    }
    public void open_event_types() {
        open_tabs();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                eventsFragment.willDisplay();
                tabsFragment.open(eventTypesFragment, "event_types");
            }
        }, 10);
    }

    public void open_user_notes(String user_id) {
        userNotesFragment.setUser(user_id);
        tabsFragment.open(userNotesFragment, "user_notes");
    }

    public void open_event_attendees(String event_id) {
        eventAttendeesFragment.setEvent(event_id);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tabsFragment.open(eventAttendeesFragment, "event-attendees");
            }
        }, 10);
    }
    public void open_atnstory() {
        open(atnStoryFragment, "atnstory");
    }
    public void open_dispatch() {
        open_tabs();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                homeFragment.willDisplay();
                tabsFragment.open(homeFragment, "home");
            }
        }, 10);
    }

    public void open_chats() {
        open_tabs();
        tabsFragment.open(chatsFragment, "chats");
    }

    public void open_explore() {
        open_tabs();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tabsFragment.open(exploreFragment, "explore");
            }
        }, 10);
    }

    public void open_dispatch_item(HashMap item) {
        dispatchContentFragment.setItem(item);
        open(dispatchContentFragment, "dispatchContent");
    }

    public void open_schedule() {
        this.scheduleFragment.willDisplay();
        this.tabsFragment.open(this.scheduleFragment, "schedule");
    }

    public void open_registration() {
        this.registrationFragment.willDisplay();
        this.tabsFragment.open(this.registrationFragment, "registration");
    }

    public void open_login() {
        open(this.loginFragment, true, "login");
    }
    public void open_ticketChoice() {
        open(this.ticketChoiceFragment, false, "ticketChoice");
    }

    public void open_loading() {
        open(this.loadingFragment, true, "loading");
    }

    public void open(Fragment frag, String tag) {
        open(frag, false, tag);
    }

    public void open_filters() {
        open(this.filtersFragment, "filters");
    }
    public void open_communities() {
        open(this.communitiesFragment, "communities");
    }
    public void open_community(String interest_id) {
        homeFragment.setChannel("interest", interest_id);
        open_dispatch();
    }
    public void open_post() {
        open(this.postFragment, "post");
    }
    public void open_event(Event event) {
        this.eventFragment.setEvent(event);
        tabsFragment.open(this.eventFragment, "event");
    }
    public void open_event() {
        tabsFragment.open(this.eventFragment, "event");
    }
    public void open_profile(Attendee atn) {
        this.profileFragment.setAttendee(atn);
        open(this.profileFragment, "profile");
    }
    public void open_search() {
        searching = true;
        this.search_close.setVisibility(View.VISIBLE);
        open(this.attendeeSearchFragment, "search");
    }

    public void open_profile_from_search(Attendee atn) {
        searching = false;
        InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.search_inp.getWindowToken(), 0);
        open_profile(atn);
    }
    public void close_search() {
        searching = false;
        this.search_close.setVisibility(View.GONE);
        this.search_inp.clearFocus();
        getActionBar().getCustomView().requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.search_inp.getWindowToken(), 0);
        open_tabs();
    }

    public void run_search(String query) {
        this.searcher.startSearch(query);
    }

    public void update_search(JSONArray users) {
        this.attendeeSearchFragment.update_items(users);
    }
    public void update_search(ArrayList<HashMap<String, String>> users) {
        this.attendeeSearchFragment.update_items(users);
    }

    public void open_checkins() {
        open(checkinFragment, "checkin");
    }



    @BindView(R.id.addCameraButton) View addCameraButton;

    public void openCamera() {
        int perm = this.checkCallingOrSelfPermission(Manifest.permission.CAMERA);
        if ( cameraFragment == null ) {
            cameraFragment = new WDSCameraFragment();
        }
        if (perm == PackageManager.PERMISSION_GRANTED) {
            open(cameraFragment, true, "cameraFragment");
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }

    public void open(Fragment frag, Boolean hideActionBar, String tag) {
        if(hideActionBar) {
            getActionBar().hide();
        }
        else getActionBar().show();
//        Puts.i(tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, frag, tag);
        if  (frag != loadingFragment) {
//            Puts.i("tag");
            transaction.addToBackStack(tag);
        }
        transaction.commit();
        if (!frags.containsKey(tag)) {
//            Puts.i("contains key");
            frags.put(tag, frag);
        }
        if (frag != tabsFragment) {
//            tabsActive = false;
//            Puts.i("frag isnt tabsfrag");
            this.active = frag;
            updateTitle();
        }
        else {
//            Puts.i("frag is active now");
            tabsActive = true;
        }
    }

    public static int getImage(String ImageName) {
        return self.getResources().getIdentifier(ImageName, "drawable", self.getPackageName());
    }
    public static void offlineAlert() {
        final Dialog dialog = new Dialog(MainActivity.self); // context, this etc.
        LayoutInflater inflater = dialog.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog, null, false);
        Button cancelBtn = (Button) view.findViewById(R.id.dialog_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        ((TextView) view.findViewById(R.id.dialog_info)).setTypeface(Font.use("Karla"));
        cancelBtn.setTypeface(Font.use("Vitesse_Medium"));
        dialog.setContentView(view);
        dialog.setTitle("We Can't Reach WDS HQ");
        dialog.show();
    }

    public Fragment getFrag(String name) {
        FragmentManager mngr = getSupportFragmentManager();
        String tabs = "meetups,schedule,home";
        if (tabs.contains(name)) {
            mngr = tabsFragment.getChildFragmentManager();
        }
        return mngr.findFragmentByTag(name);
    }

    public Boolean tabsAreActive() {
        int inx= getSupportFragmentManager().getBackStackEntryCount() - 1;
        return inx > -1 && getSupportFragmentManager().getBackStackEntryAt(inx).getName().compareTo("tabs") == 0;
    }
    public void findActive() {
        Iterator it = frags.entrySet().iterator();
        int inx= getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (inx > -1 && getSupportFragmentManager().getBackStackEntryAt(inx).getName().compareTo("tabs") == 0) {
            tabsActive = true;
        } else {
            tabsActive = false;
        }
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            Fragment f = (Fragment) pair.getValue();
            if (f.isVisible() && f != tabsFragment) {
                if (f == tabsFragment) {
//                    tabsActive = true;
                } else {
                    active = f;
                }
            }
        }
    }

    private View.OnFocusChangeListener searchFocus =  new View.OnFocusChangeListener() {
        public void onFocusChange(View view, boolean gainFocus) {
            if (gainFocus) {
                MainActivity.self.open_search();
            }
        }
    };



    private View.OnClickListener searchCloseClick =  new View.OnClickListener() {
        public void onClick (View view) {
            search_inp.setText("");
            MainActivity.self.close_search();
        }
    };

    private Fragment getCurrentFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
        String fragmentTag = entry.getName();
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag(fragmentTag);
        return currentFragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_REQUEST_CODE) {
            String resultDisplayStr;
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
                resultDisplayStr = "Card Number: " + scanResult.getRedactedCardNumber() + "\n";

                // Do something with the raw number, e.g.:
                // myService.setCardNumber( scanResult.cardNumber );

                if (scanResult.isExpiryValid()) {
                    resultDisplayStr += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
                }

                if (scanResult.cvv != null) {
                    // Never log or display a CVV
                    resultDisplayStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
                }

                if (scanResult.postalCode != null) {
                    resultDisplayStr += "Postal Code: " + scanResult.postalCode + "\n";
                }
                cartFragment.setCard(scanResult.getFormattedCardNumber(), scanResult.cvv, scanResult.expiryMonth, scanResult.expiryYear);
            }
            else {
                resultDisplayStr = "Scan was canceled.";
            }
            // do something with resultDisplayStr, maybe display it in a textView
            // resultTextView.setText(resultDisplayStr);
        }
        // else handle other activity results
    }

    @Override
    protected void onStop() {
        super.onStop();
        Me.stopWatchingNotificatons();
    }

    @Override
    public void onBackPressed() {
        // Fragment fragmentBeforeBackPress = getCurrentFragment();
        // Perform the usual back action
//        Puts.i("tabsActive: " );
//        Puts.i(tabsAreActive());
        int c = 0;
        Boolean foundFrag = true;
//        while (c < getSupportFragmentManager().getBackStackEntryCount()) {
//            FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(c);
//            c += 1;
//            if (entry != null) {
//                Puts.i(entry.getName());
//            }
//        }
        if(tabsAreActive() && active.equals(homeFragment)) {
//            Puts.i(">>>> CLOSE");
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
//            finish();
        } else {
//            Puts.i("Pop stack back");
            if(getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
            }
            if (tabsAreActive() && tabsFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                c = 0;
//                Puts.i(">>>> ");
//                while (c < tabsFragment.getChildFragmentManager().getBackStackEntryCount()) {
//                    FragmentManager.BackStackEntry entry = tabsFragment.getChildFragmentManager().getBackStackEntryAt(c);
//                    c += 1;
//                    if (entry != null) {
//                        Puts.i(entry.getName());
//                    } else {
//                        foundFrag = false;
//                    }
//                }
//                Puts.i("Pop tabs back");
                tabsFragment.getChildFragmentManager().popBackStack();
            } else {
//                super.onBackPressed();
                tabsActive = false;
            }
            Handler hnd = new Handler();
            hnd.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                    findActive();
                    updateTitle();
                    if (tabsActive) {
                        tabsFragment.updateTabs();
                    }
            }
        }, 10);
        }
    }


    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openCamera();
                    }
                }, 700);

            } else {
                // Camera denied

            }

        }
    }



    private static class TitleSpinner extends ArrayAdapter<String> {
        private TitleSpinner(Context context, int resource, ArrayList<String> items) {
            super(context, resource, items);
        }

        // Affects default (closed) state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            float density = MainActivity.density;
            int p = (int) density * 5;
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setTypeface(Font.use("Vitesse_Medium"));
            view.setPadding(0,p,0,0);
            return view;
        }

        // Affects opened state of the spinner
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            float density = MainActivity.density;
            int p = (int) density * 10;
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            view.setTypeface(Font.use("Vitesse_Medium"));
            view.setPadding(p, p, p, p);
            view.setBackgroundColor(MainActivity.self.getResources().getColor(R.color.coffee));
            return view;
        }

    }
}
