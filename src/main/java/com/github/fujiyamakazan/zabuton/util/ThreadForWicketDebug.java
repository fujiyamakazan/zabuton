package com.github.fujiyamakazan.zabuton.util;

import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Locale;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;


/**
 * WicketのComponentをインスタンス化できるスレッドを作成します。
 * サーバーを起動しないで単体テストをするときに使います。
 *
 * @author fujiyama
 */
public abstract class ThreadForWicketDebug extends Thread {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ThreadForWicketDebug.class);

    public ThreadForWicketDebug() {
        super("Thread-ThreadForWicketDebug");
    }

    @Override
    public final void run() {

        WebApplication application = new WebApplication() {
            @Override
            public Class<? extends Page> getHomePage() {
                return WebPage.class;
            }
        };
        application.setName("test");
        MockServletContext servletContext = new MockServletContext(application, "dummyServletContext");
        application.setServletContext(servletContext);
        WicketFilter wicketFilter = new WicketFilter() {

            @Override
            protected IWebApplicationFactory getApplicationFactory() {
                return new ContextParamWebApplicationFactory() {

                    @Override
                    public WebApplication createApplication(WicketFilter filter) {
                        return application;
                    }

                };
            }
        };
        FilterConfig filterConfig = new FilterConfig() {

            @Override
            public String getFilterName() {
                return null;
            }

            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }

            @Override
            public String getInitParameter(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return null;
            }

        };
        try {
            wicketFilter.init(filterConfig);
        } catch (ServletException e1) {
            throw new RuntimeException(e1);
        }
        Request request = new Request() {

            @Override
            public Url getUrl() {
                return null;
            }

            @Override
            public Locale getLocale() {
                return Locale.JAPANESE;
            }

            @Override
            public Object getContainerRequest() {
                return new MockHttpServletRequest(application, null, servletContext);
            }

            @Override
            public Url getClientUrl() {
                return null;
            }

            @Override
            public Charset getCharset() {
                return null;
            }
        };
        Response response = new Response() {

            @Override
            public void write(byte[] array, int offset, int length) {
                /* 処理なし */
            }

            @Override
            public void write(byte[] array) {
                /* 処理なし */
            }

            @Override
            public void write(CharSequence sequence) {
                /* 処理なし */
            }

            @Override
            public Object getContainerResponse() {
                return null;
            }

            @Override
            public String encodeURL(CharSequence url) {
                return null;
            }
        };
        IRequestMapper requestMapper = new IRequestMapper() {
            @Override
            public IRequestHandler mapRequest(Request request) {
                return null;
            }

            @Override
            public Url mapHandler(IRequestHandler requestHandler) {
                return null;
            }

            @Override
            public int getCompatibilityScore(Request request) {
                return 0;
            }
        };
        IExceptionMapper exceptionMapper = new IExceptionMapper() {
            @Override
            public IRequestHandler map(Exception e) {
                return null;
            }
        };
        RequestCycleContext requestCycleContex = new RequestCycleContext(
            request, response, requestMapper, exceptionMapper);
        RequestCycle requestCycle = new RequestCycle(requestCycleContex);

        Session session = new WebSession(request);

        ThreadContext.setApplication(application);
        ThreadContext.setRequestCycle(requestCycle);
        ThreadContext.setSession(session);

        onRun();
    }

    protected abstract void onRun();


}
