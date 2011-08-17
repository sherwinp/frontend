/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package airx.service;

/**
 *
 * @author sherwinp
 */
public class T2DataFormatException extends java.io.IOException{

    @Override
    public String getMessage() {
        return "Data Format Incorrect. " + super.getMessage();
    }
    
}
