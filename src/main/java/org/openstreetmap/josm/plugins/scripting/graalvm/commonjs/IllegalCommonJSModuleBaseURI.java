package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

@SuppressWarnings("unused")
public class IllegalCommonJSModuleBaseURI extends Exception {
    public IllegalCommonJSModuleBaseURI(String s) {
        super(s);
    }

    public IllegalCommonJSModuleBaseURI(Throwable throwable) {
        super(throwable);
    }

    public IllegalCommonJSModuleBaseURI(String s, Throwable throwable) {
        super(s, throwable);
    }
}
