package com.dennis.usaficustomer.Common;

import com.dennis.usaficustomer.Remote.FCMClient;
import com.dennis.usaficustomer.Remote.IFCMService;


public class Common {

    public static String currentToken = "";

    public static final String laundry_tbl = "Laundrys";
    public static final String user_laundry_tbl = "LaundryInformation";
    public static final String user_customer_tbl = "CustomersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";


    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";


    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);

    }

}
