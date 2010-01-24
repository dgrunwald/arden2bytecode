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
			result[i] = ExpressionHelpers.minimum(values[i]);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery minimum(int numberOfElements) {
		throw new RuntimeException("not implemented"); // TODO
	}

	@Override
	public DatabaseQuery maximum() {
		ArdenValue[] result = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = ExpressionHelpers.maximum(values[i]);
		return new MemoryQuery(result);
	}

	@Override
	public DatabaseQuery maximum(int numberOfElements) {
		throw new RuntimeException("not implemented"); // TODO
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
