/*
* HelloWorld.js  -  displays the number of actually open layers 
*/
importClass(Packages.javax.swing.JOptionPane)
importClass(Packages.org.openstreetmap.josm.Main)

function getMapView() {
	if (Main.main == null) return null
	if (Main.main.map == null) return null
	return Main.main.map.mapView
}


var mv = getMapView()
var numlayers = mv == null ? 0 : mv.getNumLayers() 
JOptionPane.showMessageDialog(Main.parent, "[JavaScript] Hello World! You have " + numlayers + " layer(s).")

