package com.crawler.eth.node.dao;

import com.crawler.eth.node.model.EthBrowserModel;
import com.crawler.eth.node.model.EthNodeBackupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EthNodeBackupDao extends JpaRepository<EthNodeBackupModel,Long>{
}
