package com.crawler.eth.node.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "eth_node_backup")
@Data
public class EthNodeBackupModel {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键，主键
    @Column(name="ip")
    private String ip;//ip
    @Column(name="admin")
    private String admin;//账号
    @Column(name="password")
    private String password;//密码
    @Column(name="port")
    private String port;//端口
    @Column(name="filePath")
    private String filePath;//存放路径
    @Column(name="processNum")
    private Integer processNum;//线程数
}
