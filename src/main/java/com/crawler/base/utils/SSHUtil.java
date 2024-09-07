package com.crawler.base.utils;

import com.crawler.base.common.model.MyFunction;
import net.sf.expectit.Expect;

import java.io.PrintWriter;

public interface SSHUtil {
    public static class PrintProperty{
        public Expect expect;
        public Boolean endFlag = false;
        public String stage = "0";
        public PrintWriter printWriter;
        public String console = "";
    }
    public String execCommandByShellExpect(MyFunction<PrintProperty, String> property) throws Exception;
}
