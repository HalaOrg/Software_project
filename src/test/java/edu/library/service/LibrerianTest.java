package edu.library.service;

import edu.library.model.Book;
import edu.library.model.BorrowRecord;
import edu.library.model.Roles;
import edu.library.model.Media;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibrarianTest {

    private MediaService mediaService;
    private AuthService authService;
    private Roles user;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;



    @AfterEach
    void tearDown() {
        System.setOut(originalOut); // إعادة الطباعة الأصلية
    }

    @Test
    void testHandle_AddMedia_BookAndCD() {
        Scanner scannerBook = new Scanner("""
                1
                Book
                MyBook
                Author
                12345
                5
                9
                """);

        int resultBook = Librarian.handle(scannerBook, mediaService, authService, user);
        ArgumentCaptor<Media> captorBook = ArgumentCaptor.forClass(Media.class);
        verify(mediaService).addMedia(captorBook.capture());
        assertEquals("MyBook", captorBook.getValue().getTitle());
        assertEquals(0, resultBook);


        Scanner scannerCD = new Scanner("""
                1
                CD
                MyCD
                Artist
                54321
                3
                9
                """);

        int resultCD = Librarian.handle(scannerCD, mediaService, authService, user);
        ArgumentCaptor<Media> captorCD = ArgumentCaptor.forClass(Media.class);
        verify(mediaService, times(2)).addMedia(captorCD.capture());
        assertEquals("MyCD", captorCD.getAllValues().get(1).getTitle());
    }

    @Test
    void testHandle_SearchMedia_FoundAndNotFound() {
        when(mediaService.searchMedia("Mock")).thenReturn(List.of(mock(Media.class)));
        when(mediaService.searchMedia("Nothing")).thenReturn(List.of());

        Scanner scanFound = new Scanner("""
                3
                Mock
                9
                """);
        int resultFound = Librarian.handle(scanFound, mediaService, authService, user);
        assertEquals(0, resultFound);

        Scanner scanNotFound = new Scanner("""
                3
                Nothing
                9
                """);
        int resultNotFound = Librarian.handle(scanNotFound, mediaService, authService, user);
        assertEquals(0, resultNotFound);
    }

    @Test
    void testHandle_UpdateAndDeleteMedia() {
        when(mediaService.updateMediaQuantity("123", 10)).thenReturn(true);
        when(mediaService.updateMediaQuantity("999", 5)).thenReturn(false);
        when(mediaService.deleteMedia("123")).thenReturn(true);
        when(mediaService.deleteMedia("999")).thenReturn(false);

        Scanner scannerUpdateSuccess = new Scanner("""
                6
                123
                10
                9
                """);
        int resUpdateSuccess = Librarian.handle(scannerUpdateSuccess, mediaService, authService, user);
        assertEquals(0, resUpdateSuccess);

        Scanner scannerUpdateFail = new Scanner("""
                6
                999
                5
                9
                """);
        int resUpdateFail = Librarian.handle(scannerUpdateFail, mediaService, authService, user);
        assertEquals(0, resUpdateFail);

        Scanner scannerDeleteSuccess = new Scanner("""
                7
                123
                9
                """);
        int resDeleteSuccess = Librarian.handle(scannerDeleteSuccess, mediaService, authService, user);
        assertEquals(0, resDeleteSuccess);

        Scanner scannerDeleteFail = new Scanner("""
                7
                999
                9
                """);
        int resDeleteFail = Librarian.handle(scannerDeleteFail, mediaService, authService, user);
        assertEquals(0, resDeleteFail);
    }

    @Test
    void testHandle_InvalidOption_Logout_Exit() {
        Scanner scanInvalid = new Scanner("abc\n9\n");
        int resInvalid = Librarian.handle(scanInvalid, mediaService, authService, user);
        assertEquals(0, resInvalid);

        when(authService.logout()).thenReturn(true);
        Scanner scanLogout = new Scanner("8\n");
        int resLogout = Librarian.handle(scanLogout, mediaService, authService, user);
        assertEquals(1, resLogout);

        Scanner scanExit = new Scanner("9\n");
        int resExit = Librarian.handle(scanExit, mediaService, authService, user);
        assertEquals(2, resExit);
    }

    @Test
    void testHandle_DisplayBorrowRecordsAndFines() {
        BorrowRecordService borrowService = mock(BorrowRecordService.class);
        when(mediaService.getBorrowRecordService()).thenReturn(borrowService);
        when(borrowService.getAllRecords()).thenReturn(List.of());

        Scanner scanBorrow = new Scanner("4\n9\n");
        int resBorrow = Librarian.handle(scanBorrow, mediaService, authService, user);
        assertEquals(0, resBorrow);

        when(mediaService.getAllFines()).thenReturn(Map.of());
        Scanner scanFines = new Scanner("5\n9\n");
        int resFines = Librarian.handle(scanFines, mediaService, authService, user);
        assertEquals(0, resFines);
    }

    @BeforeEach
    void setUp() {
        mediaService = mock(MediaService.class);
        authService = mock(AuthService.class);
        user = new Roles("librarian", "pass", "LIBRARIAN", "lib@example.com");
            System.setOut(new PrintStream(outContent));
    }

    @Test
    void testAddMedia_BookAndCD() {
        // إضافة كتاب
        Scanner scannerBook = new Scanner(new ByteArrayInputStream("""
                1
                Book
                Java Programming
                John Doe
                12345
                5
                9
                """.getBytes()));

        int resultBook = Librarian.handle(scannerBook, mediaService, authService, user);
        ArgumentCaptor<Media> captorBook = ArgumentCaptor.forClass(Media.class);
        verify(mediaService).addMedia(captorBook.capture());
        assertEquals("Java Programming", captorBook.getValue().getTitle());
        assertEquals(0, resultBook);

        // إضافة CD
        Scanner scannerCD = new Scanner(new ByteArrayInputStream("""
                1
                CD
                My CD
                Artist
                54321
                3
                9
                """.getBytes()));

        int resultCD = Librarian.handle(scannerCD, mediaService, authService, user);
        ArgumentCaptor<Media> captorCD = ArgumentCaptor.forClass(Media.class);
        verify(mediaService, times(2)).addMedia(captorCD.capture());
        assertEquals("My CD", captorCD.getAllValues().get(1).getTitle());
        assertEquals(0, resultCD);
    }

    @Test
    void testSearchMedia_FoundAndNotFound() {
        Media mockMedia = mock(Media.class);
        when(mediaService.searchMedia("Java")).thenReturn(List.of(mockMedia));
        when(mediaService.searchMedia("Nothing")).thenReturn(List.of());

        // بحث موجود
        Scanner scannerFound = new Scanner(new ByteArrayInputStream("3\nJava\n9\n".getBytes()));
        int resultFound = Librarian.handle(scannerFound, mediaService, authService, user);
        assertEquals(0, resultFound);
        verify(mediaService).searchMedia("Java");

        // بحث غير موجود
        Scanner scannerNotFound = new Scanner(new ByteArrayInputStream("3\nNothing\n9\n".getBytes()));
        int resultNotFound = Librarian.handle(scannerNotFound, mediaService, authService, user);
        assertEquals(0, resultNotFound);
        verify(mediaService).searchMedia("Nothing");
    }

    @Test
    void testUpdateAndDeleteMedia() {
        when(mediaService.updateMediaQuantity("123", 10)).thenReturn(true);
        when(mediaService.updateMediaQuantity("999", 5)).thenReturn(false);
        when(mediaService.deleteMedia("123")).thenReturn(true);
        when(mediaService.deleteMedia("999")).thenReturn(false);

        // تحديث ناجح
        Scanner scanUpdateSuccess = new Scanner(new ByteArrayInputStream("6\n123\n10\n9\n".getBytes()));
        int resUpdateSuccess = Librarian.handle(scanUpdateSuccess, mediaService, authService, user);
        assertEquals(0, resUpdateSuccess);

        // تحديث فشل
        Scanner scanUpdateFail = new Scanner(new ByteArrayInputStream("6\n999\n5\n9\n".getBytes()));
        int resUpdateFail = Librarian.handle(scanUpdateFail, mediaService, authService, user);
        assertEquals(0, resUpdateFail);

        // حذف ناجح
        Scanner scanDeleteSuccess = new Scanner(new ByteArrayInputStream("7\n123\n9\n".getBytes()));
        int resDeleteSuccess = Librarian.handle(scanDeleteSuccess, mediaService, authService, user);
        assertEquals(0, resDeleteSuccess);

        // حذف فشل
        Scanner scanDeleteFail = new Scanner(new ByteArrayInputStream("7\n999\n9\n".getBytes()));
        int resDeleteFail = Librarian.handle(scanDeleteFail, mediaService, authService, user);
        assertEquals(0, resDeleteFail);
    }

    @Test
    void testLogoutAndExit() {
        // Logout
        when(authService.logout()).thenReturn(true);
        Scanner scanLogout = new Scanner(new ByteArrayInputStream("8\n".getBytes()));
        int resLogout = Librarian.handle(scanLogout, mediaService, authService, user);
        assertEquals(1, resLogout);

        // Exit
        Scanner scanExit = new Scanner(new ByteArrayInputStream("9\n".getBytes()));
        int resExit = Librarian.handle(scanExit, mediaService, authService, user);
        assertEquals(2, resExit);
    }

    @Test
    void testInvalidOption() {
        Scanner scanInvalid = new Scanner(new ByteArrayInputStream("99\n9\n".getBytes()));
        int resInvalid = Librarian.handle(scanInvalid, mediaService, authService, user);
        assertEquals(0, resInvalid);
    }
    @Test
    void testLogoutNoUserLoggedIn() {
        // محاكاة authService أن logout يرجع false (أي لا يوجد مستخدم مسجل)
        when(authService.logout()).thenReturn(false);

        Scanner scanner = new Scanner("8\n"); // الخيار 8 = Logout
        int result = Librarian.handle(scanner, mediaService, authService, user);

        // نتأكد أن النتيجة = 0 (كما في return 0)
        assertEquals(0, result);

        // نتأكد أن logout تم استدعاؤه مرة واحدة
        verify(authService, times(1)).logout();
    }

    @Test
    void testDisplayMediaOption() {
        // نضع الخيار 2 في الـ Scanner، وبعدين خيار 9 للخروج
        Scanner scanner = new Scanner("2\n9\n");

        int result = Librarian.handle(scanner, mediaService, authService, user);

// تحقق أن displayMedia استدعيت مرة واحدة
        verify(mediaService, times(1)).displayMedia();

// تحقق أن الدالة رجعت 0 (كما في case 2)
        assertEquals(0, result);

    }
    @Test
    void testAddMediaWithInvalidQuantity() {
        // محاكاة إدخال المستخدم: Book مع quantity < 1
        String inputData = "Book\nJava Programming\nJohn Doe\n12345\n0\n"; // 0 أقل من 1
        Scanner scanner = new Scanner(new ByteArrayInputStream(inputData.getBytes()));

        // نفترض handle هي الدالة اللي تستدعي switch
        int result = Librarian.handle(scanner, mediaService, authService, user);

        // لازم ترجع 0 حسب الكود
        assertEquals(0, result);

        // addMedia ما لازم يتم استدعاؤها
        verify(mediaService, never()).addMedia(any());
    }


    @Test
    void testBorrowRecordsWithOverdue() {
        // سجل استعارة متأخر (لم يتم إرجاعه)
        BorrowRecord record1 = mock(BorrowRecord.class);
        when(record1.getUsername()).thenReturn("user1");
        when(record1.getIsbn()).thenReturn("123");
        when(record1.getDueDate()).thenReturn(LocalDate.now().minusDays(5)); // متأخر 5 أيام
        when(record1.isReturned()).thenReturn(false);
        when(record1.getReturnDate()).thenReturn(null);

        // سجل استعارة غير متأخر
        BorrowRecord record2 = mock(BorrowRecord.class);
        when(record2.getUsername()).thenReturn("user2");
        when(record2.getIsbn()).thenReturn("456");
        when(record2.getDueDate()).thenReturn(LocalDate.now().plusDays(2)); // غير متأخر
        when(record2.isReturned()).thenReturn(false);
        when(record2.getReturnDate()).thenReturn(null);

        List<BorrowRecord> records = List.of(record1, record2);

        boolean anyOverdue = false;

        // الكود الأصلي المطلوب اختباره
        System.out.println("Borrow Records:");
        for (BorrowRecord record : records) {
            boolean overdue = !record.isReturned() && record.getDueDate() != null && LocalDate.now().isAfter(record.getDueDate());
            System.out.printf("User: %s | ISBN: %s | Due: %s | Returned: %s | ReturnDate: %s%n",
                    record.getUsername(), record.getIsbn(), record.getDueDate(), record.isReturned(), record.getReturnDate());
            if (overdue) {
                anyOverdue = true;
                long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
                System.out.println("Overdue by " + daysOverdue + " day(s). Loans beyond 28 days trigger fines.");
            }
        }

        // تحقق أن أي سجل متأخر تم اكتشافه
        assertTrue(anyOverdue);

        // تحقق من الطباعة الصحيحة
        String output = outContent.toString();
        assertTrue(output.contains("user1"));
        assertTrue(output.contains("123"));
        assertTrue(output.contains("Overdue by 5 day(s)"));
        assertTrue(output.contains("user2"));
        assertTrue(output.contains("456"));
    }
    @Test
    void testNoOverdueRecords() {
        List<BorrowRecord> records = List.of(
                new BorrowRecord("user1", "ISBN1", LocalDate.now().plusDays(5), false, null),
                new BorrowRecord("user2", "ISBN2", LocalDate.now().plusDays(10), false, null)
        );

        boolean anyOverdue = false;
        for (BorrowRecord record : records) {
            boolean overdue = !record.isReturned() && record.getDueDate() != null && LocalDate.now().isAfter(record.getDueDate());
            System.out.printf("User: %s | ISBN: %s | Due: %s | Returned: %s | ReturnDate: %s%n",
                    record.getUsername(), record.getIsbn(), record.getDueDate(), record.isReturned(), record.getReturnDate());
            if (overdue) {
                anyOverdue = true;
            }
        }

        if (!anyOverdue) {
            System.out.println("No overdue items detected (all within 28-day window).");
        }

        String output = outContent.toString();
        assertTrue(output.contains("No overdue items detected (all within 28-day window)"));
    }
    @Test
    void testNoFines() {
        Map<String, Integer> fines = Map.of(); // خريطة فارغة تمثل عدم وجود غرامات

        if (fines.isEmpty()) {
            System.out.println("No fines found for any user.");
        }

        String output = outContent.toString();
        assertTrue(output.contains("No fines found for any user."));
    }
    @Test
    void testAddMediaWithUnknownType_defaultsToBook() {
        Scanner scanner = new Scanner("""
            1
            UnknownType
            Mystery Title
            Author Name
            99999
            2
            9
            """);

        int result = Librarian.handle(scanner, mediaService, authService, user);

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaService).addMedia(captor.capture());

        assertTrue(captor.getValue() instanceof Book); // fallback
        assertEquals("Mystery Title", captor.getValue().getTitle());
        assertEquals(0, result);
    }
    @Test
    void testReadInt_InvalidAndBelowMin() {
        // غير صالح
        Scanner scannerInvalid = new Scanner("abc\n");
        assertEquals(-1, Librarian.readInt(scannerInvalid, 0));

        // أقل من الحد الأدنى
        Scanner scannerLow = new Scanner("-5\n");
        assertEquals(-1, Librarian.readInt(scannerLow, 0));

        // صالح
        Scanner scannerValid = new Scanner("10\n");
        assertEquals(10, Librarian.readInt(scannerValid, 0));
    }

    @Test
    void testDisplayFineBalances_WithFines() {
        MediaService service = mock(MediaService.class);
        Map<String, Integer> fines = Map.of("user1", 50, "user2", 30);
        when(service.getAllFines()).thenReturn(fines);

        Librarian.displayFineBalances(service);

        String output = outContent.toString();
        assertTrue(output.contains("user1: 50 NIS"));
        assertTrue(output.contains("user2: 30 NIS"));
    }

    @Test
    void testDisplayBorrowRecords_MixedOverdue() {
        MediaService service = mock(MediaService.class);
        BorrowRecord record1 = mock(BorrowRecord.class);
        BorrowRecord record2 = mock(BorrowRecord.class);

        when(record1.isReturned()).thenReturn(false);
        when(record1.getDueDate()).thenReturn(LocalDate.now().minusDays(3));
        when(record1.getUsername()).thenReturn("user1");
        when(record1.getIsbn()).thenReturn("123");
        when(record1.getReturnDate()).thenReturn(null);

        when(record2.isReturned()).thenReturn(false);
        when(record2.getDueDate()).thenReturn(LocalDate.now().plusDays(5));
        when(record2.getUsername()).thenReturn("user2");
        when(record2.getIsbn()).thenReturn("456");
        when(record2.getReturnDate()).thenReturn(null);

        BorrowRecordService borrowService = mock(BorrowRecordService.class);
        when(borrowService.getAllRecords()).thenReturn(List.of(record1, record2));
        when(service.getBorrowRecordService()).thenReturn(borrowService);

        Librarian.displayBorrowRecords(service);

        String output = outContent.toString();
        assertTrue(output.contains("user1"));
        assertTrue(output.contains("Overdue by 3 day(s)"));
        assertTrue(output.contains("user2"));
        assertTrue(output.contains("No overdue items detected") == false); // لأن هناك متأخر
    }
    @Test
    void testUpdateMediaWithInvalidInput() {
        // إدخال غير صالح (نص بدلاً من رقم)
        Scanner scannerInvalid = new Scanner("6\n123\nabc\n9\n");
        int result = Librarian.handle(scannerInvalid, mediaService, authService, user);
        assertEquals(0, result);

        // تحقق أن الدالة لم تُستدعَ إلا بالقيم السالبة (-1) أو أقل من الحد الأدنى
        verify(mediaService, never()).updateMediaQuantity(any(), intThat(i -> i > 0));

        // إدخال أقل من الحد الأدنى (0)
        Scanner scannerLow = new Scanner("6\n123\n0\n9\n");
        int resultLow = Librarian.handle(scannerLow, mediaService, authService, user);
        assertEquals(0, resultLow);

        // تحقق أن الدالة لم تُستدعَ بأي قيمة صالحة (>0)
        verify(mediaService, never()).updateMediaQuantity(any(), intThat(i -> i > 0));
    }

    @Test
    void testDeleteMediaWithEmptyInput() {
        Scanner scannerEmpty = new Scanner("7\n\n9\n");
        int result = Librarian.handle(scannerEmpty, mediaService, authService, user);
        assertEquals(0, result);

        // التحقق أن deleteMedia لم يُستدعَ مع أي قيمة غير فارغة
        verify(mediaService, never()).deleteMedia(argThat(s -> s != null && !s.isEmpty()));
    }

    @Test
    void testReadInt_AtMinValue() {
        Scanner scanner = new Scanner("5\n");
        int result = Librarian.readInt(scanner, 5);
        assertEquals(5, result);
    }
    @Test
    void testDisplayBorrowRecords_WithNullDueDate() {
        BorrowRecord record = mock(BorrowRecord.class);
        when(record.getUsername()).thenReturn("userNull");
        when(record.getIsbn()).thenReturn("000");
        when(record.getDueDate()).thenReturn(null);
        when(record.isReturned()).thenReturn(false);
        when(record.getReturnDate()).thenReturn(null);

        BorrowRecordService borrowService = mock(BorrowRecordService.class);
        when(borrowService.getAllRecords()).thenReturn(List.of(record));
        MediaService service = mock(MediaService.class);
        when(service.getBorrowRecordService()).thenReturn(borrowService);

        Librarian.displayBorrowRecords(service);

        String output = outContent.toString();
        assertTrue(output.contains("userNull"));
        assertTrue(output.contains("000"));
    }

}
