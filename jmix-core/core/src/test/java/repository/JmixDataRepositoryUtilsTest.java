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

package repository;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.Sort;
import io.jmix.core.repository.JmixDataRepositoryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.Pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class JmixDataRepositoryUtilsTest {

    @Autowired
    Metadata metadata;

    @Test
    void test_buildPageRequest() {
        LoadContext<Pet> loadContext = new LoadContext<>(metadata.getClass(Pet.class));
        loadContext.setQueryString("select p from app_Pet p")
                .setFirstResult(0)
                .setMaxResults(10)
                .setSort(Sort.by("id"));

        Pageable pageable = JmixDataRepositoryUtils.buildPageRequest(loadContext);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(org.springframework.data.domain.Sort.by("id"), pageable.getSort());
    }

    @Test
    void test_buildPageRequest_no_paging() {
        LoadContext<Pet> loadContext = new LoadContext<>(metadata.getClass(Pet.class));
        loadContext.setQueryString("select p from app_Pet p")
                .setSort(Sort.by("id"));

        Pageable pageable = JmixDataRepositoryUtils.buildPageRequest(loadContext);

        assertTrue(pageable.isUnpaged());
        assertEquals(org.springframework.data.domain.Sort.by("id"), pageable.getSort());
    }
}
