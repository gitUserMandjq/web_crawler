package com.crawler.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class ThreadUtils {
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30, 40, 30L
            , TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000));
    private static ThreadUtils instance;
    private static Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

    /**
     * 懒加载，把在堆创建实例这个行为延迟到类的使用时
     * @return
     */
    public static ThreadUtils getInstance(){
        if(instance == null){
            synchronized (ThreadUtils.class){
                if(instance == null){
                    instance = new ThreadUtils();
                }
            }
        }
        return instance;
    }
    /**
     * 阻塞限制线程方法
     */
    public static class ChokeLimitThreadPool{
        private Semaphore semaphore;//最多同时运行的线程数量
        private CountDownLatch latch;//总执行线程数，用来实现阻塞
        private RequestAttributes context;
        private LocaleContext localeContext;
        public ChokeLimitThreadPool(Integer latchCount, Integer semaphoreCount) {
            latch = new CountDownLatch(latchCount);
            semaphore = new Semaphore(semaphoreCount);
            //上下文信息，进入多线程request会失效，需要设置子线程继承
            context = RequestContextHolder.getRequestAttributes();
            localeContext = LocaleContextHolder.getLocaleContext();
        }
        public void run(RunThread runThread) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        semaphore.acquire(); // 获取permit
                        //必须在子线程内设置，因为共享线程池高并发情况下，ThreadLocal会串用
                        RequestContextHolder.setRequestAttributes(context,true);
                        LocaleContextHolder.setLocaleContext(localeContext, true);
                        //执行方法
                        runThread.run();
                        latch.countDown();
                        semaphore.release(); // 释放permit
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            threadPoolExecutor.execute(runnable);
//            new Thread(runnable).start();
        }
        public void choke() throws InterruptedException {
            latch.await();
        }
        public interface RunThread{
            void run() throws InterruptedException;
        }
    }
    public static class FutureLimitThreadPool<T>{
        private Semaphore semaphore;//最多同时运行的线程数量
        private List<Future<T>> futureList = new ArrayList<>();
        private RequestAttributes context;
        private LocaleContext localeContext;
        public FutureLimitThreadPool(Integer semaphoreCount) {
            semaphore = new Semaphore(semaphoreCount);
            //上下文信息，进入多线程request会失效，需要设置子线程继承
            context = RequestContextHolder.getRequestAttributes();
            localeContext = LocaleContextHolder.getLocaleContext();
        }
        public void run(RunThread<T> runThread) throws Exception{
            Callable<T> runnable = new Callable<T>() {
                @Override
                public T call() throws Exception {
                    semaphore.acquire(); // 获取permit
                    //必须在子线程内设置，因为共享线程池高并发情况下，ThreadLocal会串用
                    RequestContextHolder.setRequestAttributes(context,true);
                    LocaleContextHolder.setLocaleContext(localeContext, true);
                    //执行方法
                    T run = runThread.run();
                    semaphore.release(); // 释放permit
                    return run;
                }
            };
            Future<T> submit = threadPoolExecutor.submit(runnable);
            futureList.add(submit);
//            new Thread(runnable).start();
        }

        /**
         * 可以按顺序返回List列表
         * @return
         * @throws InterruptedException
         * @throws ExecutionException
         */
        public List<T> choke() throws InterruptedException, ExecutionException {
            List<T> list = new ArrayList<>();
            for(Future<T> f:futureList){
                T t = f.get();
                list.add(t);
            }
            return list;
        }
        public interface RunThread<T>{
            T run() throws Exception;
        }
    }

    /**
     * 获得一个阻塞线程类
     * @param latchCount
     * @param semaphoreCount
     * @return
     */
    public ChokeLimitThreadPool chokeLimitThreadPool(Integer latchCount, Integer semaphoreCount){
        return new ChokeLimitThreadPool(latchCount, semaphoreCount);
    }
    /**
     * 获得一个阻塞线程类
     * @param semaphoreCount
     * @param classes
     * @return
     */
    public <T> FutureLimitThreadPool<T> futureLimitThreadPool(Integer semaphoreCount, Class<T> classes){
        return new FutureLimitThreadPool<T>(semaphoreCount);
    }

    public static void main(String[] args) throws Exception {
        {
            Date beginTime = new Date();
            Integer count = 100;
            ChokeLimitThreadPool chokeLimitThreadPool = ThreadUtils.getInstance().chokeLimitThreadPool(count, 5);
            for(int i=0;i<100;i++){
                int finalI = i;
                chokeLimitThreadPool.run(new ChokeLimitThreadPool.RunThread() {
                    @Override
                    public void run() throws InterruptedException {
                        Thread.sleep(1000L);
                        logger.info(Thread.currentThread()+":"+ finalI);
                    }
                });
            }
            chokeLimitThreadPool.choke();
            logger.info("ChokeLimitThreadPool-costTime:{}ms", new Date().getTime() - beginTime.getTime());
        }
        {
            Date beginTime = new Date();
            Integer count = 100;
            FutureLimitThreadPool<Integer> chokeLimitThreadPool = ThreadUtils.getInstance().futureLimitThreadPool(5, Integer.class);
            for(int i=0;i<100;i++){
                int finalI = i;
                chokeLimitThreadPool.run(new FutureLimitThreadPool.RunThread() {
                    @Override
                    public Integer run() throws InterruptedException {
                        Thread.sleep(1000L);
                        logger.info(Thread.currentThread()+":"+ finalI);
                        return finalI;
                    }
                });
            }
            List<Integer> choke = chokeLimitThreadPool.choke();
            logger.info(choke.toString());
            logger.info("FutureLimitThreadPool-costTime:{}ms", new Date().getTime() - beginTime.getTime());
        }
        threadPoolExecutor.shutdown();

    }
}
