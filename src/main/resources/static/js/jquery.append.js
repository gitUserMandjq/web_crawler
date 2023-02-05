
(function(window, $) {
    $.fn.serializeJson = function() {//获得form的json对象
        var serializeObj = {};
        var array = this.serializeArray();
        $(array).each(
            function() {
                if (serializeObj[this.name]) {
                    if ($.isArray(serializeObj[this.name])) {
                        serializeObj[this.name].push(this.value);
                    } else {
                        serializeObj[this.name] = [
                            serializeObj[this.name], this.value ];
                    }
                } else {
                    serializeObj[this.name] = this.value;
                }
            });
        return serializeObj;
    };
    $.extend({
        // 设置 apDiv
        getAjax:function (url, formData, func) {
            $.ajax({
                type: 'GET',
                url: url,
                data: formData,
                dataType: "json",
                success: function(result) {
                    if(isError(result)){
                        layer.msg(result.errorMessage);
                        return;
                    }
                    func(result);
                },
                error : function(data){

                }
            });
        }
    });
})(window, jQuery);