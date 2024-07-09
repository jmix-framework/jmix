import {Result} from "antd";
import {AxiosError} from "axios";

interface ErrorResultProps {
    error: AxiosError
}

export const TaskErrorResult = ({error}: ErrorResultProps) => {
    const {status} = error;
    return (
        <>
            {status === 404 && <Result status={404}
                                       title="Not found"
                                       subTitle="Task does not exist or already completed"
            />}
            {status === 403 && <Result status={403}
                                       title="Forbidden"
                                       subTitle="Access denied"
            />}
            {(status !== 404 && status !== 403) && <Result
                status={500}
                title="Internal error"
                subTitle="Sorry, something went wrong during task details loading."
            />
            }
        </>
    );
};