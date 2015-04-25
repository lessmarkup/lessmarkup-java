function LocaleDateFilter(date: any) {
    if (typeof (date) == "undefined" || date == null) {
        return "";
    }
    if (!(date instanceof Date)) {
        date = new Date(date);
    }
    return date.toLocaleDateString();
}

import module = require('./module');
module.filter('localeDate', LocaleDateFilter);