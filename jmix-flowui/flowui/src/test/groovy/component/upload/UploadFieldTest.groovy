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

package component.upload

import com.vaadin.flow.server.streams.UploadMetadata
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.upload.FileUploadField
import io.jmix.flowui.kit.component.upload.handler.SupportUploadSuccessHandler.UploadSuccessContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class UploadFieldTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    def "File name is updated when a file with the same content is uploaded under another name"() {
        given: "A field with an uploaded file"
        def field = uiComponents.create(FileUploadField)
        def valueChanges = 0
        field.addValueChangeListener { valueChanges++ }
        field.onSucceeded(new UploadSuccessContext<>(new UploadMetadata("first.png", "image/png", 1), [1] as byte[]))

        when: "A file with the same content and another name is uploaded"
        field.onSucceeded(new UploadSuccessContext<>(new UploadMetadata("second.png", "image/png", 1), [1] as byte[]))

        then: "The field shows the name of the last uploaded file, the value is not changed"
        field.uploadedFileName == "second.png"
        field.fileNameComponent.text == "second.png"
        valueChanges == 1
    }
}
