# OnePlus Watch (2021, W301GB)

Experimental Gadgetbridge driver for the original **OnePlus Watch** (2021, model W301GB, advertises as
`OnePlus Watch XXXX`). It is not the Wear OS "OnePlus Watch 2".

- Last updated: 2026-11-14
- Driver sources: `devices/oneplus/` (coordinator, sleep sessions) and `service/devices/oneplus/` (protocol, device support, this file)
- Tests: `OnePlusProtocolTest` (real captured frames) and the `OnePlus Watch 0A22` case in `BluetoothNameTest`
- The driver was written by reverse engineering the vendor app (OHealth) and Bluetooth captures; the full protocol notes live
  outside this repository, so the protocol facts needed to continue are summarised in section 5.

Effort scale: **S** = hours, **M** = a day or two, **L** = several days or more.
"Needs capture" means the message format is not known well enough yet and must be observed on a real watch first.

---

## 1. Implemented

Connection and setup
- **Pairing**: normal Bluetooth bond (numeric comparison); no key or account needed.
- **Connection**: Bluetooth Classic serial link (RFCOMM, SDP UUID `db764ac8-4b08-7f25-aafe-59d03c27bae3`), auto-reconnect.
- **First-time setup**: answers the watch's "waiting for setup" signal, so the pairing screen disappears and the watch face appears.
- **Time and timezone sync**: on every connect and when the phone's time or timezone changes.
- **Device info**: firmware and hardware version.
- **Battery**: level at connect, on each data fetch, and when the watch reports a change; "low" state at 15% or less.
- **Find my phone** (watch rings the phone, verified on a real watch): while the watch's find-phone screen is open it sends an empty `1.15` about once a second. The first one starts Gadgetbridge's standard find-phone ring; the ring stops 3.5 s after the messages stop. No reply is sent. The sound is Gadgetbridge's global "Ping tone" setting (see section 6).

Notifications
- **Notifications to the watch**: title, text, app name; removal when dismissed on the phone.
- **Quick reply** from the watch, sent back through the original app.
- **Dismiss** on the watch clears the notification on the phone.

Health data (stored in Gadgetbridge's own database, shown in its charts)
- **Heart rate history** (about one reading per minute in Continuous mode).
- **Blood oxygen (SpO2) history**, with unreliable and empty readings dropped.
- **Sleep history** as stages per minute (light, deep and awake), grouped into sleep sessions. The activity samples also carry the sleep stages, because the dashboard sleep graph and the sleep charts read sleep from there.
- **Steps, distance and calories** per minute (`5.9`/`5.10`), shown in the activity charts, the dashboard widgets and the daily totals on the device card. They are stored in their own table (`OnePlusActivitySample`, database schema 161); a read-only sample provider merges them with the heart-rate samples and the sleep stages.
  - Distance is in metres (about 0.6 m per step). Calories: the **watch sends** a kcal value for every active minute; Gadgetbridge does not estimate it from steps or heart rate. It is stored in 1/1000 kcal, the unit Gadgetbridge sums and divides by 1000. The "Calories" total on the dashboard adds Gadgetbridge's own resting-metabolism estimate (from the user profile, for the part of the day passed). The watch's own daily totals (`5.21`, including a "static" calorie value) are not used.
- **Heart-rate monitoring mode** setting: Off / Smart / Continuous. It changes the watch's automatic measurement and the polling interval (none / 10 min / 1 min).
- First sync asks for 14 days; later syncs ask from one day before the last sync.

Developer
- **Diagnostic logging toggle** (Debug section): requests extra data types and writes raw replies to logcat only; off by default.
- **Tests**: 5 protocol tests using real captured frames, plus the device-name test.

### Known caveats in what exists
- **Sleep stage codes**: 1 = light and 2 = deep are **confirmed** (light is the longest stage of a night, as expected). Code 4 has appeared once, as a 4-minute run between light sleep, and is taken as awake (unconfirmed). **REM (3) has never been seen**, so it is not mapped and the REM stage is not declared as supported; the vendor app has a "sleep REM" setting for the watch (`FitnessProto` command 51), so some models can report it, but whether this one can is unknown.
- **Timezone unit**: sent in half-minutes (UTC+2 = 240), copied from one captured example.
- **Charging state**: handled in code but never observed on a real charging watch.
- **Heart-rate data timing**: the watch hands over data in batches every 4-5 minutes, so the chart runs 2-5 minutes behind.
- **Spot measurements** taken on the watch (heart rate, SpO2) do not appear in the history.
- **Setup handshake** is sent whenever the watch says it is waiting, not only once.
- Wrist side is fixed to one value during setup (changeable on the watch itself).
- Some Gadgetbridge calls used are deprecated upstream (`acquireDB`, `updateTransferNotification`).

---

## 2. Next to implement

### 2a. High priority

| # | Item | Effort | What is known / needed |
|---|---|---|---|
| 1 | **Phone calls** (show caller on the watch; reject, silence or answer from the watch) | M | Experimental, not working yet. The driver sends the vendor's `PhoneCallChange` (service 6, command 1: `{1 status, 2 number, 4 subscription id, 9 is contact, 10 name, 13 primary}`) when the phone rings, is answered and ends, plus the SIM list (6.6) on connect. The first attempt, without the `primary` flag and the SIM list, made the watch do nothing and send no reply on service 6. The second attempt (vendor layout, `primary`, SIM list) is untested. Watch to phone messages (6.2 answer/reject/mute, 6.3 place call) are only logged. |
| – | **Find my watch** (ring the watch from the device card) | S, once known | **Tried, does not work yet.** See section 4 for exactly what was sent. Needs a capture of the official app doing it. |

### 2b. After that

| # | Item | Effort | What is known / needed |
|---|---|---|---|
| 2 | **Stress and resting heart rate storage** | S | Replies `5.34/5.35` and `5.17/5.18` already arrive in the diagnostic log; formats decoded. Store in the generic stress and resting-HR tables. |
| 3 | **Tidy-up**: run setup only once, remove unused code, replace deprecated calls, add unit tests for the SpO2 and sleep decoders | S-M | Straightforward. |
| 4 | **Music and media control** | M | The watch sends media messages (service 8); formats not decoded. Needs a capture while playing music. |
| 5 | **Alarms and other watch settings** (do-not-disturb, raise-to-wake, goals) | M | Many settings messages are defined in the app's code (service 5, commands 172-249 and others). Needs a capture per setting. |
| 6 | **REM sleep stage** | S | Add (mapping and the coordinator's REM support flag) as soon as a code other than 1, 2 and 4 shows up in real data, and verify it against a night. |
| 7 | **Prepare for upstream**: translations, a device page, cleanup, a pull request to Gadgetbridge | M | Not started. |

---

## 3. Future features

### Workout import (prerequisite for GPS tracking and barometer data)
- **Goal**: import workouts from the watch with route, heart-rate series, pace, cadence and elevation (barometer-derived).
- **Effort**: **L**
- **Known**: the watch announces a workout (`4.15` state changes `1`..`4`, then `4.39` "new sport data"). The record itself is a **file** transferred over the file service, likely on the second serial channel (SDP "SPP SERVER3"). Each record holds per-second items: timestamp, pace, heart rate, cadence, elevation, state. The app has a native parser (`libsport-record-parse.so`) for it.
- **Needs**: capture a real file transfer after a short workout; reverse the file framing and the record format; map to Gadgetbridge's workout/track tables and GPX export.

### GPS tracking
- **Goal**: GPS routes from workouts shown and exportable (GPX).
- **Effort**: **L** (shares most of its work with workout import)
- **Known**: workout records contain GPS items (`GpsDataItem`, `SportRecordGpsData`).
- **Also (GPS assist)**: the watch asks the phone for satellite assist data (service 25, command 5), the phone answers with file info (command 6) and sends the file; file types cover GPS, GLONASS, BeiDou, Galileo and ionosphere. This speeds up satellite lock. Needs the download source (the app gets it from a server) and the file transfer from the item above.

### Weather
- **Goal**: show weather on the watch from Gadgetbridge's weather provider.
- **Effort**: **M**
- **Known**: the watch asks for weather on service 9 (commands `9.1` and `9.2`, with a location code) about every 5 seconds; it is not answered today.
- **Needs**: the weather message layout. It is defined in the OHealth app's weather code and has not been read yet; then a capture of a real reply to verify. Gadgetbridge already has weather data for other watches to reuse.

### Watch face changing
- **Goal**: switch between installed watch faces and upload new ones.
- **Effort**: **L**
- **Known**: the app has a watch-face protocol (`WatchFaceMessage`) and a native packer (`librs_watchface_packer.so`); face names for this model are bundled in the app (`W301GB_xxxxx_dial_name`). OnePlus devices use a different code path than other watches, so the generic handler isn't what this watch uses.
- **Needs**: a capture of changing a face and of installing one; then the face package format and its transfer over the file service. Switching an already-installed face is probably much simpler (a single message) than uploading new ones, so it could be done first.

### Navigation sync
- **Goal**: send routes or turn-by-turn guidance to the watch.
- **Effort**: **L**, and uncertain
- **Known**: **no navigation protocol was found** in the OHealth app, and I found no route-upload feature. This model may not support it at all.
- **Needs**: first confirm whether the watch has any navigation or route screen; if so, capture what the official app does. Otherwise this may be impossible on this hardware.

### Barometer / altitude
- **Goal**: pressure and altitude data.
- **Effort**: **M** (after workout import)
- **Known**: the watch asks the phone for a reference air pressure about every 4 minutes (`4.14`). It does not report its own pressure as a separate message; elevation appears inside workout records.
- **Needs**: workout import; optionally answer the watch's pressure request (units not yet known).

### Live heart rate
- **Goal**: true real-time heart rate.
- **Effort**: **L**, and uncertain
- **Known**: no live stream exists outside phone-started guided workouts; the current Continuous mode approximates it (one reading per minute, a few minutes behind).
- **Needs**: a capture while the phone starts a workout on the watch, to see whether the watch streams live readings then.

---

## 4. Tried and did not work

### Find my watch (2026-11-14): ignored by the watch
What was implemented (about 25 lines; since removed from the driver):
- `supportsFindDevice() = true` in the coordinator, which makes Gadgetbridge show the find-device button on the device card.
- `onFindDevice(start)` in the device support sent **service 1, command 40**, body `FindDevice{1: status}`:
  start = `08 01` (status 1), stop = empty body (status 0 is the protobuf default). Frame for start: `AA 08 00 00 01 28 08 01 <crc16>`.
- An automatic stop after 3 minutes, as the vendor app does; an incoming `1.40` with status 0 was treated as "stopped on the watch".

Where it came from (OHealth, decompiled): the message builder `MessageEvent(1, 40, FindDeviceProto.FindDevice.newBuilder().setStatus(i)...)`
and the `FindWatchPresenter`, which calls it with 1 to start ringing and 0 to stop, auto-stops after 3 minutes, and treats a
`1.40` with status 0 coming from the watch as "stopped on the watch".

Result on a real watch: the write succeeded (three attempts), the watch sent **no reply**, and it neither rang nor vibrated.

Not tried (ideas for later):
- Other status values (2, 3, ...) or extra fields; sending an acknowledgement.
- The vendor app also defines a separate `FindWatchProto` (service id **268**, command 1, `{1: action, 2: status, 3: ticket}`); a
  service id above 255 cannot be a wire service byte in this protocol, so it most likely belongs to the Wear OS style channel and not to this watch.
- Checking whether the watch advertises a find-device capability in its device info / feature set (`1.7`, `1.33`).
- **Best next step:** capture the official app doing "Find watch" (OHealth connected to the watch, HCI snoop log) and compare the frame.

### Chart on the device card (2026-11-14): not implemented
Wanted: active calories, calories and steps charts reachable from the watch's card in the device list.
- Gadgetbridge's device card has three fixed gauges (steps, distance, sleep). Two calorie gauges in a second row were built and
  then removed again as not wanted.
- Tapping a connected card only shows a "long-press" hint. A small opt-in hook (a coordinator method naming a chart tab to open on tap)
  was tried; the charts screen opened but no new chart appeared, so the hook was removed.
- Today the calories and steps charts are reached through the dashboard widgets and the charts screen (the calories and steps tabs).

## 5. Protocol summary (what is needed to continue)

Transport: Bluetooth Classic RFCOMM, SDP service UUID `db764ac8-4b08-7f25-aafe-59d03c27bae3` (channel 4 on the W301GB), plain
protobuf, no app-level authentication (a normal Bluetooth bond is enough).

Frame: `AA | len (varint) | 00 00 | service | command | protobuf body | CRC16 (LE)`; `len` counts service..CRC; CRC-16/XMODEM over all preceding bytes including `AA`.

| Service | Meaning | Commands known so far |
|---|---|---|
| 1 | device / bind | `1` hello, `5` time and timezone, `7` device info, `8` battery, `15` find phone (watch to phone), `26` bind request; `24`, `25`, `27`, `56` (wearing state) seen, unmapped |
| 2 | notifications | `1` post, `3` remove, `4` dismissed on watch, `7` quick reply |
| 4 | watch and app misc | `14` air-pressure request (watch asks the phone, about every 4 min), `15` workout state, `39` new sport data; not mapped |
| 5 | fitness / health | `9/10` per-minute activity, `11/12` heart rate, `13/14` sleep, `17/18` resting HR, `21` daily totals, `23/24` SpO2, `34/35` stress, `4` automatic HR measurement mode |
| 8 | media | `8` seen; now-playing, not decoded |
| 9 | weather | `1`, `2` requests from the watch about every 5 s, not answered |
| 11 | first-time setup (OOBE) | see the driver; the watch repeats an empty `12` while it waits |
| 25 | GPS assist | `5` request for satellite assist data, `6` reply with file info (not implemented) |
| 26, 28 | file transfer, log kit | not mapped |

Timestamps are Unix seconds. The timezone offset in `1.5` is in half-minute units (UTC+2 = 240).
Sleep stage codes: 1 = light and 2 = deep are confirmed; 4 is taken as awake (seen once, 4 minutes); 3 (REM) has never been seen.
Activity items (`5.10`): `{1: minute offset, 2: calories (kcal), 3: distance (m), 5: sport type, 7: steps}`; only minutes with data are present and replies only come for packets that hold data.

## 6. Notes and findings

- **Find phone sound:** Gadgetbridge plays the single global "Ping tone" setting for every device. OHealth uses its own 2.3 s clip
  (`res/raw/find_phone_audio.ogg` in the vendor APK). It is proprietary, so it is deliberately **not** bundled with this driver;
  using it would mean per-device sound handling in `FindPhoneActivity`.
- **Heart-rate timing:** the watch hands over data in batches every 4-5 minutes, so charts run 2-5 minutes behind.
- **Diagnostic logging:** Device settings, Debug section, "Diagnostic logging" logs every message from the watch to logcat
  (`OnePlusWatchSupport: RX <service>.<command> body=<hex>`) and requests extra data types. That is how find phone (`1.15`) was found:
  trigger the action on the watch, then `adb logcat | grep OnePlusWatchSupport`.
- **Building and testing:** JDK 21, then `./gradlew --offline :app:testMainlineDebugUnitTest --tests '*OnePlus*'` and
  `:app:assembleMainlineDebug`; install with `adb install -r`.
- **Commit structure:** one commit for the driver skeleton, then one commit per feature (setup, time, battery, notifications,
  heart rate, SpO2 and sleep, diagnostics, find phone), so each feature can be reviewed on its own.

## 7. Open questions
- What do sleep codes 0 and 3 mean, and is 4 really awake? (1 = light and 2 = deep are confirmed.) Can this model report REM at all, and is it switched on by the setting the vendor app calls "sleep REM"?
- Does the watch report charging state, and in which field?
- Does the watch show calls through Bluetooth HFP only, or is there a capability bit that enables the service 6 call messages? Which messages does it expect for music?
- What do the unmapped services (4, 11, 15, 28) carry beyond setup and workouts?
- Can the watch answer the weather request in a different format than the phone sends to other OnePlus devices?
- What does the watch expect for find my watch (`1.40` with status 1 was ignored, see section 4)? Does it expect a reply to find-phone messages (`1.15`)?
