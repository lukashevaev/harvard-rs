package com.ols.ruslan.neo;


import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
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
        return field.replaceAll("[^0-9-]", "");
    }

    private Integer getPosition(String[] array, String destination) {
        for (int i = 0; i <= array.length; i++) {
            if (Objects.equals(array[i], destination)) {
                return i;
            }
        }
        return -1;
    }

    private void refactorFields() throws IOException {
        instance.deleteRecordType();
        instance.setTitleChapter("в " + instance.getTitleChapter());
        if (!"".equals(instance.getVolume()) && !"".equals(instance.getNumber())) instance.deleteNumber();
        // Запись вида автор1,автор2, ... авторn and авторn+1
        instance.getAuthor().ifPresent(author -> {
            String[] allAuthors = author.split("-");
            StringBuilder builder = new StringBuilder();
            Arrays.stream(allAuthors).forEach(fullName -> {
                String[] authors = fullName.trim().split(" ");
                //Arrays.stream(authors).forEach(s -> s = s.replaceAll(",", "").trim());
                String name = authors[0].trim() + ", ";
                builder.append(name);
                Arrays.stream(authors).skip(1).forEach(str -> builder.append(str.trim()).append(" "));
                if (allAuthors.length >= 2) {
                    Integer position = getPosition(allAuthors, fullName);
                    if (position != allAuthors.length - 2) {
                        builder.append(", ");
                    } else {
                        builder.append(" and ");
                    }
                } else {
                    builder.append(" and ");
                }
            });
            if (allAuthors.length == 1) {
                instance.setAuthor(builder.toString().replaceAll(" and ", "").trim());
            } else {
                String result = builder.toString().trim();
                if (result.endsWith(",")) {
                    result = result.substring(0, result.length() - 1);
                }
                instance.setAuthor(result);
            }
        });

        // Год должен быть указан в ()
        instance.setYear("(" + instance.getYear() + ")");
        instance.setEditor("в " + instance.getEditor() + "(ред.), ");
        instance.setVolume("том. " + getDigits(instance.getVolume()));
        instance.setNumber("№ " + getDigits(instance.getNumber()));
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
        if (instance.getAuthor().isPresent()) {
            builder.append(instance.getAuthor().get())
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
                    .append(instance.getPublisher())
                    .append(instance.getVolume())
                    .append(instance.getPages());
        } else if ("BOOK".equals(recordType)) {
            builder.append(instance.getEdition());
            if (instance.getAuthor().isPresent()) {
                builder.append(instance.getEditor());
            }
            builder.append(instance.getPublisher())
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
                    .replaceAll("\\.\\s*\\.", ".")
                    //.replaceAll("\\.[a-zA-Zа-яА-Я]?\\.", ".")
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
