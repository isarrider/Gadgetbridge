# Zepp OS: readiness ("BioCharge" / "HybridCharge") is app-side

Status: **research note** — no functional change.

## 1. Finding

On Zepp OS watches that expose a readiness/energy feature (the watch reports it as
a `bio_charge` display item, e.g. Amazfit Active 2), the readiness values are
**not transmitted by the watch**. They are computed by the vendor's phone app and
only sent to the watch for presentation.

Consequences:

* There is nothing to fetch for this metric. Instrumenting the protocol and
  waiting for the watch to send it will not work.
* The score is produced by a **proprietary algorithm** that runs on the phone.
  The vendor documents it as "BioCharge"/readiness and has published patent
  applications covering the model (readiness from physical energy, mental energy
  and body energy; fatigue from sleep quality and activity). Re-implementing it
  is therefore out of scope for Gadgetbridge, both technically and legally.
* The only interoperability surface is the **opposite direction**: writing a
  readiness value *to* the watch so it can display it.

This note exists so that nobody spends time trying to fetch the metric, and so
that anyone who wants to feed their own readiness score into the watch has a
starting point.

In other words: the goal is a **write-only** path that lets a user run their own
readiness model and have the watch display the result, without Gadgetbridge ever
shipping or replicating the vendor algorithm.

## 2. Current status

We have not yet observed the write on the wire. What is needed is a capture of
the official app while it updates the value on the watch; the resulting bytes can
then be documented here and implemented behind an experimental flag.

How to obtain the capture, using only external observation:

1. Enable the Bluetooth HCI snoop log on the phone (developer options, or
   `setprop persist.bluetooth.btsnooplogmode full` as root, followed by a
   Bluetooth restart).
2. Note the exact values shown by the app at that moment (current score and the
   "at wake-up" value) with a timestamp.
3. Force an update of the metric in the app (reconnect, refresh, or wait for the
   daily update).
4. Stop the capture and extract `btsnoop_hci.log`.
5. Correlate: the payload sent to the watch right after the values change is the
   one we are looking for.

A capture with known values is enough to describe the format; no decompilation or
inspection of the vendor app's internals is required.

## 3. Proposed Gadgetbridge integration (not implemented here)

```text
ZeppOsBioChargeService            // new service, endpoint TBD from the capture
  - disabled by default, behind an experimental per-device preference
  - writeReadiness(int current, int wakeUp)
      builds the payload observed in the capture and writes it to the watch
```

The values would come from a **Gadgetbridge-defined model**, not from the vendor
algorithm. The model should be simple, documented and user-tunable, with the
coefficients visible in the code. The value must be presented to the user as a
Gadgetbridge score, not as the vendor metric.

### Example: a user-defined energy score

The vendor models are opaque and change between firmware and app releases, so a
fixed formula cannot be "the" correct one. A user-tunable weighted sum is enough
for practical use, because each user can adjust it against their own reference
days (feeling optimal, or fully recovered).

One example, contributed by a user of this approach:

```text
Energy Score = S_score * W_s
             + HRV_score * W_hrv
             + Sleep_score * W_sleep
             + RHR_score * W_rhr

W_s     = 0.45   (sleep duration weight)
W_hrv   = 0.35   (heart rate variability weight)
W_sleep = 0.10   (sleep quality score weight)
W_rhr   = 0.10   (resting heart rate weight)

sum(W) = 1.00
```

#### Sub-scores (normalised to 0..100)

* `S_score` — **sleep duration score**. Based on the user's optimal baseline
  versus their maximum recovery hours: it scores lower below 6 h and scales up to
  100 for the 8-9 h maximum target.
* `HRV_score` — **heart rate variability score**. Computed from a custom optimal
  point (`HRV_opt`, e.g. 40 ms) and the maximum historical peak (`HRV_max`, e.g.
  60 ms):

  ```text
  if HRV >= HRV_max -> 100
  if HRV <= HRV_opt -> (HRV / HRV_opt) * 70
  ```

  Between both thresholds the score is interpolated (implementation detail).
* `Sleep_score` — **sleep quality score**. The app's sleep score mapped onto the
  user's personal optimal ranges (e.g. optimal 70-80, maximum 80-100).
* `RHR_score` — **resting heart rate score**. Inverted scale: lower values during
  sleep give higher scores, measured against the user's ideal minimum baseline
  (e.g. optimal at 55 bpm).

The result is normalised to 0..100 and is what would be written to the watch.
This is an example of the *interface* this proposal enables: the user owns the
model and its weights; Gadgetbridge only transports the resulting number.

#### Note on validation

This is the model the author actually uses, and they report that it is effective
for them. Two caveats apply to any score of this kind:

* A "personal energy" score only makes sense when it is **personalised**. The
  baselines (sleep target, optimal and peak HRV, resting heart rate) have to come
  from the user's own history, not from population averages; a fixed formula with
  fixed thresholds does not generalise between people.
* For such a score to be treated as a **viable metric** it should be supported by
  scientific studies that validate its effectiveness, or at the very least by a
  documented correlation with subjective readiness and objective outcomes. The
  example above has not been validated: it is the author's personal tuning,
  included as an illustration of the interface. It is not a medical or scientific
  metric and must not be presented to users as one.

Until the capture is available, the service should stay out of the tree; the
interface above is the expected shape.

## 4. Request for review

This note is a proposal, and it is open for feedback before any code is written:

1. Is a **write-only** readiness path acceptable in Gadgetbridge, if it is
   disabled by default, behind an experimental per-device preference, and clearly
   presented as a third-party score (never as the vendor metric)?
2. Where should the resulting model live so that users can plug in their own
   score (built-in simple model plus an override), and how should the value be
   surfaced in the UI (device settings, or nothing at all beyond the watch)?
3. Is a capture with known values (Bluetooth HCI snoop plus the values shown by
   the app at that moment) enough for the maintainers, or is anything else
   expected before the payload format is documented?
4. Should this note live in this repository or in the website documentation?

If the feature is considered out of scope, this document is still useful as a
record of why the metric cannot be fetched, so that the question does not come
back.

## 5. Related, but different

These metrics *are* produced by the watch and can be read independently:

* atmospheric pressure samples,
* naps (separate from the main sleep session),
* the daily activity summary (including floors climbed).

They are not affected by anything said in this document.
