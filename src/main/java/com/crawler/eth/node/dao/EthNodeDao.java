package com.crawler.eth.node.dao;

import com.crawler.eth.node.model.EthNodeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EthNodeDao extends JpaRepository<EthNodeModel,Long>{
    List<EthNodeModel> findByNodeType(String nodeType);
}
