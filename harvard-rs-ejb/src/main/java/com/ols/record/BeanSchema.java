package com.ols.record;

import com.ols.z3950.record.Record;

import javax.ejb.Remote;

@Remote
public interface BeanSchema {
    String toString(Record record, String encoding) throws Exception;
    Record normalize(Record record, String encoding);
    Record denormalize(Record record, String encoding);
    Object getTransformedRecord(byte[] record, String encoding) throws Exception;
    String getMimeType();
}
