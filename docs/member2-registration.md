# Member 2: Registration Engine Documentation

## Overview
Member 2 is responsible for the core registration logic of the Course Registration System (Coursify).
This includes rule-based validation before allowing a student to register for a course, as well as handling course cancellations and event notifications.

---

## Design & Architecture

### 1. Strategy Pattern & Polymorphism
All validation checks are encapsulated using the **Strategy Pattern**. 
- Abstract base class: `ValidationRule` with method `validate(Student student, Course course) throws RegistrationException`.
- Concrete rule classes inherit from `ValidationRule` and encapsulate single validation concerns:
  1. `DuplicateRule`: Ensures the student is not already actively registered for the course.
  2. `PrerequisiteRule`: Ensures the student has completed all required prerequisite courses.
  3. `TimeConflictRule`: Ensures the requested course's time slot does not clash with any of the student's registered courses.
  4. `SeatAvailabilityRule`: Ensures the course has available seats remaining (`seats_left > 0`).

`RegistrationServiceImpl` maintains a `List<ValidationRule>` executed sequentially during registration.
`SeatAvailabilityRule` is executed **last** so that students who fail duplicate, prerequisite, or schedule checks are caught early before checking seat availability, ensuring only eligible students are offered waitlist placement when a course is full.

---

## Algorithms & Logic

### 2. Prerequisite Graph Traversal (Depth-First Search)
To handle multi-level or chained prerequisites (e.g., CS301 requiring CS102 and CS201, where CS201 requires CS101), `PrerequisiteRule` performs a **Depth-First Search (DFS)** over the prerequisite graph:
- **Graph Source:** Obtained from `courseDAO.loadPrerequisiteGraph()`.
- **Traversal:** Starting from the target course code, DFS recursively traverses direct and transitive prerequisites using a `visited` set to prevent infinite loops in cyclic dependencies.
- **Validation:** Collects all required prerequisite codes into `allPrereqs` set. Compares `allPrereqs` against `studentDAO.findCompletedCourses(studentId)`.
- **Exception:** If any prerequisites are missing, throws `PrerequisiteNotMetException` containing the sorted list of missing prerequisite course codes.

### 3. Time Conflict Overlap Logic
`TimeConflictRule` checks for schedule clashes between the candidate course and the student's currently active registrations.
- **Null Slot Handling:** Courses with a `null` `TimeSlot` represent flexible/online courses and never clash.
- **Overlap Formula:** Two courses `A` and `B` clash if and only if:
  $$\text{sameDay} \land (\text{start}_A < \text{end}_B) \land (\text{start}_B < \text{end}_A)$$
- **Back-to-Back Support:** Slots such as 10:00-11:00 and 11:00-12:00 do **not** overlap because $11:00 < 11:00$ evaluates to `false`.

---

## Cancellation & Waitlist Integration
When `cancel(studentId, courseCode)` is invoked:
1. Validates that the student has an active registration for `courseCode`.
2. Calls `registrationDAO.cancelAtomic(studentId, courseCode)` to update the status to `CANCELLED` and increment `seats_left`.
3. Publishes a `RegistrationEvent` with type `CANCELLED`.
4. Invokes `waitlistService.promoteNext(courseCode)` so the next eligible student on the waitlist is automatically registered into the newly freed seat.
