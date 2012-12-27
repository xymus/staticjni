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
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import com.sun.tools.javah.Util.Exit;
import com.sun.tools.javah.staticjni.ArrayCallback;
import com.sun.tools.javah.staticjni.Callback;
import com.sun.tools.javah.staticjni.ExceptionCallback;
import com.sun.tools.javah.staticjni.FieldCallback;



/**
 * Header file generator for JNI.
 *
 * Supports only the creation of the body of the frontier.
 */
public class StaticJNIFrontierHeader extends StaticJNIGen {
    StaticJNIFrontierHeader(Util util, StaticJNIClassHelper helper) {
        super(util, helper);
    }

    protected String getFileSuffix() {
        return "_frontier.h";
    }
    
    @Override
    public void write(OutputStream o, TypeElement clazz) throws Exit {
    	super.write( o, clazz );
        try {

            String cname = mangler.mangle(clazz.getQualifiedName(), Mangle.Type.CLASS);
            PrintWriter pw = wrapWriter(o);
            
            pw.println(getIncludes());
            
            pw.println(guardBegin(cname));
            pw.println(cppGuardBegin());
            
            pw.println( "extern JNIEnv *thread_env;" );
            
            // Declare indirect method
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
                    pw.println(" */");
                    pw.println("JNIEXPORT " + jniType(mtr) +
                               " JNICALL " +
                               mangler.mangleMethod(md, clazz,
                                                   (longName) ?
                                                   Mangle.Type.METHOD_JNI_LONG :
                                                   Mangle.Type.METHOD_JNI_SHORT));
                    pw.print("  (JNIEnv *, ");
                    List<? extends VariableElement> paramargs = md.getParameters();
                    List<TypeMirror> args = new ArrayList<TypeMirror>();
                    for (VariableElement p: paramargs) {
                        args.add(types.erasure(p.asType()));
                    }
                    if (md.getModifiers().contains(Modifier.STATIC))
                        pw.print("jclass");
                    else
                        pw.print("jobject");

                    for (TypeMirror arg: args) {
                        pw.print(", ");
                        pw.print(jniType(arg));
                    }
                    pw.println(");" + lineSep);
                }
            }

            // Write referred types
            pw.println();
            for ( TypeMirror t: helper.referredTypes ) {
                if ( helper.gen.advancedStaticType( t ) ) {
	                String tname = types.asElement(t).getSimpleName().toString();
	                pw.println( "#ifndef STATICJNI_TYPE_" + tname );
	                pw.println( "#define STATICJNI_TYPE_" + tname );
	                pw.println( "typedef struct s_" + tname + " {" );
	                pw.println( "\tjobject val;" );
	                pw.println( "} *" + tname + ";" );
	                pw.println( "#endif" );
	                pw.println();
                }
            }
            
            // Write callback signatures
            for ( Callback c: helper.callbacks ) {
                pw.println( normalSignature( c ) + ";" );
            }
            
            for ( FieldCallback c: helper.fieldCallbacks ) {
                pw.println( fieldSetterSignature( c ) + ";" );
                pw.println( fieldGetterSignature( c ) + ";" );
            }
            
            for ( Callback c: helper.superCallbacks ) {
                pw.println( superSignature( c ) + ";" );
            }
            
            for ( Callback c: helper.constCallbacks ) {
                pw.println( constructorSignature( c ) + ";" );
            }
            
            for ( ExceptionCallback c: helper.exceptionCallbacks ) {
                pw.println( throwSignature( c ) + ";" );
            }
            
            for ( ArrayCallback c: helper.arrayCallbacks ) {
                
                // Length
            	String length_sig = accessArrayLength(c, null);
            	String length_sig_local = accessArrayLength(c, clazz);
                pw.println( "jint " + length_sig_local + "( " + staticjniType(c.arrayType) + " );" );
                pw.println( "#ifndef " + length_sig );
                pw.println( "#define " + length_sig + " " + length_sig_local );
                pw.println( "#endif" );
            	
            	// Get
            	String get_sig = accessArrayGet(c, null, false);
            	String get_sig_local = accessArrayGet(c, clazz, false);
                pw.println( staticjniType(c.arrayType.getComponentType()) + " *" + get_sig_local  + "( " + staticjniType(c.arrayType) + " );" );
                pw.println( "#ifndef " + get_sig );
                pw.println( "#define " + get_sig + " " + get_sig_local );
                pw.println( "#endif" );
                
                // Release
            	String release_sig = accessArrayRelease(c, null, false);
            	String release_sig_local = accessArrayRelease(c, clazz, false);
                pw.println( "void " + release_sig_local + "( " + staticjniType(c.arrayType) + ", " + staticjniType( c.arrayType.getComponentType() ) + "* );" );
                pw.println( "#ifndef " + release_sig );
                pw.println( "#define " + release_sig + " " + release_sig_local );
                pw.println( "#endif" );

            	// macro structure
                pw.println( "#ifndef " + accessArrayStructureMacro(c,false) );
                pw.println( "#define " + accessArrayStructureMacro(c,false) + "(j,n,l) \\" );
                pw.println( "l = " + length_sig + "( j ); \\" );
                pw.println( "for( n = 0; n == 0? (n="+get_sig+"( j ))||1:0; " + release_sig + "( j, n ))" );
                pw.println( "#endif" );

            	// Get critical
            	get_sig = accessArrayGet(c, null, true);
            	get_sig_local = accessArrayGet(c, clazz, true);
                pw.println( staticjniType(c.arrayType.getComponentType()) + " *" + get_sig_local  + "( " + staticjniType(c.arrayType) + " );" );
                pw.println( "#ifndef " + get_sig );
                pw.println( "#define " + get_sig + " " + get_sig_local );
                pw.println( "#endif" );
                
                // Release critical
            	release_sig = accessArrayRelease(c, null, true);
            	release_sig_local = accessArrayRelease(c, clazz, true);
                pw.println( "void " + release_sig_local + "( " + staticjniType(c.arrayType) + ", " + staticjniType( c.arrayType.getComponentType() ) + "* );" );
                pw.println( "#ifndef " + release_sig );
                pw.println( "#define " + release_sig + " " + release_sig_local );
                pw.println( "#endif" );

            	// macro structure critical
                pw.println( "#ifndef " + accessArrayStructureMacro(c,true) );
                pw.println( "#define " + accessArrayStructureMacro(c,true) + "(j,n,l) \\" );
                pw.println( "l = " + length_sig + "( j ); \\" );
                pw.println( "for( n = 0; n == 0? (n="+get_sig+"( j ))||1:0; " + release_sig + "( j, n ))" );
                pw.println( "#endif" );
                
                // New
            	String new_sig = newArray(c, null);
            	String new_sig_local = newArray(c, clazz);
                pw.println( staticjniType(c.arrayType) + " " + new_sig_local + "( jsize size );" );
                pw.println( "#ifndef " + new_sig );
                pw.println( "#define " + new_sig + " " + new_sig_local );
                pw.println( "#endif" );
                
                // Set region
            	String set_region_sig = setArrayRegion(c, null);
            	String set_region_local = setArrayRegion(c, clazz);
                pw.println( "void " + set_region_local + "( " + staticjniType(c.arrayType) + " arr, jsize start, jsize len, " + 
                		staticjniType(c.arrayType.getComponentType()) + " *value );" );
                pw.println( "#ifndef " + set_region_sig );
                pw.println( "#define " + set_region_sig + " " + set_region_local );
                pw.println( "#endif" );
            }
            
            if ( helper.usesString ) {
            	String get_sig_local = accessStringGet(clazz);
            	String release_sig_local = accessStringRelease(clazz);
            	String length_sig_local = accessStringLength(clazz);
            	String get_sig = accessStringGet(null);
            	String release_sig = accessStringRelease(null);
            	String length_sig = accessStringLength(null);
            	
            	/** unicode **/
            	
            	// Get unicode
                pw.println( "jchar *" + get_sig_local  + "( jstring );" );
                pw.println( "#ifndef " + get_sig );
                pw.println( "#define " + get_sig + " " + get_sig_local );
                pw.println( "#endif" );
                
                // Release unicode
                pw.println( "void " + release_sig_local + "( jstring, const jchar* );" );
                pw.println( "#ifndef " + release_sig );
                pw.println( "#define " + release_sig + " " + release_sig_local );
                pw.println( "#endif" );
                
                // Length unicode
                pw.println( "jint " + length_sig_local + "( jstring );" );
                pw.println( "#ifndef " + length_sig );
                pw.println( "#define " + length_sig + " " + length_sig_local );
                pw.println( "#endif" );
                
                // New unicode
            	String new_sig = newString(null);
            	String new_sig_local = newString(clazz);
                pw.println( "jstring " + new_sig_local + "( const jchar* src, jsize size );" );
                pw.println( "#ifndef " + new_sig );
                pw.println( "#define " + new_sig + " " + new_sig_local );
                pw.println( "#endif" );

            	// macro structure unicode
                pw.println( "#ifndef " + accessStringStructureMacro() );
                pw.println( "#define " + accessStringStructureMacro() + "(j,n,l) \\" );
                pw.println( "l = " + length_sig + "( j ); \\" );
                pw.println( "for( n = 0; n == 0? (n="+get_sig+"( j ))||1:0; " + release_sig + "( j, n ))" );
                pw.println( "#endif" );
            	
            	/** utf8 **/
            	
            	// Get utf8
            	get_sig_local = get_sig_local + "_utf8";
            	get_sig = get_sig + "_utf8";
                pw.println( "jchar *" + get_sig_local  + "( jstring );" );
                pw.println( "#ifndef " + get_sig );
                pw.println( "#define " + get_sig + " " + get_sig_local );
                pw.println( "#endif" );
                
                // Release utf8
                release_sig_local = release_sig_local + "_utf8";
            	release_sig = release_sig + "_utf8";
                pw.println( "void " + release_sig_local + "( jstring, jchar* );" );
                pw.println( "#ifndef " + release_sig );
                pw.println( "#define " + release_sig + " " + release_sig_local );
                pw.println( "#endif" );
                
                // Length utf8
            	length_sig_local = length_sig_local + "_utf8";
            	length_sig = length_sig + "_utf8";
                pw.println( "jint " + length_sig_local + "( jstring );" );
                pw.println( "#ifndef " + length_sig );
                pw.println( "#define " + length_sig + " " + length_sig_local );
                pw.println( "#endif" );
                
                // New utf8
            	new_sig = new_sig + "_utf8";
            	new_sig_local = new_sig_local + "_utf8";
                pw.println( "jstring " + new_sig_local + "( const jchar* src, jsize size );" );
                pw.println( "#ifndef " + new_sig );
                pw.println( "#define " + new_sig + " " + new_sig_local );
                pw.println( "#endif" );

            	// macro structure utf8
                pw.println( "#ifndef " + accessStringStructureMacro() + "_utf8" );
                pw.println( "#define " + accessStringStructureMacro() + "_utf8(j,n,l) \\" );
                pw.println( "l = " + length_sig + "( j ); \\" );
                pw.println( "for( n = 0; n == 0? (n="+get_sig+"( j ))||1:0; " + release_sig + "( j, n ))" );
                pw.println( "#endif" );
            }
            
            pw.println(cppGuardEnd());
            pw.println(guardEnd(cname));
        } catch (TypeSignature.SignatureException e) {
            util.error("jni.sigerror", e.getMessage());
        }
    }
}
