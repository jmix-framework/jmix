/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.kit.component;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonValue;
import io.jmix.messagetemplatesflowui.kit.component.event.dom.GrapesJsValueChangedDomEvent;
import io.jmix.messagetemplatesflowui.kit.component.serialization.JmixGrapesJsSerializer;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Tag("jmix-grapes-js")
@NpmPackage(value = "grapesjs", version = "0.21.13")
@NpmPackage(value = "grapesjs-blocks-basic", version = "1.0.2")
@NpmPackage(value = "grapesjs-custom-code", version = "1.0.2")
@NpmPackage(value = "grapesjs-blocks-flexbox", version = "1.0.1")
@NpmPackage(value = "grapesjs-plugin-forms", version = "2.0.6")
@NpmPackage(value = "grapesjs-preset-newsletter", version = "1.0.2")
@NpmPackage(value = "grapesjs-parser-postcss", version = "1.0.3")
@NpmPackage(value = "grapesjs-style-filter", version = "1.0.2")
@NpmPackage(value = "grapesjs-tabs", version = "1.0.6")
@NpmPackage(value = "grapesjs-tooltip", version = "0.1.8")
@NpmPackage(value = "grapesjs-tui-image-editor", version = "1.0.2")
@NpmPackage(value = "grapesjs-preset-webpage", version = "1.0.3")
@JsModule("./src/grapesjs/jmix-grapes-js.js")
@CssImport("grapesjs/dist/css/grapes.min.css")
@CssImport("./src/grapesjs/jmix-grapes-js.css")
public class JmixGrapesJs extends Component
        implements HasSize, HasStyle {

    protected JmixGrapesJsSerializer serializer;

    protected Collection<GrapesJsPlugin> plugins = new ArrayList<>();
    protected Collection<GrapesJsBlock> blocks = new ArrayList<>();

    protected StateTree.ExecutionRegistration synchronizeBlocksUpdateExecution;
    protected StateTree.ExecutionRegistration synchronizeValueUpdateExecution;

    protected String internalValue = getEmptyValue();
    protected Registration valueChangedDomRegistration;

    public JmixGrapesJs() {
        initComponent();
    }

    protected void initComponent() {
        serializer = createSerializer();
    }

    public void addPlugin(GrapesJsPlugin plugin) {
        Preconditions.checkArgument(plugin != null, "Null reference passed as parameter");
        plugins.add(plugin);
    }

    public void addBlock(GrapesJsBlock block) {
        Preconditions.checkArgument(block != null, "Null reference passed as parameter");
        blocks.add(block);

        updateBlocksOnClientSide();
    }

    public void setValue(String value) {
        setValueInternal(value, false);
    }

    public String getValue() {
        return internalValue;
    }

    public Registration addValueChangeEventListener(ComponentEventListener<GrapesJsValueChangedEvent> listener) {
        attachValueChangedEventListener();

        Registration registration = addListener(GrapesJsValueChangedEvent.class, listener);

        return () -> {
            registration.remove();

            if (!hasListener(GrapesJsValueChangedEvent.class) && valueChangedDomRegistration != null) {
                valueChangedDomRegistration.remove();
                valueChangedDomRegistration = null;
            }
        };
    }

    @ClientCallable
    protected JsonValue requestPlugins() {
        return serializer.serialize(plugins);
    }

    protected void setValueInternal(@Nullable String value, boolean fromClient) {
        String oldValue = getValue();
        value = nullToEmptyValue(value);

        if (Objects.equals(value, oldValue)) {
            return;
        }

        if (!fromClient) {
            updateValueOnClientSide();
        }

        internalValue = value;

        GrapesJsValueChangedEvent valueChangedEvent = createValueChangedEvent(oldValue, fromClient);
        ComponentUtil.fireEvent(this, valueChangedEvent);
    }

    protected void attachValueChangedEventListener() {
        if (valueChangedDomRegistration == null) {
            valueChangedDomRegistration = addListener(GrapesJsValueChangedDomEvent.class, this::onValueChangedDomEvent);
        }
    }

    protected void onValueChangedDomEvent(GrapesJsValueChangedDomEvent event) {
        setValueInternal(event.getValue(), true);
    }

    protected void updateBlocksOnClientSide() {
        // Do not call if it's still updating
        if (synchronizeBlocksUpdateExecution != null) {
            return;
        }

        getElement().getNode().runWhenAttached(ui ->
                synchronizeBlocksUpdateExecution = ui.beforeClientResponse(this, this::updateClientSideBlocks)
        );
    }

    protected void updateClientSideBlocks(ExecutionContext context) {
        getElement().callJsFunction("updateBlocks", serializer.serialize(blocks));
    }

    protected void updateValueOnClientSide() {
        // Do not call if it's still updating
        if (synchronizeValueUpdateExecution != null) {
            return;
        }

        getElement().getNode().runWhenAttached(ui ->
                synchronizeValueUpdateExecution = ui.beforeClientResponse(this, this::updateClientSideValue)
        );
    }

    protected void updateClientSideValue(ExecutionContext context) {
        getElement().callJsFunction("updateValue", internalValue);
    }

    protected GrapesJsValueChangedEvent createValueChangedEvent(@Nullable String oldValue, boolean fromClient) {
        return new GrapesJsValueChangedEvent(this, fromClient, getValue(), oldValue);
    }

    protected JmixGrapesJsSerializer createSerializer() {
        return new JmixGrapesJsSerializer();
    }

    protected String nullToEmptyValue(@Nullable String value) {
        return value == null ? getEmptyValue() : value;
    }

    protected String getEmptyValue() {
        return "<body></body>";
    }

    public static class GrapesJsValueChangedEvent extends ComponentEvent<JmixGrapesJs> {

        protected final String value;
        protected final String oldValue;

        public GrapesJsValueChangedEvent(JmixGrapesJs source, boolean fromClient,
                                         @Nullable String value, @Nullable String oldValue) {
            super(source, fromClient);

            this.value = value;
            this.oldValue = oldValue;
        }

        @Nullable
        public String getValue() {
            return value;
        }

        @Nullable
        public String getOldValue() {
            return oldValue;
        }
    }
}