import {Card, Drawer, Row, Skeleton} from "antd";
import {useEffect, useState} from "react";
import {TaskFormData, UserTask} from "../../../../core/types.ts";
import {AxiosError} from "axios";
import Col from "antd/es/grid/col";
import {useAuth} from "react-oidc-context";
import "./TaskDetailsLayout.css";
import {TaskFormCard} from "../form/TaskFormCard.tsx";
import {TaskSystemInfoCard} from "../TaskSystemInfoCard.tsx";
import {loadTask, loadTaskFormData} from "../../../../core/data-provider/taskDataProvider.ts";
import Title from "antd/es/typography/Title";
import type {DrawerStyles} from "antd/es/drawer/DrawerPanel";
import {TaskErrorResult} from "../TaskErrorResult.tsx";

const drawerStyles: DrawerStyles = {
    wrapper: {
        width: "100%"
    },
    body: {
        padding: 0,
        scrollbarWidth: "thin"
    }
};

export interface TaskDetailsLayoutProps {
    handleTaskClosed: () => void
    handleTaskCompleted: (task: UserTask) => void
    taskId: string
}

export const TaskDetailsLayout = ({
                                      handleTaskClosed,
                                      taskId: recordId,
                                      handleTaskCompleted
                                  }: TaskDetailsLayoutProps) => {
    const {user} = useAuth();
    const [loading, setLoading] = useState(false);
    const [task, setTask] = useState<UserTask>();
    const [taskForm, setTaskForm] = useState<TaskFormData>();
    const [error, setError] = useState<AxiosError>();

    useEffect(() => {
        if (!task || task.id !== recordId) {
            setLoading(true);
            const token = user?.access_token;
            const getTask = async () => {
                return await loadTask(recordId, token)
                    .then(value => value.data as UserTask).catch(reason => {
                        console.log("Error on task loading: ", reason);
                        throw reason;
                    });
            };

            const getTaskFormData = async () => {
                return await loadTaskFormData(recordId, token)
                    .then(value => value.data as TaskFormData).catch(reason => {
                        console.log("Error on task form loading: ", reason);
                        throw reason;
                    });
            };

            Promise.all([getTask(), getTaskFormData()])
                .then(([userTask, taskForm]) => {
                    setLoading(false);
                    setTask(userTask);
                    setTaskForm(taskForm);
                }).catch(error => {
                setLoading(false);
                setError(error);
            });
        }
    }, [recordId, user, task]);

    if (!recordId) {
        return null;
    }

    if (error) {
        return <TaskErrorResult error={error}/>
    }

    return (
        <>
            <Drawer title={<Title level={4} style={{marginTop: 0, marginBottom: "0.1em"}}>{task?.name}</Title>}
                    open={true} getContainer={false} loading={loading}
                    onClose={handleTaskClosed} styles={drawerStyles}>
                <Row gutter={[{xs: 5, sm: 6, md: 6, xl: 6, xxl: 6}, {xs: 8, sm: 8, md: 8, xl: 3, xxl: 3}]}
                     className="task-details-root-grid-row">
                    <Col xs={24} sm={24} md={24} xl={16}>
                        {!loading ? <TaskFormCard task={task} schema={taskForm?.jsonSchema} initialData={taskForm?.initialData}
                                                  handleTaskCompleted={handleTaskCompleted}/> : <Card>
                            <Skeleton loading={true} active>
                                <div/>
                            </Skeleton>
                        </Card>
                        }
                    </Col>
                    <Col xs={24} sm={24} md={24} xl={8}>
                        <TaskSystemInfoCard task={task} loading={loading}/>
                    </Col>
                </Row>
            </Drawer>
        </>
    );
};
