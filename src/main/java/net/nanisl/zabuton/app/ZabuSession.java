package net.nanisl.zabuton.app;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

import net.nanisl.zabuton.app.login.LoginUser;


public class ZabuSession extends WebSession {
    private static final long serialVersionUID = 1L;

    public ZabuSession(Request request) {
        super(request);
    }

    public static ZabuSession get() {
        return (ZabuSession)Session.get();
    }

    private LoginUser loginUser;

    public LoginUser getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(LoginUser loginUser) {
        this.loginUser = loginUser;
    }
}
