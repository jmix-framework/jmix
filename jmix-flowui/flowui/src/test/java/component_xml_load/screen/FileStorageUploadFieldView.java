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

package component_xml_load.screen;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.upload.FileStorageUploadField;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import test_support.entity.attachment.DocumentAttachment;

@Route("FileStorageUploadFieldView")
@ViewController("FileStorageUploadFieldView")
@ViewDescriptor("file-storage-upload-field-view.xml")
public class FileStorageUploadFieldView extends StandardView {

    @ViewComponent
    public InstanceContainer<DocumentAttachment> attachmentDc;

    @ViewComponent
    public FileStorageUploadField dataSourceFileStorageUploadField;
    @ViewComponent
    public FileStorageUploadField xmlFileStorageUploadField;
    @ViewComponent
    public FileStorageUploadField readOnlyFileStorageUploadField;
    @ViewComponent
    public FileStorageUploadField disabledFileStorageUploadField;
    @ViewComponent
    public FileStorageUploadField localizedFileStorageUploadField;

    public boolean vceOccurred;

    @Subscribe
    protected void onInit(View.InitEvent event) {
        dataSourceFileStorageUploadField.addValueChangeListener(vce -> vceOccurred = true);
    }
}
