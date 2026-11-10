/*
 * Copyright 2026 Haulmont.
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

package grapes_js_value;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import grapes_js_xml_load.view.GrapesJsXmlLoadTestView;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;
import io.jmix.messagetemplatesflowui.kit.component.event.dom.GrapesJsValueChangedDomEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.MessageTemplatesFlowuiTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UiTest(viewBasePackages = {"grapes_js_xml_load.view", "test_support.view"})
@SpringBootTest(classes = {MessageTemplatesFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class GrapesJsValueTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;

    @Test
    void setValue_document_keepsDoctypeHtmlAndHead() {
        GrapesJs grapesJs = new GrapesJs();

        grapesJs.setValue("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>${subject}</title>"
                + "<style>p { color: red; }</style></head>"
                + "<body style=\"margin: 0\"><#if name??><p>Hello ${name}</p></#if>"
                + "<a href=\"${unsubscribeUrl}\">Unsubscribe</a></body></html>");

        assertEquals("<!doctype html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>${subject}</title>"
                        + "<style>p { color: red; }</style></head>"
                        + "<body style=\"margin: 0\"><#if name??><p>Hello ${name}</p></#if>"
                        + "<a href=\"${unsubscribeUrl}\">Unsubscribe</a></body></html>",
                grapesJs.getValue());
    }

    @Test
    void setValue_document_removesUnsafeContent() {
        GrapesJs grapesJs = new GrapesJs();

        grapesJs.setValue("<html onload=\"steal()\"><head><script>steal()</script>"
                + "<meta http-equiv=\"Refresh\" content=\"0; url=https://example.com\">"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head>"
                + "<body onclick=\"steal()\"><p>Hello</p><script>steal()</script></body></html>");

        assertEquals("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head>"
                        + "<body><p>Hello</p></body></html>",
                grapesJs.getValue());
    }

    @Test
    void setValue_bodyContent_isNotTurnedIntoDocument() {
        GrapesJs grapesJs = new GrapesJs();

        grapesJs.setValue("<body><p>Hello</p></body><style>p { color: red; }</style>");

        assertEquals("<body><p>Hello</p><style>p { color: red; }</style></body>", grapesJs.getValue());
    }

    @Test
    void setValue_urlWithFreeMarker_isKept() {
        GrapesJs grapesJs = new GrapesJs();

        grapesJs.setValue("<body><a href=\"${url}\">link</a><img src=\"${imageUrl}\">"
                + "<a href=\"javascript:alert(1)\">script</a></body>");

        assertEquals("<body><a href=\"${url}\">link</a><img src=\"${imageUrl}\"><a>script</a></body>",
                grapesJs.getValue());
    }

    @Test
    void valueFromClient_escapedFreeMarker_isRestored() {
        GrapesJs grapesJs = new GrapesJs();

        ComponentUtil.fireEvent(grapesJs, new GrapesJsValueChangedDomEvent(grapesJs, true,
                "<body>&lt;#-- greeting --&gt;"
                        + "&lt;#if name??&gt;<p>Hello ${name}</p>&lt;/#if&gt;"
                        + "&lt;#if city??&gt;<p class=\"${city?string(&quot;a&quot;)}\">${a &amp;&amp; b}</p>&lt;/#if&gt;"
                        + "<p>1 &lt; 2</p></body>"));

        assertEquals("<body><#-- greeting -->"
                        + "<#if name??><p>Hello ${name}</p></#if>"
                        + "<#if city??><p class=\"${city?string(\"a\")}\">${a && b}</p></#if>"
                        + "<p>1 &lt; 2</p></body>",
                grapesJs.getValue());
    }

    @Test
    void valueToClient_freeMarker_isEscaped() {
        viewNavigationSupport.navigate(GrapesJsXmlLoadTestView.class);
        GrapesJsXmlLoadTestView view = UiTestUtils.getCurrentView();

        GrapesJs grapesJs = new GrapesJs();
        grapesJs.setValue("<body><#if name??><p>Hello ${a && b}</p></#if></body>");
        view.getContent().add(grapesJs);

        assertEquals("<body>&lt;#if name??&gt;<p>Hello ${a &amp;&amp; b}</p>&lt;/#if&gt;</body>",
                getValueSentToClient());
    }

    Object getValueSentToClient() {
        List<PendingJavaScriptInvocation> invocations = UI.getCurrent().getInternals()
                .dumpPendingJavaScriptInvocations();

        List<Object> parameters = invocations.stream()
                .filter(invocation -> invocation.getInvocation().getExpression().contains("updateValue"))
                .findFirst()
                .orElseThrow()
                .getInvocation()
                .getParameters();

        return parameters.get(parameters.size() - 1);
    }
}
