package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

import java.util.ArrayList;

public class IdenticalFieldTypes {

    @GsonAdapterKey
    User mUser;

    @GsonAdapterKey
    User mSecondUser;

    @GsonAdapterKey
    ArrayList<User> mUsersList;

    @GsonAdapterKey
    ArrayList<User> mSecondUsersList;

    @GsonAdapterKey
    ArrayList<Stats> mStatsArrayList;

    @GsonAdapterKey
    Stats mStats;
}
