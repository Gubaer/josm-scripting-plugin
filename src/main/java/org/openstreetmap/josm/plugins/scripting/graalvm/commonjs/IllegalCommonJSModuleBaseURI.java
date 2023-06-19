package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

@SuppressWarnings("unused")
public class IllegalCommonJSModuleBaseURI extends Exception {
    private static final long serialVersionUID = -6987925501214149150L;

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
