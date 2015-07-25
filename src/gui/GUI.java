package gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import xml.XMLException;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Stack;

import model.Movie;
import model.MovieLibrary;
import model.WindowsRegistry;

public class GUI {
	private static final ImageIcon applicationImage = new ImageIcon("movie.jpg");
	private static final ImageIcon cameraImage = new ImageIcon("camera.jpg");
	
	private static JFrame frame;
	private static JList<Movie> movieList;
	private static JPanel genreCheckPanel;
	private static JLabel moviePicture;
	private static JRadioButton orRadioButton;
	private static JMenuItem undoMenuItem;
	private static JMenuItem redoMenuItem;
		
	private static Stack<LinkedList<Movie>> eventStack = new Stack<LinkedList<Movie>>();
	private static Stack<LinkedList<Movie>> undoStack = new Stack<LinkedList<Movie>>();
	
	private static LinkedList<String> selectedGenres = new LinkedList<String>();
	private static boolean libraryHasChanged = false;

	public static void main(String[] args) {
		try {
			MovieLibrary.LoadLibrary();
			
			buildMainDisplay();
			buildMenu();
			
			frame.setVisible(true);
		}
		catch (XMLException ex) {
			String msg = "Error! Unable to load library from XML file: " + ex.getMessage();
			JOptionPane.showMessageDialog(frame, msg, "Library XML Load Error", JOptionPane.ERROR_MESSAGE);
		}		
	}
	
	private static void restoreDefaultSettings() {
		try {
		String locxStr = WindowsRegistry.read("defaultLocationx");
		String locyStr = WindowsRegistry.read("defaultLocationy");
		if (locxStr != null && locyStr != null) {
			int locx = Integer.parseInt(locxStr);
			int locy = Integer.parseInt(locyStr);
			frame.setLocation(locx, locy);
		}
		else
			frame.setLocation(5, 5);
		
		Dimension screenSize = frame.getToolkit().getScreenSize();
		final int OS_TASKBAR_HEIGHT = 40;
		int width = screenSize.width - 10;
		int height = screenSize.height - OS_TASKBAR_HEIGHT;
		
		String widthStr = WindowsRegistry.read("defaultWidth");
		String heightStr = WindowsRegistry.read("defaultHeight");
		if (widthStr != null && heightStr != null) {
			width = Integer.parseInt(widthStr);
			height = Integer.parseInt(heightStr);
		}
		
		String maxStr = WindowsRegistry.read("isMaximized");
		if (maxStr != null && maxStr.equalsIgnoreCase("true"))
			frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setSize(width, height);
		}
		catch (Exception ex) {
			frame.setLocation(5, 5);
			frame.setSize(frame.getToolkit().getScreenSize().width - 10,
						  frame.getToolkit().getScreenSize().height - 40);
		}
	}
	
	private static void buildMainDisplay() {
		frame = new JFrame("Movie Library");
		frame.setIconImage(applicationImage.getImage());
		restoreDefaultSettings();
		frame.getContentPane().setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					exitProgram();
				}
			});

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(20, 20, 20, 20);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		
		c.gridwidth = 2;
		frame.getContentPane().add(buildGenrePanel(), c);
		c.gridwidth = 1;
				
		c.insets = new Insets(0, 20, 20, 20);
		movieList = new JList<Movie>();
		movieList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (movieList.getSelectedValue() == null)
					return;
				
				Movie movie = (Movie)movieList.getSelectedValue();
				File file = movie.getPictureFile();
				if (file.exists())
					moviePicture.setIcon(resizePicture(new ImageIcon(file.getPath()), 150, 211));
				else
					moviePicture.setIcon(null);
			}
		});
		movieList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == 1 && e.getClickCount() >= 2) {
					int movieIndex = movieList.locationToIndex(e.getPoint());
					editMovie((Movie)movieList.getModel().getElementAt(movieIndex));
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}			
		});
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		c.weightx = 1.0;
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createTitledBorder("Movie List"));
		scrollPane.getViewport().add(movieList);
		frame.getContentPane().add(scrollPane, c);
		
		c.insets = new Insets(0, 0, 20, 20);
		c.weightx = 0;
		c.gridx++;
		moviePicture = new JLabel();
		moviePicture.setPreferredSize(new Dimension(150, 211));
		moviePicture.setMinimumSize(new Dimension(150, 211));
		moviePicture.setMaximumSize(new Dimension(150, 211));
		frame.getContentPane().add(moviePicture, c);		
	}

	private static JPanel buildGenrePanel() {
		JPanel genrePanel = new JPanel();
		genrePanel.setBorder(BorderFactory.createTitledBorder("Genre"));
		genrePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 0);
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
						
		genreCheckPanel = new JPanel();
		genreCheckPanel.setLayout(new GridBagLayout());
		buildGenreCheckBoxes();
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 3;
		genrePanel.add(genreCheckPanel, c);
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		
		c.insets = new Insets(5, 5, 5, 5);
		c.weightx = 0.1;
		c.gridx++;
		CustomButton selectAllButton = new CustomButton("Select All") {
			private static final long serialVersionUID = 1L;
			public void buttonPressed() {
				for (Component checkBox : genreCheckPanel.getComponents())
					if (checkBox instanceof JCheckBox)
						((JCheckBox)checkBox).setSelected(true);
			}
		};
		genrePanel.add(selectAllButton, c);
		selectAllButton.setMinimumSize(selectAllButton.getPreferredSize());
		
		c.gridy++;
		CustomButton clearButton = new CustomButton("Clear") {
			private static final long serialVersionUID = 1L;
			public void buttonPressed() {
				for (Component checkBox : genreCheckPanel.getComponents())
					if (checkBox instanceof JCheckBox)
						((JCheckBox)checkBox).setSelected(false);
			}
		};
		clearButton.setPreferredSize(selectAllButton.getPreferredSize());
		genrePanel.add(clearButton, c);
		clearButton.setMinimumSize(clearButton.getPreferredSize());
		
		c.gridy++;
		JPanel genreModePanel = new JPanel();
		genreModePanel.setBorder(BorderFactory.createTitledBorder(null, "Selection Mode", 
				TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
		ButtonGroup genreModeGroup = new ButtonGroup();
		orRadioButton = new JRadioButton("OR");
		orRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (selectedGenres.isEmpty())
					return;
				
				if (orRadioButton.isSelected())
					movieList.setListData(MovieLibrary.getAnyMoviesOfGenre(selectedGenres).toArray(new Movie[0]));
				else
					movieList.setListData(MovieLibrary.getOnlyMoviesOfGenre(selectedGenres).toArray(new Movie[0]));
				movieList.setSelectedIndex(0);
				movieList.requestFocusInWindow();
				
				eventStack.clear();
				undoStack.clear();
				undoMenuItem.setEnabled(false);
				redoMenuItem.setEnabled(false);
			}			
		});
		JRadioButton andRadioButton = new JRadioButton("AND");
		genreModeGroup.add(orRadioButton);
		genreModeGroup.add(andRadioButton);
		orRadioButton.setSelected(true);
		genreModePanel.add(orRadioButton);
		genreModePanel.add(andRadioButton);
		genrePanel.add(genreModePanel, c);
		genrePanel.setMinimumSize(genrePanel.getPreferredSize());
		
		return genrePanel;
	}
	
	private static void buildMenu() {
		JMenuBar menuBar = new JMenuBar();

		// LIBRARY MENU
		JMenu menu = new JMenu("Library");
		menu.setMnemonic(KeyEvent.VK_L);

		JMenuItem menuItem = new JMenuItem("Add Movie", KeyEvent.VK_A);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createMovie();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Edit Movie", KeyEvent.VK_E);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editMovie();
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Remove Movie", KeyEvent.VK_R);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeMovie();
			}
		});
		menu.add(menuItem);
		
		menu.addSeparator();

		menuItem = new JMenuItem("Save", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveLibrary();
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Load", KeyEvent.VK_L);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadLibrary();
			}
		});
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		exitProgram();
            }
        });
        menu.add(menuItem);

		menuBar.add(menu);
		
		// MOVIE MENU
		menu = new JMenu("Movie");
		menu.setMnemonic(KeyEvent.VK_M);
				
		menuItem = new JMenuItem("Hide Selected");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, 0));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selectedMovies = movieList.getSelectedValuesList().toArray();
				if (selectedMovies.length <= 0)
					return;
				
				int numSelected = movieList.getSelectedIndices().length;
				int lastSelectedIndex = movieList.getSelectedIndices()[numSelected - 1];
				lastSelectedIndex -= numSelected - 1;
				
				LinkedList<Movie> movies = new LinkedList<Movie>();
				for (int movieIdx = 0; movieIdx < movieList.getModel().getSize(); movieIdx++)
					movies.add((Movie)movieList.getModel().getElementAt(movieIdx));
				
				LinkedList<Movie> movieListEvent = new LinkedList<Movie>();
				movieListEvent.addAll(movies);
				eventStack.push(movieListEvent);
				undoStack.clear();
				undoMenuItem.setEnabled(true);
				redoMenuItem.setEnabled(false);
												
				for (int movieIdx = 0; movieIdx < selectedMovies.length; movieIdx++)
					movies.remove((Movie)selectedMovies[movieIdx]);
				
				if (lastSelectedIndex >= movies.size())
					lastSelectedIndex = movies.size() - 1;
				else if (lastSelectedIndex < 0)
					lastSelectedIndex = 0;
				
				movieList.setListData(movies.toArray(new Movie[0]));
				movieList.setSelectedIndex(lastSelectedIndex);
				movieList.requestFocusInWindow();
			}
		});
		menu.add(menuItem);
		
		menu.addSeparator();
		
		undoMenuItem = new JMenuItem("Undo List Change");
		undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		undoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LinkedList<Movie> movies = new LinkedList<Movie>();
				for (int movieIdx = 0; movieIdx < movieList.getModel().getSize(); movieIdx++)
					movies.add((Movie)movieList.getModel().getElementAt(movieIdx));
				undoStack.push(movies);
				
				movieList.setListData(eventStack.pop().toArray(new Movie[0]));
				movieList.setSelectedIndex(0);
				movieList.requestFocusInWindow();
				redoMenuItem.setEnabled(true);
				if (eventStack.isEmpty())
					undoMenuItem.setEnabled(false);
			}
		});
		menu.add(undoMenuItem);
		undoMenuItem.setEnabled(false);
		
		redoMenuItem = new JMenuItem("Redo List Change");
		redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		redoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LinkedList<Movie> movies = new LinkedList<Movie>();
				for (int movieIdx = 0; movieIdx < movieList.getModel().getSize(); movieIdx++)
					movies.add((Movie)movieList.getModel().getElementAt(movieIdx));
				eventStack.push(movies);
				
				movieList.setListData(undoStack.pop().toArray(new Movie[0]));
				movieList.setSelectedIndex(0);
				movieList.requestFocusInWindow();
				undoMenuItem.setEnabled(true);
				if (undoStack.isEmpty())
					redoMenuItem.setEnabled(false);
			}
		});
		menu.add(redoMenuItem);
		redoMenuItem.setEnabled(false);
		
		menuBar.add(menu);
		
		frame.setJMenuBar(menuBar);
	}
	
	private static void buildGenreCheckBoxes() {
		selectedGenres.clear();
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 3, 0, 3);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		genreCheckPanel.removeAll();
		for (final String genre : MovieLibrary.getGenres()) {
			final JCheckBox checkBox = new JCheckBox(genre);
			checkBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (checkBox.isSelected())
						selectedGenres.add(genre);
					else
						selectedGenres.remove(genre);
					
					if (orRadioButton.isSelected())
						movieList.setListData(MovieLibrary.getAnyMoviesOfGenre(selectedGenres).toArray(new Movie[0]));
					else
						movieList.setListData(MovieLibrary.getOnlyMoviesOfGenre(selectedGenres).toArray(new Movie[0]));
					movieList.setSelectedIndex(0);
					movieList.requestFocusInWindow();
					
					eventStack.clear();
					undoStack.clear();
					undoMenuItem.setEnabled(false);
					redoMenuItem.setEnabled(false);
				}
			});
			genreCheckPanel.add(checkBox, c);
			c.gridy++;
			if (c.gridy >= 6) {
				c.gridy = 0;
				c.gridx++;
			}
		}
	}
	
	private static ImageIcon resizePicture(ImageIcon picture, int width, int height) {
		Image image = picture.getImage();
		BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.createGraphics();
		int y = (image.getHeight(null) - height) / 2;
		g.drawImage(image, 0, y, width, height, null);
		return new ImageIcon(bi);
	}
	
	private static void createMovie() {		
		editMovie(null);
	}
	
	private static void editMovie() {
		final JDialog dialog = new JDialog(frame, "Select A Movie To Edit", JDialog.DEFAULT_MODALITY_TYPE);
		dialog.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(20, 20, 300, 20);
		c.anchor = GridBagConstraints.NORTH;
		
		final JComboBox<Movie> movieComboBox = new JComboBox<Movie>(MovieLibrary.getMovies().toArray(new Movie[0]));
		movieComboBox.setSelectedIndex(-1);
		movieComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (movieComboBox.getSelectedItem() != null) {
					dialog.dispose();
					editMovie((Movie)movieComboBox.getSelectedItem());
				}
			}
		});
		dialog.getContentPane().add(movieComboBox, c);		
		
		dialog.pack();
		dialog.setMinimumSize(new Dimension(400, 400));
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}
	
	private static void editMovie(final Movie movie) {
		String dialogTitle = "Edit Movie";
		if (movie == null)
			dialogTitle = "Add New Movie";
		final JDialog dialog = new JDialog(frame, dialogTitle, JDialog.DEFAULT_MODALITY_TYPE);
		
		final JPanel setGenrePanel = new JPanel();
		setGenrePanel.setLayout(new GridBagLayout());
		final GridBagConstraints panelc = new GridBagConstraints();
		panelc.insets = new Insets(0, 3, 0, 3);
		panelc.gridx = 0;
		panelc.gridy = 0;
		panelc.anchor = GridBagConstraints.WEST;
		setGenrePanel.removeAll();
		for (String genre : MovieLibrary.getGenres()) {
			JCheckBox checkBox = new JCheckBox(genre);
			if (movie != null && movie.containsGenre(genre))
				checkBox.setSelected(true);
			setGenrePanel.add(checkBox, panelc);
			panelc.gridy++;
			if (panelc.gridy >= 8) {
				panelc.gridy = 0;
				panelc.gridx++;
			}
		}
		
		dialog.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(20, 20, 0, 20);
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		String movieTitle = "Movie Title";
		if (movie != null)
			movieTitle = movie.getTitle();
		final JTextField titleField = new JTextField(movieTitle);
		titleField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				titleField.selectAll();
			}
			public void focusLost(FocusEvent e) {}
		});
		dialog.getContentPane().add(titleField, c);
		
		c.gridwidth = 2;
		c.gridy++;
		final JTextField genreField = new JTextField("Genre");
		genreField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				genreField.selectAll();
			}
			public void focusLost(FocusEvent e) {}
		});
		dialog.getContentPane().add(genreField, c);
		c.gridwidth = 1;
		
		c.gridx += 2;
		CustomButton genreButton = new CustomButton("Create New Genre") {
			private static final long serialVersionUID = 1L;
			public void buttonPressed() {
				String genre = genreField.getText();
				if (MovieLibrary.addGenre(genre)) {
					buildGenreCheckBoxes();				
					genreCheckPanel.validate();
					genreCheckPanel.repaint();
					
					JCheckBox checkBox = new JCheckBox(genre);
					setGenrePanel.add(checkBox, panelc);
					panelc.gridy++;
					if (panelc.gridy >= 8) {
						panelc.gridy = 0;
						panelc.gridx++;
					}
					dialog.pack();
					dialog.validate();
					dialog.repaint();
				}
			}
		};
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		dialog.getContentPane().add(genreButton, c);
		c.anchor = GridBagConstraints.CENTER;
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(setGenrePanel);
		dialog.getContentPane().add(scrollPane, c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weighty = 0;
		
		c.insets = new Insets(20, 20, 20, 0);
		c.gridy++;
		String saveText = "Save Changes";
		if (movie == null)
			saveText = "Add Movie";
		CustomButton saveButton = new CustomButton(saveText) {
			private static final long serialVersionUID = 1L;
			public void buttonPressed() {
				String action = "save changes";
				if (movie == null)
					action = "add movie";
				
				String title = titleField.getText().trim();
				if (title.isEmpty()) {
					String msg = "Unable to " + action + ": movies must have a title.";
					JOptionPane.showMessageDialog(dialog, msg, "Edit Movie Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				Movie newMovie = new Movie(title);
				for (Component component : setGenrePanel.getComponents())
					if (component instanceof JCheckBox)
						if (((JCheckBox)component).isSelected())
							newMovie.addGenre(((JCheckBox)component).getText());				
				
				if (newMovie.getGenreCount() <= 0) {
					String msg = "Unable to " + action + ": movies should have at least one genre.";
					JOptionPane.showMessageDialog(dialog, msg, "Edit Movie Error", JOptionPane.ERROR_MESSAGE);
					return;
				}					
				
				MovieLibrary.removeMovie(movie);
				if (MovieLibrary.addMovie(newMovie)) {
					libraryHasChanged = true;
					dialog.dispose();
				}
				else {
					MovieLibrary.addMovie(movie);
					String msg = "Unable to " + action + ": a movie with this same title already exists in the library.";
					JOptionPane.showMessageDialog(dialog, msg, "Edit Movie Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		dialog.getContentPane().add(saveButton, c);
		
		c.insets = new Insets(20, 60, 20, 0);
		c.gridx++;
		final CustomButton pictureButton = new CustomButton(cameraImage) {
			private static final long serialVersionUID = 1L;
			public void buttonPressed() {
				String tab = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				String message = "<html> To display an photo/picture with this movie, do the following: <br>";
				message += tab + " Make sure the picture you want is in .jpg format. <br>";
				message += tab + " Copy or move that picture to the \"" + new File("").getAbsolutePath() + "\\\" directory. <br>";
				String title = titleField.getText();
				if (title == null || title.isEmpty())
					title = "Movie Title";
				String filename = new Movie(title).getPictureFile().getName(); 
				message += tab + " Rename the picture \"" + filename + "\".";
				message += " </html>";
				JOptionPane.showMessageDialog(dialog, message, "Movie Photo", JOptionPane.INFORMATION_MESSAGE);
			}
		};
		dialog.getContentPane().add(pictureButton, c);
		
		c.insets = new Insets(20, 20, 20, 0);
		c.gridx++;
		CustomButton cancelButton = new CustomButton("Cancel") {
			private static final long serialVersionUID = 1L;
			public void buttonPressed() {
				dialog.dispose();
			}
		};
		dialog.getContentPane().add(cancelButton, c);
						
		titleField.requestFocusInWindow();
		titleField.selectAll();
		dialog.pack();
		dialog.setMinimumSize(new Dimension(400, 300));
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);		
	}
	
	private static void removeMovie() {
		final JDialog dialog = new JDialog(frame, "Remove Movies", JDialog.DEFAULT_MODALITY_TYPE);
		dialog.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(20, 20, 20, 20);
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1.0;
		
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		final JList<Movie> list = new JList<Movie>(MovieLibrary.getMovies().toArray(new Movie[0]));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(list);
		dialog.add(scrollPane, c);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		
		c.weighty = 0;
		c.gridy++;
		CustomButton removeSelectedButton = new CustomButton("Remove") {
			private static final long serialVersionUID = 1L;
			public void buttonPressed() {
				Object[] movies = list.getSelectedValuesList().toArray();
				if (movies.length <= 0)
					return;
				
				String msg = "Are you sure you want to permanently remove the selected movie from the library?";
				if (movies.length > 1)
					msg = "Are you sure you want to permanently remove these " + movies.length + " movies from the library?";
				int result = JOptionPane.showConfirmDialog(dialog, msg, "Remove Movies", JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					for (Object movie : movies) {
						MovieLibrary.removeMovie((Movie)movie);
						libraryHasChanged = true;
						list.setListData(MovieLibrary.getMovies().toArray(new Movie[0]));
					}
				}
			}
		};
		dialog.getContentPane().add(removeSelectedButton, c);
		
		c.anchor = GridBagConstraints.EAST;
		c.gridx++;
		CustomButton closeButton = new CustomButton("Close") {
			private static final long serialVersionUID = 1L;
			public void buttonPressed() {
				dialog.dispose();
			}
		};
		dialog.getContentPane().add(closeButton, c);
						
		dialog.pack();
		dialog.setMinimumSize(new Dimension(400, 400));
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}
	
	private static void saveLibrary() {		
		if (!libraryHasChanged)
			return;
		
		try {
			MovieLibrary.SaveLibrary();
			final JDialog dialog = new JDialog(frame);
			dialog.getContentPane().add(new JLabel("Saving..."));
			dialog.pack();
			dialog.setMinimumSize(new Dimension(100, 50));
			dialog.setLocationRelativeTo(frame);
			
			Timer timer = new Timer(1500, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
			timer.setRepeats(false);
			timer.start();
			dialog.setVisible(true);
			
			libraryHasChanged = false;
		}
		catch (XMLException ex) {
			String msg = "Error! Unable to save library to file: " + ex.getMessage();
			JOptionPane.showMessageDialog(frame, msg, "Library XML Load Error", JOptionPane.ERROR_MESSAGE);
		}	
	}
	
	private static void loadLibrary() {
		if (libraryHasChanged) {
			String prompt = "Would you like to restore from file and lose all changes that have been made?";
			int choice = JOptionPane.showConfirmDialog(frame, prompt, 
				"Movie Library", JOptionPane.YES_NO_CANCEL_OPTION);

			if (choice != JOptionPane.YES_OPTION)
				return;
		}
		
		try {
			MovieLibrary.LoadLibrary();
			buildGenreCheckBoxes();
			frame.validate();
			frame.repaint();
		}
		catch (XMLException ex) {
			String msg = "Error! Unable to load library from XML file: " + ex.getMessage();
			JOptionPane.showMessageDialog(frame, msg, "Library XML Load Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private static void exitProgram() {
		if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH)
			WindowsRegistry.write("isMaximized", "true");
		else {
			WindowsRegistry.write("isMaximized", "false");
			WindowsRegistry.write("defaultLocationx", Integer.toString(frame.getLocationOnScreen().x));
			WindowsRegistry.write("defaultLocationy", Integer.toString(frame.getLocationOnScreen().y));
			WindowsRegistry.write("defaultWidth", Integer.toString(frame.getSize().width));
			WindowsRegistry.write("defaultHeight", Integer.toString(frame.getSize().height));
		}
		
		if (libraryHasChanged) {
			String prompt = "Do you wish to save changes?";
			int choice = JOptionPane.showConfirmDialog(frame, prompt, 
				"Movie Library", JOptionPane.YES_NO_CANCEL_OPTION);

			if (choice == JOptionPane.YES_OPTION)
				saveLibrary();
			else if (choice != JOptionPane.NO_OPTION)
				return;
		}

		frame.dispose();
	}
}


