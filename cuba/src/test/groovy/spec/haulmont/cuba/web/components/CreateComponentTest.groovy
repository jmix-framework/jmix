/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.web.components

import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.components.mainwindow.FoldersPane
import com.haulmont.cuba.web.gui.components.CubaDataLoadCoordinator
import io.jmix.ui.Facets
import io.jmix.ui.component.RelatedEntities
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.web.UiScreenSpec
import spock.lang.Unroll

@SuppressWarnings("GroovyAccessibility")
class CreateComponentTest extends UiScreenSpec {

    @Autowired
    Facets facets

    @Unroll
    def "create standard UI component: '#name' with CubaUiComponents"() {
        expect:
        cubaUiComponents.create(name) != null

        where:
        name << [
                SearchField.NAME,
                SearchPickerField.NAME,
                OptionsGroup.NAME,
                OptionsList.NAME,
                PickerField.NAME,
                LookupField.NAME,
                LookupPickerField.NAME,

                FileUploadField.NAME,
                FileMultiUploadField.NAME,
                Filter.NAME,

                FieldGroup.NAME,
                RowsCount.NAME,
                RelatedEntities.NAME,
                BulkEditor.NAME,
                ListEditor.NAME,
                Embedded.NAME,

                FoldersPane.NAME,
                // FtsField.NAME, todo fts field
        ]
    }

    @Unroll
    def "create standard facet: '#facet' with Facets"() {
        expect:
        facets.create(facet) != null

        where:
        facet << [
                CubaDataLoadCoordinator,
                InputDialogFacet,
                Timer,
                ScreenFacet,
                LookupScreenFacet,
                EditorScreenFacet
        ]
    }
}