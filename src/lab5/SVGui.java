package lab5;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumnModel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SVGui extends JFrame {
	
	private JPanel cards;
	private static final long serialVersionUID = 1L;
	private final String IGV = "IGV Displayer";
	private final String CC = "Compute Coverage";
	private final String CST = "Color Sample Table";
	
	public SVGui(String title) {
		super(title);
		setLocationRelativeTo(null);
		//setSize(1000,500);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		double width = screenSize.getWidth();
//		double height = screenSize.getHeight();
//		int w = (int) width;
//		int h = (int) height;
		this.setSize(screenSize);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		cards = new JPanel(new CardLayout());
		//cards.setMaximumSize(new Dimension(w,h));
		cards.add(igvDisplayPanel(),IGV);
		cards.add(compCovPanel(),CC);
		cards.add(comparePanel(),CST);
		getContentPane().add(toolsPanel(), BorderLayout.NORTH);
		getContentPane().add(cards, BorderLayout.CENTER);
		setVisible(true);
	}

    
	private JPanel toolsPanel() {
		JPanel panel = new JPanel();
		String[] toolList = {IGV, CC, CST};
		JComboBox<String> toolCombo = new JComboBox<String>(toolList);
		JLabel toolText = new JLabel("Tools:");
		ItemListener toolItemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
			    CardLayout cl = (CardLayout)(cards.getLayout());
			    cl.show(cards, (String)evt.getItem());
			}
		};
		toolCombo.setEditable(false);
		toolCombo.addItemListener(toolItemListener);
		panel.setLayout(new FlowLayout());
		panel.add(toolText);
		panel.add(toolCombo);
		return panel;
	}
	
	private JPanel igvDisplayPanel() {
		JPanel panel = new JPanel(new CardLayout());
		JButton browser = new JButton("Browse");
		panel.setLayout(new FlowLayout());
		panel.add(new JLabel("Upload Images"));
		panel.add(browser);
		browser.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try {
					loadFromFile();
					//CardLayout cl = (CardLayout)(cards.getLayout());
					//cl.show(cards, "Image");
					//this.add(new JScrollPane(table), BorderLayout.CENTER);
					//this.setSize(screenSize);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
//		panel.add(new JLabel("Input BED file"));
//		panel.add(new JTextField(20));
//		panel.add(new JButton("Browse"));
//		panel.add(new JLabel("Sample Order"));
//		panel.add(new JTextField(20));
		
	

		return panel;
	}
	
	private void loadFromFile() throws IOException
	{
		
		JFileChooser jfc = new JFileChooser();
		jfc.setMultiSelectionEnabled(true);
	
		if (jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		if( jfc.getSelectedFile() == null)
		{
			return;
		}
		File[] files = jfc.getSelectedFiles();
		buildIGVTable(files);
	
	}
	
	private void buildIGVTable(File[] files) throws IOException
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double imgSize = width * .80;
		double whatLeft = (width - imgSize) * .65;
		int left = (int) whatLeft;
		int intSize = (int) imgSize;
		MyTableModel model = new MyTableModel();
		
		for (File f : files)
		{
			String fName = f.getName();
			String[] info = fName.split("_");
			String chrom = info[0] + "_" + info[1];
			String start = info[2];
			String stop = info[3];
			Image img = ImageIO.read(f);
			Image scale = getScaledImage(img, intSize,250);
			ImageIcon imgBed = new ImageIcon(scale);
			model.addRow(new Object[] {chrom,start,stop,false,imgBed});
		}
		
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
		//.setLayout(BorderLayout);
		JTextField myText = new JTextField(40);
		myText.setPreferredSize(new Dimension(intSize, 30));
		Font font = new Font("Courier", Font.BOLD,11);
		myText.setFont(font);
		JLabel label = new JLabel("Strain Labels: ");
		jPanel.add(label);
		Box.Filler hFill = new Box.Filler(new Dimension(5,0), new Dimension(left, 0), new Dimension(100, 0));
		jPanel.add(hFill);
		jPanel.add(myText);
		JTable table = new JTable(model);
		table.setRowHeight(250);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(4).setPreferredWidth(intSize);
		table.setFillsViewportHeight(true);
		//jPanel.add(JScrollPane(table), BorderLayout.CENTER)
		JPanel wholePanel = new JPanel();
		wholePanel.add(jPanel, BorderLayout.NORTH);
		wholePanel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		cards.add(wholePanel, "Image");
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, "Image");
//		this.add(jPanel, BorderLayout.NORTH);
//		this.add(new JScrollPane(table), BorderLayout.CENTER);
		//this.setSize(screenSize);

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
	private JPanel compCovPanel() {
		JPanel panel = new JPanel();
		panel.add(new JTextField("Compute Coverage tools to be displayed here."));
		return panel;
	}
	
	private JPanel comparePanel() {
		String[] colors = {"Pink", "Red", "Blue", "Green"};
		JPanel panel = new JPanel();
		JButton browser = new JButton("Browse");
		JLabel colorLabel = new JLabel("Highlight Color");
		JComboBox<String> colorCombo = new JComboBox<String>(colors);
		panel.add(new JLabel("Input Table"));
		panel.add(browser);
		panel.setLayout(new FlowLayout());
		browser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Stuff.");
			}
		});
		panel.add(colorLabel);
		panel.add(colorCombo);
		return panel;
	}


	
	public static void main(String[] args) {
		new SVGui("SV Tools");
	}

}
