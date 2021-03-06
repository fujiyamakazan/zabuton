package net.nanisl.zabuton.example;

import java.util.List;

import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.IModel;


public abstract class ZabuListView<T> extends PageableListView<T> {
	private static final long serialVersionUID = 1L;

	public ZabuListView(String id, IModel<? extends List<T>> model) {
		super(id, model, Long.MAX_VALUE);
	}

	public ZabuListView(String id, IModel<? extends List<T>> model, long itemsPerPage) {
		super(id, model, itemsPerPage);
	}

	public ZabuListView(final String id, final List<T> list) {
		super(id, list, Long.MAX_VALUE);
	}

	public ZabuListView(final String id, final List<T> list, final long itemsPerPage) {
		super(id, list, itemsPerPage);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setReuseItems(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		if (getModelObject().isEmpty()) {
			setVisible(false);
		}
	}

	public PagingNavigator createPager(String id) {

		PagingNavigator pager = new PagingNavigator(id, this) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (getItemCount() <= getItemsPerPage()) {
					setVisible(false);
				}
			}
		};

		return pager;
	}
}
