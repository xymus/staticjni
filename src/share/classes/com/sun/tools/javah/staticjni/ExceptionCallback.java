package com.sun.tools.javah.staticjni;

import javax.lang.model.type.TypeMirror;

public class ExceptionCallback {
	public TypeMirror exceptionType;

    public ExceptionCallback( TypeMirror exceptionType ) {
    	this.exceptionType = exceptionType;
    }
    public String toString() {
        return "<ExceptionCallback " + exceptionType.toString() + ">";
    }
    
    @Override
    public int hashCode() {
    	return exceptionType.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	return this.getClass() == obj.getClass() && ((ExceptionCallback)obj).exceptionType.equals(exceptionType);
    }
}
