package com.crawler.eth.node.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "eth_node_detail_dailystat")
@Data
public class EthNodeDetailDailyStatModel {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键，主键
    @Column(name="nodeDetailId")
    private Long nodeDetailId;//节点id
    @Column(name="nodeName")
    private String nodeName;//节点名称
    @Column(name="nodeType")
    private String nodeType;//节点类型
    @Column(name="statDate")
    private Date statDate;//统计日期
    @Column(name="currentValue")
    private BigDecimal currentValue;//当前值
    @Column(name="lastValue")
    private BigDecimal lastValue;//上一个值
    @Column(name="diffValue")
    private BigDecimal diffValue;//当前值和上一个值差额
    @Column(name="lastUpdateTime")
    private Date lastUpdateTime;//最后更新时间
}
