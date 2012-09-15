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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
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



/**
 * Header file generator for JNI.
 *
 * Supports only the creation of the header file for custom code.
 */
public class StaticJNIStaticHeader extends StaticJNIGen {
    StaticJNIStaticHeader(Util util, StaticJNIClassHelper helper) {
        super(util, helper);
    }

    public String getIncludes() {
    	return "";
    }
    public String getIncludes( CharSequence className ) {
        return "#include \"" + baseFileName(className) + "_frontier.h\"";
    }

    public void write(OutputStream o, TypeElement clazz) throws Util.Exit {
    	super.write( o, clazz );
        try {
            String cname = mangler.mangle(clazz.getQualifiedName(), Mangle.Type.CLASS);
            PrintWriter pw = wrapWriter(o);
            
            pw.println(getIncludes(clazz.getQualifiedName()));
            
            pw.println(guardBegin(cname+"__custom"));
            pw.println(cppGuardBegin());

            /* Write statics. */
            List<VariableElement> classfields = getAllFields(clazz);

            for (VariableElement v: classfields) {
                if (!v.getModifiers().contains(Modifier.STATIC))
                    continue;
                String s = null;
                s = defineForStatic(clazz, v);
                if (s != null) {
                    pw.println(s);
                }
            }

            /* Write methods. */
            List<ExecutableElement> classmethods = ElementFilter.methodsIn(clazz.getEnclosedElements());
            for (ExecutableElement md: classmethods) {
                if(md.getModifiers().contains(Modifier.NATIVE)){
                    TypeMirror mtr = types.erasure(md.getReturnType());
                    String sig = signature(md);
                    TypeSignature newtypesig = new TypeSignature(elems);
                    CharSequence methodName = md.getSimpleName();
                    boolean longName = false; /* if overloaded */
                    for (ExecutableElement md2: classmethods) {
                        if ((md2 != md)
                            && (methodName.equals(md2.getSimpleName()))
                            && (md2.getModifiers().contains(Modifier.NATIVE)))
                            longName = true;

                    }
                    pw.println("/*");
                    pw.println(" * Class:     " + cname);
                    pw.println(" * Method:    " +
                               mangler.mangle(methodName, Mangle.Type.FIELDSTUB));
                    pw.println(" * Signature: " + newtypesig.getTypeSignature(sig, mtr));
					
					/* Xy */
                    pw.println(" * Imported methods: " );
                    pw.println(" */");
                    
                    
                    pw.println( staticjniType(mtr) + " " +
                               getCImplName( md, clazz, longName ) );
                    pw.print("  ( ");
                    
                    List<? extends VariableElement> paramargs = md.getParameters();
                    List<TypeMirror> args = new ArrayList<TypeMirror>();
                    for (VariableElement p: paramargs) {
                        args.add(types.erasure(p.asType()));
                    }
                    if (md.getModifiers().contains(Modifier.STATIC))
                        pw.print("jclass"); // à conserver
                    else
                        pw.print( staticjniType(clazz.asType()) );

                    for (TypeMirror arg: args) {
                        pw.print(", ");
                        pw.print(staticjniType(arg));
                    }
                    pw.println(");" + lineSep);
                }
            }
            
            pw.println(cppGuardEnd());
            pw.println(guardEnd(cname));
        } catch (TypeSignature.SignatureException e) {
            util.error("jni.sigerror", e.getMessage());
        }
    }
}
