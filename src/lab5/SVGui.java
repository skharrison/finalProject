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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SVGui extends JFrame 
{
	
	private JPanel cards;
	private File[] imageFiles;
	private List<ImageIcon> scaled;
	private Thread myRender;
	private static final long serialVersionUID = 1L;
	private final String IGV = "IGV Displayer";
	private final String CC = "Compute Coverage";
	private final String CST = "Color Sample Table";
	
	public SVGui(String title) 
	{
		super(title);
		setLocationRelativeTo(null);
		setSize(500,300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cards = new JPanel(new CardLayout());
		cards.add(igvDisplayPanel(),IGV);
		cards.add(compCovPanel(),CC);
		cards.add(comparePanel(),CST);
		getContentPane().add(toolsPanel(), BorderLayout.NORTH);
		getContentPane().add(cards, BorderLayout.CENTER);
		setVisible(true);
	}
	private JPanel toolsPanel() 
	{
		JPanel panel = new JPanel();
		String[] toolList = {IGV, CC, CST};
		JComboBox<String> toolCombo = new JComboBox<String>(toolList);
		JLabel toolText = new JLabel("Tools:");
		ItemListener toolItemListener = new ItemListener() 
		{
			public void itemStateChanged(ItemEvent evt) 
			{
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
	
	private JPanel igvDisplayPanel() 
	{
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
				try 
				{
					loadFromFile();
				} catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
		});
		
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
		this.imageFiles = files;
		ImageRender render = new ImageRender();
		myRender = new Thread(render);
		myRender.start();
	}
	
	private void buildIGVTable() throws IOException
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double imgSize = width * .85;
		int intSize = (int) imgSize;
		double chromC = (width - imgSize) * .37;
		int chromSize = (int) chromC;
		double ss = (width - imgSize) * .23;
		int startSize = (int) ss;
		double check = (width - imgSize) * .15;
		int checkSize = (int) check;
		double last =  (width - imgSize) *.025;
		int lastBit = (int) last;
		int left = (startSize + startSize + checkSize + lastBit);
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
		JButton addLabel = new JButton("Add Strain Labels");
		buttonPanel.add(addLabel);
		JButton saveButton = new JButton("Save Checked Regions");
		buttonPanel.add(saveButton);
		buttonPanel.setBackground(Color.cyan);
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
		JTextField myText = new JTextField();
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
		columnModel.getColumn(0).setPreferredWidth(chromSize);
		columnModel.getColumn(3).setPreferredWidth(checkSize);
		columnModel.getColumn(1).setPreferredWidth(startSize);
		columnModel.getColumn(2).setPreferredWidth(startSize);
		table.setFillsViewportHeight(true);
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.add(buttonPanel);
		headerPanel.add(jPanel);
		JPanel wholePanel = new JPanel();
		wholePanel.add(table);
		JScrollPane scrollPane = new JScrollPane(wholePanel);
		scrollPane.setColumnHeaderView(headerPanel);
		cards.add(scrollPane, "Image");
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, "Image");
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
	private JPanel compCovPanel() 
	{
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
				try 
				{
					selectTable();
				} catch (IOException e1) 
				{
					// TODO Auto-generated catch block
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
		if (tableFile.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
		{
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
			TableColumnModel columnModel = table.getColumnModel();
			table.setPreferredScrollableViewportSize(table.getPreferredSize());
			table.setRowHeight(75);
			JPanel tablePanel = new JPanel();
			tablePanel.add(new JScrollPane(table));
			cards.add(tablePanel, "Highlight Table");
			CardLayout cl = (CardLayout)(cards.getLayout());
			cl.show(cards, "Highlight Table");
		}
		else 
		{
		}
	}
	
	private void highlightTable(JTable table) 
	{
		
	}
	
	
	private void getCheckedData(JTable table)
	{
		for (int i = 0; i < table.getRowCount(); i++) 
		{
		     Boolean isChecked = Boolean.valueOf(table.getValueAt(i, 3).toString());

		     if (isChecked) 
		     {
		       System.out.println("");
		     } 
		}
	}
	
	public static void main(String[] args) 
	{
		new SVGui("SV Tools");
	}
	
//	private class JComponentTableCellRenderer implements TableCellRenderer
//	{
//		  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
//		      boolean hasFocus, int row, int column) 
//		  {
//		    return (Component) value;
//		  }
//	}
	
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
		
		
		public void run()
		{
			try
			{
				final List<ImageIcon> list = new ArrayList<ImageIcon>();
				for (File f : imageFiles)
				{
					Image img = ImageIO.read(f);
					Image scale = getScaledImage(img, imgWidth,250);
					ImageIcon imgI = new ImageIcon(scale);
					list.add(imgI);
					
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
				//not sure
			}
		}
	}
}
