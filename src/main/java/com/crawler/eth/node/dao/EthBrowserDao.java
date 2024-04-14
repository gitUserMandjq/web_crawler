package com.crawler.eth.node.dao;

import com.crawler.eth.node.model.EthBrowserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EthBrowserDao extends JpaRepository<EthBrowserModel,Long>{
}
