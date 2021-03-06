package net.nanisl.zabuton.example;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;



public class ListdataManager<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;

//	private Collection<T> all;
	private Set<T> selected = new HashSet<T>();

	private Label selectedCountLabel;

	public ListdataManager(Collection<T> all) {
//		this.all = all;
	}

	public boolean isSelected(T data) {
		return selected.contains(data);
	}
	public void clearSelect() {
		selected.clear();
	}

	/**
	 * @return 選択済み件数を表示するラベル
	 */
	public Label getSelectedCountLabel(String id) {
		selectedCountLabel = new Label(id, new IModel<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				return String.valueOf(selected.size());
			}
		});
		selectedCountLabel.setOutputMarkupId(true);
		return selectedCountLabel;
	}

	/**
	 * @return 選択するためのチェックボックス
	 */
	public CheckBox getSelectCheckBox(String id, final T data) {

		/* 選択状態をチェックボックスから更新可能とするモデル */
		Model<Boolean> model = new Model<Boolean>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Boolean getObject() {
				return selected.contains(data);
			}

			@Override
			public void setObject(Boolean b) {
				selected.add(data);
			}
		};

		CheckBox checkbox = new CheckBox(id, model) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new OnChangeAjaxBehavior() {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(AjaxRequestTarget target) {

						/*
						 * 選択状態を即時反映する
						 */
						if (getModelObject()) {
							selected.add(data);
						} else {
							for (Iterator<T> it = selected.iterator(); it.hasNext();) {
								T d = it.next();
								if (d.equals(data)) {
									it.remove();
								}
							}
						}

						/*
						 * 選択済み件数の表示を更新する
						 */
						if (selectedCountLabel != null) {
							target.add(selectedCountLabel);
						}
					}
				});
			}
		};
		return checkbox;
	}

	public CheckBox getCheckGroupSelector(String id) {
		return null;

//		/* 選択状態をチェックボックスから更新可能とするモデル */
//		Model<Boolean> model = new Model<Boolean>() {
//			private static final long serialVersionUID = 1L;
//			@Override
//			public Boolean getObject() {
//				return selected.contains(data);
//			}
//
//			@Override
//			public void setObject(Boolean b) {
//				selected.add(data);
//			}
//		};


//		CheckBox checkbox = new CheckBox(id, model) {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			protected void onInitialize() {
//				super.onInitialize();
//				add(new OnChangeAjaxBehavior() {
//
//					private static final long serialVersionUID = 1L;
//
//					@Override
//					protected void onUpdate(AjaxRequestTarget target) {
//
//						/*
//						 * 選択状態を即時反映する
//						 */
//						if (getModelObject()) {
//							selected.add(data);
//						} else {
//							for (Iterator<T> it = selected.iterator(); it.hasNext();) {
//								T d = it.next();
//								if (d.equals(data)) {
//									it.remove();
//								}
//							}
//						}
//
//						/*
//						 * 選択済み件数の表示を更新する
//						 */
//						if (selectedCountLabel != null) {
//							target.add(selectedCountLabel);
//						}
//					}
//				});
//			}
//		};
//		return checkbox;
	}







}
