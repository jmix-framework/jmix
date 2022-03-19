/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.core.model.common;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@javax.persistence.Entity(name = "test$FileDescriptor")
@JmixEntity
@Table(name = "TEST_FILE")
@NamePattern("%s (%s)|name,createDate,extension")
@SystemLevel
public class FileDescriptor extends StandardEntity {

    private static final long serialVersionUID = 564683944299730504L;

    @Column(name = "NAME", length = 500, nullable = false)
    private String name;

    @Column(name = "EXT", length = 20)
    private String extension;

    @Column(name = "FILE_SIZE")
    private Long size;

    @Column(name = "CREATE_DATE")
    private Date createDate;

    @Column(name = "SYS_TENANT_ID")
    @TenantId
    protected String sysTenantId;

    /**
     * @return file uploading timestamp
     */
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * @return file name including extension
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return file extension, i.e. the part of name after the last dot
     */
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = StringUtils.substring(extension, 0, 20);
    }

    /**
     * @return file size in bytes
     */
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}
