package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.wicket.util.lang.Generics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * XMLテキストの読み書きします。
 *
 * @author fujiyama
 */
public class XmlText implements Serializable {

    private static final long serialVersionUID = 1L;

    private final File file;

    public XmlText(File file) {
        this.file = file;
    }

    /**
     * XPATHで値を取得します。
     * @param xpath XPATH
     * @return 取得したテキスト
     */
    public List<String> getText(String xpath) {
        List<String> results = Generics.newArrayList();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            XPath xpathObj = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpathObj.compile(xpath);
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element e = (Element) nodeList.item(i);
                results.add(e.getTextContent());
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * XPATHで値を取得します。
     * @param xpath XPATH
     * @return 取得したテキストの先頭１件。取得できなければnull
     */
    public String getTextOne(String xpath) {
        List<String> texts = getText(xpath);
        if (texts.isEmpty()) {
            return null;
        } else {
            return texts.get(0);
        }
    }
}
