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

package component_xml_load

import com.vaadin.flow.component.shared.Tooltip
import component_xml_load.screen.FileStorageUploadFieldView
import io.jmix.core.DataManager
import io.jmix.core.FileRef
import io.jmix.flowui.kit.component.upload.FileStoragePutMode
import org.apache.commons.lang3.RandomUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.attachment.DocumentAttachment
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class FileStorageUploadFieldXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    private DataManager dataManager

    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        dataManager.save(createAttachment())
    }

    def "Load FileStorageUploadField component with datasource from XML"() {
        when: "Open view with FileStorageUploadField"
        def view = navigateToView(FileStorageUploadFieldView)

        then: "Field should be bound with data container"

        view.dataSourceFileStorageUploadField.valueSource != null
        view.dataSourceFileStorageUploadField.getValue() == view.attachmentDc.item.attachment
        view.vceOccurred
    }

    def "Load FileStorageUploadField component from XML"() {
        when: "Open view with FileStorageUploadFields"
        def view = navigateToView(FileStorageUploadFieldView)

        then: "FileStorageUploadFields attributes should be loaded"
        def field = view.xmlFileStorageUploadField
        field.acceptedFileTypes[0] == ".jpg"
        field.classNames[0] == "custom-className"
        field.style.get("color") == "red"
        field.clearButtonAriaLabel == "clearButtonAriaLabel"
        field.i18n.uploading.status.connecting == "connectingStatusText"
        !field.dropAllowed
        field.errorMessage == "errorMessage"
        field.fileNotSelectedText == "fileNotSelectedText"
        field.i18n.error.fileIsTooBig == "fileTooBigText"
        field.fileStoragePutMode == FileStoragePutMode.MANUAL
        field.fileStorageName == "fss"
        field.height == "8em"
        field.width == "20em"
        field.helperText == "helperText"
        field.i18n.error.incorrectFileType == "incorrectFileTypeText"
        field.label == "label"
        field.maxFileSize == 10480000
        field.maxHeight == "100em"
        field.maxWidth == "100em"
        field.minHeight == "8em"
        field.minWidth == "20em"
        field.i18n.uploading.status.processing == "processingStatusText"
        field.i18n.uploading.remainingTime.prefix == "remainingTimeText"
        field.i18n.uploading.remainingTime.unknown == "remainingTimeUnknownText"
        field.required
        field.requiredIndicatorVisible
        field.requiredMessage == "requiredMessage"
        field.i18n.uploadDialog.cancel == "uploadDialogCancelText"
        field.i18n.uploadDialog.title == "uploadDialogTitle"
        field.uploadIcon != null
        field.uploadText == "uploadText"
        !field.visible

        field.tooltip.text == "tooltipText"
        field.tooltip.focusDelay == 1
        field.tooltip.hideDelay == 2
        field.tooltip.hoverDelay == 3
        field.tooltip.manual
        field.tooltip.opened
        field.tooltip.position == Tooltip.TooltipPosition.BOTTOM

        view.readOnlyFileStorageUploadField.readOnly
        !view.disabledFileStorageUploadField.enabled
    }

    def "Load default I18N"() {
        when: "Open view with FileStorageUploadFields"
        def view = navigateToView(FileStorageUploadFieldView)

        then: "FileStorageUploadFields default localization should be loaded"
        def field = view.localizedFileStorageUploadField

        field.i18n.uploadDialog != null
        field.i18n.error != null
        field.i18n.error.fileIsTooBig != null
        field.i18n.error.incorrectFileType != null
        field.i18n.uploading != null
        field.i18n.uploading.status != null
        field.i18n.uploading.status.connecting != null
        field.i18n.uploading.status.processing != null
        field.i18n.uploading.remainingTime != null
        field.i18n.uploading.remainingTime.prefix != null
        field.i18n.uploading.remainingTime.unknown != null
    }

    protected DocumentAttachment createAttachment() {
        def attachment = dataManager.create(DocumentAttachment)
        attachment.setName("test")
        attachment.setAttachment(FileRef.create("s1", "s1://2022/12/32/file.docx", "file.docx"))
        attachment.setPreview(RandomUtils.nextBytes(1))
        return attachment
    }
}
