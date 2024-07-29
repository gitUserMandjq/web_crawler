package com.crawler.eth.node.service;

import com.crawler.eth.node.model.EthNodeDetailModel;
import com.crawler.eth.node.model.EthNodeModel;

import java.io.IOException;

public interface IEthBrowserService {
    void ionetRefreshToken(Long id) throws IOException;

    void getionetDeviceStatus(EthNodeDetailModel node) throws IOException;
}
