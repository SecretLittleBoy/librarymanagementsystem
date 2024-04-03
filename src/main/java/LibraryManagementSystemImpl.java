import entities.Book;
import entities.Borrow;
import entities.Card;
import entities.Card.CardType;
import queries.*;
import queries.BorrowHistories.Item;
import utils.DBInitializer;
import utils.DatabaseConnector;
import java.util.*;
import org.junit.Assert;

import java.sql.*;
import java.util.List;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {
    public static boolean borrowBookThreating = false ;

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    /*
     * test passed
     */
    @Override
    public ApiResult storeBook(Book book) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO book(book_id,category,title,press,publish_year,author,price,stock) VALUES ( 0 ,?, ?, ?, ?, ?,?,?)");
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getStock());
            stmt.executeUpdate();

            stmt = conn.prepareStatement(
                    "SELECT book_id FROM book WHERE title = ? AND author = ? AND press = ? AND publish_year = ? AND category = ?");
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getCategory());
            ResultSet rs = stmt.executeQuery();
            rs.next();
            book.setBookId(rs.getInt("book_id"));
            commit(conn);
            return new ApiResult(true,
                    "INSERT INTO book(book_id,category,title,press,publish_year,author,price,stock) VALUES ("
                            + "0,\"" + book.getCategory() + "\",\"" + book.getTitle() + "\",\"" + book.getPress()
                            + "\"," + book.getPublishYear() +
                            ",\"" + book.getAuthor() + "\"," + book.getPrice() + "," + book.getStock() + ")");
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = connector.getConn();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM book WHERE book_id = " + bookId);
            if (!rs.next()) {
                return new ApiResult(false, "Book not found");
            }
            int stock = rs.getInt("stock");
            if (stock + deltaStock < 0) {
                return new ApiResult(false, "Stock cannot be negative");
            }
            PreparedStatement stmt = conn.prepareStatement("UPDATE book SET stock = ? WHERE book_id = ?");
            stmt.setInt(1, stock + deltaStock);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
            commit(conn);
            return new ApiResult(true,
                    "UPDATE book SET stock = " + (stock + deltaStock) + " WHERE book_id = " + bookId);
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        if (books.size() <= 0)
            return new ApiResult(false, "No book to store");
        Connection conn = connector.getConn();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(
                    "INSERT INTO book(book_id,category,title,press,publish_year,author,price,stock) VALUES ( 0 ,?, ?, ?, ?, ?,?,?)");
            for (Book book : books) {
                stmt.setString(1, book.getCategory());
                stmt.setString(2, book.getTitle());
                stmt.setString(3, book.getPress());
                stmt.setInt(4, book.getPublishYear());
                stmt.setString(5, book.getAuthor());
                stmt.setDouble(6, book.getPrice());
                stmt.setInt(7, book.getStock());
                stmt.addBatch();
            }
            stmt.executeBatch();
            for (Book book : books) {
                stmt = conn.prepareStatement(
                        "SELECT book_id FROM book WHERE title = ? AND author = ? AND press = ? AND publish_year = ? AND category = ?");
                stmt.setString(1, book.getTitle());
                stmt.setString(2, book.getAuthor());
                stmt.setString(3, book.getPress());
                stmt.setInt(4, book.getPublishYear());
                stmt.setString(5, book.getCategory());
                ResultSet rs = stmt.executeQuery();
                rs.next();
                book.setBookId(rs.getInt("book_id"));
            }
            commit(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "inserted all books");
    }

    @Override
    public ApiResult removeBook(int bookId) {
        Connection conn = connector.getConn();
        try {
            ResultSet rs2 = conn.createStatement().executeQuery("SELECT * FROM book WHERE book_id = " + bookId);
            if (!rs2.next()) {
                throw new SQLException("Book not found");
            }
            ResultSet rs = conn.createStatement()
            .executeQuery("SELECT * FROM borrow WHERE book_id = " + bookId + " AND return_time = 0");
            if (rs.next()) {
                return new ApiResult(false, "Some of books is borrowed and not returned");
            }
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM book WHERE book_id = ?");
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            commit(conn);
            return new ApiResult(true, "DELETE FROM book WHERE book_id = " + bookId);
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM book WHERE book_id = ?");
            stmt.setInt(1, book.getBookId());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return new ApiResult(false, "Book not found");
            }
            stmt = conn.prepareStatement(
                    "UPDATE book SET category = ?, title = ?, press = ?, publish_year = ?, author = ?, price = ? WHERE book_id = ?");
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getBookId()); 
            stmt.executeUpdate();
            commit(conn);
            return new ApiResult(true,
                    "UPDATE book SET category = '" + book.getCategory() + "', title = '" + book.getTitle()
                            + "', press = '" + book.getPress() + "', publish_year = " + book.getPublishYear()
                            + ", author = '" + book.getAuthor() + "', price = " + book.getPrice() + ", stock = "
                            + book.getStock() + " WHERE book_id = " + book.getBookId());
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {
        Connection conn = connector.getConn();
        try {
            String sql = "SELECT * FROM book WHERE 1=1 ";
            if (conditions.getCategory() != null) {
                sql += " AND category = '" + conditions.getCategory() + "'";
            }
            // if (conditions.getTitle() != null) {
            //     sql += " AND title sounds like '" + conditions.getTitle() + "'";
            // }
            if (conditions.getTitle() != null) {
                sql += " AND title like '%" + conditions.getTitle() + "%'";
            }
            if (conditions.getPress() != null) {
                sql += " AND press like '%" + conditions.getPress() + "%'";
            }
            if (conditions.getAuthor() != null) {
                sql += " AND author like '%" + conditions.getAuthor() + "%'";
            }
            if (conditions.getMinPrice() != null) {
                sql += " AND price >= " + conditions.getMinPrice();
            }
            if (conditions.getMaxPrice() != null) {
                sql += " AND price <= " + conditions.getMaxPrice();
            }
            if (conditions.getMinPublishYear() != null) {
                sql += " AND publish_year >= " + conditions.getMinPublishYear();
            }
            if (conditions.getMaxPublishYear() != null) {
                sql += " AND publish_year <= " + conditions.getMaxPublishYear();
            }
            sql += " order by " + conditions.getSortBy().getValue() + " " + conditions.getSortOrder() +" ,book_id asc";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                Book tempBook = new Book(rs.getString("category"), rs.getString("title"),
                        rs.getString("press"), rs.getInt("publish_year"), rs.getString("author"), rs.getDouble("price"),
                        rs.getInt("stock"));
                tempBook.setBookId(rs.getInt("book_id"));
                books.add(tempBook);
            }
            commit(conn);
            return new ApiResult(true, new BookQueryResults(books));
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        if(borrowBookThreating){
            return new ApiResult(false, "Borrow book is in progress");
        }else{
            borrowBookThreating = true;
        }
        Connection conn = connector.getConn();
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM borrow WHERE card_id = " + borrow.getCardId() +
                            " AND book_id = " + borrow.getBookId() + " AND return_time = 0");
            if (rs.next()) {
                borrowBookThreating = false;
                return new ApiResult(false, "You have borrowed this book");
            }
            rs = conn.createStatement().executeQuery("SELECT * FROM card WHERE card_id = " + borrow.getCardId());
            if (!rs.next()) {
                borrowBookThreating = false;
                return new ApiResult(false, "Card not found");
            }
            ResultSet rs2 = conn.createStatement()
                    .executeQuery("SELECT * FROM book WHERE book_id = " + borrow.getBookId());
            if (!rs2.next()) {
                borrowBookThreating = false;
                return new ApiResult(false, "Book not found");
            }
            int stock = rs2.getInt("stock");
            if (stock <= 0) {
                borrowBookThreating = false;
                return new ApiResult(false, "Stock is zero");
            }
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO borrow (card_id, book_id, borrow_time ,return_time) VALUES (?, ?, ?,0)");
            stmt.setInt(1, borrow.getCardId());
            stmt.setInt(2, borrow.getBookId());
            stmt.setLong(3, borrow.getBorrowTime());
            stmt.executeUpdate();
            stmt = conn.prepareStatement("UPDATE book SET stock = ? WHERE book_id = ?");
            stmt.setInt(1, stock - 1);
            stmt.setInt(2, borrow.getBookId());
            stmt.executeUpdate();
            commit(conn);
            borrowBookThreating = false;
            return new ApiResult(true, "Borrow success");
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            borrowBookThreating = false;
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM borrow WHERE card_id = " + borrow.getCardId() +
                            " AND book_id = " + borrow.getBookId()+ " order by return_time limit 1");
            if (!rs.next()) {
                //Assert.fail("You have not borrowed this book");
                return new ApiResult(false, "You have not borrowed this book");
            }
            if (rs.getLong("return_time") != 0) {
                //Assert.fail("You have returned this book"+rs.getLong("return_time"));
                return new ApiResult(false, "You have returned this book");
            }
            if(rs.getLong("borrow_time")>=borrow.getReturnTime()){
                //Assert.fail("Return time is wrong");
                return new ApiResult(false, "Return time is wrong");
            }
            rs = conn.createStatement()
                    .executeQuery("SELECT * FROM book WHERE book_id = " + borrow.getBookId());
            if (!rs.next()) {
                return new ApiResult(false, "Book not found");
            }
            int stock = rs.getInt("stock");
            PreparedStatement stmt = conn
                    .prepareStatement("UPDATE borrow SET return_time = ? WHERE card_id = ? AND book_id = ? AND return_time = 0");
            stmt.setLong(1, borrow.getReturnTime());
            stmt.setInt(2, borrow.getCardId());
            stmt.setInt(3, borrow.getBookId());
            stmt.executeUpdate();
            stmt = conn.prepareStatement("UPDATE book SET stock = ? WHERE book_id = ?");
            stmt.setInt(1, stock + 1);
            stmt.setInt(2, borrow.getBookId());
            stmt.executeUpdate();
            commit(conn);
            return new ApiResult(true, "Return success");
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        try {
            List<Item> items = new ArrayList<>();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM borrow WHERE card_id = " + cardId
                            + " order by borrow_time desc ,book_id asc");
            if (!rs.next()) {
                return new ApiResult(false, "No borrow history");
            }
            do {
                Borrow tempBorrow = new Borrow(rs.getInt("book_id"),rs.getInt("card_id"));
                tempBorrow.setBorrowTime(rs.getLong("borrow_time"));
                tempBorrow.setReturnTime(rs.getLong("return_time"));
                ResultSet bookrs = conn.createStatement()
                        .executeQuery("SELECT * FROM book WHERE book_id = " + tempBorrow.getBookId());
                if (!bookrs.next()) {
                    return new ApiResult(false, "Book not found");
                }
                Book tempBook = new Book(bookrs.getString("category"), bookrs.getString("title"),
                        bookrs.getString("press"), bookrs.getInt("publish_year"), bookrs.getString("author"),
                        bookrs.getDouble("price"),
                        bookrs.getInt("stock"));
                tempBook.setBookId(bookrs.getInt("book_id"));
                Item item = new Item(tempBorrow.getCardId(), tempBook, tempBorrow);
                items.add(item);
            } while (rs.next());
            commit(conn);
            return new ApiResult(true, new BorrowHistories(items));
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM card WHERE card_id = " + card.getCardId());
            if (rs.next()) {
                return new ApiResult(false, "Card already exists");
            }
            PreparedStatement stmt = conn
                    .prepareStatement("INSERT INTO card (card_id, name, department, type) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, card.getCardId());
            stmt.setString(2, card.getName());
            stmt.setString(3, card.getDepartment());
            stmt.setString(4, card.getType().getStr());
            stmt.executeUpdate();
            stmt = conn.prepareStatement("SELECT card_id FROM card WHERE name = ? AND department = ? AND type = ?");
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            ResultSet rs2 = stmt.executeQuery();
            if (rs2.next()) {
                card.setCardId(rs2.getInt("card_id"));
            }
            commit(conn);
            return new ApiResult(true, "Register success");
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = connector.getConn();
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM card WHERE card_id = " + cardId);
            if (!rs.next()) {
                return new ApiResult(false, "Card not found");
            }
            rs = conn.createStatement()
                    .executeQuery("SELECT * FROM borrow WHERE card_id = " + cardId + " AND return_time = 0");
            if (rs.next()) {
                return new ApiResult(false, "Card has not returned all books");
            }
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM card WHERE card_id = ?");
            stmt.setInt(1, cardId);
            stmt.executeUpdate();
            commit(conn);
            return new ApiResult(true, "Remove success");
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult showCards() {
        Connection conn = connector.getConn();
        try {
            List<Card> cards = new ArrayList<>();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM card order by card_id asc");
            if (!rs.next()) {
                return new ApiResult(false, "No card");
            }
            do {
                Card tempCard = new Card(rs.getInt("card_id"), rs.getString("name"), rs.getString("department"),
                        CardType.values(rs.getString("type") )  );
                tempCard.setCardId(rs.getInt("card_id"));
                cards.add(tempCard);
            } while (rs.next());
            commit(conn);
            return new ApiResult(true, new CardList(cards));
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
