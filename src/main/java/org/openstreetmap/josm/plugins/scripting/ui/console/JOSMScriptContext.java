package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.io.Writer;

import javax.script.SimpleScriptContext;

public class JOSMScriptContext extends SimpleScriptContext{

    private Writer out;

    public JOSMScriptContext(Writer out){
        this.out = out;
    }

    @Override
    public Writer getWriter() {
        return out;
    }

    @Override
    public Writer getErrorWriter() {
        return out;
    }
}
