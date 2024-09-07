package com.test;

import com.crawler.SpringbootApplication;
import com.crawler.base.utils.OkHttpClientUtil;
import com.crawler.base.utils.ThreadUtils;
import com.crawler.eth.node.dao.EthNodeDetailDao;
import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthNodeService;
import com.crawler.eth.node.service.IMonitorService;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

//@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootApplication.class)
@Slf4j
class MyTests {
  @Resource
  IEthNodeService ethNodeService;
  @Resource
  IMonitorService monitorService;
  @Resource
  EthNodeDetailDao ethNodeDetailDao;
//  @Autowired
//  private Web3j web3j;
  @Test
  void contextLoads() throws Exception {
    List<EthNodeModel> ethNodeModels = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_SHARDEUM);

    ThreadUtils.ChokeLimitThreadPool chokeLimitThreadPool = ThreadUtils.getInstance().chokeLimitThreadPool(ethNodeModels.size(), 10);
    for(EthNodeModel node:ethNodeModels){
      chokeLimitThreadPool.run(new ThreadUtils.ChokeLimitThreadPool.RunThread() {
        @Override
        public void run() throws InterruptedException {
          if(!"1.10.0".equals(node.getVersion()) ){
            try {
//              ethNodeService.upgradeShardeumNode(node);
              ethNodeService.restartShardeumNode(node);
            } catch (IOException e) {
              throw new RuntimeException(e);
            } catch (JSchException e) {
              throw new RuntimeException(e);
            }
//        break;
          }
        }
      });

    }
    chokeLimitThreadPool.choke();
  }
  @Test
  void contextLoads2() throws Exception {
    //加载合约地址
//    String yml = monitorService.genereatePrometheusYml();
//    System.out.println("sudo tee /root/monitoring/prometheus.yml > /dev/null << EOF\n"+yml+"EOF");
//    monitorService.restartPrometheus();
    ethNodeService.obtainQuiliBalance();
  }

  public static void main(String[] args) {
    String str = "AAAA BBBB CCCC";
    Pattern pattern = Pattern.compile("BBBB|DDDD");
    System.out.println(pattern.matcher(str).find());
  }


}
