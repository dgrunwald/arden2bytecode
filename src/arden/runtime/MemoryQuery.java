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
			result[i] = ExpressionHelpers.where(values[i], BinaryOperator.BEFORE.run(inputTime, time));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursNotBefore(ArdenTime time) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], UnaryOperator.NOT.run(BinaryOperator.BEFORE.run(inputTime,
					time)));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursAfter(ArdenTime time) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], BinaryOperator.AFTER.run(inputTime, time));
		}
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery occursNotAfter(ArdenTime time) {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++) {
			ArdenValue inputTime = UnaryOperator.TIME.run(values[i]);
			result[i] = ExpressionHelpers.where(values[i], UnaryOperator.NOT.run(BinaryOperator.AFTER.run(inputTime,
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
	public DatabaseQuery minimum() {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery minimum(int numberOfElements) {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery maximum() {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery maximum(int numberOfElements) {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery last() {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery last(int numberOfElements) {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery first() {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery first(int numberOfElements) {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery latest() {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery latest(int numberOfElements) {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery earliest() {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery earliest(int numberOfElements) {
		throw new RuntimeException("not implemented"); // TODO
	}
}
