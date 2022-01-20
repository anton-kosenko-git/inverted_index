package com;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConcurrentIndexing {
    public ConcurrentIndexing() {
    }

    public static void main(String[] args) {
        int V = 5;
        int numCores = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Math.max(numCores - 1, 1));
        ExecutorCompletionService<Document> completionService = new ExecutorCompletionService(executor);
        ConcurrentHashMap<String, StringBuffer> invertedIndex = new ConcurrentHashMap();


        File source = new File("src/main/data/aclImdb/test/neg");
        File[] files = source.listFiles();

        /*
        File source = new File("src/main/data");

        File[] files = File.listRoots();
        for(File temp:files){
            files = source.listRoots();
        }


        File source = new File("src/main/data/aclImdb/test");
        File[] files = source.listFiles();
            if(source.isDirectory()){
                for(File temp:source.listFiles()){
                files = source.listFiles();
                }
            }
            else{
                files = source.listFiles();
            }
         */

        InvertedIndexTask invertedIndexTask = new InvertedIndexTask(completionService, invertedIndex);
        Thread thread1 = new Thread(invertedIndexTask);
        thread1.start();
        InvertedIndexTask invertedIndexTask2 = new InvertedIndexTask(completionService, invertedIndex);
        Thread thread2 = new Thread(invertedIndexTask2);
        thread2.start();
        Date start = new Date();
        File[] var16 = files;
        int var15 = files.length/50*V;

        for(int var14 = files.length/50*(V-1); var14 < var15; ++var14) {
            File file = var16[var14];
            IndexingTask task = new IndexingTask(file);
            completionService.submit(task);
            if (executor.getQueue().size() > 1000) {
                do {
                    try {
                        TimeUnit.MILLISECONDS.sleep(50L);
                    } catch (InterruptedException var20) {
                        var20.printStackTrace();
                    }
                } while(executor.getQueue().size() > 1000);
            }
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1L, TimeUnit.DAYS);
            thread1.interrupt();
            thread2.interrupt();
            thread1.join();
            thread2.join();
        } catch (InterruptedException var19) {
            var19.printStackTrace();
        }

        Date end = new Date();
        System.out.println("Execution Time: " + (end.getTime() - start.getTime()));
        System.out.println("invertedIndex: " + invertedIndex.size());
        System.out.println(((StringBuffer)invertedIndex.get("book")).length());
    }
}
