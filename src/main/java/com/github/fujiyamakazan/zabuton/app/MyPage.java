package com.github.fujiyamakazan.zabuton.app;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * MyPageです。
 */
public class MyPage extends WebPage {

    private static final long serialVersionUID = 1L;

    @SpringBean
    private ZabuTimer zabutimer;

}
