/*
 * Copyright 2025 Haulmont.
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

package rest_ds;

import io.jmix.core.DataManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.TestSupport;
import test_support.entity.Employee;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtendedEntityTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Test
    void testLoad() {
        Employee employee = dataManager.load(Employee.class).id(TestSupport.UUID_10).one();

        assertThat(employee).isNotNull();
        assertThat(employee.getExtInfo()).isNotEmpty();

        List<Employee> employees = dataManager.load(Employee.class).all().list();

        assertThat(employees).isNotEmpty();
    }

    @Test
    void testCreateUpdateDelete() {
        Employee employee = dataManager.create(Employee.class);
        String newName = "new-employee-" + LocalDateTime.now();
        employee.setName(newName);
        employee.setExtInfo("ext info");

        Employee createdEmployee = dataManager.save(employee);

        assertThat(createdEmployee).isNotNull();
        assertThat(createdEmployee.getName()).isEqualTo(newName);
        assertThat(createdEmployee.getExtInfo()).isEqualTo(employee.getExtInfo());

        createdEmployee.setName("updated-employee-" + LocalDateTime.now());
        createdEmployee.setExtInfo("updated ext info");

        Employee updatedEmployee = dataManager.save(createdEmployee);

        assertThat(updatedEmployee).isNotNull();
        assertThat(updatedEmployee.getName()).isEqualTo(createdEmployee.getName());
        assertThat(updatedEmployee.getExtInfo()).isEqualTo(createdEmployee.getExtInfo());

        dataManager.remove(updatedEmployee);

        Employee deletedEmployee = dataManager.load(Employee.class).id(updatedEmployee.getId()).optional().orElse(null);

        assertThat(deletedEmployee).isNull();
    }
}
