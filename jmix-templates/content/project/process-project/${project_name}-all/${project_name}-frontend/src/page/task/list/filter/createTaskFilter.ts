import dayjs, {Dayjs} from "dayjs";
import {TaskFilterData, TaskFilterFormData} from "../../../../core/types.ts";

const DATE_TIME_FORMAT = "YYYY-MM-DDTHH:mm:ss.SSSZZ";

const minPriorities: Record<string, number> = {
    "low": 0,
    "normal": 40,
    "high": 60
}

const maxPriorities: Record<string, number> = {
    "low": 39,
    "normal": 59,
}

export const createTaskFilter = (formData: TaskFilterFormData) => {
    const filterData: TaskFilterData = {};

    if (formData.name && formData.name.length > 0) {
        filterData.nameLike = formData.name;
    }
    if (formData.process && formData.process.length > 0) {
        filterData.processDefinitionNameLike = formData.process;
    }

    if (formData.priority) {
        filterData.minPriority = minPriorities[formData.priority];
        filterData.maxPriority = maxPriorities[formData.priority];
    }

    if (formData.dueDate === "overdue") {
        filterData.dueDateBefore = dayjs().format(DATE_TIME_FORMAT);
    } else if (formData.dueDate === "today") {
        filterData.dueDateAfter = getStartOfDay(dayjs()).format(DATE_TIME_FORMAT);
        filterData.dueDateBefore = getEndOfDay(dayjs()).format(DATE_TIME_FORMAT);
    } else if (formData.dueDate === "period" && formData.dueDatePeriod && formData.dueDatePeriod.length === 2) {
        filterData.dueDateAfter = getStartOfDay(formData.dueDatePeriod[0]).format(DATE_TIME_FORMAT);
        filterData.dueDateBefore = getEndOfDay(formData.dueDatePeriod[1]).format(DATE_TIME_FORMAT);
    } else if (formData.dueDate === "noDueDate") {
        filterData.withoutDueDate = true;
    }

    if (formData.createDatePeriod && formData.createDatePeriod.length === 2) {
        filterData.createDateAfter = getStartOfDay(formData.createDatePeriod[0]).format(DATE_TIME_FORMAT);
        filterData.createDateBefore = getEndOfDay(formData.createDatePeriod[1]).format(DATE_TIME_FORMAT);
    }

    const hasAnyValue = Object.keys(filterData).find(value => (filterData as Record<string, unknown>)[value] !== undefined);

    if (!hasAnyValue) {
        return undefined;
    }
    return filterData;
}

const getStartOfDay = (date: Dayjs) => {
    const result = dayjs.isDayjs(date) ? date : dayjs(date);
    return result.hour(0).minute(0).second(0).millisecond(0);
}

const getEndOfDay = (date: Dayjs) => {
    return date.hour(23).minute(59).second(59).millisecond(0);
}