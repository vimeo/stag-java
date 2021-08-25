⚠️⚠️⚠️ This library has been deprecated and will be removed in the future. ⚠️⚠️⚠️

# Stag

Stag improves Gson performance by automatically generating reflection-less TypeAdapters for your model objects.

[![Build Status](https://travis-ci.org/vimeo/stag-java.svg?branch=develop)](https://travis-ci.org/vimeo/stag-java) [![codecov](https://codecov.io/gh/vimeo/stag-java/branch/develop/graph/badge.svg)](https://codecov.io/gh/vimeo/stag-java) [![Download](https://api.bintray.com/packages/vimeo/maven/stag-library/images/download.svg)](https://bintray.com/vimeo/maven/stag-library/_latestVersion)


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
        maven { url 'https://plugins.gradle.org/m2/' }
    }
    dependencies {
        classpath 'net.ltgt.gradle:gradle-apt-plugin:0.11'
    }
}

apply plugin: 'net.ltgt.apt'

dependencies {
    def stagVersion = '2.6.0'
    compile "com.vimeo.stag:stag-library:$stagVersion"
    apt "com.vimeo.stag:stag-library-compiler:$stagVersion"
}

// Optional annotation processor arguments (see below)
gradle.projectsEvaluated {
    tasks.withType(JavaCompile) {
        aptOptions.processorArgs = [
                "stagAssumeHungarianNotation": "true",
                "stagGeneratedPackageName"   : "com.vimeo.sample.stag.generated",
                "stagDebug "                 : "true",
                "stag.serializeNulls"        : "true",
        ]
    }
}
```

### Kotlin Gradle
```groovy
apply plugin: 'kotlin-kapt'

dependencies {
    def stagVersion = '2.6.0'
    compile "com.vimeo.stag:stag-library:$stagVersion"
    kapt "com.vimeo.stag:stag-library-compiler:$stagVersion"
}

kapt {
    correctErrorTypes = true
    // Optional annotation processor arguments (see below)
    arguments {
        arg("stagDebug", "true")
        arg("stagGeneratedPackageName", "com.vimeo.sample.stag.generated")
        arg("stagAssumeHungarianNotation", "true")
        arg("stag.serializeNulls", "true")
    }
}
```

### Android Gradle (Java)

```groovy
dependencies {
    def stagVersion = '2.6.0'
    compile "com.vimeo.stag:stag-library:$stagVersion"
    annotationProcessor "com.vimeo.stag:stag-library-compiler:$stagVersion"
}

android {
    ...
    defaultConfig {
        ...
        // Optional annotation processor arguments (see below)
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    "stagAssumeHungarianNotation": 'true',
                    "stagGeneratedPackageName"   : 'com.vimeo.sample.stag.generated',
                    "stagDebug"                  : 'true',
                    "stag.serializeNulls"        : 'true'
                ]
            }
        }
    }
}
```

#### 2. Provide optional compiler arguments to Stag
 - `stagGeneratedPackageName`: Pass package name as an argument for the generated files. By default, the files will be in generated
 in `com.vimeo.stag.generated` package. You can specify your own package for the generated files
 by passing it as an argument to the annotation processor.
 - `stagDebug`: Turn on debugging in Stag. This will cause Stag to spit out a lot of output into the gradle console.
 This can aid you in figuring out what class is giving you trouble, if the exception gradle prints out
 isn't sufficient. Default is false.
 - `stagAssumeHungarianNotation`: If your Java member variables are private and Stag needs to use setters and getters to access the field,
 Stag will look for members named `set[variable_name]` and `get[variable_name]`. If your member variables are named using Hungarian notation,
 then you will need to pass true to this parameter so that for a field named `mField`, Stag will look for `setField` and `getField` instead
 of `setMField` and `getMField`. Default is false.
 - `stag.serializeNulls`: By default this is set to false. If an object has a null field and you send it to be serialized by Gson, it is optional
 whether or not that field is serialized into the JSON. If this field is set to `false` null fields will not be serialized, and if set to `true`, 
 they will be serialized. Prior to stag version 2.6.0, null fields were always serialized to JSON. This should not affect most models. However, if
 you have a model that has a nullable field that also has a non null default value, then it might be a good idea to turn this option on.

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

1. Make sure that any private member variables have setters/getters following these naming rules:
    ```java
    private String myString;

    public String getMyString() { ... }

    public void setMyString(String parameter) { ... }
    ```
    Java setters and getters must have `protected`, `public`, or package local visibility. If you don't want to use setters and getters, make sure your member variables have `protected`, `public`, or package local visibility.
    If working with Kotlin, currently, you must make sure your getters all have `public` visibility. Because stag generates Java code, the only way it knows how to access the Kotlin fields is if the setters and getters are public. By default, the visibility set on a Kotlin member variable is also applied to its setters and getters.
2. Make sure your model class is not private and has a zero argument non-private constructor
3. Annotate the classes with `@UseStag` annotation. This will process all the member variables of the class, which makes it easy to use.
4. Use the `@SerializedName("key")` annotation to give the variables a different JSON name. (same as GSON)
5. Use your favorite `@NonNull` annotation to tell Stag to throw an exception if the field is null while deserializing or while serializing the object.
6. Register the `Stag.Factory` with Gson when you create your Gson instance: `Gson gson = new GsonBuilder().registerTypeAdapterFactory(new Stag.Factory()).create();`
7. Make sure that you are not reusing the `Stag.Factory` instance between Gson instances. The factory is stateful and must be recreated when creating a new Gson instance. If you try to reuse the instance, an `UnsupportedOperationException` will be thrown.
8. You're done!
9. [Optional] By default, stag will drop a file called `StagTypeAdapterFactory.list` into your build folder which contains the plaintext names of all your models. It is used by the compiler to generate the adapters. It's a very small file and will compress down to a few bytes in size, but if you don't want it in your compiled apk, you can exclude it using the following code (if you supply a custom package name as a compiler argument, use that in place of `com/vimeo/stag/generated/` below):
```groovy
packagingOptions {
    exclude 'com/vimeo/stag/generated/StagTypeAdapterFactory.list'
}
```

See the [example below](#example) or the [sample app](sample) to get more info on how to use Stag.

## Example

#### Java
```java
@UseStag
public class Deer {

    // Private fields require getters and setters
    @SerializedName("name")
    private String name;    // name = json value with key "name"
    
    @SerializedName("species")
    String species; // species = json value with key "species"
    
    @SerializedName("age")
    int age;        // age = json value with key "age"
    
    @SerializedName("points")
    int points;     // points = json value with key "points"
    
    @SerializedName("weight")
    float weight;   // weight = json value with key "weight"
    
    public String getName() { return name; }
    
    public void setName(String name) { this.name = name; }
}

@UseStag
public class Herd {

    @NonNull                     // add NonNull annotation to throw an exception if the field is null
    @SerializedName("data_list")
    ArrayList<Deer> data;        // data = json value with key "data_list"
    
    List<Deer> data_list_copy;   // data_list_copy = json value with key "data_list_copy"
    
    Map<String, Deer> data_map;  // data_map = json value with key "data_map"
}
```

#### Kotlin
```kotlin
@UseStag
class Deer {

    @SerializedName("name")
    var name: String? = null    // name = json value with key "name"

    @SerializedName("species")
    var species: String? = null // species = json value with key "species"

    @SerializedName("age")
    var age: Int = 0        // age = json value with key "age"

    @SerializedName("points")
    var points: Int = 0     // points = json value with key "points"

    @SerializedName("weight")
    var weight: Float = 0.toFloat()   // weight = json value with key "weight"
}

@UseStag
class Herd {

    // non null fields will be honored buy throwing an exception if the field is null
    @SerializedName("data_list")
    var data: ArrayList<Deer> = ArrayList()     // data = json value with key "data_list"

    var data_list_copy: List<Deer>? = null   // data_list_copy = json value with key "data_list_copy"

    var data_map: Map<String, Deer>? = null  // data_map = json value with key "data_map"
}
```

#### Consuming Model in Java
```java
/**
 * The class where you receive JSON 
 * containing a list of Deer objects.
 * You parse the list from JSON using
 * Gson.
 */
class MyParsingClass {
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
- Support `internal` visibility in Kotlin code
- Generate Kotlin code for Kotlin models

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
