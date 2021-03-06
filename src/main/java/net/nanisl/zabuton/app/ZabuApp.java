package net.nanisl.zabuton.app;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import net.nanisl.zabuton.Zabuton;

public abstract class ZabuApp extends WebApplication {

    @SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ZabuApp.class);

	public static void main(String[] args) {
		SpringApplication.run(ZabuApp.class);
	}

    public static final String URL_INFOPATH = "zabinfo";

//	@Autowired
//	private ApplicationContext applicationContext;

    private String title;

    public String getTitle() {
        return this.title;
    }

    public static ZabuApp get(){
         return (ZabuApp)WebApplication.get();
     }

    @Override
    final protected void init() {
        super.init();

        /* 文字コード指定 */
        getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        /* init-parameter */
        this.title = getInitParameter(Zabuton.PARAM_TITLE);

        /* 基本URLの設定 */
        mountPage("index", getHomePage());
        mountPage(URL_INFOPATH, ZabuInfoPage.class);

        /* SpringBeanを利用する */
        AnnotationConfigApplicationContext ctx;
        try {
        	ctx = new AnnotationConfigApplicationContext();
			ctx.scan(this.getClass().getPackageName()); // アプリケーションと同じ階層のコンポーネントをスキャンする
	        ctx.scan(ZabuApp.class.getPackageName()); // ZabuAppと同じ階層のコンポーネントをスキャンする
	        ctx.refresh();
			getComponentInstantiationListeners().add(new SpringComponentInjector(this, ctx));
		} catch (Exception e) {
			/* アプリケーション実装クラスがデフォルトパッケージの場合にエラーになったことがある */
			throw new RuntimeException(e);
		}
    }


	@Override
    public RuntimeConfigurationType getConfigurationType() {
        //return RuntimeConfigurationType.DEVELOPMENT;
        return RuntimeConfigurationType.DEVELOPMENT;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new ZabuSession(request);
    }


}
