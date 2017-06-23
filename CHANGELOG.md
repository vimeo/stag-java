Change Log
==========

Version 2.3.0 *(2017-06-22)*
----------------------------
- Stag now throws an exception if you try to reuse a `Stag.Factory` instance between multiple gson instances.

Version 2.2.0 *(2017-05-30)*
----------------------------
- Added support for private member variables in Java (leveraging getters/setters).
- Added support for models written in Kotlin.

Version 2.1.4 *(2017-05-10)*
----------------------------
- Added compiler support for all Java language versions.

Version 2.1.3 *(2017-05-01)*
----------------------------
- Improved performance of generic type adapter instantiation.
- Fixed bug where the type adapter for a parameterized type containing a parameterized type (e.g. `Map<T, List<T>>`) wasn't being generated.

Version 2.1.2 *(2017-04-12)*
----------------------------
- Fixed a bug where `Object` fields caused type adapter generation to fail.

Version 2.1.1 *(2017-04-10)*
----------------------------
- Fixed bug where enums could not have private and/or final fields.

Version 2.1.0 *(2017-04-07)*
----------------------------
- WARNING: Removed deprecated `@GsonAdapterKey` annotation.
- Added support for the `@JsonAdapter` Gson annotation.
- Fixed bad behavior where unannotated classes could be picked up by Stag if they were nested within annotated classes. All classes that need to use Stag should now explicitly specify the option.
- Added unit tests for a variety of use cases.

Version 2.0.2 *(2017-03-02)*
----------------------------
- Interfaces are now ignored by stag instead of throwing an error if they are annotated.
- Fixed bug where type adapters for self referential classes (see `sample/ExternalModelExample2`) would cause stack overflows when instantiated.
- Fixed bug where duplicate type adapters would be generated if a class in one module extended a class in another module.
- Fixed bug where the type adapter for `char` arrays was broken.
- Correctly suppress raw types in generated code so as to not interfere with compilation settings on projects.
- Added unit tests for all model classes to validate the creation of type adapters and their ability to be instantiated.
- Added unit tests to validate type adapters in `KnownTypeAdapters`.

Version 2.0.1 *(2017-01-31)*
----------------------------
- Fixed backwards compatibility bug where Stag wasn't generating TypeAdapters for classes containing only `@GsonAdapterKey`. (Please note that it's still advised to move away from `@GsonAdapterKey` as soon as possible).
- Fixed bug where inner classes of classes annotated with `@UseStag` were always using `FieldOption.ALL` instead of inheriting the enclosing class's field option.
- Fixed bug where Stag was not generating TypeAdapters for inner classes annotated with `@UseStag` nested within classes that aren't annotated.
- Added annotation processor option to enable debug output by Stag when compiling.

Version 2.0.0 *(2017-01-26)*
----------------------------
- Added `@UseStag` class level annotation
    - Use at the class level to mark a class to be processed by Stag
    - The goal of `@UseStag` is that it should be the only change you need to make to switch from your old non-Stag ready model to a better performing one. One annotation per model class, instead of the old Stag (different annotation for every field).
    - `@UseStag` takes `FieldOption` enum which specifies what fields Stag should pick up
        - `FieldOption.ALL`: marks **all** fields in a class for adapter generation. This is the default setting used by Stag if you don't specify any field option. This is the option you should choose in most scenarios. Unless marked with a `@SerializedName` annotation, the variable name will be used as the JSON key. There is no longer a reason to mark fields with `@GsonAdapterKey` to have them be picked up by Stag.
        - `FieldOption.NONE`: marks the class for adapter generation, but none of the class's immediate fields will be picked up by Stag. Only fields in the inheritance hierarchy will be picked up. See `VideoList.java` for an example of a class that has no immediate fields that need parsing, but has inherited fields.
        - `FieldOption.SERIALIZED_NAME`: This option only chooses fields marked with the `@SerializedName` or `@GsonAdapterKey` to be used by Stag. This will behave the same way as the previous versions of stag behaved.
- Added support for `@SerializedName` in favor of `@GsonAdapterKey`
- Deprecated `@GsonAdapterKey`. **Note: this annotation will be removed in v2.1.0, so you are advised to use this release to move over completely to** `@UseStag`. `@GsonAdapterKey` will currently take precedence over `@SerializedName` on fields annotated with both for backwards compatibility reasons.
- Created `KnownTypeAdapters` and moved it to `stag-library` module so that it doesn't have to be generated at compile time. As a result, the `stag-library` module now has a hard dependency on `gson:2.8.0`. Previously, only the `stag-library-compiler` module depended on Gson, so it wouldn't interfere with the version you use locally. However, now `gson:2.8.0` is required, unless you exclude it from the dependency.
- Performance was improved, as much reflection as possible was removed.
- Native support for enums, maps, and all primitive types supported by Gson.
- Fields marked with the `transient` keyword will be excluded from parsing
- Parsing errors on primitive types are no longer swallowed but are bubbled up.
- Various bugfixes and edge cases handled
- Please refer to the sample app and sample model in order to see all sorts of proper usage of the library.

Version 1.2.1 *(2016-12-22)*
----------------------------
- Suppress unchecked assignment warnings in generated code, preventing build failure if unchecked warnings are set to error

Version 1.2.0 *(2016-12-21)*
----------------------------
- You can now specify the package name for generated Stag files (multiple module support)
- Reduced number of generated methods by getting rid of individual adapter factories
- Support for NonNull annotations on fields
  - `@android.support.annotation.NonNull`
  - `@org.jetbrains.annotations.NotNull`
  - `@javax.annotation.Nonnull`
  - `@javax.validation.constraints.NotNull`
  - `@edu.umd.cs.findbugs.annotations.NonNull`
  - `@lombok.NonNull`
  - `@org.eclipse.jdt.annotation.NonNull`
- Added adapter caching for improved performance
- No longer generate adapters for abstract classes
- Fixed some problems with nested classes
- Support `Collection` interface
- Better performance for `List`, `Map`, and `HashMap` types
- Support for Enums
- Support for native arrays
- Updated internal gson version to 2.8.0
- Added more examples to the sample app

Version 1.1.2 *(2016-11-18)*
----------------------------
- Added support for `float` primitive type
- Support for all Map and List types
- Fixed bug caused by an static inner model class being named the same as a model class in the same package
- Better documented sample app

Version 1.1.1 *(2016-11-07)*
----------------------------
- Fixed a bug with the `TypeAdapterGenerator` in which it incorrectly created duplicate TypeAdapter fields if there were non-parameterized and parameterized (e.g. `String` and `ArrayList<String>`) fields of the same type in a model class.
- Added a try/catch around `ArrayList` parsing in `TypeAdapter.read` methods for consistency in throwing parsing errors.

Version 1.1.0 *(2016-11-02)*
----------------------------
- Stag can now be used across multiple modules in the same application
- Class member variables no longer need to be public visibility, but can now be package-local or protected
- Generated factories and adapters are now located within the same package as your model class and are named so as to not interfere with your model class namespace: `Model$TypeAdapter` and `Model$TypeAdapterFactory`

Version 1.0.1 *(2016-09-01)*
----------------------------
- Improvement: Exceptions thrown in the parsing code are now bubbled up instead of a generic exception being thrown
- Technical: META-INF is now generated by Google auto service

Version 1.0.0 *(2016-08-11)*
----------------------------
- Initial release
