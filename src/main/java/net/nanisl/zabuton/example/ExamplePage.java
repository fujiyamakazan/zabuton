package net.nanisl.zabuton.example;


import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.nanisl.zabuton.app.ZabuPage;
import net.nanisl.zabuton.app.login.LoginManager;

/**
 * 一覧データを操作する画面のサンプル
 *
 * ・ページング可能
 * ・複数選択可能
 * TODO
 * 　一括選択
 * 　Ajaxを使用したページング
 * 　並び替え
 * 　スライド
 * 　（複数の選択グループ）
 */
public class ExamplePage extends ZabuPage {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ExamplePage.class);

	@SpringBean
	private ExampleComponent exampleComponent;

	@SpringBean
	private LoginManager loginManager;


	private Model<String> modelUserId = Model.of();
	private Model<String> modelPassword = Model.of();

	@Override
	protected void onInitialize() {
		super.onInitialize();


		exampleComponent.setField1("abc");
		log.info(exampleComponent.getField1());

		add(new FeedbackPanel("feedback"));

		add(new Form<Void>("form") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onInitialize() {
				super.onInitialize();


				Model<String> modelLoginInfo = new Model<String>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {

                        boolean isLogin = loginManager.isLogin();

                        return isLogin ? "ログイン済みです" : "ログインしていません";
                    }

				};
                add(new Label("loginInfo", modelLoginInfo));


                add(new TextField<String>("userId", modelUserId));
                add(new PasswordTextField("password", modelPassword));

                /* ログインボタン */
                add(new Button("login") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onSubmit() {
                        super.onSubmit();

                        String id = modelUserId.getObject();
                        String password = modelPassword.getObject();

                        boolean valid = loginManager.inspectPassword(id, password);

                        if (valid) {
                            ExampleUser userInfo = new ExampleUser(id);
                            loginManager.login(userInfo);
                        } else {
                            error("ログインできません。");
                        }
                    }

                });

                /* ログアウトボタン */
                add(new Button("logout") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onInitialize() {
                        super.onInitialize();
                        setDefaultFormProcessing(false); // バリデータ無効
                    }

                    @Override
                    public void onSubmit() {
                        super.onSubmit();
                        loginManager.logout();
                    }
                });

                /* パスワード変更 */
                add(new Button("changePassword") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onSubmit() {
                        super.onSubmit();

                        if (loginManager.isLogin() == false) {
                            error("ログインしてください");
                            return;
                        }

                        String id = modelUserId.getObject();
                        String password = modelPassword.getObject();

                        if (StringUtils.equals(loginManager.getLoginUser().getId(), id) == false) {
                            error("ユーザIDが一致しません");
                            return;
                        }

                        /* パスワード変更実施 */
                        loginManager.changePassword(id, password);

                        success("変更しました");

                    }

                });


				final List<ExampleData> all = Generics.newArrayList();
				for (int i = 0; i < 23; i++) {
					all.add(new ExampleData(i, String.valueOf("name_" + i), "note_" + i));
				}

				final ListdataManager<ExampleData> listManager = new ListdataManager<ExampleData>(all);

				add(new WebMarkupContainer("dataset") {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onInitialize() {
						super.onInitialize();

						Label labelCount = listManager.getSelectedCountLabel("selectedCount");
						add(labelCount);

//						CheckGroup<ExampleData> group = new CheckGroup<ExampleData>("group", listManager.getSelected()); onchangeが利用できない
//						add(group);
//						group.add(new CheckGroupSelector("groupselector"));
//						add(listManager.getCheckGroupSelector("groupselector"));

						ZabuListView<ExampleData> listview = new ZabuListView<ExampleData>("lv", all, 15) {

							private static final long serialVersionUID = 1L;

							@Override
							protected void populateItem(ListItem<ExampleData> item) {

								ExampleData data = item.getModelObject();

								item.add(listManager.getSelectCheckBox("check", data));
								item.add(new Label("label", data.getName()));
							}

						};
//						group.add(listview);
						add(listview);
						add(listview.createPager("navigator"));
					}
				});

				add(new WebMarkupContainer("nodata") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onConfigure() {
						super.onConfigure();
						if (all.isEmpty() == false) {
							setVisible(false);
						}
					}
				});

				add(new Button("do") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit() {
						super.onSubmit();

						System.out.println("----------------------------");
						for (ExampleData data : all) {
							if (listManager.isSelected(data)) {
								System.out.println(data);
							}
						}

						listManager.clearSelect();
					}
				});
			}
		});
	}


}
