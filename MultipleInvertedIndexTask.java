package com;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MultipleInvertedIndexTask implements Runnable {
    private CompletionService<List<Document>> completionService;
    private ConcurrentHashMap<String, StringBuffer> invertedIndex;

    public MultipleInvertedIndexTask(CompletionService<List<Document>> completionService, ConcurrentHashMap<String, StringBuffer> invertedIndex) {
        this.completionService = completionService;
        this.invertedIndex = invertedIndex;
    }

    public void run() {
        try {
            label39:
            while(true) {
                if (!Thread.interrupted()) {
                    try {
                        List<Document> documents = (List)this.completionService.take().get();
                        Iterator var9 = documents.iterator();

                        while(true) {
                            if (!var9.hasNext()) {
                                continue label39;
                            }

                            Document document = (Document)var9.next();
                            this.updateInvertedIndex(document.getVoc(), this.invertedIndex, document.getFileName());
                        }
                    } catch (InterruptedException var5) {
                    }
                }

                while(true) {
                    Future<List<Document>> future = this.completionService.poll();
                    if (future == null) {
                        return;
                    }

                    List<Document> documents = (List)future.get();
                    Iterator var4 = documents.iterator();

                    while(var4.hasNext()) {
                        Document document = (Document)var4.next();
                        this.updateInvertedIndex(document.getVoc(), this.invertedIndex, document.getFileName());
                    }
                }
            }
        } catch (ExecutionException | InterruptedException var6) {
            var6.printStackTrace();
        }
    }

    private void updateInvertedIndex(Map<String, Integer> voc, ConcurrentHashMap<String, StringBuffer> invertedIndex, String fileName) {
        Iterator var5 = voc.keySet().iterator();

        while(var5.hasNext()) {
            String word = (String)var5.next();
            if (word.length() >= 3) {
                StringBuffer buffer = (StringBuffer)invertedIndex.computeIfAbsent(word, (k) -> {
                    return new StringBuffer();
                });
                synchronized(buffer) {
                    buffer.append(fileName).append(";");
                }
            }
        }

    }
}
