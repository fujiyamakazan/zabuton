package net.nanisl.zabuton.app;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class ZabuPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@SpringBean
	private ZabuTimer zabutimer;

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
}
