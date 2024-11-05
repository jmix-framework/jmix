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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * GrapesJS component is used to create HTML templates using a web editor. This implementation is based on
 * <a href="https://github.com/GrapesJS/grapesjs">GrapesJS JavaScript library</a>. <br/>
 * The component uses {@link GrapesJsBlock}s to build a template that can be reused. <br/>
 * The component supports {@link GrapesJsPlugin}s that can provide additional functionality such
 * as additional settings, visual appearance or blocks.
 */
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
public class JmixGrapesJs extends Component implements HasSize, HasStyle {

    private static final Logger log = LoggerFactory.getLogger(JmixGrapesJs.class);

    protected JmixGrapesJsSerializer serializer;

    protected Collection<GrapesJsPlugin> plugins = new ArrayList<>();
    protected Collection<GrapesJsBlock> blocks = new ArrayList<>();

    protected StateTree.ExecutionRegistration synchronizeBlocksUpdateExecution;
    protected StateTree.ExecutionRegistration synchronizeValueUpdateExecution;

    protected String internalValue = getEmptyValue();

    /**
     * Creates new instance of GrapesJS editor.
     */
    public JmixGrapesJs() {
        initComponent();
    }

    protected void initComponent() {
        serializer = createSerializer();

        addListener(GrapesJsValueChangedDomEvent.class, this::onValueChangedDomEvent);
    }

    /**
     * Adds a plugin to the GrapesJS editor. Plugins can only be modified before the client-side of the component
     * is initialized.
     *
     * @param plugin plugin to add
     */
    public void addPlugin(GrapesJsPlugin plugin) {
        Preconditions.checkArgument(plugin != null, "Null reference passed as parameter");
        plugins.add(plugin);
    }

    /**
     * Adds collection of plugins to the GrapesJS editor.
     *
     * @param plugins collection of plugins to add
     */
    public void addPlugins(Collection<GrapesJsPlugin> plugins) {
        Preconditions.checkArgument(plugins != null, "Null reference passed as parameter");

        if (plugins.isEmpty()) {
            log.info("An empty collection is passed for adding {} to the {}",
                    GrapesJsPlugin.class.getSimpleName(), getClass().getSimpleName());
            return;
        }
        this.plugins.addAll(plugins);
    }

    /**
     * Adds array of the plugins to the GrapesJS editor.
     *
     * @param plugins array of plugins to add
     */
    public void addPlugins(GrapesJsPlugin... plugins) {
        Preconditions.checkArgument(plugins != null, "Null reference passed as parameter");
        addPlugins(List.of(plugins));
    }

    /**
     * Adds a block to the GrapesJS editor.
     *
     * @param block block to add
     */
    public void addBlock(GrapesJsBlock block) {
        Preconditions.checkArgument(block != null, "Null reference passed as parameter");
        blocks.add(block);

        updateBlocksOnClientSide();
    }

    /**
     * Add collection of blocks to the GrapesJS editor.
     *
     * @param blocks collection of blocks to add
     */
    public void addBlocks(Collection<GrapesJsBlock> blocks) {
        Preconditions.checkArgument(blocks != null, "Null reference passed as parameter");

        if (blocks.isEmpty()) {
            log.info("An empty collection is provided for adding {} to the {}",
                    GrapesJsBlock.class.getSimpleName(), getClass().getSimpleName());
            return;
        }
        this.blocks.addAll(blocks);

        updateBlocksOnClientSide();
    }

    /**
     * Add array of blocks to the GrapesJS editor.
     *
     * @param blocks array of blocks to add
     */
    public void addBlocks(GrapesJsBlock... blocks) {
        Preconditions.checkArgument(blocks != null, "Null reference passed as parameter");
        addBlocks(List.of(blocks));
    }

    /**
     * Removes the block with passed ID from the GrapesJS editor.
     *
     * @param blockId ID of the block to remove
     */
    public void removeBlock(String blockId) {
        Preconditions.checkArgument(blockId != null, "Null reference passed as parameter");

        blocks.removeIf(block -> blockId.equals(block.getId()));
        callJsFunction("removeBlock", blockId);
    }

    /**
     * Removes the collection of blocks with passed IDs from the GrapesJS editor.
     *
     * @param blockIds collection of IDs of the blocks to remove
     */
    public void removeBlocks(Collection<String> blockIds) {
        Preconditions.checkArgument(blockIds != null, "Null reference passed as parameter");
        if (blocks.isEmpty()) {
            log.info("An empty collection is provided for removing {} to the {}",
                    GrapesJsBlock.class.getSimpleName(), getClass().getSimpleName());
            return;
        }

        blocks.removeIf(block -> blockIds.contains(block.getId()));
        callJsFunction("removeBlocks", serializer.serialize(blockIds));
    }

    /**
     * Removes the collection of blocks with passed IDs from the GrapesJS editor.
     *
     * @param blockIds array of IDs of the blocks to remove
     */
    public void removeBlocks(String... blockIds) {
        Preconditions.checkArgument(blockIds != null, "Null reference passed as parameter");
        removeBlocks(List.of(blockIds));
    }

    /**
     * Sets the passed HTML string as template value for the GrapesJS editor. <br/>
     * <b>Note:</b> {@code null} value will be converted to an empty HTML string (see {@link #getEmptyValue()}).
     *
     * @param value HTML string to set
     */
    public void setValue(@Nullable String value) {
        setValueInternal(value, false);
    }

    /**
     * @return HTML string from GrapesJS template
     */
    public String getValue() {
        return internalValue;
    }

    /**
     * Adds a value change listener. The listener is called when the value of GrapesJS is changed either by the user
     * on the client-side or programmatically.
     *
     * @param listener the value change listener, not null
     * @return a registration for the listener
     */
    public Registration addValueChangeEventListener(ComponentEventListener<GrapesJsValueChangedEvent> listener) {
        return addListener(GrapesJsValueChangedEvent.class, listener);
    }

    /**
     * Execute command for GrapesJS editor.
     *
     * @param command command to execute
     * @see <a href="https://grapesjs.com/docs/api/editor.html#runcommand">GrapesJS Docs - runCommand</a>
     */
    public void runCommand(String command) {
        callJsFunction("runCommand", command);
    }

    /**
     * Execute command for GrapesJS editor.
     *
     * @param command    command to execute
     * @param parameters parameters for command as a JSON string
     * @see <a href="https://grapesjs.com/docs/api/editor.html#runcommand">GrapesJS Docs - runCommand</a>
     */
    public void runCommand(String command, String parameters) {
        callJsFunction("runCommand", command, parameters);
    }

    /**
     * Stop the command for GrapesJS editor if stop method was provided.
     *
     * @param command command to stop
     * @see <a href="https://grapesjs.com/docs/api/editor.html#stopcommand">GrapesJS Docs - stopCommand</a>
     */
    public void stopCommand(String command) {
        callJsFunction("stopCommand", command);
    }

    /**
     * Stop the command for GrapesJS editor if stop method was provided.
     *
     * @param command    command to stop
     * @param parameters parameters for command as a JSON string
     * @see <a href="https://grapesjs.com/docs/api/editor.html#stopcommand">GrapesJS Docs - stopCommand</a>
     */
    public void stopCommand(String command, String parameters) {
        callJsFunction("stopCommand", command, parameters);
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
        synchronizeBlocksUpdateExecution = null;
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
        synchronizeValueUpdateExecution = null;
    }

    protected void callJsFunction(String functionName, Serializable... arguments) {
        getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, __ ->
                        getElement().callJsFunction(functionName, arguments)
                )
        );
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

    /**
     * Event is fired at the moment of updating the value of the {@link JmixGrapesJs} template.
     */
    public static class GrapesJsValueChangedEvent extends ComponentEvent<JmixGrapesJs> {

        protected final String value;
        protected final String oldValue;

        public GrapesJsValueChangedEvent(JmixGrapesJs source, boolean fromClient,
                                         String value, String oldValue) {
            super(source, fromClient);

            this.value = value;
            this.oldValue = oldValue;
        }

        /**
         * @return updated GrapesJS template value
         */
        public String getValue() {
            return value;
        }

        /**
         * @return previous GrapesJS template value
         */
        @Nullable
        public String getOldValue() {
            return oldValue;
        }
    }
}