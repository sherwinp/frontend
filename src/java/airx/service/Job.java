/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package airx.service;

import airx.storage.FileController;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.security.DigestInputStream;

import java.util.zip.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.FactoryConfigurationError;
import org.xml.sax.Attributes;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.marklogic.xcc.*;
import com.marklogic.xcc.exceptions.RequestException;

import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author sherwinp
 */
public class Job extends Thread {

    File shipmentFile;

    public Job(File shipmentFile) {
        this.setDaemon(false);
        this.shipmentFile = shipmentFile;
    }

    public void run() {
        try {

            XMLReader rparser = XMLReaderFactory.createXMLReader();
            ShipmentContentHandler shipmentcontent = new ShipmentContentHandler();
            rparser.setContentHandler(shipmentcontent);
            rparser.parse(new InputSource(new FileInputStream(shipmentFile)));


            if (!shipmentcontent.filePath.isEmpty()) {

                File datFile = new File(shipmentcontent.filePath);
                if (shipmentcontent.compressed) {
                    datFile = decompress(datFile);
                }
                // uncompressed data file process
                processFile(datFile);

                //update shipment record in db
                //possibly store datFile
                Terminate();
            }

        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (FactoryConfigurationError e) {
        } catch (Throwable t) {
            t.printStackTrace();
        }

        System.out.println("Job: Run() -- Complete.");
        try {
            Thread.sleep(3000);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    File decompress(File datFile) throws FileNotFoundException, IOException {
        int BUFFER = 4096;
        // decompress file
        java.util.zip.ZipEntry entry;

        java.util.zip.ZipFile zipfile = new java.util.zip.ZipFile(datFile, java.util.zip.ZipFile.OPEN_READ);
        java.util.Enumeration e = zipfile.entries();
        String filePath = "";
        if (e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();

            if (entry.isDirectory()) {
                zipfile.close();
                throw new java.io.IOException("File Entry not found in Zip File!");
            }

            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            System.out.println("Extracting: " + entry);
            is = new BufferedInputStream(zipfile.getInputStream(entry));
            int count;
            byte data[] = new byte[BUFFER];

            filePath = datFile.getPath() + ".xtrc";
            FileOutputStream fos = new FileOutputStream(filePath);
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = is.read(data, 0, BUFFER))
                    != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
            fos.close();
            is.close();
        }
        return new File(filePath);
    }

    void processFile(File deCompressedFile) throws
            java.net.URISyntaxException, IOException, FileNotFoundException, 
            InterruptedException, NoSuchAlgorithmException, T2DataFormatException {
        int BUFFER_SIZE = 4;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bulk = 0;
        FileInputStream filestream = null;
        try {
            //Access File Stream
            filestream = new java.io.FileInputStream(deCompressedFile);
            filestream.read(buffer);
            filestream.close();
                if (bulk < 0) {
                    
                }
                // test first byte of decompressed file
                switch (buffer[0]) {
                    //possible good
                    case 'T':
                        new ProcessAsciiFile(this).run(deCompressedFile);
                        break;  // acsii file   
                    case 0x3c:
                        
                        break; // xml file    
                    case 0x25:
                        
                        break; // pdf file
                    // all bad
                    case 0x4d:
                        throw new T2DataFormatException();
                    case 0xd:
                        throw new T2DataFormatException(); // office document
                    default:
                        throw new T2DataFormatException(); // unknown
                }
        
        } catch (IOException e) {
            if(filestream != null)
                filestream.close();
        }
    }

    public void Terminate() {
        String[] children = null;
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                boolean result = name.startsWith(shipmentFile.getName().substring(0, 15));
                return result;
            }
        };
        children = shipmentFile.getParentFile().list(filter);
        for (String fileName : children) {
            new File(shipmentFile.getParentFile(), fileName).delete();
        }
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = super.equals(obj);
        Job job = ((Job) obj);
        if (job != null) {
            result = shipmentFile.getName().equals(job.getShipmentFileName());
        }
        return result;
    }

    @Override
    public void start() {
        super.start();
    }

    public String getShipmentFileName() {
        return shipmentFile.getName();
    }

    class ShipmentContentHandler extends DefaultHandler {

        String tempVal;
        public String filePath;
        public boolean compressed;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (qName.equalsIgnoreCase("filepath")) {
                filePath = tempVal;
            } else if (qName.equalsIgnoreCase("compressed")) {
                compressed = Boolean.parseBoolean(tempVal);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            tempVal = new String(ch, start, length);

        }
    }
}
