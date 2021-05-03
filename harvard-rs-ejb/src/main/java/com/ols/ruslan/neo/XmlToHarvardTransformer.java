package com.ols.ruslan.neo;


import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.logging.Logger;


@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Singleton(name = "XmlToHarvardTransformer")
@Startup
@Remote(MediaTypeTransformerFacade.class)
@EJB(name = "java:global/ruslan/mediaType/application/xml/application/harvard", beanInterface = MediaTypeTransformerFacade.class)
public class XmlToHarvardTransformer implements MediaTypeTransformerFacade {
    private static final Logger log = Logger.getLogger(XmlToHarvardTransformer.class
            .getName());
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static Templates templates;


    @PostConstruct
    private void startup() {
        log.info("Startup");
        try {
            templates = transformerFactory.newTemplates(new StreamSource(
                    XmlToHarvardTransformer.class.getClassLoader().getResourceAsStream(
                            "RUSMARC2Harvard.xsl")));

        } catch (TransformerConfigurationException e) {
            log.severe("Unable to initialise templates: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @Override
    public byte[] transform(byte[] content, String encoding) throws Exception {
        //Setting a transformer to transform one xml to another
        Transformer transformer = templates.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMResult result = new DOMResult();

        //Setting a source Document form byte-array
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(content));

        //Transforming and parsing
        transformer.transform(new DOMSource(document), result);
        Map<String, String> fields = XmlParser.parse((Document) result.getNode());
        HarvardBuilder bibTexBuilder = new HarvardBuilder(fields);

        return bibTexBuilder.buildHarvard().getBytes(encoding);
    }

}

