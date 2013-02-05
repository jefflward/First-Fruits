package com.wardware.givingtracker.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest
{
    @Test
    public void testGetBestMatch()
    {
        final List<String> dataList = new ArrayList<String>();
        dataList.add("John Doe");
        dataList.add("Barney Fife");
        dataList.add("John Wayne");
        dataList.add("Jeff Ward");
        Assert.assertEquals("Barney Fife", StringUtils.getBestMatch("ne", dataList, false));
        Assert.assertEquals("John Doe", StringUtils.getBestMatch("hn", dataList, false));
        Assert.assertEquals("John Wayne", StringUtils.getBestMatch("Wa", dataList, false));
        Assert.assertEquals("Jeff Ward", StringUtils.getBestMatch("War", dataList, false));
    }
}
