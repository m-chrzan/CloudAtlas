package pl.edu.mimuw.cloudatlas.model;

import java.io.ByteArrayInputStream;

import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.querysigner.QueryData;

/**
 * A class that holds a CloudAtlas query.
 */
public class ValueQuery extends Value {
    // Original source code
    private String code;
    // Parsed query
    private Program query;
    // Query signature
    private byte[] signature;
    // Query signing timestamp
    private long timestamp;
    // Query installation status
    private boolean installed;

    /**
     * Constructs a new <code>ValueQuery</code> object.
     *
     * @param query the code of the query
     */
//    public ValueQuery(String query) throws Exception {
//        this.code = query;
//        if (!query.isEmpty()) {
//            Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
//            this.query = (new parser(lex)).pProgram();
//        }
//        this.signature = null;
//        this.timestamp = System.currentTimeMillis();
//        this.installed = true;
//    }

//    public ValueQuery(String query, byte[] querySignature) throws Exception {
//        this.code = query;
//        if (!query.isEmpty()) {
//            Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
//            this.query = (new parser(lex)).pProgram();
//        }
//        this.signature = querySignature;
//        this.timestamp = System.currentTimeMillis();
//        this.installed = true;
//    }

    public ValueQuery(QueryData queryData) throws Exception {
        this.code = queryData.getCode();
        if (!queryData.getCode().isEmpty()) {
            Yylex lex = new Yylex(new ByteArrayInputStream(queryData.getCode().getBytes()));
            this.query = (new parser(lex)).pProgram();
        }
        this.signature = queryData.getSignature();
        this.timestamp = queryData.getTimestamp();
        this.installed = queryData.isInstalled();
    }

    public ValueQuery(String query, long timestamp) throws Exception {
        this.code = query;
        if (!query.isEmpty()) {
            Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
            this.query = (new parser(lex)).pProgram();
        }
        this.signature = null;
        this.timestamp = timestamp;
        this.installed = true;
    }

    private ValueQuery() {
        this.code = null;
        this.query = null;
        this.signature = null;
        this.timestamp = System.currentTimeMillis();
        this.installed = true;
    }

    public String getCode() { return code; }

    public Program getQuery() {
        return query;
    }

    public byte[] getSignature() { return signature; }

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isInstalled() { return installed; }

    public void setInstalled(boolean installed) { this.installed = installed; }

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
