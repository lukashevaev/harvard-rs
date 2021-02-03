package com.ols.record;

import com.ols.z3950.record.Record;

import javax.ejb.Remote;

@Remote
public interface RecordSchema {
    String getURI();
    String toString(Record record, String encoding) throws Exception;
    //Document transformSchema(Document src) throws TransformerException;
    //Document toDocument(Record record, String encoding) throws Exception;
    Record normalize(Record record, String encoding);
    Record denormalize(Record record, String encoding);
}
