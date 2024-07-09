import {Flex, Result} from "antd";
import {useCallback, useEffect, useState} from "react";
import "./TaskListLayout.css";
import {PageData, TaskFilterData, TaskTableSortData, UserTaskResponse} from "../../../core/types.ts";
import {AxiosError} from "axios";
import {useAuth} from "react-oidc-context";
import {loadTasks} from "../../../core/data-provider/taskDataProvider.ts";
import {useSearchParams} from "react-router-dom";
import Title from "antd/es/typography/Title";
import {getPageDataParams, getSortParams, setPageDataParams} from "../../../core/searchParamsUtils.ts";
import {TaskFilterPanel} from "./filter/TaskFilterPanel.tsx";
import {TaskTable} from "./TaskTable.tsx";

export interface TaskListLayoutProps {
    handleTaskSelected: (taskId: string) => void
    lastCompletedTask?: string
    handleErrorOnLoad: () => void
}

const defaultPageData: PageData = {
    page: 1,
    size: 10
}

const defaultSort: TaskTableSortData = {
    order: "desc",
    property: "createDate"
};


export const TaskListLayout = ({handleTaskSelected, lastCompletedTask, handleErrorOnLoad}: TaskListLayoutProps) => {
    const {user} = useAuth();
    const [taskListResponse, setTaskListResponse] = useState<UserTaskResponse | undefined>();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<AxiosError | undefined>();
    const [searchParams, setSearchParams] = useSearchParams();
    const [pageData, setPageData] = useState<PageData>(getPageDataParams(searchParams) || defaultPageData);
    const [filterData, setFilterData] = useState<TaskFilterData | undefined>();
    const [taskSortData, setTaskSortData] = useState<TaskTableSortData | undefined>(getSortParams(searchParams) || defaultSort);

    useEffect(() => {
        const token = user?.access_token;
        loadTasks(token, pageData, taskSortData, filterData)
            .then(value => {
                setLoading(false);
                setTaskListResponse(value.data);
            })
            .catch(reason => {
                console.log("Error on task list loading:", reason);
                setLoading(false);
                setError(reason);
                handleErrorOnLoad();
            });
    }, [pageData, user?.access_token, lastCompletedTask, handleErrorOnLoad, filterData]);


    const handleFilterChange = useCallback((taskFilters?: TaskFilterData) => {
        setFilterData(taskFilters);
        setPageDataParams(searchParams, 1, pageData.size);
        setSearchParams(searchParams);
        setPageData({page: 1, size: pageData.size});
    }, [pageData.size, searchParams, setSearchParams]);

    const handlePageDataChange = useCallback((pageData: PageData) => {
        setPageData(pageData);
    }, []);

    const handleSortChange = useCallback((taskSortData?: TaskTableSortData) => {
        setTaskSortData(taskSortData);
    }, []);

    if (error) {
        return <Result
            status="500"
            title="Internal error"
            subTitle="Sorry, something went wrong during tasks loading."
        />
    }

    const totalElements = taskListResponse?.totalElements;

    return (
        <>
            <Flex vertical={true} className="task-list-layout-root" gap={10}>
                <Flex align="baseline" gap={3}>
                    <Title level={3} style={{marginBottom: 0}}>My tasks</Title>
                    {totalElements !== undefined && <Title level={4} style={{marginBottom: 0, fontWeight: "bold"}}
                                                           type="secondary">({totalElements})</Title>}
                </Flex>
                <TaskFilterPanel handleSubmit={handleFilterChange}
                                 handleReset={handleFilterChange}/>
                <TaskTable data={taskListResponse?.data} totalElements={taskListResponse?.totalElements}
                           loading={loading}
                           handlePageDataChange={handlePageDataChange}
                           handleSortChange={handleSortChange}
                           handleTaskSelected={handleTaskSelected}/>
            </Flex>
        </>
    );
}

