package com.example.exampleserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.exampleserver.database.AppDatabase;
import com.example.exampleserver.database.LogItem;
import com.example.exampleserver.server.ApiService;
import com.example.exampleserver.server.ServerResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // todo 4 : create variables
    private String ApiAddress = "http://192.168.2.31/example/";
    private CompositeDisposable disposable;
    private MaterialButton sendButton;
    private ProgressBar progressBar;
    private AppDatabase database;
    private Retrofit retrofit;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // todo 5 : init views
        progressBar = findViewById(R.id.progress_bar);
        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(view -> getLogFromDatabaseAndSendToServer());
        disposable = new CompositeDisposable();

        // todo 6 : init database
        initDatabase();

        // todo 7 : init api service
        initApiService();


    }

    private void getLogFromDatabaseAndSendToServer() {
        progressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
        database.logDao().getLogs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<LogItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<LogItem> logItems) {
                        sendLogsToServer(logItems);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailed(e);
                    }
                });
    }

    private void sendLogsToServer(List<LogItem> items) {

        // send items to server
        apiService.sendLogs(items)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ServerResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(ServerResponse serverResponse) {
                        onSendDataDone(serverResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailed(e);
                    }
                });
    }

    private void onSendDataDone(ServerResponse response) {
        // Handle send done
        progressBar.setVisibility(View.GONE);
        sendButton.setEnabled(true);
        Toast.makeText(this, response.message, Toast.LENGTH_LONG).show();
    }

    private void onFailed(Throwable throwable) {
        // Handle error
        progressBar.setVisibility(View.GONE);
        sendButton.setEnabled(true);
        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();

    }

    private void initApiService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(ApiAddress)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void initDatabase() {
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .allowMainThreadQueries()
                .build();

        database.logDao().checkIfDataExists().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(Integer count) {
                        Log.i("TAG","Count is : "+count);
                        if (count == 0) {
                            // Data not exists in the database
                            addFakeDataToDatabase();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailed(e);
                    }
                });

    }

    private void addFakeDataToDatabase() {
        LogItem logItemHamid = new LogItem("Hamid", "1120");
        LogItem logItemReza = new LogItem("Reza", "1121");
        LogItem logItemJavad = new LogItem("Javad", "1122");
        LogItem logItemAli = new LogItem("Ali", "1123");

        List<LogItem> logItems = new ArrayList<>();
        logItems.add(logItemHamid);
        logItems.add(logItemReza);
        logItems.add(logItemJavad);
        logItems.add(logItemAli);


        database.logDao().insertLogs(logItems)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.i("TAG","Add log to database completed");
                        Toast.makeText(getApplicationContext(), "Add log to database completed", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("TAG","Add log to database failed");
                        Toast.makeText(getApplicationContext(), "Add log to database failed", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();

    }
}