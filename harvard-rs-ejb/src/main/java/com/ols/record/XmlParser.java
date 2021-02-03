package com.ols.record;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedHashMap;
import java.util.Map;

public class XmlParser {
    private final Map<String, String> fields;

    public XmlParser(Document document){
        fields = new LinkedHashMap<>();
        Node root = document.getDocumentElement();
        Node book = root.getChildNodes().item(0);
        NodeList bookProps = book.getChildNodes();
        for (int j = 0; j < bookProps.getLength(); j++) {
            Node bookProp = bookProps.item(j);
            String nodeName = bookProp.getNodeName();
            String nodeValue = "";
            if (bookProp.hasChildNodes())
            nodeValue = bookProp.getChildNodes().item(0).getTextContent();
            if (!nodeValue.equals(""))
                fields.put(nodeName, nodeValue.trim());
        }
    }

    public Map<String, String> getFields(){
        return fields;
    }
}
