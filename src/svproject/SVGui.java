package svproject;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class SVGui extends JFrame
{
	private JPanel cards;
	private File[] imageFiles;
	private List<ImageIcon> scaled;
	private Thread myRender;
	private static final long serialVersionUID = 1L;
	private final String blank = "blank";
	private final String IGV = "IGV Displayer";
	private final String CC = "Compute Coverage";
	private final String CST = "Color Sample Table";
	private String current = blank;
	private JTextField imageLabels;
	private JButton browser;
	private JTable imageTable;
	private Map<Integer,List<Integer>> highlightCells;
	private Color specifiedColor = Color.YELLOW;
	private JComboBox<String> colorCombo;
	private File[] bamFiles;
	private File bedFile;
	private File covOut;
	private String bedCommand;
	private Thread bedThread;
	private JButton submit;
	private JTextField bamLabel;
	private JTextField bedLabel;
	private JTextField outLabel;
	private ProgressMonitor renderMonitor;
	private JButton submitImages;
	private JTextArea renderText;
	private JProgressBar bedToolProgress;
	private JLabel bedToolOutText;
	private JButton addLabel;
	private boolean toSwitch;
	private boolean saidNo = false;
	private boolean isTable = false;

	public SVGui(String title)
	{
		super(title);
		setLocationRelativeTo(null);
		setSize(700,300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cards = new JPanel(new CardLayout());
		cards.add(new JPanel(),blank);
		cards.add(igvDisplayPanel(),IGV);
		cards.add(compCovPanel(),CC);
		cards.add(comparePanel(),CST);
		getContentPane().add(allTools(),BorderLayout.NORTH);
		getContentPane().add(cards, BorderLayout.CENTER);
		setVisible(true);
	}

	private JToolBar allTools()
	{
		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);
		JButton igvButton = new JButton("IGV Displayer");
		JButton bedButton = new JButton("Compute Coverage");
		JButton compButton = new JButton("Color Sample Table");
		igvButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (! bamLabel.getText().equals("") || ! bedLabel.getText().equals("") || ! outLabel.getText().equals("") || isTable || specifiedColor != Color.YELLOW) {
					toSwitch = true;
					switchTools(IGV, toSwitch);
				} else
				{
					toSwitch = false;
					switchTools(IGV, toSwitch);
				}
				if (! saidNo)
				{
					igvButton.setEnabled(false);
					bedButton.setEnabled(true);
					compButton.setEnabled(true);
				}
			}
		});
		bedButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (browser.isEnabled() || submitImages.isEnabled() || isTable || specifiedColor != Color.YELLOW) {
					toSwitch = true;
					switchTools(CC, toSwitch);
				} else
				{
					toSwitch = false;
					switchTools(CC, toSwitch);
				}
				if (! saidNo)
				{
					igvButton.setEnabled(true);
					bedButton.setEnabled(false);
					compButton.setEnabled(true);
				}
			}
		});
		compButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (! bamLabel.getText().equals("") || ! bedLabel.getText().equals("") || ! outLabel.getText().equals("") || browser.isEnabled() || submitImages.isEnabled()) {
					toSwitch = true;
					switchTools(CST, toSwitch);
				} else
				{
					toSwitch = false;
					switchTools(CST, toSwitch);
				}
				if (! saidNo)
				{
					igvButton.setEnabled(true);
					bedButton.setEnabled(true);
					compButton.setEnabled(false);
				}
				}
		});
		igvButton.setBackground(Color.pink);
		bedButton.setBackground(Color.CYAN);
		compButton.setBackground(Color.YELLOW);
		igvButton.setOpaque(true);
		bedButton.setOpaque(true);
		compButton.setOpaque(true);
		igvButton.setBorderPainted(false);
		bedButton.setBorderPainted(false);
		compButton.setBorderPainted(false);
		toolBar.add(igvButton);
		toolBar.addSeparator(new Dimension(5, 5));
		toolBar.add(bedButton);
		toolBar.addSeparator(new Dimension(5, 5));
		toolBar.add(compButton);
		toolBar.addSeparator();
        JSeparator separator = new JSeparator();
        separator.setOrientation(JSeparator.VERTICAL);
        toolBar.add(separator);
        toolBar.addSeparator();
		JButton reset = new JButton("Reset");
		JButton help = new JButton("Help");
		toolBar.add(reset);
		toolBar.add(help);
		reset.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				resetTools();
			}
		});
		help.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ev)
			{
			     try {
					java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://github.com/skharrison/progFinal"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return toolBar;
	}

	private JPanel igvDisplayPanel()
	{
		final JPanel panel = new JPanel();
		final JPanel all = new JPanel();
		browser = new JButton("Browse");
		panel.add(new JLabel("Upload Images"));
		panel.add(browser, BorderLayout.CENTER);
		addLabel = new JButton("Add Strain Labels");
		panel.setLayout(new FlowLayout());
		panel.add(addLabel, BorderLayout.CENTER);
		browser.setEnabled(false);
		browser.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					loadFromFile();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		addLabel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				String answer = JOptionPane.showInputDialog(panel,"Input Sample Names In order (comma seperated):", null);
				if (answer == null)
				{
					//Do nothing
				}
				else
				{
					String typedPattern = answer.toUpperCase();
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					double width = screenSize.getWidth();
					double imgSize = width * .85;
					final int intSize = (int) imgSize;
					makeSampleLabel(typedPattern, intSize);
				}
			}
		});
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		JPanel buttons = new JPanel();
		submitImages = new JButton("View Images");
		submitImages.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					renderMonitor = new ProgressMonitor(all, "Rendering Images","", 0, imageFiles.length);
					ImageRender render = new ImageRender();
					myRender = new Thread(render);
					renderMonitor.setProgress(0);
					myRender.start();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});
		submitImages.setEnabled(false);
		buttons.add(submitImages);
		all.add(panel);
		all.add(buttons);
		renderText = new JTextArea("",5,20);
		all.add(renderText);
		return all;
	}

	private void loadFromFile() throws IOException
	{
		JFileChooser jfc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "svg");
		jfc.addChoosableFileFilter(filter);
		jfc.setAcceptAllFileFilterUsed(true);
		jfc.setMultiSelectionEnabled(true);
		if (jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		if( jfc.getSelectedFile() == null)
		{
			return;
		}
		File[] files = jfc.getSelectedFiles();
		imageFiles = files;
		if (imageFiles != null)
		{
			submitImages.setEnabled(true);
		}
	}

	private void buildIGVTable() throws IOException
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double imgSize = width * .85;
		int intSize = (int) imgSize;
		double chromC = (width - imgSize) * .37;
		int chromSize = (int) chromC;
		double ss = (width - imgSize) * .24;
		int startSize = (int) ss;
		double check = (width - imgSize) * .15;
		int checkSize = (int) check;
		MyTableModel model = new MyTableModel();
		int index = 0;
		for (File f : imageFiles)
		{
			String fName = f.getName();
			String[] info = fName.split("_");
			String chrom = info[0] + "_" + info[1];
			String start = info[2];
			String stop = info[3];
			ImageIcon imgBed = scaled.get(index);
			model.addRow(new Object[] {chrom,start,stop,false,imgBed});
			index++;
		}
		JPanel buttonPanel = new JPanel();
		JButton saveButton = new JButton("Save Checked Regions");
		buttonPanel.add(saveButton,BorderLayout.WEST);
		buttonPanel.setBackground(Color.cyan);
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
		Font font = new Font("Courier", Font.BOLD,12);
		int textwidth = (int)(font.getStringBounds("Strain Labels:", frc).getWidth());
		double Wleft = (width - imgSize);
		int wl = (int) Wleft;
		int l = (wl - textwidth);
		float ll = l;
		float yep = ll * .90f;
		int left = (int) yep;
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
		JLabel label = new JLabel("Strain Labels:");
		label.setFont(font);
		jPanel.add(label);
		jPanel.add(Box.createHorizontalStrut(left));
		jPanel.add(imageLabels);
	    Border blackline = BorderFactory.createLineBorder(Color.black);
	    buttonPanel.setBorder(blackline);
		imageTable = new JTable(model);
		imageTable.setRowHeight(250);
		TableColumnModel columnModel = imageTable.getColumnModel();
		columnModel.getColumn(4).setPreferredWidth(intSize);
		columnModel.getColumn(0).setPreferredWidth(chromSize);
		columnModel.getColumn(3).setPreferredWidth(checkSize);
		columnModel.getColumn(1).setPreferredWidth(startSize);
		columnModel.getColumn(2).setPreferredWidth(startSize);
		imageTable.setFillsViewportHeight(true);
		saveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				getCheckedData(imageTable);
			}
		});
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.add(buttonPanel);
		headerPanel.add(jPanel);
		JPanel wholePanel = new JPanel();
		wholePanel.add(imageTable);
		JScrollPane scrollPane = new JScrollPane(wholePanel);
		scrollPane.setColumnHeaderView(headerPanel);
		cards.add(scrollPane, "Image");
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, "Image");
		this.setSize(screenSize);
	}

	private void makeSampleLabel(String labels, int width)
	{
		String[] allLabels = labels.split(",");
		String joinString = String.join("|", allLabels);
		int numSamples = allLabels.length;
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
		Font font = new Font("Courier",Font.BOLD,12);
		int textwidth = (int)(font.getStringBounds(joinString, frc).getWidth());
		int space = (int)(font.getStringBounds(" ", frc).getWidth());
		int allowableSp = (width - textwidth) / space;
		int howMany = (allowableSp) / (numSamples);
		List<String> ll = new ArrayList<String>();
		for (String a : allLabels)
		{
			int left = howMany/2;
			int right = howMany - left;
			ll.add("|");
			for (int i = 0; i < left; ++i)
			{
				ll.add(" ");
			}
			ll.add(a);
			for (int i = 0; i < right; ++i)
			{
				ll.add(" ");
			}
			if (a.equals(allLabels[allLabels.length-1]))
				ll.add("|");
		}
		String finalLabel = String.join("",ll);
		imageLabels = new JTextField(finalLabel);
		imageLabels.setFont(font);
		browser.setEnabled(true);
	}

	private void getCheckedData(JTable table)
	{
		JFileChooser jfc = new JFileChooser();
		if( jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		if( jfc.getSelectedFile() == null)
		{
			return;
		}
		File chosenFile = jfc.getSelectedFile();
		if( jfc.getSelectedFile().exists())
		{
			String message = "File " + jfc.getSelectedFile().getName() + " exists.  Overwrite?";
			if( JOptionPane.showConfirmDialog(this, message) !=
					JOptionPane.YES_OPTION)
					return;
		}
		try
		{
			BufferedWriter writer= new BufferedWriter(new FileWriter(chosenFile));
			for (int i = 0; i < table.getRowCount(); i++)
			{
			     Boolean isChecked = Boolean.valueOf(table.getValueAt(i, 3).toString());
			     if (isChecked)
			     {
			       writer.write(table.getModel().getValueAt(i, 0).toString());
			       writer.write("\t");
			       writer.write(table.getModel().getValueAt(i, 1).toString());
			       writer.write("\t");
			       writer.write(table.getModel().getValueAt(i, 2).toString());
			       writer.write("\n");
			     }
			}
			writer.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not write file", JOptionPane.ERROR_MESSAGE);
		}
	}

	private Image getScaledImage(Image srcImg, int w, int h)
	{
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();
		return resizedImg;
	}

	private JPanel compCovPanel()
	{
		JPanel covPanel = new JPanel();
		covPanel.setLayout(new BoxLayout(covPanel, BoxLayout.Y_AXIS));
		submit = new JButton("Submit");
		JPanel multiPanel = makeMultiPanel();
		covPanel.add(multiPanel);
		JPanel sPanel = new JPanel();
		sPanel.add(submit);
		bedToolProgress = new JProgressBar(0,100);
		bedToolProgress.setStringPainted(true);
		bedToolProgress.setString("Processing...");
		bedToolProgress.setVisible(false);
		covPanel.add(sPanel);
		bedToolOutText = new JLabel();
		covPanel.add(bedToolProgress);
		covPanel.add(Box.createVerticalStrut(20));
		covPanel.add(bedToolOutText);
		covPanel.add(Box.createVerticalStrut(20));
		submit.setEnabled(false);
		submit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					submit.setEnabled(false);
					bedToolProgress.setVisible(true);
					bedToolProgress.setIndeterminate(true);
					bedCommand = makeBedMultiCommand();
					CommandRunner myCommand = new CommandRunner();
					bedThread = new Thread(myCommand);
					bedThread.start();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});
		return covPanel;
	}

	private JPanel makeMultiPanel()
	{
		JLabel lbl = new JLabel("Bedtools Option: ");
		String[] choices = { "multicov", "igv"};
		JComboBox<String> cb = new JComboBox<String>(choices);
		JPanel multiPanel = new JPanel();
		multiPanel.setLayout(new GridLayout(4,3));
		JButton bamBrowse = new JButton("Browse");
		bamBrowse.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					loadInBams();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				if (bamFiles != null && bedFile != null && covOut != null)
				{
					submit.setEnabled(true);
				}
			}
		});
		JButton bedBrowse = new JButton("Browse");
		bedBrowse.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					loadBed();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				if (bamFiles != null && bedFile != null && covOut != null)
				{
					submit.setEnabled(true);
				}
			}
		});
		JButton outFile = new JButton("Browse");
		outFile.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					chooseOutput();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				if (bamFiles != null && bedFile != null && covOut != null)
				{
					submit.setEnabled(true);
				}
			}
		});
		JLabel blankLabel = new JLabel();
		bamLabel = new JTextField();
		bedLabel = new JTextField();
		outLabel = new JTextField();
		multiPanel.add(lbl);
		multiPanel.add(cb);
		multiPanel.add(blankLabel);
		multiPanel.add(new JLabel("Select Bam Files: "));
		multiPanel.add(bamBrowse);
		multiPanel.add(bamLabel);
		multiPanel.add(new JLabel("Input Bed File:  "));
		multiPanel.add(bedBrowse);
		multiPanel.add(bedLabel);
		multiPanel.add(new JLabel("Output File: "));
		multiPanel.add(outFile);
		multiPanel.add(outLabel);
		return multiPanel;
	}

	private void loadInBams() throws IOException
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f) {
				if(f.isDirectory())
				{
					return true;
				}
				else
				{
					return f.getName().toLowerCase().endsWith(".bam");
				}
			}
			@Override
			public String getDescription() {
				return ".bam";
			}
		});
		jfc.setMultiSelectionEnabled(true);
		if (jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		if( jfc.getSelectedFile() == null)
		{
			return;
		}
		File[] files = jfc.getSelectedFiles();
		if (files.length != 0  && files != null)
		{
			String fileText = getFileNames(files);
			bamLabel.setText(fileText);
			this.bamFiles = files;
		}
	}

	private String getFileNames(File[] names)
	{
		List<String> files = new ArrayList<String>();
		for (File f : names)
		{
			String fName = f.getName();
			files.add(fName);
		}
		String myFiles = String.join(",", files);
		return myFiles;
	}

	private void loadBed() throws IOException
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f) {
				if(f.isDirectory())
				{
					return true;
				}
				else
				{
					return f.getName().toLowerCase().endsWith(".bed");
				}
			}
			@Override
			public String getDescription() {
				return ".bed";
			}
		});
		if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File file = jfc.getSelectedFile();
			this.bedFile = file;
			String bedText = file.toString();
			bedLabel.setText(bedText);
		}
	}

	private void chooseOutput() throws IOException
	{
		JFileChooser jfc = new JFileChooser();
		if( jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		if( jfc.getSelectedFile() == null)
		{
			return;
		}
		File chosenFile = jfc.getSelectedFile();
		if( jfc.getSelectedFile().exists())
		{
			String message = "File " + jfc.getSelectedFile().getName() + " exists.  Overwrite?";
			if( JOptionPane.showConfirmDialog(this, message) !=
					JOptionPane.YES_OPTION)
					return;
		}
		this.covOut = chosenFile;
		String text = chosenFile.toString();
		outLabel.setText(text);
	}

	private String makeBedMultiCommand()
	{
		StringBuffer allBams = new StringBuffer();
		allBams.append("bedtools multicov -bams ");
		for (File f : bamFiles)
		{
			String fName = f.getAbsolutePath();
			allBams.append(fName);
			allBams.append(" ");
		}
		String myBed = bedFile.getAbsolutePath();
		allBams.append("-bed " + myBed);
		String outFile = covOut.getAbsolutePath();
		allBams.append(" > " + outFile);
		String theCommand = allBams.toString();
		System.out.println(theCommand);
		return theCommand;
	}

	private JPanel comparePanel() {
		String[] colors = {"Yellow", "Pink", "Red", "Cyan", "Green"};
		JPanel panel = new JPanel();
		JButton theBrowser = new JButton("Browse");
		JLabel colorLabel = new JLabel("Highlight Color");
		colorCombo = new JComboBox<String>(colors);
		colorCombo.setSelectedIndex(-1);
		panel.add(new JLabel("Input Table"));
		panel.add(theBrowser);
		panel.setLayout(new FlowLayout());
		colorCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try
				{
					String color = colorCombo.getSelectedItem().toString();
					if (color == "Cyan")
					{
						specifiedColor = Color.CYAN;
					}
					else if(color == "Pink")
					{
						specifiedColor = Color.PINK;
					}
					else if(color == "Red")
					{
						specifiedColor = Color.RED;
					}
					else if (color == "Green")
					{
						specifiedColor = Color.GREEN;
				}
					else if (color == "Yellow")
					{
						specifiedColor = Color.YELLOW;
					}
				}
				catch (NullPointerException ex)
				{
					specifiedColor = Color.YELLOW;
				}
			}
		});
		theBrowser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try
				{
					selectTable();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		panel.add(colorLabel);
		panel.add(colorCombo);
		return panel;
	}

	private void selectTable() throws IOException
	{
		JFileChooser tableFile = new JFileChooser();
		tableFile.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f) {
				if(f.isDirectory())
				{
					return true;
				}
				else
				{
					return f.getName().toLowerCase().endsWith(".txt");
				}
			}
			@Override
			public String getDescription() {
				return ".txt";
			}
		});
		if (tableFile.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			File file = tableFile.getSelectedFile();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String header = reader.readLine();
			String[] cols = header.split("\t");
			DefaultTableModel model = new DefaultTableModel(cols,0);
			for (String nextLine = reader.readLine(); nextLine != null; nextLine = reader.readLine())
			{
				model.addRow(nextLine.split("\t"));
			}
			reader.close();
			JTable table = new JTable();
			table.setModel(model);
			table.setPreferredSize(new Dimension(350,350));
			table.setPreferredScrollableViewportSize(table.getPreferredSize());
			table.setRowHeight(50);
			table.setRowHeight(50);
			highlightTable(table);
			JPanel tablePanel = new JPanel();
			tablePanel.setLayout(new BorderLayout());
			tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
			cards.add(tablePanel, "Highlight Table");
			isTable = true;
			CardLayout cl = (CardLayout)(cards.getLayout());
			cl.show(cards, "Highlight Table");
			current = "Highlight Table";
			this.setSize(screenSize);
		}
	}

	/*
	 * - iterates through columns, finds specified cells
	 * - highlight those cells
	 */

	private void highlightTable(JTable table)
	{
		highlightCells = new LinkedHashMap<Integer,List<Integer>>();
		Map<Integer,String> strains = new LinkedHashMap<Integer,String>();
		for(int i = 0; i < table.getModel().getRowCount(); i++)
		{
			strains.put(i,table.getModel().getValueAt(i, 4).toString());
		}
		for (Map.Entry<Integer, String> entry : strains.entrySet())
		{
			String[] sepStrains = entry.getValue().split(":");
			List<Integer> specCols = new ArrayList<Integer>();
			for (String s : sepStrains)
			{
				specCols.add(table.getColumn(s).getModelIndex());
			}
			highlightCells.putIfAbsent(entry.getKey(),specCols);
		}
		HighlightCellRenderer cellHighlight = new HighlightCellRenderer();
		for (int i = 0; i < table.getColumnCount(); i++)
		{
			TableColumn col = table.getColumnModel().getColumn(i);
			col.setCellRenderer(cellHighlight);
		}
	}

	/*
	 * - confirmation option pane when switching tools
	 */

	private void switchTools(String card, boolean toSwitch)
	{
		CardLayout cl = (CardLayout)(cards.getLayout());
		if (current != blank && current != card && toSwitch)
		{
			int change = JOptionPane.showConfirmDialog(null, "Are you sure you want to switch tools?", "Confirm Switch", JOptionPane.YES_NO_OPTION);
			if (change == JOptionPane.YES_OPTION)
			{
				cl.show(cards, card);
				current = card;
				resetTools();
				setSize(700,300);
			} else if (change == JOptionPane.NO_OPTION)
			{
				saidNo = true;
			}
		} else
		{
			cl.show(cards, card);
			current = card;
		}
	}

	/*
	 * - reset to the default panel for each tool
	 * - used for reset button and a yes confirmation on confirm dialog
	 */

	private void resetTools()
	{
		CardLayout cl = (CardLayout)(cards.getLayout());
		bamLabel.setText("");
		bedLabel.setText("");
		outLabel.setText("");
		addLabel.setEnabled(true);
		browser.setEnabled(false);
		submitImages.setEnabled(false);
		colorCombo.setSelectedIndex(-1);
		isTable = false;
		saidNo = false;
		scaled = null;
		imageLabels = null;
		bedToolProgress.setVisible(false);
		bedToolOutText.setText("");
		renderText.setText("");
		if (current == "Highlight Table")
		{
			cl.show(cards, CST);
			current = CST;
		} else if (current.equals("Image"))
        {
            cl.show(cards, IGV);
            current = IGV;
        }
        setSize(700,300);
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		new SVGui("SV Tools");
	}

	private class HighlightCellRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 1L;
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			List<Integer> rowToHighlight = new ArrayList<Integer>();
			for (Map.Entry<Integer, List<Integer>> entry : highlightCells.entrySet())
			{
				if (entry.getValue().contains(column))
				{
					rowToHighlight.add(entry.getKey());
				}
			}
			if (rowToHighlight.contains(row))
			{
				cell.setBackground(specifiedColor);
			}
			else
			{
				cell.setBackground(Color.WHITE);
			}
			return cell;
		}
	}

	private class CommandRunner implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				executeCommands();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		public void executeCommands() throws IOException, InterruptedException {
		    File tempScript = createTempScript();
		    try
		    {
		        ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
		        Process process = pb.start();
		        process.waitFor();
		    }
		    finally
		    {
		        SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						bedToolProgress.setString("Done!");
						bedToolProgress.setIndeterminate(false);
						bedToolOutText.setText("Coverage file saved to: " + covOut);
						submit.setEnabled(true);
					}
				});
		    }
		}
		public File createTempScript() throws IOException
		{
		    File tempScript = File.createTempFile("script", null);
		    OutputStreamWriter streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
		    PrintWriter printWriter = new PrintWriter(streamWriter);
		    printWriter.println("#!/bin/bash");
		    printWriter.println(bedCommand);
		    printWriter.close();
		    return tempScript;
		}
	}

	private class ImageRender implements Runnable
	{
		private final int imgWidth;
		public ImageRender()
		{
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			double width = screenSize.getWidth();
			double imgSize = width * .85;
			imgWidth = (int) imgSize;
		}
		@Override
		public void run()
		{
			try
			{
				final List<ImageIcon> list = new ArrayList<ImageIcon>();
				int i = 0;
				for (File f : imageFiles)
				{
					final int progress=i;
					Image img = ImageIO.read(f);
					Image scale = getScaledImage(img, imgWidth,250);
					ImageIcon imgI = new ImageIcon(scale);
					list.add(imgI);
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							renderMonitor.setProgress(progress);
			                  renderText.setText(renderText.getText()
			                     + String.format("Completed %d%% of task.\n", progress));
						}
					});
					i++;
				}
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						scaled = list;
						try
						{
							buildIGVTable();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				});
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}