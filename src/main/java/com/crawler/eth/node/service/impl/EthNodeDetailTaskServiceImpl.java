package com.crawler.eth.node.service.impl;

import com.crawler.eth.node.dao.EthNodeBackupDao;
import com.crawler.eth.node.dao.EthNodeDetailDao;
import com.crawler.eth.node.dao.EthNodeDetailTaskDao;
import com.crawler.eth.node.enums.NodeTaskType;
import com.crawler.eth.node.model.EthNodeBackupModel;
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
    @Resource
    EthNodeDetailDao ethNodeDetailDao;
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
            lastestDetailTask.setTaskType(EthNodeDetailTaskModel.TASK_BACKUP);
        }
        lastestDetailTask.setCreateTime(new Date());
        lastestDetailTask.setState(NodeTaskType.BackupEnum.PREPARE.getCode());
        ethNodeDetailTaskDao.save(lastestDetailTask);
        detail.setTaskState("准备备份");
        ethNodeDetailDao.save(detail);
    }
    @Override
    public boolean isBackup(EthNodeDetailModel detail, EthNodeBackupModel nodeBackup){
        if(detail == null){
            return false;
        }
        List<EthNodeDetailTaskModel> list = listProgressTask(EthNodeDetailTaskModel.TASK_BACKUP);
        if(!list.isEmpty()){
            int size = 0;
            //备份任务比较占用流量，如果有多个任务，那么只进行第一个任务
            for (EthNodeDetailTaskModel task : list) {
                size++;
                if(task.getNodeDetailId().equals(detail.getId())){
                    if(NodeTaskType.BackupEnum.PREPARE.getCode().equals(task.getState())){
                        //开始备份
                        startBackup(task);
                        detail.setTaskState("开始备份");
                        ethNodeDetailDao.save(detail);
                        return true;
                    }else if(NodeTaskType.BackupEnum.START.getCode().equals(task.getState())
                            && (new Date().getTime() - task.getStartTime().getTime() >= 1000 * 60 * 10)){
                        errorBackup(task, "备份超时");
                        detail.setTaskState("备份超时");
                        ethNodeDetailDao.save(detail);
                        //备份开始10分钟后还没成功
                        return false;
                    }
                }else if(NodeTaskType.BackupEnum.PREPARE.getCode().equals(task.getState())
                        && (new Date().getTime() - task.getCreateTime().getTime() >= 1000 * 60 * 20)){//30分钟内没有开始备份视为超时
                    errorBackup(task, "备份超时");
                }
                //控制同时备份的数量
                if(size >= nodeBackup.getProcessNum()){
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
            ethNodeDetailDao.save(detail);
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
