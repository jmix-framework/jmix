package io.jmix.flowui.sys.vaadin;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Universe;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class SecurityContextHolderAtmosphereInterceptor implements AtmosphereInterceptor {

    @EventListener
    public void onReady(ApplicationReadyEvent event) {
        Universe.framework().interceptor(this);
    }

    @Override
    public Action inspect(AtmosphereResource atmosphereResource) {
        Object context = atmosphereResource.getRequest().getSession().getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (context instanceof SecurityContext) {
            SecurityContextHolder.setContext((SecurityContext) context);
        }
        return Action.CONTINUE;
    }

    @Override
    public void postInspect(AtmosphereResource atmosphereResource) {
        SecurityContext context = SecurityContextHolder.getContext();
        atmosphereResource.getRequest().getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        SecurityContextHolder.clearContext();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void configure(AtmosphereConfig config) {
    }
}
