/*
 * Copyright 2015 John Ahlroos
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.ui.widget.addon.dragdroplayouts.interfaces;

import javax.annotation.Nullable;

public interface DragImageReferenceSupport {

    /**
     * Set a component as a drag image for a component in the layout. The drag
     * image will be shown instead of the component when the user drag a
     * component in the layout.
     * 
     * @param provider
     *            The image provider
     * 
     */
    void setDragImageProvider(@Nullable DragImageProvider provider);

    /**
     * Returns the drag image provider
     * 
     * @return the image provider
     */
    @Nullable
    DragImageProvider getDragImageProvider();
}
