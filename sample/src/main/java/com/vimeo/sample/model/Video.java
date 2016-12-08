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

import java.util.Date;

/**
 * Simple video model used by the sample app.
 */
@UseStag
public class Video {

    @SerializedName("user")
    public User mUser;

    @SerializedName("link")
    public String mLink;

    @SerializedName("name")
    public String mName;

    @SerializedName("created_time")
    public Date mCreatedTime;

    @SerializedName("stats")
    public Stats mStats;

    @Override
    public String toString() {
        return "user: { " + (mUser != null ? mUser.toString() : null) + " }\nlink: " + mLink + "\nname: " +
               mName + "\ncreated_time: " + mCreatedTime + "\nstats: { " +
               (mStats != null ? mStats.toString() : null) + " }";
    }
}
