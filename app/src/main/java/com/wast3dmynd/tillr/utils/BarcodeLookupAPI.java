package com.wast3dmynd.tillr.utils;
//import com.cloudmersive.client.invoker.ApiClient;
//import com.cloudmersive.client.invoker.ApiException;
//import com.cloudmersive.client.invoker.Configuration;
//import com.cloudmersive.client.invoker.auth.*;
//import com.cloudmersive.client.BarcodeLookupApi;


public class BarcodeLookupAPI {

    public static String getBarcodeURLString(int barcode, String registrationKey){

        //check if,this lookup query is using the default testing registrationKey; when -
        if(registrationKey==null)registrationKey = "400000000";
        else if(registrationKey.isEmpty())registrationKey = "400000000";

        StringBuilder barcodeURLString = new StringBuilder("http://opengtindb.org/?ean=");
        barcodeURLString.append(String.valueOf(barcode));
        barcodeURLString.append("&cmd=query&queryid=");
        barcodeURLString.append(registrationKey);
        return barcodeURLString.toString();
    }
}
