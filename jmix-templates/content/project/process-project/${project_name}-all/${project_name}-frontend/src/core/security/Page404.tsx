import {Result} from "antd";
import Button from "antd/es/button";
import {useNavigate} from "react-router-dom";

export const Page404 = () => {
    const navigate = useNavigate();
    return (
        <>
            <Result
                status="404"
                title="404"
                subTitle="Sorry, we couldn't find the page you were looking for"
                extra={
                    <Button type="primary" onClick={() => navigate(-1)}>
                        Back
                    </Button>
                }
            />
        </>
    );
};