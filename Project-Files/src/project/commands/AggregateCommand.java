package project.commands;

import project.*;

import java.util.*;

/**
 * Command handler for performing aggregate operations (sum, product, min, max)
 * on a numeric column for rows matching a search criterion.
 */
public class AggregateCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs an AggregateCommand.
     * @param database The database instance to operate on.
     */
    public AggregateCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the aggregate command.
     * Performs an aggregation (sum, product, min, max) on a target numeric column
     * for rows that match a specific value in a search column.
     * Usage: aggregate &lt;table&gt; &lt;search column index&gt; &lt;search value&gt; &lt;target column index&gt; &lt;operation&gt;
     * Valid operations: sum, product, maximum, minimum.
     * Target column must be of type INTEGER or DOUBLE.
     * @param args Command arguments: table name, search column index, search value, target column index, operation.
     */
    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 5) {
                System.out.println("Usage: aggregate <table> <search column index> <search value> <target column index> <operation>\nThe valid operations are: sum, product, maximum, minimum");
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
                System.out.println("ERROR: Invalid index. Please provide numeric indices for columns.");
                return;
            }

            Column searchColumn = table.getColumn(searchColIndex); // Might throw if index out of bounds
            Column targetColumn = table.getColumn(targetColIndex); // Might throw if index out of bounds
            DataType targetType = targetColumn.getType();

            if (targetType != DataType.INTEGER && targetType != DataType.DOUBLE) {
                System.out.println("WARNING: Target column '" + targetColumn.getName() + "' must be numeric (INTEGER or DOUBLE) for aggregation.");
                return;
            }

            List<Number> targetValues = new ArrayList<>();
            try {
                // Filter rows and collect target numeric values.
                // Iterate through each row in the table.
                for (Row row : table.getRows()) {
                    if (TypeParser.looselyEquals(row.getValue(searchColIndex), searchVal, searchColumn.getType())) {
                        Object targetCellValue = row.getValue(targetColIndex);
                        if (targetCellValue instanceof Number) {
                            targetValues.add((Number) targetCellValue);
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("ERROR: During aggregate filter - column index out of bounds.", e);
            }

            if (targetValues.isEmpty()) {
                System.out.println("WARNING: No matching rows with numeric target values (INTEGER or DOUBLE) found for aggregation.");
                return;
            }

            // Perform the specified aggregation.
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
                case "sum":
                    result = Optional.of(sum);
                    break;
                case "product":
                    result = Optional.of(product);
                    break;
                case "maximum":
                    result = (max != null) ? Optional.of(max) : Optional.empty();
                    break;
                case "minimum":
                    result = (min != null) ? Optional.of(min) : Optional.empty();
                    break;
                default:
                    System.out.println("WARNING: Unknown operation: '" + args[4] + "'. Valid operations are: sum, product, maximum, minimum.");
                    return;
            }

            if (result.isPresent()) {
                double resValue = result.get();
                if (targetType == DataType.INTEGER && resValue == Math.floor(resValue) && !Double.isInfinite(resValue)) {
                    System.out.println("Result (" + operation + "): " + (int) resValue);
                } else {
                    System.out.println("Result (" + operation + "): " + resValue);
                }
            } else {
                System.out.println("ERROR: Could not compute result for operation '" + operation + "'.");
            }
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}