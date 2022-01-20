package com;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class MultipleIndexingTask implements Callable<List<Document>> {
    private List<File> files;

    public MultipleIndexingTask(List<File> files) {
        this.files = files;
    }

    public List<Document> call() throws Exception {
        List<Document> documents = new ArrayList();
        DocumentParser parser = new DocumentParser();
        Iterator var4 = this.files.iterator();

        while(var4.hasNext()) {
            File file = (File)var4.next();
            Map<String, Integer> voc = parser.parse(file.getAbsolutePath());
            Document document = new Document();
            document.setFileName(file.getName());
            document.setVoc(voc);
            documents.add(document);
        }

        return documents;
    }
}