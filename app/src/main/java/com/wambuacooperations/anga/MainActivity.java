package com.wambuacooperations.anga;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.wambuacooperations.anga.http.WeatherResponse;
import com.wambuacooperations.anga.http.WeatherService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public static String BaseUrl = "https://api.openweathermap.org/";
    public static String AppId = "0aecef321bf2bb62975976f08ee5f3ef";
    public static String q;
    public static String units="metric";
    ImageView weather_icon;
    Button searchButton;
    TextInputLayout cityNameEditText;
    CardView cardView;
    ProgressBar progressBar;
    TextView cityName,current_time,main_weather_info,temparature,humidity,wind_speed;
    String enteredCity;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityNameEditText=findViewById(R.id.city_input);
        cityName=findViewById(R.id.city_name);
        current_time=findViewById(R.id.current_time);
        main_weather_info=findViewById(R.id.weather_info);
        temparature=findViewById(R.id.temparature);
        humidity=findViewById(R.id.humidity);
        wind_speed=findViewById(R.id.windSpeed);
        weather_icon=findViewById(R.id.weather_icon);


        searchButton=findViewById(R.id.search_button);
        cardView=findViewById(R.id.card);
        progressBar=findViewById(R.id.progressBar);
        cardView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }
    public void searchClicked(View view){


        enteredCity=cityNameEditText.getEditText().getText().toString();
        if (enteredCity.isEmpty()){
            cityNameEditText.setError("Please Enter a city");
        }else{
            cityNameEditText.setVisibility(View.INVISIBLE);
            view.setEnabled(false);
            q=enteredCity;
            progressBar.setVisibility(View.VISIBLE);
            getCurrentData();
        }

        cityNameEditText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cityNameEditText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


     void getCurrentData() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherService service=retrofit.create(WeatherService.class);
        Call<WeatherResponse> call=service.getCurrentWeatherData(q,AppId,units);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.code() ==200){
                    WeatherResponse weatherResponse = response.body();
                    assert weatherResponse!=null;

                progressBar.setVisibility(View.GONE);
                cardView.setVisibility(View.VISIBLE);
                cityName.setText(weatherResponse.getName());
                main_weather_info.setText(weatherResponse.getWeather().get(0).getDescription());

                    int temp= Integer.valueOf(Math.toIntExact(Math.round(weatherResponse.getMain().getTemp())));
                    String icon=weatherResponse.getWeather().get(0).getIcon();
                    String iconURL="https://openweathermap.org/img/wn/"+icon+"@2x.png";

                    Glide.with(getApplicationContext())
                            .load(iconURL)
                            .centerCrop()
                            .into(weather_icon);


                    Date currentTime = Calendar.getInstance().getTime();
                    current_time.setText(String.valueOf(currentTime));
                    String windString =weatherResponse.getWind().getSpeed()+"m/s Winds";
                    String humidityString = weatherResponse.getMain().getHumidity() +"% Humidity";

                    String temparatureString=temp+"\u2103";
                    temparature.setText(temparatureString);

                    humidity.setText(humidityString);
                    wind_speed.setText(windString);


                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {

               Log.i("ERROOOOOOR!!!!",t.getMessage());
               showErrorSnackBar();
            }
        });
    }

    public void closeCard(View view){
        cityNameEditText.setVisibility(View.VISIBLE);
        cityNameEditText.getEditText().setText("");
        searchButton.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        cardView.setVisibility(View.INVISIBLE);
    }

    private void showErrorSnackBar() {

        View rootView = findViewById(android.R.id.content);
        final Snackbar snackbar = Snackbar
                .make(rootView, "Error Loading Weather Information", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentData();
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }


}
