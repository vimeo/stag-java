/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Vimeo
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
import com.vimeo.sample.model.DateParser;
import com.vimeo.sample.model.Video;
import com.vimeo.sample.model.VideoList;
import com.vimeo.sample.stag.generated.Stag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public final class NetworkRequest {

    static final String TAG = NetworkRequest.class.getSimpleName();

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
        Callback mCallback;
        @NonNull
        private final Handler mHandler;

        Request(@NonNull Callback Callback) {
            mCallback = Callback;
            mHandler = new Handler(Looper.myLooper());
        }

        void cancelRequest() {
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

                Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateParser())
                        .registerTypeAdapterFactory(new Stag.Factory())
                        .create();

                JsonObject jsonObject = new JsonParser().parse(builder.toString()).getAsJsonObject();

                long time = System.currentTimeMillis();

                videos.addAll(gson.fromJson(jsonObject, VideoList.class).data);

                Log.d(TAG, "Time elapsed while parsing: " + (System.currentTimeMillis() - time) + " ms");

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
