package com.wast3dmynd.tillr.utils;

import android.graphics.Color;

import java.util.ArrayList;

public class ColorGenerator {

    private static final ArrayList<Integer> colors = new ArrayList<>();

    /**
     * @return integer color,
     * generated based on {@code RandomUtils.randInt()};
     * that's passed to {@code Color.rgb()} params.
     **/
    public static int generateColor()
    {
        final int range = 255;

        int rRed = RandomUtils.randomInt(range),
                rGreen = RandomUtils.randomInt(range),
                rBlue = RandomUtils.randomInt(range);

        int color = Color.rgb(rRed, rGreen, rBlue);
        if(colors.contains(color)) generateColor();
        colors.add(color);
        return color;
    }
}
