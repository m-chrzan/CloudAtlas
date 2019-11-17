package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.model.ValueNull;

class EnvironmentRow extends Environment {
    private final TableRow row;
    private final Map<String, Integer> columns = new HashMap<String, Integer>();

    public EnvironmentRow(TableRow row, List<String> columns) {
        this.row = row;
        int i = 0;
        for(String c : columns)
            this.columns.put(c, i++);
    }

    public Result getIdent(String ident) {
        try {
            return new ResultSingle(row.getIth(columns.get(ident)));
        } catch(NullPointerException exception) {
            return new ResultSingle(ValueNull.getInstance());
        }
    }
}

