package com.ols.ruslan.neo;


import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
        instance.deleteRecordType();
        instance.setTitleChapter("в " + instance.getTitleChapter());
        if (!"".equals(instance.getVolume()) && !"".equals(instance.getNumber())) instance.deleteNumber();
        // Запись вида автор1,автор2, ... авторn and авторn+1
        if (!instance.getAuthor().equals("")) {
            String[] authors = instance.getAuthor().split("-");
            switch (authors.length) {
                case 1: {
                    instance.setAuthor(authors[0]);//.substring(0, authors[0].length() - 1));
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
        // Год должен быть указан в ()
        instance.setYear("(" + instance.getYear() + ")");
        instance.setEditor("в " + instance.getEditor() + "(ред.), ");
        //instance.setOldType("(" + instance.getOldType() + ")");
        if (PatternFactory.universityPattern.matcher(instance.getPublisher()).find())
            instance.setUniversity(instance.getPublisher());

        //Удаляем пустые поля
        instance.setFields(
                instance.getFields()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() != null && !entry.getValue().equals("") && PatternFactory.notEmptyFieldPattern.matcher(entry.getValue()).find())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue , (a, b) -> a, LinkedHashMap::new)));
    }

    public String buildHarvard() {
        StringBuilder builder = new StringBuilder();

        if ("INPROCEEDINGS".equals(recordType)
                || "ARTICLE".equals(recordType)
                || "PHDTHESIS".equals(recordType)
                || "MASTERSTHESIS".equals(recordType)
                || "INBOOK".equals(recordType)
        ) instance.setTitle("\"" + instance.getTitle() + "\"");
        instance.getFields().entrySet().forEach(entry -> entry.setValue(entry.getValue() + ", "));
        if (!instance.getAuthor().equals("")) {
            builder.append(instance.getAuthor())
                    .append(instance.getYear())
                    .append(instance.getTitle());
        }
        else if (!instance.getEditor().equals("")){
            builder.append(instance.getEditor())
                    .append(instance.getYear())
                    .append(instance.getTitle());
        }
        else{
            builder.append(instance.getTitle())
                    .append(instance.getYear());
        }

        if ("ARTICLE".equals(recordType)) {
            builder.append(instance.getJournal())
                    .append(instance.getVolume())
                    .append(instance.getPages());
        } else if ("BOOK".equals(recordType)) {
            builder.append(instance.getEdition())
                    .append(instance.getEditor())
                    .append(instance.getPublisher())
                    .append(instance.getAddress())
                    .append(instance.getPages());
        } else if ("INBOOK".equals(recordType)) {
            builder.append(instance.getTitleChapter())
                    .append(instance.getPublisher())
                    .append(instance.getAddress())
                    .append(instance.getPages());
        }
        else if ("PHDTHESIS".equals(recordType)) {
            builder.append("Abstract of bachelor dissertation")
                    .append(instance.getUniversity())
                    .append(instance.getAddress());
        } else if ("MASTERSTHESIS".equals(recordType)) {
            builder.append("Abstract of master dissertation")
                    .append(instance.getUniversity())
                    .append(instance.getAddress());
        } else if ("PROCEEDINGS".equals(recordType)) {
            builder.append(instance.getConference())
                    .append(instance.getAddress())
                    .append(instance.getData())
                    .append(instance.getPages());
        } else if ("INPROCEEDINGS".equals(recordType)) {
            builder.append(instance.getConference())
                    .append(instance.getAddress())
                    .append(instance.getData())
                    .append(instance.getPages());
        } else {
            builder = new StringBuilder();
            instance.getFields().values().forEach(builder::append);
        }

        builder.trimToSize();
        String[] words = builder.toString().split(" ");
        String field = null;
        for (int i = words.length - 1; i >= 0; i--) {
            field = words[i];
            if (PatternFactory.notEmptyFieldPattern.matcher(field).find() && field.length() > 1) {
                break;
            }
        }
        String result = builder.toString();
        if (field != null) {
            result = builder
                    .substring(0, result.lastIndexOf(field) + field.length())
                    .replaceAll("\\.\\s*[a-zA-Zа-яА-Я]?\\s*\\.", ".")
                    .replaceAll(",\\s*[,.]", ",")
                    .replaceAll(":\\s*[,.]", ":");

            if (PatternFactory.notEmptyFieldPattern.matcher(
                    String.valueOf(result.charAt(result.length() - 1))).find()) {
                return result.concat(".")
                        .replaceAll("\\.\\.", ".");
            } else return result.substring(0, result.length() - 1)
                    .concat(".")
                    .replaceAll("\\.\\.", ".");
        }
        return result;
    }
}
