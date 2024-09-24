package com.crawler.eth.node.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "eth_node_detail_task")
@Data
public class EthNodeDetailTaskModel {

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
    @Column(name="taskType")
    private String taskType;//任务类型
    public static final String TASK_BACKUP = "备份";
    @Column(name="state")
    private String state;//任务状态
    @Column(name="comment")
    private String comment;//备注
    @Column(name="createTime")
    private Date createTime;//任务创建时间
    @Column(name="startTime")
    private Date startTime;//任务开始时间
    @Column(name="endTime")
    private Date endTime;//任务结束时间
}
