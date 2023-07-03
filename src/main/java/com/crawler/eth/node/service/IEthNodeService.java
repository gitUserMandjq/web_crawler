package com.crawler.eth.node.service;

import com.crawler.eth.node.model.EthNodeModel;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public interface IEthNodeService {
    /**
     * 获得节点信息
     * @param nodeType
     * @return
     */
    List<EthNodeModel> listNodeByNodeType(String nodeType);

    /**
     * 登陆shardeum
     * @param client
     * @param node
     * @return
     */
    String loginShardeum(OkHttpClient client, EthNodeModel node) throws IOException, NoSuchAlgorithmException, KeyManagementException;
    /**
     * 获取shardeum节点状态
     * @param client
     * @param node
     * @return
     */
    Map<String, Object> getShardeumStatus(OkHttpClient client, EthNodeModel node, String accessToken) throws IOException;
    /**
     * 停止shardeum节点
     * @param client
     * @param node
     * @return
     */
    String stopShardeumNode(OkHttpClient client, EthNodeModel node, String accessToken) throws IOException;
    /**
     * 开启shardeum节点
     * @param client
     * @param node
     * @return
     */
    String startShardeumNode(OkHttpClient client, EthNodeModel node, String accessToken) throws IOException;
}