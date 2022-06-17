package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.CommonJSModuleRepositoryRegistry;
import org.openstreetmap.josm.plugins.scripting.ui.EditorPaneBuilder;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.*;

import static org.openstreetmap.josm.tools.I18n.tr;

public class CommonJSRepoConfigurationPanel extends AbstractRepoConfigurationPanel{

    protected JPanel buildInfoPanel() {
        final JEditorPane pane = EditorPaneBuilder.buildInfoEditorPane();
        final String text =
            "<html>"
            + tr(
                 "<p>"
                + "GraalJS can load <strong>CommonJS modules</strong> "
                + "with the function <code>require()</code>. It resolves CommonJS modules "
                + "in the directories and jar files configured below."
                + "</p>"
            )
            + "</html>";
        pane.setText(text);
        final JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(pane, BorderLayout.CENTER);
        return pnl;
    }

    protected RepositoriesListModel buildRepositoriesListModel(@NotNull final ListSelectionModel selectionModel) {
        final var model = new RepositoriesListModel(selectionModel);
        model.loadRepositories(CommonJSModuleRepositoryRegistry.getInstance());
        return model;
    }

    public void persistToPreferences() {
        // will also persist the configured CommonJS module base URIs to
        // preferences
        mdlRepositories.saveRepositories(CommonJSModuleRepositoryRegistry.getInstance());
        CommonJSModuleRepositoryRegistry.getInstance().saveToPreferences(Preferences.main());
    }
}
