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
package com.haulmont.cuba.core.app;

import io.jmix.core.Entity;
import io.jmix.core.pessimisticlocking.LockInfo;
import io.jmix.core.pessimisticlocking.LockManager;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Component(LockService.NAME)
public class LockServiceBean implements LockService {

    @Autowired
    private LockManager lockManager;

    @Override
    public LockInfo lock(String name, String id) {
        return lockManager.lock(name, id);
    }

    @Nullable
    @Override
    public LockInfo lock(Entity entity) {
        return lockManager.lock(entity);
    }

    @Override
    public void unlock(String name, String id) {
        lockManager.unlock(name, id);
    }

    @Override
    public void unlock(Entity entity) {
        lockManager.unlock(entity);
    }

    @Override
    public LockInfo getLockInfo(String name, String id) {
        return lockManager.getLockInfo(name, id);
    }

    @Override
    public List<LockInfo> getCurrentLocks() {
        return new ArrayList<>(lockManager.getCurrentLocks());
    }

    @Override
    public void reloadConfiguration() {
        lockManager.reloadConfiguration();
    }
}