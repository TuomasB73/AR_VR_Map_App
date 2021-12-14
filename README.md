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

The app can be just cloned and installed using Android studio.

## Screenshot