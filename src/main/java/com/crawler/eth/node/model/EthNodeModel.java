package com.crawler.eth.node.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "eth_node")
@Data
public class EthNodeModel {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键，主键
    @Column(name="name")
    private String name;//名称
    @Column(name="url")
    private String url;//地址
    @Column(name="state")
    private String state;//状态
    @Column(name="lastUpdateTime")
    private Date lastUpdateTime;//最后更新时间
    @Column(name="lastStopTime")
    private Date lastStopTime;//最后停止时间
    @Column(name="nodeType")
    private String nodeType;//节点类型

    public static final String NODETYPE_SHARDEUM = "shardeum";
}