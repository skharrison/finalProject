package lab5;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumnModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;   

public class SvDisplay extends JFrame 
{
	private static final long serialVersionUID = -2577401354224614779L;
	
	public SvDisplay()
	{
		super("SV VISUALIZATION");
		setLocationRelativeTo(null);
		setSize(400,300);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setJMenuBar(createMenuBar());
		setVisible(true);
		setLayout(new BorderLayout());
		
	}
	
	private JMenuBar createMenuBar() 
	{
		JMenuBar menuBar = new JMenuBar();
		JMenu Imenu = new JMenu("IGV Viewer");
		menuBar.add(Imenu);
		JMenuItem openFiles = new JMenuItem("Open Images");
		Imenu.add(openFiles);
		openFiles.setMnemonic('O');
		
		JMenu saveFile = new JMenu("Save Checked");
		Imenu.add(saveFile);
		
		JMenuItem savebed = new JMenuItem("Save bed");
		savebed.setToolTipText("Save regions checked in verified column to new bed file");
		saveFile.add(savebed);
		
		JMenuItem saveImg = new JMenuItem("Save images and regions to PDF");
		saveFile.add(saveImg);
		saveImg.setToolTipText("Save regions of interest including images to PDF file");
		
		JMenuItem newSession = new JMenuItem("Load New Session");
		Imenu.add(newSession);
		
		openFiles.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try {
					loadFromFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JMenu covMenu = new JMenu("Coverage Analysis");
		menuBar.add(covMenu);
		JMenuItem compCov = new JMenuItem("Compute");
		covMenu.add(compCov);
		compCov.setMnemonic('C');
		JMenuItem saveBed = new JMenuItem("Save");
		saveBed.setMnemonic('S');
		covMenu.add(saveBed);
		JCheckBox addNorm = new JCheckBox("Normalize Coverage");
		addNorm.setMnemonic('C');
		covMenu.add(addNorm);
		JMenu Cmenu = new JMenu("Display Comparisons");
		menuBar.add(Cmenu);
		
		return menuBar;
		
	}
	
	/* TODO
		- file chooser to remember last known directory
		- make sure that images in proper format (horizontal)
		-Better way to add in sample ID's then manual entry at top
		-want to base size of image render on how many samples there are in image
		-enable user to save as one big image?? not sure 
		-fix that must make frame bigger for info to display when first upload images
		-get working checkbox's
	*/
	
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
		this.add(jPanel, BorderLayout.NORTH);
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		this.setSize(screenSize);

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
	
	public static void main(String[] args)
	{
		new SvDisplay();
	}

}
