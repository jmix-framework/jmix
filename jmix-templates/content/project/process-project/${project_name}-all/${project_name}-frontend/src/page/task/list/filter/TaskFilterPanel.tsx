import {Badge, Collapse, Flex, theme} from "antd";
import {TaskFilterForm} from "./TaskFilterForm.tsx";
import {createTaskFilter} from "./createTaskFilter.ts";
import {useCallback, useState} from "react";
import {FilterFilled, RightOutlined} from "@ant-design/icons";
import Title from "antd/es/typography/Title";
import {TaskFilterData, TaskFilterFormData} from "../../../../core/types.ts";

export interface TaskFilterPanelProps {
    handleSubmit: (taskFilters: TaskFilterData) => void
    handleReset: () => void
}

export const TaskFilterPanel = ({handleReset, handleSubmit}: TaskFilterPanelProps) => {
    const [hideFilters, setHideFilters] = useState<boolean>(true);
    const [filterValues, setFilterValues] = useState<TaskFilterFormData>();

    const handleFilterFormSubmit = useCallback((taskFilters: TaskFilterFormData) => {
        setFilterValues(taskFilters);
        const taskFilter = createTaskFilter(taskFilters);
        if (taskFilter) {
            handleSubmit(taskFilter);
        } else {
            handleReset();
        }

    }, [handleSubmit, handleReset]);

    const handleFilterFormReset = useCallback(() => {
        setFilterValues(undefined);
        handleReset();
    }, [handleReset]);

    const onTaskFilterPanelChange = useCallback((key: string | string[]) => setHideFilters(!key || key.length === 0), []);

    return (
        <>
            <Collapse style={{width: "100%"}} onChange={onTaskFilterPanelChange} items={[{
                key: "1",
                label: <TaskFilterHeader hideFilters={hideFilters} filterValues={filterValues}/>,
                showArrow: false,
                children: <TaskFilterForm handleSubmit={handleFilterFormSubmit} handleReset={handleFilterFormReset}
                />
            }]}/>
        </>
    );
};

interface TaskFilterHeaderProps {
    hideFilters: boolean
    filterValues?: TaskFilterFormData
}

const TaskFilterHeader = ({filterValues, hideFilters}: TaskFilterHeaderProps) => {
    const {
        token: {colorPrimary, colorInfoTextHover, colorTextTertiary, colorTextSecondary},
    } = theme.useToken();

    const calcValuesCount = useCallback(() => {
        if (!filterValues) {
            return undefined;
        }
        const {name, process, dueDate, createDatePeriod, dueDatePeriod, priority} = filterValues;
        let count = 0;
        if (name && name.length > 0) count++;
        if (process && process.length > 0) count++;
        if (dueDate && (filterValues.dueDate !== "period" || dueDatePeriod?.length > 0)) count++;
        if (createDatePeriod && createDatePeriod.length > 0) count++;
        if (priority) count++;

        return count;

    }, [filterValues]);

    const filterCount = calcValuesCount();
    const showFiltersCount = filterValues && filterCount !== undefined && filterCount > 0;

    return (
        <>
            <Flex align="center" gap={5} vertical={false} wrap={true} justify="flex-start">
                <FilterFilled
                    style={{
                        height: "100%",
                        marginTop: "0.1em",
                        color: hideFilters ? colorTextTertiary : colorPrimary
                    }}/>
                <Title level={5} style={{margin: 0}}>Filter</Title>
                {showFiltersCount && <Badge count={filterCount} color={colorInfoTextHover} style={{boxShadow: "none"}}/>}
                <RightOutlined rotate={hideFilters ? 0 : 90}
                               style={{
                                   marginTop: "0.3em",
                                   width: "0.6em", height: "0.6em",
                                   color: colorTextSecondary
                               }}/>
            </Flex>
        </>
    );
};