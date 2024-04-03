import entities.*;
import entities.Book.SortColumn;
import entities.Card.CardType;
import queries.BookQueryConditions;
import queries.BorrowHistories.Item;
import queries.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;

public class GUI {
	private LibraryManagementSystem library;
	private Container c;
	private JMenuBar mb;
	private JMenu menuBook, menuCard;
	private JMenuItem itemBook, itemCard;
	private JFrame frame, cardFrame;
	private JButton btdelete, btadd, btmodify, btsearch, btBorrowReturn;
	private JLabel lbcategory, lbtitle, lbpress, lbminPublishYear, lbmaxPublishYear, lbauthor, lbminPrice, lbmaxPrice,
			lbsortBy, lbsortOrder;
	private JTextField tfcategory, tftitle, tfpress, tfminPublishYear, tfmaxPublishYear, tfauthor, tfminPrice,
			tfmaxPrice;
	private JTextField add_tfpublishYear, add_tfprice, add_tfstock, add_tfcategory,
			add_tfauthor, add_tfpress, add_tftitle, add_tfdeltastock, modify_bookId;
	private JTextField cardName, cardDepartment;
	private JComboBox<String> cardType;
	private JTextField borrowReturn_tfCardId;
	private JComboBox<String> cbSortby;
	private JComboBox<String> cbSortorder;
	private JTable tbquerybook, tbQueryCard;

	public GUI(LibraryManagementSystem library) {
		this.library = library;
		initializeGUI();
	}

	private void initializeGUI() {
		frame = new JFrame();
		frame.setBounds(100, 100, 900, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new FlowLayout());

		c = frame.getContentPane();
		mb = new JMenuBar();
		menuBook = new JMenu("Books");
		menuCard = new JMenu("Cards");
		itemBook = new JMenuItem("Books");
		itemBook.addActionListener(new handerMenu());
		itemCard = new JMenuItem("Cards");
		itemCard.addActionListener(new handerMenu());
		menuBook.add(itemBook);
		menuCard.add(itemCard);
		mb.add(menuBook);
		mb.add(menuCard);
		frame.setJMenuBar(mb);

		lbcategory = new JLabel("Category:");
		c.add(lbcategory);
		tfcategory = new JTextField(10);
		c.add(tfcategory);

		lbtitle = new JLabel("Title:");
		c.add(lbtitle);
		tftitle = new JTextField(10);
		c.add(tftitle);

		lbpress = new JLabel("Press:");
		c.add(lbpress);
		tfpress = new JTextField(10);
		c.add(tfpress);

		lbminPublishYear = new JLabel("Min Publish Year:");
		c.add(lbminPublishYear);
		tfminPublishYear = new JTextField(10);
		c.add(tfminPublishYear);

		lbmaxPublishYear = new JLabel("Max Publish Year:");
		c.add(lbmaxPublishYear);
		tfmaxPublishYear = new JTextField(10);
		c.add(tfmaxPublishYear);

		lbauthor = new JLabel("Author:");
		c.add(lbauthor);
		tfauthor = new JTextField(10);
		c.add(tfauthor);

		lbminPrice = new JLabel("Min Price:");
		c.add(lbminPrice);
		tfminPrice = new JTextField(10);
		c.add(tfminPrice);

		lbmaxPrice = new JLabel("Max Price:");
		c.add(lbmaxPrice);
		tfmaxPrice = new JTextField(10);
		c.add(tfmaxPrice);

		lbsortBy = new JLabel("Sort By:");
		c.add(lbsortBy);
		String[] sortby = { "book_id", "Category", "Title", "Press", "Publish Year", "Author", "Price" };
		cbSortby = new JComboBox<String>(sortby);
		c.add(cbSortby);

		lbsortOrder = new JLabel("Sort Order:");
		c.add(lbsortOrder);
		String[] sortorder = { "Ascending", "Descending" };
		cbSortorder = new JComboBox<String>(sortorder);
		c.add(cbSortorder);

		btsearch = new JButton("Search");
		btsearch.addActionListener(new handerSearch());
		c.add(btsearch);

		btdelete = new JButton("Delete");
		btdelete.addActionListener(new handerSearch());
		c.add(btdelete);

		btadd = new JButton("Add");
		btadd.addActionListener(new handerSearch());
		c.add(btadd);

		btmodify = new JButton("Modify");
		btmodify.addActionListener(new handerSearch());
		c.add(btmodify);

		btBorrowReturn = new JButton("borrow & return");
		btBorrowReturn.addActionListener(new handerSearch());
		c.add(btBorrowReturn);
	}

	private void initializeCardFrame() {
		cardFrame = new JFrame();
		cardFrame.setBounds(100, 100, 900, 700);
		cardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cardFrame.getContentPane().setLayout(new FlowLayout());

		c = cardFrame.getContentPane();
		mb = new JMenuBar();
		menuBook = new JMenu("Books");
		menuCard = new JMenu("Cards");
		itemBook = new JMenuItem("Books");
		itemBook.addActionListener(new handerMenu());
		itemCard = new JMenuItem("Cards");
		itemCard.addActionListener(new handerMenu());
		menuBook.add(itemBook);
		menuCard.add(itemCard);
		mb.add(menuBook);
		mb.add(menuCard);
		cardFrame.setJMenuBar(mb);

		JButton showCards = new JButton("showCards");
		showCards.addActionListener(new handerCards());
		JButton removeCard = new JButton("remove Card");
		removeCard.addActionListener(new handerCards());
		JButton registerCard = new JButton("register Card");
		registerCard.addActionListener(new handerCards());
		JButton showBorrowHistory = new JButton("show Borrow History");
		showBorrowHistory.addActionListener(new handerCards());
		c.add(showCards);
		c.add(removeCard);
		c.add(registerCard);
		c.add(showBorrowHistory);
	}

	public void show(int whichFrame) {
		switch (whichFrame) {
			case 1:
				frame.setVisible(true);
				cardFrame.setVisible(false);
				break;
			case 2:
				frame.setVisible(false);
				cardFrame.setVisible(true);
		}
	}

	public class handerMenu implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == itemBook) {
				if (frame.isVisible()) {
					return;
				}
				System.out.println("Book");
				initializeGUI();
				show(1);
			} else if (e.getSource() == itemCard) {
				if (cardFrame != null && cardFrame.isVisible()) {
					return;
				}
				System.out.println("Card");
				initializeCardFrame();
				show(2);
			}
		}
	}

	public class handerSearch implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btsearch) {
				try {
					while (true) {
						c.remove(25);
					}
				} catch (Exception expt) {
				}

				BookQueryConditions bqc = new BookQueryConditions();
				if (!tfcategory.getText().equals(""))
					bqc.setCategory(tfcategory.getText());
				if (!tftitle.getText().equals(""))
					bqc.setTitle(tftitle.getText());
				if (!tfpress.getText().equals(""))
					bqc.setPress(tfpress.getText());
				if (!tfminPublishYear.getText().equals("")) {
					try {
						int minyear = Integer.parseInt(tfminPublishYear.getText());
						bqc.setMinPublishYear(minyear);
					} catch (Exception expt) {
						System.out.println("Invalid minPublishYear");
						JOptionPane.showMessageDialog(null, "Invalid minPublishYear\nminPublishYear is ignored",
								"Search Error",
								JOptionPane.WARNING_MESSAGE);
					}
				}
				if (!tfmaxPublishYear.getText().equals("")) {
					try {
						int maxyear = Integer.parseInt(tfmaxPublishYear.getText());
						bqc.setMaxPublishYear(maxyear);
					} catch (Exception expt) {
						System.out.println("Invalid maxPublishYear");
						JOptionPane.showMessageDialog(null, "Invalid maxPublishYear\nmaxPublishYear is ignored",
								"Search Error",
								JOptionPane.WARNING_MESSAGE);
					}
				}
				if (!tfauthor.getText().equals(""))
					bqc.setAuthor(tfauthor.getText());
				if (!tfminPrice.getText().equals("")) {
					try {
						double minprice = Double.parseDouble(tfminPrice.getText());
						bqc.setMinPrice(minprice);
					} catch (Exception expt) {
						System.out.println("Invalid minPrice");
						JOptionPane.showMessageDialog(null, "Invalid minPrice\nminPrice is ignored",
								"Search Error",
								JOptionPane.WARNING_MESSAGE);
					}
				}
				if (!tfmaxPrice.getText().equals("")) {
					try {
						double maxprice = Double.parseDouble(tfmaxPrice.getText());
						bqc.setMaxPrice(maxprice);
					} catch (Exception expt) {
						System.out.println("Invalid maxPrice");
						JOptionPane.showMessageDialog(null, "Invalid maxPrice\nmaxPrice is ignored",
								"Search Error",
								JOptionPane.WARNING_MESSAGE);
					}
				}
				if (cbSortby.getSelectedItem().equals("book_id"))
					bqc.setSortBy(SortColumn.BOOK_ID);
				else if (cbSortby.getSelectedItem().equals("Category"))
					bqc.setSortBy(SortColumn.CATEGORY);
				else if (cbSortby.getSelectedItem().equals("Title"))
					bqc.setSortBy(SortColumn.TITLE);
				else if (cbSortby.getSelectedItem().equals("Press"))
					bqc.setSortBy(SortColumn.PRESS);
				else if (cbSortby.getSelectedItem().equals("Publish Year"))
					bqc.setSortBy(SortColumn.PUBLISH_YEAR);
				else if (cbSortby.getSelectedItem().equals("Author"))
					bqc.setSortBy(SortColumn.AUTHOR);
				else if (cbSortby.getSelectedItem().equals("Price"))
					bqc.setSortBy(SortColumn.PRICE);
				if (cbSortorder.getSelectedItem().equals("Ascending"))
					bqc.setSortOrder(queries.SortOrder.ASC);
				else if (cbSortorder.getSelectedItem().equals("Descending"))
					bqc.setSortOrder(queries.SortOrder.DESC);

				try {
					BookQueryResults bqrst = ((BookQueryResults) library.queryBook(bqc).payload);
					Vector<String> columnNames = new Vector<String>(Arrays.asList("book_id", "Category", "Title",
							"Press", "Publish Year", "Author", "Price", "Stock"));
					Vector<Vector<String>> tablevalues = new Vector<Vector<String>>();
					System.out.println("BookQueryResults:");
					bqrst.getResults().forEach((book) -> {
						System.out.println(book.toString());
						Vector<String> onerow = new Vector<String>();
						onerow.add(Integer.toString(book.getBookId()));
						onerow.add(book.getCategory());
						onerow.add(book.getTitle());
						onerow.add(book.getPress());
						onerow.add(Integer.toString(book.getPublishYear()));
						onerow.add(book.getAuthor());
						onerow.add(Double.toString(book.getPrice()));
						onerow.add(Double.toString(book.getStock()));
						tablevalues.add(onerow);
					});
					tbquerybook = new JTable(tablevalues, columnNames) {
						public boolean isCellEditable(int row, int column) {
							return false;
						}
					};
					JScrollPane scrollPane = new JScrollPane(tbquerybook);
					scrollPane.setBounds(0, 0, 9000, frame.getHeight());
					frame.add(scrollPane, BorderLayout.CENTER);
				} catch (Exception expt) {
					System.out.println("query fails");
				}

			} else if (e.getSource() == btdelete) {
				System.out.println("Delete");
				int[] selected = tbquerybook.getSelectedRows();
				for (int i = 0; i < selected.length; i++) {
					int bookid = Integer.parseInt((String) tbquerybook.getValueAt(selected[i], 0));
					ApiResult result = library.removeBook(bookid);
					if (result.ok) {
						JOptionPane.showMessageDialog(null, "delete success:\nplease refresh the search result",
								"delete success",
								JOptionPane.WARNING_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, "delete failed:\n" + result.message,
								"delete failed",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			} else if (e.getSource() == btadd) {
				System.out.println("Add");
				JDialog addbook = new JDialog(frame, "add book", true);
				addbook.setSize(800, 400);
				addbook.setLocation(450, 350);
				JLabel label_category = new JLabel("category:");
				JLabel label_title = new JLabel("title:");
				JLabel label_press = new JLabel("press:");
				JLabel label_publishYear = new JLabel("publishYear:");
				JLabel label_author = new JLabel("author:");
				JLabel label_price = new JLabel("price:");
				JLabel label_stock = new JLabel("stock:");
				add_tfcategory = new JTextField(10);
				add_tftitle = new JTextField(10);
				add_tfpress = new JTextField(10);
				add_tfpublishYear = new JTextField(10);
				add_tfauthor = new JTextField(10);
				add_tfprice = new JTextField(10);
				add_tfstock = new JTextField(10);
				JButton button = new JButton("Add Book");
				button.addActionListener(new handerAdd());
				JPanel panel = new JPanel(new GridLayout(8, 2));
				panel.add(label_category);
				panel.add(add_tfcategory);
				panel.add(label_title);
				panel.add(add_tftitle);
				panel.add(label_press);
				panel.add(add_tfpress);
				panel.add(label_publishYear);
				panel.add(add_tfpublishYear);
				panel.add(label_author);
				panel.add(add_tfauthor);
				panel.add(label_price);
				panel.add(add_tfprice);
				panel.add(label_stock);
				panel.add(add_tfstock);
				panel.add(button);
				addbook.getContentPane().add(panel);
				addbook.setVisible(true);

			} else if (e.getSource() == btmodify) {
				System.out.println("Modify");
				int[] selected = tbquerybook.getSelectedRows();
				if (selected.length == 0) {
					System.out.println("Please select a book");
					return;
				}
				for (int selectedOne : selected) {
					JDialog addbook = new JDialog(frame, "Modify book", true);
					addbook.setSize(800, 400);
					addbook.setLocation(450, 350);
					JLabel label_bookid = new JLabel("bookid:");
					JLabel label_category = new JLabel("category:");
					JLabel label_title = new JLabel("title:");
					JLabel label_press = new JLabel("press:");
					JLabel label_publishYear = new JLabel("publishYear:");
					JLabel label_author = new JLabel("author:");
					JLabel label_price = new JLabel("price:");
					JLabel label_stock = new JLabel("stock:");
					JLabel label_deltastock = new JLabel("deltastock:");
					modify_bookId = new JTextField(10);
					modify_bookId.setText((String) tbquerybook.getValueAt(selectedOne, 0));
					modify_bookId.setEditable(false);
					add_tfcategory = new JTextField(10);
					add_tfcategory.setText((String) tbquerybook.getValueAt(selectedOne, 1));
					add_tftitle = new JTextField(10);
					add_tftitle.setText((String) tbquerybook.getValueAt(selectedOne, 2));
					add_tfpress = new JTextField(10);
					add_tfpress.setText((String) tbquerybook.getValueAt(selectedOne, 3));
					add_tfpublishYear = new JTextField(10);
					add_tfpublishYear.setText((String) tbquerybook.getValueAt(selectedOne, 4));
					add_tfauthor = new JTextField(10);
					add_tfauthor.setText((String) tbquerybook.getValueAt(selectedOne, 5));
					add_tfprice = new JTextField(10);
					add_tfprice.setText((String) tbquerybook.getValueAt(selectedOne, 6));
					add_tfstock = new JTextField(10);
					add_tfstock.setText((String) tbquerybook.getValueAt(selectedOne, 7));
					add_tfstock.setEditable(false);
					add_tfdeltastock = new JTextField(10);
					add_tfdeltastock.setText("0");
					JButton button = new JButton("Modify Book");
					button.addActionListener(new handerModify());
					JPanel panel = new JPanel(new GridLayout(10, 2));
					panel.add(label_bookid);
					panel.add(modify_bookId);
					panel.add(label_category);
					panel.add(add_tfcategory);
					panel.add(label_title);
					panel.add(add_tftitle);
					panel.add(label_press);
					panel.add(add_tfpress);
					panel.add(label_publishYear);
					panel.add(add_tfpublishYear);
					panel.add(label_author);
					panel.add(add_tfauthor);
					panel.add(label_price);
					panel.add(add_tfprice);
					panel.add(label_stock);
					panel.add(add_tfstock);
					panel.add(label_deltastock);
					panel.add(add_tfdeltastock);
					panel.add(button);
					addbook.getContentPane().add(panel);
					addbook.setVisible(true);
				}
			} else if (e.getSource() == btBorrowReturn) {
				System.out.println("Borrow and Return");
				int[] selected = tbquerybook.getSelectedRows();
				if (selected.length == 0) {
					System.out.println("Please select a book");
					return;
				}
				for (int selectedOne : selected) {
					JDialog borrowReturn = new JDialog(frame, "Borrow and Return", true);
					borrowReturn.setSize(600, 300);
					borrowReturn.setLocation(450, 350);
					JLabel lbbookId = new JLabel("bookId:");
					JTextField tfbookId = new JTextField(10);
					tfbookId.setText((String) tbquerybook.getValueAt(selectedOne, 0));
					tfbookId.setEditable(false);
					JLabel lbcardId = new JLabel("cardId:");
					borrowReturn_tfCardId = new JTextField(10);
					JButton btBorrow = new JButton("Borrow");
					btBorrow.putClientProperty("bookId", tfbookId.getText());
					btBorrow.addActionListener(new handerBorrowReturn());
					JButton btreturn = new JButton("Return");
					btreturn.putClientProperty("bookId", tfbookId.getText());
					btreturn.addActionListener(new handerBorrowReturn());
					JPanel panel = new JPanel(new GridLayout(3, 2));
					panel.add(lbbookId);
					panel.add(tfbookId);
					panel.add(lbcardId);
					panel.add(borrowReturn_tfCardId);
					panel.add(btBorrow);
					panel.add(btreturn);
					borrowReturn.getContentPane().add(panel);
					borrowReturn.setVisible(true);
				}
			}

			frame.repaint();
			frame.setVisible(true);
		}
	}

	public class handerAdd implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Book book = new Book();
			if (!add_tfcategory.getText().equals(""))
				book.setCategory(add_tfcategory.getText());
			else {
				System.out.println("Invalid Category");
				JOptionPane.showMessageDialog(null, "Invalid Category\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tftitle.getText().equals(""))
				book.setTitle(add_tftitle.getText());
			else {
				System.out.println("Invalid Title");
				JOptionPane.showMessageDialog(null, "Invalid Title\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfpress.getText().equals(""))
				book.setPress(add_tfpress.getText());
			else {
				System.out.println("Invalid Press");
				JOptionPane.showMessageDialog(null, "Invalid Press\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfpublishYear.getText().equals("")) {
				try {
					int publishYear = Integer.parseInt(add_tfpublishYear.getText());
					book.setPublishYear(publishYear);
				} catch (Exception expt) {
					System.out.println("Invalid PublishYear");
					JOptionPane.showMessageDialog(null, "Invalid PublishYear\n",
							"Add Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			} else {
				System.out.println("Invalid PublishYear");
				JOptionPane.showMessageDialog(null, "Invalid PublishYear\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfauthor.getText().equals(""))
				book.setAuthor(add_tfauthor.getText());
			else {
				System.out.println("Invalid Author");
				JOptionPane.showMessageDialog(null, "Invalid Author\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfprice.getText().equals("")) {
				try {
					double price = Double.parseDouble(add_tfprice.getText());
					book.setPrice(price);
				} catch (Exception expt) {
					System.out.println("Invalid Price");
					JOptionPane.showMessageDialog(null, "Invalid Price\n",
							"Add Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			} else {
				System.out.println("Invalid Price");
				JOptionPane.showMessageDialog(null, "Invalid Price\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfstock.getText().equals("")) {
				try {
					int stock = Integer.parseInt(add_tfstock.getText());
					book.setStock(stock);
				} catch (Exception expt) {
					System.out.println("Invalid Stock");
					JOptionPane.showMessageDialog(null, "Invalid Stock\n",
							"Add Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			} else {
				System.out.println("Invalid Stock");
				JOptionPane.showMessageDialog(null, "Invalid Stock\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (library.storeBook(book).ok) {
				System.out.println("add success");
				JOptionPane.showMessageDialog(null, "Add Success\n",
						"Add Success",
						JOptionPane.WARNING_MESSAGE);
			} else {
				System.out.println("add fails");
				JOptionPane.showMessageDialog(null, "Add Fails\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public class handerModify implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Book book = new Book();
			int deltastock;
			book.setBookId(Integer.parseInt(modify_bookId.getText()));
			if (!add_tfcategory.getText().equals(""))
				book.setCategory(add_tfcategory.getText());
			else {
				System.out.println("Invalid Category");
				JOptionPane.showMessageDialog(null, "Invalid Category\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tftitle.getText().equals(""))
				book.setTitle(add_tftitle.getText());
			else {
				System.out.println("Invalid Title");
				JOptionPane.showMessageDialog(null, "Invalid Title\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfpress.getText().equals(""))
				book.setPress(add_tfpress.getText());
			else {
				System.out.println("Invalid Press");
				JOptionPane.showMessageDialog(null, "Invalid Press\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfpublishYear.getText().equals("")) {
				try {
					int publishYear = Integer.parseInt(add_tfpublishYear.getText());
					book.setPublishYear(publishYear);
				} catch (Exception expt) {
					System.out.println("Invalid PublishYear");
					JOptionPane.showMessageDialog(null, "Invalid PublishYear\n",
							"Add Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			} else {
				System.out.println("Invalid PublishYear");
				JOptionPane.showMessageDialog(null, "Invalid PublishYear\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfauthor.getText().equals(""))
				book.setAuthor(add_tfauthor.getText());
			else {
				System.out.println("Invalid Author");
				JOptionPane.showMessageDialog(null, "Invalid Author\n",
						"Add Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfprice.getText().equals("")) {
				try {
					double price = Double.parseDouble(add_tfprice.getText());
					book.setPrice(price);
				} catch (Exception expt) {
					System.out.println("Invalid Price");
					JOptionPane.showMessageDialog(null, "Invalid Price\n",
							"Add Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			} else {
				System.out.println("Invalid Price");
				JOptionPane.showMessageDialog(null, "Invalid Price\n",
						"Modify Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!add_tfdeltastock.getText().equals("")) {
				try {
					deltastock = Integer.parseInt(add_tfdeltastock.getText());

				} catch (Exception expt) {
					System.out.println("Invalid deltaStock");
					JOptionPane.showMessageDialog(null, "Invalid deltaStock\n",
							"Modify Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			} else {
				System.out.println("Invalid deltaStock");
				JOptionPane.showMessageDialog(null, "Invalid deltaStock\n",
						"Modify Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			ApiResult modifyBookInforesult = library.modifyBookInfo(book);
			ApiResult incBookStockresult = library.incBookStock(book.getBookId(), deltastock);
			if (modifyBookInforesult.ok && incBookStockresult.ok) {
				System.out.println("Modify success");
				JOptionPane.showMessageDialog(null, "Modify Success\n",
						"Modify Success",
						JOptionPane.WARNING_MESSAGE);
			} else {
				System.out.println("Modify fails");
				JOptionPane.showMessageDialog(null, "Modify Fails:\n" + modifyBookInforesult.message +
						"\n" + incBookStockresult.message,
						"Modify Error",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public class handerBorrowReturn implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().toString().contains(",text=Borrow,")) {
				System.out.println("Borrow");
				Borrow borrow;
				try {
					int bookId = Integer.parseInt((String) ((JButton) e.getSource()).getClientProperty(
							"bookId"));
					int cardId = Integer.parseInt(borrowReturn_tfCardId.getText());
					System.out.println("bookId:" + bookId + " cardId:" + cardId);
					borrow = new Borrow(bookId, cardId);
				} catch (Exception exp) {
					JOptionPane.showMessageDialog(null,
							"Input format error", "parse error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				borrow.resetBorrowTime();
				ApiResult result = library.borrowBook(borrow);
				if (result.ok) {
					JOptionPane.showMessageDialog(null, "Borrow success",
							"Borrow success",
							JOptionPane.WARNING_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null,
							result.message, "Borrow fails",
							JOptionPane.WARNING_MESSAGE);
				}

			} else if (e.getSource().toString().contains(",text=Return,")) {
				System.out.println("Return");
				Borrow borrow;
				try {
					int bookId = Integer.parseInt((String) ((JButton) e.getSource()).getClientProperty(
							"bookId"));
					int cardId = Integer.parseInt(borrowReturn_tfCardId.getText());
					System.out.println("bookId:" + bookId + " cardId:" + cardId);
					borrow = new Borrow(bookId, cardId);
				} catch (Exception exp) {
					JOptionPane.showMessageDialog(null,
							"Input format error", "parse error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				borrow.resetReturnTime();
				ApiResult result = library.returnBook(borrow);
				if (result.ok) {
					JOptionPane.showMessageDialog(null, "Return success",
							"Return success",
							JOptionPane.WARNING_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null,
							result.message, "Return fails",
							JOptionPane.WARNING_MESSAGE);
				}

			} else {
				System.out.println("toString:" + e.getSource().toString());
			}
		}
	}

	public class handerCards implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().toString().contains(",text=showCards,")) {
				System.out.println("showCards");
				try {
					while (true) {
						c.remove(4);
					}
				} catch (Exception expt) {
				}
				Vector<String> columnNames = new Vector<String>(Arrays.asList("cardId", "name", "department",
						"type"));
				Vector<Vector<String>> tablevalues = new Vector<Vector<String>>();
				for (Card card : ((CardList) library.showCards().payload).getCards()) {
					System.out.println("CardQueryResults:");
					System.out.println(card.toString());
					Vector<String> onerow = new Vector<String>();
					onerow.add(Integer.toString(card.getCardId()));
					onerow.add(card.getName());
					onerow.add(card.getDepartment());
					onerow.add(card.getType().getStr());
					tablevalues.add(onerow);
				}
				tbQueryCard = new JTable(tablevalues, columnNames) {
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};
				JScrollPane scrollPane = new JScrollPane(tbQueryCard);
				scrollPane.setBounds(0, 0, 9000, frame.getHeight());
				cardFrame.add(scrollPane, BorderLayout.CENTER);
			} else if (e.getSource().toString().contains(",text=remove Card,")) {
				System.out.println("remove Card");
				int[] selected = tbQueryCard.getSelectedRows();
				for (int i = 0; i < selected.length; i++) {
					int cardid = Integer.parseInt((String) tbQueryCard.getValueAt(selected[i], 0));
					ApiResult result = library.removeCard(cardid);
					if (result.ok) {
						JOptionPane.showMessageDialog(null, "delete success:\nplease refresh the showCards result",
								"delete success",
								JOptionPane.WARNING_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, "delete failed:\n" + result.message,
								"delete failed",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			} else if (e.getSource().toString().contains(",text=register Card,")) {
				System.out.println("register Card");
				JDialog registerCard = new JDialog(cardFrame, "register Card", true);
				registerCard.setSize(800, 400);
				registerCard.setLocation(450, 350);
				JLabel label_name = new JLabel("name:");
				JLabel label_department = new JLabel("department:");
				JLabel label_type = new JLabel("type:");
				cardName = new JTextField(10);
				cardDepartment = new JTextField(10);
				String[] type = { "Teacher", "Student" };
				cardType = new JComboBox<String>(type);
				JButton button = new JButton("register card");
				button.addActionListener(new handerRegisterCard());
				JPanel panel = new JPanel(new GridLayout(4, 2));
				panel.add(label_name);
				panel.add(cardName);
				panel.add(label_department);
				panel.add(cardDepartment);
				panel.add(label_type);
				panel.add(cardType);
				panel.add(button);
				registerCard.getContentPane().add(panel);
				registerCard.setVisible(true);
			} else if (e.getSource().toString().contains(",text=show Borrow History,")) {
				System.out.println("show Borrow History:");
				if (tbQueryCard.getSelectedRowCount() != 1) {
					JOptionPane.showMessageDialog(null, "please select one row of card!",
							"error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				Vector<String> columnNames = new Vector<String>(
						Arrays.asList("cardId", "bookId", "book title", "borrow time",
								"return time"));
				Vector<Vector<String>> tablevalues = new Vector<Vector<String>>();
				ApiResult result = library.showBorrowHistory(Integer.parseInt(
						(String) tbQueryCard.getValueAt(tbQueryCard.getSelectedRow(), 0)));
				if (!result.ok) {
					JOptionPane.showMessageDialog(null, result.message,
							"showBorrowHistory error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				try {
					while (true) {
						c.remove(4);
					}
				} catch (Exception expt) {
				}
				for (Item item : ((BorrowHistories) result.payload).getItems()) {
					System.out.println("BorrowHistories:");
					System.out.println(item.toString());
					Vector<String> onerow = new Vector<String>();
					onerow.add(Integer.toString(item.getCardId()));
					onerow.add(Integer.toString(item.getBookId()));
					onerow.add(item.getTitle());
					onerow.add(Long.toString(item.getBorrowTime()));
					onerow.add(Long.toString(item.getReturnTime()));
					tablevalues.add(onerow);
				}
				tbQueryCard = new JTable(tablevalues, columnNames) {
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};
				JScrollPane scrollPane = new JScrollPane(tbQueryCard);
				scrollPane.setBounds(0, 0, 9000, frame.getHeight());
				cardFrame.add(scrollPane, BorderLayout.CENTER);
			}
			cardFrame.repaint();
			cardFrame.setVisible(true);
		}
	}

	public class handerRegisterCard implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Card card = new Card(0, cardName.getText(), cardDepartment.getText(),
					cardType.getSelectedIndex() == 0 ? CardType.Teacher : CardType.Student);
			ApiResult result = library.registerCard(card);
			if (result.ok) {
				JOptionPane.showMessageDialog(null, "Id:" + card.getCardId() + " name:" + card.getName() +
						" department:" + card.getDepartment() + " type:" + card.getType().getStr(),
						"register success", JOptionPane.WARNING_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, result.message,
						"register failed",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}