package co.tophe.utils;

import junit.framework.TestCase;

import java.util.Date;

public class DateUtilsTest extends TestCase {

    private static final String HTTP_DATE1 = "Tue, 27 Jan 2015 08:57:51 UTC";
    private static final String HTTP_DATE2 = "Wed, 21 Jan 2015 08:42:00 +0000";
    private static final String HTTP_DATE3 = "Mon, 01 Aug 2011 09:42:29 -0700";
    private static final String HTTP_DATE4 = "Tue, 19 Oct 2004 13:38:55 -0400";
    private static final String HTTP_DATE5 = "Tue, 27 Jan 2015 22:23:39 GMT";
    private static final String HTTP_DATE6 = "Thu, 06 Nov 2014 00:01:00 PST";

    public void testParseDate1() throws Exception {
        Date date = DateUtils.parseDate(HTTP_DATE1);
        assertNotNull(date);
    }

    public void testParseDate2() throws Exception {
        Date date = DateUtils.parseDate(HTTP_DATE2);
        assertNotNull(date);
    }

    public void testParseDate3() throws Exception {
        Date date = DateUtils.parseDate(HTTP_DATE3);
        assertNotNull(date);
    }

    public void testParseDate4() throws Exception {
        Date date = DateUtils.parseDate(HTTP_DATE4);
        assertNotNull(date);
    }

    public void testParseDate5() throws Exception {
        Date date = DateUtils.parseDate(HTTP_DATE5);
        assertNotNull(date);
    }

    public void testParseDate6() throws Exception {
        Date date = DateUtils.parseDate(HTTP_DATE6);
        assertNotNull(date);
    }
}
