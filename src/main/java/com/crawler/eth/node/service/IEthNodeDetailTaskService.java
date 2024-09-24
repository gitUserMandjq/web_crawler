package com.crawler.eth.node.service;

import com.crawler.eth.node.model.EthNodeDetailModel;
import com.crawler.eth.node.model.EthNodeDetailTaskModel;

public interface IEthNodeDetailTaskService {
    void addBackupTask(EthNodeDetailModel detail, boolean force) throws Exception;

    boolean isBackup(EthNodeDetailModel detail);

    void finishBackup(EthNodeDetailModel detail);

    EthNodeDetailTaskModel getLastestDetailTask(Long detailId, String taskType);
}
