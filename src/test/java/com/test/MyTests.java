package com.test;

import com.crawler.SpringbootApplication;
import com.crawler.base.common.model.MyFunction;
import com.crawler.base.utils.*;
import com.crawler.eth.node.dao.EthNodeDao;
import com.crawler.eth.node.dao.EthNodeDetailDao;
import com.crawler.eth.node.model.EthNodeDetailModel;
import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthNodeService;
import com.crawler.eth.node.service.IMonitorService;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.Matchers;
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
  @Resource
  EthNodeDao ethNodeDao;
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
//    ethNodeService.obtainQuiliBalance();
    ethNodeService.dealSSHOrder(EthNodeModel.NODETYPE_QUILIBRIUM, new MyFunction<EthNodeDetailModel, Boolean>(){

      @Override
      public Boolean apply(EthNodeDetailModel e) throws Exception {
//        return "screen3".equals(e.getData());
        return false;
      }
    }, new MyFunction<SSHClientUtil.PrintProperty<EthNodeDetailModel>, String>() {
      @Override
      public String apply(SSHClientUtil.PrintProperty<EthNodeDetailModel> e) throws Exception {
        e.expect.sendLine("sudo su");
        e.expect.sendLine("cd /root && wget -O Quili.sh https://git.dadunode.com/smeb_y/Quilibrium/raw/branch/main/Quili.sh && sed -i \"s/screen -dmS Quili bash -c '.\\/release_autorun.sh'/screen -dmS Quili bash -c '.\\/release_autorun.sh 2>\\&1 | tee \\/var\\/log\\/quili.log'/g\" ./Quili.sh && chmod +x Quili.sh");
//        e.expect.sendLine(LinuxUtils.replace("/root/Quili.sh", "screen -dmS Quili bash -c './release_autorun.sh'", "screen -dmS Quili bash -c './release_autorun.sh 2>&1 | tee /var/log/quili.log'"));
        e.expect.sendLine("ls /root");
        e.expect.expect(Matchers.contains("Quili.sh"));
        e.append.setData("screen3");
        return null;
      }
    }, new MyFunction<EthNodeDetailModel, String>() {
      @Override
      public String apply(EthNodeDetailModel e) throws Exception {
        ethNodeDetailDao.save(e);
        return null;
      }
    });
  }

  public static void main(String[] args) {
    String str = "AAAA BBBB CCCC";
    Pattern pattern = Pattern.compile("BBBB|DDDD");
    System.out.println(pattern.matcher(str).find());
  }


}
