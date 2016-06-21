# Stag
####Using Gson Without Reflection####

### Gradle usage
```groovy
compile project(':stag:library')
apt project(':stag:library-compiler')
```

Additionally, if you are using this on an Android project, you will need to use the android apt plugin (`com.neenbedankt.gradle.plugins:android-apt:1.8`). You must apply the plugin (`apply plugin: 'com.neenbedankt.android-apt'`) on the `build.gradle` file of the module you wish to use this on.

### Why

Gson is the essential JSON parsing library. It's widely used and greatly simplifies what can be the verbose and boilerplate-ridden process of parsing JSON into your model objects. It accomplishes this simplicity and unobtrusiveness through use of reflection. Unfortunately, using reflection is not fast on all systems (particulary on the Android OS). Gson also includes the concept of creating your own custom `TypeAdapter` that tells Gson how to (de)serialize an object. The main use case is for classes that you don't have control over (e.g. parsing a string passed in JSON into a `Date` object), but if you really need to reduce the (de)serialization time of Gson in a performance critical part of your code, you can create a TypeAdapter that doesn't use reflection.

But... if you have a lot of model objects, and you want to remove the use of reflection for (de)serialization of your model objects, suddenly you have to write many, many TypeAdapters. If you've ever written one or many of these TypeAdapters, you will know that it is a tedious process. In fact, if you end up writing your own you might ask what you are doing using Gson in the first place!!!

The Stag library solves this problem. It generates TypeAdapters for your model objects that don't use reflection. Instead of writing your own custom TypeAdapters for each model object, or forgoing the performance gain of eliminating reflection, using Stag and the `@GsonAdapterKey` annotation on your model fields will do all the work for you.

### How to use

- You do not need to use `@SerializedName("json_key)`.
- Your model class must have a zero argument constructor.
- The member variables of your model class need to have `public` visibility (for now).
- The member variables of your model class that you wish to be filled must be annotated with `@GsonAdapterKey`.
    - If you want to use the variable name as the JSON key, just use `@GsonAdapterKey`.
    - If you want to use a different name as the JSON key, use `@GsonAdapterKey("json_key")`.

### Example

```java
public class Deer {
    @GsonAdapterKey("name")
    public String mName;
    
    @GsonAdapterKey("species")
    public String mSpecies;
    
    @GsonAdapterKey("age")
    public int mAge;
}

public class Stag extends Deer {
    @GsonAdapterKey("points")
    public int mPoints;
}

MyRandomClass {
    private Gson gson = new GsonBuilder()
                                 .registerTypeAdapterFactory(new AdapterFactory.Factory())
                                 .create();

    public Stag fromJson(String json) {
        return gson.fromJson(json, Stag.class);
    }
}

```

### TO-DO

- need to add support for `ArrayList<?>`
- generate code in such a way that member variables only need to be package local
- generate `TypeAdapterFactory` so that consumer of the library does not need to register each adapter
