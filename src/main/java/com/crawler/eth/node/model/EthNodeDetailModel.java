package com.crawler.eth.node.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "eth_node_detail")
@Data
public class EthNodeDetailModel {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键，主键
    @Column(name="nodeId")
    private Long nodeId;//服务器id
    @Column(name="serverName")
    private String serverName;//服务器名称
    @Column(name="serverIp")
    private String serverIp;//服务器ip
    @Column(name="nodeName")
    private String nodeName;//节点名称
    @Column(name="nodeType")
    private String nodeType;//节点类型
    @Column(name="comment")
    private String comment;//节点类型
    @Column(name="lastUpdateTime")
    private Date lastUpdateTime;//最后更新时间
    @Column(name="error")
    private String error;//节点类型
}
