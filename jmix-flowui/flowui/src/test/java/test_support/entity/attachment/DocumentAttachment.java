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

package test_support.entity.attachment;

import io.jmix.core.FileRef;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.TestBaseEntity;

import javax.persistence.*;

@Entity(name = "attch_DocumentAttachment")
@JmixEntity
@Table(name = "ATTCH_DOCUMENTATTACHMENT")
public class DocumentAttachment extends TestBaseEntity {

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "ATTACHMENT")
    private FileRef attachment;

    @Column(name = "PREVIEW")
    private byte[] preview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOCUMENT_ID")
    private Document document;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileRef getAttachment() {
        return attachment;
    }

    public void setAttachment(FileRef attachment) {
        this.attachment = attachment;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public byte[] getPreview() {
        return preview;
    }

    public void setPreview(byte[] preview) {
        this.preview = preview;
    }
}
