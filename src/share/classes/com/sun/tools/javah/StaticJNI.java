/*
 * Copyright (c) 2002, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.javah;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import com.sun.tools.javac.code.Attribute.Array;
import com.sun.tools.javah.Util.Exit;


/**
 * Header file generator for JNI.
 *
 * Not a true Gen... Wrapper for 3 other Gens
 */
public class StaticJNI extends StaticJNIGen {
    StaticJNI(Util util) {
        super(util, null);
        
        helper = new StaticJNIClassHelper(this);
        
        frontier_header = new StaticJNIFrontierHeader(util, helper);
        frontier_body = new StaticJNIFrontierBody(util, helper);
        static_header = new StaticJNIStaticHeader(util, helper);
        
        subs = new ArrayList<Gen>();
        subs.add(frontier_header);
        subs.add(frontier_body);
        subs.add(static_header);
    }
    
    protected StaticJNIFrontierHeader frontier_header;
    protected StaticJNIFrontierBody frontier_body;
    protected StaticJNIStaticHeader static_header;
    protected ArrayList<Gen> subs;
    
    //StaticJNIClassHelper helper;

    @Override
    public String getIncludes() {
        return "";
    }
    
    @Override
    public void setClasses(Set<TypeElement> classes) {
    	// TODO Auto-generated method stub
    	super.setClasses(classes);
    	
    	for ( Gen g: subs ) g.setClasses(classes);
    }
    
    @Override
    void setProcessingEnvironment(javax.annotation.processing.ProcessingEnvironment pEnv) {
    	super.setProcessingEnvironment(pEnv);
    	for ( Gen g: subs ) g.setProcessingEnvironment(pEnv);
    }
    
    @Override
    public void setForce(boolean state) {
    	super.setForce(state);
    	for ( Gen g: subs ) g.setForce(state);
    }
    
    @Override
    public void setFileManager(javax.tools.JavaFileManager fm) {
    	super.setFileManager(fm);
    	for ( Gen g: subs ) g.setFileManager(fm);
    }

    @Override
    public void run() throws IOException, ClassNotFoundException, Util.Exit {
        for ( Gen g: subs ) g.run();
    }

	@Override
	public void write(OutputStream o, TypeElement clazz) throws Exit {
	}
	
	@Override
    public void setOutFile(JavaFileObject outFile) {
        static_header.setOutFile(outFile);
    }

	public void setFileManagerLocal(JavaFileManager fileManager) {
		super.setFileManager(fileManager);
		frontier_header.setFileManager(fileManager);
	    frontier_body.setFileManager(fileManager);
	}
}
