/*
 * Copyright 2022 Haulmont.
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

package io.jmix.eclipselink.impl;

import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionExecution;
import org.springframework.transaction.TransactionExecutionListener;

public class JmixEclipselinkTransactionExecutionListener implements TransactionExecutionListener {


    private final String key;
    private final ApplicationContext applicationContext;

    public JmixEclipselinkTransactionExecutionListener(String key, ApplicationContext applicationContext) {
        this.key = key;
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterBegin(TransactionExecution transaction, @Nullable Throwable beginFailure) {
        EclipselinkPersistenceSupport persistenceSupport = applicationContext.getBean(EclipselinkPersistenceSupport.class);
        persistenceSupport.registerSynchronizations(key);
    }
}
