import {Card, Flex, notification} from "antd";
import {TaskCommonProps, UserTask} from "../../../../core/types.ts";
import Button from "antd/es/button";
import {CheckOutlined} from "@ant-design/icons";
import {useCallback, useEffect, useRef, useState} from "react";
import {Form} from "@bpmn-io/form-js";
import "@bpmn-io/form-js/dist/assets/form-js.css";
import {submitTaskForm} from "../../../../core/data-provider/taskDataProvider.ts";
import {useAuth} from "react-oidc-context";
import "./TaskFormCard.css";
import Text from "antd/es/typography/Text";

export interface TaskFormCardProps {
    schema?: string
    initialData?: Record<string, unknown>
    handleTaskCompleted: (task: UserTask) => void;
}

interface SubmitFormEvent {
    data: Record<string, unknown>
    errors: Record<string, string[]>
}

export const TaskFormCard = (props: TaskCommonProps & TaskFormCardProps) => {
    const {task, schema, initialData, handleTaskCompleted} = props;
    const formContainerRef = useRef<HTMLDivElement>(null);
    const [formViewer, setFormViewer] = useState<Form | undefined>();
    const [importSchemaError, setImportSchemaError] = useState();
    const {user} = useAuth();
    const [api, contextHolder] = notification.useNotification();

    const handleFormSubmit = useCallback(async (event: SubmitFormEvent) => {
        //debugger
        if (!event.errors || Object.keys(event.errors).length === 0) {
            if (task) {
                await submitTaskForm(task.id, event.data, user?.access_token)
                    .then(() => {
                        handleTaskCompleted(task);
                    }).catch(error => {
                        console.log("Error on task complete: ", error);
                        api.error({
                            message: `Unable to complete task "\${task.name}"`,
                            placement: "top",
                            duration: 3
                        });
                    });
            }
        }
    }, [api, handleTaskCompleted, task, user?.access_token])


    useEffect(() => {
        if (!formViewer) {
            const newFormViewer = new Form({
                container: formContainerRef.current,
            });

            newFormViewer.on("submit", handleFormSubmit);
            setFormViewer(newFormViewer);
        }
    }, [formViewer, handleFormSubmit]);

    useEffect(() => {
        if (formViewer && schema) {
            formViewer.importSchema(JSON.parse(schema), initialData)
                .catch(reason => {
                        console.log("Unable to import schema: ", reason);
                        setImportSchemaError(reason);
                    }
                );
        }
    }, [formViewer, schema, initialData]);

    const onCompleteButtonClick = useCallback(() => {
        formViewer?.submit();
    }, [formViewer]);

    return (
        <>
            {contextHolder}
            <Card bordered={true}>
                <>
                    {importSchemaError && <Text type="danger">Unable to import form schema</Text>}
                    <div id="form-container" ref={formContainerRef} className="jmix-bpm-tasklist__form-container">
                    </div>
                    <Flex align="center" justify="end" style={{width: "100%"}}>
                        <Button type="primary" icon={<CheckOutlined/>} onClick={onCompleteButtonClick}>Complete</Button>
                    </Flex>
                </>
            </Card>
        </>
    )

}