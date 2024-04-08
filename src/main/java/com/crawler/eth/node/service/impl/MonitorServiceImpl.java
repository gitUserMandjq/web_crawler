package com.crawler.eth.node.service.impl;

import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthNodeService;
import com.crawler.eth.node.service.IMonitorService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
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
}
