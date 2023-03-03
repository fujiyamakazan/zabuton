package com.github.fujiyamakazan.zabuton.app.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.github.fujiyamakazan.zabuton.spring.ZabuTimer;


/**
 * HomePageです。
 */
public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;

    @SpringBean
    private ZabuTimer zabutimer;

}
