import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;

// Referencing: https://zetcode.com/java/dom/
public class XMLParser {
    DocumentBuilder builder;
    InputStream input;
    Document doc;
    boolean hasFeedAuthor = false;

    public XMLParser(String body) {
        try {
            input = new ByteArrayInputStream(body.getBytes("UTF-8"));
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (UnsupportedEncodingException eu) {
            eu.printStackTrace();
        } catch (ParserConfigurationException pe) {
            System.err.println(pe.getMessage());
            pe.printStackTrace();
        }
    }

    // This method act as intermediate to parse the xml to document
    public void parseAtom() throws IOException, SAXException {
        doc = builder.parse(input);
        doc.getDocumentElement().normalize();
    }

    // Return a string of feed after parsing
    public String getFeed() {
        if (doc == null)
            return null;

        StringBuilder sb = new StringBuilder();
        Element feed = doc.getDocumentElement();
        NodeList children = feed.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("entry")) {
                parseEntry(sb, child);
            } else if (child.getNodeName().equals("author")) {
                sb.append("author: " + ((Element) child).getElementsByTagName("name").item(0).getTextContent() + "\n");
            } else if (child.getNodeName().equals("#text")) {
                continue;
            } else {
                sb.append(child.getNodeName() + ": " + child.getTextContent() + "\n");
            }
        }
        return sb.toString();

    }

    // This method will parse the entry, which go after the feed in an xml feed
    private void parseEntry(StringBuilder feedBuilder, Node entry) {
        NodeList children = entry.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("author")) {
                feedBuilder.append(
                        "author: " + ((Element) child).getElementsByTagName("name").item(0).getTextContent() + "\n");
            } else if (child.getNodeName().equals("#text")) {
                continue;
            } else {
                feedBuilder.append(child.getNodeName() + ": " + child.getTextContent() + "\n");
            }
        }
    }
}