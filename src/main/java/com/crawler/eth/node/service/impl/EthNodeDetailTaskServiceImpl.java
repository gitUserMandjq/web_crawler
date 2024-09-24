package com.crawler.eth.node.service.impl;

import com.crawler.eth.node.dao.EthNodeDetailTaskDao;
import com.crawler.eth.node.enums.NodeTaskType;
import com.crawler.eth.node.model.EthNodeDetailModel;
import com.crawler.eth.node.model.EthNodeDetailTaskModel;
import com.crawler.eth.node.service.IEthNodeDetailTaskService;
import com.crawler.eth.node.service.IEthNodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class EthNodeDetailTaskServiceImpl implements IEthNodeDetailTaskService {
    @Resource
    EthNodeDetailTaskDao ethNodeDetailTaskDao;
    @Resource
    IEthNodeService ethNodeService;
    public List<EthNodeDetailTaskModel> listProgressTask(String taskType){
        List<EthNodeDetailTaskModel> taskList = ethNodeDetailTaskDao.listProgressTask(taskType);
        return taskList;
    }
    @Override
    public void addBackupTask(EthNodeDetailModel detail, boolean force) throws Exception {
        EthNodeDetailTaskModel lastestDetailTask = getLastestDetailTask(detail.getId(), EthNodeDetailTaskModel.TASK_BACKUP);
        if(!force && lastestDetailTask != null
                && (NodeTaskType.BackupEnum.PREPARE.getCode().equals(lastestDetailTask.getState())
                    || NodeTaskType.BackupEnum.START.getCode().equals(lastestDetailTask.getState()))){
            throw new Exception("备份任务已经存在,请等任务结束");
        }
        if(lastestDetailTask == null){
            lastestDetailTask = new EthNodeDetailTaskModel();
            lastestDetailTask.setNodeDetailId(detail.getId());
            lastestDetailTask.setNodeName(detail.getNodeName());
            lastestDetailTask.setNodeType(detail.getNodeType());
        }
        lastestDetailTask.setCreateTime(new Date());
        lastestDetailTask.setState(NodeTaskType.BackupEnum.PREPARE.getCode());
        ethNodeDetailTaskDao.save(lastestDetailTask);
        detail.setTaskState("准备备份");
        ethNodeService.update(detail);
    }
    @Override
    public boolean isBackup(EthNodeDetailModel detail){
        if(detail == null){
            return false;
        }
        List<EthNodeDetailTaskModel> list = listProgressTask(EthNodeDetailTaskModel.TASK_BACKUP);
        if(!list.isEmpty()){
            //备份任务比较占用流量，如果有多个任务，那么只进行第一个任务
            EthNodeDetailTaskModel task = list.get(0);
            if(task.getNodeDetailId().equals(detail.getId())){
                if(NodeTaskType.BackupEnum.PREPARE.getCode().equals(task.getState())){
                    //开始备份
                    startBackup(task);
                    detail.setTaskState("开始备份");
                    ethNodeService.update(detail);
                    return true;
                }else if(NodeTaskType.BackupEnum.START.getCode().equals(task.getState())
                    && (new Date().getTime() - task.getStartTime().getTime() >= 1000 * 60 * 10)){
                    errorBackup(task, "备份超时");
                    detail.setTaskState("备份超时");
                    ethNodeService.update(detail);
                    //备份开始10分钟后还没成功
                    return false;
                }
            }
        }
        return false;
    }
    @Override
    public void finishBackup(EthNodeDetailModel detail){
        EthNodeDetailTaskModel lastestDetailTask = getLastestDetailTask(detail.getId(), EthNodeDetailTaskModel.TASK_BACKUP);
        if(lastestDetailTask != null){
            finishBackup(lastestDetailTask);
            detail.setTaskState("完成备份");
            ethNodeService.update(detail);
        }
    }
    public void startBackup(EthNodeDetailTaskModel task){
        task.setState(NodeTaskType.BackupEnum.START.getCode());
        task.setStartTime(new Date());
        ethNodeDetailTaskDao.save(task);
    }
    public void finishBackup(EthNodeDetailTaskModel task){
        task.setState(NodeTaskType.BackupEnum.END.getCode());
        task.setEndTime(new Date());
        ethNodeDetailTaskDao.save(task);
    }
    public void errorBackup(EthNodeDetailTaskModel task, String comment){
        task.setState(NodeTaskType.BackupEnum.ERROR.getCode());
        task.setEndTime(new Date());
        task.setComment(comment);
        ethNodeDetailTaskDao.save(task);
    }
    @Override
    public EthNodeDetailTaskModel getLastestDetailTask(Long detailId, String taskType){
        return ethNodeDetailTaskDao.getLastestDetailTask(detailId, taskType);
    }
}
