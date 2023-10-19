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
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;

/**
 * Describes all Misfire Instruction options available for Quartz {@link SimpleTrigger}.
 * Each option relates to some MISFIRE_INSTRUCTION constant and can call corresponding method of the Schedule builder
 * to apply target misfire instruction.
 */
public enum SimpleTriggerMisfireInstruction implements EnumClass<String> {

    SMART_POLICY("smart_policy", builder -> {}, SimpleTrigger.MISFIRE_INSTRUCTION_SMART_POLICY),
    IGNORE_MISFIRE_POLICY("ignore_misfire_policy",
            SimpleScheduleBuilder::withMisfireHandlingInstructionIgnoreMisfires,
            SimpleTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY),
    FIRE_NOW("fire_now",
            SimpleScheduleBuilder::withMisfireHandlingInstructionFireNow,
            SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW),
    RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT("reschedule_now_with_existing_repeat_count",
            SimpleScheduleBuilder::withMisfireHandlingInstructionNowWithExistingCount,
            SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT),
    RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT("reschedule_now_with_remaining_repeat_count",
            SimpleScheduleBuilder::withMisfireHandlingInstructionNowWithRemainingCount,
            SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT),
    RESCHEDULE_NEXT_WITH_EXISTING_REPEAT_COUNT("reschedule_next_with_existing_repeat_count",
            SimpleScheduleBuilder::withMisfireHandlingInstructionNextWithExistingCount,
            SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT),
    RESCHEDULE_NEXT_WITH_REMAINING_REPEAT_COUNT("reschedule_next_with_remaining_repeat_count",
            SimpleScheduleBuilder:: withMisfireHandlingInstructionNextWithRemainingCount,
            SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);

    private final String id;
    private final Consumer<SimpleScheduleBuilder> consumer;
    private final int code;

    /**
     * Creates new misfire instruction option.
     * @param id internal enum id
     * @param consumer action to perform on {@link SimpleScheduleBuilder} to apply target misfire instruction
     * @param code Quartz-specific integer number of target misfire instruction (MISFIRE_INSTRUCTION constant value)
     */
    SimpleTriggerMisfireInstruction(String id, Consumer<SimpleScheduleBuilder> consumer, int code) {
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
     * Calls necessary method of {@link SimpleScheduleBuilder} to apply target misfire instruction
     * @param builder schedule builder
     */
    public void applyInstruction(SimpleScheduleBuilder builder) {
        consumer.accept(builder);
    }

    @Nullable
    public static SimpleTriggerMisfireInstruction fromId(String id) {
        for (SimpleTriggerMisfireInstruction at : SimpleTriggerMisfireInstruction.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    @Nullable
    public static SimpleTriggerMisfireInstruction fromCode(int code) {
        for (SimpleTriggerMisfireInstruction value : SimpleTriggerMisfireInstruction.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}