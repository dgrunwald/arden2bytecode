// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.compiler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import arden.runtime.ArdenList;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.LibraryMetadata;
import arden.runtime.MaintenanceMetadata;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;

/**
 * Represents a compiled MedicalLogicModule with minimal Metadata (as loaded from a .class File)
 * 
 * Allows saving the compiled bytecode into a .class file by calling the
 * saveClassFile() method.
 * 
 * Allows loading of MedicalLogicModuleImplementation .class file by 
 * calling loadClassFile().
 * 
 * When createInstance() or run() is called, the compiled bytecode is loaded
 * using the InMemoryClassLoader for execution.
 * 
 * @author Daniel Grunwald, Hannes Flicka
 * 
 */
public final class RawCompiledMlm implements MedicalLogicModule {
	private byte[] data;
	private final String mlmname;
	private Constructor<? extends MedicalLogicModuleImplementation> ctor;

	RawCompiledMlm(byte[] data, String mlmname) {
		if (mlmname == null)
			throw new NullPointerException();
		this.data = data;
		this.mlmname = mlmname;
	}
	
	public RawCompiledMlm(File mlmfile, String mlmname) throws IOException {		
		this((byte[])null, mlmname);
		//System.err.println("mlm: " + mlmfile.getPath());
		//System.err.println("mlmname: " + mlmname);
		loadClassFile(mlmfile);
	}

	public void saveClassFile(OutputStream os) throws IOException {
		os.write(data);
	}
	
	public void loadClassFile(File file) throws IOException {
		loadClassFile(
				new BufferedInputStream(
						new FileInputStream(file)),
				(int)(file.length()));
	}
	
	public void loadClassFile(InputStream in, int len) throws IOException {
		data = new byte[len];
		in.read(data, 0, len);
	}

	@SuppressWarnings("unchecked")
	private synchronized Constructor<? extends MedicalLogicModuleImplementation> getConstructor() {
		if (ctor == null) {
			Class<? extends MedicalLogicModuleImplementation> clazz;
			try {
				ClassLoader classLoader = new InMemoryClassLoader(mlmname, data);
				clazz = (Class<? extends MedicalLogicModuleImplementation>) classLoader.loadClass(mlmname);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			// We know the class has an appropriate constructor because we
			// compiled it, so wrap all the checked exceptions that should never
			// occur.
			try {
				ctor = clazz.getConstructor(ExecutionContext.class, MedicalLogicModule.class, ArdenValue[].class);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		return ctor;
	}

	/** Creates an instance of the implementation class. */
	@Override
	public MedicalLogicModuleImplementation createInstance(ExecutionContext context, ArdenValue[] arguments)
			throws InvocationTargetException {
		if (context == null)
			throw new NullPointerException();

		if (arguments == null)
			arguments = ArdenList.EMPTY.values;

		try {
			return getConstructor().newInstance(context, this, arguments);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Executes the MLM.
	 * 
	 * @return Returns the value(s) provided by the "return" statement, or
	 *         (Java) null if no return statement was executed.
	 */
	@Override
	public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments) throws InvocationTargetException {
		MedicalLogicModuleImplementation impl = createInstance(context, arguments);
		try {
			if (impl.logic(context))
				return impl.action(context);
			else
				return null;
		} catch (Exception ex) {
			throw new InvocationTargetException(ex);
		}
	}

	@Override
	public MaintenanceMetadata getMaintenance() {
		return null;
	}

	@Override
	public LibraryMetadata getLibrary() {
		return null;
	}

	@Override
	public String getName() {
		return mlmname;
	}

	@Override
	public double getPriority() {
		return -1.0;
	}
}
