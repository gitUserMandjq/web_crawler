Date.prototype.format = function(format) {

    var date = {

        "M+": this.getMonth() + 1,

        "d+": this.getDate(),

        "H+": this.getHours(),

        "m+": this.getMinutes(),

        "s+": this.getSeconds(),

        "q+": Math.floor((this.getMonth() + 3) / 3),

        "S+": this.getMilliseconds()

    };

    if (/(y+)/i.test(format)) {

        format = format.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length));

    }

    for (var k in date) {

        if (new RegExp("(" + k + ")").test(format)) {

            format = format.replace(RegExp.$1, RegExp.$1.length == 1

                ? date[k] : ("00" + date[k]).substr(("" + date[k]).length));

        }

    }

    return format;

}

Date.prototype.getLocalTime = function (i) {
    if (typeof i !== 'number') return;
    //得到1970年一月一日到现在的秒数
    var len = this.getTime();

    //本地时间与GMT时间的时间偏移差(注意：GMT这是UTC的民间名称。GMT=UTC）
    var offset = this.getTimezoneOffset() * 60000;

    //得到现在的格林尼治时间
    var utcTime = len + offset;

    return new Date(utcTime + 3600000 * i);
}