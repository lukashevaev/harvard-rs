package com.ols.record;


import com.ols.z3950.record.Record;
import org.w3c.dom.Document;

//import javax.annotation.PostConstruct;
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
@Remote(RecordSchema.class)
@EJB(name = "java:global/ruslan/recordSchema/harvard", beanInterface = RecordSchema.class , beanName = "harvard")
public class HarvardRecordSchema implements RecordSchema {
    private static final String URI = "bibtex";
    private static final Logger log = Logger.getLogger(HarvardRecordSchema.class
            .getName());
    private static final TransformerFactory transformerFactory = TransformerFactory
            .newInstance();
    private static Templates templates;

    @EJB(lookup = "java:global/ruslan/recordSchema/ruslan", beanInterface = RecordSchema.class)
    private RecordSchema ruslanRecordSchema;

    //@PostConstruct
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
    public String getURI() {
        return URI;
    }

    @Override
    public String toString(Record record, String encoding) throws Exception {
        return ruslanRecordSchema.toString(record, encoding);
    }

   //@Override
   //public Document toDocument(Record record, String encoding) throws Exception {
   //    return transformSchema(ruslanRecordSchema.toDocument(record, encoding));
   //}

    //@Override
    public org.jsoup.nodes.Document transformSchema(Document src) throws Exception {
        Transformer transformer = templates.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(src), result);

        PatternFactory.createPatternsForType();
        XmlParser parser = new XmlParser((Document) result.getNode());
        Map<String, String> fields = parser.getFields();
        HarvardBuilder builder = new HarvardBuilder(fields);
        return builder.build();
    }

    @Override
    public Record normalize(Record record, String encoding) {
        return ruslanRecordSchema.normalize(record, encoding);
    }

    @Override
    public Record denormalize(Record record, String encoding) {
        return ruslanRecordSchema.denormalize(record, encoding);	}


}
