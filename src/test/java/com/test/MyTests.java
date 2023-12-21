package com.test;

import com.crawler.SpringbootApplication;
import com.crawler.base.utils.OkHttpClientUtil;
import com.crawler.base.utils.ThreadUtils;
import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthNodeService;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;

//@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootApplication.class)
@Slf4j
class MyTests {
  @Resource
  IEthNodeService ethNodeService;
//  @Autowired
//  private Web3j web3j;
  @Test
  void contextLoads() throws Exception {
    List<EthNodeModel> ethNodeModels = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_SHARDEUM);
    for(EthNodeModel node:ethNodeModels){
      if("shardeum-1".equals(node.getName())){
        ethNodeService.upgradeShardeumNode(node);
        break;
      }

    }
  }


}
