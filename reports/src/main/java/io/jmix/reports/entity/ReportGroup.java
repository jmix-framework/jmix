/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.ModelProperty;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity(name = "report$ReportGroup")
@Table(name = "REPORT_GROUP")
@NamePattern("#getLocName|title,localeNames")
@SuppressWarnings("unused")
public class ReportGroup extends StandardEntity {

    private static final long serialVersionUID = 5399528790289039413L;

    @Column(name = "TITLE", unique = true, nullable = false)
    private String title;

    @Column(name = "CODE")
    private String code;

    @Column(name = "LOCALE_NAMES")
    private String localeNames;

    @SystemLevel
    @Column(name = "SYS_TENANT_ID")
    private String sysTenantId;

    @Transient
    private String localeName;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocaleNames() {
        return localeNames;
    }

    public void setLocaleNames(String localeNames) {
        this.localeNames = localeNames;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }

    @ModelProperty
    public String getLocName() {
        if (localeName == null) {
            //TODO Locale helper
//            localeName = LocaleHelper.getLocalizedName(localeNames);
            if (localeName == null)
                localeName = title;
        }
        return localeName;
    }

    @ModelProperty
    @DependsOnProperties("code")
    public Boolean getSystemFlag() {
        return StringUtils.isNotEmpty(code);
    }
}