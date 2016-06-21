package com.vimeo.sample.network;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vimeo.sample.model.Video;
import com.vimeo.sample.model.VideoList;
import com.vimeo.stag.generated.AdapterFactory.Factory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public final class NetworkRequest {

    private static final String TAG = NetworkRequest.class.getSimpleName();

    public interface Callback {

        void onReceived(List<Video> list);

        void onError();
    }

    @NonNull
    private final Request mRequest;

    private NetworkRequest(@NonNull Request request) {
        mRequest = request;
    }

    @NonNull
    public static NetworkRequest startRequest(@NonNull Callback callback) {
        Request request = new Request(callback);
        request.executeOnExecutor(Executors.newSingleThreadExecutor());
        return new NetworkRequest(request);
    }

    public void cancelRequest() {
        mRequest.cancelRequest();
    }

    private static final class Request extends AsyncTask<Void, Void, List<Video>> {

        @Nullable
        private Callback mCallback;
        @NonNull
        private final Handler mHandler;

        private Request(@NonNull Callback Callback) {
            mCallback = Callback;
            mHandler = new Handler(Looper.myLooper());
        }

        public void cancelRequest() {
            mCallback = null;
            cancel(true);
        }

        @Override
        protected List<Video> doInBackground(Void... params) {
            String url = "https://api.vimeo.com/channels/staffpicks/videos";
            String token = "bearer b8e31bd89ba1ee093dc6ab0f863db1bd";
            ArrayList<Video> videos = new ArrayList<>();

            StringBuilder builder = new StringBuilder();
            BufferedReader stream = null;
            try {
                URL uri = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
                connection.setRequestProperty("Authorization", token);
                connection.setRequestMethod("GET");
                stream = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;

                while ((line = stream.readLine()) != null) {
                    builder.append(line);
                }

                Gson gson = new GsonBuilder().registerTypeAdapterFactory(new Factory()).create();

                JsonObject jsonObject = new JsonParser().parse(builder.toString()).getAsJsonObject();

                videos.addAll(gson.fromJson(jsonObject, VideoList.class).mVideoList);

            } catch (IOException e) {
                Log.e(TAG, "Error", e);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onError();
                        }
                    }
                });
                return null;
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error", e);
                    }
                }
            }
            return videos;
        }

        @Override
        protected void onPostExecute(final List<Video> videos) {
            if (videos != null && mCallback != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.onReceived(videos);
                    }
                });
            }
        }
    }
}
