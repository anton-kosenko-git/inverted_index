package com;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class InvertedIndexTask implements Runnable {
    private CompletionService<Document> completionService;
    private ConcurrentHashMap<String, StringBuffer> invertedIndex;

    public InvertedIndexTask(CompletionService<Document> completionService, ConcurrentHashMap<String, StringBuffer> invertedIndex) {
        this.completionService = completionService;
        this.invertedIndex = invertedIndex;
    }

    public void run() {
        try {
            while(true) {
                if (!Thread.interrupted()) {
                    try {
                        Document document = (Document)this.completionService.take().get();
                        this.updateInvertedIndex(document.getVoc(), this.invertedIndex, document.getFileName());
                        continue;
                    } catch (InterruptedException var3) {
                    }
                }

                while(true) {
                    Future<Document> future = this.completionService.poll();
                    if (future == null) {
                        return;
                    }

                    Document document = (Document)future.get();
                    this.updateInvertedIndex(document.getVoc(), this.invertedIndex, document.getFileName());
                }
            }
        } catch (ExecutionException | InterruptedException var4) {
            var4.printStackTrace();
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
                buffer.append(fileName).append(";");
            }
        }

    }
}
