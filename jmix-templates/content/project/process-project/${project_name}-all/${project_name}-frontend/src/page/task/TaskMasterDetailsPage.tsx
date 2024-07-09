import {notification, Row} from "antd";
import {useSearchParams} from "react-router-dom";
import {TaskListLayout} from "./list/TaskListLayout.tsx";
import {TaskDetailsLayout} from "./details/layout/TaskDetailsLayout.tsx";
import Col from "antd/es/grid/col";
import {useCallback, useState} from "react";
import {deleteTaskParam, getTaskParam, setTaskParam} from "../../core/searchParamsUtils.ts";
import {UserTask} from "../../core/types.ts";
import "./TaskMaterDetailsPage.css";
import {AppFooter} from "../../core/layout/footer/AppFooter.tsx";

export const TaskMasterDetailsPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [selectedRowKey, setSelectedRowKey] = useState<string | null | undefined>(getTaskParam(searchParams));
    const [lastCompletedTask, setLastCompletedTask] = useState<string>();
    const [api, contextHolder] = notification.useNotification();

    const handleTaskSelected = useCallback((taskId: string) => {
        if (taskId !== getTaskParam(searchParams)) {
            setTaskParam(searchParams, taskId);
            setSearchParams(searchParams);
            setSelectedRowKey(taskId);
        }
    }, [searchParams, setSearchParams]);

    const handleTaskClosed = useCallback(() => {
        deleteTaskParam(searchParams);
        setSearchParams(searchParams);
        setSelectedRowKey(undefined);
    }, [searchParams, setSearchParams]);

    const handleTaskCompleted = (task: UserTask) => {
        handleTaskClosed();
        api.success({
            message: `Task "\${task?.name}" completed`,
            placement: "top",
            duration: 3
        });
        setLastCompletedTask(task.id);
    };

    const handleErrorOnTaskListLoad = useCallback(() => {
        deleteTaskParam(searchParams);
        setSearchParams(searchParams);
        setSelectedRowKey(undefined);
    }, [searchParams, setSearchParams]);

    return (
        <>
            <Row className="task-master-details-root-container">
                <Col xs={selectedRowKey ? 0 : 24}
                     sm={selectedRowKey ? 0: 24}
                     md={selectedRowKey ? 10 : 19}
                     xl={selectedRowKey ? 10 : 18}
                     xxl={selectedRowKey ? 10 : 16}
                     className="task-list-root-container" style={{paddingBottom: selectedRowKey ? "2.5em" : 0}}>
                    <TaskListLayout handleTaskSelected={handleTaskSelected}
                                    lastCompletedTask={lastCompletedTask}
                                    handleErrorOnLoad={handleErrorOnTaskListLoad}/>
                    {selectedRowKey && <AppFooter justify="start"/>}
                </Col>
                {selectedRowKey && <Col xs={24} sm={24} md={14} xl={14}>
                    <TaskDetailsLayout handleTaskClosed={handleTaskClosed} taskId={selectedRowKey}
                                       handleTaskCompleted={handleTaskCompleted}/>
                </Col>}
            </Row>
            {contextHolder}
        </>
    );
};