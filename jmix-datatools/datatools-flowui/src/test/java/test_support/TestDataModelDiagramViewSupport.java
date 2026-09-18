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

package test_support;

import io.jmix.datatoolsflowui.datamodel.DataModelDiagramViewSupport;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;

/**
 * Records the generated diagram instead of opening a view for it.
 */
public class TestDataModelDiagramViewSupport implements DataModelDiagramViewSupport {

    @Nullable
    protected byte[] openedDiagramData;

    @Override
    public void open(View<?> origin, byte[] diagramData) {
        openedDiagramData = diagramData;
    }

    @Nullable
    public byte[] getOpenedDiagramData() {
        return openedDiagramData;
    }
}
