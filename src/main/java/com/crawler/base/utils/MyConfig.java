package com.crawler.base.utils;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.common.Factory;
import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.transport.cipher.Cipher;
import net.schmizz.sshj.transport.kex.KeyExchange;
import net.schmizz.sshj.transport.mac.MAC;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyConfig extends DefaultConfig {
    private Logger logger;

    public MyConfig(){
        super();
        setLoggerFactory(LoggerFactory.DEFAULT);
    }

    @Override
    public void setLoggerFactory(LoggerFactory loggerFactory) {
        super.setLoggerFactory(loggerFactory);
        logger = loggerFactory.getLogger(getClass());
    }

    public void setCiphersFactories(String input) {
        String[] ciphers = input.trim().split(",");
        List<String> factoryNames = Factory.Named.Util.getNames(getCipherFactories());

        List<Factory.Named<Cipher>> transportCiphers = Arrays.stream(ciphers)
                .filter(factoryNames::contains)
                .map(cipher -> Factory.Named.Util.get(getCipherFactories(), cipher))
                .collect(Collectors.toList());

        logger.info("Client-side cipher factories set to: {}", transportCiphers);

        setCipherFactories(transportCiphers);
    }

    public void setMacFactories(String input) {
        String[] macs = input.trim().split(",");
        List<String> factoryNames = Factory.Named.Util.getNames(getMACFactories());

        List<Factory.Named<MAC>> macCiphers = Arrays.stream(macs)
                .filter(factoryNames::contains)
                .map(mac -> Factory.Named.Util.get(getMACFactories(), mac))
                .collect(Collectors.toList());

        logger.info("Client-side MAC factories set to: {}", Factory.Named.Util.getNames(macCiphers));

        setMACFactories(macCiphers);
    }

    public void setKexFactories(String input) {
        String[] kexs = input.trim().split(",");
        List<String> factoryNames = Factory.Named.Util.getNames(getKeyExchangeFactories());

        List<Factory.Named<KeyExchange>> kexFactors = Arrays.stream(kexs)
                .filter(factoryNames::contains)
                .map(kex -> Factory.Named.Util.get(getKeyExchangeFactories(), kex))
                .collect(Collectors.toList());

        logger.info("Client-side MAC factories set to: {}", Factory.Named.Util.getNames(kexFactors));

        setKeyExchangeFactories(kexFactors);
    }
}

