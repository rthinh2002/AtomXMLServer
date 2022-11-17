import java.util.HashMap;

// This class will perform as a parser for text to atom xml, but it's just pure text only, not the true xml
public class TextParser {
    private String[] feeds; // This String represent the whole body get insert from the database from the
                            // PUT request
    private String xmlString = "";

    // Constructor
    public TextParser(String[] feeds) {
        this.feeds = feeds;
    }

    // Method to parse
    public void parsingXML() {
        this.xmlString += "<?xml version='1.0' encoding='iso-8859-1' ?>\n<feed xml:lang=\"en-US\" xmlns=\"http://www.w3.org/2005/Atom\">\n";

        // First information feed part
        HashMap<String, String> feedMap = toHashMap(this.feeds[0]);
        parseFirstPart(feedMap);

        for (int i = 1; i < this.feeds.length; i++) {
            // Convert the feeds infomation to HashMap
            feedMap = toHashMap(this.feeds[i]);
            this.xmlString += "\t<entry>\n";
            parseSubPart(feedMap);
            this.xmlString += "\t</entry>\n";
        }
        this.xmlString += "</feed>\n\n";
    }

    private void parseFirstPart(HashMap<String, String> feedMap) {
        this.xmlString += getTitleTag(feedMap.get("title"));
        if (feedMap.containsKey("subtitle"))
            this.xmlString += getSubtitleTag(feedMap.get("subtitle"));
        this.xmlString += getLinkTag(feedMap.get("link"));
        if (feedMap.containsKey("updated"))
            this.xmlString += getUpdatedTag(feedMap.get("updated"));
        if (feedMap.containsKey("author"))
            this.xmlString += getAuthorAndNameTag(feedMap.get("author"));
        this.xmlString += getIdTag(feedMap.get("id"));
        if (feedMap.containsKey("summary"))
            this.xmlString += getSummaryTag(feedMap.get("summary"));
    }

    private void parseSubPart(HashMap<String, String> feedMap) {
        this.xmlString += ("\t" + getTitleTag(feedMap.get("title")));
        if (feedMap.containsKey("subtitle"))
            this.xmlString += ("\t" + getSubtitleTag(feedMap.get("subtitle")));
        this.xmlString += ("\t" + getLinkTag(feedMap.get("link")));
        if (feedMap.containsKey("updated"))
            this.xmlString += ("\t" + getUpdatedTag(feedMap.get("updated")));
        if (feedMap.containsKey("author"))
            this.xmlString += ("\t" + getAuthorAndNameTag(feedMap.get("author")));
        this.xmlString += ("\t" + getIdTag(feedMap.get("id")));
        if (feedMap.containsKey("summary"))
            this.xmlString += ("\t" + getSummaryTag(feedMap.get("summary")));
    }

    // Method to return the tag surround the title -- Assume feed is valid after
    // checking
    private String getTitleTag(String title) {
        return "\t<title>" + title + "</title>\n";
    }

    // Method to return the tag surround the subttile
    private String getSubtitleTag(String subtitle) {
        return "\t<subtitle>" + subtitle + "</subtitle>\n";
    }

    // Method to return the tag surround the link
    private String getLinkTag(String link) {
        return "\t<link>" + link + "</link>\n";
    }

    // Method to return updated
    private String getUpdatedTag(String updated) {
        return "\t<updated>" + updated + "</updated>\n";
    }

    // Method to return author + name
    private String getAuthorAndNameTag(String author) {
        String tag = "\t<author>\n";
        tag += "\t\t<name>";
        tag += author;
        tag += "</name>\n";
        tag += "\t</author>\n";
        return tag;
    }

    // Method to return id
    private String getIdTag(String id) {
        return "\t<id>" + id + "</id>\n";
    }

    // Method to return summary
    private String getSummaryTag(String summary) {
        return "\t<summary>" + summary + "</summary>\n";
    }

    // Method to return a String represent the XML documents for the server to
    // return to client during GET request
    public String getXML() {
        return this.xmlString;
    }

    // Method to convert feed to hashmap
    private HashMap<String, String> toHashMap(String feed) {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] feedLine = feed.split("\n");

        for (int i = 0; i < feedLine.length; i++) {
            if (isAtomElement(feedLine[i])) {
                int delim = feedLine[i].indexOf(":");
                if (delim == -1)
                    continue;
                String atomElement = feedLine[i].substring(0, delim);

                if (atomElement.contains("summary")) { // Summary is at the end of feed
                    String summary = feedLine[i].substring(delim + 1);
                    for (int j = i + 1; j < feedLine.length; j++) { // Add all the line
                        summary += feedLine[j];
                    }
                    map.put(atomElement, summary);
                    break;
                } else { // Other atoms element
                    map.put(atomElement, feedLine[i].substring(delim + 1));
                }
            } else { // Error: Unrecognized atom element
                System.err.println("FEED ERROR! UNRECOGNIZED ATOM ELEMENT " + feedLine[i]);
                break;
            }
        }
        return map;
    }

    // Check if valid atom element
    private boolean isAtomElement(String element) {
        if (element.contains("title") || element.contains("subtitle") || element.contains("link")
                || element.contains("updated")
                || element.contains("author") || element.contains("name") || element.contains("id")
                || element.contains("summary") || element.contains("entry"))
            return true;
        return false;
    }
}
