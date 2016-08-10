# Stag
####Using Gson Without Reflection####

### Gradle usage
Stag can be easily incorporated into your projects using Gradle; it is available on jCenter but can also be built as a submodule. In addition to including Stag, you will need to apply a plugin that allows you to use Java annotation processors.

#### Apply a Gradle Annotation Processor Plugin
If you are using Stag for a Java project, you will need to include (`net.ltgt.gradle:gradle-apt-plugin:0.6`) and apply the plugin (`apply plugin 'net.ltgt.apt'`) in the `build.gradle` file of the module you wish to use Stag in.
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

If you are using this for an Android project, you will need to use the android specific plugin (`com.neenbedankt.gradle.plugins:android-apt:1.8`). You must apply the plugin (`apply plugin: 'com.neenbedankt.android-apt'`) in the `build.gradle` file of the module you wish to use this in.
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

#### Include Stag using jCenter
```groovy
dependencies {
    compile 'com.vimeo.stag:stag-library:1.0.0'
    apt 'com.vimeo.stag:stag-library-compiler:1.0.0'
}
```

#### Include Stag as a Submodule
To add Stag as a submodule, you must clone this repository into your project. Next, add the following dependencies:
```groovy
dependencies {
    compile project(':stag-library')
    apt project(':stag-library-compiler')
}
```


### Why build Stag?

Gson is the essential JSON parsing library. It greatly simplifies what can be the verbose and boilerplate-ridden process of parsing JSON into model objects. It does this by leveraging reflection. Unfortunately, using reflection can be slow (particularly on the Android OS). Gson also includes the concept of creating your own custom `TypeAdapter` that tells Gson how to (de)serialize an object. The main use case is for classes that you don't have control over (e.g. parsing a string passed in JSON into a `Date` object), but if you really need to reduce the (de)serialization time of Gson in a performance critical part of your code, you can create a TypeAdapter that doesn't use reflection.

But... if you have a lot of model objects, and you want to remove the use of reflection for (de)serialization of each one, suddenly you have to write many, many TypeAdapters. If you've ever written one or many of these TypeAdapters, you will know that it is a tedious process. In fact, if you end up writing your own you might ask what you are doing using Gson in the first place!!!

The Stag library solves this problem. It leverages annotations to automatically generate reflection-less TypeAdapters for your model objects. Instead of writing your own custom TypeAdapters for each model object, or forgoing the performance gain of eliminating reflection, using Stag and the `@GsonAdapterKey` annotation on your model fields will do all the work for you.

### How to use

- You do not need to use `@SerializedName("json_key")`.
- Your model class must have a zero argument constructor.
- The member variables of your model class need to have `public` visibility (for now).
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

- Generate code in such a way that member variables only need to be package local
