/*
 * This scripts sets a sequence of house numbers on the currently selected nodes.
 * 
 * The user can enter a start number and and an increment.
 */

import java.awt.BorderLayout;

import groovy.swing.SwingBuilder;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;
import org.openstreetmap.josm.data.osm.DataSet
import org.openstreetmap.josm.data.osm.Node;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.Dimension;

class AddHouseNumberDialog extends JDialog {
	
	private JTextField tfStart;
	private JTextField tfIncrement;
	
	public AddHouseNumberDialog(){
		super(Main.parent, true /* modal */)
		build();
	}
	
	def buildInfoPanel() {
		def info = new HtmlPanel(
		"""
		<html>
		Enter the <strong>first house number</strong> to be applied to the currently selected nodes
		and the amount by which the house number is <strong>incremented</strong>.
		</html>
		"""
		)
	}
	
	def buildInputPanel() {
		SwingBuilder swing = new SwingBuilder()
		return swing.panel(){
			gridBagLayout()
			label(text: "Start:", 
				horizontalAlignment: JLabel.LEFT,
				constraints: gbc(gridx:0,gridy:0,weightx:0.0, weighty:0.0, fill: GridBagConstraints.NONE, anchor: GridBagConstraints.WEST)
			)
			tfStart = textField(constraints: gbc(gridx:1,gridy:0,weightx:1.0, weighty:0.0, fill: GridBagConstraints.HORIZONTAL, insets:[2,2,2,2]))
			label(text: "Increment:", horizontalAlignment: JLabel.LEFT, constraints: gbc(gridx:0,gridy:1,weightx:0.0, weighty:0.0, anchor: GridBagConstraints.WEST, insets:[2,2,2,2]))
			tfIncrement = textField(constraints: gbc(gridx:1,gridy:1,weightx:1.0, weighty:0.0, fill: GridBagConstraints.HORIZONTAL, anchor: GridBagConstraints.WEST, insets:[2,2,2,2]))
			panel(constraints: gbc(gridx:0,gridy:2,weightx:1.0, weighty:1.0, gridwidth:2, fill: GridBagConstraints.BOTH, insets:[2,2,2,2]))
		} 
	}
	
	def buildControlButtonPanel() {
		SwingBuilder swing = new SwingBuilder()
		return swing.panel(layout: new FlowLayout(FlowLayout.CENTER)) {
			button(text: "Cancel", icon: ImageProvider.get("cancel"), actionPerformed: {setVisible(false)})
			button(text: "Apply", icon: ImageProvider.get("ok"), actionPerformed: {
				apply()
				setVisible(false)
			})
		}
	}
	
	def apply() {
		def start
		try {
			start = tfStart.text.trim().toInteger()
		} catch(NumberFormatException e){
			e.printStackTrace()
			return
		}
		def incr
		try  {
			incr = tfIncrement.text.trim().toInteger()
		} catch(NumberFormatException e){
			e.printStackTrace()
			return
		} 
		def nodes = getCurrentlySelectedNodes()
		def cmds = []
		nodes.each {Node n ->
			Node nn = new Node(n)
			nn.put("addr:housenumber", start.toString())
			start += incr
			cmds << new ChangeCommand(n, nn)			
		}
		if (cmds.isEmpty()) return
		Main.main.undoRedo.add(new SequenceCommand("Setting house numbers", cmds))
	}
	
	def build() {
		setTitle("Set house numbers")
		def cp = getContentPane()
		cp.setLayout(new BorderLayout())
		cp.add(buildInfoPanel(), BorderLayout.NORTH)
		cp.add(buildInputPanel(), BorderLayout.CENTER)
		cp.add(buildControlButtonPanel(), BorderLayout.SOUTH)
	}	
	
	def getCurrentDataSet() {
		def layer = Main?.map?.mapView?.getActiveLayer()
		if (layer == null) return null
		if (! (layer instanceof OsmDataLayer)) return null
		return layer.data
	}
	
	def getCurrentlySelectedNodes() {
		def DataSet ds = getCurrentDataSet()
		if (ds == null) return []
		return ds.getSelectedNodes().asList()
	}
	@Override
	public void setVisible(boolean b) {
		if (b){
			WindowGeometry.centerInWindow(getParent(), new Dimension(400,200)).applySafe(this)
		}
		super.setVisible(b);
	}
}

def dialog = new AddHouseNumberDialog()
dialog.setVisible(true)