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

package component.image.view;

import com.vaadin.flow.router.Route;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.image.JmixImage;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.attachment.DocumentAttachment;

@Route("JmixImageTestView")
@ViewController("JmixImageTestView")
@ViewDescriptor("jmix-image-test-view.xml")
public class JmixImageTestView extends StandardView {

    @ViewComponent
    private InstanceContainer<DocumentAttachment> documentAttachmentDc;
    @Autowired
    private Metadata metadata;

    @ViewComponent
    public JmixImage<byte[]> imageByteArray;

    @Subscribe
    public void onReady(ReadyEvent event) {
        DocumentAttachment attachment = metadata.create(DocumentAttachment.class);
        attachment.setPreview(RandomUtils.nextBytes(1));
        documentAttachmentDc.setItem(attachment);
    }
}
