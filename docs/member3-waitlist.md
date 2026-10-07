# Member 3: Waitlist & Analytics

## Observer Pattern
`RegistrationSubjectImpl` uses the Observer pattern to notify listeners (like the UI or other services) about registration events (e.g. `WAITLISTED`, `PROMOTED`). We use a `CopyOnWriteArrayList` to safely iterate over listeners even if some are added or removed during notification. When `useEdt` is true, notifications are dispatched to the Swing Event Dispatch Thread (EDT) using `SwingUtilities.invokeLater()` to ensure UI updates are thread-safe.

## PriorityQueue for Waitlist
Each course's waitlist is backed by a `PriorityQueue`. Depending on the configured strategy, we use `WaitlistComparators.BY_JOIN_TIME` (simple FIFO queue) or `WaitlistComparators.BY_CGPA_THEN_TIME` (merit-based priority queue). The queue ensures that when a seat opens up (and `promoteNext()` is called), the student with the highest priority is automatically promoted and registered.

## Max-heap for Analytics
In `AnalyticsServiceImpl`, we build a max-heap (a `PriorityQueue` with a reversed comparator based on `getDemandScore()`) to determine the top-k most in-demand courses. The demand score is calculated as `(registered + waitlisted) / (double) capacity`. By keeping all courses in a max-heap, we can extract the top `k` efficiently by polling `k` times.
