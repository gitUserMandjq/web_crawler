package com.crawler.eth.node.dao;

import com.crawler.eth.node.model.EthNodeDetailModel;
import com.crawler.eth.node.model.EthNodeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EthNodeDetailDao extends JpaRepository<EthNodeDetailModel,Long>{
    @Query(value="select u from EthNodeDetailModel u where u.nodeType = ?1 and u.enabled = 1")
    List<EthNodeDetailModel> findByNodeType(String nodeType);
    @Query(value="select u from EthNodeDetailModel u where u.nodeType = ?1 and u.nodeName = ?2 and u.enabled = 1")
    EthNodeDetailModel findByNodeName(String nodeType, String nodeName);
}
