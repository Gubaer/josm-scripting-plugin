package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;
import org.openstreetmap.josm.plugins.scripting.util.IOUtil;

import jsyntaxpane.DefaultSyntaxKit;
/**
 * <p>Provides a map of mime types to class names for syntax kits. Looks for two configuration files:</p>
 *
 * <ol>
 *   <li><tt>/resources/syntax-kit-map.default</tt> - the default mappings provided in the plugin jar</li>
 *   <li><tt>$PLUGIN_DATA_DIR/syntax-kit-map</tt> - additional local mappings</li>
 * </ol>
 *
 */
public class MimeTypeToSyntaxKitMap {

    static private MimeTypeToSyntaxKitMap instance = null;
    public static MimeTypeToSyntaxKitMap getInstance() {
        if (instance == null){
            instance = new MimeTypeToSyntaxKitMap();
            instance.loadFromResourceFiles();
        }
        return instance;
    }

    private Map<String,String> map = new HashMap<String,String>();

    protected boolean isClassAvailable(String mimeType, String className){
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println(tr("Warning: syntax kit ''{0}'' can''t be loaded. Ignoring mapping for mime type ''{1}''.", className, mimeType));
            return false;
        }
    }

    public boolean isSupported(String mimeType){
        return map.containsKey(mimeType);
    }

    protected void loadMappings(BufferedReader br) {
        Pattern p = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)");
        br.lines()
            .filter(l -> !l.matches("^\\s*#"))
            .forEach(line -> {
                Matcher m = p.matcher(line);
                if (!m.matches()) return;
                String mimeType = m.group(1);
                String className = m.group(2);
                if (! isClassAvailable(mimeType, className)) return;
                map.put(mimeType, className);
            });
    }

    protected void loadDefaultMappings() {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/resources/syntax-kit-map.default");
            if (in == null){
                System.out.println(tr("Warning: failed to open resource file ''{0}''", "/resources/syntax-kit-map.default"));
            }
            System.out.println(tr("Loading default map from mime-types to syntax kits from resource ''{0}''", "/resources/syntax-kit-map.default"));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            loadMappings(br);
        } finally {
            IOUtil.close(in);
        }
    }

    protected void loadLocalMappings() {
        ScriptingPlugin plugin = ScriptingPlugin.getInstance();
        if (plugin == null) return;
        String dir = plugin.getPluginDir();
        if (dir == null) return;
        File f = new File(dir, "syntax-kit-map");
        if (! f.exists() || !f.isFile() || !f.canRead()) return;
        try (InputStream in = new FileInputStream(f)){
            System.out.println(tr("Loading local map from mime-types to syntax kits from resource ''{0}''",f.toString()));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            loadMappings(br);
        } catch(IOException e){
            System.out.println(tr("Error: failed to load local map from mime-types to syntax kits"));
            e.printStackTrace();
        }
    }

    protected void configureDefaultSyntaxKit(){
        map.entrySet().stream().forEach(e -> {
           DefaultSyntaxKit.registerContentType(e.getKey(), e.getValue());
        });
    }

    public void loadFromResourceFiles() {
        map.clear();
        loadDefaultMappings();
        loadLocalMappings();
        configureDefaultSyntaxKit();
    }
}
