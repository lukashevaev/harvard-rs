package com.ols.record;


import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class HarvardBuilder {
    private final String recordType;
    private final Map<String, String> fields;

    public HarvardBuilder(Map<String, String> fields) {
        this.fields = fields;
        TypeDefiner typeDefiner = new TypeDefiner(fields);
        this.recordType = typeDefiner.getRecordType();
        refactorFields();
    }

    private void refactorFields() {
        StringBuilder russianTitle = new StringBuilder();
        boolean isEng = fields.values()
                .stream()
                .noneMatch(field -> PatternFactory.russianPattern.matcher(field.toLowerCase()).find());
        if (!isEng) {
            fields.put("author", Transliterator.cyr2lat(fields.get("author")));
            fields.put("address", Transliterator.cyr2lat(fields.get("address")));
            switch (recordType) {
                case "BOOK":
                    russianTitle.append(Transliterator.cyr2lat(fields.get("title")))
                            .append("[")
                          //  .append(GoogleTranslate.translate("en", fields.get("title")))
                            .append("]");
                    fields.put("title", russianTitle.toString());
                    break;
                case "ARTICLE":
                    russianTitle.append("\"")
                            //.append(GoogleTranslate.translate("en", fields.get("title")))
                            .append("\"");
                    fields.put("title", russianTitle.toString());
                    fields.put("publisher", Transliterator.cyr2lat(fields.get("publisher")));
                    break;
                case "MASTERSTHESIS":
                case "PHDSTHESIS":
                case "ABSTRACT":
                    russianTitle.append("\"")
                            //.append(GoogleTranslate.translate("en", fields.get("title")))
                            .append("\"");
                    break;
            }
            if (PatternFactory.spbPattern.matcher(fields.get("address").toLowerCase()).find())
                fields.put("address", "Saint-Petersburg");
            fields.put("address", fields.get("address") + ", Russia");
        }


        /*if (PatternFactory.russianPattern.matcher(fields.get("author").toLowerCase()).find()) {
            fields.put("author", Transliterator.cyr2lat(fields.get("author")));
        }
        if (PatternFactory.russianPattern.matcher(fields.get("title").toLowerCase()).find()){
            StringBuilder russianTitle = new StringBuilder();
            russianTitle.append(Transliterator.cyr2lat(fields.get("title")))
                        .append("[")
                        .append(GoogleTranslate.translate("en", fields.get("title")))
                        .append("]");
            fields.put("title", russianTitle.toString());
        }

        //for (Map.Entry<String, String> entry : fields.entrySet()) {
        //    fields.put(entry.getKey(), GoogleTranslate.translate("en", entry.getValue()));
        //}


        fields.put("author", fields.get("author").substring(0, fields.get("author").length() - 1));*/
        String[] authors = fields.get("author").split("-");
        switch (authors.length){
            case 1: {
                fields.put("author", authors[0].substring(0, authors[0].length() - 1));
                break;
            }
            case 2: {
                fields.put("author", authors[0].substring(0, authors[0].length() - 1) + " and " + authors[1].substring(0, authors[1].length() - 1));
                break;
            }
            default: {
                StringBuilder author = new StringBuilder();
                Arrays.stream(authors).forEach(author::append);
                author.replace(author.lastIndexOf(","), author.lastIndexOf(",") + 1, "" );
                author.replace(author.lastIndexOf(","), author.lastIndexOf(",") + 1, " and " );
                fields.put("author", author.toString());
                break;
            }

        }
        //fields.put("year", "(" + fields.get("year").compareTo(")"));
        if (PatternFactory.universityPattern.matcher(fields.get("publisher")).find())
            fields.put("university", fields.get("publisher"));
    }

    public org.w3c.dom.Document buildHarvard(){
        String delimiter = ", ";
        Document document = Jsoup.parse("<html></html>");
        document.body().appendText(fields.get("author")).appendText("(")
                        .appendText(fields.get("year")).appendText(")")
                        .appendText(delimiter);
        if (recordType.equals("BOOK"))
                        document.body().appendElement("i")
                                .appendText("'")
                                .appendText(fields.get("title"))
                                .appendText("'");
        else if (recordType.equals("ARTICLE") || recordType.equals("PROCEEDINGS") || recordType.equals("ABSTRACT")){
            document.body()
                    .appendText("'")
                    .appendText(fields.get("title"))
                    .appendText("'");
        } else document.body().appendText(fields.get("title"));
        document.body().appendText(delimiter);
        switch (recordType) {
            case "ARTICLE":
                document.body().appendElement("i")
                        .appendText(fields.get("journal")).appendText(delimiter);
                document.body().appendText(fields.get("volume")).appendText(delimiter);
                document.body().appendText(fields.get("pages")).appendText(delimiter);
                break;
            case "BOOK":
                document.body().appendText(fields.get("publisher")).appendText(delimiter);
                document.body().appendText(fields.get("address")).appendText(delimiter);
                break;
            case "PROCEEDINGS":
                document.body().appendText(fields.get("conference")).appendText(delimiter);
                document.body().appendText(fields.get("address")).appendText(delimiter);
                document.body().appendText(fields.get("date")).appendText(delimiter);
                document.body().appendText(fields.get("pages")).appendText(delimiter);
                break;
            case "ABSTRACT":
                document.body().appendText("Abstract of " + (PatternFactory.getPatternsForType()
                        .get(RecordType.MASTERSTHESIS)
                        .matcher(fields.get("recordType").toLowerCase()).find() ? "Ph. Sc" : "Ph. D")
                ).appendText(delimiter);
                ///////////// Specialization
//            document.body().appendText(fields.get("university")).appendText(delimiter);
                document.body().appendText(fields.get("address"));
                break;
        }

        W3CDom w3cDom = new W3CDom();
        return w3cDom.fromJsoup(document);
    }
}
