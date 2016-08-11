# Stag

Gson uses reflection to map JSON data to an object's fields; Stag eliminates this usage of reflection and speeds up object creation by generating fast Gson type adapters.

### Why Build Stag?

Gson is the essential JSON parsing library. It greatly simplifies what can be the verbose and boilerplate-ridden process of parsing JSON into model objects. It does this by leveraging reflection. Unfortunately, using reflection can be slow (particularly on the Android OS). Gson also includes the concept of creating your own custom `TypeAdapter` that tells Gson how to (de)serialize an object. The main use case is for classes that you don't have control over (e.g. parsing a string passed in JSON into a `Date` object), but if you really need to reduce the (de)serialization time of Gson in a performance critical part of your code, you can create a TypeAdapter that doesn't use reflection.

But... if you have a lot of model objects, and you want to remove the use of reflection for (de)serialization of each one, suddenly you have to write many, many TypeAdapters. If you've ever written one or many of these TypeAdapters, you will know that it is a tedious process. In fact, if you end up writing your own you might ask what you are doing using Gson in the first place!!!

The Stag library solves this problem. It leverages annotations to automatically generate reflection-less TypeAdapters for your model objects. Instead of writing your own custom TypeAdapters for each model object, or forgoing the performance gain of eliminating reflection, using Stag and the `@GsonAdapterKey` annotation on your model fields will do all the work for you.

### Gradle Usage

#### Add the Stag dependencies

from jCenter
```groovy
dependencies {
    compile 'com.vimeo.stag:stag-library:1.0.0'
    apt 'com.vimeo.stag:stag-library-compiler:1.0.0'
}
```

or as a submodule
```groovy
dependencies {
    compile project(':stag-library')
    apt project(':stag-library-compiler')
}
```

#### Add the Annotation Processor Plugin

In a Java project (see below for Android), apply the 'apt' plugin in your module-level `build.gradle`:
```groovy
buildscript {
    repositories {
        maven {
            url 'https://plugins.gradle.org/m2/'
        }
    }
    dependencies {
        classpath 'net.ltgt.gradle:gradle-apt-plugin:0.6'
    }
}

apply plugin: 'net.ltgt.apt'
```

In an Android project, apply the 'android-apt' plugin in your module-level `build.gradle`:
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

apply plugin: 'com.neenbedankt.android-apt'
```

### Usage

- You do not need to use `@SerializedName("json_key")`.
- Your model class must have a zero argument constructor.
- The member variables of your model class need to have `public` visibility ([for now](#future-enhancements)).
- The member variables of your model class that you wish to be populated must be annotated with `@GsonAdapterKey`.
    - If you want to use the variable name as the JSON key, just use `@GsonAdapterKey`.
    - If you want to use a different name as the JSON key, use `@GsonAdapterKey("json_key")`.
- Supported types - Various types and their support shown below:
    - YES: All native types supported by Gson (boolean, double, int, long)
    - YES: String
    - YES: ArrayList (List interface or other types of lists are currently not supported)
    - NO: Enums are not supported, we will fall back to Gson for parsing them

### Example

```java
public class Deer {
    @GsonAdapterKey("name")
    public String mName;
    
    @GsonAdapterKey("species")
    public String mSpecies;
    
    @GsonAdapterKey("age")
    public int mAge;
    
    @GsonAdapterKey("points")
    public int mPoints;
}

public class Herd {
    @GsonAdapterKey("data_list")
    ArrayList<Deer> mIndividuals;
}

/**
 * The class where you receive JSON 
 * containing a list of Deer objects.
 * You parse the list from JSON using
 * Gson.
 */
MyParsingClass {
    private Gson gson = new GsonBuilder()
                                 .registerTypeAdapterFactory(new Stag.Factory())
                                 .create();

    public Herd fromJson(String json) {
        return gson.fromJson(json, Herd.class);
    }
}

```

### Future Enhancements

- Generate code so that member variables only need to be package local

### License
`stag-java` is available under the MIT license. See the [LICENSE](LICENSE) file for more information.
