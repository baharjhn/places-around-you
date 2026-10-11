# Posts & Comments: Android App with Offline Caching

An Android app (Java) that loads posts and comments from a REST API and caches comments in a local **SQLite** database, so they can still be read without an internet connection. Built as a course project in 2019.

## Features

- **REST API client:** fetches posts and comments as JSON from the public [JSONPlaceholder](https://jsonplaceholder.typicode.com) API using **Volley**.
- **List and grid views:** one button switches the posts screen between a `ListView` and a `GridView` (`ViewSwitcher`).
- **Comments screen:** tap a post to open its comments.
- **Offline caching with SQLite:** comments are saved locally the first time they are downloaded. When a post is opened again, the app decides where to load from:

| Situation | Source |
|---|---|
| Comments not cached yet | Server (then saved to SQLite) |
| Cached and the device is offline | SQLite |
| Cached, online, and more than 10 s since the last server request | Server |
| Cached, online, requested again within 10 s | SQLite |

- **Team dialog:** shows the names of the project members.

## Tech stack

Java · Android SDK 28 (min SDK 22) · Volley · SQLite · Android Support Library · Gradle

## Project structure

```
app/src/main/java/com/example/myapplication/
├── MainActivity.java      # Loads posts, list/grid switch, chooses server vs. cache for comments
├── commentActivity.java   # Shows comments from the server or from SQLite and caches new ones
├── SubApp.java            # Application class: creates the SQLite comment table
├── post.java, comment.java # Data classes
└── dialog.java            # Team members dialog
```

## Running it

1. Open the project in Android Studio.
2. Let Gradle sync, then run the `app` configuration on an emulator or device with internet access.
3. To see the cache in action, open a post's comments, turn on airplane mode and open the same post again.

Note: the comment cache is reset each time the app starts.

## Team

Bahar Jahani, Amirreza Kazemi and Preni Golazizian.

## Changes before publishing

- Fixed a crash on first launch: the app dropped the comment table before it existed.
- Switched SQL queries to parameterized queries, so comments with quotes or apostrophes are cached correctly.
- Removed Android Studio project files from version control.
