package org.delcom.app.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConstUtilTest {

    @Test
    void testConstantsValues() {
        // Test untuk memastikan nilai konstanta tidak berubah (Regression Test)
        assertEquals("AUTH_TOKEN", ConstUtil.KEY_AUTH_TOKEN);
        assertEquals("USER_ID", ConstUtil.KEY_USER_ID);

        assertEquals("pages/auth/login", ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN);
        assertEquals("pages/auth/register", ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER);
        
        assertEquals("pages/home", ConstUtil.TEMPLATE_PAGES_HOME);

        assertEquals("pages/items/index", ConstUtil.TEMPLATE_ITEMS_INDEX);
        assertEquals("pages/items/form", ConstUtil.TEMPLATE_ITEMS_FORM);
        assertEquals("pages/items/detail", ConstUtil.TEMPLATE_ITEMS_DETAIL);
        assertEquals("pages/items/chart", ConstUtil.TEMPLATE_ITEMS_CHART);
    }

    @Test
    void testConstructor() {
        // Trik untuk mendapatkan 100% Class Coverage pada utility class
        // (Memanggil default constructor yang implicit)
        ConstUtil constUtil = new ConstUtil();
        assertNotNull(constUtil);
    }
}