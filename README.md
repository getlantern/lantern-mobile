Lantern Android
================================================================================

Overview
--------------------------------------------------------------------------------

<img src="screenshots/screenshot1.png" height="330px" width="200px">

Lantern Android is an app that uses the Android VpnService API to route all device traffic through a packet interception service and subsequently the Lantern circumvention tool.

## Building Lantern Android

### Building from Android Studio

#### Prerequisites

* [Android Studio][2]
* git

Download the most recent copy of the Lantern Android source code using `git`:

```
mkdir -p ~/AndroidstudioProjects
cd ~/AndroidstudioProjects
git clone https://github.com/getlantern/lantern-mobile.git
```

In the welcome screen choose the "Open an existing Android Studio" option and
select the `lantern` folder you just checked out with git.
 
### Building from the Command Line (beta, for development only)

#### Prerequisites

* Java Development Kit 1.7
* Git

#### Building, installing and running

Build the Debug target:

```
make build-debug
```

Install it:

```
make install
```

Run the app on the device from the command line:

```
make run
```

By default, all three tasks will be run in order with:

```
make
```
 
 
 
