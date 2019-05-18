package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.TimeZone;


/**
 * Created by nicky on 5/19/15.
 */
public class DispatchAdapter extends ArrayAdapter<HashMap>{
    public ImageLoader mImageLoader;
    public Boolean isLoading = false;
    public HomeFragment mContext;
    public Handler mCounterHandler = new Handler();
    public int mTypes = 1;
    public DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .considerExifParams(true)
            .showImageOnLoading(R.drawable.gray_dots)
            .build();
    public ArrayList<String> mAvailTypes;
    //    public int mAtnPreShown = 0;
//    public int mAtnShell = 0;
//    public int mMsgShell = 0;
    public DispatchAdapter(Context context, ArrayList<HashMap> items) {
        super(context, 0, items);
        mImageLoader = ImageLoader.getInstance(); //(context.getApplicationContext());
        mAvailTypes = new ArrayList<>();
        mAvailTypes.add("item");
        mAvailTypes.add("update");
        mAvailTypes.add("attendee-stories");
        mAvailTypes.add("preorder");
        mAvailTypes.add("closed-preorder");
        mAvailTypes.add("postorder");
    }
    public String getSince() {
        if (getCount() > 0) {
            HashMap<String, String> item = getItem(0);
            if (item != null) {
                return String.valueOf(getItem(0).get("feed_id"));
            }
            else {
                return "0";
            }
        }
        else {
            return "0";
        }
    }

    @Override
    public int getViewTypeCount() {
        return mTypes;
    }
    @Override
    public int getItemViewType(int position) {
        final HashMap<String, Object> item = getItem(position);
        int i = 0;
        for (String t : mAvailTypes) {
            if (item.get("type") != null && item.get("type").equals(t)) {
                return 1;
            }
            i += 1;
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DispatchAdapter ref = this;
        final ViewHolder holder;
        final int p = position;
        final HashMap itemMap = getItem(position);
        if (itemMap.get("type") != null) {
            return getSpecialTile(itemMap, convertView, parent);
        } else {
            float percent = (float) p / this.getCount();
            if (percent > 0.75f && !this.isLoading) {
                this.isLoading = true;
                String before = String.valueOf((getItem(getCount() - 1).get("feed_id")));
                mContext.load_more(before, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject rsp) {
                        ref.isLoading = false;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ref.isLoading = false;
                        MainActivity.offlineAlert();
                    }
                });
            }
            final DispatchItem item = DispatchItem.fromHashMap(getItem(position));
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dispatch_row, parent, false);
                holder = new ViewHolder();
                holder.card = (LinearLayout) convertView.findViewById(R.id.card);
                holder.buttons = (LinearLayout) convertView.findViewById(R.id.buttons);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.name.setTypeface(Font.use("Vitesse_Bold"));
                holder.timestamp = (RelativeTimeTextView) convertView.findViewById(R.id.timestamp);
                holder.timestamp.setTypeface(Font.use("Karla"));
                holder.channel = (TextView) convertView.findViewById(R.id.channel);
                holder.channel.setTypeface(Font.use("Karla_Italic"));
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                holder.media = (ImageView) convertView.findViewById(R.id.media);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.content.setTypeface(Font.use("Karla"));
                holder.num_likes = (TextView) convertView.findViewById(R.id.num_likes);
                holder.num_likes.setTypeface(Font.use("Karla_Bold"));
                holder.num_comments = (TextView) convertView.findViewById(R.id.num_comments);
                holder.num_comments.setTypeface(Font.use("Karla_Bold"));
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            if (holder.media != null) {
                if (item.media != null && !item.media.equals("null") && item.media.length() > 0) {
                    mImageLoader.displayImage("https://photos.wds.fm/media/" + item.media + "_large", holder.media, mDisplayImageOptions);
                    holder.media.setVisibility(View.VISIBLE);
                } else {
                    holder.media.setVisibility(View.GONE);
                }
            }
            if (holder.avatar != null) {
                holder.avatar.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.default_avatar));
                mImageLoader.displayImage(item.author.getPic(120), holder.avatar);
            }
            holder.name.setText(item.author.full_name);
            holder.content.setText(item.content);
            Linkify.addLinks(holder.content, Linkify.WEB_URLS);
            holder.channel.setText(" - " + item.channel);
            holder.timestamp.setReferenceTime(item.created_at);
            holder.num_likes.setText(item.num_likes_str);
            holder.num_likes.setTag(item.feed_id);
            holder.num_comments.setText(item.num_comments_str);
            holder.num_comments.setTag(item.feed_id);
            holder.num_likes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String feed_id = (String) v.getTag();
                    if (feed_id != null) {
                        Me.toggleLike(feed_id, new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject rsp) {
                                getItem(p).put("num_likes", rsp.optString("num_likes"));
                                //holder.num_likes.setText(rsp.optString("num_likes"));
                                //item.num_likes = rsp.optString("num_likes");
                                ref.notifyDataSetChanged();
                            }
                        }, new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                MainActivity.offlineAlert();
                            }
                        });
                    }
                }
            });
            holder.num_comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_dispatch_item(itemMap);
                }
            });
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_profile(item.author);
                }
            });
            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_profile(item.author);
                }
            });
            return convertView;
        }
    }

    public View getSpecialTile(final HashMap<String, Object> item, View convertView, ViewGroup parent) {
        String type = (String) item.get("type");
        if (type.equals("update")) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dispatch_generic_row, parent, false);
                holder.bg = (RelativeLayout) convertView.findViewById(R.id.bg);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.content.setTypeface(Font.use("Karla_BoldItalic"));
                holder.icon.setImageResource(MainActivity.self.getImage("download"));
                holder.content.setText("\uD83C\uDF89   Get the New App Update!");
                holder.bg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String appPackageName = MainActivity.self.getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            MainActivity.self.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            MainActivity.self.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
        } else if (type.equals("announce")) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dispatch_generic_row, parent, false);
                holder.bg = (RelativeLayout) convertView.findViewById(R.id.bg);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.content.setTypeface(Font.use("Karla_BoldItalic"));
                holder.content.setText(((HashMap<String, String>)item.get("msg")).get("message"));
                holder.bg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String appPackageName = MainActivity.self.getPackageName(); // getPackageName() from Context or Activity object
                        String link = ((HashMap<String, String>)item.get("msg")).get("link");
                        if (link != null && link.length() > 0) {
                            MainActivity.self.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(((HashMap<String, String>)item.get("msg")).get("link"))));
                        }
                    }
                });
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
        } else if (type.equals("attendee-stories")) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dispatch_icon_double_row, parent, false);
                holder.bg = (RelativeLayout) convertView.findViewById(R.id.bg);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.sub = (TextView) convertView.findViewById(R.id.sub);
                holder.emoji = (ImageView) convertView.findViewById(R.id.emoji);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.content.setTypeface(Font.use("Karla_BoldItalic"));
                holder.sub.setTypeface(Font.use("Karla_Italic"));
                holder.icon.setImageResource(MainActivity.self.getImage("chevron_right_brown"));
                holder.content.setText("Share Your Attendee Story");
//                holder.emoji.setText("\uD83C\uDF99 ");
                holder.emoji.setImageResource(MainActivity.self.getImage("mic"));
                holder.sub.setText("Submissions close Saturday at 2:00pm");
                holder.bg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.self.open_atnstory();
                    }
                });
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
        } else if (type.equals("closed-preorder")) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dispatch_generic_row, parent, false);
                holder.bg = (RelativeLayout) convertView.findViewById(R.id.bg);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.content.setTypeface(Font.use("Karla_BoldItalic"));
                holder.icon.setImageResource(MainActivity.self.getImage("chevron_right_brown"));
                holder.content.setText("\uD83C\uDF86   Pre-order for WDS 2019 & 2020!");
                holder.bg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Store.set("preorder19", "");
                        MainActivity.self.homeFragment.update_items();
                    }
                });
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
        } else if (type.equals("preorder")) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dispatch_preorder_row, parent, false);
                holder.icon = (ImageView) convertView.findViewById(R.id.preimg);
                holder.button = (Button) convertView.findViewById(R.id.submit);
                holder.button.setTypeface(Font.use("Vitesse_Bold"));
                holder.doubleShell = (LinearLayout) convertView.findViewById(R.id.doubleShell);
                holder.singleShell = (LinearLayout) convertView.findViewById(R.id.singleShell);
                holder.doubleSoldOut = (ImageView) convertView.findViewById(R.id.doubleSoldOut);
                holder.singleSoldOut = (ImageView) convertView.findViewById(R.id.singleSoldOut);
                holder.doubleTxt = (TextView) convertView.findViewById(R.id.doubleContent);
                holder.doubleTxt.setTypeface(Font.use("Vitesse_Bold"));
                holder.singleTxt = (TextView) convertView.findViewById(R.id.singleContent);
                holder.singleTxt.setTypeface(Font.use("Vitesse_Bold"));
                holder.close = (Button) convertView.findViewById(R.id.close);
                holder.close.setTypeface(Font.use("Karla_BoldItalic"));
                holder.counter = (TextView) convertView.findViewById(R.id.counter);
                holder.counter.setTypeface(Font.use("Karla_BoldItalic"));
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        HashMap<String, String> prod = new HashMap<>();
//                        prod.put("name", "WDS 2018");
//                        prod.put("descr", "360 Ticket to WDS 2018");
//                        MainActivity.self.open_cart("wds2018", prod);
                        MainActivity.self.open_ticketChoice();
                    }
                });
                holder.close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCounterHandler.removeCallbacksAndMessages(null);
                        Store.set("preorder19", "closed");
                        MainActivity.self.homeFragment.update_items();
                    }
                });
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (MainActivity.state != null && MainActivity.state.get("pre") != null) {
                HashMap<String, Object> pre = (HashMap) MainActivity.state.get("pre");
                if (pre.get("double_soldout") != null) {
                    long double_soldout = (Long) pre.get("double_soldout");
                    if (double_soldout > 0) {
                        holder.doubleShell.setAlpha(0.4f);
                        holder.doubleSoldOut.setVisibility(View.VISIBLE);
                    } else {
                        holder.doubleShell.setAlpha(1f);
                        holder.doubleSoldOut.setVisibility(View.GONE);
                    }
                }
                if (pre.get("single_soldout") != null) {
                    long single_soldout = (Long) pre.get("single_soldout");
                    if (single_soldout > 0) {
                        holder.singleShell.setAlpha(0.4f);
                        holder.singleSoldOut.setVisibility(View.VISIBLE);
                    } else {
                        holder.singleShell.setAlpha(1f);
                        holder.singleSoldOut.setVisibility(View.GONE);
                    }
                }
            }
            mCounterHandler.removeCallbacksAndMessages(null);
            doCounter(holder.counter);
//            doRealtime();
            Display display = MainActivity.self.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            holder.icon.getLayoutParams().height = (int) (size.x * 0.4176f);
            holder.icon.requestLayout();
        } else if (type.equals("postorder")) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dispatch_postorder_row, parent, false);
                holder.bg = (RelativeLayout) convertView.findViewById(R.id.bg);
                holder.icon = (ImageView) convertView.findViewById(R.id.preimg);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.button = (Button) convertView.findViewById(R.id.submit);
                holder.button.setTypeface(Font.use("Vitesse_Bold"));
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Store.set("preorder19", "done");
                        if (MainActivity.self.homeFragment != null) {
                            MainActivity.self.homeFragment.update_items();
                        }
                    }
                });
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            Display display = MainActivity.self.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            holder.icon.getLayoutParams().height = (int) (size.x * 0.787f);
            holder.icon.requestLayout();
        }
        return convertView;
    }

    public long now() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public void doCounter(final TextView cd) {
        if (MainActivity.state != null && MainActivity.state.get("pre") != null) {
            HashMap<String, Object> pre = (HashMap) MainActivity.state.get("pre");
            if (pre.get("ends") != null) {
                long now = now();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    TimeZone tz = TimeZone.getDefault();
//                    long offsetFromUtc = tz.getOffset(now.getTime());
                    long end = formatter.parse((String) pre.get("ends")).getTime();
                    long diff = (end - now) / 1000;
                    if (diff < 0) {
                        diff = 0;
                    }
                    String hours = String.valueOf((int) Math.floor(diff / 3600));
                    diff = diff % 3600;
                    String mins = String.valueOf((int) Math.floor(diff / 60));
                    String seconds = String.valueOf((int) diff % 60);
                    cd.setText(hours+"h "+mins+"m "+seconds+"s");
                } catch (ParseException e) {
                    Log.e("WDS", "Parse Exception", e);
                }
            }
        }
        mCounterHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doCounter(cd);
            }
        }, 1000);
    }
//    public void doRealtime() {
//        Puts.i(MainActivity.pre.toString());
//        if (mAtnPreShown > 4) {
//            showRealtimeMessage();
//        } else {
//            if (MainActivity.pre != null) {
//                HashMap<String, ArrayList> pres = MainActivity.pre;
//                HashMap<String, String> atn = null;
//                if (pres.get("fresh") != null && pres.get("fresh").size() > 0) {
//                    ArrayList<HashMap> fresh = (ArrayList) MainActivity.pre.get("fresh");
//                    ArrayList<HashMap> used = (ArrayList) MainActivity.pre.get("used");
//                    atn = fresh.remove(fresh.size() - 1);
//                    used.add(atn);
//                    MainActivity.pre.put("fresh", fresh);
//                    MainActivity.pre.put("used", used);
//                } else if (pres.get("used") != null && pres.get("used").size() > 0) {
//                    ArrayList<HashMap> used = (ArrayList) MainActivity.pre.get("used");
//                    Random rand = new Random();
//                    int inx = rand.nextInt(used.size());
//                    atn = used.get(inx);
//                }
//                if (atn == null) {
//                    showRealtimeMessage();
//                } else {
//                    showAttendeeMessage(atn);
//                }
//            }
//        }
//    }
//    public void showRealtimeMessage() {
//        mAtnPreShown = 0;
//    }
//    public void showAttendeeMessage(HashMap<String, String> atn) {
//        mAtnPreShown += 1;
//        String avatar = "http://avatar.wds.fm/"+atn.get("user_id")+"?width=48";
//        String msg = atn.get("name")+" will be at WDS 2017!";
//        String ago = DateUtils.getRelativeTimeSpanString(Long.parseLong(atn.get("created_at"))).toString();
//
//    }

    private class ViewHolder {
        private TextView name;
        private TextView channel;
        private LinearLayout card;
        private LinearLayout buttons;
        private RelativeLayout bg;
        private RelativeTimeTextView timestamp;
        private ImageView avatar;
        private ImageView media;
        private ImageView icon;
        private TextView content;
        private TextView doubleTxt;
        private TextView singleTxt;
        private LinearLayout doubleShell;
        private LinearLayout singleShell;
        private ImageView doubleSoldOut;
        private ImageView singleSoldOut;
        private TextView sub;
        private ImageView emoji;
        private TextView num_likes;
        private TextView num_comments;
        private Button button;
        private Button close;
        private TextView counter;
    }

}

