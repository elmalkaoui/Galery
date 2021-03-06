package fr.telemaque.galery.galery.managers;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.telemaque.galery.galery.api.GalleryInterface;
import fr.telemaque.galery.galery.models.Picture;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by THINKPAD T450 on 17/11/2018.
 */

/*
* this class design a web service manager, within we configure retrofit and handling asynchrones requests
*/
public class WSManager {
    private static WSManager instance;

    public static WSManager getInstance() {
        return instance != null ? instance : (instance = new WSManager());
    }

    //this methode return a OkHttpclient configured to log actions and connect and erad time out
    private OkHttpClient getHttpClient() {
        OkHttpClient okClient = new OkHttpClient.Builder()
                //add Intercepter for logging actions
                .addInterceptor(
                        new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request original = chain.request();
                                Request.Builder requestBuilder = original.newBuilder()
                                        .method(original.method(), original.body());
                                requestBuilder.removeHeader("User-Agent");
                                Request request = requestBuilder.build();
                                Response response = chain.proceed(request);
                                Log.e("WS", "Request: " + request.url());
                                return response;
                            }
                        })
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();
        return okClient;
    }

    //configuration of retrofit
    private Retrofit getRetrofit(String baseURL) {
        return new Retrofit.Builder()
                .baseUrl(baseURL)
                // convert the json automatically
                .addConverterFactory(GsonConverterFactory.create())
                .client(getHttpClient())
                .build();
    }

    public void cancelAll() {
        //cancel all httpclient calls currently enqueued or executing
        getHttpClient().dispatcher().cancelAll();
    }

    //this method return an implementation of web the service
    private GalleryInterface getAPIService() {
        return getRetrofit("http://stage-api-mbe.tlmq.fr").create(GalleryInterface.class);
    }

    //function return list of pictures retrofit convert automatically result into ArrayList
    public Callback<ArrayList<Picture>> getPictures(Callback<ArrayList<Picture>> callback) {
        Call<ArrayList<Picture>> call = getAPIService().getPictures();
        call.enqueue(callback);
        return callback;
    }

}
