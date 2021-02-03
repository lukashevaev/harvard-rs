package com.ols.record;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TypeDefiner {
    private final Map<String, String> fields;
    private String recordType;
    private final Map<RecordType, Pattern> patternsForType;

    public TypeDefiner(Map<String, String> fields){
        patternsForType = PatternFactory.getPatternsForType();
        this.fields = fields;
        recordType = fields.get("recordType").toLowerCase();
        defineType();
    }

    private void defineType() {
        String oldType = recordType;
        //patternsLookup
        for (Map.Entry<RecordType, Pattern> entry : patternsForType.entrySet()) {
            if (entry.getValue().matcher(oldType).find() || entry.getValue().matcher(fields.get("title").toLowerCase()).find()) {
                recordType = entry.getKey().toString();
                break;
            }
        }
    }
    public String getRecordType(){
        return recordType;
    }
}
