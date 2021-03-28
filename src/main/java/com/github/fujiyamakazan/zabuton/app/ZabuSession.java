package com.github.fujiyamakazan.zabuton.app;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;


public class ZabuSession extends WebSession {
    private static final long serialVersionUID = 1L;

    public ZabuSession(Request request) {
        super(request);
    }

    public static ZabuSession get() {
        return (ZabuSession)Session.get();
    }

}
