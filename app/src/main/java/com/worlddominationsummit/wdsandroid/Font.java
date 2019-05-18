package com.worlddominationsummit.wdsandroid;

import java.util.HashMap;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by nicky on 5/18/15.
 */
public class Font {
    private static HashMap<String, Typeface> fonts;
    public static void init(MainActivity context) {
        Font.fonts = new HashMap<String, Typeface>();
        Font.fonts.put("Vitesse", Typeface.createFromAsset(context.getAssets(), "fonts/Produkt-Semibold-App.ttf"));
        Font.fonts.put("Vitesse_Light", Typeface.createFromAsset(context.getAssets(), "fonts/Produkt-Semibold-App.ttf"));
        Font.fonts.put("Vitesse_Medium", Typeface.createFromAsset(context.getAssets(), "fonts/Produkt-Semibold-App.ttf"));
        Font.fonts.put("Vitesse_Bold", Typeface.createFromAsset(context.getAssets(), "fonts/Produkt-Semibold-App.ttf"));
        Font.fonts.put("Karla", Typeface.createFromAsset(context.getAssets(), "fonts/Graphik-Medium-App.ttf"));
        Font.fonts.put("Karla_Bold", Typeface.createFromAsset(context.getAssets(), "fonts/Graphik-Semibold-App.ttf"));
        Font.fonts.put("Karla_Italic", Typeface.createFromAsset(context.getAssets(), "fonts/KarlaItalic.ttf"));
        Font.fonts.put("Karla_BoldItalic", Typeface.createFromAsset(context.getAssets(), "fonts/KarlaBoldItalic.ttf"));
    }
//    public static void applyTo(View view) {
//        ViewGroup parent = (ViewGroup) view;
//        int count = parent.getChildCount();
//        for (int i = 0; i <= count; i++) {
//            View v = parent.getChildAt(i);
//
//        }
//
//    }
    public static void applyTo(View view)
    {
        ViewGroup parent = (ViewGroup) view;
        for(int i = 0; i < parent.getChildCount(); i++)
        {
            View v = parent.getChildAt(i);
            if(v instanceof ViewGroup)
            {
                applyTo((ViewGroup)v);
            }
            else if(v != null)
            {
                Object raw_tag = v.getTag();
                if (raw_tag != null) {
                    String tag = raw_tag.toString();
                    if (!tag.equals("") && tag.contains("font_")) {
                        String font = tag.replace("font_", "");
                        if (v instanceof Button) {
                            ((Button) v).setTypeface(Font.use(font));
                        } else if (v instanceof EditText) {
                            ((EditText) v).setTypeface(Font.use(font));
                        } else if (v instanceof Switch) {
                            ((Switch) v).setTypeface(Font.use(font));
                        } else if (v instanceof TextView) {
                            ((TextView) v).setTypeface(Font.use(font));
                        }
                    }
                }
            }
        }
    }
    public static Typeface use(String fontName) {
        return Font.fonts.get(fontName);
    }
}
