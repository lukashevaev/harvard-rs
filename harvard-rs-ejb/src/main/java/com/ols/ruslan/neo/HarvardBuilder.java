package com.ols.ruslan.neo;


import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class HarvardBuilder {
    private final String recordType;
    private final HarvardInstance instance;


    public HarvardBuilder(Map<String, String> fields) {
        instance = new HarvardInstance(fields);
        TypeDefiner typeDefiner = new TypeDefiner(instance);
        this.recordType = typeDefiner.getRecordType();
        try {
            refactorFields();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для выделения цифр из поля
    public String getDigits(String field) {
        return field.replaceAll("[^0-9]", "");
    }

    private void refactorFields() throws IOException {
        if (!instance.getAuthor().equals("")) {
            String[] authors = instance.getAuthor().split("-");
            switch (authors.length) {
                case 1: {
                    instance.setAuthor(authors[0].substring(0, authors[0].length() - 1));
                    break;
                }
                case 2: {
                    instance.setAuthor(authors[0].substring(0, authors[0].length() - 1) + " and " + authors[1].substring(0, authors[1].length() - 1));
                    break;
                }
                default: {
                    StringBuilder author = new StringBuilder();
                    Arrays.stream(authors).forEach(author::append);
                    author.replace(author.lastIndexOf(","), author.lastIndexOf(",") + 1, "");
                    author.replace(author.lastIndexOf(","), author.lastIndexOf(",") + 1, " and ");
                    instance.setAuthor(author.toString());
                    break;
                }
            }
        }
        instance.setYear("(" + instance.getYear() + ")");
        if (PatternFactory.universityPattern.matcher(instance.getPublisher()).find())
            instance.setUniversity(instance.getPublisher());
    }

    public String buildHarvard() {
        StringBuilder builder = new StringBuilder();
        Map<String, String> fields = instance.getFields();
        fields.entrySet().forEach(entry -> entry.setValue(entry.getValue() + ", "));
        if ("INPROCEEDINGS".equals(recordType)
                || "ARTICLE".equals(recordType)
                || "PHDTHESIS".equals(recordType)
                || "MASTERSTHESIS".equals(recordType)
                || "INBOOK".equals(recordType)
        ) instance.setTitle("\"" + instance.getTitle() + "\"");
        builder.append(instance.getAuthor())
                .append(instance.getYear())
                .append(instance.getTitle());
        if ("ARTICLE".equals(recordType)) {
            builder.append(instance.getJournal());
            builder.append(instance.getVolume());
            builder.append(instance.getPages());
        } else if ("BOOK".equals(recordType)) {
            builder.append(instance.getPublisher());
            builder.append(instance.getAddress());
        } else if ("INBOOK".equals(recordType)) {
            instance.setTitleChapter("в " + instance.getTitleChapter());
            builder.append(instance.getTitleChapter());
            builder.append(instance.getPublisher());
            builder.append(instance.getAddress());
            builder.append(instance.getPages());
        }
        else if ("PHDTHESIS".equals(recordType)) {
            builder.append("Abstract of bachelor dissertation");
            builder.append(instance.getUniversity());
            builder.append(instance.getAddress());
        } else if ("MASTERSTHESIS".equals(recordType)) {
            builder.append("Abstract of master dissertation");
            builder.append(instance.getUniversity());
            builder.append(instance.getAddress());
        } else if ("PROCEEDINGS".equals(recordType)) {
            builder.append(instance.getConference());
            builder.append(instance.getAddress());
            builder.append(instance.getData());
            builder.append(instance.getPages());
        }
        builder.trimToSize();
        builder.deleteCharAt(builder.length() - 2);
        return builder.toString().replace(",,", ",");
    }
}
