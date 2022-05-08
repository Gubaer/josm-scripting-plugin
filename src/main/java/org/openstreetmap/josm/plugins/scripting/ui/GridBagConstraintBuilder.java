package org.openstreetmap.josm.plugins.scripting.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

@SuppressWarnings("unused")
public class GridBagConstraintBuilder {

    private GridBagConstraints gbc = new GridBagConstraints();

    static public GridBagConstraintBuilder gbc() {
        return gbc(null);
    }

    static public GridBagConstraintBuilder gbc(GridBagConstraints gc) {
        return new GridBagConstraintBuilder(gc);
    }

    public GridBagConstraintBuilder() {
        this(null);
    }

    public GridBagConstraintBuilder(GridBagConstraints gbc) {
        this.reset();
        if (gbc != null){
            this.gbc = (GridBagConstraints)gbc.clone();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public GridBagConstraintBuilder reset() {
        gbc = new GridBagConstraints();
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill  = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        return this;
    }

    public GridBagConstraintBuilder gridx(int x){
        gbc.gridx = x;
        return this;
    }

    public GridBagConstraintBuilder gridy(int y){
        gbc.gridy = y;
        return this;
    }

    public GridBagConstraintBuilder cell(int x, int y){
        gbc.gridx  =x;
        gbc.gridy = y;
        return this;
    }

    public GridBagConstraintBuilder cell(int x, int y, int width, int height){
        gbc.gridheight = height;
        gbc.gridwidth = width;
        gbc.gridx = x;
        gbc.gridy = y;
        return this;
    }

    public GridBagConstraintBuilder weightx(double weightx){
        gbc.weightx = weightx;
        return this;
    }

    public GridBagConstraintBuilder weighy(double weighty){
        gbc.weighty = weighty;
        return this;
    }

    public GridBagConstraintBuilder weight(double weightx, double weighty){
        gbc.weightx  =weightx;
        gbc.weighty = weighty;
        return this;
    }

    public GridBagConstraintBuilder anchor(int anchor){
        gbc.anchor = anchor;
        return this;
    }

    public GridBagConstraintBuilder row(int y){
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        return this;
    }

    public GridBagConstraintBuilder nofill() {
        gbc.fill = GridBagConstraints.NONE;
        return this;
    }

    public GridBagConstraintBuilder fillboth() {
        gbc.fill = GridBagConstraints.BOTH;
        return this;
    }

    public GridBagConstraintBuilder fill(int fill) {
        gbc.fill = fill;
        return this;
    }

    public GridBagConstraintBuilder fillHorizontal() {
        return fill(GridBagConstraints.HORIZONTAL);
    }

    public GridBagConstraintBuilder fillVertical() {
        return fill(GridBagConstraints.VERTICAL);
    }

    public GridBagConstraintBuilder spacingright(int space){
        if (gbc.insets == null) {
            gbc.insets = new Insets(0,0,0,0);
        }
        gbc.insets.right= space;
        return this;
    }

    public GridBagConstraintBuilder spacingleft(int space){
        if (gbc.insets == null) {
            gbc.insets = new Insets(0,0,0,0);
        }
        gbc.insets.left= space;
        return this;
    }

    public GridBagConstraintBuilder insets(int top, int right, int bottom,
            int left) {
        gbc.insets = new Insets(top, right, bottom, left);
        return this;
    }

    public GridBagConstraintBuilder insets(Insets insets) {
        gbc.insets = insets;
        return this;
    }

    public GridBagConstraints constraints() {
        return gbc;
    }
}
