package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;

import javax.swing.JTabbedPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.plugins.scripting.RunScriptDialog;

public class ConfigureAction extends JosmAction {
	public ConfigureAction() {
		super(
			tr("Configure..."),        // title
			"preference", 			    // icon name
			tr("Configure scripting preferences"),  // tooltip 
			null,                // no shortcut 
			false                // don't register
		);		
	}
	
	protected Component getChildByName(Component parent, String name){
		if (parent == null) return null;
		if (name == null) return null;
		if (name.equals(parent.getName())) return parent;
		if (parent instanceof Container) {
			for (Component child: ((Container)parent).getComponents()) {
				Component found = getChildByName(child, name);
				if (found != null) return found;
			}
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		PreferenceDialog dialog = new PreferenceDialog(Main.parent);
		
		/*
		 * FIXME: JOSM core doesn't provide a method to jump to a specific preference
		 * tab in the preference dialog. We use reflection as work around.
		 * Fix this as soon as the JOSM core provides a better way.
		 * 
		 * Exception handling: just dump the stack to the console, no further action
		 * required. If we can't open the preference editor at the tab for 
		 * scripting preferences, it is started at the default start position
		 * instead. Not optimal, but no big deal.
		 */
		try {
			Field f = PreferenceDialog.class.getDeclaredField("tpPreferences");
			f.setAccessible(true);
			JTabbedPane tp = (JTabbedPane)f.get(dialog);
			/*
			 * lookup the preferences tab which is an ancestor of our scripting
			 * preferences panel
			 */
			for(int i=0; i< tp.getTabCount(); i++) {
				if (getChildByName(tp.getComponentAt(i), "scripting.preferences.editor") != null) {
					tp.setSelectedIndex(i);
					break;
				}
			}
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		} catch(SecurityException e){
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			dialog.setVisible(true);
		}
	}		
}
