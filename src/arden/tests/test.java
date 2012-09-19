package arden.tests;

import java.lang.reflect.Field;

import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModuleImplementation;

public class test {
	public static class Mlm extends MedicalLogicModuleImplementation {

		private ArdenValue p1 = new ArdenString("abc");
		private ArdenString p2 = new ArdenString("def");
		
		public Mlm(ExecutionContext context, ArdenValue[] args) {
			
		}
		
		@Override
		public boolean logic(ExecutionContext context) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public ArdenValue[] action(ExecutionContext context) {
			// TODO Auto-generated method stub
			return null;
		}
		
		public ArdenValue getValue(String id) {
			Field field;
			try {
				field = this.getClass().getDeclaredField(id);
				return (ArdenValue) field.get(this);
			} catch (SecurityException e) {
			} catch (NoSuchFieldException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
			return null;
		}
	}
	
	public static void main(String args[]) {
			System.out.println(new Mlm(null, null).getValue("p1"));
	}
}
