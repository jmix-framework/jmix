import Button from "antd/es/button";
import {PlayCircleOutlined} from "@ant-design/icons";
import {useCallback} from "react";
import {ProcessDefinition} from "../../core/types.ts";
import {notification} from "antd";
import {useAuth} from "react-oidc-context";
import {startProcess} from "../../core/data-provider/processDataProvider.ts";
import {
    getProcessDefinitionRecordRepresentation
} from "../../core/record-representation/getProcessDefinitionRecordRepresentation.ts";
import "./StartProcessButton.css";

interface StartProcessButtonProps {
    processDefinition: ProcessDefinition
}

export const StartProcessButton = ({processDefinition}: StartProcessButtonProps) => {
    const [api, contextHolder] = notification.useNotification();
    const {user} = useAuth();

    const onClick = useCallback(async () => {
        const token = user?.access_token;
        const processRecordRepresentation = getProcessDefinitionRecordRepresentation(processDefinition);
        await startProcess(processDefinition.id, token).then(() => {
            api.success({
                message: `The "\${processRecordRepresentation}" process has been started`,
                placement: "top",
                duration: 3
            });
        }).catch((error) => {
            console.log("Error on process starting: ", error);
            api.error({
                message: `The "\${processRecordRepresentation}" process has not been started`,
                placement: "top",
                duration: 3
            });
        });
    }, [api, processDefinition, user?.access_token]);

    return (
        <>
            {contextHolder}
            <Button type="primary" icon={<PlayCircleOutlined/>} className="start-process-btn" onClick={onClick}>Start
                process</Button>
        </>
    );
};