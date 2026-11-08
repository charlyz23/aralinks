# AraLinks (JavaFX)

AraLinks: University-Based Peer Tutoring System. OOP 101, Team 1 **WAP: What A Pass** (CS-2102).

## Run it

You need **JDK 21 or newer**. You don't need to install Maven: the `mvnw` wrapper downloads it.

```bash
./mvnw javafx:run      # open the app (Windows: mvnw.cmd javafx:run)
./mvnw test            # run the JUnit tests
```

Demo logins (password `demo123`): learner `25-11111`, tutor `23-01234`, admin `00-00001`.

## Layout

```
src/main/java/aralinks/
├── App.java          entry point (currently a login screen that proves the layers connect)
├── model/            the 10 UML classes + 3 status enums      ← no JavaFX imports here, ever
├── exception/        AraLinksException and its subclasses
├── service/          DataStore (ArrayLists, IDs, demo data), AuthService
└── ui/               JavaFX screens (to build)
src/main/resources/   aralinks.css, fonts/, mascot/, logo/
src/test/java/        JUnit tests
```

## How the UML maps to code

| UML | Code |
|---|---|
| `<<abstract>> User` | `abstract class User`; `getRole()` and `displayMenu()` are abstract |
| Administrator / Tutor / Learner | `extends User`, each overrides `displayMenu()` (polymorphism) |
| Tutor ◆— Availability (composition) | Only `Tutor.addAvailability()` can create an `Availability` (package-private constructor) |
| Tutor ◇— Course, TutoringClass ◇— Learner, TutoringSession ◇— Learner (aggregation) | `List<...>` of objects that live in `DataStore` |
| Arrows (sent to, about, covers, conducted by, for, written by) | A field on the class at the arrow's tail |
| Reverse questions ("requests sent to this tutor") | `DataStore` lookups, not extra fields |

### Deliberate changes from the UML (update the diagram to match)

1. **Statuses are enums:** `RequestStatus`, `ClassStatus`, `SessionStatus` instead of `String`.
2. **`Request.preferredSchedule`** became `day` + `startTime` + `endTime` so it can be checked against `Availability`. `getPreferredSchedule()` still returns text. `Request` also stores the `learner` who sent it.
3. **`Availability.day`** is a `DayOfWeek`, not a `String`.
4. **Methods have parameters**, e.g. `submitRequest(requestId, tutor, course, day, start, end, message)`.
5. **Methods that "display" return text** (`displayMenu(): List<String>`, `viewTutorProfile(): String`, `viewRecords(): String`, `displayReview(): String`) so screens and tests can use the result.
6. **Association fields** are written out: `Review.session`, `Review.learner`, `TutoringClass.tutor/course/learners`, `TutoringSession.tutor/course/learners/tutoringClass`.
7. **New helpers:** `Request.cancel()`, `TutoringClass.cancel()`, `TutoringSession.addLearner()`, `Tutor.isAvailable()`, `User.isActive()`.
8. **Exception hierarchy** (add a box to the diagram): `AraLinksException` → `ClassFullException`, `InvalidRatingException`, `InvalidRateException`, `InvalidScheduleException`, `UnavailableScheduleException`, `DuplicateRequestException`, `InvalidCredentialsException`, `InvalidLinkException`, `InvalidRequestStateException`.
9. A **group join** goes straight through `TutoringClass.addLearner()` (flow chart: capacity left → add learner). `Request` is for private sessions only, as its UML links (Tutor, Course) suggest.

## What's done and what's yours

Methods marked `TODO (Name)` throw `UnsupportedOperationException` until implemented. Search the project for `TODO (` to see them all. When you finish one, remove `@Disabled` from its test (if it has one) and make it pass.

| Owner | TODO methods |
|---|---|
| **Kennard** | `User.updateProfile`, `Tutor.acceptRequest`, `declineRequest`, `completeSession`, `computeAverageRating`, `Learner.searchTutors`, `viewTutorProfile`, `submitRequest`, `cancelRequest`, `rateSession`, `AuthService.register`, more demo data in `DataStore` |
| **Charlyz** | `Request.accept`, `decline`, `cancel`, `getDetails`, `TutoringClass.setClassroomLink`, `startClass`, `endClass`, `TutoringSession.schedule`, `markCompleted`, `cancel`, `getDetails` |
| **Aifa** | `Administrator.manageUser`, `removeListing`, `viewRecords`, `Review.displayReview`, the `ui` screens and CSS |

Already done as worked examples: `Course`, `Availability`, `User.login`, `Tutor` courses/rate/availability, `TutoringClass` enrollment, `TutoringSession.computeCost`, `Review` validation, `DataStore`, `AuthService.login`.

## Team rules

- Fields are `private`; collections are returned read-only (`Collections.unmodifiableList`). Change data only through methods.
- Break a rule → throw a specific `AraLinksException` subclass. Screens catch it and show the message in the red banner.
- Every rule gets a JUnit test. `./mvnw test` must pass before you push.
- One branch per feature, pull request into `main`, one teammate reviews.

Fonts: Nunito (SIL Open Font License, see `src/main/resources/fonts/FONT-LICENSE.md`). Mascot and logo: Sparty, from `assets/demo-kit`.
