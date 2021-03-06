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
package systemtools.commandline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import systemtools.BaseDialog;
import systemtools.JTextAreaOutputStream;
import systemtools.images.SimToolsImages;

/**
 * Zeigt einen Dialog an, in dem die verf�gbaren Kommandozeilenparameter
 * direkt im Programm ausprobiert werden k�nnen.
 * @author Alexander Herzog
 * @see BaseCommandLineSystem
 */
public class CommandLineDialog extends BaseDialog {
	private static final long serialVersionUID = -6197903197950433862L;

	/** Bezeichner f�r den Titel des Dialogs */
	public static String title="Kommandozeilenbefehl ausf�hren";

	/** Bezeichner f�r die "Anhalten"-Schaltfl�che */
	public static String stop="Anhalten";
	/** Bezeichner f�r den Tooltip f�r die "Anhalten"-Schaltfl�che */
	public static String stopHint="Bricht die Verarbeitung des Befehls ab.";
	/** Bezeichner f�r die Beschriftung des Befehl-Dropdown-Elements */
	public static String labelCommand="Gew�hlter Befehl";
	/** Bezeichner f�r den Tab zur Beschreibung des Befehls */
	public static String tabDescription="Beschreibung des Befehls";
	/** Bezeichner f�r den Tab zur Anzeige von Parametern und Ausgabe des Befehls */
	public static String tabParametersAndResults="Parameter und Ausgabe";
	/** Bezeichner f�r die Beschriftung das Eingabefeldes f�r die optionalen Parameter des Befehls */
	public static String labelParameters="Parameter f�r diesen Befehl";
	/** Bezeichner f�r die Beschriftung des Ausgabebereichs des Befehls */
	public static String labelResults="Ergebnisse";

	private final Function<PrintStream,BaseCommandLineSystem> commandLineSystemGetter;
	private final AbstractCommand[] commands;
	private JComboBox<String> command;
	private JTabbedPane tabs;
	private JTextPane viewer;
	private JTextField parameters;
	private JTextArea results;
	private CommandWorkThread thread;

	/**
	 * Konstruktor der Klasse (macht den Dialog am Ende auch gleich sichtbar)
	 * @param owner	�bergeordnetes Element
	 * @param commandLineSystemGetter	Liefert eine neue Instanz des Kommandozeilensystems
	 * @param helpCallback	Hilfe-Callback
	 */
	public CommandLineDialog(final Component owner, final Function<PrintStream,BaseCommandLineSystem> commandLineSystemGetter, final Consumer<Window> helpCallback) {
		super(owner,title);

		this.commandLineSystemGetter=commandLineSystemGetter;
		commands=commandLineSystemGetter.apply(null).getCommands().stream().filter(cmd->!cmd.isHidden()).toArray(AbstractCommand[]::new);

		addUserButton(stop,stopHint,SimToolsImages.CANCEL.getURL());
		final JPanel content=createGUI(600,800,()->helpCallback.accept(getOwner()));

		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		JPanel p;

		/* Befehl ausw�hlen */

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(labelCommand+":"));

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(command=new JComboBox<>(getCommandStrings()));
		command.setSelectedIndex(0);
		command.addActionListener(new ComboActionListener());

		JPanel tab;
		content.add(tabs=new JTabbedPane());

		/* Tab "Beschreibung des Befehls" */

		tabs.add(tabDescription,tab=new JPanel());

		tab.setLayout(new BorderLayout());
		tab.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		viewer=new JTextPane();
		viewer.setEditable(false);
		viewer.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		viewer.setContentType("text/html");
		JScrollPane sp=new JScrollPane(viewer);
		tab.add(sp,BorderLayout.CENTER);
		sp.setBorder(BorderFactory.createEmptyBorder());

		/* Tab "Parameter und Ausgabe" */

		tabs.add(tabParametersAndResults,tab=new JPanel());
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(labelParameters+":"));

		tab.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(parameters=new JTextField(80));

		tab.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(labelResults));

		tab.add(new JScrollPane(results=new JTextArea(20,80),ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
		results.setEditable(false);

		/* Initialisierung Dialogs */

		new ComboActionListener().actionPerformed(null);

		/* Icons f�r Tabs */

		tabs.setIconAt(0,SimToolsImages.HELP.getIcon());
		tabs.setIconAt(1,SimToolsImages.COMMAND_LINE.getIcon());

		/* Starten des Dialogs */

		getUserButton(0).setVisible(false);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	private String[] getCommandStrings() {
		final List<String> list=new ArrayList<>();

		Arrays.sort(commands,new Comparator<AbstractCommand>() {
			@Override
			public int compare(AbstractCommand o1, AbstractCommand o2) {
				String s1=o1.getName();
				String s2=o2.getName();
				return s1.compareTo(s2);
			}
		});

		for (AbstractCommand command : commands) {
			list.add("<html><b>"+command.getName()+"</b><br>"+command.getShortDescription()+"</html>");
		}
		return list.toArray(new String[0]);
	}

	private String[] splitArguments(String text) {
		final List<String> args=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		String sub="";
		for (char c : text.trim().toCharArray()) {
			if  (c==' ' && sub.length()==0) {
				String s=sb.toString().trim();
				if (!s.isEmpty()) args.add(s);
				sb=new StringBuilder();
				continue;
			}
			if (c=='"' || c=='\'') {
				if (sub.length()==0) {
					sub+=c;
					continue;
				} else {
					if (sub.charAt(sub.length()-1)==c) {
						sub=sub.substring(0,sub.length()-1);
						if (sub.length()==0) continue;
					} else {
						sub+=c;
					}
				}
			}
			sb.append(c);
		}
		String s=sb.toString().trim();
		if (!s.isEmpty()) args.add(s);
		return args.toArray(new String[0]);
	}

	@Override
	protected boolean checkData() {
		int index=command.getSelectedIndex();
		String name=commands[index].getName();
		String[] args=splitArguments(name+" "+parameters.getText().trim());

		tabs.setSelectedIndex(1);

		thread=new CommandWorkThread(args);
		thread.start();

		return false;
	}

	private class ComboActionListener implements ActionListener {
		private static final String head=
				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
						"<html>\n"+
						"<head>\n"+
						"  <style type=\"text/css\">\n"+
						"  body {font-family: Verdana, Lucida, sans-serif; background-color: #FFFFF3; margin: 2px; padding: 5px;}\n"+
						"  h2 {margin-bottom: 0px;}\n"+
						"  </style>\n"+
						"</head>\n"+
						"<body>\n";

		@Override
		public void actionPerformed(ActionEvent e) {
			int index=command.getSelectedIndex();
			AbstractCommand cmd=commands[index];

			StringBuilder sb=new StringBuilder();

			sb.append("<h2>"+cmd.getName()+"</h2>");
			sb.append("<p><b>"+cmd.getShortDescription()+"</b><br>");
			for (String s: cmd.getLongDescription()) {sb.append("<br>"); sb.append(s);}
			sb.append("</p>");

			viewer.setText(head+sb.toString()+"</body></html>");
		}
	}

	private class CommandWorkThread extends Thread {
		private final String[] args;
		private BaseCommandLineSystem commandLineSimulator;

		public CommandWorkThread(String[] args) {
			super();
			this.args=args;
		}

		private synchronized void setGUIEnabled(boolean enabled) {
			setEnableButtons(enabled);
			getUserButton(0).setVisible(!enabled);
			getUserButton(0).setEnabled(!enabled);
			command.setEnabled(enabled);
			parameters.setEnabled(enabled);
		}

		@Override
		public void run() {
			setGUIEnabled(false);

			try (PrintStream stream=new PrintStream(new JTextAreaOutputStream(results,true))) {
				commandLineSimulator=commandLineSystemGetter.apply(stream);
				commandLineSimulator.run(args);
			}

			setGUIEnabled(true);
		}

		public synchronized void setQuit() {
			commandLineSimulator.setQuit();
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		if (thread!=null) thread.setQuit();
	}
}