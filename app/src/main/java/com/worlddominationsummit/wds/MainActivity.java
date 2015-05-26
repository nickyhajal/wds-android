package com.worlddominationsummit.wds;

import android.os.Handler;
import android.support.v4.app.Fragment;
//import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.android.volley.VolleyError;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

public class MainActivity extends FragmentActivity implements Runnable {

    public static MainActivity self;
    private Button search_close;
    private EditText search_inp;
    private FrameLayout contentLayout;
    private AttendeeSearcher searcher;
    private Boolean tabsStarted = false;
    public LoginFragment loginFragment;
    public LoadingFragment loadingFragment;
    public MeetupsFragment meetupsFragment;
    public AttendeeSearchFragment attendeeSearchFragment;
    public ScheduleFragment scheduleFragment;
    public HomeFragment homeFragment;
//    public ProfileFragment profileFragment;
    public MeetupFragment meetupFragment;
    public TabsFragment tabsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.self = this;
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        this.contentLayout = new FrameLayout(this);
        this.contentLayout.setId(R.id.frame_layout);
        this.setContentView(this.contentLayout);
        this.initApp();
        this.initSearch();
        this.startExperience();
    }

    public void initSearch(){
        final MainActivity ref = this;
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.attendee_search_bar);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        View bar = actionBar.getCustomView();
        this.search_inp = (EditText) bar.findViewById(R.id.search_inp);
        this.search_close = (Button) bar.findViewById(R.id.search_close);
        this.search_close.setTypeface(Font.use("Karla_Bold"));
        this.search_close.setVisibility(View.GONE);
        this.search_inp.setTypeface(Font.use("Karla"));
        this.searcher = new AttendeeSearcher(this, search_inp);
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
    }

    public void initApp() {
        Font.init(this);
        Store.init(this);
        Api.init(this);
        initScreens();
        this.open_loading();
        Assets.init(this);
        Me.init(this);
    }

    public void initScreens() {
        this.loginFragment = new LoginFragment();
        this.loadingFragment = new LoadingFragment();
        this.tabsFragment = new TabsFragment();
        this.meetupsFragment = new MeetupsFragment();
        this.meetupFragment = new MeetupFragment();
        this.scheduleFragment = new ScheduleFragment();
        this.attendeeSearchFragment = new AttendeeSearchFragment();
        this.homeFragment= new HomeFragment();
        this.homeFragment.init();
    }

    public void startExperience() {
        Store.set("walkthrough", "8");
        int step = Me.checkWalkthrough();
        if(step < 7) {
            this.open_walkthrough(step);
        }
        else {
            Me.checkLoggedIn();
        }
    }

    public void open_tabs() {
        if(this.tabsStarted) {
            Log.i("WDS", "Show meetups");
            open(this.tabsFragment);
        }
        else {
            Puts.i("START TABS");
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
                Handler handler = new Handler();
                handler.postDelayed(ref, 100);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ref.tabsStarted = true;
                ref.open_tabs();
                Handler handler = new Handler();
                handler.postDelayed(ref, 100);
            }
        });

    }

    public void run() {
        open_dispatch();
    }

    public void open_meetups() {
        this.meetupsFragment.willDisplay();
        this.tabsFragment.open(this.meetupsFragment);
    }

    public void open_dispatch() {
        this.homeFragment.willDisplay();
        this.tabsFragment.open(this.homeFragment);
    }

    public void open_schedule() {
        this.scheduleFragment.willDisplay();
        this.tabsFragment.open(this.scheduleFragment);
    }

    public void open_login() {
        open(this.loginFragment, true);
    }

    public void open_loading() {
        open(this.loadingFragment, true);
    }

    public void open(Fragment frag) {
        open(frag, false);
    }
    public void open(Fragment frag, Boolean hideActionBar) {
        if(hideActionBar) {
            getActionBar().hide();
        }
        else getActionBar().show();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, frag).addToBackStack(null).commit();
    }

    public void open_meetup(Event event) {
        this.meetupFragment.setEvent(event);
        open(this.meetupFragment);
    }

    public void open_search() {
        this.search_close.setVisibility(View.VISIBLE);
        open(this.attendeeSearchFragment);
    }

    public void close_search() {
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

    public void open_walkthrough(int step) {

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
            MainActivity.self.close_search();
        }
    };
}
