package edu.library.presentation;

import edu.library.service.AuthService;
import edu.library.service.BorrowRecordService;
import edu.library.service.FineService;
import edu.library.service.MediaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.library.domain.model.Book;
import edu.library.domain.model.CD;
import edu.library.domain.model.Roles;
import edu.library.domain.fine.FineCalculator;
import edu.library.domain.time.TimeProvider;
import edu.library.domain.model.BorrowRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberTest {

    private MediaService service;
    private AuthService auth;
    private Roles user;

    @TempDir
    Path tempDir;

    private Path fineFile;
    private Path borrowFile;

    @BeforeEach
    void setup() throws IOException {
        Path mediaFile = tempDir.resolve("media_test.txt");
        Files.createFile(mediaFile);

        borrowFile = tempDir.resolve("borrow_records_test.txt");
        Files.createFile(borrowFile);

        // Direct fines storage into the per-test temp directory to avoid touching real files.
        fineFile = tempDir.resolve("fines_test.txt");
        Files.createFile(fineFile);

        BorrowRecordService borrowRecordService =
                new BorrowRecordService(borrowFile.toString());

        FineService fineService = new FineService(fineFile.toString());
        TimeProvider timeProvider = mock(TimeProvider.class);
        when(timeProvider.today()).thenReturn(LocalDate.of(2025, 12, 3));

        service = new MediaService(
                mediaFile.toString(),
                borrowRecordService,
                fineService,
                timeProvider,
                new FineCalculator()
        );

        auth = mock(AuthService.class);
        user = new Roles("testuser", "Test User", "test@example.com", "Member");
        when(auth.logout()).thenReturn(true);

        Member.fineFilePath = fineFile.toString();

        service.addMedia(new Book("Java Programming", "Author A", "ISBN001", 1));
        service.addMedia(new Book("Python Basics", "Author B", "ISBN002", 1));
        service.addMedia(new CD("Java CD", "Author A", "CD001", 1));
        service.addMedia(new CD("Python CD", "Author B", "CD002", 1));
    }

    @AfterEach
    void cleanup() throws IOException {
        if (fineFile != null) Files.deleteIfExists(fineFile);
        if (borrowFile != null) Files.deleteIfExists(borrowFile);
    }

    private int runMemberWithInput(String inputStr) {
        return Member.handle(
                new java.util.Scanner(new ByteArrayInputStream(inputStr.getBytes())),
                service,
                auth,
                user
        );
    }

    private void writeFine(String username, int amount) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fineFile.toFile()))) {
            bw.write(username + "," + amount);
        }
    }

    @Test
    void testSearchBookFoundAndNotFound() {
        assertEquals(0, runMemberWithInput("1\nJava\n"));
        assertEquals(0, runMemberWithInput("1\nNonExisting\n"));
    }

    @Test
    void testBorrowBook() {
        assertEquals(0, runMemberWithInput("2\nISBN001\n"));
        assertFalse(service.findBookByIsbn("ISBN001").isAvailable());

        assertEquals(0, runMemberWithInput("2\nINVALID\n"));

        assertEquals(0, runMemberWithInput("2\nISBN001\n"));
    }

    @Test
    void testDisplayAllBooks() {
        assertEquals(0, runMemberWithInput("4\n"));
    }

    @Test
    void testPayFines() throws IOException {
        assertEquals(0, runMemberWithInput("5\n"));
        writeFine("testuser", 50);
        String input = "5\n30\n";
        assertEquals(0, runMemberWithInput(input));
    }

    @Test
    void testRemainingTimeBooks() {
        service.borrowBook(service.findBookByIsbn("ISBN001"), user.getUsername());
        assertEquals(0, runMemberWithInput("6\n"));
    }

    @Test
    void testSearchCD() {
        assertEquals(0, runMemberWithInput("7\nJava\n"));
        assertEquals(0, runMemberWithInput("7\nNonExist\n"));
    }

    @Test
    void testBorrowCD() {
        assertEquals(0, runMemberWithInput("8\nCD001\n"));
        assertFalse(service.findCDByIsbn("CD001").isAvailable());

        assertEquals(0, runMemberWithInput("8\nINVALID\n"));

        assertEquals(0, runMemberWithInput("8\nCD001\n"));
    }

    @Test
    void testDisplayAllCDs() {
        assertEquals(0, runMemberWithInput("10\n"));
    }

    @Test
    void testRemainingTimeCDs() {
        service.borrowCD(service.findCDByIsbn("CD001"), user.getUsername());
        assertEquals(0, runMemberWithInput("11\n"));
    }

    @Test
    void testLogout() {
        assertEquals(1, runMemberWithInput("12\n"));
        verify(auth, times(1)).logout();
    }

    @Test
    void testExit() {
        assertEquals(2, runMemberWithInput("13\n"));
    }

    @Test
    void testInvalidOptionAndNonNumber() {
        assertEquals(0, runMemberWithInput("99\n"));
        assertEquals(0, runMemberWithInput("abc\n"));
    }

    @Test
    void testReturnBookNotBorrowedOrNonExistent() {
        // محاولة إعادة كتاب غير موجود
        assertEquals(0, runMemberWithInput("3\nINVALID\n"));

        // محاولة إعادة كتاب لم يتم استعارته
        assertEquals(0, runMemberWithInput("3\nISBN002\n"));
    }

    @Test
    void testReturnCDNotBorrowedOrNonExistent() {
        // محاولة إعادة CD غير موجود
        assertEquals(0, runMemberWithInput("9\nINVALID\n"));

        // محاولة إعادة CD لم يتم استعارته
        assertEquals(0, runMemberWithInput("9\nCD002\n"));
    }



    @Test
    void testUpdateFineFileFileNotExist() throws IOException {
        // نحذف الملف ليكون غير موجود
        Files.deleteIfExists(fineFile);
        Member.updateFineFile("newuser", 25);
        assertEquals(25, Member.getOutstandingFineFromFile("newuser"));
    }

    @Test
    void testGetOutstandingFineFromFileFileNotExist() throws IOException {
        Files.deleteIfExists(fineFile);
        assertEquals(0, Member.getOutstandingFineFromFile("anyuser"));
    }

    @Test
    void testViewRemainingBooksNoActive() {
        // بدون أي استعارة
        assertEquals(0, runMemberWithInput("6\n"));
    }

    @Test
    void testViewRemainingCDsNoActive() {
        // بدون أي استعارة
        assertEquals(0, runMemberWithInput("11\n"));
    }



    @Test
    void testReturnCDWithoutPayingFines() throws IOException {
        writeFine("testuser", 50);

        CD cd = service.findCDByIsbn("CD002");
        assertNotNull(cd, "CD002 should exist");

        service.borrowCD(cd, user.getUsername());

        String inputFlow = "9\nCD002\nno\n"; // لا يتم الدفع
        int exitCode = runMemberWithInput(inputFlow);
        assertEquals(0, exitCode);

        int remaining = Member.getOutstandingFineFromFile("testuser");
        assertEquals(50, remaining, "Remaining fine should remain 50 since no payment was made");
    }

    @Test
    void testPayFinesInvalidAmounts() throws IOException {
        writeFine("testuser", 50);

        String inputFlow = "5\nyes\n100\n"; // محاولة دفع أكثر من الغرامة
        int exitCode = runMemberWithInput(inputFlow);
        assertEquals(0, exitCode);

        int remaining = Member.getOutstandingFineFromFile("testuser");
        assertEquals(50, remaining, "Remaining fine should remain 50 since overpayment is not allowed");
    }
    @Test
    void testHandleNonNumericOptionAndInvalidNumber() {
        // خيار غير رقمي
        assertEquals(0, runMemberWithInput("abc\n"));

        // خيار رقمي غير موجود
        assertEquals(0, runMemberWithInput("99\n"));
    }

    @Test
    void testPayFinesZeroOrNegativeAmount() throws IOException {
        writeFine("testuser", 50);

        // محاولة دفع صفر
        String inputZero = "5\n0\n";
        assertEquals(0, runMemberWithInput(inputZero));
        assertEquals(50, Member.getOutstandingFineFromFile("testuser"));

        // محاولة دفع قيمة سالبة
        String inputNegative = "5\n-10\n";
        assertEquals(0, runMemberWithInput(inputNegative));
        assertEquals(50, Member.getOutstandingFineFromFile("testuser"));
    }

    @Test
    void testReturnCDPayFineZeroOrOverOutstanding() throws IOException {
        writeFine("testuser", 50);

        CD cd = service.findCDByIsbn("CD002");
        service.borrowCD(cd, user.getUsername());

        // محاولة دفع صفر
        String inputZero = "9\nCD002\nyes\n0\n";
        assertEquals(0, runMemberWithInput(inputZero));
        assertEquals(50, Member.getOutstandingFineFromFile("testuser"));

        // محاولة دفع قيمة أكبر من الغرامة
        String inputOver = "9\nCD002\nyes\n100\n";
        assertEquals(0, runMemberWithInput(inputOver));
        // بعد الدفع، يجب أن تبقى الغرامة الأصلية (أو تصحح لتصبح الحد الأقصى)
        int remaining = Member.getOutstandingFineFromFile("testuser");
        assertEquals(50, remaining);
    }

    @Test
    void testReturnCDInvalidAmountInput() throws IOException {
        writeFine("testuser", 50);

        CD cd = service.findCDByIsbn("CD002");
        service.borrowCD(cd, user.getUsername());

        // إدخال غير رقمي عند دفع الغرامة
        String input = "9\nCD002\nyes\nabc\n";
        assertEquals(0, runMemberWithInput(input));
        assertEquals(50, Member.getOutstandingFineFromFile("testuser"));
    }

    @Test
    void testReturnBookWithOutstandingFines() throws IOException {
        writeFine("testuser", 30);

        Book book = service.findBookByIsbn("ISBN001");
        service.borrowBook(book, user.getUsername());

        String input = "3\nISBN001\n"; // إعادة الكتاب
        assertEquals(0, runMemberWithInput(input));

        int remaining = Member.getOutstandingFineFromFile("testuser");
        assertEquals(30, remaining, "Fine should remain after returning book without paying");
    }

    @Test
    void testUpdateFineFileUserNotFound() throws IOException {
        // الملف موجود، لكن المستخدم غير موجود
        Member.updateFineFile("anotheruser", 40);
        int fine = Member.getOutstandingFineFromFile("anotheruser");
        assertEquals(40, fine);
    }

    @Test
    void testUpdateFineFileFileDoesNotExist() throws IOException {
        Files.deleteIfExists(fineFile);
        Member.updateFineFile("newuser", 25);
        int fine = Member.getOutstandingFineFromFile("newuser");
        assertEquals(25, fine);
    }

    @Test
    void testViewRemainingBooksAndCDsOverdue() {
        Book book = service.findBookByIsbn("ISBN001");
        service.borrowBook(book, user.getUsername());
        // تعديل dueDate ليصبح متأخر
        book.setDueDate(LocalDate.now().minusDays(2));

        CD cd = service.findCDByIsbn("CD001");
        service.borrowCD(cd, user.getUsername());
        cd.setDueDate(LocalDate.now().minusDays(3));

        // استدعاء الفروع التي تعرض overdue
        assertEquals(0, runMemberWithInput("6\n"));
        assertEquals(0, runMemberWithInput("11\n"));
    }

    @Test
    void testReturnCDWithoutOutstandingFines() throws IOException {
        // لا توجد غرامة مسجلة
        CD cd = service.findCDByIsbn("CD002");
        service.borrowCD(cd, user.getUsername());

        String input = "9\nCD002\n"; // مجرد إعادة بدون غرامة
        assertEquals(0, runMemberWithInput(input));
    }
    @Test
    void testBorrowBookNotAvailable() {
        Book book = service.findBookByIsbn("ISBN001");
        book.setAvailable(false); // نجعل الكتاب غير متاح
        String input = "2\nISBN001\n";
        assertEquals(0, runMemberWithInput(input));
    }

    @Test
    void testBorrowCDNotAvailable() {
        CD cd = service.findCDByIsbn("CD001");
        cd.setAvailable(false); // نجعل CD غير متاح
        String input = "8\nCD001\n";
        assertEquals(0, runMemberWithInput(input));
    }



    @Test
    void testReturnCDPayFineInvalidNumber() throws IOException {
        writeFine("testuser", 30);
        CD cd = service.findCDByIsbn("CD002");
        service.borrowCD(cd, user.getUsername());
        String input = "9\nCD002\nyes\nxyz\n"; // إدخال غير رقمي
        assertEquals(0, runMemberWithInput(input));
        assertEquals(30, Member.getOutstandingFineFromFile("testuser"));
    }



    @Test
    void testUpdateFineFileIOExceptionHandling() throws IOException {
        // نجعل الملف مجلد لكي يسبب IOException عند الكتابة
        Path folder = tempDir.resolve("fakedir");
        Files.createDirectory(folder);
        Member.fineFilePath = folder.toString(); // المسار ليس ملف
        // لا يجب أن يقذف استثناء
        Member.updateFineFile("user", 10);
    }
    @Test
    void testLogoutFailure() {
        AuthService authFail = mock(AuthService.class);
        when(authFail.logout()).thenReturn(false); // simulate logout failure

        int result = Member.handle(new Scanner("12\n"), service, authFail, user);
        assertEquals(0, result); // يجب أن ترجع 0 إذا فشل logout
    }

    @Test
    void testBorrowBookServiceReturnsFalse() {
        MediaService serviceSpy = spy(service);
        Book book = service.findBookByIsbn("ISBN001");

        doReturn(false).when(serviceSpy).borrowBook(book, user.getUsername());

        String input = "2\nISBN001\n";
        int result = Member.handle(new Scanner(input), serviceSpy, auth, user);
        assertEquals(0, result);
    }

    @Test
    void testBorrowCDServiceReturnsFalse() {
        MediaService serviceSpy = spy(service);
        CD cd = service.findCDByIsbn("CD001");

        doReturn(false).when(serviceSpy).borrowCD(cd, user.getUsername());

        String input = "8\nCD001\n";
        int result = Member.handle(new Scanner(input), serviceSpy, auth, user);
        assertEquals(0, result);
    }

    @Test
    void testReturnBookServiceReturnsFalse() {
        MediaService serviceSpy = spy(service);
        Book book = service.findBookByIsbn("ISBN001");
        service.borrowBook(book, user.getUsername());

        doReturn(false).when(serviceSpy).returnBook(book, user.getUsername());

        String input = "3\nISBN001\n";
        int result = Member.handle(new Scanner(input), serviceSpy, auth, user);
        assertEquals(0, result);
    }

    @Test
    void testReturnCDServiceReturnsFalse() {
        MediaService serviceSpy = spy(service);
        CD cd = service.findCDByIsbn("CD002");
        service.borrowCD(cd, user.getUsername());

        doReturn(false).when(serviceSpy).returnCD(cd, user.getUsername());

        String input = "9\nCD002\n";
        int result = Member.handle(new Scanner(input), serviceSpy, auth, user);
        assertEquals(0, result);
    }

    @Test
    void testReturnCDPayFineInvalidYesInput() throws IOException {
        writeFine("testuser", 50);
        CD cd = service.findCDByIsbn("CD002");
        service.borrowCD(cd, user.getUsername());

        // إدخال payNow ليس "yes" (مثلاً "maybe") يجب أن يتجاهل الدفع
        String inputFlow = "9\nCD002\nmaybe\n";
        int exitCode = runMemberWithInput(inputFlow);
        assertEquals(0, exitCode);

        int remaining = Member.getOutstandingFineFromFile("testuser");
        assertEquals(50, remaining, "Fine should remain unchanged when payNow is not 'yes'");
    }

    @Test
    void testReturnBookActiveRecordButBookMissing() {
        // محاكاة وجود borrow record لكتاب غير موجود في النظام
        BorrowRecord record = new BorrowRecord("MISSING_ISBN", "testuser", LocalDate.now().plusDays(5));
        service.getActiveBorrowRecordsForUser(user.getUsername()).add(record);
        service.getActiveBorrowRecordsForUser(user.getUsername()).add(record);

        assertEquals(0, runMemberWithInput("6\n")); // viewRemainingBooks يجب أن تتجاهل null
    }

    @Test
    void testReturnCDNoOutstandingFine() {
        CD cd = service.findCDByIsbn("CD001");
        service.borrowCD(cd, user.getUsername());
        // إزالة أي غرامة
        assertEquals(0, Member.getOutstandingFineFromFile(user.getUsername()));

        String input = "9\nCD001\n";
        assertEquals(0, runMemberWithInput(input)); // يجب أن تطبع "No outstanding fines for this user."
    }

    @Test
    void testPayFinesNonNumericOverMax() throws IOException {
        writeFine("testuser", 50);
        String input = "5\nabc\n100\n"; // أول إدخال غير رقمي ثم رقم أكبر من الغرامة
        assertEquals(0, runMemberWithInput(input));
        assertEquals(50, Member.getOutstandingFineFromFile("testuser"));
    }

    @Test
    void testSwitchDefaultNegativeOption() {
        assertEquals(0, runMemberWithInput("-5\n"));
    }

}
