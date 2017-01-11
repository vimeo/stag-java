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
package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

import java.util.ArrayList;
import java.util.List;

/**
 * This class simulates the scenario where
 * a class has multiple fields of the same
 * type as well as parameterized fields
 * with that type as a parameter. This ensures
 * that multiple adapter fields are not
 * generated for the same type within the
 * type adapter factory.
 */
@UseStag
public class IdenticalFieldTypes {

    @SerializedName("mUser")
    User mUser;

    @SerializedName("mSecondUser")
    User mSecondUser;

    @SerializedName("mUsersList")
    ArrayList<User> mUsersList;

    @SerializedName("mSecondUsersList")
    ArrayList<User> mSecondUsersList;

    @SerializedName("mStatsArrayList")
    ArrayList<Stats> mStatsArrayList;

    @SerializedName("mListOfList")
    List<List<Stats>> mListOfList;

    @SerializedName("mStats")
    Stats mStats;
}