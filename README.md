# ARea Explorer

This is the Android side of the app ARea Explorer. The application also
contains [Backend](https://github.com/Lauri92/arvr-backend)
and [Web-Frontend](https://github.com/arnaud18o5/AReaExplorerWebApp). Be sure to check out those
repositories to get a better idea how this application functions as a whole. This README will only
cover areas related to the Android application.

## Features

The application allows the scanning of QR-codes in order to then display 3D map of a specific
location in Augmented Reality. That map could represent for example:

* A floor at a campus
* Map of an airport ( or terminal)
* Museum
* A bigger map like a center of some city

The maps allow several different kind of interactions with them such as:

* Displaying various points of interest which vary depending on the map:
    * For an airport they could indicate the location of a specific gate or a shop, cafeteria..
    * For a floor at a campus they could represent something else like a classroom or elevators,
      stairs..
    * For a city center it could include sights or hotels, parks...
* Scaling of those points of interest size in the map
    * A slider to adjust the size to be fitting
* Adding and removing from favorites
    * Upon adding a map as favorite the user will be allowed to access the map later without needing
      to scan a QR-code again
* Motion gestures
    * Motion gestures allow to interact with the map without touching the screen at all using Linear
      Acceleration sensor
    * Supported gestures include tilting the phone to the left or the right
        * Map will be zoomed in or out
    * Shaking phone up and down
        * A dialog for removing all the points of interest will pop up
    * Motion gestures can be turned off in order to avoid unintended behavior from the user
* User location finding
    * Allows the users to find the approximate location of themselves on the map
    * Based on finding the closest point of interest of the user
    * Not as reliable indoors as the location service is not as accurate if used inside of a
      building

List of saved maps

* Allows to see all favorited maps
* Short description
* Show the physical location in a standard 2D map of what the 3D map represents in AR mode

User registration and logging is required

* The user credentials and favorited maps are saved in Azure

## Installation

The app can be just cloned and installed using Android studio. The app requires Android 7.0 or newer version.

## Instructions

1. Login and registration

   <img src="https://user-images.githubusercontent.com/64253189/146197114-63e1a4d1-7b50-4376-b655-c4d25053e9c8.jpg" width="200" />
   <img src="https://user-images.githubusercontent.com/64253189/146197216-5c2c8202-a801-4ba8-84dd-0172a3b65efd.jpg" width="200" />
   
   Login or register in the app first. See the credential requirements by tapping the info button.


2. Home screen

   <img src="https://user-images.githubusercontent.com/64253189/146197571-31fc3393-f897-4316-9ca6-2dcf5a4c5e68.jpg" width="200" />
   
   This is the home screen. There are two main features, QR code scanning and saved maps screen.


3. QR code scanning

<img src="https://user-images.githubusercontent.com/64253189/146197999-88fa4423-de05-40a9-8467-9bf4e39eec3d.jpg" width="200" />
On the home screen press the "Read QR code" button, point the camera at a QR code that references an AR map and then press "Open in AR" button in the dialog that appears.

4. Finding a plane/level in the AR mode and placing the 3D map

<img src="https://user-images.githubusercontent.com/64253189/146198760-6dc229ab-0b9b-466b-ad03-a32852cc568b.jpg" width="200" />
Find a plane/level in the AR mode by moving the phone and pointing the camera at different surfaces. When a grid of white dots appears, a plane/level is recognized. Now press the "Place 3D map" button and the map will be loaded and displayed in a moment.

5. Add points of interest on the map

<img src="https://user-images.githubusercontent.com/64253189/146200188-a23f145b-d74d-4d65-b48f-4062a68f17d6.jpg" width="200" />
Swipe from the left edge of the screen to open the points of interest menu. Here you can see all the points of interest for the map. Tap on the items that you want to see on the map and then close the menu. Now you can see those points of interest on the map with a text and/or logo. Tap them to see details about them.

6. Interaction and features

<img src="https://user-images.githubusercontent.com/64253189/146201445-624105bd-bb41-4db9-8f31-02fcc1c25632.jpg" width="200" />
On the top of the screen there is a row of buttons for different features. Press the location icon to see your own approximate location on the map. Press the arrow icon to resize the points of interest. You can move and zoom the map simply with the touchscreen, but there are also motion gesture interactions available for zooming the map and removing all points of interest from the map. See instructions for the motion gestures by tapping the info button, and disable or enable the gesture controls with the switch on the top corner of the screen. You can also move yourself around the model and view it closer or further away. Lastly, you can press the heart icon to save the map for yourself on the app.

7. Saved maps screen and map location

<img src="https://user-images.githubusercontent.com/64253189/146203098-54aafcd0-d17e-4f1c-8bee-c93da3fef76e.jpg" width="200" />
On the home screen press the "My saved maps" button to see all your saved maps. You can search from them by the name and tap on them to see details. Press the "Show location on map" button on the details dialog to see the real location of the 3D map model on a regular map view. You can also open the AR mode from the details dialog as well as deleting it from your saved maps.
