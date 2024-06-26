package scrollsexplorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;
import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import com.gg.slider.SideBar;
import com.gg.slider.SideBar.SideBarMode;
import com.gg.slider.SidebarSection;
import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;

import bsa.source.BsaMaterialsSource;
import bsa.source.BsaMeshSource;
import bsa.source.BsaSoundSource;
import bsa.source.BsaTextureSource;
import bsaio.ArchiveInputStream;
import bsaio.BSArchiveSetFile;
import client.BootStrap;
import esfilemanager.common.PluginException;
import esfilemanager.loader.ESMManagerFile;
import esfilemanager.loader.IESMManager;
import esfilemanager.tes3.MasterFile;
import esfilemanager.utils.source.EsmSoundKeyToName;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import javaawt.VMEventQueue;
import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import nativeLinker.LWJGLLinker;
import nif.BgsmSource;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.j3d.particles.tes3.J3dNiParticles;
import scrollsexplorer.simpleclient.BethWorldVisualBranch;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.SimpleWalkSetup;
import scrollsexplorer.simpleclient.SimpleWalkSetupInterface;
import scrollsexplorer.simpleclient.physics.DynamicsEngine;
import scrollsexplorer.simpleclient.settings.DistanceSettingsPanel;
import scrollsexplorer.simpleclient.settings.GeneralSettingsPanel;
import scrollsexplorer.simpleclient.settings.GraphicsSettingsPanel;
import scrollsexplorer.simpleclient.settings.MemoryStatusPanel;
import scrollsexplorer.simpleclient.settings.SetBethFoldersDialog;
import scrollsexplorer.simpleclient.settings.ShowOutlinesPanel;
import scrollsexplorer.simpleclient.tes3.Tes3Extensions;
import tools.io.ConfigLoader;
import tools.swing.TitledPanel;
import tools.swing.UserGuideDisplay;
import tools.swing.VerticalFlowLayout;
import tools3d.utils.YawPitch;
import tools3d.utils.loader.PropertyCodec;
import tools3d.utils.scenegraph.LocationUpdateListener;
import utils.source.MediaSources;
import utils.source.MeshSource;
import utils.source.SoundSource;
import utils.source.TextureSource;
import utils.source.file.FileMediaRoots;
import utils.source.file.FileMeshSource;
import utils.source.file.FileSoundSource;
import utils.source.file.FileTextureSource;

public class ScrollsExplorer extends JFrame implements BethRenderSettings.UpdateListener, LocationUpdateListener {
	public Dashboard						dashboard			= new Dashboard();

	private SimpleBethCellManager			simpleBethCellManager;

	private SimpleWalkSetupInterface		simpleWalkSetup;

	private static ESMCellTable				table;

	private DistanceSettingsPanel			distanceSettingsPanel;

	private GraphicsSettingsPanel			graphicsSettingsPanel;

	private ShowOutlinesPanel				showOutlinesPanel;

	private GeneralSettingsPanel			generalSettingsPanel;

	private MemoryStatusPanel				memoryStatusPanel;

	//	private DefaultTableModel tableModel;

	//	private String[] columnNames = new String[] { "File", "Int/Ext", "Cell Id", "Name" };

	private MediaSources					mediaSources;

	public IESMManager						esmManager;

	public BSArchiveSetFile					bsaFileSet;

	private GameConfig						selectedGameConfig	= null;

	private HashMap<GameConfig, JButton>	gameButtons			= new HashMap<GameConfig, JButton>();

	public JPanel							mainPanel			= new JPanel();

	public JPanel							buttonPanel			= new JPanel();

	public JPanel							quickEdit			= new JPanel();

	public JCheckBoxMenuItem				cbLoadAllMenuItem	= new JCheckBoxMenuItem("Load all BSA Archives", true);

	public JCheckBoxMenuItem				cbBsaMenuItem		= new JCheckBoxMenuItem("Use BSA not Files", true);

	public JCheckBoxMenuItem				cbAzertyKB			= new JCheckBoxMenuItem("Azerty", false);

	public JMenuItem						setFolders			= new JMenuItem("Set Folders");

	public JMenuItem						setGraphics			= new JMenuItem("Set Graphics");

	public JMenuItem						showUserGuide		= new JMenuItem("User Guide");

	public JMenuItem						loadSaveGame		= new JMenuItem("Load Save Game");

	private UserGuideDisplay				ugd					= new UserGuideDisplay();

	private Preferences						prefs;

	private boolean							autoLoadStartCell	= true;

	private boolean							LOAD_ESP_FILES		= false;

	private Tes3Extensions					tes3Extensions;

	public ScrollsExplorer() {
		super("ScrollsExplorer");
		
		BethRenderSettings.setFarLoadGridCount(16);
		BethRenderSettings.setLOD_LOAD_DIST_MAX(128);
		BethRenderSettings.setNearLoadGridCount(4);
		
		
		
		// debug for memory leaks
/*		BethWorldVisualBranch.SHOW_DEBUG_MAKERS = true;
		BethRenderSettings.setFarLoadGridCount(0);
		BethRenderSettings.setLOD_LOAD_DIST_MAX(0);
		BethRenderSettings.setNearLoadGridCount(0);*/
		
	//	J3dNiTransformInterpolator.CACHE_WEAK = false;
	//	NiGeometryAppearanceFixed.CACHE_WEAK = false;
	//	J3dNiBSplineCompTransformInterpolator.CACHE_WEAK = false;
	//	BhkShapeToCollisionShape.CACHE_WEAK = false;
	//	RootCollisionNodeToCollisionShape.CACHE_WEAK = false;
	//	J3dNiPathInterpolator.CACHE_WEAK = false;
	//	J3dRECOType.SHARE_MODELS = false;
	//	TreeMaker.SHARE_MODELS = false;
	
		BethWorldVisualBranch.LOAD_PHYS_FROM_VIS = false;  // true for this should now work, there was a bug p.x,p.x rather than p.x,-p.z

		BsaTextureSource.allowedTextureFormats = BsaTextureSource.AllowedTextureFormats.ALL;// just for debug of FO76

		javaawt.image.BufferedImage.installBufferedImageDelegate(VMBufferedImage.class);
		javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);
		javaawt.EventQueue.installEventQueueImpl(VMEventQueue.class);

		NiGeometryAppearanceFactoryShader.setAsDefault();
		CompressedTextureLoader.setAnisotropicFilterDegree(8);

		DynamicsEngine.MAX_SUB_STEPS = 5;

		try {
			PropertyLoader.load();

			prefs = Preferences.userNodeForPackage(ScrollsExplorerNewt.class);

			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.getContentPane().setLayout(new BorderLayout(1, 1));
			this.setSize(600, 800);

			mainPanel.setLayout(new BorderLayout());

			JMenuBar menuBar = new JMenuBar();
			menuBar.setOpaque(true);
			JMenu fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
			menuBar.add(fileMenu);

			boolean loadAll = Boolean.parseBoolean(prefs.get("load.all", "true"));
			cbLoadAllMenuItem.setSelected(loadAll);
			fileMenu.add(cbLoadAllMenuItem);

			boolean useBsa = Boolean.parseBoolean(prefs.get("use.bsa", "true"));
			cbBsaMenuItem.setSelected(useBsa);
			fileMenu.add(cbBsaMenuItem);

			boolean useAzerty = Boolean.parseBoolean(prefs.get("use.azerty", "false"));
			cbAzertyKB.setSelected(useAzerty);
			fileMenu.add(cbAzertyKB);
			cbAzertyKB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					simpleWalkSetup.setAzerty(cbAzertyKB.isSelected());
				}
			});

			fileMenu.add(setFolders);
			setFolders.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setFolders();
				}
			});

			fileMenu.add(setGraphics);
			setGraphics.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					simpleWalkSetup.resetGraphicsSetting();
				}
			});

			fileMenu.add(loadSaveGame);
			loadSaveGame.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					loadSaveGame();
				}
			});
			loadSaveGame.setEnabled(false);

			JMenu helpMenu = new JMenu("Help");
			helpMenu.setMnemonic(KeyEvent.VK_H);
			menuBar.add(helpMenu);

			helpMenu.add(showUserGuide);
			showUserGuide.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					showUserGuide();
				}
			});

			this.setJMenuBar(menuBar);
			//this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			//this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

			buttonPanel.setLayout(new GridLayout(-1, 3));

			for (final GameConfig gameConfig : GameConfig.allGameConfigs) {
				JButton gameButton = new JButton(gameConfig.gameName);
				buttonPanel.add(gameButton);
				gameButton.setEnabled(false);
				gameButtons.put(gameConfig, gameButton);
				gameButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setSelectedGameConfig(gameConfig);
					}
				});
			}

			simpleWalkSetup = new SimpleWalkSetup("SimpleBethCellManager");
			simpleWalkSetup.setAzerty(cbAzertyKB.isSelected());
			quickEdit.setLayout(new VerticalFlowLayout());
			quickEdit.add(new TitledPanel("Location", locField));
			quickEdit.add(new TitledPanel("Go To", warpPanel));
			mainPanel.add(buttonPanel, BorderLayout.NORTH);
			table = new ESMCellTable(this);
			mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

			mainPanel.add(dashboard.getMainPanel(), BorderLayout.SOUTH);

			simpleBethCellManager = new SimpleBethCellManager(simpleWalkSetup);

			distanceSettingsPanel = new DistanceSettingsPanel();
			graphicsSettingsPanel = new GraphicsSettingsPanel();
			showOutlinesPanel = new ShowOutlinesPanel(simpleWalkSetup);
			generalSettingsPanel = new GeneralSettingsPanel(this);
			memoryStatusPanel = new MemoryStatusPanel();

			BethRenderSettings.setFogEnabled(false);

			BethRenderSettings.addUpdateListener(this);

			this.getContentPane().invalidate();
			this.getContentPane().validate();
			this.getContentPane().doLayout();
			this.invalidate();
			this.validate();
			this.doLayout();

			SideBar sideBar = new SideBar(SideBarMode.TOP_LEVEL, true, 200, true);
			//SidebarSection ss1 = new SidebarSection(sideBar, "dashboard", dashboard, null);
			//sideBar.addSection(ss1);
			SidebarSection ss2 = new SidebarSection(sideBar, "Avartar", quickEdit, null);
			sideBar.addSection(ss2);
			SidebarSection ss3 = new SidebarSection(sideBar, "General", generalSettingsPanel, null);
			sideBar.addSection(ss3);
			SidebarSection ss4b = new SidebarSection(sideBar, "Distances", distanceSettingsPanel, null);
			sideBar.addSection(ss4b);
			SidebarSection ss4 = new SidebarSection(sideBar, "Graphics", graphicsSettingsPanel, null);
			sideBar.addSection(ss4);
			SidebarSection ss5 = new SidebarSection(sideBar, "Outlines", showOutlinesPanel, null);
			sideBar.addSection(ss5);
			SidebarSection ss6 = new SidebarSection(sideBar, "Memory", memoryStatusPanel, null);
			sideBar.addSection(ss6);

			this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			this.getContentPane().add(sideBar, BorderLayout.WEST);

			this.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent arg0) {
					//Just until the real window listens to damn events properly!					
					simpleWalkSetup.closingTime();
					simpleBethCellManager.closingTime();
					closingTime();

				}
			});

			setVisible(true);// need to be visible in case of set folders
			// My system for guarantees rendering of a component (test this)
			this.setFont(this.getFont());
			enableButtons();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// MY system for guarantee rendering of a component (test this)
		this.setFont(this.getFont());

		warpPanel.setLayout(new FlowLayout());
		warpPanel.add(warpField);
		warpField.setSize(200, 20);
		ActionListener warpActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String warp = warpField.getText().trim();
				String[] parts = warp.split("[^\\d-]+");

				if (parts.length == 3) {
					simpleWalkSetup.warp(new Vector3f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]),
							Float.parseFloat(parts[2])));
				}
			}
		};
		warpField.addActionListener(warpActionListener);
		JButton warpButton = new JButton("Go");
		warpPanel.add(warpButton);
		warpButton.addActionListener(warpActionListener);

		simpleWalkSetup.getAvatarLocation().addAvatarLocationListener(this);

	}

	private JTextField	locField	= new JTextField("0000,0000,0000");

	private JPanel		warpPanel	= new JPanel();

	private JTextField	warpField	= new JTextField("                ");

	public Component getLocField() {
		return locField;
	}

	public Component getWarpField() {
		return warpPanel;
	}

	private long lastLocationUpdate = 0;

	@Override
	public void locationUpdated(Quat4f rot, Vector3f trans) {
		if (System.currentTimeMillis() - lastLocationUpdate > 200) {
			//oddly this is mildly expensive so only update 5 times per second
			locField.setText(("" + trans.x).split("\\.")[0] + "," + ("" + trans.y).split("\\.")[0] + ","
								+ ("" + trans.z).split("\\.")[0]);
			lastLocationUpdate = System.currentTimeMillis();
		}
	}

	protected void showUserGuide() {
		ugd.display(this, "docs\\userGuide.htm");
	}

	public void closingTime() {
		if (esmManager != null) {
			PropertyLoader.properties.setProperty("YawPitch" + esmManager.getName(),
					new YawPitch(simpleWalkSetup.getAvatarLocation().getTransform()).toString());
			PropertyLoader.properties.setProperty("Trans" + esmManager.getName(),
					"" + PropertyCodec.vector3fIn(simpleWalkSetup.getAvatarLocation().get(new Vector3f())));
			PropertyLoader.properties.setProperty("CellId" + esmManager.getName(),
					"" + simpleBethCellManager.getCurrentCellFormId());
		}
		PropertyLoader.save();

		prefs.put("use.bsa", Boolean.toString(cbBsaMenuItem.isSelected()));
		prefs.put("load.all", Boolean.toString(cbLoadAllMenuItem.isSelected()));
		prefs.put("use.azerty", Boolean.toString(cbAzertyKB.isSelected()));
		
		//FIXME: something is keeping me alive ! started about when I added proper thread pools to bethworld vis loading, but it's not them
		// maybe there's a thread in the bytebuffer pool code in the sa load?
		//ArchiveInputStream.pool.close();//no it's not that
		System.exit(0);
	}

	private void setFolders() {
		SetBethFoldersDialog setBethFoldersDialog = new SetBethFoldersDialog(this);
		setBethFoldersDialog.setSize(400, 400);
		setBethFoldersDialog.setVisible(true);
		setBethFoldersDialog.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				enableButtons();
			}
		});

	}

	private void enableButtons() {
		boolean noFoldersSet = true;
		for (GameConfig gameConfig : GameConfig.allGameConfigs) {
			JButton gameButton = gameButtons.get(gameConfig);
			// must have no game selected and have a folder and folder must ahve right files
			boolean enable = selectedGameConfig == null && gameConfig.scrollsFolder != null
								&& hasESMAndBSAFiles(gameConfig);
			gameButton.setEnabled(enable);
			noFoldersSet = noFoldersSet && gameConfig.scrollsFolder == null;
		}

		//in case of nothing selected show dialog, funny infinite loop for recidivist non-setters
		if (noFoldersSet) {
			showUserGuide();
			setFolders();
		}
		mainPanel.validate();
		mainPanel.invalidate();
		mainPanel.doLayout();
		mainPanel.repaint();
	}

	private static boolean hasESMAndBSAFiles(GameConfig gameConfig) {
		// check to ensure the esm file and at least one bsa file are in the folder
		File checkEsm = new File(gameConfig.scrollsFolder, gameConfig.mainESMFile);
		if (!checkEsm.exists()) {
			return false;
		}

		int countOfBsa = 0;
		File checkBsa = new File(gameConfig.scrollsFolder);
		for (File f : checkBsa.listFiles()) {
			countOfBsa += f.getName().toLowerCase().endsWith(".bsa") ? 1 : 0;
			countOfBsa += f.getName().toLowerCase().endsWith(".ba2") ? 1 : 0;
		}

		if (countOfBsa == 0) {
			return false;
		}

		return true;
	}

	@Override
	public void renderSettingsUpdated() {
		simpleBethCellManager.updateBranches();
	}

	/**
	 
	 */
	private void setSelectedGameConfig(GameConfig newGameConfig) {
		selectedGameConfig = newGameConfig;
		enableButtons();
		simpleWalkSetup.getAvatarCollisionInfo().setAvatarYHeight(selectedGameConfig.avatarYHeight);

		Thread t = new Thread("ESM Master File load") {
			@Override
			public void run() {
				synchronized (selectedGameConfig) {
					IDashboard.dashboard.setEsmLoading(1);

					System.out.println("ESM Master File loading: " + selectedGameConfig.getESMPath());
					esmManager = ESMManagerFile.getESMManager(selectedGameConfig.getESMPath());
					bsaFileSet = null;
					if (esmManager != null) {

						if (LOAD_ESP_FILES) {
							//Lets load up the esp files too! search the same folder
							File dir = new File(selectedGameConfig.getESMPath()).getParentFile();
							for (File f : dir.listFiles()) {
								if (f.getName().endsWith(".esp")
									|| (f.getName().endsWith(".esm") && !f.getName().equals(esmManager.getName()))) {
									System.out.println("ESM File loading: " + f.getAbsolutePath());
									esmManager.addMaster(f.getAbsolutePath());
								}
							}
						}

						//TODO: all these should be connected strongly to GameConfig
						if (esmManager.getName().indexOf("Morrowind") != -1) {
							J3dLAND.setTes3();
							BethRenderSettings.setTes3(true);
						} else if (selectedGameConfig.folderKey.startsWith("FallOut4")) {
						
							BethRenderSettings.setFarLoadGridCount(8);
							BethRenderSettings.setLOD_LOAD_DIST_MAX(128);
							BethRenderSettings.setNearLoadGridCount(2);
						}

						YawPitch yp = YawPitch.parse(PropertyLoader.properties
								.getProperty("YawPitch" + esmManager.getName(), selectedGameConfig.startYP.toString()));
						Vector3f trans = PropertyCodec.vector3fOut(PropertyLoader.properties.getProperty(
								"Trans" + esmManager.getName(), selectedGameConfig.startLocation.toString()));
						int prevCellformid = Integer
								.parseInt(PropertyLoader.properties.getProperty("CellId" + esmManager.getName(), "-1"));
						simpleWalkSetup.getAvatarLocation().set(yp.get(new Quat4f()), trans);

						if (prevCellformid == -1) {
							prevCellformid = selectedGameConfig.startCellId;
						}

						new EsmSoundKeyToName(esmManager);
						MeshSource meshSource;
						TextureSource textureSource;
						SoundSource soundSource;
						BgsmSource materialsSource;

						if (cbBsaMenuItem.isSelected()) {
							if (bsaFileSet == null) {
								bsaFileSet = new BSArchiveSetFile(new String[] {selectedGameConfig.scrollsFolder},
										cbLoadAllMenuItem.isSelected());
							}

							if (bsaFileSet.size() == 0) {
								JOptionPane.showMessageDialog(
										ScrollsExplorer.this, selectedGameConfig.scrollsFolder
																+ " contains no *.bsa files nothing can be loaded");
								setFolders();
								IDashboard.dashboard.setEsmLoading(-1);
								return;
							}

							meshSource = new BsaMeshSource(bsaFileSet);
							textureSource = new BsaTextureSource(bsaFileSet);
							soundSource = new BsaSoundSource(bsaFileSet, null);//new EsmSoundKeyToName(esmManager));
							materialsSource = new BsaMaterialsSource(bsaFileSet);
						} else {
							FileMediaRoots.setMediaRoots(new String[] {selectedGameConfig.scrollsFolder});
							meshSource = new FileMeshSource();
							textureSource = new FileTextureSource();
							soundSource = new FileSoundSource();
							materialsSource = new BgsmSource();
						}

						//Just for the crazy new fallout 4 system
						BgsmSource.setBgsmSource(materialsSource);

						mediaSources = new MediaSources(meshSource, textureSource, soundSource);

						simpleWalkSetup.configure(meshSource, simpleBethCellManager);
						simpleWalkSetup.setEnabled(false);

						//FIXME: stops working once fully running, but responds up to that point
						// that is to say the button no longer sends anything through
						// button won't work off the event thread, so I need to add my own system in and ignore the button
						// button only runs if display is called on the window but that cuts FPS in half
						simpleWalkSetup.getWindow().setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
						simpleWalkSetup.getWindow().addWindowListener(new WindowAdapter() {
							@Override
							public void windowDestroyNotify(WindowEvent arg0) {
								//simpleWalkSetup.closingTime();
								//closingTime();
								//System.exit(0);
							}

							@Override
							public void windowResized(final WindowEvent e) {
								J3dNiParticles.setScreenWidth(simpleWalkSetup.getWindow().getWidth());
							}

						});
						J3dNiParticles.setScreenWidth(simpleWalkSetup.getWindow().getWidth());
						simpleWalkSetup.getWindow().addKeyListener(new com.jogamp.newt.event.KeyAdapter() {
							@Override
							public void keyPressed(com.jogamp.newt.event.KeyEvent e) {
								if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
									//simpleWalkSetup.closingTime();
									//closingTime();
									//System.exit(0);
									ScrollsExplorer.this.dispose();
								}
							}
						});

						// I could use the j3dcellfactory now? with the cached cell records?
						simpleBethCellManager.setSources(selectedGameConfig, esmManager, mediaSources);

						if (selectedGameConfig == GameConfig.allGameConfigs.get(0)) {
							System.out.println("Adding Tes3 extensions");
							tes3Extensions = new Tes3Extensions(selectedGameConfig, esmManager, mediaSources,
									simpleWalkSetup, simpleBethCellManager);
						}
						table.loadTableCells(selectedGameConfig, prevCellformid);

						loadSaveGame.setEnabled(true);

						if (autoLoadStartCell) {
							display(prevCellformid);
						}
					} else {
						JOptionPane.showMessageDialog(ScrollsExplorer.this,
								selectedGameConfig.mainESMFile	+ " is not in folder set for game "
																			+ selectedGameConfig.gameName);
						setFolders();
					}
					mainPanel.validate();
					mainPanel.invalidate();
					mainPanel.doLayout();
					mainPanel.repaint();

					IDashboard.dashboard.setEsmLoading(-1);
				}

			}
		};
		t.start();
	}

	void display(final int cellformid) {
		Vector3f t = simpleWalkSetup.getAvatarLocation().get(new Vector3f());
		Quat4f r = simpleWalkSetup.getAvatarLocation().get(new Quat4f());
		simpleBethCellManager.setCurrentCellFormId(cellformid, t, r);
	}

	public boolean isAutoLoadStartCell() {
		return autoLoadStartCell;
	}

	public void setAutoLoadStartCell(boolean autoLoadStartCell) {
		this.autoLoadStartCell = autoLoadStartCell;
	}

	public SimpleBethCellManager getSimpleBethCellManager() {
		return simpleBethCellManager;
	}

	public SimpleWalkSetupInterface getSimpleWalkSetup() {
		return simpleWalkSetup;
	}

	public IESMManager getEsmManager() {
		return esmManager;
	}

	private static void setDebug(boolean b) {
		if (b) {
			System.out.println("DEBUG ON");
			// leave settings alone for optional debug parts
		} else {

		}
	}

	private void loadSaveGame() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setSelectedFile(new File(prefs.get("Save-" + selectedGameConfig.gameName, "")));
		fc.setDialogTitle("Select save file");
		int result = fc.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			prefs.put("Save-" + selectedGameConfig.gameName, f.getAbsolutePath());

			if (esmManager.getName().indexOf("Morrowind") != -1) {
				try {
					MasterFile ess = new MasterFile(f);
					ess.load();
					System.out.println("woop!");

					/*GMDT (124 bytes)
					float Unknown[6]
						- Unknown values rot loc?
					char  CellName[64]
						- Current cell name of character?
					float Unknown
					char CharacterName[32]*/

				} catch (IOException e) {
					e.printStackTrace();
				} catch (PluginException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Sorry only Morrowind for now");
			}
		}

	}

	public static void main(String[] args) {
		//Arguments for goodness
		//-Xmx1200m -Xms900m  -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -Dsun.java2d.noddraw=true
		//-Dj3d.cacheAutoComputeBounds=true -Dj3d.sharedctx=true
		//-Dj3d.stencilClear=true  -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -server -Djava.ext.dirs=.\none\

		// some other interesting settings
		//java -server -XX:CompileThreshold=2 -XX:+AggressiveOpts -XX:+UseFastAccessorMethods

		//-Dj3d.implicitAntialiasing=true check it why not set? MacOSX needs for AA, if set always AA

		String versionString = BootStrap.ZIP_PREFIX + "-" + BootStrap.MAJOR_VERSION + "-" + BootStrap.MINOR_VERSION;
		System.out.println("VERSION: " + versionString);
		System.err.println("VERSION: " + versionString);

		System.setProperty("sun.awt.noerasebackground", "true");
		System.setProperty("j3d.cacheAutoComputeBounds", "true");
		System.setProperty("j3d.defaultReadCapability", "false");
		System.setProperty("j3d.defaultNodePickable", "false");
		System.setProperty("j3d.defaultNodeCollidable", "false");

		ConfigLoader.loadConfig(args);

		// always load lwjgl for jbullet debug
		new LWJGLLinker();

		if (args.length > 0 && args[0].equals("debug")) {
			ScrollsExplorer.setDebug(true);
		} else {
			ScrollsExplorer.setDebug(false);
		}

		new ScrollsExplorer();

	}

}
