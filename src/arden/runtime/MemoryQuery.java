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

package arden.runtime;

/**
 * Implementation of DatabaseQuery that works in memory using ArdenValues.
 * 
 * @author Daniel Grunwald
 * 
 */
public final class MemoryQuery extends DatabaseQuery {
	private final ArdenValue[] values;

	public MemoryQuery(ArdenValue[] values) {
		if (values == null)
			throw new NullPointerException();
		this.values = values;
	}

	@Override
	public ArdenValue[] execute() {
		return values;
	}

	@Override
	public DatabaseQuery occursWithinTo(ArdenTime start, ArdenTime end) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], TernaryOperator.WITHINTO.run(inputTime, start, end));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursNotWithinTo(ArdenTime start, ArdenTime end) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], UnaryOperator.NOT.run(TernaryOperator.WITHINTO.run(
					inputTime, start, end)));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursBefore(ArdenTime time) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], BinaryOperator.ISBEFORE.run(inputTime, time));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursNotBefore(ArdenTime time) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], UnaryOperator.NOT.run(BinaryOperator.ISBEFORE.run(inputTime,
					time)));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursAfter(ArdenTime time) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], BinaryOperator.ISAFTER.run(inputTime, time));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursNotAfter(ArdenTime time) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], UnaryOperator.NOT.run(BinaryOperator.ISAFTER.run(inputTime,
					time)));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursAt(ArdenTime time) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], BinaryOperator.EQ.run(inputTime, time));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursNotAt(ArdenTime time) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], BinaryOperator.NE.run(inputTime, time));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery average() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.average(values[i]);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery count() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.count(values[i]);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery exist() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.exist(values[i]);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery sum() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.sum(values[i]);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery median() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.median(values[i]);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery minimum() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.elementAt(values[i], ExpressionHelpers.indexMinimum(values[i]));
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery minimum(int numberOfElements) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.elementAt(values[i], ExpressionHelpers.indexMinimum(values[i], numberOfElements));
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery maximum() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.elementAt(values[i], ExpressionHelpers.indexMaximum(values[i]));
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery maximum(int numberOfElements) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.elementAt(values[i], ExpressionHelpers.indexMaximum(values[i], numberOfElements));
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery last() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.last(values[i]);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery last(int numberOfElements) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.last(values[i], numberOfElements);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery first() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.first(values[i]);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery first(int numberOfElements) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.first(values[i], numberOfElements);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery latest() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.elementAt(values[i], ExpressionHelpers.indexLatest(values[i]));
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery latest(int numberOfElements) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.elementAt(values[i], ExpressionHelpers.indexLatest(values[i], numberOfElements));
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery earliest() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.elementAt(values[i], ExpressionHelpers.indexEarliest(values[i]));
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery earliest(int numberOfElements) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.elementAt(values[i], ExpressionHelpers.indexEarliest(values[i], numberOfElements));
		return new MemoryQuery(result);
	}
}
