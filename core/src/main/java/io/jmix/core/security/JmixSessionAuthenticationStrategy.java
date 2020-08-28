package io.jmix.core.security;

import io.jmix.core.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JmixSessionAuthenticationStrategy implements SessionAuthenticationStrategy {

    @Autowired
    protected RememberMeServices rememberMeServices;

    @Autowired
    protected Events events;

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {
        SecurityContextHelper.setAuthentication(authentication);
        rememberMeServices.loginSuccess(request, response, authentication);
        events.publish(new InteractiveAuthenticationSuccessEvent(
                authentication, this.getClass()));
    }
}
