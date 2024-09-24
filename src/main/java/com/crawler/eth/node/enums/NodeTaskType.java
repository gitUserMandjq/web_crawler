package com.crawler.eth.node.enums;

import lombok.Data;

public class NodeTaskType {
    public static enum BackupEnum{
        PREPARE("0", "准备备份"),
        START("1", "备份开始"),
        END("2", "备份完成"),
        ERROR("3", "备份异常");
        private String code;
        private String description;

        BackupEnum(String code, String description) {
            this.code = code;
            this.description = description;
        }
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static void main(String[] args) {
        System.out.println(BackupEnum.PREPARE.getCode());
    }
}
