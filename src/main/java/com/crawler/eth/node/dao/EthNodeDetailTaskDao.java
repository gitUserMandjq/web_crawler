package com.crawler.eth.node.dao;

import com.crawler.eth.node.model.EthNodeDetailModel;
import com.crawler.eth.node.model.EthNodeDetailTaskModel;
import com.crawler.eth.node.model.EthNodeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;


@Repository
public interface EthNodeDetailTaskDao extends JpaRepository<EthNodeDetailTaskModel,Long>{

    @Query(value="select u from EthNodeDetailTaskModel u where u.taskType = ?1 and u.state in ('0','1') order by u.createTime asc")
    List<EthNodeDetailTaskModel> listProgressTask(String taskType);

    @Query(nativeQuery = true, value="select u.* from eth_node_detail_task u where u.nodeDetailId = ?1 and u.taskType = ?2 order by u.createTime desc limit 1")
    EthNodeDetailTaskModel getLastestDetailTask(Long detailId, String taskType);
    @Query(value="select u from EthNodeDetailTaskModel u where u.nodeDetailId in ?1")
    List<EthNodeDetailTaskModel> listTaskByDetailId(Iterable<Long> detailIds);
}
