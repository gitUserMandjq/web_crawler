package com.crawler.base.common.model;

import net.sf.expectit.Expect;

public interface MyFunction<T, R> {

    R apply(T e) throws Exception;
}
