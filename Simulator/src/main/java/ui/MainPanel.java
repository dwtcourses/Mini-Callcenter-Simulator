/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Element;

import language.Language;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.NeverDistributionImpl;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropperData;
import simulator.Simulator;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MainPanelBase;
import systemtools.MsgBox;
import systemtools.commandline.CommandLineDialog;
import systemtools.statistics.StatisticsBasePanel;
import tools.ExportQSModel;
import tools.SetupData;
import ui.calculator.CalculatorDialog;
import ui.calculator.QueueingCalculatorDialog;
import ui.commandline.CommandLineSystem;
import ui.compare.ComparePanel;
import ui.compare.CompareSelectDialog;
import ui.dialogs.LicenseViewer;
import ui.dialogs.SetupDialog;
import ui.help.Help;
import ui.images.Images;
import ui.statistics.StatisticsPanel;
import ui.tools.SpecialPanel;
import ui.tools.WaitPanel;
import xml.XMLTools;

/**
 * Diese Klasse stellt den Arbeitsbereich innerhalb des Programmfensters dar.
 * @see MainPanelBase
 * @author Alexander Herzog
 */
public class MainPanel extends MainPanelBase {
	private static final long serialVersionUID = 7636118203704616559L;

	/**
	 * Autor des Programms
	 */
	public static final String AUTHOR="Alexander Herzog";

	/**
	 * E-Mail-Adresse des Autors
	 */
	public static final String AUTHOR_EMAIL="alexander.herzog@tu-clausthal.de";

	/**
	 * Programmversion
	 */
	public static final String systemVersion="5.6.228";

	/**
	 * Bezeichnung f�r "ungespeichertes Modell" in der Titelzeile f�r ein neues Modell, welches noch keinen Namen besitzt
	 */
	public static String UNSAVED_MODEL="ungespeichertes Modell";

	private List<JMenuItem> enabledOnEditorPanel;
	private List<JButton> visibleOnEditorPanel;
	private List<JButton> visibleOnStatisticsPanel;
	private List<AbstractButton> selectOnEditorPanel;
	private List<AbstractButton> selectOnStatisticsPanel;
	private List<AbstractButton> enabledOnStatisticsAvailable;

	private JMenuItem menuFileModelRecentlyUsed;
	private JMenuItem menuExtrasCompareKept;
	private JMenuItem menuModelCompareReturn;

	private JButton buttonPageInfo;

	private final SetupData setup;
	private Runnable reloadWindow;

	private JPanel currentPanel;
	private final EditorPanel editorPanel;
	private final WaitPanel waitPanel;
	private final StatisticsPanel statisticsPanel;
	private SpecialPanel specialPanel;

	/**
	 * Modell f�r den Vergleich mit einem ge�nderten Modell festhalten
	 */
	private EditModel pinnedModel;

	private Statistics[] compareStatistics=new Statistics[2];

	/**
	 * Konstruktor der Klasse
	 * @param ownerWindow	�bergeordnetes Fenster
	 * @param programName	Name des Programms (wird dann �ber {@link MainPanelBase#programName} angeboten)
	 * @param isReload	Gibt an, ob es sich bei dem Aufbau des Panels um einen Programmstart (<code>false</code>) oder nur um einen Wiederaufbau z.B. nach dem �ndern der Sprache (<code>true</code>) handelt
	 */
	public MainPanel(final Window ownerWindow, final String programName, final boolean isReload) {
		super(ownerWindow,programName);
		initActions();
		initToolbar();
		setup=SetupData.getSetup();
		setAdditionalTitle(UNSAVED_MODEL);

		editorPanel=new EditorPanel();
		waitPanel=new WaitPanel();
		statisticsPanel=new StatisticsPanel(()->commandSimulation(null,null,null));
		statisticsPanel.addFileDropListener(e->{if (e.getSource() instanceof FileDropperData) dropFile((FileDropperData)e.getSource());});
		specialPanel=null;

		SwingUtilities.invokeLater(()->{
			setCurrentPanel(editorPanel);
			commandFileModelNew(0);
			if (!isReload) languageInfo();
		});

		setup.addChangeNotifyListener(()->reloadSetup());
		reloadSetup();
	}

	private void setCurrentPanel(final JPanel visiblePanel) {
		if (visiblePanel!=editorPanel) mainPanel.remove(editorPanel);
		if (visiblePanel!=waitPanel) mainPanel.remove(waitPanel);
		if (visiblePanel!=statisticsPanel) mainPanel.remove(statisticsPanel);
		if (specialPanel!=null && visiblePanel!=specialPanel) mainPanel.remove(specialPanel);

		boolean isInPanel=false;
		for (Component component : mainPanel.getComponents()) if (component==visiblePanel) {isInPanel=true; break;}
		if (!isInPanel) mainPanel.add(visiblePanel);

		currentPanel=visiblePanel;
		if (currentPanel instanceof SpecialPanel) specialPanel=(SpecialPanel)currentPanel; else specialPanel=null;

		mainPanel.repaint();

		final boolean editorPanelActive=(visiblePanel==editorPanel);
		final boolean statisticsPanelActive=(visiblePanel==statisticsPanel);
		if (enabledOnEditorPanel!=null) for (JMenuItem item: enabledOnEditorPanel) item.setEnabled(editorPanelActive);
		if (selectOnEditorPanel!=null) for (AbstractButton button: selectOnEditorPanel) button.setSelected(editorPanelActive);
		if (selectOnStatisticsPanel!=null) for (AbstractButton button: selectOnStatisticsPanel) button.setSelected(statisticsPanelActive);
		if (visibleOnEditorPanel!=null) for (JButton button: visibleOnEditorPanel) button.setVisible(editorPanelActive);
		if (visibleOnStatisticsPanel!=null) for (JButton button: visibleOnStatisticsPanel) button.setVisible(statisticsPanelActive);
	}

	@Override
	protected URL getResourceURL(final String path) {
		return MainPanel.class.getResource(path);
	}

	private void dropFile(final FileDropperData drop) {
		final File file=drop.getFile();
		if (file.isFile()) {
			drop.dragDropConsumed();
			SwingUtilities.invokeLater(()->{
				if (loadAnyFile(file,drop.getDropComponent(),drop.getDropPosition(),true)) {
					CommonVariables.setInitialDirectoryFromFile(file);
				}
			});
		}
	}

	private void initActions() {
		/* Datei */
		addAction("FileNew1",e->commandFileModelNew(0));
		addAction("FileNew2",e->commandFileModelNew(1));
		addAction("FileNew3",e->commandFileModelNew(2));
		addAction("FileNew4",e->commandFileModelNew(3));
		addAction("FileNew5",e->commandFileModelNew(4));
		addAction("FileLoad",e->commandFileModelLoad(null));
		addAction("FileSave",e->commandFileModelSave(false));
		addAction("FileSaveAs",e->commandFileModelSave(true));
		addAction("FileSaveCopyAs",e->commandFileModelSaveCopyAs());
		addAction("FileExportQSModel",e->commandFileExportQSModel());
		addAction("FileStatisticsLoad",e->commandFileStatisticsLoad(null));
		addAction("FileStatisticsSave",e->commandFileStatisticsSave());
		addAction("FileSetup",e->commandFileSetup());
		addAction("FileQuit",e->{if (allowQuitProgram()) close();});

		/* Ansicht */
		addAction("ViewEditor",e->setCurrentPanel(editorPanel));
		addAction("ViewStatistics",e->setCurrentPanel(statisticsPanel));

		/* Simulation */
		addAction("SimulationSimulation",e->commandSimulation(null,null,null));
		addAction("SimulationSimulationLog",e->commandSimulationLog());
		addAction("SimulationModel",e->commandSimulationModel());

		/* Extras */
		addAction("ExtrasCompare",e->commandExtrasCompare());
		addAction("ExtrasCompareKeep",e->commandExtrasCompareTwoInit());
		addAction("ExtrasCompareKept",e->commandExtrasCompareTwoRun(0));
		addAction("ExtrasCompareReturn",e->commandExtrasCompareReturn());
		addAction("ExtrasCalculator",e->commandExtrasCalculator(""));
		addAction("ExtrasQueueingCalculator",e->commandExtrasQueueingCalculator());
		addAction("ExtrasExecuteCommand",e->commandExtrasExecuteCommand());

		/* Hilfe */
		addAction("HelpHelp",e->commandHelpHelp());
		addAction("HelpContent",e->commandHelpContent());
		addAction("HelpBook",e->commandHelpBook());
		addAction("HelpSupport",e->commandHelpSupport());
		addAction("HelpHomepage",e->commandHelpHomepage());
		addAction("HelpLicense",e->commandHelpLicenseInfo());
		addAction("HelpInfo",e->commandHelpInfo());
		addAction("HelpPageInfo",e->commandHelpPageInfo());
	}

	@Override
	public JToolBar createToolBar() {
		if (visibleOnEditorPanel==null) visibleOnEditorPanel=new ArrayList<>();
		if (visibleOnStatisticsPanel==null) visibleOnStatisticsPanel=new ArrayList<>();
		if (selectOnEditorPanel==null) selectOnEditorPanel=new ArrayList<>();
		if (selectOnStatisticsPanel==null) selectOnStatisticsPanel=new ArrayList<>();
		if (enabledOnStatisticsAvailable==null) enabledOnStatisticsAvailable=new ArrayList<>();

		JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		JButton button;

		visibleOnEditorPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.LoadModel"),Language.tr("Main.Toolbar.LoadModel.Hint"),Images.MODEL_LOAD.getIcon(),"FileLoad"));
		visibleOnEditorPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.SaveModel"),Language.tr("Main.Toolbar.SaveModel.Hint"),Images.MODEL_SAVE.getIcon(),"FileSave"));
		visibleOnStatisticsPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.LoadStatistics"),Language.tr("Main.Toolbar.LoadStatistics.Hint"),Images.STATISTICS_LOAD.getIcon(),"FileStatisticsLoad"));
		button=createToolbarButton(toolbar,Language.tr("Main.Toolbar.SaveStatistics"),Language.tr("Main.Toolbar.SaveStatistics.Hint"),Images.STATISTICS_SAVE.getIcon(),"FileStatisticsSave");
		visibleOnStatisticsPanel.add(button);
		enabledOnStatisticsAvailable.add(button);
		toolbar.addSeparator();
		selectOnEditorPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.ShowEditor"),Language.tr("Main.Toolbar.ShowEditor.Hint"),Images.MODEL.getIcon(),"ViewEditor"));
		selectOnStatisticsPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.ShowStatistics"),Language.tr("Main.Toolbar.ShowStatistics.Hint"),Images.STATISTICS.getIcon(),"ViewStatistics"));
		toolbar.addSeparator();
		createToolbarButton(toolbar,Language.tr("Main.Toolbar.StartSimulation"),Language.tr("Main.Toolbar.StartSimulation.Hint"),Images.SIMULATION.getIcon(),"SimulationSimulation");
		button=createToolbarButton(toolbar,Language.tr("Main.Toolbar.ShowModelForTheseResults"),Language.tr("Main.Toolbar.ShowModelForTheseResults.Hint"),Images.MODEL.getIcon(),"SimulationModel");
		visibleOnStatisticsPanel.add(button);
		enabledOnStatisticsAvailable.add(button);
		toolbar.addSeparator();
		createToolbarButton(toolbar,Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint"),Images.HELP.getIcon(),"HelpHelp");

		/*
		toolbar.add(button=new JButton("Test"));
		button.addActionListener(e->{ });
		 */

		toolbar.add(Box.createHorizontalGlue());

		visibleOnEditorPanel.add(buttonPageInfo=createToolbarButton(toolbar,Language.tr("Main.Toolbar.PageInfo"),Language.tr("Main.Toolbar.PageInfo.Hint"),Images.GENERAL_INFO.getIcon(),"HelpPageInfo"));

		return toolbar;
	}

	@Override
	public JMenuBar createMenu() {
		if (selectOnEditorPanel==null) selectOnEditorPanel=new ArrayList<>();
		if (selectOnStatisticsPanel==null) selectOnStatisticsPanel=new ArrayList<>();
		if (enabledOnStatisticsAvailable==null) enabledOnStatisticsAvailable=new ArrayList<>();

		final JMenuBar menubar=new JMenuBar();
		JMenu menu,sub;
		JMenuItem item;

		/* Datei */

		menubar.add(menu=new JMenu(Language.tr("Main.Menu.File")));
		setMnemonic(menu,Language.tr("Main.Menu.File.Mnemonic"));

		menu.add(sub=new JMenu(Language.tr("Main.Menu.File.New")));
		createMenuItem(sub,"M/M/c/infty Modell",'\0',"FileNew1");
		createMenuItem(sub,"M/M/c/K Modell",'\0',"FileNew2");
		createMenuItem(sub,"M/M/c/K+M Modell",'\0',"FileNew3");
		createMenuItem(sub,"M/M/c/K+M+M+Weiterleitungen Modell",'\0',"FileNew4");
		createMenuItem(sub,"M/G/c/K+G+G+Weiterleitungen Modell",'\0',"FileNew5");

		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.Load"),Images.MODEL_LOAD.getIcon(),Language.tr("Main.Menu.File.Load.Mnemonic"),KeyEvent.VK_L,"FileLoad");

		menu.add(menuFileModelRecentlyUsed=new JMenu(Language.tr("Main.Menu.File.RecentlyUsed")));
		setMnemonic(menuFileModelRecentlyUsed,Language.tr("Main.Menu.File.RecentlyUsed.Mnemonic"));
		updateRecentlyUsedList();

		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.Save"),Images.MODEL_SAVE.getIcon(),Language.tr("Main.Menu.File.Save.Mnemonic"),KeyEvent.VK_S,"FileSave");
		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.SaveAs"),Language.tr("Main.Menu.File.SaveAs.Mnemonic"),KeyEvent.VK_U,"FileSaveAs");
		createMenuItem(menu,Language.tr("Main.Menu.File.SaveCopyAs"),Language.tr("Main.Menu.File.SaveCopyAs.Mnemonic"),"FileSaveCopyAs");
		item=createMenuItem(menu,Language.tr("Main.Menu.File.ExportQSModel"),Images.EXTRAS_QUEUE.getIcon(),Language.tr("Main.Menu.File.ExportQSModel.Mnemonic"),"FileExportQSModel");
		item.setToolTipText(Language.tr("Main.Menu.File.ExportQSModel.Info"));

		menu.addSeparator();

		createMenuItemCtrlShift(menu,Language.tr("Main.Menu.File.LoadStatistics"),Images.STATISTICS_LOAD.getIcon(),Language.tr("Main.Menu.File.LoadStatistics.Mnemonic"),KeyEvent.VK_L,"FileStatisticsLoad");
		enabledOnStatisticsAvailable.add(createMenuItemCtrlShift(menu,Language.tr("Main.Menu.File.SaveStatistics"),Images.STATISTICS_SAVE.getIcon(),Language.tr("Main.Menu.File.SaveStatistics.Mnemonic"),KeyEvent.VK_U,"FileStatisticsSave"));

		menu.addSeparator();

		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.Settings"),Images.GENERAL_SETUP.getIcon(),Language.tr("Main.Menu.File.Settings.Mnemonic"),KeyEvent.VK_P,"FileSetup");

		menu.addSeparator();

		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.Quit"),Images.GENERAL_EXIT.getIcon(),Language.tr("Main.Menu.File.Quit.Mnemonic"),KeyEvent.VK_W,"FileQuit");

		/* Ansicht */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.View")));
		setMnemonic(menu,Language.tr("Main.Menu.View.Mnemonic"));

		selectOnEditorPanel.add(createCheckBoxMenuItemIcon(menu,Language.tr("Main.Menu.View.ModelEditor"),Images.MODEL.getIcon(),Language.tr("Main.Menu.View.ModelEditor.Mnemonic"),KeyEvent.VK_F3,"ViewEditor"));
		selectOnStatisticsPanel.add(createCheckBoxMenuItemIcon(menu,Language.tr("Main.Menu.View.SimulationResults"),Images.STATISTICS.getIcon(),Language.tr("Main.Menu.View.SimulationResults.Mnemonic"),KeyEvent.VK_F4,"ViewStatistics"));

		/* Simulation */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.Simulation")));
		setMnemonic(menu,Language.tr("Main.Menu.Simulation.Mnemonic"));

		createMenuItem(menu,Language.tr("Main.Menu.StartSimulation"),Images.SIMULATION.getIcon(),Language.tr("Main.Menu.StartSimulation.Mnemonic"),KeyEvent.VK_F5,"SimulationSimulation");
		createMenuItem(menu,Language.tr("Main.Menu.RecordSimulation"),Images.SIMULATION_LOG.getIcon(),Language.tr("Main.Menu.RecordSimulation.Mnemonic"),"SimulationSimulationLog");

		/* Extras */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.Extras")));
		setMnemonic(menu,Language.tr("Main.Menu.Extras.Mnemonic"));

		createMenuItem(menu,Language.tr("Main.Menu.Extras.CompareModels"),Images.MODEL_COMPARE.getIcon(),Language.tr("Main.Menu.Extras.CompareModels.Mnemonic"),"ExtrasCompare");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Extras.KeepModel"),Images.MODEL_COMPARE_KEEP.getIcon(),Language.tr("Main.Menu.Extras.KeepModel.Mnemonic"),"ExtrasCompareKeep");
		menuExtrasCompareKept=createMenuItem(menu,Language.tr("Main.Menu.Extras.CompareWithKeptModel"),Images.MODEL_COMPARE_COMPARE.getIcon(),Language.tr("Main.Menu.Extras.CompareWithKeptModel.Mnemonic"),"ExtrasCompareKept");
		menuExtrasCompareKept.setEnabled(false);
		menuModelCompareReturn=createMenuItem(menu,Language.tr("Main.Menu.Extras.ReturnToKeptModel"),Images.MODEL_COMPARE_GO_BACK.getIcon(),Language.tr("Main.Menu.Extras.ReturnToKeptModel.Mnemonic"),"ExtrasCompareReturn");
		menuModelCompareReturn.setEnabled(false);
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Extras.Calculator"),Images.EXTRAS_CALCULATOR.getIcon(),Language.tr("Main.Menu.Extras.Calculator.Mnemonic"),"ExtrasCalculator");
		createMenuItem(menu,Language.tr("Main.Menu.Extras.QueueingCalculator"),Images.EXTRAS_QUEUE.getIcon(),Language.tr("Main.Menu.Extras.QueueingCalculator.Mnemonic"),"ExtrasQueueingCalculator");
		createMenuItem(menu,Language.tr("Main.Menu.Extras.ExecuteCommand"),Images.EXTRAS_COMMANDLINE.getIcon(),Language.tr("Main.Menu.Extras.ExecuteCommand.Mnemonic"),"ExtrasExecuteCommand");

		/* Hilfe */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.Help")));
		setMnemonic(menu,Language.tr("Main.Menu.Help.Mnemonic"));

		createMenuItem(menu,Language.tr("Main.Menu.Help.Help"),Images.HELP.getIcon(),Language.tr("Main.Menu.Help.Help.Mnemonic"),KeyEvent.VK_F1,"HelpHelp");
		createMenuItemShift(menu,Language.tr("Main.Menu.Help.HelpContent"),Images.HELP_CONTENT.getIcon(),Language.tr("Main.Menu.Help.HelpContent.Mnemonic"),KeyEvent.VK_F1,"HelpContent");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("MainMenu.Help.Book"),Images.HELP_BOOK.getIcon(),Language.tr("MainMenu.Help.Book.Mnemonic"),"HelpBook");
		createMenuItem(menu,Language.tr("Main.Menu.Help.Support"),Images.HELP_EMAIL.getIcon(),Language.tr("Main.Menu.Help.Support.Mnemonic"),"HelpSupport");
		createMenuItem(menu,Language.tr("MainMenu.Help.Homepage"),Images.HELP_HOMEPAGE.getIcon(),Language.tr("MainMenu.Help.Homepage.Mnemonic"),"HelpHomepage");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Help.LicenseInformation"),Language.tr("Main.Menu.Help.LicenseInformation.Mnemonic"),"HelpLicense");
		createMenuItemCtrlShift(menu,Language.tr("Main.Menu.Help.ProgramInformation"),Images.GENERAL_INFO.getIcon(),Language.tr("Main.Menu.Help.ProgramInformation.Mnemonic"),KeyEvent.VK_F1,"HelpInfo");

		return menubar;
	}

	private void updateRecentlyUsedList() {
		menuFileModelRecentlyUsed.removeAll();
		menuFileModelRecentlyUsed.setEnabled(setup.lastFiles!=null && setup.lastFiles.length>0);
		if (!menuFileModelRecentlyUsed.isEnabled()) return;

		for (int i=0;i<setup.lastFiles.length; i++) {
			JMenuItem sub=new JMenuItem(setup.lastFiles[i]);
			sub.addActionListener(actionListener);
			menuFileModelRecentlyUsed.add(sub);
		}
	}

	private void addFileToRecentlyUsedList(String fileName) {
		final ArrayList<String> files=(setup.lastFiles==null)?new ArrayList<String>():new ArrayList<String>(Arrays.asList(setup.lastFiles));

		int index=files.indexOf(fileName);
		if (index==0) return; /* Eintrag ist bereits ganz oben in der Liste, nichts zu tun */
		if (index>0) files.remove(index); /* Wenn schon in Liste: Element an alter Position entfernen */
		files.add(0,fileName); /* Element ganz vorne einf�gen */
		while (files.size()>5) files.remove(files.size()-1); /* Maximal die letzten 5 Dateien merken */

		setup.lastFiles=files.toArray(new String[0]);
		setup.saveSetup();

		updateRecentlyUsedList();
	}

	private boolean isDiscardModelOk() {
		if (!editorPanel.isModelChanged()) return true;

		switch (MsgBox.confirmSave(getOwnerWindow(),Language.tr("Window.DiscardConfirmation.Title"),Language.tr("Window.DiscardConfirmation.Info"))) {
		case JOptionPane.YES_OPTION:
			commandFileModelSave(false);
			return isDiscardModelOk();
		case JOptionPane.NO_OPTION:
			return true;
		case JOptionPane.CANCEL_OPTION:
			return false;
		default:
			return false;
		}
	}

	@Override
	public boolean allowQuitProgram() {
		if (currentPanel==null) return false;
		if (currentPanel==waitPanel) {waitPanel.abortSimulation(); return false;}
		if (currentPanel==specialPanel) {specialPanel.requestClose(); return false;}
		return isDiscardModelOk();
	}

	@Override
	public boolean loadAnyFile(final File file, final Component dropComponent, final Point dropPosition, final boolean errorMessageOnFail) {
		if (file==null) {
			if (errorMessageOnFail) MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),Language.tr("XML.NoFileSelected"));
			return false;
		}
		if (!file.exists()) {
			if (errorMessageOnFail) MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),String.format(Language.tr("XML.FileNotFound"),file.toString()));
			return false;
		}

		final XMLTools xml=new xml.XMLTools(file);
		final Element root=xml.load();
		if (root==null) {
			if (errorMessageOnFail) MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),xml.getError());
			return false;
		}

		final String name=root.getNodeName();

		for (String test: new EditModel().getRootNodeNames()) if (name.equalsIgnoreCase(test)) return commandFileModelLoad(file);
		for (String test: new Statistics().getRootNodeNames()) if (name.equalsIgnoreCase(test)) return commandFileStatisticsLoad(file);

		if (errorMessageOnFail) MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),Language.tr("XML.UnknownFileFormat"));

		return false;
	}

	private void languageInfo() {
		if (!setup.languageWasAutomaticallySet()) return;
		setMessagePanel("",Language.tr("Window.LanguageAutomatic"),MessagePanelIcon.INFO).setBackground(new Color(255,255,240));
		new Timer().schedule(new TimerTask() {@Override public void run() {setMessagePanel(null,null,null);}},7500);
	}

	private void commandFileModelNew(final int type) {
		if (!isDiscardModelOk()) return;

		final EditModel newModel=new EditModel();

		newModel.interArrivalTimeDist=new ExponentialDistribution(60);
		newModel.batchArrival=1;
		newModel.batchWorking=1;
		newModel.agents=4;
		newModel.callsToSimulate=1000000;

		switch (type) {
		case 0:
			newModel.name="M/M/c/infty "+Language.tr("Example.Model");
			newModel.description=Language.tr("Example.ErlangC");
			newModel.workingTimeDist=new ExponentialDistribution(180);
			newModel.waitingTimeDist=new NeverDistributionImpl();
			newModel.waitingRoomSize=-1;
			newModel.callContinueProbability=0;
			newModel.retryProbability=0;
			newModel.retryTimeDist=new ExponentialDistribution(1800);
			break;
		case 1:
			newModel.name="M/M/c/K "+Language.tr("Example.Model");
			newModel.description=Language.tr("Example.ErlangC");
			newModel.workingTimeDist=new ExponentialDistribution(180);
			newModel.waitingTimeDist=new NeverDistributionImpl();
			newModel.waitingRoomSize=5;
			newModel.callContinueProbability=0;
			newModel.retryProbability=0;
			newModel.retryTimeDist=new ExponentialDistribution(1800);
			break;
		case 2:
			newModel.name="M/M/c/K+M "+Language.tr("Example.Model");
			newModel.description=Language.tr("Example.ExtErlangC");
			newModel.workingTimeDist=new ExponentialDistribution(180);
			newModel.waitingTimeDist=new ExponentialDistribution(120);
			newModel.waitingRoomSize=15;
			newModel.callContinueProbability=0;
			newModel.retryProbability=0;
			newModel.retryTimeDist=new ExponentialDistribution(1800);
			break;
		case 3:
			newModel.name="M/M/c/K+M+M+"+Language.tr("Example.Forwarding")+" "+Language.tr("Example.Model");
			newModel.description=Language.tr("Example.ModelWithRetry");
			newModel.workingTimeDist=new ExponentialDistribution(180);
			newModel.waitingTimeDist=new ExponentialDistribution(120);
			newModel.waitingRoomSize=15;
			newModel.callContinueProbability=0.2;
			newModel.retryProbability=0.75;
			newModel.retryTimeDist=new ExponentialDistribution(1800);
			break;
		case 4:
			newModel.name="M/G/c/K+G+G+"+Language.tr("Example.Forwarding")+" "+Language.tr("Example.Model");
			newModel.description=Language.tr("Example.ModelGeneral");
			newModel.workingTimeDist=new LogNormalDistributionImpl(180,30);
			newModel.waitingTimeDist=new LogNormalDistributionImpl(120,60);
			newModel.waitingRoomSize=15;
			newModel.callContinueProbability=0.2;
			newModel.retryProbability=0.75;
			newModel.retryTimeDist=new LogNormalDistributionImpl(1800,600);
			break;
		}

		editorPanel.setModel(newModel);
		setAdditionalTitle(null);
		statisticsPanel.setStatistics(null);
		for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(false);
		setCurrentPanel(editorPanel);
	}

	private boolean commandFileModelLoad(final File file) {
		if (!isDiscardModelOk()) return true;
		final String error=editorPanel.loadModel(file);
		if (error==null) {
			statisticsPanel.setStatistics(null);
			for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(false);
			setCurrentPanel(editorPanel);
		} else {
			MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),error);
		}
		if (editorPanel.getLastFile()!=null) {
			addFileToRecentlyUsedList(editorPanel.getLastFile().toString());
			setAdditionalTitle(editorPanel.getLastFile().getName());
			CommonVariables.setInitialDirectoryFromFile(editorPanel.getLastFile());
		}
		return error==null;
	}

	private boolean commandFileModelSave(final boolean saveAs) {
		final File file=(saveAs)?null:editorPanel.getLastFile();
		final String error=editorPanel.saveModel(file);
		if (error!=null) MsgBox.error(getOwnerWindow(),Language.tr("XML.SaveErrorTitle"),error);
		if (editorPanel.getLastFile()!=null) {
			addFileToRecentlyUsedList(editorPanel.getLastFile().toString());
			setAdditionalTitle(editorPanel.getLastFile().getName());
		}
		return error==null;
	}

	private boolean commandFileModelSaveCopyAs() {
		final String error=editorPanel.saveModelCopy();
		if (error!=null) {
			MsgBox.error(getOwnerWindow(),Language.tr("XML.SaveErrorTitle"),error);
		} else {
			if (editorPanel.getLastFile()!=null) {
				addFileToRecentlyUsedList(editorPanel.getLastFile().toString());
			}
		}
		return error==null;
	}

	private void commandFileExportQSModel() {
		final File file=ExportQSModel.selectFile(this);
		if (file==null) return;

		final ExportQSModel exporter=new ExportQSModel(editorPanel.getModel());
		if (!exporter.work(file)) {
			MsgBox.error(this,Language.tr("QSExport.Error.Title"),String.format(Language.tr("QSExport.Error.Info"),file.toString()));
		}
	}

	private boolean commandFileStatisticsLoad(final File file) {
		final String error=statisticsPanel.loadStatistics(file);
		if (error==null) {
			for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(true);
			setCurrentPanel(statisticsPanel);
		} else {
			MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),error);
		}
		return error==null;
	}

	private boolean commandFileStatisticsSave() {
		String error=statisticsPanel.saveStatistics(null);
		if (error!=null) MsgBox.error(getOwnerWindow(),Language.tr("XML.SaveErrorTitle"),error);
		return error==null;
	}

	private void commandFileSetup() {
		new SetupDialog(this);
		reloadSetup();
	}

	private void commandSimulation(final EditModel simModel, final File logFile, final Runnable whenDone) {
		final EditModel editModel=(simModel==null)?editorPanel.getModel():simModel;
		final Simulator simulator=new Simulator(editModel,logFile);

		String error=simulator.prepare();
		if (error!=null) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.Simulation.ModelIsFaulty"),"<html>"+Language.tr("Window.Simulation.ErrorInitializatingSimulation")+":<br>"+error+"</html>");
			return;
		}

		simulator.start();
		enableMenuBar(false);

		waitPanel.setSimulator(simulator,()->{
			if (waitPanel.isSimulationSuccessful()) {
				statisticsPanel.setStatistics(simulator.getStatistic());
				for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(true);
				setCurrentPanel(statisticsPanel);
			} else {
				setCurrentPanel(editorPanel);
			}
			enableMenuBar(true);
			if (whenDone!=null) whenDone.run();
		});
		setCurrentPanel(waitPanel);
	}

	private void commandSimulationLog() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Main.Menu.RecordSimulation.LogFile"));
		final FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);

		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return;
		}

		commandSimulation(null,file,null);
	}

	private void commandSimulationModel() {
		final Statistics statistics=statisticsPanel.getStatistics();
		if (statistics==null) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.CannotShowModel.Title"),Language.tr("Window.CannotShowModel.Info"));
			return;
		}

		final ModelViewerFrame viewer=new ModelViewerFrame(getOwnerWindow(),statistics.editModel,null,()->{
			if (!isDiscardModelOk()) return;
			editorPanel.setModel(statistics.editModel);
			setCurrentPanel(editorPanel);
		});
		viewer.setVisible(true);
	}


	private void commandExtrasCompare() {
		CompareSelectDialog dialog=new CompareSelectDialog(getOwnerWindow(),5);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		File[] files=dialog.getSelectedFiles();
		Statistics[] statistics=ComparePanel.getStatisticFiles(files);
		String[] title=new String[statistics.length];
		for (int i=0;i<statistics.length;i++) {
			if (statistics[i]==null) {
				MsgBox.error(getOwnerWindow(),Language.tr("Window.Compare.NotAValidStatisticsFile.Title"),String.format(Language.tr("Window.Compare.NotAValidStatisticsFile.Info"),""+(i+1),files[i].toString()));
				return;
			}
			title[i]=statistics[i].editModel.name;
		}

		enableMenuBar(false);
		setCurrentPanel(new ComparePanel(getOwnerWindow(),statistics,title,true,()->{
			if (currentPanel instanceof ComparePanel) {
				ComparePanel comparePanel=(ComparePanel)currentPanel;
				EditModel model=comparePanel.getModelForEditor();
				if (model!=null) {
					if (!isDiscardModelOk()) return;
					editorPanel.setModel(model);
				}
			}
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
		}));
	}

	private void commandExtrasCompareTwoInit() {
		EditModel model=editorPanel.getModel();
		Object obj=RunModel.getRunModel(model);
		if (obj instanceof String) {
			MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.ModelError.Title"),Language.tr("Compare.Error.ModelError.CannotCompare"));
			return;
		}

		if (pinnedModel!=null) {
			if (!MsgBox.confirm(getOwnerWindow(),Language.tr("Compare.ReplaceKeptModel.Title"),Language.tr("Compare.ReplaceKeptModel.Info"),Language.tr("Compare.ReplaceKeptModel.YesInfo"),Language.tr("Compare.ReplaceKeptModel.NoInfo"))) return;
		}

		pinnedModel=model;
		MsgBox.info(getOwnerWindow(),Language.tr("Compare.Kept.Title"),Language.tr("Compare.Kept.Info"));

		menuExtrasCompareKept.setEnabled(true);
		menuModelCompareReturn.setEnabled(true);
	}

	private void commandExtrasCompareTwoRun(final int level) {
		if (level==0) {
			if (pinnedModel==null) {
				MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.NoModelKept.Title"),Language.tr("Compare.Error.NoModelKept.Info"));
				return;
			}

			EditModel model=editorPanel.getModel();
			Object obj=RunModel.getRunModel(model);
			if (obj instanceof String) {
				MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.ModelError.Title"),Language.tr("Compare.Error.ModelError.CannotKeep"));
				return;
			}

			if (pinnedModel.equalsEditModel(model)) {
				MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.IdenticalModels.Title"),Language.tr("Compare.Error.IdenticalModels.Info"));
				return;
			}

			commandSimulation(pinnedModel,null,()->{
				compareStatistics[0]=statisticsPanel.getStatistics();
				commandExtrasCompareTwoRun(1);
			});
			return;
		}

		if (level==1) {
			commandSimulation(null,null,()->{
				compareStatistics[1]=statisticsPanel.getStatistics();
				commandExtrasCompareTwoRun(2);
			});
			return;
		}

		if (level==2) {
			enableMenuBar(false);
			setCurrentPanel(new ComparePanel(getOwnerWindow(),compareStatistics,new String[] {Language.tr("Compare.Models.Base"),Language.tr("Compare.Models.Changed")},true,()->{
				if (currentPanel instanceof ComparePanel) {
					ComparePanel comparePanel=(ComparePanel) currentPanel;
					EditModel model=comparePanel.getModelForEditor();
					if (model!=null) {if (!isDiscardModelOk()) return; editorPanel.setModel(model);}
				}
				setCurrentPanel(editorPanel);
				enableMenuBar(true);
			}));
			return;
		}
	}

	private void commandExtrasCompareReturn() {
		if (pinnedModel==null) {
			MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.NoModelKept.Title"),Language.tr("Compare.Error.NoModelKept.Info2"));
			return;
		}

		EditModel model=editorPanel.getModel();

		if (pinnedModel.equalsEditModel(model)) {
			MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.IdenticalModels.Title"),Language.tr("Compare.Error.IdenticalModels.Info"));
			return;
		}

		if (editorPanel.isModelChanged()) {
			if (!MsgBox.confirm(getOwnerWindow(),Language.tr("Compare.ReturnConfirm.Title"),Language.tr("Compare.ReturnConfirm.Info"),Language.tr("Compare.ReturnConfirm.InfoYes"),Language.tr("Compare.ReturnConfirm.InfoNo"))) return;
		}

		editorPanel.setModel(pinnedModel);
	}

	private void commandExtrasCalculator(final String initialExpression) {
		final CalculatorDialog dialog=new CalculatorDialog(this,initialExpression);
		dialog.setVisible(true);
	}

	private void commandExtrasQueueingCalculator() {
		final QueueingCalculatorDialog dialog=new QueueingCalculatorDialog(this);
		dialog.setVisible(true);
	}

	private void commandExtrasExecuteCommand() {
		new CommandLineDialog(this,stream->new CommandLineSystem(null,stream),window->Help.topicModal(window,"CommandLineDialog"));
	}

	private void commandHelpHelp() {
		if (currentPanel==editorPanel) {Help.topic(this,Help.pageEditor); return;}
		if (currentPanel==statisticsPanel) {Help.topic(this,Help.pageStatistics); return;}
		Help.topic(this,"");
	}

	private void commandHelpContent() {
		Help.topic(this,"");
	}

	private void commandHelpBook() {
		try {
			Desktop.getDesktop().browse(new URI("https://www.springer.com/de/book/9783658183080"));
		} catch (IOException | URISyntaxException e) {
			MsgBox.error(this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),"https://www.springer.com/de/book/9783658183080"));
		}
	}


	private void commandHelpSupport() {
		try {
			Desktop.getDesktop().mail(new URI("mailto:"+MainPanel.AUTHOR_EMAIL));
		} catch (IOException | URISyntaxException e1) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.Info.NoEMailProgram.Title"),String.format(Language.tr("Window.Info.NoEMailProgram.Info"),"mailto:"+MainPanel.AUTHOR_EMAIL));
		}
		return;
	}

	private void commandHelpHomepage() {
		try {
			Desktop.getDesktop().browse(new URI("https://github.com/A-Herzog/Mini-Callcenter-Simulator"));
		} catch (IOException | URISyntaxException e) {
			MsgBox.error(this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),"https://github.com/A-Herzog/Mini-Callcenter-Simulator"));
		}
	}

	private void commandHelpLicenseInfo() {
		new LicenseViewer(this);
	}

	private void commandHelpInfo() {
		MsgBox.info(
				this,
				Language.tr("InfoDialog.Title"),
				"<html><b>"+programName+"</b><br>"+Language.tr("InfoDialog.Version")+" "+EditModel.systemVersion+"<br>"+Language.tr("InfoDialog.WrittenBy")+" "+AUTHOR+"</html>"
				);
	}

	private void commandHelpPageInfo() {
		buttonPageInfo.setSelected(!buttonPageInfo.isSelected());
		editorPanel.setInfoPanelVisible(buttonPageInfo.isSelected());
	}

	@Override
	protected void action(final Object sender) {
		/* Datei - Letzte Dokumente */
		final Component[] sub=((JMenu)menuFileModelRecentlyUsed).getMenuComponents();
		for (int i=0;i<sub.length;i++) if (sender==sub[i]) {commandFileModelLoad(new File(setup.lastFiles[i])); return;}
	}

	/**
	 * �ber diese Methode kann dem Panal ein Callback mitgeteilt werden,
	 * das aufgerufen wird, wenn das Fenster neu geladen werden soll.
	 * @param reloadWindow	Callback, welches ein Neuladen des Fensters veranlasst.
	 */
	public void setReloadWindow(final Runnable reloadWindow) {
		this.reloadWindow=reloadWindow;
	}

	private void reloadSetup() {
		/* Sprache neu laden? */
		if (!setup.language.equals(Language.getCurrentLanguage())) {
			setup.resetLanguageWasAutomatically();
			Help.hideHelpFrame();
			if (reloadWindow!=null) SwingUtilities.invokeLater(reloadWindow);
		} else {
			invalidate();
			if (reloadWindow!=null) SwingUtilities.invokeLater(()->repaint());
		}

		/* "�ffnen"-Buttons in Statistik */
		StatisticsBasePanel.viewerPrograms.clear();
		if (setup.openWord) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.WORD);
		if (setup.openODT) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.ODT);
		if (setup.openExcel) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.EXCEL);
		if (setup.openODS) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.ODS);
	}

	/**
	 * Liefert alle Daten innerhalb dieses Panels als Objekt-Array
	 * um dann das Panel neu laden und die Daten wiederherstellen
	 * zu k�nnen.
	 * @return	5-elementiges Objekt-Array mit allen Daten des Panels
	 * @see #setAllData(Object[])
	 */
	public Object[] getAllData() {
		return new Object[]{
				editorPanel.getModel(),
				editorPanel.isModelChanged(),
				editorPanel.getLastFile(),
				statisticsPanel.getStatistics(),
				Integer.valueOf((currentPanel==statisticsPanel)?1:0)
		};
	}

	/**
	 * Reinitialisiert die Daten in dem Panel wieder aus einem
	 * zuvor erstellten Objekt-Array.
	 * @param data	5-elementiges Objekt-Array mit allen Daten des Panels
	 * @return	Gibt an, ob die Daten aus dem Array erfolgreich geladen werden konnten
	 * @see #getAllData()
	 */
	public boolean setAllData(Object[] data) {
		if (data==null || data.length!=5) return false;
		if (!(data[0] instanceof EditModel)) return false;
		if (!(data[1] instanceof Boolean)) return false;

		if (data[2]!=null && !(data[2] instanceof File)) return false;
		if (data[3]!=null && !(data[3] instanceof Statistics)) return false;
		if (data[4]==null || !(data[4] instanceof Integer)) return false;

		editorPanel.setModel((EditModel)data[0]);
		editorPanel.setModelChanged((Boolean)data[1]);
		editorPanel.setLastFile((File)data[2]); if (data[2]!=null) setAdditionalTitle(((File)data[3]).getName());
		statisticsPanel.setStatistics((Statistics)data[3]);
		if ((Integer)data[4]==1) setCurrentPanel(statisticsPanel); else setCurrentPanel(editorPanel);

		return true;
	}
}