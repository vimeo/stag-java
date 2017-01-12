# Stag

Stag improves Gson performance by automatically generating reflection-less TypeAdapters for your model objects.

| Branch | Build Status |
|--------|--------------|
| master | [![Build Status](https://circleci.com/gh/vimeo/stag-java/tree/master.svg?style=shield&circle-token=4d5dd11678a93587658d1677d0ef2b8c64b56574)](https://circleci.com/gh/vimeo/stag-java/tree/master) |
| dev    | [![Build Status](https://circleci.com/gh/vimeo/stag-java/tree/dev.svg?style=shield&circle-token=4d5dd11678a93587658d1677d0ef2b8c64b56574)](https://circleci.com/gh/vimeo/stag-java/tree/dev) |


## Why Build Stag ?

Gson is the essential JSON parsing library. It greatly simplifies what can be the verbose and boilerplate-ridden process of parsing JSON into model objects. It does this by leveraging reflection. Unfortunately, using reflection can be slow (particularly on the Android OS).

You can work around this by writing a custom `TypeAdapter`, a class that Gson uses to (de)serialize an object. The main use case for custom type adapters is for classes that you don't have control over (e.g. parsing a JSON string into a `java.util.Date` object). They are used to manually map JSON to fields in your model object. So, you can just write a custom TypeAdapter to tell Gson how to map data to fields and the performance will improve, since it won't have to use reflection.

But... if you have a lot of model objects, and you want to remove the use of reflection for (de)serialization of each one, suddenly you have to write many, many TypeAdapters. If you've ever written one or many of these type adapters, you will know that it is a tedious process. In fact, when writing your own `TypeAdapter`, you might ask what you are doing using Gson in the first place!!! 

The Stag library solves this problem. It leverages annotations to automatically generate reflection-less TypeAdapters for your model objects at compile time. Instead of spending time writing your own custom TypeAdapters for each model object, or forgoing the performance gain of eliminating reflection, using Stag and the `@GsonAdapterKey` annotation on your model fields will do all the work for you.

## Gradle Usages

#### 1. Add the Stag dependencies

from jCenter
```groovy
dependencies {
    compile 'com.vimeo.stag:stag-library:1.2.1'
    apt 'com.vimeo.stag:stag-library-compiler:1.2.1'
}
```

or as a submodule
```groovy
dependencies {
    compile project(':stag-library')
    apt project(':stag-library-compiler')
}
```

#### 2. Add the Annotation Processor Plugin

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

#### 3. Pass package name as an argument for the generated files (Optional)
By default, the files will be in generated in `com.vimeo.sample.stag.generated` package. But, you can specify your own package for the generated files by passing it as an argument to the apt compiler.
```groovy
apt {
    arguments {
        stagGeneratedPackageName "com.vimeo.sample.stag.generated"
    }
}
```

## Features

#### 1. Class Level Annotation

Stag supports class level annotation `@UseStag` which processes all the fields for a particular class, which makes it easy to use and integrate.

`@UseStag` has three different variants -

    * @UseStag(UseStag.FIELD_OPTION_ALL) : Will serialize/de-serialize all member variables which are not static or transient

    * @UseStag(UseStag.FIELD_OPTION_NONE) : Will skip serialization and deserialization for all member variables

    * @UseStag(UseStag.FIELD_OPTION_SERIALIZED_NAME) : Will Serialize or Deserialize Fields only which are annotated with SerializedName or GsonAdapterKey(deprecated)

#### 2. @SerializedName("") Support

Similar to GSON, you can use @SerializedName annotation to provide a different JSON name to a member field. It also supports alternate name feature of the @SerializedName

    @SerializedName("name') or @SerializedName(value = "name", alternate = {"name1", "name2"})

#### 3. Cross Module Support

Stag has the ability to cross reference TypeAdapters accross modules.

#### 4. In parity with GSON

Last but not the least, Stag is almost in parity with GSON.

## Stag Rules

1. Make sure the member variables of your model class are not private (should be public, protected, or package-local visibility)
2. Make sure your model class is not private and has a zero argument non-private constructor
3. Annotate the classes with `@UseStag` annotation. This will process all the member variables of the class, which makes it easy to use.
4. Use the @SerializedName("") annotation to give the variables a different JSON name. (similiar to GSON)
5. Use your favorite `@NonNull` annotation to tell Stag to throw an exception if the field is null while deserializing or while serializing the object.
6. Register the `Stag.Factory` with Gson when you create your Gson instance: `Gson gson = new GsonBuilder().registerTypeAdapterFactory(new Stag.Factory()).create();`
7. You're done!

<b>NOTE</b> : @GsonAdapterKey has been deprecated and will be removed in future releases. It is advisable to migrate to @SerializedName and @UseStag annotations.   

See the [example below](#example) or the [sample app](sample) to get more info on how to use Stag.

## Example

```java
@UseStag
public class Deer {
    @SerializedName("name")
    String mName;    // mName = json value with key "name"
    
    @SerializedName("species")
    String mSpecies; // mSpecies = json value with key "species"
    
    @SerializedName("age")
    int mAge;        // mAge = json value with key "age"
    
    @SerializedName("points")
    int mPoints;     // mPoints = json value with key "points"
    
    @SerializedName("weight")
    float mWeight;     // mWeight = json value with key "weight"
}

@UseStag
public class Herd {

    @NonNull                    // add NonNull annotation to throw an exception if the field is null
    @SerializedName("data_list")
    ArrayList<Deer> data_list;  // data_list = json value with key "data_list"
    
    @SerializedName("data_list_copy")
    List<Deer> data_list_copy;  // data_list_copy = json value with key "data_list_copy"
    
    @SerializedName("data_map")
    Map<String, Deer> data_map;  // data_map = json value with key "data_map"
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

- Add an option to absorb parsing errors rather than crashing and halting parsing (default gson behavior)

## License
`stag-java` is available under the MIT license. See the [LICENSE](LICENSE) file for more information.

## Questions
Post on [Stack Overflow](http://stackoverflow.com/questions/tagged/vimeo-android) with the tag `vimeo-android`.
