# Stag

Stag improves Gson performance by automatically generating reflection-less TypeAdapters for your model objects.

| Branch | Build Status |
|--------|--------------|
| master | [![Build Status](https://travis-ci.org/vimeo/stag-java.svg?branch=master)](https://travis-ci.org/vimeo/stag-java) |
| dev    | [![Build Status](https://travis-ci.org/vimeo/stag-java.svg?branch=dev)](https://travis-ci.org/vimeo/stag-java) |

| Artifact | Latest Version |
|----------|----------------|
| stag-library | [![Download](https://api.bintray.com/packages/vimeo/maven/stag-library/images/download.svg)](https://bintray.com/vimeo/maven/stag-library/_latestVersion) |
| stag-library-compiler | [![Download](https://api.bintray.com/packages/vimeo/maven/stag-library-compiler/images/download.svg)](https://bintray.com/vimeo/maven/stag-library-compiler/_latestVersion) |

## Why Build Stag?

Gson is the essential JSON parsing library. It greatly simplifies what can be the verbose and boilerplate-ridden process of parsing JSON into model objects. It does this by leveraging reflection. Unfortunately, using reflection can be slow (particularly on the Android OS).

You can work around this by writing a custom `TypeAdapter`, a class that Gson uses to (de)serialize an object. The main use case for custom type adapters is for classes that you don't have control over (e.g. parsing a JSON string into a `java.util.Date` object). They are used to manually map JSON to fields in your model object. So, you can just write a custom TypeAdapter to tell Gson how to map data to fields and the performance will improve, since it won't have to use reflection.

But... if you have a lot of model objects, and you want to remove the use of reflection for (de)serialization of each one, suddenly you have to write many, many TypeAdapters. If you've ever written one or many of these type adapters, you will know that it is a tedious process. In fact, when writing your own `TypeAdapter`, you might ask what you are doing using Gson in the first place!!! 

The Stag library solves this problem. It leverages annotations to automatically generate reflection-less TypeAdapters for your model objects at compile time. Instead of spending time writing your own custom TypeAdapters for each model object, or forgoing the performance gain of eliminating reflection, use Stag and apply the `@UseStag` to your model class declarations and all the work will be done for you.

## Gradle Usages

#### 1. Add the Stag dependencies

All jar dependencies are available on jcenter.

### Java Gradle

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

dependencies {
    compile 'com.vimeo.stag:stag-library:2.1.3'
    apt 'com.vimeo.stag:stag-library-compiler:2.1.3'
}

// Optional annotation processor arguments (see below)
apt {
    arguments {
        stagGeneratedPackageName "com.vimeo.sample.stag.generated"
        stagDebug true
    }
}
```

### Android Gradle

```groovy
dependencies {
    compile 'com.vimeo.stag:stag-library:2.1.3'
    annotationProcessor 'com.vimeo.stag:stag-library-compiler:2.1.3'
}

android {
    ...
    defaultConfig {
        ...
        // Optional annotation processor arguments (see below)
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    stagGeneratedPackageName: 'com.vimeo.sample.stag.generated',
                    stagDebug               : 'true'
                ]
            }
        }
    }
}
```

#### 2. Provide optional compiler arguments to Stag
 - `stagGeneratedPackageName`: Pass package name as an argument for the generated files. By default, the files will be in generated
 in `com.vimeo.sample.stag.generated` package. But, you can specify your own package for the generated files
 by passing it as an argument to the apt compiler.
 - `stagDebug`: Turn on debugging in Stag. This will cause Stag to spit out a lot of output into the gradle console.
 This can aid you in figuring out what class is giving you trouble, if the exception gradle prints out
 isn't sufficient.

## Features

#### 1. Class Level Annotation

Stag supports class level annotation `@UseStag` which processes all the fields for a particular class, which makes it easy to use and integrate.

`@UseStag` has three different variants:

 - `@UseStag(FieldOption.ALL)`: Will serialize/de-serialize all member variables which are not static or transient
 - `@UseStag(FieldOption.NONE)`: Will skip serialization and deserialization for all member variables. Only member variables inherited from annotated classes will be included.
 - `@UseStag(FieldOption.SERIALIZED_NAME)`: Will Serialize or Deserialize Fields only which are annotated with `SerializedName`.

#### 2. `@SerializedName("key")` Support

Similar to GSON, you can use the`@SerializedName` annotation to provide a different JSON name to a member field. It also supports alternate name feature of the `@SerializedName` annotation.
 `@SerializedName("name")` or `@SerializedName(value = "name", alternate = {"name1", "name2"})`.

#### 3. Cross Module Support

Stag has the ability to reference TypeAdapters across modules.

#### 4. Parity with GSON

Last but not the least, Stag is almost in parity with GSON.

## Stag Rules

1. Make sure the member variables of your model class are not private (should be public, protected, or package-local visibility)
2. Make sure your model class is not private and has a zero argument non-private constructor
3. Annotate the classes with `@UseStag` annotation. This will process all the member variables of the class, which makes it easy to use.
4. Use the `@SerializedName("key")` annotation to give the variables a different JSON name. (same as GSON)
5. Use your favorite `@NonNull` annotation to tell Stag to throw an exception if the field is null while deserializing or while serializing the object.
6. Register the `Stag.Factory` with Gson when you create your Gson instance: `Gson gson = new GsonBuilder().registerTypeAdapterFactory(new Stag.Factory()).create();`
7. You're done!

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
    float mWeight;   // mWeight = json value with key "weight"
}

@UseStag
public class Herd {

    @NonNull                     // add NonNull annotation to throw an exception if the field is null
    @SerializedName("data_list")
    ArrayList<Deer> data;        // data = json value with key "data_list"
    
    List<Deer> data_list_copy;   // data_list_copy = json value with key "data_list_copy"
    
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

## Development
```sh
git clone git@github.com:vimeo/stag-java.git
cd stag-java
bundle install
# dev like a boss
bundle exec fastlane test
# commit and push like a boss
```

#### Manage build dependencies
Aside from specifying Java dependencies in the `.gradle` files, you can use the `.travis.yml` file to specify external build depencies such as the Android SDK to compile against (see the `android.components` section).


## License
`stag-java` is available under the MIT license. See the [LICENSE](LICENSE) file for more information.

## Questions
Post on [Stack Overflow](http://stackoverflow.com/questions/tagged/vimeo-android) with the tag `vimeo-android`.
