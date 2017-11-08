package PNGto4BPP;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.FileInputStream;
import SpriteManipulator.*;

// class
public class PNGto4BPP {
	// version number
	static final String VERSION_TAG = "v1.3.5";

	// accepted extensions
	static final String[] IMAGEEXTS = { "png" }; // image import types
	static final String[] PALETTEEXTS = { "gpl", "pal", "txt" }; // ascii palette import types
	static final String[] BINARYEXTS = { "pal" }; // binary palette import types
	static final String[] SPREXTS = { "spr" }; // sprite file import types
	static final String[] ROMEXTS = { "sfc" }; // rom file import types
	static final String[] EXPORTEXTS = { "spr", "sfc" }; // export types
	static final String[] LOGEXTS = { "txt" }; // debug file types

	//These fields are utilized by functions
	static final JTextField imageName = new JTextField("");
	static final JTextField palName = new JTextField("");
	static final JTextField fileName = new JTextField("");

	// palette reading methods
	static String[] palChoices = {
				"Read ASCII (" + String.join(", ",PALETTEEXTS) +")",
				"Binary (YY-CHR .PAL)",
				"Extract from last block of PNG"
				};
	static final JComboBox<String>	palOptions = new JComboBox<String>(palChoices);
	static final JFrame frame = new JFrame("PNGto4BPP " + VERSION_TAG);

	static StringWriter debugLogging;
	static PrintWriter debugWriter;

	// Summary
	// Command line usage:
	// imgSrc: Full path for image
	// palMethod: palFileMethod [0:ASCII(.GPL|.PAL|.TXT), 1:Binary(YY-CHR .PAL), 2:Extract from Last Block of PNG]
	// palSrc:(Used if Method 0 or 1 selected): Full Path for Palette File.
	// sprTarget(optional): Name of sprite that will be created. Will default to name of imgSrc with new extension.
	// romTarget(optional): Path of ROM to patch.

	// main and stuff
	public static void main(String[] args) {
		//try to set metal
		try {
			UIManager.setLookAndFeel("metal");
		} catch (UnsupportedLookAndFeelException
				| ClassNotFoundException
				| InstantiationException
				| IllegalAccessException e) {
			// try to set System default
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException
					| ClassNotFoundException
					| InstantiationException
					| IllegalAccessException e2) {
					// do nothing
			} //end System
		} // end metal

		// window building
		final JFrame frame = new JFrame("PNGto4BPP " + VERSION_TAG);
		final JDialog debugFrame = new JDialog(frame, "Debug");
		final JDialog aboutFrame = new JDialog(frame, "About");
		final Dimension d = new Dimension(600,182);
		final Dimension d2 = new Dimension(600,600);
		final Dimension textFieldD = new Dimension(250, 20);
		final TextArea debugLog = new TextArea("Debug log:",0,0,TextArea.SCROLLBARS_VERTICAL_ONLY);
		debugLog.setEditable(false);
		final Container fullWrap = new Container();
		final Container frameWrap = frame.getContentPane();
		fullWrap.setLayout(new GridBagLayout());
		SpringLayout wrap = new SpringLayout();
		frameWrap.setLayout(wrap);
		frameWrap.setPreferredSize(d);
		GridBagConstraints w = new GridBagConstraints();
		w.fill = GridBagConstraints.HORIZONTAL;

		// image name
		final JButton imageBtn = new JButton("Load PNG");
		w.gridy = 0;
		w.gridx = 0;
		w.gridwidth = 2;
		fullWrap.add(imageName, w);
		w.gridx = 2;
		w.gridwidth = 1;
		fullWrap.add(imageBtn, w);

		// palette
		final JButton palBtn = new JButton("Load palette");
		w.gridy++;
		w.gridx = 0;
		w.gridwidth = 1;
		palName.setPreferredSize(textFieldD);
		fullWrap.add(palName, w);
		w.gridx = 1;
		fullWrap.add(palOptions, w);
		w.gridx = 2;
		fullWrap.add(palBtn, w);

		// save
		final JButton fileNameBtn = new JButton("Save/Patch to...");
		w.gridy++;
		w.gridx = 0;
		w.gridwidth = 2;
		fullWrap.add(fileName, w);
		w.gridx = 2;
		w.gridwidth = 1;
		fullWrap.add(fileNameBtn, w);

		// convert
		final JButton runBtn = new JButton("Convert");
		w.gridy++;
		w.gridx = 0;
		w.gridwidth = 3;
		fullWrap.add(runBtn, w);

		wrap.putConstraint(SpringLayout.NORTH, fullWrap, 15,
				SpringLayout.NORTH, frame);
		wrap.putConstraint(SpringLayout.EAST, fullWrap, 0,
				SpringLayout.EAST, frame);
		wrap.putConstraint(SpringLayout.WEST, fullWrap, 0,
				SpringLayout.WEST, frame);
		frameWrap.add(fullWrap);

		// Acknowledgments
		final TextArea peepsList = new TextArea("", 0,0,TextArea.SCROLLBARS_VERTICAL_ONLY);
		peepsList.setEditable(false);
		peepsList.append("Written by fatmanspanda"); // hey, that's me
		peepsList.append("\n\nSpecial thanks:\nMikeTrethewey"); // forced me to do this and falls in every category
		peepsList.append("\n\nCode contribution:\n");
		peepsList.append(String.join(", ",
				new String[] {
						"Zarby89", // a lot of conversion help
						"Glan", // various optimizations and bitwise help
						"CGG Zayik" // command line functions
				}));
		peepsList.append("\n\nTesting and feedback:\n");
		peepsList.append(String.join(", ",
				new String[] {
						"CGG Zayik", // test sprite contributor
						"RyuTech", // test sprite contributor
						"Damon" // test sprite contributor
				}));
		peepsList.append("\n\nResources and development:\n");
		peepsList.append(String.join(", ",
				new String[] {
						"Veetorp", // provided most valuable documentation
						"Zarby89", // various documentation and answers
						"Damon", // Paint.NET palettes
						"Sosuke3" // various snes code answers
				}));
		peepsList.append("\n\nIcon by:\n");
		peepsList.append(String.join(", ",
				new String[] {
						"Hoodyha"
				}));
		peepsList.append("\n\nUpdates at:\n");
		peepsList.append(String.join(", ", new String[] {
						"http://github.com/fatmanspanda/ALttPNG/wiki"
						}));
		aboutFrame.add(peepsList);

		// debug text
		final JPanel debugWrapper = new JPanel(new BorderLayout());
		final JButton clrLog = new JButton("Clear");
		final JButton expLog = new JButton("Export");
		debugWrapper.add(expLog,BorderLayout.EAST);
		debugFrame.add(debugLog);
		debugFrame.add(debugWrapper,BorderLayout.SOUTH);
		debugLogging = new StringWriter();
		debugWriter = new PrintWriter(debugLogging);
		debugLogging.write("Debug log:\n");

		// menu
		final JMenuBar menu = new JMenuBar();
		final JMenu fileMenu = new JMenu("File");
		final JMenuItem debug = new JMenuItem("Debug");
		ImageIcon bee = new ImageIcon(
				PNGto4BPP.class.getResource("/PNGto4BPP/Bee.png")
			);
		debug.setIcon(bee);
		fileMenu.add(debug);

		// exit
		final JMenuItem exit = new JMenuItem("Exit");
		ImageIcon mirror = new ImageIcon(
				PNGto4BPP.class.getResource("/PNGto4BPP/Mirror.png")
			);
		exit.setIcon(mirror);
		fileMenu.add(exit);
		exit.addActionListener(arg0 -> System.exit(0));

		menu.add(fileMenu);
		// end file menu

		// help menu
		final JMenu helpMenu = new JMenu("Help");

		// Acknowledgements
		final JMenuItem peeps = new JMenuItem("About");
		ImageIcon mapIcon = new ImageIcon(
				PNGto4BPP.class.getResource("/PNGto4BPP/Map.png")
			);
		peeps.setIcon(mapIcon);
		helpMenu.add(peeps);

		menu.add(helpMenu);
		// end help menu

		frame.setJMenuBar(menu);

		// file explorer
		final BetterJFileChooser explorer = new BetterJFileChooser();

		explorer.setCurrentDirectory(new File(".")); // quick way to set to current .jar loc

		// set filters
		FileNameExtensionFilter imgFilter =
				new FileNameExtensionFilter("PNG files", IMAGEEXTS);
		FileNameExtensionFilter palFilter =
				new FileNameExtensionFilter("Palette files (" +String.join(", ",PALETTEEXTS) +")",
						PALETTEEXTS);
		FileNameExtensionFilter binPalFilter =
				new FileNameExtensionFilter("YY-CHR palettes", BINARYEXTS);
		FileNameExtensionFilter sprFilter =
				new FileNameExtensionFilter("ALttP Sprite files", SPREXTS);
		FileNameExtensionFilter romFilter =
				new FileNameExtensionFilter("ROM files", ROMEXTS);
		FileNameExtensionFilter logFilter =
				new FileNameExtensionFilter("text files", LOGEXTS);

		explorer.setAcceptAllFileFilterUsed(false);

		frame.setSize(d);
		debugFrame.setSize(d2);
		aboutFrame.setSize(d2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocation(200,200);

		// ico
		final ImageIcon ico = new ImageIcon(
				PNGto4BPP.class.getResource("/PNGto4BPP/ico.png")
			);
		frame.setIconImage(ico.getImage());
		debugFrame.setIconImage(ico.getImage());
		aboutFrame.setIconImage(ico.getImage());

		frame.setVisible(true);
		// can't clear text due to wonky code
		// have to set a blank file instead
		final File EEE = new File("");

		// about
		peeps.addActionListener(
			arg0 -> {
				aboutFrame.setVisible(true);
			});

		// debug
		debug.addActionListener(
			arg0 -> {
				debugLog.setText(debugLogging.toString());
				debugFrame.setVisible(true);
			});

		// debug clear
		clrLog.addActionListener(
			arg0 -> {
				debugLogging.flush();
				debugLogging.getBuffer().setLength(0);
				debugLogging.write("Debug log:\n");
				debugLog.setText(debugLogging.toString());
			});

		// export log to a text file
		expLog.addActionListener(
			arg0 -> {
				explorer.removeAllFilters();
				explorer.setSelectedFile(new File("error log (" + System.currentTimeMillis() + ").txt"));
				explorer.setFileFilter(logFilter);
				int option = explorer.showSaveDialog(expLog);
				if (option == JFileChooser.CANCEL_OPTION) {
					return;
				}
				String n = "";
				try {
					n = explorer.getSelectedFile().getPath();
				} catch (NullPointerException e) {
					// do nothing
				} finally {
					if (!SpriteManipulator.testFileType(n,LOGEXTS)) {
						JOptionPane.showMessageDialog(frame,
								"Debug logs must be of the following extensions:\n" +
										String.join(", ",LOGEXTS),
								"Beep",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
				}

				PrintWriter logBugs;
				try {
					logBugs = new PrintWriter(n);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(frame,
							"There was a problem writing to the log!",
							"WOW",
							JOptionPane.WARNING_MESSAGE);
					e.printStackTrace(debugWriter);
					return;
				}
				logBugs.write(debugLogging.toString());
				logBugs.close();
				JOptionPane.showMessageDialog(frame,
						"Error log written to:\n" + n,
						"YAY",
						JOptionPane.PLAIN_MESSAGE);
			});

		// image button
		imageBtn.addActionListener(
			arg0 -> {
				explorer.removeAllFilters();
				explorer.setSelectedFile(EEE);
				explorer.setFileFilter(imgFilter);
				int option = explorer.showOpenDialog(imageBtn);
				if (option == JFileChooser.CANCEL_OPTION) {
					return;
				}
				String n = "";
				try {
					n = explorer.getSelectedFile().getPath();
				} catch (NullPointerException e) {
					// do nothing
				} finally {
					if (SpriteManipulator.testFileType(n,IMAGEEXTS)) {
						imageName.setText(n);
					}
				}
			});

		// palette button
		palBtn.addActionListener(
			arg0 -> {
				explorer.removeAllFilters();
				explorer.setSelectedFile(EEE);
				if (palOptions.getSelectedIndex() == 1) {
					explorer.setFileFilter(binPalFilter);
				}
				else {
					explorer.setFileFilter(palFilter);
				}
				int option = explorer.showOpenDialog(palBtn);
				if (option == JFileChooser.CANCEL_OPTION) {
					return;
				}
				String n = "";
				try {
					n = explorer.getSelectedFile().getPath();
				} catch (NullPointerException e) {
					// do nothing
				} finally {
					if (SpriteManipulator.testFileType(n,PALETTEEXTS)) {
						palName.setText(n);
					}
				}
			});

		// file name button
		fileNameBtn.addActionListener(
			arg0 -> {
				explorer.removeAllFilters();
				explorer.setSelectedFile(EEE);
				explorer.setFileFilter(sprFilter);
				explorer.addChoosableFileFilter(romFilter);
				int option = explorer.showSaveDialog(fileNameBtn);
				if (option == JFileChooser.CANCEL_OPTION) {
					return;
				}
				String n = "";
				try {
					n = explorer.getSelectedFile().getPath();
				} catch (NullPointerException e) {
					// do nothing
				} finally {
					if (!SpriteManipulator.testFileType(n,EXPORTEXTS)) {
						// if invalid filetype
						if(n.contains(".")) {
							changeExtension(n,"spr");
						} else {
							// no filetype, append spr
							n = n + ".spr";
						}
					}
					fileName.setText(n);
				}
			});

		// run button
		runBtn.addActionListener(
			arg0 -> {
				convertPngToSprite(false);
			});

		// If arguments are greater than 1, then we have the necessary arguments to do command line processing.
		if(args.length > 1) {
			// If we encountered no errors processing the arguments, then convert and end program.
			if(ProcessArgs(args)) {
				System.exit(0);
			}
			// Leave program open with the fields that succeeded in being set.
			else {
				System.out.println("Failed to process arguments.");
			}
		}

	}

// Summary
	// ProcessArgs checks if the arguments are valid, and if so, sets the TextFields and ComboBox with the values from the passed arguments.
	// Returns True if arguments were processed successfully. False if not.
	public static boolean ProcessArgs(String[] args) {
		if(args.length < 2 || args.length > 5) {
			return false;
		}
		String imgSrc = "";
		String palSrc = "";
		String sprTarget = "";
		String romTarget = "";
		int palOption = -1;
		boolean argumentErrorsFound = false;

		for(int i = 0; i < args.length; i++) {
			// Tokenize argument
			String[] tokens = args[i].split("=");
			// System.out.println(tokens[0]);

			// imgSrc: Full Path for Image
			// palOption: palFileOption [0:ASCII(.GPL|.PAL|.TXT), 1:Binary(YY-CHR .PAL), 2:Extract from Last Block of PNG]
			// palSrc:(Used if Option 0 or 1 selected) Full Path for Pal File.
			// sprTarget: Name of Sprite that will be created.
			if(tokens.length == 2) {
				switch(tokens[0]) {
					case "imgSrc":
						imgSrc = tokens[1];
						imageName.setText(imgSrc);
						break;
					case "palOption":
						if(IsInteger(tokens[i])) {
							palOption = Integer.parseInt(tokens[1]);

							if(palOption >= 0 && palOption < palOptions.getItemCount()) {
								palOptions.setSelectedIndex(palOption);
							}
							else {
								System.out.println("The palOption: " + palOption + " is out of range.");
								argumentErrorsFound = true;
							}
						}
						else {
							System.out.println("The argument: " +
									tokens[1] +
									" is not a valid integer to specify the palette option. " +
									"0: ASCII ; 1:Binary ; 2:Extract from last block of PNG");
							argumentErrorsFound = true;
						}
						break;
					case "palSrc":
						palSrc = tokens[1];
						palName.setText(palSrc);
						break;
					case "sprTarget":
						sprTarget = tokens[1];
						fileName.setText(sprTarget);
						break;
					case "romTarget":
						romTarget = tokens[1];
						break;
				}
			}
			else {
				System.out.println("The argument: " + args[i] + " is invalid.");
				argumentErrorsFound = true;
			}
		} // end loops

		if(argumentErrorsFound) {
			return false;
		}

		// Ensure imgSrc exists
		if(imgSrc == "") {
			System.out.println("No source image was specified or was not specified correctly.");
			argumentErrorsFound = true;
		}

		// enters here if palMethod is between 1-3
		if(palSrc == "" && (palOption == 0 || palOption == 1)) {
			System.out.println("No palette source was specified despite using a palette method that requires it.");
			argumentErrorsFound = true;
		}

		// If sprite target name is not set, use the img source name with .spr extension.
		if(sprTarget == "") {
			sprTarget = changeExtension(imgSrc, "spr");
			fileName.setText(sprTarget);
		}

		// If all arguments check out, lets finish everything we want to do.
		if(!argumentErrorsFound) {
			// Returns true if successful
			if(convertPngToSprite(true)) {

				if(romTarget != "") {
					try {
						// Push change to ROM
						UpdateRom(sprTarget, romTarget);
					} catch (IOException e) {
						System.out.println("ERROR: " + e);
						return false;
					}
				}
			}
		}
		return !argumentErrorsFound;
	}

	public static void UpdateRom(String sprTarget, String romTarget) throws IOException, FileNotFoundException {

		byte[] sprite_data = new byte[0x7078];

		// filestream open .spr file
		FileInputStream fsInput = new FileInputStream(sprTarget);
		fsInput.read(sprite_data);
		fsInput.close();
		SpriteManipulator.patchRom(sprite_data, romTarget);
	}

	public static boolean IsInteger(String string) {
		try {
			Integer.valueOf(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static String changeExtension(String file, String extension) {
		String filename = file;

		if (filename.contains(".")) {
			filename = filename.substring(0, filename.lastIndexOf('.'));
		}
		filename += "." + extension;

		return filename;
	}

	public static boolean convertPngToSprite(boolean ignoreSuccessMessage) {
		BufferedImage img;
		BufferedImage imgRead;
		byte[] pixels;
		String imgName = imageName.getText();
		String paletteName = palName.getText();
		File imageFile = new File(imgName);
		BufferedReader br;
		int[] palette = null;
		byte[] palData = null;
		byte[][][] eightbyeight;
		int palChoice = palOptions.getSelectedIndex(); // see which palette method we're using

		boolean extensionERR = false; // let the program spit out all extension errors at once
		boolean patchingROM = false;

		// test image type
		if (!SpriteManipulator.testFileType(imgName, IMAGEEXTS)) {
			JOptionPane.showMessageDialog(frame,
					"Images must be of the following extensions:\n" +
							String.join(", ", IMAGEEXTS),
					"Good job",
					JOptionPane.WARNING_MESSAGE);
			extensionERR = true;
		}

		// test palette type
		if (!SpriteManipulator.testFileType(paletteName, PALETTEEXTS) && (palChoice != 2)) {
			if(paletteName.length() == 0) {
				JOptionPane.showMessageDialog(frame,
						"No Palette source was specified despite using a palette method that requires it",
						"Oops",
						JOptionPane.WARNING_MESSAGE);
				extensionERR = true;
			}
			else {
				JOptionPane.showMessageDialog(frame,
						"Palettes must be of the following extensions:\n" +
								String.join(", ", PALETTEEXTS),
						"HEY! LISTEN!",
						JOptionPane.WARNING_MESSAGE);
				extensionERR = true;
			}
		}

		// save location
		String loc = fileName.getText();

		// default name
		if (loc.equals("")) {
			loc = imgName;
			try {
				loc = loc.substring(0,loc.lastIndexOf("."));
			} catch(StringIndexOutOfBoundsException e) {
				loc = "oops";
			} finally {
				// still add extension here so that the user isn't fooled into thinking they need this field
				loc += " (exported).spr";
			}
		}

		// sfc means we're patching a ROM
		if (SpriteManipulator.testFileType(loc,"sfc")) {
			patchingROM = true;
		}

		// only allow sprite/ROM files
		if (!SpriteManipulator.testFileType(loc, EXPORTEXTS)) {
			if(loc.contains(".")) {
				JOptionPane.showMessageDialog(frame,
						"Export location must be of the following extensions:\n" +
								String.join(", ", EXPORTEXTS),
								"C'mon",
								JOptionPane.WARNING_MESSAGE);
				extensionERR = true;
			} else {
				loc = loc + ".spr";
			}
		}

		// break if any extension related errors
		if (extensionERR) {
			return false;
		}

		// image file
		try {
			imgRead = ImageIO.read(imageFile);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(frame,
					"Image file not found",
					"Where'd it go?",
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace(debugWriter);
			return false;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame,
					"Error reading image",
					"Well huh",
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace(debugWriter);
			return false;
		}

		// convert to RGB colorspace
		img = SpriteManipulator.convertToABGR(imgRead);

		// image raster
		try {
			pixels = getImageRaster(img);
		} catch (BadDimensionsException e) {
			JOptionPane.showMessageDialog(frame,
					"Image dimensions must be 128x448",
					"Puh-lease",
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace(debugWriter);
			return false;
		}

		// round image raster
		pixels = SpriteManipulator.roundRaster(pixels);

		// explicit ASCII palette
		if (palChoice == 0) {
			// get palette file
			try {
				br = getPaletteFile(paletteName);
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(frame,
						"Palette file not found",
						"Hmmmmm",
						JOptionPane.WARNING_MESSAGE);
				e.printStackTrace(debugWriter);
				return false;
			}
			// palette parsing
			try {
				// test file type to determine format
				if (SpriteManipulator.testFileType(paletteName, "txt")) {
					palette = getPaletteColorsFromPaintNET(br);
				}
				else {
					palette = getPaletteColorsFromFile(br);
				}
				palette = SpriteManipulator.roundPalette(palette);
				palData = SpriteManipulator.getPalDataFromArray(palette);
			} catch (NumberFormatException|IOException e) {
				JOptionPane.showMessageDialog(frame,
						"Error reading palette",
						"Uhhhhhhh",
						JOptionPane.WARNING_MESSAGE);
				e.printStackTrace(debugWriter);
				return false;
			} catch (ShortPaletteException e) {
				JOptionPane.showMessageDialog(frame,
						"Unable to find 16 colors",
						"This one is YOUR fault",
						JOptionPane.WARNING_MESSAGE);
				e.printStackTrace(debugWriter);
				return false;
			}
		}

		// binary (YY-CHR) pal
		if (palChoice == 1) {
			if (!SpriteManipulator.testFileType(paletteName, "pal")) {
				JOptionPane.showMessageDialog(frame,
						"Binary palette reading must by a .PAL file",
						"Gosh dernit",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			try {
				byte[] palX = SpriteManipulator.readFile(paletteName);
				palette = palFromBinary(palX);
				palData = SpriteManipulator.getPalDataFromArray(palette);
			} catch(Exception e) {
				return false;
			}
		}

		// extract from last block
		if (palChoice == 2) {
			palette = palExtract(pixels);
			palData = SpriteManipulator.getPalDataFromArray(palette);
		}

		// make the file
		try {
			// only try to make a file if we're making a new sprite
			if (!patchingROM) {
				new File(loc);
			}
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(frame,
					"Invalid file name",
					"FFS",
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace(debugWriter);
		}

		// split bytes into blocks
		eightbyeight = SpriteManipulator.indexAnd8x8(pixels, palette);
		byte[] SNESdata = SpriteManipulator.exportToSPR(eightbyeight, palData);

		// write data to SPR file
		try {
			if (patchingROM) {
				SpriteManipulator.patchRom(SNESdata, loc);
			}
			else {
				SpriteManipulator.writeSPR(SNESdata, loc);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame,
					"Error writing sprite",
					"Drats!",
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace(debugWriter);
			return false;
		}

		if(!ignoreSuccessMessage) {
			// success
			JOptionPane.showMessageDialog(frame,
				"Sprite file successfully " +
				(patchingROM ? "patched" : "written") +
				" to:" + "\n" + (new File(loc).getName()),
				"YAY",
				JOptionPane.PLAIN_MESSAGE);
		}
		return true;
	}

	/**
	 * Get the full image raster
	 * @param img - image to read
	 * @throws BadDimensionsException if the image is not 128 pixels wide and 448 pixels tall
	 */
	public static byte[] getImageRaster(BufferedImage img) throws BadDimensionsException {
		int w = img.getWidth();
		int h = img.getHeight();
		if (w != 128 || h != 448) {
			throw new BadDimensionsException("Invalid dimensions of {" + w + "," + h + "}");
		}
		return SpriteManipulator.getImageRaster(img);
	}

	/**
	 * Finds the palette file (as a .gpl or .pal) from <tt>palPath<tt>
	 * @param palPath - full file path of the palette
	 * @throws FileNotFoundException
	 */
	public static BufferedReader getPaletteFile(String palPath)	throws FileNotFoundException {
		FileReader pal = new FileReader(palPath);
		BufferedReader ret = new BufferedReader(pal);
		return ret;
	}

	/**
	 * Reads a GIMP ({@code .gpl}) or Graphics Gale ({@code .pal}) palette file for colors.
	 * <br><br>
	 * This function first finds as many colors as it can from the palette.
	 * Once the palette is fully read, the number of colors recognized is
	 * rounded down to the nearest multiple of 16.
	 * Each multiple of 16 represents one of Link's mail palettes
	 * (green, blue, red, bunny).
	 * If fewer than 4 palettes are found, any empty palette is copied from green mail.
	 *
	 * @param pal - Palette to read
	 * @return {@code int[]} of 64 colors as integers (RRRGGGBBB)
	 * @throws ShortPaletteException Halts the process if enough colors are not found.
	 */
	public static int[] getPaletteColorsFromFile(BufferedReader pal)
			throws NumberFormatException, IOException, ShortPaletteException {
		int[] palret = new int[64];
		String line;

		// read palette
		int pali = 0;
		while ((line = pal.readLine()) != null) {
			// look for 3 numbers
			String[] line2 = (line.trim()).split("\\D+");
			int colori = 0;
			int[] colorArray = new int[3];
			if (line2.length >= 3) {
				for (String s : line2) {
					int curCol = -1;
					try {
						curCol = Integer.parseInt(s);
					} catch(NumberFormatException e) {
						// nothing
					} finally {
						colorArray[colori] = curCol;
						colori++;
					}
					if (colori == 3) {
						break;
					}
				}
				// read RGB bytes as ints
				int r = colorArray[0];
				int g = colorArray[1];
				int b = colorArray[2];
				palret[pali] = (r * 1000000) + (g * 1000) + b; // add to palette as RRRGGGBBB
				pali++; // increment palette index
			}
			if (pali == 64) {
				break;
			}
		}
		// short palettes throw an error
		if (pali < 16 ) {
			throw new ShortPaletteException("Only " + pali + " colors were found.");
		}
		// truncate long palettes
		int[] newret = new int[64];
		pali = 16 * (pali / 16);
		if (pali > 64) {
			pali = 64;
		}
		for (int i = 0; i < pali; i++) {
			newret[i] = palret[i];
		}
		if (pali < 64) {
			for (int i = pali; i < 64; i++) {
				newret[i] = palret[i%16];
			}
		}
		return newret;
	}

	/**
	 * Reads a Paint.NET palette ({@code .txt}) for colors.
	 * This method must be separate as Paint.NET uses HEX values to write colors.
	 * <br><br>
	 * This function firsts find as many colors as it can from the palette.
	 * Once the palette is fully read, the number of colors recognized is
	 * rounded down to the nearest multiple of 16.
	 * Each multiple of 16 represents one of Link's mail palettes
	 * (green, blue, red, bunny).
	 * If fewer than 4 palettes are found, any empty palette is copied from green mail.
	 *
	 * @param pal - Palette to read
	 * @return {@code int[]} of 64 colors as integers (RRRGGGBBB) of 64 colors as integers (RRRGGGBBB)
	 * @throws ShortPaletteException Halts the process if enough colors are not found.
	 */
	public static int[] getPaletteColorsFromPaintNET(BufferedReader pal)
			throws NumberFormatException, IOException, ShortPaletteException {
		int[] palret = new int[64];
		String line;

		// read palette
		int pali = 0;
		while ( (line = pal.readLine()) != null) {
			if (line.matches("[0-9A-F] {8}")) {
				char[] line2 = line.toCharArray();
				// read RGB bytes as ints
				int r = Integer.parseInt( ("" + line2[2] + line2[3]), 16);
				int g = Integer.parseInt( ("" + line2[4] + line2[5]), 16);
				int b = Integer.parseInt( ("" + line2[6] + line2[7]), 16);
				palret[pali] = (r * 1000000) + (g * 1000) + b; // add to palette as RRRGGGBBB
				pali++; // increment palette index
			}
			if (pali == 64) {
				break;
			}
		}
		// Paint.NET forces 96 colors, but put this here just in case
		if (pali < 16 ) {
			throw new ShortPaletteException("Only " + pali + " colors were found.");
		}
		// truncate long palettes
		int[] newret = new int[64];
		pali = 16 * (pali / 16);
		if (pali > 64) {
			pali = 64;
		}
		for (int i = 0; i < pali; i++) {
			newret[i] = palret[i];
		}
		if (pali < 64) {
			for (int i = pali; i < 64; i++) {
				newret[i] = palret[i%16];
			}
		}
		return palret;
	}

	/**
	 * Extracts palette colors from last 8x8 block of the image.
	 * Each row of this 8x8 block represents one-half of a mail palette.
	 * Row 1 contains green mail's colors 0x0&ndash;0x7;
	 * row 2 contains green mail's colors 0x8&ndash;0xF; etc.
	 * <br><br>
	 * If any pixel of the latter 3 mails matches the color at {0,0}
	 * (green mail's transparent pixel),
	 * it will be replaced with the corresponding color at green mail for that palette's index.
	 * This is done as an attempt to completely fill out all 64 colors of the palette.
	 * @param pixels - image raster, assumed ABGR
	 * @return
	 */
	public static int[] palExtract(byte[] pixels) {
		int[] palret = new int[64];
		int pali = 0;
		int startAt = (128 * 448 - 8) - (128 * 7);
		int endAt = startAt + (8 * 128);
		for (int i = startAt; i < endAt; i+= 128) {
			for (int j = 0; j < 8; j++) {
				int k = i + j;
				int b = (pixels[k*4+1]+256)%256;
				int g = (pixels[k*4+2]+256)%256;
				int r = (pixels[k*4+3]+256)%256;
				palret[pali] = (1000000 * r) + (1000 * g) + b;
				pali++;
			}
		}

		// fill out the palette by removing empty indices
		for (int i = 16; i < palret.length; i++) {
			if (palret[i] == palret[0])
				palret[i] = palret[i%16];
		}

		return palret;
	}

	public static int[] palFromBinary(byte[] pal) {
		int[] ret = new int[64];
		for (int i = 0; i < 64; i++) {
			int pos = (i * 3);
			int r = Byte.toUnsignedInt(pal[pos]);
			int g = Byte.toUnsignedInt(pal[pos+1]);
			int b = Byte.toUnsignedInt(pal[pos+2]);
			ret[i] = (r * 1000000) + (g * 1000) + b;
		}
		return ret;
	}

	// errors

	/**
	 * Palette has <16 colors
	 */
	@SuppressWarnings("serial")
	public static class ShortPaletteException extends Exception {
		public ShortPaletteException(String message) {
			super(message);
		}

		public ShortPaletteException() {}
	}

	/**
	 * Image is wrong dimensions
	 */
	@SuppressWarnings("serial")
	public static class BadDimensionsException extends Exception {
		public BadDimensionsException(String message) {
			super(message);
		}

		public BadDimensionsException() {}
	}
}