/*
* HelloWorld.js  -  displays the number of open layers
*/

const JOptionPane = Java.type('javax.swing.JOptionPane')
const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')

const numLayers = MainApplication.getLayerManager().getLayers().size()
JOptionPane.showMessageDialog(
    MainApplication.getMainFrame(),
    `[JavaScript] Hello World! You have ${numLayers} layers(s)`
)
