/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.repair;

import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;

/**
 * Outcome of {@link JpqlRepairService#repairIfNeeded}: the resulting query draft, its validation
 * result, the number of repair attempts made and whether a repair was attempted at all.
 */
public class JpqlRepairResult {

    protected GeneratedJpqlResult generatedJpqlResult;
    protected JpqlValidationResult validationResult;
    protected int repairAttempts;
    protected boolean repaired;

    public JpqlRepairResult(GeneratedJpqlResult generatedJpqlResult,
                            JpqlValidationResult validationResult,
                            int repairAttempts,
                            boolean repaired) {
        this.generatedJpqlResult = generatedJpqlResult;
        this.validationResult = validationResult;
        this.repairAttempts = repairAttempts;
        this.repaired = repaired;
    }

    /**
     * Returns the resulting query draft (repaired if a repair succeeded, otherwise the original).
     *
     * @return query draft
     */
    public GeneratedJpqlResult getGeneratedJpqlResult() {
        return generatedJpqlResult;
    }

    /**
     * Returns the validation result of the resulting query draft.
     *
     * @return validation result
     */
    public JpqlValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * Returns the number of repair attempts made.
     *
     * @return repair attempt count, {@code 0} if no repair was attempted
     */
    public int getRepairAttempts() {
        return repairAttempts;
    }

    /**
     * Returns whether a repair was attempted (regardless of whether it produced a valid query).
     *
     * @return {@code true} if a repair attempt was made
     */
    public boolean isRepaired() {
        return repaired;
    }
}
