package com.worlddominationsummit.wdsandroid

import java.util.HashMap
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView

/**
 * Created by nicky on 5/18/15.
 */
object Color {
    private var fonts: HashMap<String, Typeface>? = null
    fun init(context: MainActivity) {
    }

    fun use(color: Int): Int {
        return MainActivity.self.resources.getColor(color)
    }

    fun Orange(): Int { return use(R.color.orange) }
    fun Red(): Int { return use(R.color.red) }
    fun Tan(): Int { return use(R.color.tan) }
    fun Tan90(): Int { return use(R.color.tan_90) }
    fun YellowishTan(): Int { return use(R.color.yellowish_tan) }
    fun DarkYellowTan(): Int { return use(R.color.dark_yellow_tan) }
    fun DarkGrayBlue(): Int { return use(R.color.dark_gray_blue) }
    fun DarkYellowTan50(): Int { return use(R.color.dark_yellow_tan_50) }
    fun MidTan(): Int { return use(R.color.mid_tan) }
    fun BrightTan(): Int { return use(R.color.bright_tan) }
    fun MediumTan(): Int { return use(R.color.medium_tan) }
    fun LightTan(): Int { return use(R.color.light_tan) }
    fun LightTan50(): Int { return use(R.color.light_tan_50) }
    fun Gray(): Int { return use(R.color.gray) }
    fun LightGray(): Int { return use(R.color.light_gray) }
    fun DarkGray(): Int { return use(R.color.dark_gray) }
    fun DarkGray35(): Int { return use(R.color.dark_gray_35) }
    fun OrangishGray(): Int { return use(R.color.orangish_gray) }
    fun Blue(): Int { return use(R.color.blue) }
    fun Green(): Int { return use(R.color.green) }
    fun BrightGreen(): Int { return use(R.color.bright_green) }
    fun SelGreen(): Int { return use(R.color.sel_green) }
    fun White(): Int { return use(R.color.white) }
    fun Coffee(): Int { return use(R.color.coffee) }
    fun Clear(): Int { return use(R.color.clear) }
    fun LightCoffee(): Int { return use(R.color.light_coffee) }
    fun YellowCanvas(): Int { return use(R.color.yellow_canvas) }
    fun WhiteCanvas(): Int { return use(R.color.white_canvas) }
    fun DarkCanvas(): Int { return use(R.color.dark_canvas) }

}
