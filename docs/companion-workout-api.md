# Companion workout API (draft, version 1)

This local Binder API lets a user-approved training app submit structured workouts to
supported paired devices without confirming each transfer in the installer. It is
independent of the client app and device vendor. The first backend is Garmin workout
FIT files (type 5). It does not install firmware, apps, courses or training calendars.

## User approval

Launch `nodomain.freeyourgadget.gadgetbridge.action.REQUEST_WORKOUT_ACCESS` with
`setPackage()` set to the installed Gadgetbridge distribution, using an Activity Result
launcher (or startActivityForResult). Android's calling package identifies the requester;
a package name in extras is not accepted. The user selects a supported paired device
and approves the requesting package and its current signing identity. Success returns
RESULT_OK. Requests without a calling Activity cannot grant access.

The user can revoke grants under **Settings → Intent API → Workout companion apps**. Signing changes
require approval again. Revocation prevents queued jobs from starting; it does not
interrupt a file already transferring. Shared-UID packages share Android's security
identity, and another package in that UID can use a grant held by that UID.

## Binding

Copy `app/src/main/aidl/nodomain/freeyourgadget/gadgetbridge/companion/IWorkoutSyncService.aidl`
into the client, preserving its namespace. Bind an explicit service Intent for
`nodomain.freeyourgadget.gadgetbridge.action.WORKOUT_SYNC_SERVICE`, with the selected
Gadgetbridge package and BIND_AUTO_CREATE. The runtime package varies between mainline,
nightly and other distributions; clients must not assume one installed package.

`getApiVersion()` returns 1 without exposing device information. `getDevices()` returns
only approved devices with the workout-file capability, each as a Bundle with `device`
(address), `name` and `connected` (initialized state). A new backend must implement
WorkoutInstallHandler and opt into DeviceCoordinator.supportsWorkoutFileImport.

## Submitting and querying

Call Binder methods off the UI thread. For each workout:

1. Create a content URI from a provider the client owns or is permitted to read.
2. Grant Gadgetbridge read access with Context.grantUriPermission. Passing a URI as a
   Binder argument does not itself grant permission.
3. Call `submitWorkout(requestId, deviceAddress, contentUri)`.
4. After it returns, Gadgetbridge owns a private copy and the client may revoke the
   source grant and remove its temporary file.

Only content URIs are accepted. Request IDs are nonblank strings of up to 128 characters.
A request is isolated by approved package/signing identity, device and request ID.
Submitting that identity again with identical bytes returns the same job; different
bytes produce IllegalArgumentException (`request_id_conflict`). Use a new request ID
for a new revision.

This API never deletes anything on the device. Whether a newer upload replaces an older
workout is decided by the watch. On the Garmin Enduro 3 (checked 29-30 October 2026) the
watch treats a workout FIT file as the same workout when its `file_id` fields (type,
manufacturer, product, serial number, time created and number) are identical, and
replaces the stored copy; name and steps are irrelevant, and a different `time_created`
is stored as a new workout. Garmin's protobuf `DeleteItemRequest` and the legacy
erase/archive flags did not remove workouts on that watch. A client that wants a bounded
number of workouts on the watch can therefore reuse a fixed set of `file_id` values.

The returned Bundle and `getJob(requestId, deviceAddress)` expose:

- `jobId`: opaque internal job key.
- `requestId`, `device`: caller request and target.
- `sha256`: digest of the exact submitted bytes.
- `state`: QUEUED, TRANSFERRING, TRANSFERRED, FAILED, UNCERTAIN or CANCELLED.
- `error`: stable machine-readable reason, or an empty string.

Unknown requests return `state=NOT_FOUND`. Unauthorized operations throw SecurityException.
Invalid arguments throw IllegalArgumentException. No arbitrary filename, package or
job-ID lookup is exposed. The client polls its own job; there is no public broadcast
of workout contents or device identity.

QUEUED means saved, not installed. Input parsing/capability validation happens before
any file transfer. The worker requires a WorkoutInstallHandler and rejects compatibility
warnings that would require interactive confirmation, including developer overrides.
TRANSFERRING is saved before dispatching the transfer.
TRANSFERRED means the device transfer protocol acknowledged success, not that a
readback verified the watch's workout list or training calendar.

If the process dies during transfer, or completion is not confirmed, the result is
UNCERTAIN. A timeout does not necessarily stop a radio transfer already in progress.
The same request is not blindly retransmitted: clients should reconcile or ask the
user to check the watch. This avoids claiming exactly-once watch installation when
an acknowledgement may have been lost.

`cancelJob(requestId, deviceAddress)` cancels queued work atomically against transfer
claiming. It rejects an in-flight transfer (`transfer_in_progress`). Terminal states
are immutable, including on duplicate requests and late callbacks.
`forgetJob(requestId, deviceAddress)` removes a terminal result. The client must retain
a local copy of any result it needs first: after forgetting, the ID is no longer
reserved against replay.

## Background behaviour and limits

Jobs use private AtomicFiles and WorkManager. Reconnect/idle events wake queued jobs;
periodic reconciliation covers missed events and process restarts. The recovery task
is scheduled only after use and stops when there are no pending jobs. Android scheduling
and Bluetooth availability are best effort. Interactive install sessions are not
preempted, and disconnected requests remain queued.

Files are limited to 5 MiB, the whole pending file queue to 20 MiB, and each owner to
256 retained jobs. Clients should forget terminal results after recording them.
Transferred/failed/uncertain/cancelled files are deleted while their result remains.
The job Bundle does not include a watch-side identifier.

Job changes emit an explicit-package `WORKOUT_JOB_CHANGED` broadcast to the
still-authorized owner, with `requestId`, `device`, and `state`. Treat it as a wake-up
hint and query authenticated status; background delivery remains best effort.

Garmin interactive batches accept at most 32 files and 20 MiB in total, reject mixed or
corrupt batches before sending, and retain the ordinary Install confirmation.

## Tests

Unit tests cover approval, revocation and signer changes, Binder access without a grant, the job
store (restart recovery, request ID conflicts, cancelling against a transfer starting, terminal
states) and the Garmin workout handler (batches, corrupt or mixed files, compatibility warnings).

Hardware checked so far: one Garmin Enduro 3, with an upload from another app both through the
installer and through this API. Other models, offline/reconnect, process death and revoking a
queued job have only been covered by unit tests.
