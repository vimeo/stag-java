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

    private final List<Video> mVideoList = new ArrayList<>();
    private ArrayAdapter mAdapter;
    private NetworkRequest mRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listView = (ListView) findViewById(R.id.list_view);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        final TextView errorText = (TextView) findViewById(R.id.error_text);

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