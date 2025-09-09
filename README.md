# WearOS Sensor Collector
A wearable interface client designed to collect and stream physiological data provided by the heart rate sensor and IMU sensors (accelerometer, gyroscope), made for an Android WearOS smartwatch. The data is transfered via websockets to an external server. Project completed as a student under NIST's Summer High School Intern Program.

## Installation 
1. Download Android Studio (https://developer.android.com/studio)
2. Install Android Studio
   a. Double click android-studio-xxxx-windows.exe to install and setup android studio
   b. Choose Componets => check Android Virtual Device

3. Clone the project
   a. Run Android Studio for first time
   b. Click Clone Respository for Projects
   c. Open a brower and copy and paste the following URL
      https://github.com/angl-angl/WearOS_SensorCollector
   d. From github.com page, click green button 'Code'
   e. Click the icon -Copy url to clipboard from HTTPS tab
   f. Paste the url to URL textbox, make sure choose Git for Version control, then click Clone button
   g. Wait for Android Stuido finish loading the project
4. Create Virtual Devices
   a. After opening a project, select View > Tool Windows > Device Manager from the main menu bar, then click the +, and then click Create Virtual Device.
   b. Select 'Wear OS' in Form Factor in Add Device windows
   c. Click 'Wear OS large Round', click Next, then Finish
5. Run the project 
   a. In the target device menu, make sure the device-'Wear OS Large Round'(default) selected
   b. Click Run button to run Android Emulator 
