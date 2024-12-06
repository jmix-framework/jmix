/*
 * Copyright 2024 Haulmont.
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

package io.jmix.security.usersubstitution;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Interface to be implemented by beans that store {@code UserSubstitutionModel} in a persistent storage.
 */
public interface UserSubstitutionPersistence {

    boolean isViewPermitted();

    List<UserSubstitutionModel> loadSubstitutionsOf(String username);

    void remove(Collection<UserSubstitutionModel> userSubstitutionModels);

    UserSubstitutionModel load(UUID id);

    UserSubstitutionModel save(UserSubstitutionModel userSubstitutionModel);
}
