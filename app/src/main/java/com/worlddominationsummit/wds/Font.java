package com.worlddominationsummit.wds;

import java.util.HashMap;
import android.graphics.Typeface;

/**
 * Created by nicky on 5/18/15.
 */
public class Font {
    private static HashMap<String, Typeface> fonts;
    public static void init(MainActivity context) {
        Font.fonts = new HashMap<String, Typeface>();
        Font.fonts.put("Vitesse", Typeface.createFromAsset(context.getAssets(), "fonts/VitesseBook.otf"));
        Font.fonts.put("Vitesse_Light", Typeface.createFromAsset(context.getAssets(), "fonts/VitesseLight.otf"));
        Font.fonts.put("Vitesse_Medium", Typeface.createFromAsset(context.getAssets(), "fonts/VitesseMedium.otf"));
        Font.fonts.put("Vitesse_Bold", Typeface.createFromAsset(context.getAssets(), "fonts/VitesseBold.otf"));
        Font.fonts.put("Karla", Typeface.createFromAsset(context.getAssets(), "fonts/KarlaRegular.ttf"));
        Font.fonts.put("Karla_Bold", Typeface.createFromAsset(context.getAssets(), "fonts/KarlaBold.ttf"));
        Font.fonts.put("Karla_Italic", Typeface.createFromAsset(context.getAssets(), "fonts/KarlaItalic.ttf"));
    }
    public static Typeface use(String fontName) {
        return Font.fonts.get(fontName);
    }
}
