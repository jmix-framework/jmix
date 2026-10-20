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

import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.upload.UploadButton
import com.vaadin.flow.component.upload.UploadManager
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.upload.FileStorageUploadField
import io.jmix.flowui.component.upload.FileUploadField
import io.jmix.flowui.component.upload.handler.FileTemporaryStorageUploadHandler
import io.jmix.flowui.kit.component.upload.AbstractSingleUploadField
import io.jmix.flowui.kit.component.upload.JmixUploadI18N
import io.jmix.flowui.kit.component.upload.event.FileUploadFileRejectedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import static com.vaadin.flow.component.upload.UploadManager.FileRejectionReason.*

@SpringBootTest
class UploadFieldTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    def "Upload button is a stock UploadButton linked to the field's manager"() {
        when: "A field is created"
        def field = uiComponents.create(FileUploadField)

        then: "The button is Vaadin's UploadButton linked to the manager the field owns"
        field.uploadButton.class == UploadButton
        // getUploadManager() is a default method of the package-private HasUploadManager; read the data it reads
        ComponentUtil.getData(field.uploadButton, UploadManager).is(field.uploadManager)
        field.uploadManager.maxFiles == 1
        field.uploadButton.classNames.contains("jmix-upload-field-button")
        field.uploadHandler != null
    }

    def "Manager is disabled while the field is read-only or disabled"() {
        given: "A field"
        def field = uiComponents.create(FileUploadField)

        when: "The field becomes read-only"
        field.readOnly = true

        then: "The manager is disabled"
        !field.uploadManager.enabled

        when: "The field becomes editable"
        field.readOnly = false

        then: "The manager is enabled"
        field.uploadManager.enabled

        when: "The field is disabled"
        field.enabled = false

        then: "The manager is disabled"
        !field.uploadManager.enabled
    }

    def "Upload text and icon go to the upload button"() {
        given: "A field and an icon"
        def field = uiComponents.create(FileUploadField)
        def icon = VaadinIcon.UPLOAD.create()

        when: "Upload text and icon are set"
        field.uploadText = "t"
        field.uploadIcon = icon

        then: "The upload button shows them"
        field.uploadButton.text == "t"
        field.uploadButton.icon.is(icon)
        field.uploadIcon.is(icon)
    }

    def "Max file size goes to the manager"() {
        given: "A field"
        def field = uiComponents.create(FileUploadField)

        when: "Max file size is set"
        field.maxFileSize = 1024

        then: "The manager limits the file size"
        field.uploadManager.maxFileSize == 1024L
        field.maxFileSize == 1024
    }

    def "Clear button clears the value and the manager's file list"() {
        given: "An attached field with a value"
        def field = uiComponents.create(FileUploadField)
        UI.current.add(field)
        field.value = [1] as byte[]
        pendingExpressions()

        when: "The clear button is clicked"
        field.onClearButtonClick(null)

        then: "The value is cleared and the client file list is cleared"
        field.value == null
        pendingExpressions().any { it.contains("clearFileList") }
    }

    def "Rejection message comes from i18n, then from Vaadin's defaults"() {
        given: "A field with default localization"
        def field = uiComponents.create(FileUploadField)

        expect: "Texts from messages.properties"
        field.getFileRejectedMessage(FILE_TOO_LARGE) == "File is too big"
        field.getFileRejectedMessage(INCORRECT_FILE_TYPE) == "Incorrect file type"
        field.getFileRejectedMessage(UNKNOWN) == "Incorrect file type"

        when: "The i18n has no texts"
        field.i18n = new JmixUploadI18N()

        then: "Vaadin's client defaults are used"
        field.getFileRejectedMessage(TOO_MANY_FILES) == "Too Many Files."
        field.getFileRejectedMessage(FILE_TOO_LARGE) == "File is Too Big."
        field.getFileRejectedMessage(INCORRECT_FILE_TYPE) == "Incorrect File Type."
    }

    def "Rejected file fires FileUploadFileRejectedEvent with the mapped text"() {
        given: "A field with a file-rejected listener"
        def field = uiComponents.create(FileUploadField)
        def messages = []
        field.addFileUploadFileRejectedListener { FileUploadFileRejectedEvent event -> messages << event.errorMessage }

        when: "The manager rejects a file"
        ComponentUtil.fireEvent(field, new UploadManager.FileRejectedEvent(field, true, FILE_TOO_LARGE, "a.bin"))

        then: "The listener receives the localized text"
        messages == ["File is too big"]
    }

    def "Internal error from the client reaches handleJmixUploadInternalError"() {
        given: "A field that records internal errors"
        def captured = []
        def field = new FileUploadField() {
            @Override
            protected void handleJmixUploadInternalError(String fileName) {
                captured << fileName
            }
        }
        field.applicationContext = applicationContext
        field.afterPropertiesSet()

        when: "The client reports an internal error"
        ComponentUtil.fireEvent(field, new AbstractSingleUploadField.JmixUploadInternalErrorEvent(field, true, "a.bin"))

        then: "The handler receives the file name"
        captured == ["a.bin"]
    }

    def "FileStorageUploadField keeps its upload handler for temp-file cleanup"() {
        when: "A file storage upload field is created"
        def field = uiComponents.create(FileStorageUploadField)

        then: "Its upload handler is the temporary storage handler"
        field.uploadHandler instanceof FileTemporaryStorageUploadHandler
    }

    protected List<String> pendingExpressions() {
        def internals = UI.current.internals
        internals.stateTree.runExecutionsBeforeClientResponse()
        return internals.dumpPendingJavaScriptInvocations()*.invocation*.expression
    }
}
