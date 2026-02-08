/*
 * Copyright 2025 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.devserver.compatibility;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouteNotFoundError;
import com.vaadin.flow.router.internal.DefaultErrorHandler;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Tag(Tag.DIV)
@AnonymousAllowed
@DefaultErrorHandler
public class JmixRouteNotFoundError extends RouteNotFoundError {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        int result = super.setRouteNotFoundErrorParameter(event, parameter);
        addStudioCompatibilityNotification();
        return result;
    }

    private void addStudioCompatibilityNotification() {
        String studioCompatibilityMessage = "Route not found. Please update Jmix Studio to version 2.5.1 or above";
        Span studioCompatibilityMessageSpan = new Span(studioCompatibilityMessage);
        Style style = studioCompatibilityMessageSpan.getStyle();
        style.setFontWeight(Style.FontWeight.BOLD);
        style.setColor("oklch(.59 .2 25)"); // same as aura red
        style.set("margin", "0.5rem");

        VerticalLayout container = new VerticalLayout(studioCompatibilityMessageSpan);
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        getElement().setChild(0, container.getElement());
    }
}