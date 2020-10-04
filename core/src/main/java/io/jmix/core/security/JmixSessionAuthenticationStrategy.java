package io.jmix.core.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
    protected ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {
        SecurityContextHelper.setAuthentication(authentication);
        rememberMeServices.loginSuccess(request, response, authentication);
        applicationEventPublisher.publishEvent(
                new InteractiveAuthenticationSuccessEvent(authentication, this.getClass()));
    }
}
