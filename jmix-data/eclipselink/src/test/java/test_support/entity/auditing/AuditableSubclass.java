/*
 * Copyright 2020 Haulmont.
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

package test_support.entity.auditing;

import io.jmix.core.metamodel.annotation.JmixEntity;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.Date;

@Table(name = "TEST_AUDITABLE_SUBCLASS")
@JmixEntity
@Entity(name = "test_AuditableSubclass")
public class AuditableSubclass extends CreatableSubclass {
    private static final long serialVersionUID = -2432809166567946764L;

    @Column(name = "TOUCHED_BY")
    @LastModifiedBy
    private String touchedBy;

    @Column(name = "TOUCH_DATE")
    @LastModifiedDate
    private Date touchDate;

    @Column(name = "VERSION")
    @Version
    private Integer version;


    public String getTouchedBy() {
        return touchedBy;
    }

    public void setTouchedBy(String touchedBy) {
        this.touchedBy = touchedBy;
    }

    public Date getTouchDate() {
        return touchDate;
    }

    public void setTouchDate(Date touchDate) {
        this.touchDate = touchDate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}