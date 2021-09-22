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

package io.jmix.core.security.impl;

import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.core.security.SpecificPolicyInfoRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component("core_SpecificPolicyInfoRegistry")
public class SpecificPolicyInfoRegistryImpl implements SpecificPolicyInfoRegistry {

    @Autowired
    protected JmixModulesClasspathScanner jmixModulesClasspathScanner;

    protected List<SpecificPolicyInfo> specificPolicyInfoList = new ArrayList<>();

    protected volatile boolean initialized = false;

    protected static final Logger log = LoggerFactory.getLogger(SpecificPolicyInfoRegistryImpl.class);

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    log.info("Initializing specific policy infos list");
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        specificPolicyInfoList.clear();
        Set<String> classNames = jmixModulesClasspathScanner.getClassNames(SpecificOperationAccessContextDetector.class);
        for (String className : classNames) {
            try {
                Class<?> aClass = Class.forName(className);
                SpecificOperationAccessContext newInstance = (SpecificOperationAccessContext) aClass.getDeclaredConstructor().newInstance();
                specificPolicyInfoList.add(new SpecificPolicyInfo(newInstance.getName()));
            } catch (Exception e) {
                log.error("Cannot instantiate an instance of {}", className, e);
            }
        }
    }

    @Override
    public List<SpecificPolicyInfo> getSpecificPolicyInfos() {
        lock.readLock().lock();
        try {
            checkInitialized();
            return Collections.unmodifiableList(specificPolicyInfoList);
        } finally {
            lock.readLock().unlock();
        }
    }
}