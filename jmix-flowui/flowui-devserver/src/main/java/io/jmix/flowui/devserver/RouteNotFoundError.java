package io.jmix.flowui.devserver;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.AbstractRouteNotFoundError;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.internal.DefaultErrorHandler;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Tag(Tag.DIV)
@AnonymousAllowed
@DefaultErrorHandler
public class RouteNotFoundError extends AbstractRouteNotFoundError implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        // we have no routing in Studio designer, just ignore this errors
        return 200;
    }
}