/*
 * Created on Jun 6, 2003
 */
package edu.duke.cs.snarfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Administrator
 */
public class XMLUtils {

    public static Document parseXML(InputStream in) throws IOException,
            SAXException {
        Document doc = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            doc = builder.parse(in);
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch (FactoryConfigurationError e) {
            throw new IOException(e.getMessage());
        }
        return doc;
    }

    public static Document createDOM() {
        Document doc = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            doc = builder.newDocument();
            return doc;
        } catch (ParserConfigurationException e) {
            return null;
        } catch (FactoryConfigurationError e) {
            return null;
        }
    }

    public static void writeXML(OutputStream out, Node xml) {
        try {
            TransformerFactory tffactory = TransformerFactory.newInstance();
            Transformer transformer = tffactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(xml);
            StreamResult result = new StreamResult(out);

            transformer.transform(source, result);
            try {
            	out.flush();
                out.close();
            } catch (IOException e1) {
            }

        } catch (TransformerConfigurationException e) {
        } catch (TransformerFactoryConfigurationError e) {
        } catch (TransformerException e) {
        }
    }

    public static OutputStream openOutput(File f) throws FileNotFoundException {
        f.getParentFile().mkdirs();
        return new BufferedOutputStream(new FileOutputStream(f), 4096);
    }

    public static String stripRoot(String filename) {
        File[] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            if (filename.toLowerCase().startsWith(
                    roots[i].toString().toLowerCase()))
                return filename.substring(roots[i].toString().length());
        }
        return filename;
    }

    public static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] buf = new byte[4096];
        int bytes;
        while ((bytes = in.read(buf)) != -1)
            out.write(buf, 0, bytes);
    }

    public static String getFileName(URL url) {
        File f = new File(url.getFile());
        return f.getName();
    }
}
