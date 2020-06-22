package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;

import com.drew.lang.annotations.NotNull;
import org.openstreetmap.josm.plugins.scripting.preferences.graalvm.GraalVMPrivilegesModel.TernaryAccessPolicy;
import org.openstreetmap.josm.plugins.scripting.ui.EditorPaneBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

public class GraalVMPrivilegesPanel extends JPanel {

    static private final Logger logger =
        Logger.getLogger(GraalVMPrivilegesPanel.class.getName());

    private JRadioButton rbAllAccessAllow;
    private JRadioButton rbAllAccessDeny;

    private abstract static class TernaryAccessPolicyPanel extends JPanel {

        private JRadioButton rbDefault;
        private JRadioButton rbAllow;
        private JRadioButton rbDeny;
        private JEditorPane epText;
        private ButtonGroup bgOptions;
        protected GraalVMPrivilegesModel model;
        protected String text = "missing text";

        TernaryAccessPolicyPanel(@NotNull final GraalVMPrivilegesModel model) {
            this.model = model;
            build();
            initWithPolicy(readFromModel());
        }

        public void setText(@NotNull final String text) {
            this.text = text;
            if (epText != null) {
                epText.setText(text);
            }
        }

        public @NotNull String getText() {
            return text;
        }

        private void buildRadioButtons() {
            rbDefault = new JRadioButton(tr("derive"));
            rbDefault.setActionCommand("set-derive");
            rbDefault.setToolTipText(tr("Allow or deny based on the default value for all access privileges"));

            rbAllow = new JRadioButton(tr("allow"));
            rbAllow.setActionCommand("set-allow");
            rbDefault.setToolTipText(tr("Grant the GraalVM the access privilege"));

            rbDeny = new JRadioButton(tr("deny"));
            rbDeny.setActionCommand("set-deny");
            rbDeny.setToolTipText(tr("Deny the GraalVM the access privilege"));

            bgOptions = new ButtonGroup();
            bgOptions.add(rbDefault);
            bgOptions.add(rbAllow);
            bgOptions.add(rbDeny);

            rbDefault.addActionListener(this::onSelectOption);
            rbAllow.addActionListener(this::onSelectOption);
            rbDeny.addActionListener(this::onSelectOption);
        }

        private JPanel buildButtonPanel() {
            // prepare button panel
            final JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnl.add(rbDefault);
            pnl.add(rbAllow);
            pnl.add(rbDeny);
            return pnl;
        }

        private JPanel buildTextPanel() {
            // prepare info panel with description and help text
            epText = EditorPaneBuilder.buildInfoEditorPane();
            epText.setText(text);
            final JPanel pnl = new JPanel(new BorderLayout());
            pnl.add(epText, BorderLayout.CENTER);
            return pnl;
        }

        private void build() {
            buildRadioButtons();

            // assemble the panel consisting of the info and the button panel
            GridBagConstraints gc;
            final Insets insets = new Insets(0,3,3,0);
            setLayout(new GridBagLayout());
            gc = gbc().cell(0,0).fillHorizontal()
                    .weight(1.0, 0.0).insets(insets).constraints();
            add(buildTextPanel(), gc);
            gc = gbc().cell(0,1).fillboth()
                    .weight(1.0, 0.0).insets(insets).constraints();
            add(buildButtonPanel(), gc);
        }

        private void onSelectOption(final ActionEvent event) {
            final TernaryAccessPolicy policy;
            switch(event.getActionCommand()) {
                case "set-derive":
                    policy = TernaryAccessPolicy.DERIVE;
                    break;
                case "set-allow":
                    policy = TernaryAccessPolicy.ALLOW;
                    break;
                case "set-deny":
                    policy = TernaryAccessPolicy.DENY;
                    break;
                default:
                    // should not happen, but just in case
                    final String message = "unexpected action command '%s'";
                    logger.log(Level.WARNING,
                        String.format(message, event.getActionCommand()));
                    throw new IllegalStateException(message);
            }
            writeToModel(policy);
        }

        protected void initWithPolicy(@NotNull final TernaryAccessPolicy policy) {
            Objects.requireNonNull(policy);
            ButtonModel selected;
            switch(policy) {
                case DERIVE:
                    selected = rbDefault.getModel();
                    break;
                case ALLOW:
                    selected = rbAllow.getModel();
                    break;
                case DENY:
                    selected = rbDeny.getModel();
                    break;
                default:
                    final String message = "unexpected policy '%s'";
                    logger.log(Level.WARNING, String.format(message, policy));
                    throw new IllegalStateException(message);
            }

            bgOptions.setSelected(selected, true);
        }

        protected abstract @NotNull TernaryAccessPolicy readFromModel();
        protected abstract void writeToModel(@NotNull TernaryAccessPolicy policy);
    }

    static private class CreateProcessPrivilegePanel extends TernaryAccessPolicyPanel {
        static private final String TEXT =
              "<html>"
              + tr(
                  "<p>"
                + "Are scripts allowed to execute external processes? "
                + "Default is false (<strong>recommended</strong>). "
                + "If all access is set to true, then process creation is "
                + "enabled to. To override, select <strong>deny</strong> below."
                + "</p>"
              )
            + "</html>";

        public CreateProcessPrivilegePanel(GraalVMPrivilegesModel model) {
            super(model);
            setText(TEXT);
        }

        @Override
        protected TernaryAccessPolicy readFromModel() {
            return model.getCreateProcessPolicy();
        }

        @Override
        protected void writeToModel(TernaryAccessPolicy policy) {
            model.setCreateProcessPolicy(policy);
        }
    }

    private static class CreateThreadPrivilegePanel extends  TernaryAccessPolicyPanel {
        static final String TEXT =
            "<html>"
            + tr(
                "<p>"
              + "Are scripts allowed to create and execute new threads? "
              + "Default is false (<strong>recommended</strong>). "
              + "If all access is set to true, then the creation of "
              + "threads is enabled to. To override, select <strong>deny</strong> below."
              + "</p>"
              )
            + "</html>";

        public CreateThreadPrivilegePanel(GraalVMPrivilegesModel model) {
            super(model);
            setText(TEXT);
        }

        @Override
        protected TernaryAccessPolicy readFromModel() {
            return model.getCreateThreadPolicy();
        }

        @Override
        protected void writeToModel(TernaryAccessPolicy policy) {
            model.setCreateThreadPolicy(policy);
        }
    }

    private static class UseExperimentalOptionsPrivilegePanel extends TernaryAccessPolicyPanel {

        static final String TEXT =
            "<html>"
            + tr(
                  "<p>"
                + "Are scripts allowed to use experimental language options? "
                + "Default is false (<strong>recommended</strong>). "
                + "If all access is set to true, then usage of experimental "
                + "language options is allowed to. "
                + "To override, select <strong>deny</strong> below."
                + "</p>"
            )
            + "</html>";

        public UseExperimentalOptionsPrivilegePanel(GraalVMPrivilegesModel model) {
            super(model);
            setText(TEXT);
        }

        @Override
        protected TernaryAccessPolicy readFromModel() {
            return model.getUseExperimentalOptionsPolicy();
        }

        @Override
        protected void writeToModel(TernaryAccessPolicy policy) {
            model.setUseExperimentalOptionsPolicy(policy);
        }
    }

    private static class HostClassLoadingPrivilegePanel extends TernaryAccessPolicyPanel {

        static final String TEXT =
            "<html>"
            + tr(
                  "<p>"
                + "Are scripts allowed to load new Java classes via jar or class files? "
                + "Default is false (<strong>recommended</strong>). "
                + "If all access is set to true, host class loading  "
                + "is allowed to. "
                + "To override, select <strong>deny</strong> below."
                + "</p>"
            )
            + "</html>";

        public HostClassLoadingPrivilegePanel(GraalVMPrivilegesModel model) {
            super(model);
            setText(TEXT);
        }

        @Override
        protected TernaryAccessPolicy readFromModel() {
            return model.getHostClassLoadingPolicy();
        }

        @Override
        protected void writeToModel(TernaryAccessPolicy policy) {
            model.setHostClassLoadingPolicy(policy);
        }
    }

    private static class IOPolicyPanel extends TernaryAccessPolicyPanel {

        static final String TEXT =
            "<html>"
            + tr(
                  "<p>"
                + "Are scripts allowed to perform unrestricted IO operations on the host system "
                + "(open and read files, create files and directories, etc.)? "
                + "Default is false (<strong>recommended</strong>). "
                + "If all access is set to true, then unrestricted IO "
                + "is allowed to. "
                + "To override, select <strong>deny</strong> below."
                + "</p>"
            )
            + "</html>";

        public IOPolicyPanel(GraalVMPrivilegesModel model) {
            super(model);
            setText(TEXT);
        }

        @Override
        protected TernaryAccessPolicy readFromModel() {
            return model.getIOPolicy();
        }

        @Override
        protected void writeToModel(TernaryAccessPolicy policy) {
            model.setIOPolicy(policy);
        }
    }


    final private GraalVMPrivilegesModel model;

    GraalVMPrivilegesPanel(@NotNull final GraalVMPrivilegesModel model) {
        this.model = model;
        build();
    }

    public JPanel buildDefaultAccessPrivilegesPanel() {

        final ButtonGroup group = new ButtonGroup();
        rbAllAccessAllow = new JRadioButton(tr("allow all access"));
        rbAllAccessAllow.setActionCommand("allow-all-access");
        rbAllAccessDeny = new JRadioButton(tr("deny all access"));
        rbAllAccessDeny.setActionCommand("deny-all-access");
        group.add(rbAllAccessAllow);
        group.add(rbAllAccessDeny);

        // init state with current value
        group.setSelected(rbAllAccessAllow.getModel(), model.isDefaultAccessAllowed());

        // install action listeners
        final ActionListener listener = event -> {
            final String command = group.getSelection().getActionCommand();
            switch(command) {
                case "allow-all-access":
                    model.setDefaultAccessPolicy(
                        GraalVMPrivilegesModel.DefaultAccessPolicy.ALLOW_ALL);
                    break;
                case "deny-all-access":
                    model.setDefaultAccessPolicy(
                        GraalVMPrivilegesModel.DefaultAccessPolicy.DENY_ALL);
                    break;
                default:
                    final String message = "unexpected action command '%s'. Ignoring.";
                    logger.log(Level.WARNING, String.format(message, command));
            }
        };
        Collections.list(group.getElements())
            .forEach(button -> button.addActionListener(listener));

        // prepare info panel with description and help text
        final JEditorPane pane = EditorPaneBuilder.buildInfoEditorPane();
        final String text =
            "<html>"
            + tr(
            "<p>"
                + "Sets the default value for all privileges. If not explicitly "
                + "specified, then all access is false. If all access is "
                + "enabled then certain privileges may still be disabled by "
                + "configuring it explicitly below."
            + "</p>"
            )
            + "</html>";
        pane.setText(text);
        final JPanel panePanel = new JPanel(new BorderLayout());
        panePanel.add(pane, BorderLayout.CENTER);

        // prepare the button panel
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(rbAllAccessAllow);
        buttonPanel.add(rbAllAccessDeny);

        GridBagConstraints gc;
        final Insets insets = new Insets(0,3,3,0);
        final JPanel pnl = new JPanel();
        setLayout(new GridBagLayout());
        gc = gbc().cell(0,0).fillHorizontal()
                .weight(1.0, 0.0).insets(insets).constraints();
        pnl.add(panePanel, gc);
        gc = gbc().cell(0,1).fillboth()
                .weight(1.0, 0.0).insets(insets).constraints();
        pnl.add(buttonPanel, gc);
        return pnl;
    }

    public void build() {
        setLayout(new GridBagLayout());

        GridBagConstraints gc;
        gc = gbc().cell(0,1).fillboth().weight(1.0, 0.0).constraints();
        add(buildDefaultAccessPrivilegesPanel(), gc);

        gc = gbc().cell(0,2).fillboth().weight(1.0, 0.0).constraints();
        add( new CreateProcessPrivilegePanel(model), gc);

        gc = gbc().cell(0,3).fillboth().weight(1.0, 0.0).constraints();
        add(new CreateThreadPrivilegePanel(model), gc);

        gc = gbc().cell(0,4).fillboth().weight(1.0, 0.0).constraints();
        add(new UseExperimentalOptionsPrivilegePanel(model), gc);

        gc = gbc().cell(0,5).fillboth().weight(1.0, 0.0).constraints();
        add(new HostClassLoadingPrivilegePanel(model), gc);

        gc = gbc().cell(0,6).fillboth().weight(1.0, 0.0).constraints();
        add(new IOPolicyPanel(model), gc);

        gc = gbc().cell(0,7).fillboth().weight(1.0, 1.0).constraints();
        add(new JPanel(), gc);

    }
}
