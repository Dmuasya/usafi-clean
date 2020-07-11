package com.dennis.usaficustomer.Remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import com.dennis.usaficustomer.Model.FCMResponse;
import com.dennis.usaficustomer.Model.Sender;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAigy_yOA:APA91bEAhg-7YrB_-qxFuN5Vpc1jGu6u58CMLgflBX8Jef_-BIikhvc3sqL7GdlGpHtptDraBBBh7aef-ZOpJiYC0GIAbMV4FWncgThVNXQXbbxZWxTRqj50Wu-mNkngZETk443COGXB"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessege(@Body Sender body);
}
