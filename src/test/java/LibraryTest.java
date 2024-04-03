import entities.Book;
import entities.Borrow;
import entities.Card;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import queries.*;
import utils.ConnectConfig;
import utils.DatabaseConnector;
import utils.RandomData;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LibraryTest {

    private DatabaseConnector connector;
    private LibraryManagementSystem library;

    private static ConnectConfig connectConfig = null;

    static {
        try {
            // parse connection config from "resources/application.yaml"
            connectConfig = new ConnectConfig();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public LibraryTest() {
        try {
            // connect to database
            connector = new DatabaseConnector(connectConfig);
            library = new LibraryManagementSystemImpl(connector);
            System.out.println("Successfully init class BookTest.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Before
    public void prepareTest() {
        boolean connStatus = connector.connect();
        Assert.assertTrue(connStatus);
        System.out.println("Successfully connect to database.");
        ApiResult result = library.resetDatabase();
        if (!result.ok) {
            System.out.printf("Failed to reset database, reason: %s\n", result.message);
            Assert.fail();
        }
        System.out.println("Successfully reset database.");
    }

    @After
    public void afterTest() {
        boolean releaseStatus = connector.release();
        if (releaseStatus) {
            System.out.println("Successfully release database connection.");
        } else {
            System.out.println("Failed to release database connection.");
        }
    }

    @Test
    public void bookRegisterTest() {
        Book b0 = new Book("Computer Science", "Database System Concepts",
                "Machine Industry Press", 2023, "Mike", 188.88, 10);
        Assert.assertTrue(library.storeBook(b0).ok);
        /* Not allowed to create duplicated records */
        Book b1 = new Book("Computer Science", "Database System Concepts",
                "Machine Industry Press", 2023, "Mike", 188.88, 5);
        Book b2 = new Book("Computer Science", "Database System Concepts",
                "Machine Industry Press", 2023, "Mike", 99.99, 10);
        Assert.assertFalse(library.storeBook(b1).ok);
        Assert.assertFalse(library.storeBook(b2).ok);
        /* check equal function */
        // records all books generated by test case, include duplicate books.
        List<Book> originBookList = new ArrayList<Book>() {{
            add(b0);
            add(b1);
            add(b2);
        }};
        // records which books should exist in database
        Set<Book> actualBookList = new HashSet<Book>() {{
            add(b0); // b0 already inserted to database
        }};
        // corresponding to originBookList, mark whether a book is duplicated
        List<Boolean> bookValid = new ArrayList<Boolean>() {{
            // b0 already inserted to database, so bookValid[0] is false
            add(false);
            add(false);
            add(false);
        }};
        for (int i = 0; i < originBookList.size(); i++) {
            if (actualBookList.contains(originBookList.get(i))) {
                Assert.assertFalse(bookValid.get(i));
            } else {
                actualBookList.add(originBookList.get(i));
                System.out.println(originBookList.get(i));
                Assert.assertTrue(bookValid.get(i));
            }
        }
        Assert.assertEquals(1, actualBookList.size());
        /* generate some books */
        for (int i = 0; i < 50; i++) {
            Book b = RandomData.randomBook();
            originBookList.add(b);
            if (actualBookList.contains(b)) {
                bookValid.add(false);
            } else {
                actualBookList.add(b);
                bookValid.add(true);
            }
        }
        Assert.assertEquals(originBookList.size(), bookValid.size());
        Assert.assertTrue(originBookList.size() > actualBookList.size());
        /* generate some duplicate books */
        for (int i = 0; i < 10; i++) {
            int dupIndex = RandomUtils.nextInt(0, originBookList.size());
            Book ob = originBookList.get(dupIndex);
            if (bookValid.get(i)) {
                Assert.assertTrue(actualBookList.contains(ob));
                Book cb = ob.clone();
                // randomly change some attributes
                if (RandomUtils.nextBoolean()) {
                    cb.setStock(RandomData.randomStock());
                    cb.setPrice(RandomData.randomPrice());
                }
                Assert.assertTrue(actualBookList.contains(cb));
                originBookList.add(cb);
                bookValid.add(false);
            }
        }
        Assert.assertEquals(originBookList.size(), bookValid.size());
        Assert.assertTrue(originBookList.size() > actualBookList.size());
        /* bulk load these books */
        for (int i = 0; i < originBookList.size(); i++) {
            Book book = originBookList.get(i);
            if (bookValid.get(i)) {
                Assert.assertTrue(library.storeBook(book).ok);
            } else {
                Assert.assertFalse(library.storeBook(book).ok);
            }
        }
        /* use query interface to check correctness */
        ApiResult queryResult = library.queryBook(new BookQueryConditions());
        Assert.assertTrue(queryResult.ok);
        // parse query results from payload
        BookQueryResults selectedResults = (BookQueryResults) queryResult.payload;
        Assert.assertEquals(selectedResults.getCount(), selectedResults.getResults().size());
        // sort actual book list by its PK
        List<Book> compareBooks = new ArrayList<>(actualBookList);
        compareBooks.sort(Comparator.comparingInt(Book::getBookId));
        Assert.assertEquals(compareBooks.size(), selectedResults.getCount());
        for (int i = 0; i < compareBooks.size(); i++) {
            Book o1 = compareBooks.get(i);
            Book o2 = selectedResults.getResults().get(i);
            Assert.assertEquals(o1.toString(), o2.toString());
        }
    }

    @Test
    public void incBookStockTest() {
        /* simply insert some books to database */
        Set<Book> bookSet = new HashSet<>();
        Set<Integer> bookIds = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            bookSet.add(RandomData.randomBook());
        }
        for (Book book : bookSet) {
            Assert.assertTrue(library.storeBook(book).ok);
            bookIds.add(book.getBookId());
        }
        Assert.assertEquals(bookSet.size(), bookIds.size());
        /* corner case: invalid book id */
        Assert.assertFalse(library.incBookStock(-1, 6).ok);
        int k = bookSet.size() + 1;
        while (bookIds.contains(k)) {
            ++k;
        }
        Assert.assertFalse(library.incBookStock(k, 10).ok);
        /* corner case: invalid book stock */
        List<Book> bookList = new ArrayList<>(bookSet);
        Book b0 = bookList.get(0);
        Assert.assertTrue(library.incBookStock(b0.getBookId(), -b0.getStock()).ok); // stock = 0
        Assert.assertTrue(library.incBookStock(b0.getBookId(), 1).ok);  // stock = 1
        Assert.assertFalse(library.incBookStock(b0.getBookId(), -2).ok); // stock = -1
        b0.setStock(1);
        /* randomly choose some books to do this operation */
        int nOps = 1000;
        for (int i = 0; i < nOps; i++) {
            Book book = bookList.get(new Random().nextInt(bookList.size()));
            int deltaStock = RandomUtils.nextInt(0, 24) - 8;
            if (book.getStock() + deltaStock >= 0) {
                Assert.assertTrue(library.incBookStock(book.getBookId(), deltaStock).ok);
                book.setStock(book.getStock() + deltaStock);
            } else {
                Assert.assertFalse(library.incBookStock(book.getBookId(), deltaStock).ok);
            }
        }
        /* use query interface to check correctness */
        bookList.sort(Comparator.comparingInt(Book::getBookId));
        ApiResult queryResult = library.queryBook(new BookQueryConditions());
        Assert.assertTrue(queryResult.ok);
        BookQueryResults selectedResults = (BookQueryResults) queryResult.payload;
        for (int i = 0; i < bookList.size(); i++) {
            Book o1 = bookList.get(i);
            Book o2 = selectedResults.getResults().get(i);
            Assert.assertEquals(o1.toString(), o2.toString());
        }
    }

    @Test
    public void bulkRegisterBookTest() {
        /* simply insert some books to database */
        int nOps = 1000;
        Set<Book> bookSet = new HashSet<>();
        for (int i = 0; i < nOps; i++) {
            bookSet.add(RandomData.randomBook());
        }
        /* provide some duplicate records */
        List<Book> bookList1 = new ArrayList<>(bookSet);
        for (int i = 0; i < 3; i++) {
            Book cb = bookList1.get(new Random().nextInt(bookList1.size())).clone();
            // randomly change some attributes
            if (RandomUtils.nextBoolean()) {
                cb.setStock(RandomUtils.nextInt(0, 20));
                cb.setPrice(RandomUtils.nextDouble(6.66, 233.33));
            }
            bookList1.add(cb);
        }
        Collections.shuffle(bookList1);
        Assert.assertFalse(library.storeBook(bookList1).ok);
        /* make sure that none of the books are inserted */
        ApiResult queryResult1 = library.queryBook(new BookQueryConditions());
        Assert.assertTrue(queryResult1.ok);
        BookQueryResults selectedResults1 = (BookQueryResults) queryResult1.payload;
        Assert.assertEquals(0, selectedResults1.getCount());
        /* normal batch insert */
        List<Book> bookList2 = new ArrayList<>(bookSet);
        Assert.assertTrue(library.storeBook(bookList2).ok);
        ApiResult queryResult2 = library.queryBook(new BookQueryConditions());
        Assert.assertTrue(queryResult2.ok);
        BookQueryResults selectedResults2 = (BookQueryResults) queryResult2.payload;
        Assert.assertEquals(bookList2.size(), selectedResults2.getCount());
        bookList2.sort(Comparator.comparingInt(Book::getBookId));
        for (int i = 0; i < bookList2.size(); i++) {
            Book o1 = bookList2.get(i);
            Book o2 = selectedResults2.getResults().get(i);
            Assert.assertEquals(o1.toString(), o2.toString());
        }
    }

    @Test
    public void removeBookTest() {
        /* simply insert some data to database */
        MyLibrary my = MyLibrary.createLibrary(library, 100, 1, 0);
        /* remove a non-exist book */
        Assert.assertFalse(library.removeBook(-1).ok);
        //Assert.assertTrue(library.removeBook(12).ok);
        /* remove a book that someone has not returned yet */
        Borrow borrow = new Borrow(my.books.get(0), my.cards.get(0));
        borrow.resetBorrowTime();
        Assert.assertTrue(library.borrowBook(borrow).ok);
        Assert.assertFalse(library.removeBook(my.books.get(0).getBookId()).ok);
        borrow.resetReturnTime();
        Assert.assertTrue(library.returnBook(borrow).ok);
        Assert.assertTrue(library.removeBook(my.books.get(0).getBookId()).ok);
        /* remove a non-exist book */
        Assert.assertFalse(library.removeBook(my.books.get(0).getBookId()).ok);
        my.books.remove(0);
        /* randomly choose nRemove books to remove */
        int nRemove = RandomUtils.nextInt(10, 50);
        Collections.shuffle(my.books);
        for (int i = 0; i < nRemove; i++) {
            Assert.assertTrue(library.removeBook(my.books.get(0).getBookId()).ok);
            /* remove a non-exist book */
            Assert.assertFalse(library.removeBook(my.books.get(0).getBookId()).ok);
            my.books.remove(0);
        }
        /* compare results */
        ApiResult queryResult1 = library.queryBook(new BookQueryConditions());
        Assert.assertTrue(queryResult1.ok);
        BookQueryResults selectedResults1 = (BookQueryResults) queryResult1.payload;
        Assert.assertEquals(my.books.size(), selectedResults1.getCount());
        my.books.sort(Comparator.comparingInt(Book::getBookId));
        for (int i = 0; i < my.books.size(); i++) {
            Book o1 = my.books.get(i);
            Book o2 = selectedResults1.getResults().get(i);
            Assert.assertEquals(o1.toString(), o2.toString());
        }
    }

    @Test
    public void modifyBookTest() {
        /* simply insert some books to database */
        Set<Book> bookSet = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            bookSet.add(RandomData.randomBook());
        }
        List<Book> bookList = new ArrayList<>(bookSet);
        for (Book book : bookList) {
            Assert.assertTrue(library.storeBook(book).ok);
        }
        /* randomly change books */
        for (Book book : bookList) {
            // remove old book from book set
            Assert.assertTrue(bookSet.remove(book));
            int oldStock = book.getStock(); // book's stock cannot be changed by modifyBookInfo
            do {  // make sure the new book does not exist in database
                // use bit mask to determine which field to update
                int mask = RandomUtils.nextInt(0, 128);
                if ((mask & 1) > 0) {
                    book.setCategory(RandomData.randomCategory());
                }
                if ((mask & 2) > 0) {
                    book.setTitle(RandomData.randomTitle());
                }
                if ((mask & 4) > 0) {
                    book.setPress(RandomData.randomPress());
                }
                if ((mask & 8) > 0) {
                    book.setPublishYear(RandomData.randomPublishYear());
                }
                if ((mask & 16) > 0) {
                    book.setAuthor(RandomData.randomAuthor());
                }
                if ((mask & 32) > 0) {
                    book.setPrice(RandomData.randomPrice());
                }
                if ((mask & 64) > 0) {
                    book.setStock(RandomData.randomStock());
                }
            } while (bookSet.contains(book));
            // insert new book to book set
            bookSet.add(book);
            Assert.assertTrue(library.modifyBookInfo(book).ok);
            book.setStock(oldStock);
        }
        /* compare results */
        ApiResult queryResult = library.queryBook(new BookQueryConditions());
        Assert.assertTrue(queryResult.ok);
        BookQueryResults selectedResults = (BookQueryResults) queryResult.payload;
        Assert.assertEquals(bookList.size(), selectedResults.getCount());
        bookList = new ArrayList<>(bookSet);
        bookList.sort(Comparator.comparingInt(Book::getBookId));
        for (int i = 0; i < bookList.size(); i++) {
            Book o1 = bookList.get(i);
            Book o2 = selectedResults.getResults().get(i);
            Assert.assertEquals(o1.toString(), o2.toString());
        }
    }

    @Test
    public void queryBookTest() {
        /* simply insert some books to database */
        MyLibrary my = MyLibrary.createLibrary(library, 1000, 0, 0);
        /* generate single query condition */
        List<BookQueryConditions> queryConditions = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            queryConditions.add(new BookQueryConditions());
        }
        queryConditions.get(0).setCategory(RandomData.randomCategory());
        queryConditions.get(1).setTitle(RandomData.randomTitle());
        queryConditions.get(2).setPress("Press");   // test fuzzy matching
        queryConditions.get(3).setPress(RandomData.randomPress());
        queryConditions.get(4).setMinPrice(RandomData.randomPrice());
        queryConditions.get(5).setMaxPrice(RandomData.randomPrice());
        queryConditions.get(6).setMinPrice(20.23);
        queryConditions.get(6).setMaxPrice(52.42);
        queryConditions.get(7).setMinPublishYear(2008);
        queryConditions.get(8).setMaxPublishYear(2020);
        {
            int minY = RandomUtils.nextInt(2000, 2015);
            int maxY = Math.max(RandomUtils.nextInt(2007, 2024), minY);
            queryConditions.get(9).setMinPublishYear(minY);
            queryConditions.get(9).setMaxPublishYear(maxY);
        }
        queryConditions.get(10).setAuthor("o");   // test fuzzy matching
        queryConditions.get(11).setAuthor(RandomData.randomAuthor());
        queryConditions.get(12).setSortBy(Book.SortColumn.PRICE);
        queryConditions.get(12).setSortOrder(SortOrder.ASC);
        queryConditions.get(13).setSortBy(Book.SortColumn.PRICE);
        queryConditions.get(13).setSortOrder(SortOrder.DESC);
        queryConditions.get(14).setSortBy(Book.SortColumn.PUBLISH_YEAR);
        queryConditions.get(14).setSortOrder(SortOrder.DESC);
        /* generate multi query conditions */
        for (int i = 0; i < 45; i++) {
            BookQueryConditions c = new BookQueryConditions();
            int mask = RandomUtils.nextInt(0, 32);
            int selected = 0;
            if ((mask & 1) > 0) {
                c.setPress(RandomData.randomPress());
                selected++;
            }
            if ((mask & 2) > 0) {
                c.setCategory(RandomData.randomCategory());
                selected++;
            }
            if ((mask & 4) > 0) {
                c.setAuthor(RandomData.randomAuthor());
                selected++;
            }
            // randomly select year
            if (RandomUtils.nextInt(1, 2 + selected) == 1) {
                int minY = RandomUtils.nextInt(2000, 2015);
                int maxY = Math.max(RandomUtils.nextInt(2007, 2024), minY + 7);
                c.setMinPublishYear(minY);
                c.setMaxPublishYear(maxY);
                selected++;
            }
            // randomly select price
            if (RandomUtils.nextInt(1, 3 + selected) == 1) {
                double minP = RandomData.randomPrice();
                double maxP = Math.max(RandomData.randomPrice(), minP + 16.66);
                c.setMinPrice(minP);
                c.setMaxPrice(maxP);
            }
            // randomly choose one column to sort
            if (RandomUtils.nextInt(1, 4) != 1) {
                c.setSortBy(Book.SortColumn.random());
                c.setSortOrder(SortOrder.random());
            }
            queryConditions.add(c);
        }
        /* loop testing */
        for (BookQueryConditions queryCondition : queryConditions) {
            ApiResult queryResult = library.queryBook(queryCondition);
            Assert.assertTrue(queryResult.ok);
            BookQueryResults bookResults = (BookQueryResults) queryResult.payload;
            List<Book> expectedResults = verifyQueryResult(my.books, queryCondition);
            Assert.assertEquals(expectedResults.size(), bookResults.getCount());
            for (int i = 0; i < expectedResults.size(); i++) {
                Book o1 = expectedResults.get(i);
                Book o2 = bookResults.getResults().get(i);
                Assert.assertEquals(o1.toString(), o2.toString());
            }
        }
    }

    @Test
    public void borrowAndReturnBookTest() {
        /* insert some books & cards & borrow histories to database */
        MyLibrary my = MyLibrary.createLibrary(library, 50, 50, 100);
        /* borrow a non-exists book */
        // book not exists
        Set<Integer> bookIds = my.books.stream().map(Book::getBookId).collect(Collectors.toSet());
        Map<Integer, Integer> stockMap = my.books.stream().collect(
                Collectors.toMap(Book::getBookId, Book::getStock));
        int nbId = RandomUtils.nextInt(0, 200);
        while (bookIds.contains(nbId)) {
            nbId = RandomUtils.nextInt(0, 200);
        }
        Set<Integer> cardIds = my.cards.stream().map(Card::getCardId).collect(Collectors.toSet());
        Borrow nb = new Borrow(nbId, my.cards.get(0).getCardId());
        nb.resetBorrowTime();
        nb.resetReturnTime();
        Assert.assertFalse(library.borrowBook(nb).ok);
        Assert.assertFalse(library.returnBook(nb).ok);
        // card not exists
        int ncId = RandomUtils.nextInt(0, 200);
        while (cardIds.contains(ncId)) {
            ncId = RandomUtils.nextInt(0, 200);
        }
        Borrow nc = new Borrow(my.books.get(0).getBookId(), ncId);
        nc.resetBorrowTime();
        nc.resetReturnTime();
        Assert.assertFalse(library.borrowBook(nc).ok);
        Assert.assertFalse(library.returnBook(nc).ok);
        // book & card both not exist
        Borrow nbc = new Borrow(nbId, ncId);
        nbc.resetBorrowTime();
        Assert.assertFalse(library.borrowBook(nbc).ok);
        /* borrow a book */
        Book b0 = my.books.get(RandomUtils.nextInt(0, my.nBooks()));
        Assert.assertTrue(b0.getStock() > 0);
        Card c0 = my.cards.get(RandomUtils.nextInt(0, my.nCards()));
        Borrow r0 = new Borrow(b0, c0);
        r0.resetBorrowTime();
        r0.resetReturnTime();
        Assert.assertFalse(library.returnBook(r0).ok);
        Assert.assertTrue(library.borrowBook(r0).ok);
        /* borrow it again */
        Borrow r1 = new Borrow(b0, c0);
        r1.resetBorrowTime();
        Assert.assertFalse(library.borrowBook(r1).ok);
        /* return this book */
        // corner case, for return_time > borrow_time
        Borrow nt = new Borrow(b0, c0);
        nt.setReturnTime(666);
        Assert.assertFalse(library.returnBook(nt).ok);
        nt.setReturnTime(r0.getBorrowTime());
        Assert.assertFalse(library.returnBook(nt).ok);
        // normal case
        r0.resetReturnTime();
        Assert.assertTrue(library.returnBook(r0).ok);
        my.borrows.add(r0);     // add to borrow list after operation
        /* return this book again */
        r1.resetReturnTime();
        Assert.assertFalse(library.returnBook(r1).ok);
        /* borrow & return this book */
        Borrow r2 = new Borrow(b0, c0);
        r2.resetBorrowTime();
        Assert.assertTrue(library.borrowBook(r2).ok);
        r2.resetReturnTime();
        Assert.assertTrue(library.returnBook(r2).ok);
        my.borrows.add(r2);     // add to borrow list after operation
        /* try to borrow a zero-stock book */
        Assert.assertTrue(library.incBookStock(b0.getBookId(), -b0.getStock()).ok);
        Borrow r3 = new Borrow(b0, c0);
        r3.resetBorrowTime();
        Assert.assertFalse(library.borrowBook(r3).ok);
        stockMap.put(b0.getBookId(), 0);    // now b0.stock == 0
        /* randomly borrow & return books */
        List<Borrow> borrowList = new ArrayList<>();
        Set<Pair<Integer, Integer>> borrowStatus = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            if (RandomUtils.nextBoolean() && borrowList.size() > 0) { // do return book
                int k = RandomUtils.nextInt(0, borrowList.size());
                Borrow r = borrowList.get(k);
                r.resetReturnTime();
                Pair<Integer, Integer> sp = new ImmutablePair<>(r.getBookId(), r.getCardId());
                Assert.assertTrue(library.returnBook(r).ok);
                borrowList.remove(k);
                borrowStatus.remove(sp);
                my.borrows.add(r);    // add to borrow list after operation
                stockMap.put(r.getBookId(), stockMap.get(r.getBookId()) + 1);
            } else {    // do borrow book
                Book b = my.books.get(RandomUtils.nextInt(0, my.nBooks()));
                Card c = my.cards.get(RandomUtils.nextInt(0, my.nCards()));
                Borrow r = new Borrow(b, c);
                r.resetBorrowTime();
                Pair<Integer, Integer> sp = new ImmutablePair<>(b.getBookId(), c.getCardId());
                if (borrowStatus.contains(sp) || stockMap.get(r.getBookId()) == 0) {
                    Assert.assertFalse(library.borrowBook(r).ok);
                } else {
                    Assert.assertTrue(library.borrowBook(r).ok);
                    borrowStatus.add(sp);
                    borrowList.add(r);
                    stockMap.put(r.getBookId(), stockMap.get(r.getBookId()) - 1);
                }
            }
        }
        // add un-returned books to borrow histories
        my.borrows.addAll(borrowList);
        /* compare borrow histories */
        Map<Integer, Book> bookMap = my.books.stream().collect(
                Collectors.toMap(Book::getBookId, v -> v));
        // card_id --> borrow_items
        Map<Integer, List<BorrowHistories.Item>> expectedBorrowMap = new HashMap<>();
        for (Borrow borrow : my.borrows) {
            BorrowHistories.Item item = new BorrowHistories.Item(borrow.getCardId(),
                    bookMap.get(borrow.getBookId()), borrow);
            if (!expectedBorrowMap.containsKey(borrow.getCardId())) {
                expectedBorrowMap.put(borrow.getCardId(), new ArrayList<>());
            }
            expectedBorrowMap.get(borrow.getCardId()).add(item);
        }
        for (List<BorrowHistories.Item> list : expectedBorrowMap.values()) {
            list.sort((x, y) -> {
                if (x.getBorrowTime() == y.getBorrowTime()) {
                    return x.getBookId() - y.getBookId();
                }
                return x.getBorrowTime() < y.getBorrowTime() ? 1 : -1;
            });
        }
        for (Card card : my.cards) {
            ApiResult result = library.showBorrowHistory(card.getCardId());
            Assert.assertTrue(result.ok);
            BorrowHistories histories = (BorrowHistories) result.payload;
            List<BorrowHistories.Item> expectedList = expectedBorrowMap.get(card.getCardId());
            Assert.assertEquals(expectedList.size(), histories.getCount());
            for (int i = 0; i < expectedList.size(); i++) {
                BorrowHistories.Item o1 = expectedList.get(i);
                BorrowHistories.Item o2 = histories.getItems().get(i);
                Assert.assertEquals(o1.toString(), o2.toString());
            }
        }
    }

    @Test
    public void parallelBorrowBookTest() {
        int nThreads = BorrowThread.nThreads;
        MyLibrary my = MyLibrary.createLibrary(library, 1, nThreads, 0);
        Book book = my.books.get(0);
        // let book.stock = 1
        Assert.assertTrue(library.incBookStock(book.getBookId(), -book.getStock() + 1).ok);
        /* all threads connect to database */
        List<DatabaseConnector> connectors = new ArrayList<>();
        List<LibraryManagementSystem> libraries = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            DatabaseConnector connector = new DatabaseConnector(connectConfig);
            Assert.assertTrue(connector.connect());
            connectors.add(connector);
            libraries.add(new LibraryManagementSystemImpl(connector));
        }
        /* start all threads */
        List<BorrowThread> borrowThreads = new ArrayList<>();
        BorrowThread.acquireAll();
        for (int i = 0; i < nThreads; i++) {
            Borrow borrow = new Borrow(book, my.cards.get(i));
            borrow.resetBorrowTime();
            BorrowThread thd = new BorrowThread(i, libraries.get(i), borrow);
            thd.start();
            borrowThreads.add(thd);
        }
        BorrowThread.releaseAll();
        /* wait all threads finish */
        for (int i = 0; i < nThreads; i++) {
            try {
                borrowThreads.get(i).join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /* check results */
        // only one thread can successfully borrow the book
        Assert.assertEquals(1, BorrowThread.successOps.get());
        /* release all connections */
        for (int i = 0; i < nThreads; i++) {
            Assert.assertTrue(connectors.get(i).release());
        }
    }

    @Test
    public void registerAndShowAndRemoveCardTest() {
        /* simply insert N cards */
        MyLibrary my = MyLibrary.createLibrary(library, 1, 100, 0);
        /* duplicate create */
        Card duplicateCard = my.cards.get(RandomUtils.nextInt(0, my.nCards())).clone();
        duplicateCard.setCardId(0);
        Assert.assertFalse(library.registerCard(duplicateCard).ok);
        /* delete a card that has some un-returned books */
        int delPos = RandomUtils.nextInt(0, my.nCards());
        Card delCard = my.cards.get(delPos);
        Borrow borrow = new Borrow(my.books.get(0), delCard);
        borrow.resetBorrowTime();
        Assert.assertTrue(library.borrowBook(borrow).ok);
        Assert.assertFalse(library.removeCard(delCard.getCardId()).ok);
        borrow.resetReturnTime();
        Assert.assertTrue(library.returnBook(borrow).ok);
        Assert.assertTrue(library.removeCard(delCard.getCardId()).ok);
        /* delete a non-exists card */
        Assert.assertFalse(library.removeCard(-1).ok);
        Assert.assertFalse(library.removeCard(delCard.getCardId()).ok);
        my.cards.remove(delPos);
        /* randomly delete some cards */
        Collections.shuffle(my.cards);
        for (int i = 0; i < 20; i++) {
            Card dCard = my.cards.get(0);
            Assert.assertTrue(library.removeCard(dCard.getCardId()).ok);
            Assert.assertFalse(library.removeCard(dCard.getCardId()).ok);
            my.cards.remove(0);
        }
        /* check cards */
        my.cards.sort(Comparator.comparingInt(Card::getCardId));
        ApiResult result = library.showCards();
        Assert.assertTrue(result.ok);
        CardList resCardList = (CardList) result.payload;
        Assert.assertEquals(my.nCards(), resCardList.getCount());
        for (int i = 0; i < my.nCards(); i++) {
            Card o1 = my.cards.get(i);
            Card o2 = resCardList.getCards().get(i);
            Assert.assertEquals(o1.toString(), o2.toString());
        }
    }

    private List<Book> verifyQueryResult(List<Book> books, BookQueryConditions conditions) {
        Stream<Book> stream = books.stream();
        if (conditions.getCategory() != null) {
            stream = stream.filter(b -> b.getCategory().equals(conditions.getCategory()));
        }
        if (conditions.getTitle() != null) {
            stream = stream.filter(b -> b.getTitle().contains(conditions.getTitle()));
        }
        if (conditions.getPress() != null) {
            stream = stream.filter(b -> b.getPress().contains(conditions.getPress()));
        }
        if (conditions.getMinPublishYear() != null) {
            stream = stream.filter(b -> b.getPublishYear() >= conditions.getMinPublishYear());
        }
        if (conditions.getMaxPublishYear() != null) {
            stream = stream.filter(b -> b.getPublishYear() <= conditions.getMaxPublishYear());
        }
        if (conditions.getAuthor() != null) {
            stream = stream.filter(b -> b.getAuthor().contains(conditions.getAuthor()));
        }
        if (conditions.getMinPrice() != null) {
            stream = stream.filter(b -> b.getPrice() >= conditions.getMinPrice());
        }
        if (conditions.getMaxPrice() != null) {
            stream = stream.filter(b -> b.getPrice() <= conditions.getMaxPrice());
        }
        Comparator<Book> cmp = conditions.getSortBy().getComparator();
        if (conditions.getSortOrder() == SortOrder.DESC) {
            cmp = cmp.reversed();
        }
        Comparator<Book> comparator = cmp;
        Comparator<Book> sortComparator = (lhs, rhs) -> {
            if (comparator.compare(lhs, rhs) == 0) {
                return lhs.getBookId() - rhs.getBookId();
            }
            return comparator.compare(lhs, rhs);
        };
        return stream.sorted(sortComparator).collect(Collectors.toList());

    }

}
