/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.core.global;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Interface to provide instances of {@link TransactionalAction}
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link org.springframework.transaction.support.TransactionTemplate}
 * or {@link org.springframework.transaction.PlatformTransactionManager}.
 */
@Deprecated
public interface TransactionalActionFactory {

    String NAME = "cuba_TransactionalActionFactory";

    /**
     * Creates instance of {@link TransactionalAction}.
     *
     * @param supplier defines how to retrieve {@link CommitContext}
     * @return instance of {@link TransactionalAction} without any additional
     * actions ({@code onSuccess, onFail, beforeCommit, afterCompletion}) and with {@code joinTransaction=false}
     */
    TransactionalAction getTransactionalAction(Supplier<CommitContext> supplier);

    /**
     * Creates instance of {@link TransactionalAction}.
     *
     * @param commitContext contains all required changes (created, updated and deleted entities)
     * @return instance of {@link TransactionalAction} without any additional
     * actions ({@code onSuccess, onFail, beforeCommit, afterCompletion}) and with {@code joinTransaction=false}
     */
    TransactionalAction getTransactionalAction(CommitContext commitContext);

    /**
     * Creates instance of {@link TransactionalAction}.
     *
     * @param supplier        defines how to retrieve {@link CommitContext}
     * @param joinTransaction defines should {@link CommitContext} be joined to existing transaction or not
     * @return instance of {@link TransactionalAction} without any additional
     * actions ({@code onSuccess, onFail, beforeCommit, afterCompletion})
     */
    TransactionalAction getTransactionalAction(Supplier<CommitContext> supplier, boolean joinTransaction);

    /**
     * Creates instance of {@link TransactionalAction}.
     *
     * @param supplier          defines how to retrieve {@link CommitContext}
     * @param onSuccessAction   specifies action that should be performed in case of success commit
     * @param onFailAction      specifies action that should be performed in case of commit failure.
     *                          If {@code onFailAction != null} then exception won't be thrown in case of not success commit
     * @param afterCommitAction specifies action that should be performed after commit
     * @param joinTransaction   defines should {@link CommitContext} be joined to existing transaction or not
     * @return instance of {@link TransactionalAction}
     */
    TransactionalAction getTransactionalAction(Supplier<CommitContext> supplier,
                                               Consumer<EntitySet> onSuccessAction,
                                               BiConsumer<CommitContext, Throwable> onFailAction,
                                               Consumer<CommitContext> afterCommitAction,
                                               boolean joinTransaction);

    /**
     * Creates instance of {@link TransactionalAction}.
     *
     * @return instance of {@link TransactionalAction} without {@code commitContext} and any additional
     * actions ({@code onSuccess, onFail, beforeCommit, afterCompletion}) and with {@code joinTransaction=false}
     */
    TransactionalAction getTransactionalAction();

}
