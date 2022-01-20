package com;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MultipleConcurrentIndexing {
    public MultipleConcurrentIndexing() {
    }

    public static void main(String[] args) {
        int V = 5;
        int numCores = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Math.max(numCores - 1, 1));
        ExecutorCompletionService<List<Document>> completionService = new ExecutorCompletionService(executor);
        ConcurrentHashMap<String, StringBuffer> invertedIndex = new ConcurrentHashMap();
        boolean NUMBER_OF_DOCUMENTS = true;
        File source = new File("src/main/data/aclImdb/test/neg");
        File[] files = source.listFiles();
        Date start = new Date();
        MultipleInvertedIndexTask invertedIndexTask = new MultipleInvertedIndexTask(completionService, invertedIndex);
        Thread thread1 = new Thread(invertedIndexTask);
        thread1.start();
        MultipleInvertedIndexTask invertedIndexTask2 = new MultipleInvertedIndexTask(completionService, invertedIndex);
        Thread thread2 = new Thread(invertedIndexTask2);
        thread2.start();
        List<File> taskFiles = new ArrayList();
        File[] var18 = files;
        int var17 = files.length/50*V;

        for(int var16 = files.length/50*(V-1); var16 < var17; ++var16) {
            File file = var18[var16];
            taskFiles.add(file);
            if (taskFiles.size() == 50) {
                MultipleIndexingTask task = new MultipleIndexingTask(taskFiles);
                completionService.submit(task);
                taskFiles = new ArrayList();
            }

            if (executor.getQueue().size() > 10) {
                do {
                    try {
                        TimeUnit.MILLISECONDS.sleep(50L);
                    } catch (InterruptedException var21) {
                        var21.printStackTrace();
                    }
                } while(executor.getQueue().size() > 10);
            }
        }

        if (taskFiles.size() > 0) {
            MultipleIndexingTask task = new MultipleIndexingTask(taskFiles);
            completionService.submit(task);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1L, TimeUnit.DAYS);
            thread1.interrupt();
            thread2.interrupt();
            thread1.join();
            thread2.join();
        } catch (InterruptedException var20) {
            var20.printStackTrace();
        }

        Date end = new Date();
        System.out.println("Execution Time: " + (end.getTime() - start.getTime()));
        System.out.println("invertedIndex: " + invertedIndex.size());
        System.out.println(((StringBuffer)invertedIndex.get("book")).length());
        thread1.interrupt();
        thread2.interrupt();
    }
}
