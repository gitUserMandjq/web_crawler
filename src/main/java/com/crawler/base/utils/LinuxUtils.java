package com.crawler.base.utils;

public class LinuxUtils {
    public static String setEnvVars(String paraName, String value){
        // /etc/profile在cron定时器中生效
        // /root/.bashrc在ssh时生效
        String  order = setVars("/etc/profile", "export " + paraName, value);
        order += "  && source /etc/profile";
        return order;
    }
    public static String setVars(String fileName, String paraName, String value){
        String  order = "awk -v v1=\""+value+"\" 'BEGIN{FS=OFS=\"=\";addV1=1} $1==\""+paraName+"\"{addV1=0;$2=v1} {print > \""+fileName+"\"}; END{if(addV1)print \""+paraName+"=\" v1 >> \""+fileName+"\"}' "+fileName;
        return order;
    }
    public static String replace(String fileName, String oldValue, String newValue){
        oldValue = escape(oldValue);
        newValue = escape(newValue);
        String order = "sed -i \"s/"+oldValue+"/"+newValue+"/g\" "+fileName;
        return order;
    }
    private static String escape(String value){
        value = value.replaceAll("/", "\\\\/").replaceAll("&", "\\\\&");
        return value;
    }
    public static String bashNetScript(String url){
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        String order = "wget -O "+fileName+ " " + url + " && chmod +x "+fileName+" && ./"+fileName;
        return order;
    }
    public static String getNetScript(String url){
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        String order = "wget -O "+fileName+" " + url + " && chmod +x "+fileName;
        return order;
    }
    public static String addCrontab(String cron, String task){
        return addCrontab(cron, task, "/dev/null");
    }
    public static String addCrontab(String cron, String task, String logPath){
        //将已有的值和添加的值一起作为输出到crontab -
        String order = "(crontab -l 2>/dev/null;echo \""+cron+" "+task+" >> "+logPath+" 2>&1\") | crontab - && service cron restart";
        return order;
    }
    public static String deleteCrontab(String task){
        //grep -v等于反选
        String order = "crontab -l | grep -v '"+task+"' | crontab -";
        return order;
    }
    public static String enableCronLog(){
        String order = "sed -i 's/.*cron.*/cron.*  \\/var\\/log\\/cron.log/g' /etc/rsyslog.d/50-default.conf && service rsyslog restart && service cron restart";
        return order;
    }
    public static void main(String[] args) {
        String url = "https://raw.githubusercontent.com/gitUserMandjq/linuxScript/master/blockchain/monitor/quilimonitor.sh";
        String order = bashNetScript(url);
        System.out.println(order);
        System.out.println(escape("&"));
    }
    public static class Screen{
        public static String quitScreen(String screenName){
            return "screen -S "+screenName+" -X quit";
        }
    }
}
