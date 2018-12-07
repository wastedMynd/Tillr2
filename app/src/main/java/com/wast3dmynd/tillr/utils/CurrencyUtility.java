package com.wast3dmynd.tillr.utils;

import android.content.res.Resources;
import android.support.v4.os.ConfigurationCompat;
import android.text.InputFilter;
import android.text.Spanned;

import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyUtility {

    public static Locale getLocale() {
        return ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
    }

    public static String getCurrencySymbol() {
        Currency currency = Currency.getInstance(getLocale());
        return currency.getSymbol();
    }

    public static String getCurrencyDisplay(double price) {
        return String.format(getLocale(), "%s %.2f", getCurrencySymbol(), price);
    }

    public static double reformatCurrency(String price) {
        String cleanCurrency = price.replace(CurrencyUtility.getCurrencySymbol(), "").trim();
        return Double.valueOf(cleanCurrency.replace(",", "."));
    }

    public static class CurrencyFormatInputFilter implements InputFilter {

        Pattern mPattern = Pattern.compile("(0|[1-9]+[0-9]*)?(\\.[0-9]{0,2})?");

        @Override
        public CharSequence filter(
                CharSequence source,
                int start,
                int end,
                Spanned dest,
                int dstart,
                int dend) {

            String result =
                    dest.subSequence(0, dstart)
                            + source.toString()
                            + dest.subSequence(dend, dest.length());

            Matcher matcher = mPattern.matcher(result);

            if (!matcher.matches()) return dest.subSequence(dstart, dend);

            return null;
        }
    }
}
