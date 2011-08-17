/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package airx.storage;

import java.security.MessageDigest;
import java.security.DigestInputStream;

import java.io.IOException;
import java.util.*;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;

import javax.servlet.ServletException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author sherwinp
 */
@WebServlet("/secure/upload")
@MultipartConfig(fileSizeThreshold = 4096)
public class FileController extends HttpServlet {

    int BUFFER_SIZE = 4096;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("uploadfile.jsf").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        Collection<Part> parts = request.getParts();
        String ticket = String.format("T%s.", new Date().getTime());
        
            File tempDir = (File) request.getSession().getServletContext().getAttribute("javax.servlet.context.tempdir");
            
            String tempFileName = String.format("%s%s", ticket, "upload.dat");

            byte[] md5 = null;
            try {
                for (Part part : parts) {
                    String fieldName = part.getName();
                    if (fieldName != null && fieldName.equalsIgnoreCase("theFile")) {

                        md5 = writefile(request.getSession(),
                                tempDir, tempFileName,
                                part.getInputStream(), part.getSize());

                        break;
                    }
                }
            } catch (Exception ex) {
            }

            java.util.logging.Logger.getAnonymousLogger().info(String.format("Uploaded: %s%s%s", tempDir, File.separator, tempFileName));
  /*
   *    Write Shipment Record
   */
            Shipments.writeshipmentrecord(tempDir + File.separator + tempFileName, md5);
        
        HttpSession session = request.getSession();
        session.setAttribute("shipmentfileId", ticket);
        response.sendRedirect("filestatus.jsf");
        return;
    }

    private byte[] writefile(HttpSession session, File tempDir, String fileName, InputStream inStream, long lngContentSize)
            throws IOException, NoSuchAlgorithmException {
        byte[] buffer = new byte[BUFFER_SIZE];
        FileOutputStream fileOutputStream = new FileOutputStream(new File(tempDir, fileName));
        int bulk;
        long lngPosition = 0;

        // compute MD5 digest 
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        DigestInputStream dinStream = new DigestInputStream(inStream, md5);
        while (true) {
            bulk = dinStream.read(buffer);
            lngPosition += bulk;
            if (bulk < 0) {
                break;
            }
            // write read content to out stream
            fileOutputStream.write(buffer, 0, bulk);
            fileOutputStream.flush();
            // update progress percentage in user's session object
            session.setAttribute("upload", ((double) lngPosition / (double) lngContentSize) * 100);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        }

        fileOutputStream.close();
        dinStream.close();
        inStream.close();
        // Return 
        return md5.digest();
    }
    /*
     * table of file signatures (aka "magic numbers")
     * we will decompress
     *  001F009D    GZ,GZIP,TGZ
     *  001F009E    GZ MAGIC
     *  001F001E    PACK MAGIC
     *  001F008B    PKZIP MAGIC
     *  04034b50    WinZIP
     */
    public static HashMap<String, String>  FileHeaderLookup(){
       HashMap<String, String> FILEHEADERS = new HashMap<String, String>();
        char[][] headers = {
            {0x20, 0x20, 0x20, 0x20},
            {0x25, 0x50, 0x44, 0x46},
            {0x50, 0x4b, 0x3, 0x4},
            {0x1e, 0x0, 0x1f, 0x0},
            {0x8b, 0x0, 0x1f, 0x0},
            {0x9e, 0x0, 0x1f, 0x0},
            {0x9d, 0x0, 0x1f, 0x0},
            {0x4d, 0x5a, 0x0, 0x0}
        };
        FILEHEADERS.put(new String(headers[0]), new String("UNKNOWN"));
        FILEHEADERS.put(new String(headers[1]), new String("PDF"));
        FILEHEADERS.put(new String(headers[2]), new String("WINZIP"));
        FILEHEADERS.put(new String(headers[3]), new String("PACK"));
        FILEHEADERS.put(new String(headers[4]), new String("PKZIP"));
        FILEHEADERS.put(new String(headers[5]), new String("GZ"));
        FILEHEADERS.put(new String(headers[6]), new String("GZIP"));
        FILEHEADERS.put(new String(headers[7]), new String("EXE"));
        return FILEHEADERS;
    }
    public static String FileType (java.io.FileInputStream reader)
            throws IOException {
        HashMap<String, String> FILEHEADERS = FileHeaderLookup();
        char[] fileHeader = new char[4];
        java.io.InputStreamReader ireader = new java.io.InputStreamReader(reader, java.nio.charset.Charset.forName("UTF-8"));

        if (ireader.read(fileHeader, 0, fileHeader.length) != fileHeader.length) {
            return "UNKNOWN";
        }
        String result = FILEHEADERS.get(new String(fileHeader));
        if (result == null || result.isEmpty()) {
            return "UNKNOWN";
        } else {
            return result;
        }
    }
  
    public static boolean isCompressed (String fileheaderresult) throws java.io.IOException
    {
        boolean compressed=false;
        HashMap<String, String> FILEHEADERS = FileHeaderLookup();        
        int ix = 0;
     
        for ( String teststring: FILEHEADERS.values()){
        
            if(teststring.equals(fileheaderresult)){
                compressed = ix > 1;
                break;
            }
            
            ix++;
        }
        return compressed;
    }
}
