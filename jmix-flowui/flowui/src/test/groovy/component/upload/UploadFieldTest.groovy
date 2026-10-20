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

    def "Too many files message is localized"() {
        expect: "Both fields have the localized text"
        uiComponents.create(FileUploadField).getFileRejectedMessage(TOO_MANY_FILES) == "Only one file can be uploaded"
        uiComponents.create(FileStorageUploadField).getFileRejectedMessage(TOO_MANY_FILES) ==
                "Only one file can be uploaded"
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

    def "Clear button aria-label is set on the clear button"() {
        given: "A field"
        def field = uiComponents.create(FileUploadField)

        expect: "The default label is on the clear button"
        field.clearComponent.element.getAttribute("aria-label") == "Remove file"

        when: "The clear button aria-label is set"
        field.clearButtonAriaLabel = "Delete"

        then: "It lands on the clear button, not on the upload button"
        field.clearButtonAriaLabel == "Delete"
        field.clearComponent.element.getAttribute("aria-label") == "Delete"
        !field.uploadButton.element.hasAttribute("aria-label")
    }

    def "Deprecated accepted types are a client hint and accept legacy values"() {
        given: "A field"
        def field = uiComponents.create(FileUploadField)

        when: "Deprecated accepted types are set, one of them without a dot"
        field.acceptedFileTypes = ["pdf", ".doc", "image/*"] as String[]

        then: "They are stored, pushed to the client as is and not validated on the server"
        field.acceptedFileTypes == ["pdf", ".doc", "image/*"]
        field.element.getProperty("acceptedFileTypes") == "pdf,.doc,image/*"
        field.uploadManager.acceptedMimeTypes.empty
        field.uploadManager.acceptedFileExtensions.empty
    }

    def "Mixing deprecated and new accepted types throws like Upload"() {
        given: "A field with deprecated accepted types"
        def field = uiComponents.create(FileUploadField)
        field.acceptedFileTypes = [".pdf"] as String[]

        when: "MIME types are set"
        field.acceptedMimeTypes = ["image/png"] as String[]

        then: "Mixing is refused"
        def e1 = thrown(IllegalStateException)
        e1.message == "Cannot use setAcceptedMimeTypes when setAcceptedFileTypes is configured. " +
                "Mixing the deprecated and new APIs is not supported."

        when: "File extensions are set"
        field.acceptedFileExtensions = [".png"] as String[]

        then: "Mixing is refused"
        def e2 = thrown(IllegalStateException)
        e2.message == "Cannot use setAcceptedFileExtensions when setAcceptedFileTypes is configured. " +
                "Mixing the deprecated and new APIs is not supported."

        when: "MIME types are read"
        field.acceptedMimeTypes

        then: "Reading the new API is refused"
        def e3 = thrown(IllegalStateException)
        e3.message == "Cannot use getAcceptedMimeTypes when setAcceptedFileTypes is configured. " +
                "Mixing the deprecated and new APIs is not supported."

        when: "File extensions are read"
        field.acceptedFileExtensions

        then: "Reading the new API is refused"
        def e4 = thrown(IllegalStateException)
        e4.message == "Cannot use getAcceptedFileExtensions when setAcceptedFileTypes is configured. " +
                "Mixing the deprecated and new APIs is not supported."

        when: "Another field has MIME types and deprecated types are set"
        def other = uiComponents.create(FileUploadField)
        other.acceptedMimeTypes = ["image/png"] as String[]
        other.acceptedFileTypes = [".pdf"] as String[]

        then: "Mixing is refused"
        def e5 = thrown(IllegalStateException)
        e5.message == "Cannot use setAcceptedFileTypes when setAcceptedMimeTypes or setAcceptedFileExtensions " +
                "are configured. Mixing the deprecated and new APIs is not supported."

        when: "Deprecated types are read"
        other.acceptedFileTypes

        then: "Reading the deprecated API is refused"
        def e6 = thrown(IllegalStateException)
        e6.message == "Cannot use getAcceptedFileTypes when setAcceptedMimeTypes or setAcceptedFileExtensions " +
                "are configured. Use getAcceptedMimeTypes() and getAcceptedFileExtensions() instead."
    }

    def "Empty new-API call keeps the deprecated hint"() {
        given: "A field with deprecated accepted types"
        def field = uiComponents.create(FileUploadField)
        field.acceptedFileTypes = [".pdf"] as String[]

        when: "Empty MIME types and file extensions are set"
        field.acceptedMimeTypes = [] as String[]
        field.acceptedFileExtensions = [] as String[]

        then: "Nothing is thrown and the hint stays"
        noExceptionThrown()
        field.element.getProperty("acceptedFileTypes") == ".pdf"
        field.acceptedFileTypes == [".pdf"]
        field.uploadManager.acceptedMimeTypes.empty
        field.uploadManager.acceptedFileExtensions.empty
    }

    def "Clearing the deprecated types removes the hint"() {
        given: "A field with deprecated accepted types"
        def field = uiComponents.create(FileUploadField)
        field.acceptedFileTypes = [".pdf", null] as String[]

        expect: "Null entries are skipped"
        field.acceptedFileTypes == [".pdf"]
        field.element.getProperty("acceptedFileTypes") == ".pdf"

        when: "Deprecated types are cleared"
        field.acceptedFileTypes = [] as String[]

        then: "The hint is removed and the new API can be used"
        field.acceptedFileTypes.empty
        field.element.getProperty("acceptedFileTypes") == ""

        when: "MIME types are set"
        field.acceptedMimeTypes = ["image/png"] as String[]

        then: "They reach the manager"
        field.acceptedMimeTypes == ["image/png"]
        field.uploadManager.acceptedMimeTypes == ["image/png"]
    }

    protected List<String> pendingExpressions() {
        def internals = UI.current.internals
        internals.stateTree.runExecutionsBeforeClientResponse()
        return internals.dumpPendingJavaScriptInvocations()*.invocation*.expression
    }
}
