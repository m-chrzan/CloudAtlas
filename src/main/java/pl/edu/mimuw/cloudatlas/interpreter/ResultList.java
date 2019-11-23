package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueList;

class ResultList extends Result {
    private final List<Value> list;

    public ResultList(List<Value> list) {
        this.list = list;
    }

    @Override
    protected ResultList binaryOperationTyped(BinaryOperation operation, ResultSingle right) {
        System.out.println("in binoptyped for ResultList on ResultSingle " + right);
        List<Value> results = new ArrayList<Value>();

        for (Value value : list) {
            results.add(operation.perform(value, right.getValue()));
        }

        System.out.println("went through list" + results);

        return new ResultList(results);
    }

    protected Result binaryOperationTyped(BinaryOperation operation, ResultList right) {
        throw new UnsupportedOperationException("Binary operation not supported on two ResultLists");
    }

    protected Result binaryOperationTyped(BinaryOperation operation, ResultColumn right) {
        throw new UnsupportedOperationException("Binary operation not supported on ResultList and ResultColumn");
    }

    @Override
    public ResultList unaryOperation(UnaryOperation operation) {
        List<Value> results = new ArrayList<Value>();

        for (Value value : list) {
            results.add(operation.perform(value));
        }
        return new ResultList(results);
    }

    @Override
    protected Result callMe(BinaryOperation operation, Result left) {
        return left.binaryOperationTyped(operation, this);
    }

    @Override
    public Value getValue() {
        throw new UnsupportedOperationException("ResultList: Not a ResultSingle.");
    }

    @Override
    public ValueList getList() {
        return new ValueList(list, TypeCollection.computeElementType(list));
    }

    @Override
    public ValueList getColumn() {
        throw new UnsupportedOperationException("Not a ResultColumn.");
    }

    @Override
    public ResultSingle aggregationOperation(AggregationOperation operation) {
        return new ResultSingle(operation.perform(getList()));
    }

    @Override
    public Result transformOperation(TransformOperation operation) {
        return new ResultList(operation.perform(getList()));
    }

    @Override
    public Result filterNulls() {
        throw new UnsupportedOperationException("Operation filterNulls not supported yet.");
    }

    @Override
    public Result first(int size) {
        List<Value> subList = list.subList(0, Math.min(size, list.size()));
        return new ResultSingle(new ValueList(subList, TypeCollection.computeElementType(subList)));
    }

    @Override
    public Result last(int size) {
        List<Value> subList = list.subList(
                Math.max(0, list.size() - size),
                list.size()
        );
        return new ResultSingle(new ValueList(subList, TypeCollection.computeElementType(subList)));
    }

    @Override
    public Result random(int size) {
        return new ResultSingle(
            randomList(
                new ValueList(
                    list,
                    TypeCollection.computeElementType(list)
                ),
                size
            )
        );
    }

    @Override
    public ResultList convertTo(Type to) {
        List<Value> results = new ArrayList<Value>();

        for (Value value : list) {
            results.add(value.convertTo(to));
        }

        return new ResultList(results);
    }

    @Override
    public ResultSingle isNull() {
        return new ResultSingle(new ValueBoolean(true));
    }

    @Override
    public Type getType() {
        Type type = TypePrimitive.NULL;
        for (Value value : list) {
            if (value.getType() != TypePrimitive.NULL) {
                type = value.getType();
            }
        }

        return type;
    }
}
