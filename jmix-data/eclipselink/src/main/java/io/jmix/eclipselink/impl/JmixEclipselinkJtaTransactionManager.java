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

package io.jmix.eclipselink.impl;

import io.jmix.data.impl.JmixJtaTransactionManager;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.eclipse.persistence.internal.helper.JmixUtil;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class JmixEclipselinkJtaTransactionManager extends JmixJtaTransactionManager {

    public JmixEclipselinkJtaTransactionManager(String key,
                                                UserTransaction userTransaction,
                                                TransactionManager transactionManager) {
        super(key, userTransaction, transactionManager);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        // set soft deletion at beginning of each new transaction
        JmixUtil.setSoftDeletion(true);
        JmixUtil.setOriginalSoftDeletion(true);
    }

    @Override
    protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
        super.prepareSynchronization(status, definition);
        // lookup instead of injection to avoid circular dependency
        EclipselinkPersistenceSupport persistenceSupport = applicationContext.getBean(EclipselinkPersistenceSupport.class);
        persistenceSupport.registerSynchronizations(getKey());
    }
}
