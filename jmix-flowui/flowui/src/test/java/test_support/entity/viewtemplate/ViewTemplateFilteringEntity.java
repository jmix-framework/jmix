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

package test_support.entity.viewtemplate;

import io.jmix.core.annotation.Secret;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.flowui.view.template.DetailViewTemplate;
import io.jmix.flowui.view.template.ListViewTemplate;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import test_support.entity.TestBaseEntity;
import test_support.entity.sales.Address;
import test_support.entity.sales.Customer;

import java.util.List;

@JmixEntity
@Table(name = "TEST_VIEW_TEMPLATE_FILTERING")
@Entity(name = "test_ViewTemplateFilteringEntity")
@ListViewTemplate(
        templateParams = """
                {"includeProperties":["createdBy"],"excludeProperties":["active"]}
                """
)
@DetailViewTemplate(
        viewId = "test_ViewTemplateFilteringEntity.edit",
        templateParams = """
                {"includeProperties":["createdBy"],"excludeProperties":["active"]}
                """
)
public class ViewTemplateFilteringEntity extends TestBaseEntity {

    @Column(name = "NAME")
    protected String name;

    @Column(name = "ACTIVE")
    protected Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    protected Customer customer;

    @Secret
    @Column(name = "SECRET_TOKEN")
    protected String secretToken;

    @SystemLevel
    @Column(name = "SYSTEM_VALUE")
    protected String systemValue;

    @Embedded
    protected Address address;

    @ElementCollection
    protected List<String> tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    public String getSystemValue() {
        return systemValue;
    }

    public void setSystemValue(String systemValue) {
        this.systemValue = systemValue;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
