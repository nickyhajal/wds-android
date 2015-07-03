package com.worlddominationsummit.wdsandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
//import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import info.hoang8f.android.segmented.SegmentedGroup;
public class MainActivity extends FragmentActivity implements Runnable {

    public static MainActivity self;
    private Button search_close;
    public EditText search_inp;
    private TextView title;
    private LinearLayout titleShell;
    private RelativeLayout searchShell;
    private FrameLayout contentLayout;
    private Spinner mMeetupSpinner;
    private LinearLayout mMeetupHead;
    private AttendeeSearcher searcher;
    private Boolean tabsStarted = false;
    public LoginFragment loginFragment;
    public LoadingFragment loadingFragment;
    public WalkthroughFragment walkthroughFragment;
    public MeetupsFragment meetupsFragment;
    public AttendeeSearchFragment attendeeSearchFragment;
    public ScheduleFragment scheduleFragment;
    public HomeFragment homeFragment;
    public ProfileFragment profileFragment;
    public PostFragment postFragment;
    public FiltersFragment filtersFragment;
    public CommunitiesFragment communitiesFragment;
    public UserNotesFragment userNotesFragment;
    public MeetupFragment meetupFragment;
    public MeetupAttendeesFragment meetupAttendeesFragment;
    public DispatchContentFragment dispatchContentFragment;
    public TabsFragment tabsFragment;
    public Boolean tabsActive = false;
    public Boolean searching = false;
    public Fragment active;
    public HashMap<String, Fragment> frags = new HashMap<String, Fragment>();
    public String ntfnData = "";
    public static float density;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.self = this;
        MainActivity.density = getResources().getDisplayMetrics().density;
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
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

    public void initActionBar(){
        final MainActivity ref = this;
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.attendee_search_bar);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
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
        mMeetupSpinner = (Spinner) bar.findViewById(R.id.meetupSpinner);
        mMeetupHead = (LinearLayout) bar.findViewById(R.id.meetups);
        mMeetupHead.setVisibility(View.GONE);
        ArrayList<String> meetupSections = new ArrayList<String>();
        meetupSections.add("Browse Meetups");
        meetupSections.add("Attending Meetups");
        meetupSections.add("Suggested Meetups");
        TitleSpinner meetupSpinnerAdapter =  new TitleSpinner(this, R.layout.title_spinner, meetupSections);
        mMeetupSpinner.setAdapter(meetupSpinnerAdapter);
        mMeetupSpinner.setSelection(0);
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
        mMeetupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                meetupsFragment.changeState(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    public void initApp() {
        Font.init(this);
        this.initActionBar();
        Store.init(this);
        Api.init(this);
        initScreens();
        this.open_loading();
        Assets.init(this);
        Me.init(this);
    }

    public void initScreens() {
        loadingFragment = new LoadingFragment();
        loginFragment = new LoginFragment();
        walkthroughFragment = new WalkthroughFragment();
        tabsFragment = new TabsFragment();
        meetupsFragment = new MeetupsFragment();
        meetupFragment = new MeetupFragment();
        profileFragment = new ProfileFragment();
        scheduleFragment = new ScheduleFragment();
        attendeeSearchFragment = new AttendeeSearchFragment();
        postFragment = new PostFragment();
        meetupAttendeesFragment = new MeetupAttendeesFragment();
        communitiesFragment = new CommunitiesFragment();
        filtersFragment = new FiltersFragment();
        dispatchContentFragment = new DispatchContentFragment();
        homeFragment= new HomeFragment();
        userNotesFragment = new UserNotesFragment();
        homeFragment.init();
    }

    public void startExperience() {
        //Store.set("walkthrough", "0");
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
            Log.i("WDS", "Show meetups");
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
        this.open_loading();
        Assets.sync(new Response.Listener<JSONObject>() {
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
            } else if (active == meetupsFragment) {
                title = "Meetups";
            } else if (active == homeFragment) {
                if (homeFragment.activeMeetup != null) {
                    title = "Dispatch: " + meetupFragment.event.what;
                }
            } else if (active == communitiesFragment) {
                title = "Communities";
            } else if (active == scheduleFragment) {
                title = "Your Schedule";
            } else if (active == filtersFragment) {
                title = "Dispatch Filters";
            } else if (active == userNotesFragment) {
                title = "User Notes";
            } else if (active == dispatchContentFragment) {
                title = "Conversation";
            } else if (active == meetupFragment) {
                title = this.meetupFragment.event.what;
            } else if (active == meetupAttendeesFragment) {
                title = "Attendees for "+this.meetupFragment.event.what;
            } else if (active == profileFragment) {
                title = " ";
            }
            this.titleShell.setVisibility(View.GONE);
            this.searchShell.setVisibility(View.GONE);
            mMeetupHead.setVisibility(View.GONE);
            if (title.length() > 0) {
                if (title.equals("Meetups")) {
                    mMeetupHead.setVisibility(View.VISIBLE);
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
    public void open_meetups() {
        open_tabs();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                meetupsFragment.willDisplay();
                tabsFragment.open(meetupsFragment, "meetups");
            }
        }, 10);
    }

    public void open_user_notes(String user_id) {
        userNotesFragment.setUser(user_id);
        open(userNotesFragment, "user_notes");
    }

    public void open_meetup_attendees(String meetup_id) {
        meetupAttendeesFragment.setMeetup(meetup_id);
        open(meetupAttendeesFragment, "meetup-attendees");
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

    public void open_dispatch_item(HashMap item) {
        dispatchContentFragment.setItem(item);
        open(dispatchContentFragment, "dispatchContent");
    }

    public void open_schedule() {
        this.scheduleFragment.willDisplay();
        this.tabsFragment.open(this.scheduleFragment, "schedule");
    }

    public void open_login() {
        open(this.loginFragment, true, "login");
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
    public void open_meetup(Event event) {
        this.meetupFragment.setEvent(event);
        open(this.meetupFragment, "meetup");
    }
    public void open_meetup() {
        open(this.meetupFragment, "meetup");
    }
    public void open_profile(Attendee atn) {
        this.profileFragment.setAttendee(atn);
        open(this.profileFragment, "meetups");
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


    public void open(Fragment frag, Boolean hideActionBar, String tag) {
        if(hideActionBar) {
            getActionBar().hide();
        }
        else getActionBar().show();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, frag, tag);
        if  (frag != loadingFragment) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
        if (!frags.containsKey(tag)) {
            frags.put(tag, frag);
        }
        if (frag != tabsFragment) {
            tabsActive = false;
            this.active = frag;
            updateTitle();
        }
        else {
            tabsActive = true;
        }
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

    public void findActive() {
        Iterator it = frags.entrySet().iterator();
        tabsActive = false;
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            Fragment f = (Fragment) pair.getValue();
            if (f.isVisible() && f != tabsFragment) {
                if (f == tabsFragment) {
                    tabsActive = true;
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
    public void onBackPressed() {
        // Fragment fragmentBeforeBackPress = getCurrentFragment();
        // Perform the usual back action
        if (tabsActive && tabsFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
            tabsFragment.getChildFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
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