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

package test_support.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Visible base of an inheritance hierarchy with a {@code @ExcludeFromAi} subclass, {@link SecretDocument}: a query
 * over this entity must not return the subclass's records. The {@code parent} reference lets a test reach it through
 * a join or a path. {@code licenseKey} and {@code systemRecord} refer to entities left out of the model, by the
 * annotation and by configuration.
 */
@Entity(name = "aitls_Document")
@Table(name = "AITLS_DOCUMENT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JmixEntity
public class Document {

    @Id
    @Column(name = "ID")
    protected Long id;

    @Column(name = "TITLE")
    protected String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    protected Document parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LICENSE_KEY_ID")
    protected HiddenEntity licenseKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SYSTEM_RECORD_ID")
    protected SystemLevelEntity systemRecord;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Document getParent() {
        return parent;
    }

    public void setParent(Document parent) {
        this.parent = parent;
    }

    public HiddenEntity getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(HiddenEntity licenseKey) {
        this.licenseKey = licenseKey;
    }

    public SystemLevelEntity getSystemRecord() {
        return systemRecord;
    }

    public void setSystemRecord(SystemLevelEntity systemRecord) {
        this.systemRecord = systemRecord;
    }
}
