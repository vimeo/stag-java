# Stag

Stag improves Gson performance by automatically generating reflection-less TypeAdapters for your model objects.

## Why Build Stag?

Gson is the essential JSON parsing library. It greatly simplifies what can be the verbose and boilerplate-ridden process of parsing JSON into model objects. It does this by leveraging reflection. Unfortunately, using reflection can be slow (particularly on the Android OS). 

You can work around this by writing a custom `TypeAdapter`, a class that Gson uses to (de)serialize an object. The main use case for custom type adapters is for classes that you don't have control over (e.g. parsing a JSON string into a `java.util.Date` object). They are used to manually map JSON to fields in your model object. So, you can just write a custom TypeAdapter to tell Gson how to map data to fields and the performance will improve, since it won't have to use reflection.

But... if you have a lot of model objects, and you want to remove the use of reflection for (de)serialization of each one, suddenly you have to write many, many TypeAdapters. If you've ever written one or many of these type adapters, you will know that it is a tedious process. In fact, when writing your own `TypeAdapter`, you might ask what you are doing using Gson in the first place!!! 

The Stag library solves this problem. It leverages annotations to automatically generate reflection-less TypeAdapters for your model objects at compile time. Instead of spending time writing your own custom TypeAdapters for each model object, or forgoing the performance gain of eliminating reflection, using Stag and the `@GsonAdapterKey` annotation on your model fields will do all the work for you.

## Gradle Usage

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

## Usage

1. Make sure the member variables of your model class are public ([for now](#future-enhancements))
2. Make sure your model class is public and has a zero argument public constructor
3. Annotate each member variable you want populated
    - `@GsonAdapterKey("json_key")`: populates the field using the JSON value with the specified key
    - `@GsonAdapterKey`: populates the field using the JSON value with the key named the same as the member variable
4. Register the `Stag.Factory` with Gson when you create your Gson instance: `Gson gson = new GsonBuilder().registerTypeAdapterFactory(new Stag.Factory()).create();`
5. You're done!

Note:
- `@SerializedName("json_key")` annotations you might be using will be ignored.
- Variable types supported by Stag:
    - YES: All native types supported by Gson (boolean, double, int, long)
    - YES: String
    - YES: ArrayList (List interface or other types of lists are currently not supported)
    - NO: Enums are not supported, we will fall back to Gson for parsing them

See the [example below](#example) or the [sample app](sample) to get more info on how to use Stag.

## Example

```java
public class Deer {
    @GsonAdapterKey("name")
    public String mName;    // mName = json value with key "name"
    
    @GsonAdapterKey("species")
    public String mSpecies; // mSpecies = json value with key "species"
    
    @GsonAdapterKey("age")
    public int mAge;        // mAge = json value with key "age"
    
    @GsonAdapterKey("points")
    public int mPoints;     // mPoints = json value with key "points"
}

public class Herd {
    @GsonAdapterKey
    public ArrayList<Deer> data_list;  // data_list = json value with key "data_list"
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

## Future Enhancements

- Generate code so that member variables only need to be package local

## License
`stag-java` is available under the MIT license. See the [LICENSE](LICENSE) file for more information.

## Questions
Post on [Stack Overflow](http://stackoverflow.com/questions/tagged/vimeo-android) with the tag `vimeo-android`.
