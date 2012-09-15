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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import javax.lang.model.element.AnnotationMirror;
import javax.swing.text.Utilities;

import com.sun.mirror.type.VoidType;
import com.sun.tools.classfile.Annotation;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.util.Pair;

import net.xymus.staticjni.*;

/**
 * Header file generator for JNI.
 *
 * Supports only the creation of the body of the frontier.
 */
public class StaticJNIFrontierBody extends StaticJNIGen {
    StaticJNIFrontierBody(Util util, StaticJNIClassHelper helper) {
        super(util, helper);
    }

    protected String getFileSuffix() {
        return "_frontier.c";
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
            
            pw.println(cppGuardBegin());
            
            pw.println( "JNIEnv *thread_env;" );
            
            /* List of callbacks to Java */
          //  Set<ExecutableElement> callbacks = new HashSet<>();
          //  Set<VariableElement> callbacks_to_field = new HashSet<>();

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

            /* Write outgoing methods. */
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
                    pw.println(" * Imported methods:" );
                    pw.println(" */");
                    

                    List<? extends VariableElement> paramargs = md.getParameters();
                    List<TypeMirror> args = new ArrayList<TypeMirror>();
                    for (VariableElement p: paramargs) {
                        args.add(types.erasure(p.asType()));
                    }
                    
                    // impl declaration

                	pw.print("extern " + staticjniType( mtr ) + " " + getCImplName( md, clazz, longName ) + "( " );
                	if (md.getModifiers().contains(Modifier.STATIC))
                		pw.print("jclass");
                	else
                		pw.print( staticjniType( clazz.asType() ) );
                    for (TypeMirror arg: args) pw.print( ", " + staticjniType(arg) );
                	pw.println( " );" );
                    
                    
                    // implementation of outgoing calls (Java -> C)
                    // Normal JNI function impl
                    pw.println("JNIEXPORT " + jniType(mtr) +
                               " JNICALL " +
                               mangler.mangleMethod(md, clazz,
                                                   (longName) ?
                                                   Mangle.Type.METHOD_JNI_LONG :
                                                   Mangle.Type.METHOD_JNI_SHORT));
                    pw.print("  (JNIEnv *env, ");
                	if (md.getModifiers().contains(Modifier.STATIC))
                    	pw.print( "jclass self" );
                    else
                    	pw.print( "jobject self" );

                    //for (TypeMirror arg: args) {
                    for ( int a = 0; a < args.size(); a ++ )
                        pw.print(", " + jniType(args.get(a)) + " " + paramargs.get(a).getSimpleName() );

                    pw.println(") {");
                    
                    pw.println( "\tthread_env = env;" );
                    
                    pw.print( "\t" );
                    
                    // implementation of outgoing calls (Java -> C)
                    if ( mtr.getKind() != TypeKind.VOID )
                    	pw.print( staticjniType( mtr ) + " rval = " );
                    
                	// prepare/transform arg for C
                	pw.print( getCImplName( md, clazz, longName ) + "( " );
                    
                	// self type
                	if (md.getModifiers().contains(Modifier.STATIC))
                		pw.print("self");
                	else
                		pw.print( castToStaticjni(clazz.asType(), "self") );
                    
                	// args
                	for ( int a = 0; a < args.size(); a ++ )
                		pw.print( ", " + castToStaticjni( args.get(a), paramargs.get(a).getSimpleName().toString() ) );
                	pw.println( " );" );

                    if ( mtr.getKind() != TypeKind.VOID )
                    	pw.println( "\treturn " + castFromStaticjni( mtr, "rval") + ";" );
                    
                    pw.println("}");
                }
            }
            
            /* Write callbacks */
            for ( ExecutableElement m: helper.callbacks ) {
            	
            	// TODO add conflict detection
            	
            	TypeMirror rtm = m.getReturnType();
            	
            	/* Signature */
            	// return
            	pw.print( staticjniType(rtm) + " " );
            	
            	// fun name
            	pw.print( getCName( m, clazz, false ) + "( " );
            	
            	// self
            	if (m.getModifiers().contains(Modifier.STATIC))
                	pw.print( "jclass self" );
                else
            		pw.print( staticjniType(clazz.asType()) + " self" );

            	// params (with var names)
                List<? extends VariableElement> paramargs = m.getParameters();
                for (VariableElement p: paramargs) {
                	TypeMirror arg = types.erasure(p.asType());
            		pw.print( ", " + staticjniType(arg) + " " + p.getSimpleName().toString() );
                }
            	pw.println( " ) {" );
            	
            	
            	/* Implementation of callback */

                
                if ( ! m.getModifiers().contains(Modifier.STATIC) ) {
                    pw.println( "\tjclass jclass = (*thread_env)->GetObjectClass( thread_env, " + castFromStaticjni(clazz.asType(), "self") + " );" );
                    pw.print( "\tif ( jclass == 0 ) {" );
                    pw.print( "\t\tfprintf( stderr, \"Cannot find class\\n\" );" );
                    pw.print( "\t}" );
                    
                    
                    String sig = signature(m);
                    TypeSignature newtypesig = new TypeSignature(elems);
                    
                    pw.println( "\tjmethodID jmeth = (*thread_env)->GetMethodID( thread_env, jclass, \"" + m.getSimpleName().toString() + "\", \"" + newtypesig.getTypeSignature(sig, rtm) + "\" );" );
                    pw.print( "\tif ( jmeth == 0 ) {" ); 
                    pw.print( "\t\tfprintf( stderr, \"Cannot find method: " + m.getSimpleName().toString() + "\\n\" );" );
                    pw.print( "\t}" );
                }

            	pw.print( "\t" );
                if (rtm.getKind() != TypeKind.VOID )
                	pw.print( jniType(rtm) + " rval = " );
                
                pw.print( "(*thread_env)->Call" + (m.getModifiers().contains(Modifier.STATIC)? "Static":"") );
                pw.print( getCallSuffixForReturn( rtm ) );
                pw.print( "( thread_env, " + castFromStaticjni( clazz.asType(), "self" ) + ", " );
                pw.print( "jmeth" );
                for (VariableElement p: paramargs) {
                	TypeMirror arg = types.erasure(p.asType());
            		pw.print( ", " + castFromStaticjni( arg, p.getSimpleName().toString() ) );
                }
            	pw.println( " );" );
            	
                if (rtm.getKind() != TypeKind.VOID )
                	pw.println( "\treturn " + castToStaticjni(rtm, "rval") + ";" );
            	
            	pw.println( " }" );
            }
            
            pw.println(cppGuardEnd());
        } catch (TypeSignature.SignatureException e) {
            util.error("jni.sigerror", e.getMessage());
        }
    }
    
    String getCallSuffixForReturn( TypeMirror t ) {
    	switch ( t.getKind() ) {
			case VOID:     return "VoidMethod";
			case BOOLEAN:  return "BooleanMethod";
			case BYTE:     return "ByteMethod";
			case CHAR:     return "CharMethod";
			case SHORT:    return "ShortMethod";
			case INT:      return "IntMethod";
			case LONG:     return "LongMethod";
			case FLOAT:    return "FloatMethod";
			case DOUBLE:   return "DoubleMethod";
			default: 		return "ObjectMethod";
    	}
    }
    
    void tryToRegisterNativeCall(TypeElement clazz, String name,
    		Set<ExecutableElement> callbacks,
    		Set<VariableElement> callbacks_to_field ) {
    	
		List<ExecutableElement> methods = ElementFilter.methodsIn( clazz.getEnclosedElements() );
		for (ExecutableElement m: methods) {
			if ( name.toString().equals( m.getSimpleName().toString() ) ) {
				callbacks.add(m);
				return;
			}
		}
			
		List<VariableElement> fields = ElementFilter.fieldsIn( clazz.getEnclosedElements() );
		for ( VariableElement f: fields ) {
			if ( f.getSimpleName().toString().equals( name ) ) {
				callbacks_to_field.add( f );
				return;
			}
		}
		
		util.error("err.staticjni.targetnotfound", name ); // TODO intl
    }
}
