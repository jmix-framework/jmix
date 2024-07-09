import {ProcessDefinition} from "../../core/types.ts";
import {Card, Col, Empty, Flex, Result, Row, Tooltip} from "antd";
import {useCallback, useEffect, useState} from "react";
import {AxiosError} from "axios";
import useBreakpoint from "antd/es/grid/hooks/useBreakpoint";
import Text from "antd/es/typography/Text";
import {StartProcessButton} from "./StartProcessButton.tsx";
import {DataLoading} from "../common/DataLoading.tsx";
import Title from "antd/es/typography/Title";
import {useAuth} from "react-oidc-context";
import {
    getProcessDefinitionRecordRepresentation
} from "../../core/record-representation/getProcessDefinitionRecordRepresentation.ts";
import {loadProcessDefinitions} from "../../core/data-provider/processDataProvider.ts";
import "./ProcessListPage.css";
import {SearchInput} from "./SearchInput.tsx";
import {useSearchParams} from "react-router-dom";

export const ProcessListPage = () => {
    const {user} = useAuth();
    const [loading, setLoading] = useState(true);
    const [searchParams] = useSearchParams();
    const [error, setError] = useState<AxiosError | undefined>();
    const [processList, setProcessList] = useState<ProcessDefinition[]>([]);
    const [searchString, setSearchString] = useState<string | undefined | null>(searchParams.get("q"));

    useEffect(() => {
        loadProcessDefinitions(user?.access_token, searchString ? {nameOrKeyOrDescriptionLike: searchString} : undefined)
            .then(value => {
                setLoading(false);
                setProcessList(value?.data);
            }).catch(error => {
            console.log("Error on process definitions loading:", error);
            setLoading(false);
            setError(error);
        });
    }, [user?.access_token, searchString]);

    const handleSearch = useCallback((newSearchString?: string | null) => {
        if (searchString != newSearchString) {
            setSearchString(newSearchString);
        }
    }, [searchString]);

    if (loading) {
        return <DataLoading/>
    }

    if (error) {
        return <Result
            status="500"
            title="Internal error"
            subTitle="Sorry, something went wrong."
        />
    }
    const totalElements = processList.length;

    return (
        <>
            <Row gutter={[0, 10]}
                 className="process-list-layout-container">
                <Col xs={24} sm={24} md={19} xl={18} xxl={16}>
                    <Flex vertical={true} className="process-list-layout-root" gap={10}>
                        <Flex align="baseline" gap={3}>
                            <Title level={3} style={{marginBottom: 0}}>My processes</Title>
                            {totalElements !== undefined && <Title level={4} style={{marginBottom: 0, fontWeight: "bold"}}
                                                           type="secondary">({totalElements})</Title>}
                        </Flex>
                        <Row gutter={[0, 20]}>
                            <Col xs={24} sm={24} md={24} xl={24}>
                                <SearchInput className="process-list-search-input" handleSearch={handleSearch}/>
                            </Col>
                            <Col xs={24} xl={22}>
                                {processList && processList.length > 0
                                    ? <Row gutter={[12, 18]}>
                                        {processList.map((process: ProcessDefinition) => <Col key={process.id}
                                                                                              style={{display: "flex"}}
                                                                                              xs={{flex: "100%"}}
                                                                                              sm={{flex: "50%"}}
                                                                                              md={{flex: "45%"}}
                                                                                              lg={{flex: "33.3%"}}
                                                                                              xl={{flex: "33.3%"}}>
                                            <ProcessDefinitionCard item={process}/>
                                        </Col>)}
                                    </Row>
                                    : <Empty description={<Text>No processes found</Text>}/>}
                            </Col>
                        </Row>
                    </Flex>
                </Col>
            </Row>
        </>
    );
}

interface ProcessDefinitionCardProps {
    item: ProcessDefinition
}

const ProcessDefinitionCard = ({item}: ProcessDefinitionCardProps) => {
    const {description, key} = item;
    const {lg} = useBreakpoint();

    return (
        <>
            <Card className="process-card"
                  title={<Title level={5}>{getProcessDefinitionRecordRepresentation(item)}</Title>}
                  bordered={true}
                  styles={{body: {height: lg ? "8.5em" : "10em"}}}
                  hoverable={true}
                  actions={[<StartProcessButton processDefinition={item}/>]}>

                <Flex vertical={true} wrap={true}>
                    <Flex gap="small">
                        <Text>Key: </Text>
                        <Text type="secondary">{key}</Text>
                    </Flex>
                    <div className={lg ? "process-card-description-lg" : "process-card-description"}>
                        <Text>Description: </Text>
                        <Tooltip title={description || "-"} placement="right">
                            <Text type="secondary" className={"line-clamp"}>{description || "-"}</Text>
                        </Tooltip>
                    </div>
                </Flex>
            </Card>
        </>
    );
};
