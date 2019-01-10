### EventDetectorService

A link to the source code can be found here:
[EventDetectorService](https://github.com/vnegreanu/EventDetectorService)

#### Description
An Android based service that connects via Bluetooth to a hardware device for detecting local hardware events
#### Files

- `BluetoothClient.java` : Main class that handles BT MAC address, interval and counters
- `BluetoothApplication.java` : Assures singleton of the BT Clients.
- `AlarmReceiver.java` : Handles the alarms set by the Main Activity
- `BootUpReceiver.java` : Handles behavior after boot 
- `PreferencesManager.java` : helper functions for persistance storage
- `MainActivity.java` : Main activity that implements all the logic.


#### Implementation details

- 2 BT clients are connected via SDP profile to a HW device linked with some pushed buttons.
- If one of the buttons gets hit the event is stored locally and then pushed into the `PreferenceManager` component for storage.
- An alarm is set when the application is intialized and storage saved once again after elapsing. Also SMS can be sent for confirmation.
- Commands supported by the HW remote devices are : `COMMAND_GET_DATA_RECORD`, `COMMAND_WORD_STOP`, `COMMAND_DELETE_RECORDS`. User can investigate the number of events per device, can stop the monitoring or delete all the recoreded data to the current time. 






