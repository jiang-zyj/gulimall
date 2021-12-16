package com.zyj.gulimall.search.thread;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;

import java.util.concurrent.*;

/**
 * @program: gulimall
 * @ClassName ThreadTest
 * @author: YaJun
 * @Date: 2021 - 12 - 16 - 20:09
 * @Package: com.zyj.gulimall.search.thread
 * @Description: 线程的几种创建方式及细节
 */
public class ThreadTest {


    public static ExecutorService service = Executors.newFixedThreadPool(10);


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /**
         * 1. 继承Thread
         *      Thread01 thread01 = new Thread01();
         *      thread01.start();
         * 2. 实现Runnable接口
         *      Runnable01 runnable01 = new Runnable01();
         *          Thread thread = new Thread(runnable01);
         *          thread.start();
         * 3. 实现Callable接口
         *      Callable01 callable01 = new Callable01();
         *      FutureTask<Integer> futureTask = new FutureTask<>(callable01);
         *      Thread thread = new Thread(futureTask);
         *      thread.start();
         *      // 阻塞等待整个线程执行完成，获取返回结果
         *      Integer result = futureTask.get();
         * 4. 线程池
         *      给线程池直接提交任务。
         *      1. 创建：
         *          1)、Executors
         *          2)、new ThreadPoolExecutor
         *
         *      Future: 可以获取到异步结果
         * 区别：
         *      1、2不能得到返回值，3可以获取返回值
         *      1、2、3都不能控制资源，4可以控制资源，性能稳定。
         *      以后在业务代码里面，前三种启动线程的方式都不用。【将所有的多线程异步任务都交给线程池执行】
         */
        System.out.println("main start...");

        // 当前系统中池只有一两个，每个异步任务，提交给线程池让它自己去执行就行
        service.execute(new Runnable01());

        /**
         * 七大参数
         * int corePoolSize: [5] 核心线程数[一直存在，除非设置allowCoreThreadTimeOut]；线程池创建好以后就准备就绪的线程数量，就等待来接收异步任务去执行。
         *              5个 Thread thread = new Thread(); thread.start();
         * int maximumPoolSize: [200] 最大线程数量；控制资源
         * long keepAliveTime: 存活时间。如果当前的线程数量大于corePoolSize数量。
         *              释放空闲的线程（maximumPoolSize - corePoolSize）。只要线程空闲大于指定的keepAliveTime。
         * TimeUnit unit: 时间单位
         * BlockingQueue<Runnable> workQueue: 阻塞队列。如果任务有很多大于（corePoolSize），就会将目前多的任务
         *              放在队列里面。只要有线程空闲，就会去队列里面取出新的任务继续执行。
         * ThreadFactory threadFactory: 线程的创建工厂
         * RejectedExecutionHandler handler: 拒绝策略。如果队列满了，maximumPoolSize也满了，那么其他的线程就会
         *              按我们指定的拒绝策略拒绝执行任务。
         *
         * 工作顺序：
         *  1. 线程池创建，准备好corePoolSize数量的核心线程，准备接收任务
         *  2. corePoolSize满了，就将再进来的任务放入到阻塞队列中。如果有空闲的corePoolSize线程，就会自己去阻塞队列中获取任务执行
         *  3. 阻塞队列满了，就直接开新线程执行，最大只能开到maximumPoolSize指定的数量
         *  4. 如果maximumPoolSize也满了，就用RejectedExecutionHandler拒绝任务
         *  5. 如果(maximumPoolSize - corePoolSize)里的任务都执行完成，有很多空闲，且没有其他任务进来，
         *      则在指定的keepAliveTime以后，释放 (maximumPoolSize - corePoolSize) 这些线程
         *
         *      new LinkedBlockingQueue<>()：默认是 Integer 的最大值，可能会导致内存不够
         *
         *  实例：
         *      一个线程 core: 7, max: 20, queue: 50。如果100个并发进来是怎么分配的？
         *      7个会立即得到执行，然后将50个放入阻塞队列，又创建13个新线程，剩余的30个使用拒绝策略
         *      如果不想抛弃，还要执行，使用：CallerRunsPolicy
         */
        new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        // Executors 创建线程池
        Executors.newCachedThreadPool();    // core 是 0，所有都可以回收
        Executors.newFixedThreadPool(10);   // 固定大小，core = max；都不可以回收
        Executors.newScheduledThreadPool(10);   // 定时任务
        Executors.newSingleThreadExecutor();        // 单线程的线程池；一次创建一个；后台从队列里面获取任务，挨个执行。

        System.out.println("main end...");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }
}
