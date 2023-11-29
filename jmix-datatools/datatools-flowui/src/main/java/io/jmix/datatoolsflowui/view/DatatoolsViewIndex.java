/*
 * Copyright 2022 Haulmont.
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

package io.jmix.datatoolsflowui.view;

import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorDetailView;
import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorListView;
import io.jmix.flowui.view.index.ViewIndexEntry;
import io.jmix.flowui.view.index.ViewIndexEntryGroup;

import java.util.List;

/**
 * A list of views available in the datatools module. The index may be used while defining view access using the
 * simple-security module.
 */
public interface DatatoolsViewIndex {

    ViewIndexEntry entityInspectorDetailView = new ViewIndexEntry(EntityInspectorDetailView.ID);

    ViewIndexEntry entityInspectorListView = new ViewIndexEntry(EntityInspectorListView.ID);

    ViewIndexEntryGroup entityInspectorViewsGroup = new ViewIndexEntryGroup(
            List.of(
                    entityInspectorDetailView,
                    entityInspectorListView
            )
    );
}
