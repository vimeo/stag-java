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
package com.vimeo.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vimeo.sample.model.Video;
import com.vimeo.sample.network.NetworkRequest;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final List<Video> mVideoList = new ArrayList<>();
    ArrayAdapter<?> mAdapter;
    private NetworkRequest mRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listView = findViewById(R.id.list_view);
        final ProgressBar progressBar = findViewById(R.id.progress_bar);
        final TextView errorText = findViewById(R.id.error_text);

        if (listView == null || progressBar == null || errorText == null) {
            throw new RuntimeException("Unable to find view");
        }

        mAdapter = new ArrayAdapter<>(this, R.layout.row_layout, R.id.text, mVideoList);
        listView.setAdapter(mAdapter);

        mRequest = NetworkRequest.startRequest(new NetworkRequest.Callback() {
            @Override
            public void onReceived(List<Video> list) {
                mVideoList.clear();
                mVideoList.addAll(list);
                mAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                errorText.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "Oops, couldn't load the videos", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                errorText.setVisibility(View.VISIBLE);
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRequest != null) {
            mRequest.cancelRequest();
        }
    }

}
