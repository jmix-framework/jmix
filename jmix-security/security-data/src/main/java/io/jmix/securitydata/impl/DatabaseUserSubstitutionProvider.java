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

package io.jmix.securitydata.impl;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.usersubstitution.UserSubstitution;
import io.jmix.core.usersubstitution.UserSubstitutionProvider;
import io.jmix.securitydata.entity.UserSubstitutionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link UserSubstitutionProvider} that stores {@link UserSubstitution} in a database.
 */
@Component("sec_DatabaseUserSubstitutionProvider")
public class DatabaseUserSubstitutionProvider implements UserSubstitutionProvider {

    @Autowired
    private UnconstrainedDataManager dataManager;

    @Override
    public Collection<UserSubstitution> getUserSubstitutions(String username, Date date) {
        List<UserSubstitutionEntity> userSubstitutionEntities = dataManager.load(UserSubstitutionEntity.class)
                .query("e.username = :username " +
                        "and (e.startDate is null or e.startDate <= :date) " +
                        "and (e.endDate is null or e.endDate >= :date)")
                .parameter("date", date)
                .parameter("username", username)
                .list();

        return userSubstitutionEntities.stream()
                .map(entity -> new UserSubstitution(entity.getUsername(),
                        entity.getSubstitutedUsername(),
                        entity.getStartDate(),
                        entity.getEndDate()))
                .collect(Collectors.toList());
    }
}
