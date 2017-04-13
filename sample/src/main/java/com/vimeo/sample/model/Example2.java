package com.vimeo.sample.model;

import java.util.List;

/**
 * Created by anshul.garg on 13/04/17.
 */

public abstract class Example2<T extends User> {
    public List<Renderable<T>> value;
}
