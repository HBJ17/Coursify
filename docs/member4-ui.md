# Member 4: UI & Main

Swing screens, controllers and the program entry point. The UI talks only to the service interfaces in
`AppContext`, never to SQL, DAOs or another member's classes.

## Screen flow

```
                 ┌──────────────┐
   Main ───────► │  LoginPanel  │ ◄──────────── Logout (HeaderBar)
                 └──────┬───────┘                    ▲
          role STUDENT  │  role ADMIN                │
        ┌───────────────┴───────────────┐            │
        ▼                               ▼            │
 StudentHomePanel                 Admin screen ──────┤
  ├ Browse Courses  (search, Register, waitlist offer)│
  ├ My Courses      (Cancel; auto-promotes waitlist) │
  └ My Waitlists    (position, Leave)                │
        └──────────────────────────────────────────►─┘
                                  AdminDashboardPanel
                                   ├ Top 5 in-demand courses
                                   ├ All active registrations
                                   └ Add course / Remove course
```

## Classes

| Class | What it does | Concept |
|---|---|---|
| `app.Main` | Sets the system look and feel, opens `MainFrame` on the Swing thread with `AppContext.createDefault()` (Oracle if `db.properties` exists, else fakes). `--console` runs `ConsoleDemo`. | Entry point |
| `ui.MainFrame` | The only window. A `CardLayout` holds the current screen; `showScreen` rebuilds it and unhooks the old screen's listeners. | CardLayout |
| `ui.ScreenNavigator` | One-method interface for "switch screen". Panels depend on it, not on the JFrame. | Abstraction / testability |
| `ui.PanelFactory` | Maps a screen name (`LOGIN`, `STUDENT`, `ADMIN`) to the panel that builds it. | **Factory pattern** |
| `ui.Session` | Holds the logged-in `Person` (Student or Admin). | Polymorphism via `getRole()` |
| `ui.RefreshablePanel<T>` | Base for live screens: subscribes to `RegistrationSubject`, reloads in a `SwingWorker` on every event. | **Observer pattern**, generics, template method |
| `ui.BrowseCoursesPanel` | JTable of all courses + live search (`DocumentListener`), Register button / double-click. | |
| `ui.MyCoursesPanel` | Student's registered courses, total credits, Cancel button. | |
| `ui.WaitlistPanel` | Waitlists with position (1 = next), Leave button. | |
| `ui.AdminDashboardPanel` | Top-5 demand (from the analytics max-heap) + all registrations, Add/Remove course. | |
| `ui.AddCourseDialog` | Modal form that builds a course with `Course.builder(...)`. | **Builder pattern** |
| `ui.HeaderBar`, `ReadOnlyTableModel`, `UiFormat` | Welcome + Logout strip; non-editable table model; formatting helpers. | Reuse |
| `controller.BaseController` | `runAsync`: run a service call in a `SwingWorker`, return the result on the Swing thread, show errors in a `JOptionPane`. | MVC controller, threading |
| `controller.LoginController` | Logs in, then routes to the student or admin screen by role. | |
| `controller.RegisterController` | Register (offers waitlist on `CourseFullException`), cancel, leave waitlist. | Exception handling |
| `controller.AdminController` | Add course, remove course. | |

## Threading rule

Swing is single-threaded: only the Event Dispatch Thread (EDT) may touch components, and slow work (database)
must not run on it or the window freezes. So:

1. A button click (EDT) calls a controller.
2. The controller starts a `SwingWorker`; `doInBackground()` calls the service on a worker thread.
3. `done()` runs back on the EDT and updates the screen or shows a dialog.
4. Events from the service may arrive on any thread, so `RefreshablePanel.onRegistrationChanged` uses
   `SwingUtilities.invokeLater` before reloading.

## How errors reach the user

Services throw `CourseFullException`, `TimeConflictException`, `PrerequisiteNotMetException`,
`DuplicateRegistrationException`, `AuthException`, or `IllegalArgumentException` / `IllegalStateException`, and the
DAO layer wraps SQL errors in `DatabaseOperationException`. `SwingWorker.get()` wraps them in
`ExecutionException`; `BaseController` unwraps `getCause()` and shows its message with a matching title.

## Viva questions

1. **Why SwingWorker?** Database calls are slow. Running them on the EDT would freeze the window, so they run in the
   background and only the result is handed back to the EDT.
2. **Where is the Observer pattern?** `RefreshablePanel` implements `RegistrationListener` and subscribes to the
   `RegistrationSubject`. When a registration changes, the subject notifies every open panel, which reloads itself.
   On logout `MainFrame` unsubscribes them so old screens don't keep listening.
3. **Where is the Factory pattern and why?** `PanelFactory.create(name)`. `MainFrame` never constructs panels itself,
   so adding a screen means changing one place.
4. **How does the UI stay independent of the database?** It only uses the service interfaces from `AppContext`. The
   same UI runs on the in-memory fakes or on Oracle without any change.
5. **How is the full-course case handled?** `RegisterController` catches `CourseFullException` and asks whether to
   join the waitlist; if yes it calls `WaitlistService.join` and shows the position.

## Run

```
mvn exec:java                            # GUI (fakes, or Oracle if db.properties exists)
mvn exec:java -Dexec.args="--console"    # text demo
```
