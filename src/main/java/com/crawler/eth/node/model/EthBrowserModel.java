package com.crawler.eth.node.model;

import lombok.Data;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "eth_browser")
@Data
@Proxy(lazy = false)
public class EthBrowserModel {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键

    @Column(name="type")
    private String type;//名称

    @Column(name="account")
    private String account;//账号

    @Column(name="password")
    private String password;//密码

    @Column(name="data")
    private String data;//数据
    @Column(name="cookies")
    private String cookies;//cookies

    @Column(name="lastUpdateTime")
    private Date lastUpdateTime;//最后更新时间

}
