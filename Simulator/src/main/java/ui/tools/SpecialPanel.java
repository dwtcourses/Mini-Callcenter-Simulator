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
package ui.tools;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ui.MainPanel;
import ui.images.Images;

/**
 * Basisklasse f�r weitere Panels, die im Arbeitsbereich des <code>SimulatorPanel</code> angezeigt werden k�nnen.
 * @see MainPanel
 * @author Alexander Herzog
 */
public class SpecialPanel extends JPanel {
	private static final long serialVersionUID = 399014899599365120L;

	/** Name der "Schlie�en"-Schaltfl�che */
	public static String buttonClose="Schlie�en";

	/** Tooltip f�r die "Schlie�en"-Schaltfl�che */
	public static String buttonCloseHint="Schlie�t die aktuelle Ansicht";

	private final Runnable closePanel;
	private JToolBar toolbar=null;
	private JButton closeButton=null;
	private List<JButton> userButtons=new ArrayList<>();
	private ButtonListener buttonListener=new ButtonListener();

	/**
	 * Konstruktor der Klasse <code>SpecialPanel</code>
	 * @param closePanel	Objekt vom Typ <code>Runnable</code> welches aktiviert wird, wenn das Panel seine Arbeit beendet hat und sich schlie�en m�chte (was es durch den Aufruf von <code>close</code> kenntlich macht)
	 * @see #close()
	 */
	public SpecialPanel(final Runnable closePanel) {
		super();
		this.closePanel=closePanel;

		setLayout(new BorderLayout());

		InputMap inputMap=getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		getActionMap().put("ESCAPE",new AbstractAction() {
			private static final long serialVersionUID = -3369192426710383359L;
			@Override
			public void actionPerformed(ActionEvent e) {requestClose();}
		});
	}

	/**
	 * Signalisiert dem �bergerodneten Panel, dass dieses Panel seine Arbeit beendet hat und entfernt werden kann.
	 */
	protected void close() {
		if (closePanel!=null) SwingUtilities.invokeLater(closePanel);
	}

	/**
	 * Teilt dem Panel mit, dass es geschlossen werden soll. Das Panel kann dies ignorieren.
	 *  Wenn das Panel bereit ist, geschlossen zu werden, muss es die <code>close</code>-Methode aufrufen.
	 *  @see #close()
	 */
	public void requestClose() {
		close();
	}

	private void initToolbar() {
		if (toolbar!=null) return;
		add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);
	}

	private JButton addButtonInt(final String title, final String hint, final URL icon) {
		initToolbar();
		JButton button=new JButton(title);
		toolbar.add(button);
		if (hint!=null && !hint.isEmpty()) button.setToolTipText(hint);
		button.addActionListener(buttonListener);
		if (icon!=null) button.setIcon(new ImageIcon(icon));
		return button;
	}

	/**
	 * F�gt eine "Schlie�en"-Schaltfl�che zu der Symbolleiste des Panels hinzu.
	 * (Im Bedarfsfalle wird die Symbolleiste daf�r zun�chst angelegt.)
	 */
	protected final void addCloseButton() {
		if (closeButton!=null) return;
		closeButton=addButtonInt(buttonClose,buttonCloseHint,Images.GENERAL_EXIT.getURL());
	}

	/**
	 * F�gt einen Trennen in die Symbolleiste des Panels ein.
	 * (Im Bedarfsfalle wird die Symbolleiste daf�r zun�chst angelegt.)
	 */
	protected final void addSeparator() {
		initToolbar();
		toolbar.addSeparator();
	}

	/**
	 * F�gt eine benutzerdefinierte Schaltfl�che zu der Symbolleiste des Panels hinzu.
	 * (Im Bedarfsfalle wird die Symbolleiste daf�r zun�chst angelegt.)
	 * @param title	Titel der Schaltfl�che
	 * @param hint	Optionaler Tooltip-Text f�r die Schaltfl�che (wird <code>null</code> �bergeben, so wird kein Tooltip angezeigt)
	 * @param icon	Icon, das auf der Schaltfl�che angezeigt werden soll (wird <code>null</code> �bergeben, so wird kein Icon angezeigt)
	 * @return	Liefert die Schaltfl�che zur�ck
	 * @see #userButtonClick(int, JButton)
	 */
	protected final JButton addUserButton(final String title, final String hint, final URL icon) {
		JButton button=addButtonInt(title,hint,icon);
		userButtons.add(button);
		return button;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine der benutzerdefinierten Schaltfl�chen angeklickt wurde.
	 * @param index	0-basierender Index der angeklickten Schaltfl�che
	 * @param button	Angeklickte Schaltfl�che
	 * @see #addUserButton(String, String, URL)
	 */
	protected void userButtonClick(int index, JButton button) {}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object sender=e.getSource();
			if (sender==closeButton) {requestClose(); return;}
			int index=userButtons.indexOf(sender); if (index>=0) {userButtonClick(index,userButtons.get(index)); return;}
		}
	}
}