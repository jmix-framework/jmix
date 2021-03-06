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

package io.jmix.ui.relatedentities;

import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.FrameOwner;

import java.util.Collection;
import java.util.Map;

public interface RelatedEntitiesAPI {

    String NAME = "jmix_RelatedEntities";

    /**
     * Creates a related screen builder.
     * <p>
     * Note, it is necessary to set MetaClass or entity class and property or MetaProperty to builder.
     * <p>
     * Example of creating screen with entity class and property:
     * <pre>{@code
     *      RelatedEntitiesBuilder builder = relatedEntitiesAPI.builder(this);
     *      Screen colourBrowser = builder
     *              .withEntityClass(Car.class)
     *              .withProperty("colour")
     *              .withSelectedEntities(carsTable.getSelected())
     *              .withScreenClass(ColourBrowser.class)
     *              .build();
     *      colourBrowser.show();}
     * </pre>
     *
     * @param frameOwner invoking screen
     * @return builder instance
     */
    RelatedEntitiesBuilder builder(FrameOwner frameOwner);

    /**
     * Shows found related entities in default browse screen.
     *
     * @param selectedEntities set of entities which represents one side of relation
     * @param metaClass        metaClass of single entity from <code>selectedEntities</code>
     * @param metaProperty     chosen field to find related entities. Can be obtained from <code>metaClass</code>
     * @deprecated Use {@link #builder(FrameOwner)} to create and set up screen for related entities.
     */
    @Deprecated
    void openRelatedScreen(Collection<? extends JmixEntity> selectedEntities, MetaClass metaClass, MetaProperty metaProperty);

    /**
     * Shows found related entities in chosen screen.
     *
     * @param selectedEntities set of entities which represents one side of relation
     * @param metaClass        metaClass of single entity from <code>selectedEntities</code>
     * @param metaProperty     chosen field to find related entities. Can be obtained from <code>metaClass</code>
     * @param descriptor       descriptor contains screen id, {@code OpenType} and
     *                         generated filter caption
     * @deprecated Use {@link #builder(FrameOwner)} to create and set up screen for related entities.
     */
    @Deprecated
    void openRelatedScreen(Collection<? extends JmixEntity> selectedEntities, MetaClass metaClass, MetaProperty metaProperty,
                           RelatedScreenDescriptor descriptor);

    /**
     * Shows found related entities in default browse screen.
     *
     * @param selectedEntities set of entities which represents one side of relation
     * @param clazz            class of single entity from <code>selectedEntities</code>
     * @param property         chosen field to find related entities
     * @deprecated Use {@link #builder(FrameOwner)} to create and set up screen for related entities.
     */
    @Deprecated
    <T extends JmixEntity> void openRelatedScreen(Collection<T> selectedEntities, Class<T> clazz, String property);

    /**
     * Shows found related entities in chosen screen.
     *
     * @param selectedEntities set of entities which represents one side of relation
     * @param clazz            class of single entity from <code>selectedEntities</code>
     * @param property         chosen field to find related entities
     * @param descriptor       descriptor contains screen id, {@code OpenType} and
     *                         generated filter caption
     * @deprecated Use {@link #builder(FrameOwner)} to create and set up screen for related entities.
     */
    @Deprecated
    <T extends JmixEntity> void openRelatedScreen(Collection<T> selectedEntities, Class<T> clazz, String property,
                                                  RelatedScreenDescriptor descriptor);

    /**
     * @deprecated Use {@link #builder(FrameOwner)} to create and set up screen for related entities.
     */
    @Deprecated
    class RelatedScreenDescriptor {

        protected String screenId;
        protected OpenType openType = OpenType.THIS_TAB;
        protected String filterCaption;
        protected Map<String, Object> screenParams;

        public RelatedScreenDescriptor(String screenId, OpenType openType) {
            this.screenId = screenId;
            this.openType = openType;
        }

        public RelatedScreenDescriptor() {
        }

        public String getScreenId() {
            return screenId;
        }

        public OpenType getOpenType() {
            return openType;
        }

        public String getFilterCaption() {
            return filterCaption;
        }

        public Map<String, Object> getScreenParams() {
            return screenParams;
        }

        public void setScreenId(String screenId) {
            this.screenId = screenId;
        }

        public void setOpenType(OpenType openType) {
            this.openType = openType;
        }

        public void setFilterCaption(String filterCaption) {
            this.filterCaption = filterCaption;
        }

        public void setScreenParams(Map<String, Object> screenParams) {
            this.screenParams = screenParams;
        }
    }
}
