/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.OpenMode;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.Map;
import java.util.function.Consumer;

public interface EntityLinkField<V> extends Field<V>, Component.Focusable {

    String NAME = "entityLinkField";

    /**
     * @return an editor screen id
     */
    @Nullable
    String getScreen();

    /**
     * Sets an editor screen id.
     *
     * @param screen an editor screen id
     */
    void setScreen(@Nullable String screen);

    /**
     * @return open mode for editor screen
     */
    OpenMode getOpenMode();

    /**
     * Sets open mode for editor screen.
     *
     * @param openMode open mode
     */
    void setOpenMode(OpenMode openMode);

    @Nullable
    Map<String, Object> getScreenParams();

    void setScreenParams(@Nullable Map<String, Object> params);

    /**
     * Adds editor close listener.
     *
     * @param editorCloseListener a listener to set
     * @return subscription
     */
    Subscription addEditorCloseListener(Consumer<EditorCloseEvent> editorCloseListener);

    /**
     * @return click handler
     */
    @Nullable
    Consumer<EntityLinkField> getCustomClickHandler();

    /**
     * Sets a custom click handler to the field.
     *
     * @param clickHandler click handler
     */
    void setCustomClickHandler(@Nullable Consumer<EntityLinkField> clickHandler);

    /**
     * @return a field meta class
     */
    @Nullable
    MetaClass getMetaClass();

    /**
     * Sets field meta class.
     *
     * @param metaClass a field meta class
     */
    void setMetaClass(@Nullable MetaClass metaClass);

    /**
     * @return owner list component
     */
    @Nullable
    ListComponent getOwner();

    /**
     * Sets an owner list component {@link ListComponent} to the field.
     *
     * @param owner owner list component
     */
    void setOwner(ListComponent owner);

    /**
     * Describes editor close event.
     */
    class EditorCloseEvent<V> extends EventObject {
        protected EditorScreen screen;
        protected String actionId;

        public EditorCloseEvent(EntityLinkField<V> source, @Nullable EditorScreen screen, @Nullable String actionId) {
            super(source);
            this.screen = screen;
            this.actionId = actionId;
        }

        @Nullable
        public EditorScreen getEditorScreen() {
            return screen;
        }

        @Nullable
        public String getActionId() {
            return actionId;
        }

        @Override
        public EntityLinkField<V> getSource() {
            //noinspection unchecked
            return (EntityLinkField<V>) super.getSource();
        }
    }
}