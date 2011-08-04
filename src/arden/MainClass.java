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

package arden;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class MainClass {

	public static void main(String[] args) {
		System.out.println("arden2bytecode Interpreter example");
		System.out.println("Copyright 2010 Daniel Grunwald");
		System.out.println("");
		System.out.println("This program is free software; you can redistribute it and/or modify it");
		System.out.println("under the terms of the GNU General Public License.");
		System.out.println("");

		Compiler compiler = new Compiler();
		CompiledMlm mlm;
		try {
			compiler.enableDebugging(args[0]);
			mlm = compiler.compileMlm(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			System.err.println("error: File " + args[0] + " was not found.");
			return;
		} catch (CompilerException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("error: No MLM source file given.");
			System.err.println("usage: java <arden2bytecode program> <MLM-file>");
			System.err.println("");
			System.err.println("e.g.: java -jar arden2bytecode.jar <MLM-file>");
			System.err.println("or: java arden.MainClass <MLM-file>");
			return;
		}
		ExecutionContext context = new ExecutionContext() {
			@Override
			public void write(ArdenValue message, String destination) {
				System.out.println(message.toString());
			}
		};
		try {
			ArdenValue[] result = mlm.run(context, null);
			if (result != null && result.length == 1) {
				System.out.println("Return Value: " + result[0].toString());
			} else {
				System.out.println("There was no return value or result length was not equal to 1.");
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
