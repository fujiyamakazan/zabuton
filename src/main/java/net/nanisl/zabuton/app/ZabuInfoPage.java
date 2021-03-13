package net.nanisl.zabuton.app;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Generics;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ZabuInfoPage extends WebPage {

    public static final String CLASS = "class";
    private static final String TITLE = "title";

    private static final long serialVersionUID = 1L;

    //    public static void main(String[] args) throws Exception {
    //
    //        OutputStreamWriter outStreamWriter = new OutputStreamWriter(System.out, "utf-8");
    //
    //        Map<String, String> datas = Generics.newHashMap();
    //        datas.put(CLASS, "com.example.ExampleApp");
    //        datas.put(TITLE, "サンプルアプリケーション");
    //        writeXml(outStreamWriter, datas);
    //    }

    @Override
    protected void onRender() {

        OutputStreamWriter outStreamWriter = null;
        try {

            //HttpServletResponse containerResponse = (HttpServletResponse) getResponse().getContainerResponse();
            //ServletOutputStream outputStream = containerResponse.getOutputStream();
            OutputStream outputStream = getResponse().getOutputStream();
            outStreamWriter = new OutputStreamWriter(outputStream, "utf-8");

            Map<String, String> datas = Generics.newHashMap();
            datas.put(CLASS, ZabuApp.get().getClass().getName());
            datas.put(TITLE, ZabuApp.get().getTitle());

            writeXml(outStreamWriter, datas);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (outStreamWriter != null) {
                    outStreamWriter.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void writeXml(OutputStreamWriter out, Map<String, String> datas) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation domImpl = builder.getDOMImplementation();
        Document document = domImpl.createDocument("", "root", null);
        Element root = document.getDocumentElement();

        for (Map.Entry<String, String> data : datas.entrySet()) {
            Element item = document.createElement(data.getKey());
            //item.setAttribute("type", "text");
            item.appendChild(document.createTextNode(data.getValue()));
            root.appendChild(item);
        }

        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }

    @Override
    protected void configureResponse(WebResponse response) {
        super.configureResponse(response);
        response.setContentType("text/xml");
    }
}
