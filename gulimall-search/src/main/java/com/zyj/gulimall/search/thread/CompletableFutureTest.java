package com.zyj.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: gulimall
 * @ClassName CompletableFutureTest
 * @author: YaJun
 * @Date: 2021 - 12 - 16 - 21:44
 * @Package: com.zyj.gulimall.search.thread
 * @Description: CompletableFuture 异步线程编排(类似于Vue中的promise，可以.then{...})
 */
public class CompletableFutureTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("main start...");

        // 1. 启动异步任务
        // 创建CompletableFuture; runAsync 无返回值
        //CompletableFuture.runAsync(() -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());
        //    int i = 10 / 2;
        //    System.out.println("运行结果：" + i);
        //}, executor);

        // 创建CompletableFuture; supplyAsync 有返回值
        //CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());
        //    int i = 10 / 0;
        //    System.out.println("运行结果：" + i);
        //    return i;
        //}, executor);
        //System.out.println("返回值：" + future.get());


        // 2. 完成回调和异常感知；会新开线程
        //CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());     // 当前线程：12
        //    int i = 10 / 0;
        //    System.out.println("运行结果：" + i);
        //    return i;
        //}, executor).whenCompleteAsync((res, exception) -> {
        //    // 虽然能感知到异常，但是没法修改异常返回数据
        //    System.out.println("当前线程：" + Thread.currentThread().getId());     // 当前线程：13
        //    System.out.println("异步任务成功完成了... 结果是：" + res + "异常是: " + exception);
        //}, executor).exceptionally(throwable -> {
        //    // 可以感知异常，同时返回异常默认值
        //    return 10;
        //});


        // 3. 方法执行完成后的处理: handleAsync; 会开新线程
        //CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());   // 当前线程：12
        //    int i = 10 / 4;
        //    System.out.println("运行结果：" + i);
        //    return i;
        //}, executor).handleAsync((res, throwable) -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());   // 当前线程：13
        //    if (res != null) {
        //        return res * 2;
        //    }
        //    if (throwable != null) {
        //        return 0;
        //    }
        //    return 1001;
        //}, executor);

        // 4. 线程串行化方法:
        // thenRun: 不能获取前一个任务的返回结果，且本身无返回结果
        // thenApply: 当一个线程依赖另一个线程时，获取上一个任务的返回结果，并返回当前任务的返回值;
        // thenAccept: 需要前一个任务的返回结果，并消费处理，本身无返回结果
        // 带有Async默认是异步执行的
        // 以上都要前置任务执行成功
        //CompletableFuture.supplyAsync(() -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());   // 当前线程：12
        //    int i = 10 / 4;
        //    System.out.println("运行结果：" + i);
        //    return i;
        //}, executor).thenRunAsync(() -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());   // 当前线程：13
        //    System.out.println("任务2启动了...");
        //}, executor);

        //CompletableFuture.supplyAsync(() -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());   // 当前线程：12
        //    int i = 10 / 4;
        //    System.out.println("运行结果：" + i);
        //    return i;
        //}, executor).thenAcceptAsync((res) -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());   // 当前线程：13
        //    System.out.println("任务2启动了..." + res);
        //}, executor);

        //CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());   // 当前线程：12
        //    int i = 10 / 4;
        //    System.out.println("运行结果：" + i);
        //    return i;
        //}, executor).thenApplyAsync((res) -> {
        //    System.out.println("当前线程：" + Thread.currentThread().getId());   // 当前线程：13
        //    System.out.println("任务2启动了..." + res);
        //    return res * 2;
        //}, executor);
        //System.out.println("返回值: " + future.get());



        // 5. 两任务组合 - 都要完成
        // thenCombine: 组合两个future，获取两个 future 的返回结果，并返回当前任务的返回值
        // thenAcceptBoth: 组合两个future，获取两个 future 任务的返回结果，然后处理任务，没有返回值
        // runAfterBoth: 组合两个future，不需要获取 future 的结果，只需要两个 future 处理完任务后，处理该任务

        //CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("任务1线程：" + Thread.currentThread().getId());   // 当前线程：12
        //    int i = 10 / 4;
        //    System.out.println("任务1结束：");
        //    return i;
        //}, executor);
        //
        //CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("任务2线程：" + Thread.currentThread().getId());   // 当前线程：12
        //    System.out.println("任务2结束：");
        //    return "hello";
        //}, executor);


        //future01.runAfterBothAsync(future02, () -> {
        //    System.out.println("任务3开始...");
        //}, executor);

        // void accept(T t, U u);
        //future01.thenAcceptBothAsync(future02, (f1, f2) -> {
        //    System.out.println("任务3开始...结果1：" + f1 + "\t 结果2：" + f2);
        //}, executor);

        // R apply(T t, U u);
        //CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
        //    return f1 + "->" + f2;
        //}, executor);
        //System.out.println("thenCombineAsync的结果是：" + future.get());



        // 6. 两任务组合 - 一个完成
        // runAfterEither: 两个任务有一个执行完成，不需要获取 future 的结果，处理任务，也没有返回结果
        // acceptEither: 两个任务有一个执行完成，获取它的返回值，处理任务，没有新的返回值
        // applyToEither: 两个任务有一个执行完成，获取它的返回值，处理任务并有新的返回值
        // 带有Async默认是异步执行的

        //CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("任务1线程：" + Thread.currentThread().getId());   // 当前线程：12
        //    int i = 10 / 4;
        //    System.out.println("任务1结束：");
        //    return i;
        //}, executor);
        //
        //CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("任务2线程：" + Thread.currentThread().getId());   // 当前线程：12
        //    try {
        //        Thread.sleep(3000);
        //        System.out.println("任务2结束：");
        //    } catch (InterruptedException e) {
        //        e.printStackTrace();
        //    }
        //    return "hello";
        //}, executor);

        //future01.runAfterEitherAsync(future02, () -> {
        //    System.out.println("任务3开始...");
        //}, executor);

        // void accept(T t);
        //future01.acceptEitherAsync(future02, (res) -> {
        //    System.out.println("任务3开始..." + res);
        //}, executor);

        // R apply(T t);
        //CompletableFuture<String> future = future01.applyToEitherAsync(future02, (res) -> {
        //    System.out.println("任务3开始..." + res);
        //    return res.toString() + "哈哈";
        //}, executor);
        //System.out.println("applyToEitherAsync 结果为：" + future.get());



        // 7. 多任务组合
        // allOf: 等待所有任务完成
        // anyOf: 只要有一个任务完成
        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品图片信息");
            return "hello.jpg";
        }, executor);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("查询商品属性");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "黑色+256G";
        }, executor);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品介绍");
            return "华为";
        }, executor);

        //CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
        //allOf.get();  // 等待所有任务完成
        //System.out.println("allOf结果为：" + futureImg.get() + "=>" + futureAttr.get() + "=>" + futureDesc.get());

        //CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        //anyOf.get();
        //System.out.println("anyOf结果为：" + anyOf.get());


        System.out.println("main end...");

    }

}
