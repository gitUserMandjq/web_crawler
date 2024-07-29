package com.crawler.eth.node.service;

import com.crawler.base.utils.JSchUtil;
import com.crawler.eth.node.model.EthNodeDetailModel;
import com.crawler.eth.node.model.EthNodeModel;
import com.jcraft.jsch.JSchException;
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

    List<EthNodeDetailModel> listNodeDetailByNodeType(String nodeType);

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

    void update(EthNodeModel node);

    void update(EthNodeDetailModel node);

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

    void upgradeShardeumNode(EthNodeModel node) throws IOException, JSchException;

    JSchUtil connectToNode(EthNodeModel node);

    void restartShardeumNode(EthNodeModel node) throws IOException, JSchException;

    void restartIonetNode(EthNodeModel node) throws IOException, JSchException;

    /**
     * 获取opside状态
     * @param client
     * @param nodeList
     * @return
     * @throws Exception
     */
    void getOpsideStatus(OkHttpClient client, List<EthNodeModel> nodeList)throws Exception;

    void startOpsideNode(EthNodeModel node) throws IOException, JSchException;

    void getAvailSessionKey(EthNodeModel node) throws IOException, JSchException;

    void obtainQuiliBalance() throws InterruptedException;

    void obtainQuiliBalance(EthNodeDetailModel ethNodeDetailModel);

    void obtainQuiliBalance(Long detailId);
}
