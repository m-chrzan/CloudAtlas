package pl.edu.mimuw.cloudatlas.model;

import java.io.ByteArrayInputStream;

import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.model.Value;

/**
 * A class that holds a CloudAtlas query.
 */
public class ValueQuery extends Value {
    // Original source code
    private String code;
    // Parsed query
    private Program query;
    /**
     * Constructs a new <code>ValueQuery</code> object.
     *
     * @param name the name of the query
     * @param query the code of the query
     */
    public ValueQuery(String query) throws Exception {
        this.code = query;
        Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
        this.query = (new parser(lex)).pProgram();
    }

    private ValueQuery() {
        this.code = null;
        this.query = null;
    }

    public String getCode() { return code; }

    public Program getQuery() {
        return query;
    }

    @Override
    public Type getType() {
        return TypePrimitive.QUERY;
    }

    @Override
    public boolean isNull() {
        return query == null || code == null;
    }

    public Value isEqual(Value value) {
        sameTypesOrThrow(value, Operation.EQUAL);
        if(isNull() && value.isNull())
            return new ValueBoolean(true);
        else if(isNull() || value.isNull())
            return new ValueBoolean(false);
        return new ValueBoolean(code.equals(((ValueQuery)value).code));
    }

    @Override
    public Value getDefaultValue() {
        return new ValueQuery();
    }

    @Override
    public Value convertTo(Type type) {
        switch(type.getPrimaryType()) {
            case QUERY:
                return this;
            case STRING:
                return isNull() ? ValueString.NULL_STRING : new ValueString(code);
            default:
                throw new UnsupportedConversionException(getType(), type);
        }
    }
}
