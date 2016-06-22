package com.vimeo.sample.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateParser extends TypeAdapter<Date> {

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);

    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        out.value(dateFormat.format(value));
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        try {
            return dateFormat.parse(in.nextString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
