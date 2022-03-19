/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.core.views

import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.core.global.View
import com.haulmont.cuba.core.model.common.Group
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.core.testsupport.TestSupport
import io.jmix.core.DevelopmentException
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanRepository
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

class ViewReferenceAttrTest extends CoreTestSpecification {
    @Autowired
    FetchPlanRepository viewRepository
    @Autowired
    DataManager dataManager
    @Autowired
    TestSupport testSupport

    User user
    Group group

    void setup() {
        group = new Group(name: 'Company')
        user = new User(login: 'admin', group: group)

        dataManager.commit(user, group)
    }

    void cleanup() {
        testSupport.deleteRecord(user, group)
    }

    def "Negative (String): PL-9999 Raise exception at the moment of view creation if not reference attribute has view"() {
        FetchPlan wrongView = new View(User.class)
                .addProperty(
                        "name",
                        viewRepository.getFetchPlan(User.class, "user.locale")
                )

        LoadContext<User> ctx = new LoadContext<>(User.class)
                .setView(wrongView)
        ctx.setQueryString("select u from test\$User u")

        String exceptionMessage = ""

        when:
        try {
            dataManager.loadList(ctx)
        } catch (DevelopmentException e) {
            exceptionMessage = e.getMessage()
        }

        then:
        "Wrong fetch plans mechanism usage found. Fetch plan \"user.locale\" is set for property \"name\" of class \"test\$User\", " +
                "but this property does not point to an Entity" == exceptionMessage
    }

    def "Negative (Boxed primitive): PL-9999 Raise exception at the moment of view creation if not reference attribute has view"() {
        FetchPlan wrongView = new View(User.class)
                .addProperty(
                        "active",
                        viewRepository.getFetchPlan(User.class, "user.locale")
                )

        LoadContext<User> ctx = new LoadContext<>(User.class)
                .setView(wrongView)
        ctx.setQueryString("select u from test\$User u")

        String exceptionMessage = ""

        when:
        try {
            dataManager.loadList(ctx)
        } catch (DevelopmentException e) {
            exceptionMessage = e.getMessage()
        }

        then:
        "Wrong fetch plans mechanism usage found. Fetch plan \"user.locale\" is set for property \"active\" of class \"test\$User\", " +
                "but this property does not point to an Entity" == exceptionMessage
    }

    def "Positive: PL-9999 Raise exception at the moment of view creation if not reference attribute has view"() {
        FetchPlan correctView = new View(User.class)
                .addProperty("login")
                .addProperty(
                        "group",
                        new View(Group.class)
                                .addProperty("name")
                )

        LoadContext<User> ctx = LoadContext.create(User.class)
                .setView(correctView)
        ctx.setQueryString("select u from test\$User u where u.login = 'admin'")

        User user

        when:
        user = dataManager.load(ctx)

        then:
        "Company" == user.group.name
    }
}
