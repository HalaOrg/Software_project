# Sprint Coverage Check

## Setup (Environment & Tools)
- Maven project with Java 17 source/target configuration present (`pom.xml`).

## Sprint 1 – Core Admin & Book Management
- **Admin login/logout** handled in `AuthService.login`/`logout`, storing the current user and clearing the session on logout.
- **Add book** supported through `MediaService.addMedia`, which persists added items.
- **Search book** available via `MediaService.searchBook` and generic `searchMedia` for title/author/ISBN matching.

## Sprint 2 – Borrowing & Overdue Logic
- **Borrow book** implemented in `MediaService.borrow`, computing the due date using the media-specific borrow duration and recording the borrow.
- **Overdue detection** available through `ReminderService.getOverdueDays`, which checks due dates against the provided `TimeProvider`.
- **Pay fine** provided by `FineService.payFine`, reducing balances with floor at zero.

## Sprint 3 – Communication & Mocking
- **Send reminder** implemented in `ReminderService`, which builds overdue counts and notifies observers (defaulting to `EmailNotifier`/`SmtpEmailServer`) with the message format "You have n overdue book(s)."

## Sprint 4 – Advanced Borrowing Rules
- **Borrow restrictions** enforced in `MediaService.borrow`, which blocks borrowing when outstanding fines exist, when active loans are overdue, and when no copies are available.
- **Unregister user** guarded by `AuthService.removeUserWithRestrictions`, which requires an admin session and blocks removal when fines or active loans are present.

## Sprint 5 – Media Extension
- **Borrow CD for 7 days** via `CD.getBorrowDurationDays` (7-day window).
- **CD overdue fine (20 NIS)** via `CD.getDailyFine` (20 per day) vs. books at 10 NIS.
- **Mixed media handling** supported because `MediaService` stores both `Book` and `CD` items in a single collection and returns unified results for searches and fine queries.

## Testing Status
- `mvn test` currently fails in this environment because Maven cannot download required plugins/dependencies (HTTP 403 from Maven Central), so automated verification could not be run here.
