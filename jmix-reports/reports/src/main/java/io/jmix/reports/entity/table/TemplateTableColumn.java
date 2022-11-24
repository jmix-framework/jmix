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

package io.jmix.reports.entity.table;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Id;
import java.util.UUID;

@JmixEntity(name = "report_TemplateTableColumn")
public class TemplateTableColumn {

    @Id
    @JmixGeneratedValue
    protected UUID id;

    /**
     * Full path of the properties tree.
     */
    @JmixProperty
    protected String key;

    /**
     * Key to extract value from band data.
     * <p>In most cases coincides with the {@link #key} value but in case of displaying some properties of elements
     * of reference collection it shouldn't include root collection property itself because band data keeps values
     * relatively to properties within region. And collection property requires new tabulated region.
     * <ul>
     *     <li>
     *         Single-reference attribute:
     *         <ul>
     *             <li>
     *                 key: singleReference.name
     *             </li>
     *             <li>
     *                 dataKey: singleReference.name
     *             </li>
     *         </ul>
     *     </li>
     *     <li>
     *         Multi-reference attribute
     *         <ul>
     *             <li>
     *                 key: multiReference.name
     *             </li>
     *             <li>
     *                 dataKey: name
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     */
    @JmixProperty
    protected String dataKey;

    @JmixProperty
    protected String caption;

    @JmixProperty
    protected Integer position;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDataKey() {
        return StringUtils.isNotEmpty(dataKey)
                ? dataKey
                : key;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
