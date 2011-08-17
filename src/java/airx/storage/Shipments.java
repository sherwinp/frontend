/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package airx.storage;

import airx.storage.FileController;
import java.text.SimpleDateFormat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.io.IOException;
import java.util.Calendar;
import jdbm.InverseHashView;
import jdbm.PrimaryStoreMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.SecondaryKeyExtractor;
import jdbm.SecondaryTreeMap;
import jdbm.Serializer;
import jdbm.SerializerInput;
import jdbm.SerializerOutput;
import jdbm.helper.Serialization;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.marklogic.xcc.*;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;

import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author sherwinp
 */
public class Shipments {

    public static void writeshipmentrecord(String filePath, byte[] md5Hash) {
        String filePathShipmentRecord = filePath.replaceAll(".dat", ".txt");
        Document doc = null;
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            doc = factory.newDocumentBuilder().newDocument();
            doc.setXmlStandalone(true);

            Element rootElement = doc.createElement("shipment");
            doc.appendChild(rootElement);

            /*
             * YYYY-MM-DDThh:mm:ss
             * where the capital letter T is used to separate 
             * the date and time components.
             * Example: 2003-04-01T13:01:02 
             * see ISO 8601 timestamp format
             */
            Calendar calendardatetime = java.util.GregorianCalendar.getInstance();
            calendardatetime.setTime(new java.util.Date()); 

            Node node = rootElement.appendChild(doc.createElement("datetime"));
            node.setTextContent(javax.xml.bind.DatatypeConverter.printDateTime(calendardatetime));

            node = rootElement.appendChild(doc.createElement("filepath"));
            node.setTextContent(filePath);

            node = rootElement.appendChild(doc.createElement("filehash"));
            node.setTextContent(new String(Base64.encodeBase64(md5Hash)));

            java.io.FileInputStream filestream = new java.io.FileInputStream(filePath);
            
            node = rootElement.appendChild(doc.createElement("filemimetype"));
            String fileheadermagicnumber = FileController.FileType(filestream);
            node.setTextContent(fileheadermagicnumber);
            
            filestream.close();
            filestream = null;
            
            node = rootElement.appendChild(doc.createElement("compressed"));
            node.setTextContent(Boolean.toString(FileController.isCompressed(fileheadermagicnumber)));

            WriteXMLDoc(filePathShipmentRecord, doc);

            System.out.println("XML File Created Succesfully");
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }


        try {
            java.io.File fileShipmentRecord = new java.io.File(filePathShipmentRecord);
            LoadDBXml(new java.net.URI("xcc://superuser:super@localhost:8003"), fileShipmentRecord, "/U15/" + fileShipmentRecord.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void WriteXMLDoc(String filePath, Document doc) {
        try {
            //Save the Created XML on Local Disc using Transformation APIs
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource s = new DOMSource(doc);

            StreamResult res = new StreamResult(new java.io.FileOutputStream(filePath));
            transformer.transform(s, res);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void LoadDBXml(java.net.URI uriUpload, java.io.File file, String destination)
            throws IOException {
        try {
            Session session = ContentSourceFactory.newContentSource(uriUpload).newSession();
            ContentCreateOptions cco = new ContentCreateOptions();
            cco.setEncoding("UTF-8");
            cco.setRepairLevel(DocumentRepairLevel.NONE);
            cco.setFormatXml();
            Content content = ContentFactory.newContent(destination, file, cco);
            session.insertContent(content);
        } catch (com.marklogic.xcc.exceptions.XccConfigException e) {
            e.printStackTrace();
        } catch (com.marklogic.xcc.exceptions.RequestException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static void LoadDBText(java.net.URI uriUpload, java.io.File file, String destination)
            throws IOException {
        try {
            Session session = ContentSourceFactory.newContentSource(uriUpload).newSession();
            ContentCreateOptions cco = new ContentCreateOptions();
            cco.setEncoding("UTF-8");
            cco.setRepairLevel(DocumentRepairLevel.NONE);
            cco.setFormatText();
            Content content = ContentFactory.newContent(destination, file, cco);
            session.insertContent(content);
        } catch (com.marklogic.xcc.exceptions.XccConfigException e) {
            e.printStackTrace();
        } catch (com.marklogic.xcc.exceptions.RequestException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static void LoadDBBinary(java.net.URI uriUpload, java.io.File file, String destination)
            throws IOException {
        try {
            Session session = ContentSourceFactory.newContentSource(uriUpload).newSession();
            ContentCreateOptions cco = new ContentCreateOptions();
            cco.setEncoding("UTF-8");
            cco.setRepairLevel(DocumentRepairLevel.NONE);
            cco.setFormatBinary();
            Content content = ContentFactory.newContent(destination, file, cco);
            session.insertContent(content);
        } catch (com.marklogic.xcc.exceptions.XccConfigException e) {
            e.printStackTrace();
        } catch (com.marklogic.xcc.exceptions.RequestException e) {
            e.printStackTrace();
        } finally {
        }
    }
}
/*
 * 
 * CREATE TABLE [dbo].[shpmntrec](
[filing_year] [int] NOT NULL,
[e_filename] [char](15) NOT NULL,
[tcc] [char](5) NOT NULL,
[ein] [char](9) NOT NULL,
[tcb] [char](2) NOT NULL,
[trnsmttr_name] [varchar](50) NOT NULL,
[trnsmttr_address] [varchar](40) NOT NULL,
[trnsmttr_city] [varchar](40) NOT NULL,
[trnsmttr_state] [char](2) NOT NULL,
[trnsmttr_zip] [varchar](9) NOT NULL,
[trnsmttr_phone] [varchar](20) NOT NULL,
[trnsmttr_ph_ext] [varchar](10) NULL,
[contact_name] [varchar](31) NOT NULL,
[email] [varchar](50) NULL,
[data_type] [varchar](4) NOT NULL,
[payee_count] [int] NOT NULL,
[rec_count] [int] NOT NULL,
[e_repl_filename] [char](15) NOT NULL,
[pin_used] [varchar](10) NOT NULL,
[recv_date] [datetime] NULL,
[sent] [bit] NOT NULL,
[t2_processed] [bit] NOT NULL,
[isGood] [bit] NOT NULL,
[replace] [bit] NOT NULL,
[processed] [bit] NOT NULL,
[prcs_date] [datetime] NULL,
[file_released] [bit] NOT NULL,
[t2_error_code] [char](3) NOT NULL,
[tcb_upload_ind] [bit] NOT NULL,
[irb_upload_ind] [char](1) NOT NULL,
[addtl_error_ind] [bit] NOT NULL,
[msg_read_date] [datetime] NULL,
[comment] [varchar](4000) NULL,
[twenty_one_day_letter_count] [smallint] NULL,
[date_21_day_letter_sent] [datetime] NULL,
[file_status_letter_sent] [char](1) NULL,
[date_file_status_letter_sent] [datetime] NULL,
[media_type] [char](1) NULL,
[their_filename] [varchar](50) NULL,
[their_ipaddr] [varchar](30) NULL,
[fraud_mark] [varchar](2) NULL,
CONSTRAINT [PK_shpmntrec] PRIMARY KEY CLUSTERED 
(
[filing_year] ASC,
[e_filename] ASC,
[tcc] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON, FILLFACTOR = 70) ON [PRIMARY]
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

ALTER TABLE [dbo].[shpmntrec]  WITH NOCHECK ADD  CONSTRAINT [CK__shpmntrec__file___6E814571] CHECK  (([file_status_letter_sent]='N' OR [file_status_letter_sent]='Y'))
GO

ALTER TABLE [dbo].[shpmntrec] CHECK CONSTRAINT [CK__shpmntrec__file___6E814571]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_tcb]  DEFAULT (' ') FOR [tcb]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_trnsmttr_address]  DEFAULT (' ') FOR [trnsmttr_address]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_trnsmttr_state]  DEFAULT (' ') FOR [trnsmttr_state]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_trnsmttr_zip]  DEFAULT (' ') FOR [trnsmttr_zip]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_trnsmttr_phone]  DEFAULT (' ') FOR [trnsmttr_phone]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_contact_name]  DEFAULT (' ') FOR [contact_name]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_email]  DEFAULT (' ') FOR [email]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_data_type]  DEFAULT (' ') FOR [data_type]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_payee_count]  DEFAULT ((0)) FOR [payee_count]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [rec_count]  DEFAULT ((0)) FOR [rec_count]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_e_repl_filename]  DEFAULT (' ') FOR [e_repl_filename]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_pin_used]  DEFAULT (' ') FOR [pin_used]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_recv_date]  DEFAULT (getdate()) FOR [recv_date]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [sent]  DEFAULT ((0)) FOR [sent]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [t2_processed]  DEFAULT ((0)) FOR [t2_processed]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF_shpmntrec_isGood]  DEFAULT ((1)) FOR [isGood]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [replace]  DEFAULT ((0)) FOR [replace]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [processed]  DEFAULT ((0)) FOR [processed]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [file_released]  DEFAULT ((0)) FOR [file_released]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [t2_error_code]  DEFAULT ('  ') FOR [t2_error_code]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [tcb_upload_ind]  DEFAULT ((0)) FOR [tcb_upload_ind]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [irb_upload_ind]  DEFAULT ('  ') FOR [irb_upload_ind]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [addtl_error_ind]  DEFAULT ((0)) FOR [addtl_error_ind]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF__shpmntrec__twent__6C98FCFF]  DEFAULT ((0)) FOR [twenty_one_day_letter_count]
GO

ALTER TABLE [dbo].[shpmntrec] ADD  CONSTRAINT [DF__shpmntrec__file___6D8D2138]  DEFAULT ('N') FOR [file_status_letter_sent]
GO
 * */
