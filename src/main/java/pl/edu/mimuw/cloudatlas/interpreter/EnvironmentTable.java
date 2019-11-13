package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.model.ValueNull;

class EnvironmentTable extends Environment {
	private final Table table;

	public EnvironmentTable(Table table) {
		this.table = table;
	}

	public Result getIdent(String ident) {
        return new ResultColumn(table.getColumn(ident).getValue());
	}
}
