package com.crawler.eth.node.service.impl;

import com.crawler.base.utils.JSchUtil;
import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthNodeService;
import com.crawler.eth.node.service.IMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class MonitorServiceImpl implements IMonitorService {
    @Resource
    IEthNodeService ethNodeService;
    @Override
    public String genereatePrometheusYml(){
        StringBuilder sb = new StringBuilder();
        sb.append("global:\n" +
                "  scrape_interval:   60s\n" +
                "  evaluation_interval: 60s\n" +
                "scrape_configs:\n");
        {//监听服务器
            List<EthNodeModel> nodeList = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_MONITOR);
            if(!nodeList.isEmpty()){
                EthNodeModel node = nodeList.get(0);
                sb.append("  - job_name: \"节点监控\"\n" +
                        "    static_configs:\n" +
                        "      - targets: ['"+node.getUrl()+":9100']\n" +
                        "        labels:\n" +
                        "          instance: " + node.getName()+"\n");
            }
        }
        {//shardeum服务器
            List<EthNodeModel> nodeList = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_SHARDEUM);
            if(!nodeList.isEmpty()){
                sb.append("  - job_name: \""+EthNodeModel.NODETYPE_SHARDEUM+"\"\n" +
                        "    static_configs:\n");
                for (EthNodeModel node : nodeList) {
                    sb.append( "      - targets: ['"+node.getUrl()+":9100']\n" +
                            "        labels:\n" +
                            "          instance: " + node.getName()+"\n");
                }
            }
        }
        {//gaganode服务器
            List<EthNodeModel> nodeList = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_GAGANODE);
            if(!nodeList.isEmpty()){
                sb.append("  - job_name: \""+EthNodeModel.NODETYPE_GAGANODE+"\"\n" +
                        "    static_configs:\n");
                for (EthNodeModel node : nodeList) {
                    sb.append("      - targets: ['"+node.getUrl()+":9100']\n" +
                            "        labels:\n" +
                            "          instance: " + node.getName()+"\n");
                }
            }
        }
        {//depin服务器
            List<EthNodeModel> nodeList = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_DEPIN);
            if(!nodeList.isEmpty()){
                sb.append("  - job_name: \""+EthNodeModel.NODETYPE_DEPIN+"\"\n" +
                        "    static_configs:\n");
                for (EthNodeModel node : nodeList) {
                    sb.append("      - targets: ['"+node.getUrl()+":9100']\n" +
                            "        labels:\n" +
                            "          instance: " + node.getName()+"\n");
                }
            }
        }
        {//io-net服务器
            List<EthNodeModel> nodeList = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_IONET);
            if(!nodeList.isEmpty()){
                sb.append("  - job_name: \""+EthNodeModel.NODETYPE_IONET+"\"\n" +
                        "    static_configs:\n");
                for (EthNodeModel node : nodeList) {
                    sb.append("      - targets: ['"+node.getUrl()+":9100']\n" +
                            "        labels:\n" +
                            "          instance: " + node.getName()+"\n");
                }
            }
        }
        return sb.toString();
    }
    public String genereatePrometheusYml(String type){
        StringBuilder sb = new StringBuilder();
        {
            List<EthNodeModel> nodeList = ethNodeService.listNodeByNodeType(type);
            if(!nodeList.isEmpty()){
                for (EthNodeModel node : nodeList) {
                    sb.append( "      - targets: \n" +
                            "        - " +node.getUrl()+ ":9100\n" +
                            "        labels:\n" +
                            "          instance: " + node.getName()+"\n");
                }
            }
        }
        return sb.toString();
    }
    @Override
    public void updatePrometheusNode(){
        List<EthNodeModel> nodeList = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_MONITOR);
        if(nodeList.isEmpty()){
           return;
        }
        EthNodeModel node = nodeList.get(0);
        String ipAddr = node.getUrl();
        String userName = node.getAdmin();
        String password = node.getPassword();
        int port = 22;
        log.info("连接节点:{},{},{}",node.getName(), node.getIndexNum(),node.getUrl());
        String privateKey = node.getPrivateKey();
        try {
            JSchUtil jSchUtil = JSchUtil.getInstance(node.getUrl(), userName, password);
            String command = "sudo su\n" +
                    "cd /root\n";
            command += updateYml(EthNodeModel.NODETYPE_SHARDEUM);
            command += updateYml(EthNodeModel.NODETYPE_GAGANODE);
            command += updateYml(EthNodeModel.NODETYPE_DEPIN);
            command += updateYml(EthNodeModel.NODETYPE_IONET);
//            command += "docker restart prometheus\n";
            command += "exit\n";
            command += "exit\n";
            command += "exit\n";
            command += "exit\n";
            String result = jSchUtil.execCommandByShell(command);
            jSchUtil.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error:" + e.getMessage());
        }
    }

    @NotNull
    private String updateYml(String type) {
        String command = "";
        String yml = genereatePrometheusYml(type);
        command += "sudo tee /root/monitoring/prometheus/targets/"+type+".yml > /dev/null << EOF\n"+yml+"EOF\n";
        return command;
    }
}
