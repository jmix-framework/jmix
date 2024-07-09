import {Dayjs} from "dayjs";

export interface TaskCommonProps {
    task?: UserTask
}

export interface ProcessDefinition {
    id: string,
    name?: string
    version: number
    description?: string
    key: string
}

export interface TaskSort {
    order: SortDirection
    property: TaskSortProperty
}

export enum TaskSortProperty {
    NAME = "name",
    DUE_DATE = "dueDate",
    CREATE_DATE = "createDate",
    PRIORITY = "priority"
}

export enum SortDirection {
    ASC = "asc",
    DESC = "desc"
}

export interface TaskFormData {
    jsonSchema?: string
    formKey: string
    version?: number
    initialData?: Record<string, unknown>
}

export interface TaskTableSortData {
    order: string
    property: string
}

export interface UserTaskResponse {
    data: UserTask[]
    totalElements: number
}

export interface UserTask {
    id: string
    name: string
    description?: string | null
    dueDate?: string | null
    createDate?: string,
    taskDefinitionKey: string
    formKey?: string | null
    priority: number
    processDefinition: ProcessDefinition
    assignee?: string
}

export interface TaskFilterFormData {
    name?: string
    process?: string
    priority?: "low" | "normal" | "high"
    dueDate: "overdue" | "noDueDate" | "today" | "period"
    dueDatePeriod: Dayjs[]
    createDatePeriod: Dayjs[]
}

export interface TaskFilterData {
    nameLike?: string
    processDefinitionNameLike?: string
    minPriority?: number
    maxPriority?: number
    withoutDueDate?: boolean
    dueDateBefore?: string
    dueDateAfter?: string
    createDateBefore?: string
    createDateAfter?: string
}

export interface PageData {
    page: number
    size: number
}

export interface ProcessFilter {
    nameOrKeyOrDescriptionLike?: string
}

export interface UserTaskStatistics {
    activeTasksCount?: number
    overdueTasksCount?: number
    monthlyStatistics?: TaskExecutionStatistics
    lastCreatedTasks?: UserTask[]
    upcomingTasks?: UserTask[]
    weeklyActivity?: TaskExecutionPeriodStatistics
}

export interface TaskExecutionStatistics {
    totalTasks: number
    completedTasksCount: number
}

export interface TaskExecutionPeriodStatistics extends TaskExecutionStatistics {
    items: TaskExecutionDateStatistics[]
}

export interface TaskExecutionDateStatistics extends TaskExecutionStatistics {
    date: string
}