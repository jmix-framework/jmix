import {useCallback, useEffect, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {Input} from "antd";
import {SearchOutlined} from "@ant-design/icons";

interface SearchInputProps {
    className: string,
    handleSearch: (searchString?: string | null) => void
}

export const SearchInput = ({handleSearch, className}: SearchInputProps,) => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [searchTerm, setSearchTerm] = useState<undefined | string | null>(searchParams.get("q"));

    useEffect(() => {
        if (searchTerm) {
            setSearchParams({q: searchTerm});
        } else {
            setSearchParams({});
        }

        if (!searchTerm || searchTerm.length === 0) {
            handleSearch(searchTerm);
        } else {
            const delayDebounceFn = setTimeout(() => {
                handleSearch(searchTerm);
            }, 800);

            return () => clearTimeout(delayDebounceFn);
        }

    }, [searchTerm, handleSearch]);

    const onSearchInputChange = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setSearchTerm(event.target.value);
    }, []);

    return (
        <>
            <Input placeholder="Search by name, key or description..."
                   className={className}
                   allowClear={true}
                   onChange={onSearchInputChange}
                   value={searchTerm ? searchTerm : ""}
                   prefix={<SearchOutlined style={{color: "rgba(0,0,0,.25)"}}/>}
            />
        </>
    );
};