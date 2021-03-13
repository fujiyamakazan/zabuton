package net.nanisl.zabuton.file;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XmlFileObj extends Utf8FileObj {
    private static final long serialVersionUID = 1L;

    public XmlFileObj(File file) {
        super(file);
    }

    public String getText(String xpath) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            XPath xpathObj = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpathObj.compile(xpath);
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            String text = "";
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element e = (Element) nodeList.item(i);
                text = e.getTextContent();
            }
            return text;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
