package com.wast3dmynd.tillr;

import com.wast3dmynd.tillr.utils.CurrencyUtility;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void isCurrencyCorrect()
    {
        assertEquals("R 15,00", CurrencyUtility.getCurrencyDisplay(15));
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}