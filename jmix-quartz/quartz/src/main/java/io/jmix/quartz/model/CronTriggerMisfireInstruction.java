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

package io.jmix.quartz.model;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;

/**
 * Describes all Misfire Instruction options available for Quartz {@link CronTrigger}.
 * Each option relates to some MISFIRE_INSTRUCTION constant and can call corresponding method of the Schedule builder
 * to apply target misfire instruction.
 */
public enum CronTriggerMisfireInstruction implements EnumClass<String> {

    SMART_POLICY("smart_policy", builder -> {}, CronTrigger.MISFIRE_INSTRUCTION_SMART_POLICY),
    IGNORE_MISFIRE_POLICY("ignore_misfire_policy",
            CronScheduleBuilder::withMisfireHandlingInstructionIgnoreMisfires,
            CronTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY),
    FIRE_NOW("fire_now", CronScheduleBuilder::withMisfireHandlingInstructionFireAndProceed,
            CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW),
    DO_NOTHING("do_nothing",
            CronScheduleBuilder::withMisfireHandlingInstructionDoNothing,
            CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);

    private final String id;
    private final Consumer<CronScheduleBuilder> consumer;
    private final int code;

    /**
     * Creates new misfire instruction option.
     * @param id internal enum id
     * @param consumer action to perform on {@link CronScheduleBuilder} to apply target misfire instruction
     * @param code Quartz-specific integer number of target misfire instruction (MISFIRE_INSTRUCTION constant value)
     */
    CronTriggerMisfireInstruction(String id, Consumer<CronScheduleBuilder> consumer, int code) {
        this.id = id;
        this.consumer = consumer;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public int getCode() {
        return code;
    }

    /**
     * Calls necessary method of {@link CronScheduleBuilder} to apply target misfire instruction
     * @param builder schedule builder
     */
    public void applyInstruction(CronScheduleBuilder builder) {
        consumer.accept(builder);
    }

    @Nullable
    public static CronTriggerMisfireInstruction fromId(String id) {
        for (CronTriggerMisfireInstruction at : CronTriggerMisfireInstruction.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    @Nullable
    public static CronTriggerMisfireInstruction fromCode(int code) {
        for (CronTriggerMisfireInstruction value : CronTriggerMisfireInstruction.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}