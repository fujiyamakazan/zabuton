package com.github.fujiyamakazan.zabuton.wicket;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class ZabuApp extends WebApplication {

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ZabuApp.class);

    public static final String INIT_PARAM_TITLE = ZabuApp.class.getName() + ".PARAM_TITLE";
    public static final String URL_INFOPATH = "zabinfo";

    private String title;

    public String getTitle() {
        return this.title;
    }

    public static ZabuApp get() {
        return (ZabuApp) WebApplication.get();
    }

    private AnnotationConfigApplicationContext ctx;

    @Override
    protected void init() {
        super.init();

        /* 文字コード指定 */
        getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        /* init-parameter */
        this.title = getInitParameter(ZabuApp.INIT_PARAM_TITLE);

        /* 基本URLの設定 */
        mountPage("index", getHomePage());
        mountPage(URL_INFOPATH, ZabuInfoPage.class);

        /* SpringBeanを利用する */
        try  {
            ctx = new AnnotationConfigApplicationContext();
            ctx.scan(this.getClass().getPackageName()); // アプリケーションと同じ階層のコンポーネントをスキャンする
            ctx.scan(ZabuApp.class.getPackageName()); // ZabuAppと同じ階層のコンポーネントをスキャンする
            ctx.refresh();
            getComponentInstantiationListeners().add(new SpringComponentInjector(this, ctx));
        } catch (Exception e) {
            /* アプリケーション実装クラスがデフォルトパッケージの場合にエラーになったことがある */
            throw new RuntimeException(e);
        }

        /* XML利用時のヒント */
        //ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("Hibernate.xml",...);


    }

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        //return RuntimeConfigurationType.DEVELOPMENT;
        return RuntimeConfigurationType.DEPLOYMENT;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new WebSession(request);
    }

}
