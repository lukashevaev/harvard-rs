package com.ols.ruslan.neo;

import java.util.Map;
import java.util.regex.Pattern;

public class TypeDefiner {
    private String recordType;
    private final Map<RecordType, Pattern> patternsForType;
    private final HarvardInstance instance;

    public TypeDefiner(HarvardInstance instance){
        patternsForType = PatternFactory.getPatternsForType();
        this.instance = instance;
        recordType = instance.getRecordType().toLowerCase();
        defineType();
    }

    private void defineType() {
        String oldType = recordType;
        //patternsLookup
        for (Map.Entry<RecordType, Pattern> entry : patternsForType.entrySet()) {
            if (entry.getValue().matcher(oldType).find() || entry.getValue().matcher(instance.getTitle().toLowerCase()).find()) {
                recordType = "BOOK";
                break;
            }
        }
    }
    public String getRecordType(){
        return recordType;
    }
}
