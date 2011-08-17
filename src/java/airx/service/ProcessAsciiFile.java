/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package airx.service;

import airx.storage.Shipments;
import java.net.URISyntaxException;

/**
 *
 * @author sherwinp
 */
public class ProcessAsciiFile {
    Job job;
    ProcessAsciiFile(Job job){
        this.job = job;
    }
    void run(java.io.File file) throws java.net.URISyntaxException, java.io.IOException {
        Shipments.LoadDBText(new java.net.URI("xcc://superuser:niksoft@localhost:8003"), file, "/U15/processed/" + file.getName());
        
        /*
                     int bulk;
            long lngPosition = 0;

            // compute MD5 digest 
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            DigestInputStream dinStream = new DigestInputStream(filestream, md5);

            boolean done = false;
            while (!done) { // record line processing
                bulk = dinStream.read(buffer);
                lngPosition += bulk;
                Thread.currentThread().sleep(1000);

            

            byte[] md5hash = md5.digest(); // of scanned uncompressed content

         */
    }
}
