# **How to build application**

# Project Build Documentation

## 1. Required Tools
To build the project, you will need to have **Android Studio** version 2024.1.1 or higher. The embedded version of **Java** should be version **17** or higher.

### Steps:
1. Install **Android Studio**. You can download the latest version from the [official Android Studio website](https://developer.android.com/studio).
2. Ensure that **Java 17** is used in Android Studio. You can verify this in the studio settings under **File → Project Structure → SDK Location**.

## 2. Project Structure and Dependencies
The project includes two key dependencies that need to be integrated for correct functionality:

1. **blockchain-sdk** — a library for interacting with a blockchain system.
2. **tangem-sdk** — SDK for working with Tangem cards and interacting with their API.

Both dependencies are pulled from **GitHub Package Registry**.

### Steps to Set Up Dependencies:
1. To download dependencies from GitHub Registry, you need to generate a **personal access token** on GitHub:
    - Go to [GitHub Settings → Developer settings → Personal access tokens](https://github.com/settings/tokens).
    - Create a new token with the required access (e.g., `read:packages` to read from the registry).

2. After that, you need to add the token to the `local.properties` file of your project for authentication:
    - Open the `local.properties` file at the root of your project (create it if it doesn't exist).
    - Add the following lines:
      ```properties
      gpr.user=YOUR_GITHUB_USERNAME
      gpr.key=YOUR_GENERATED_TOKEN
      ```
    - **YOUR_GITHUB_USERNAME** — your GitHub username.
    - **YOUR_GENERATED_TOKEN** — the token you generated in the previous step.

3. Once this is done, the dependencies will be automatically downloaded during the build process via Gradle.

## 3. Set Up `google-services.json`
You need to add the **`google-services.json`** file to the `/app` folder of your project for Firebase services and Google APIs to work properly.

### Steps:
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project or select an existing one.
3. **Use the package name `com.tangem.wallet.debug`** when setting up your Firebase project for Android.
4. Download the **`google-services.json`** file:
    - Add an Android app to the Firebase project.
    - Download the **`google-services.json`** file.

5. Place the **`google-services.json`** file inside the `/app` folder of your project.

## 4. Create Configuration Folder
Create the following folder structure in your project.

Inside this folder, place the **`config_dev.json`** file.

### Steps:
1. Create the `tangem-app-config` folder under `app/src/main/assets`.
2. Place the **`config_dev.json`** file into the `tangem-app-config` folder.

## 5. Sync Gradle and Build the Project
Once all the previous steps are completed, you need to sync your Gradle files and run the build.

### Steps:
1. In Android Studio, go to the **File** menu and select **Sync Project with Gradle Files**.
2. Once the sync is complete, you can build the project by selecting **Build → Make Project** or pressing **Ctrl+F9** (Windows/Linux) or **Cmd+F9** (macOS).
3. Ensure there are no errors in the **Build Output** and that the project is successfully built.
