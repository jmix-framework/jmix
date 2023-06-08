/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.entity.wizard;

import io.jmix.core.metamodel.datatype.EnumClass;

public enum TemplateFileType implements EnumClass<Integer> {
    HTML(30),
    DOCX(40),
    XLSX(50),
    CHART(60),
    CSV(70),
    TABLE(80);

    private Integer id;

    TemplateFileType(Integer id) {
        this.id = id;
    }

    public static TemplateFileType fromId(Integer id) {
        for (TemplateFileType type : TemplateFileType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public Integer getId() {
        return id;
    }

}
