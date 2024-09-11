package com.crawler.base.utils;

import com.crawler.base.common.model.MyFunction;
import net.sf.expectit.Expect;
import org.apache.poi.ss.formula.functions.T;

import java.io.PrintWriter;

public interface SSHUtil {
    public static class PrintProperty<T>{
        public Expect expect;
        public Boolean endFlag = false;
        public String stage = "0";
        public PrintWriter printWriter;
        public String console = "";
        public T append;
    }
    public <T> String execCommandByShellExpect(MyFunction<PrintProperty<T>, String> property) throws Exception;
    public <T> String execCommandByShellExpect(MyFunction<PrintProperty<T>, String> property, T append) throws Exception;
}
