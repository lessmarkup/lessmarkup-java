app.filter('relativeDate', function() {
    return function(date) {
        var now = new Date();

        function calculateDelta() {
            return Math.round((now - date) / 1000);
        }

        if (!(date instanceof Date)) {
            date = new Date(date);
        }
        var delta = calculateDelta();
        var minute = 60;
        var hour = minute * 60;
        var day = hour * 24;
        var week = day * 7;
        var month = day * 30;
        var year = day * 365;
        if (delta > day && delta < week) {
            date = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0);
            delta = calculateDelta();
        }
        switch (true) {
        case (delta < 30):
            return '[[#text]]JustNow[[/text]]';
        case (delta < minute):
            return "" + delta + " [[#text]]SecondsAgo[[/text]]";
        case (delta < 2 * minute):
            return '[[#text]]MinuteAgo[[/text]]';
        case (delta < hour):
            return "" + (Math.floor(delta / minute)) + " [[#text]]MinutesAgo[[/text]]";
        case Math.floor(delta / hour) == 1:
            return '[[#text]]HourAgo[[/text]]';
        case (delta < day):
            return "" + (Math.floor(delta / hour)) + " [[#text]]HoursAgo[[/text]]";
        case (delta < day * 2):
            return '[[#text]]Yesterday[[/text]]';
        case (delta < week):
            return "" + (Math.floor(delta / day)) + " [[#text]]DaysAgo[[/text]]";
        case Math.floor(delta / week) == 1:
            return '[[#text]]WeekAgo[[/text]]';
        case (delta < month):
            return "" + (Math.floor(delta / week)) + " [[#text]]WeeksAgo[[/text]]";
        case Math.floor(delta / month) == 1:
            return '[[#text]]MonthAgo[[/text]]';
        case (delta < year):
            return "" + (Math.floor(delta / month)) + " [[#text]]MonthsAgo[[/text]]";
        case Math.floor(delta / year) == 1:
            return '[[#text]]YearAgo[[/text]]';
        default:
            return '[[#text]]OverYearAgo[[/text]]';
        }
    };
});
