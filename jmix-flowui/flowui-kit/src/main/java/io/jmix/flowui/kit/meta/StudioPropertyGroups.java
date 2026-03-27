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

package io.jmix.flowui.kit.meta;

/**
 * Standard reusable {@link StudioPropertyGroup} definitions.
 */
public final class StudioPropertyGroups {

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    public interface Size {

    }
    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"})
            }
    )
    public interface SizeWithAutoAndFullOptions {

    }

    private StudioPropertyGroups() {
    }
}
