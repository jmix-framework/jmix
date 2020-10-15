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

import io.jmix.ui.screen.FrameOwner;

public interface RelatedEntitiesSupport {

    /**
     * Creates a related screen builder.
     * <p>
     * Note, it is necessary to set MetaClass or entity class and property or MetaProperty to builder.
     * <p>
     * Example of creating screen with entity class and property:
     * <pre>{@code
     *      RelatedEntitiesBuilder builder = RelatedEntitiesSupport.builder(this);
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

}
