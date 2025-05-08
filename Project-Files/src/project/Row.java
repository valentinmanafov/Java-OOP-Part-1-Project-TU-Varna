package project;

import java.util.ArrayList;
import java.util.List;

public class Row {
    private List<Object> values;

    public Row(List<Object> values) {
        this.values = new ArrayList<>(values);
    }

    public List<Object> getValues() {
        return new ArrayList<>(values);
    }

    public Object getValue(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }
        throw new IndexOutOfBoundsException("ERROR: Row index out of bounds: " + index);
    }

    public void setValue(int index, Object value) throws IndexOutOfBoundsException {
        if (index >= 0 && index < values.size()) {
            values.set(index, value);
        } else {
            throw new IndexOutOfBoundsException("ERROR: Row index out of bounds: " + index);
        }
    }

    public int size() {
        return values.size();
    }

    protected void addValue(Object value) {
        values.add(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            sb.append(value == null ? "NULL" : value.toString());
            if (i < values.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}