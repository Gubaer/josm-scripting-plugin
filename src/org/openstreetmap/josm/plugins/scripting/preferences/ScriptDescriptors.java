package org.openstreetmap.josm.plugins.scripting.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.openstreetmap.josm.plugins.scripting.util.IOUtil;
import org.xml.sax.SAXException;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="scriptsType")
@XmlRootElement(name="scripts")
public class ScriptDescriptors {

    /**
     * Unmarshals a script descriptor list from an XML stream.
     *
     * @param reader the XML stream
     * @return the list of script descriptors. Never null. Replies an empty list, if the
     * XML stream doesn't contain at least one script descriptor.
     *
     * @throws JAXBException throw if JAXB fails
     * @throws SAXException thrown if parsing the XML stream fails
     * @throws IOException thrown if an IO operation fails
     */
    static public List<ScriptDescriptor> unmarshall(Reader reader) throws JAXBException, SAXException, IOException{
        JAXBContext jc = JAXBContext.newInstance(ScriptDescriptors.class, ScriptDescriptor.class);
        Unmarshaller um = jc.createUnmarshaller();
        um.setSchema(getSchema());
        um.setEventHandler(new ValidationEventHandler() {
            @Override
            public boolean handleEvent(ValidationEvent event) {
                return false;
            }
        });
        ScriptDescriptors sd = (ScriptDescriptors)um.unmarshal(reader);
        return sd.scripts == null ? Collections.<ScriptDescriptor>emptyList() : sd.scripts;
    }

    static public void marshall(Writer writer, List<ScriptDescriptor> scripts) throws JAXBException{
        JAXBContext jc = JAXBContext.newInstance(ScriptDescriptors.class, ScriptDescriptor.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        ScriptDescriptors descs = new ScriptDescriptors();
        descs.scripts = scripts;
        m.marshal(descs, writer);
    }

    /**
     * Replies the XSD schema for the script descriptors file.
     *
     * @return the schema
     * @throws IOException thrown if the schema can't be read from resources
     * @throws SAXException thrown if the schema can't be parsed
     */
    static public Schema getSchema() throws IOException, SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        InputStream is = null;
        try {
            is = ScriptDescriptor.class.getResourceAsStream("script.xsd");
            if (is == null){
                throw new IOException(
                        MessageFormat.format("failed to open input stream to resource ''{0}''", "script.xsd")
                );
            }
            return factory.newSchema(new StreamSource(is));
        } finally {
            IOUtil.close(is);
        }
    }

    @XmlElement(name="script")
    protected List<ScriptDescriptor> scripts;
}