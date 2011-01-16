#
# HelloWorld.py  -  sample JOSM script in Python
# 
from javax.swing import JOptionPane
from org.openstreetmap.josm import Main

JOptionPane.showMessageDialog(Main.parent, "[Python] Hello World!")
