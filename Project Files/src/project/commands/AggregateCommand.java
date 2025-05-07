package project.commands;

import project.*;
import java.util.*;

public class AggregateCommand implements CommandHandler {

    private final Database database;

    public AggregateCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 5) {
                System.out.println("Usage: aggregate <tbl> <s_idx> <s_val> <t_idx> <op>\nOps: sum, product, maximum, minimum");
                return;
            }
            String tableName = args[0];
            String searchColNStr = args[1];
            String searchVal = args[2];
            String targetColNStr = args[3];
            String operation = args[4].trim().toLowerCase();
            Table table = database.getTable(tableName);
            int searchColIndex, targetColIndex;
            try {
                searchColIndex = Integer.parseInt(searchColNStr);
                targetColIndex = Integer.parseInt(targetColNStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid index.");
                return;
            }
            Column searchColumn = table.getColumn(searchColIndex);
            Column targetColumn = table.getColumn(targetColIndex);
            DataType targetType = targetColumn.getType();
            if (targetType != DataType.INTEGER && targetType != DataType.DOUBLE) {
                System.out.println("Target column must be numeric.");
                return;
            }
            List<Number> targetValues = new ArrayList<>();
            try {
                for (Row row : table.getRows()) {
                    if (TypeParser.looselyEquals(row.getValue(searchColIndex), searchVal, searchColumn.getType())) {
                        Object targetCellValue = row.getValue(targetColIndex);
                        if (targetCellValue instanceof Number) {
                            targetValues.add((Number) targetCellValue);
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("Internal error during aggregate filter", e);
            }
            if (targetValues.isEmpty()) {
                System.out.println("No matching rows with numeric target values.");
                return;
            }
            double sum = 0;
            double product = 1;
            Double max = null;
            Double min = null;
            boolean first = true;
            for (Number num : targetValues) {
                double val = num.doubleValue();
                sum += val;
                product *= val;
                if (first) {
                    max = val;
                    min = val;
                    first = false;
                } else {
                    if (val > max) max = val;
                    if (val < min) min = val;
                }
            }
            Optional<Double> result = Optional.empty();
            switch (operation) {
                case "sum": result = Optional.of(sum); break;
                case "product": result = Optional.of(product); break;
                case "maximum": result = (max != null) ? Optional.of(max) : Optional.empty(); break;
                case "minimum": result = (min != null) ? Optional.of(min) : Optional.empty(); break;
                default: System.out.println("Unknown operation: " + args[4]); return;
            }
            if (result.isPresent()) {
                double resValue = result.get();
                if (targetType == DataType.INTEGER && resValue == Math.floor(resValue) && !Double.isInfinite(resValue)) {
                    System.out.println("Result (" + operation + "): " + (int)resValue);
                } else {
                    System.out.println("Result (" + operation + "): " + resValue);
                }
            } else {
                System.out.println("Could not compute result.");
            }
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}