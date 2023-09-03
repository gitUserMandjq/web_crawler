package com.crawler.eth.node.dao;

import com.crawler.eth.node.model.EthNodeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EthNodeDao extends JpaRepository<EthNodeModel,Long>{
    @Query(value="select u from EthNodeModel u where u.nodeType = ?1 and u.enabled = 1")
    List<EthNodeModel> findByNodeType(String nodeType);
}
