package com.crawler.eth.node.dao;

import com.crawler.eth.node.model.EthNodeDetailDailyStatModel;
import com.crawler.eth.node.model.EthNodeDetailModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface EthNodeDetailDailyStatDao extends JpaRepository<EthNodeDetailDailyStatModel,Long>{
    @Query(value="select u from EthNodeDetailDailyStatModel u where u.nodeDetailId = ?1 and u.statDate = ?2")
    EthNodeDetailDailyStatModel findByNodeDetailIdAndStatDate(Long nodeDetailId, Date statDate);

    @Query(nativeQuery = true, value="select u.* from eth_node_detail_dailystat u " +
            "where u.nodeDetailId = ?1 and u.statDate < ?2 order by u.statDate desc limit 1")
    EthNodeDetailDailyStatModel findLastDailyStat(Long nodeDetailId, Date statDate);
}
