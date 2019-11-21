package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueList;

class ResultColumn extends Result {
    private final List<Value> column;

    public ResultColumn(List<Value> column) {
        this.column = column;
    }

    @Override
    protected ResultColumn binaryOperationTyped(BinaryOperation operation, ResultSingle right) {
        List<Value> results = new ArrayList<Value>();

        for (Value value : column) {
            results.add(operation.perform(value, right.getValue()));
        }

        return new ResultColumn(results);
    }

    protected ResultColumn binaryOperationTyped(BinaryOperation operation, ResultColumn right) {
        List<Value> results = new ArrayList<Value>();

        for (int i = 0; i < column.size(); i++) {
            results.add(operation.perform(column.get(i), right.column.get(i)));
        }

        return new ResultColumn(results);
    }

    @Override
    public ResultColumn unaryOperation(UnaryOperation operation) {
        List<Value> results = new ArrayList<Value>();

        for (Value value : column) {
            results.add(operation.perform(value));
        }
        return new ResultColumn(results);
    }

    @Override
    protected Result callMe(BinaryOperation operation, Result left) {
        return left.binaryOperationTyped(operation, this);
    }

    @Override
    public Value getValue() {
        throw new UnsupportedOperationException("Not a ResultSingle.");
    }

    @Override
    public ValueList getList() {
        throw new UnsupportedOperationException("Not a ResultList.");
    }

    @Override
    public ValueList getColumn() {
        return new ValueList(column, TypeCollection.computeElementType(column));
    }

    @Override
    public ResultSingle aggregationOperation(AggregationOperation operation) {
        return new ResultSingle(operation.perform(getColumn()));
    }

    @Override
    public Result transformOperation(TransformOperation operation) {
        // TODO: this should be a ResultList
        return new ResultColumn(operation.perform(getColumn()));
    }

    @Override
    public Result filterNulls() {
        throw new UnsupportedOperationException("Operation filterNulls not supported yet.");
    }

    @Override
    public Result first(int size) {
        List<Value> subList = column.subList(0, Math.min(size, column.size()));
        return new ResultSingle(new ValueList(subList, TypeCollection.computeElementType(subList)));
    }

    @Override
    public Result last(int size) {
        List<Value> subList = column.subList(
                Math.max(0, column.size() - size),
                column.size()
        );
        return new ResultSingle(new ValueList(subList, TypeCollection.computeElementType(subList)));
    }

    @Override
    public Result random(int size) {
        return new ResultSingle(
            randomList(
                new ValueList(
                    column,
                    TypeCollection.computeElementType(column)
                ),
                size
            )
        );
    }

    @Override
    public ResultColumn convertTo(Type to) {
        List<Value> results = new ArrayList<Value>();

        for (Value value : column) {
            results.add(value.convertTo(to));
        }

        return new ResultColumn(results);
    }

    @Override
    public ResultSingle isNull() {
        throw new UnsupportedOperationException("Operation isNull not supported yet.");
        // return new ResultSingle(new ValueBoolean(value.isNull()));
    }

    @Override
    public Type getType() {
        throw new UnsupportedOperationException("Operation getType not supported yet.");
        // return value.getType();
    }
}
