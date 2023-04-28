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

package test_support.entity.entity_extension;

import io.jmix.core.entity.annotation.ReplaceEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;

@JmixEntity
@Entity(name = "exttest_ExtDoc")
@Table(name = "EXTTEST_EXT_DOC")
@DiscriminatorValue("200")
@PrimaryKeyJoinColumn(name = "CARD_ID", referencedColumnName = "ID")
@ReplaceEntity(Doc.class)
public class ExtDoc extends Doc {

    @Column(name = "EXT_NAME")
    private String extName;

    public String getExtName() {
        return extName;
    }

    public void setExtName(String extName) {
        this.extName = extName;
    }
}
