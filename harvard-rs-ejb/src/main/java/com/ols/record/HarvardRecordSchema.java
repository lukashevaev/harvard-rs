package com.ols.record;


import com.ols.z3950.record.Record;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;


@Singleton(name = "harvard")
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
@Remote(BeanSchema.class)
@EJB(name = "java:global/ruslan/recordSchema/harvard", beanInterface = BeanSchema.class , beanName = "harvard")
public class HarvardRecordSchema implements BeanSchema {
    private static final Logger log = Logger.getLogger(HarvardRecordSchema.class
            .getName());
    private static final TransformerFactory transformerFactory = TransformerFactory
            .newInstance();
    private static Templates templates;

    @EJB(lookup = "java:global/ruslan/recordSchema/ruslan", beanInterface = RecordSchema.class)
    private RecordSchema ruslanRecordSchema;

    @PostConstruct
    public void init() {
        log.fine("Preparing XSL templates");
        log.fine(Objects.requireNonNull(getClass().getClassLoader().getResource("RUSMARC2Harvard.xsl")).toString());
        try {
            templates = transformerFactory.newTemplates(new StreamSource(
                    getClass().getClassLoader().getResourceAsStream(
                            "RUSMARC2Harvard.xsl")));

        } catch (TransformerConfigurationException e) {
            log.severe("Unable to initialise templates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Object getTransformedRecord(byte[] record, String encoding) throws Exception {
        Document src = ruslanRecordSchema.toDocument(record, encoding);
        HarvardBuilder builder = getBuilder(src);
        return builder.buildHarvard();

    }

    @Override
    public String getMimeType() {
        return "application/html";
    }

    private HarvardBuilder getBuilder(Document src) throws Exception {
        Transformer transformer = templates.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(src), result);
        //получаем поля из схемы для составления формата
        Map<String, String> fields = XmlParser.parse((Document) result.getNode());
        return new HarvardBuilder(fields);
    }



    @Override
    public String toString(Record record, String encoding) throws Exception {
        return ruslanRecordSchema.toString(record, encoding);
    }

    @Override
    public Record normalize(Record record, String encoding) {
        return ruslanRecordSchema.normalize(record, encoding);
    }

    @Override
    public Record denormalize(Record record, String encoding) {
        return ruslanRecordSchema.denormalize(record, encoding);
    }



}
