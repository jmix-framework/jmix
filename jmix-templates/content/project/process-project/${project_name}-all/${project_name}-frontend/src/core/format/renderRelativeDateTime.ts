import dayjs, {Dayjs} from "dayjs";
import duration from 'dayjs/plugin/duration';
import Pluralize from 'pluralize';

dayjs.extend(duration);

export const renderRelativeDateTime = (date1?: string | number, date2?: string | number) => {
    if (!date1 || !date2) {
        return "Invalid date";
    }

    const value1 = dayjs(date1);
    const value2 = dayjs(date2);

    return getDateTimeDiff(value1, value2);
}

const getDateTimeDiff = (date1: Dayjs, date2: Dayjs) => {
    const diff = dayjs.duration(date1.diff(date2));
    const days = diff.days();
    const hours = diff.hours();
    const minutes = diff.minutes();
    const seconds = diff.seconds();

    if (days > 0) {
        return `\${days} \${Pluralize("day", days)} \${hours > 0 ?  hours + " " + Pluralize("hour", hours) : ""}`
    }
    if (hours > 0 && minutes > 0) {
        return `\${hours} \${Pluralize("hour", hours)} \${minutes > 0 ? minutes + " min" : ""}`;
    }

    if (minutes > 0) {
        return `\${minutes} min`;
    }

    if (seconds > 0) {
        return `\${seconds} sec`;
    }

    if (days === 0 && hours === 0 && minutes === 0 && hours === 0) {
        return "Now";
    }
}