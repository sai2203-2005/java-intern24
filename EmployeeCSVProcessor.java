import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class EmployeeCSVProcessor {

    static class Employee {
        String id, name, department, joiningDate;
        int age;
        double salary;

        Employee(String[] data) {
            id = data[0].trim();
            name = data[1].trim();
            age = Integer.parseInt(data[2].trim());
            department = data[3].trim();
            salary = Double.parseDouble(data[4].trim());
            joiningDate = data[5].trim();
        }
    }

    public static void main(String[] args) {
        String inputFile = "employees.csv";

        Set<String> employeeIds = new HashSet<>();
        Map<String, Integer> departmentCount = new TreeMap<>();
        Map<String, Double> departmentSalary = new TreeMap<>();

        int total = 0, valid = 0, invalid = 0;
        double totalSalary = 0;
        double minSalary = Double.MAX_VALUE;
        double maxSalary = Double.MIN_VALUE;

        try (
            BufferedReader br = Files.newBufferedReader(
                Paths.get(inputFile));
            BufferedWriter processed = Files.newBufferedWriter(
                Paths.get("processed_employees.csv"));
            BufferedWriter rejected = Files.newBufferedWriter(
                Paths.get("invalid_employees.csv"))
        ) {
            String header = br.readLine();

            if (header == null) {
                System.out.println("CSV file is empty!");
                return;
            }

            String[] expectedHeaders = {
                "EmployeeID", "Name", "Age",
                "Department", "Salary", "JoiningDate"
            };

            if (!Arrays.equals(
                    Arrays.stream(header.split(",", -1))
                        .map(String::trim)
                        .toArray(String[]::new),
                    expectedHeaders)) {
                System.out.println("Invalid CSV header!");
                return;
            }

            processed.write(header);
            processed.newLine();
            rejected.write(header + ",ErrorReason");
            rejected.newLine();

            String line;

            while ((line = br.readLine()) != null) {
                total++;

                String[] data = line.split(",", -1);
                List<String> errors = new ArrayList<>();

                if (data.length != 6) {
                    errors.add("Expected 6 columns");
                } else {
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i].trim();
                    }

                    // Required fields
                    if (data[0].isEmpty())
                        errors.add("Missing employee ID");

                    if (data[1].isEmpty())
                        errors.add("Missing name");

                    if (data[3].isEmpty())
                        errors.add("Missing department");

                    if (data[5].isEmpty())
                        errors.add("Missing joining date");

                    // Duplicate employee ID
                    if (!data[0].isEmpty()
                            && employeeIds.contains(data[0])) {
                        errors.add("Duplicate employee ID");
                    }

                    // Validate age
                    int age = -1;
                    try {
                        age = Integer.parseInt(data[2]);
                        if (age < 18 || age > 60)
                            errors.add("Age must be 18-60");
                    } catch (NumberFormatException e) {
                        errors.add("Invalid age");
                    }

                    // Validate salary
                    double salary = -1;
                    try {
                        salary = Double.parseDouble(data[4]);
                        if (!Double.isFinite(salary) || salary < 0)
                            errors.add("Invalid salary");
                    } catch (NumberFormatException e) {
                        errors.add("Invalid salary");
                    }

                    // Validate joining date
                    try {
                        LocalDate.parse(data[5]);
                    } catch (DateTimeParseException e) {
                        errors.add("Invalid joining date");
                    }

                    // Process valid records
                    if (errors.isEmpty()) {
                        Employee emp = new Employee(data);

                        processed.write(line);
                        processed.newLine();

                        employeeIds.add(emp.id);
                        valid++;

                        totalSalary += emp.salary;
                        minSalary = Math.min(minSalary, emp.salary);
                        maxSalary = Math.max(maxSalary, emp.salary);

                        departmentCount.put(
                            emp.department,
                            departmentCount.getOrDefault(
                                emp.department, 0) + 1);

                        departmentSalary.put(
                            emp.department,
                            departmentSalary.getOrDefault(
                                emp.department, 0.0) + emp.salary);
                    } else {
                        rejected.write(line + ",\"" +
                            String.join("; ", errors) + "\"");
                        rejected.newLine();
                        invalid++;
                    }
                }

                if (data.length != 6) {
                    rejected.write(line +
                        ",\"Expected 6 columns\"");
                    rejected.newLine();
                    invalid++;
                }
            }

            // Generate statistics file
            try (BufferedWriter stats = Files.newBufferedWriter(
                    Paths.get("employee_statistics.csv"))) {

                stats.write("Metric,Value");
                stats.newLine();
                stats.write("Total Records," + total);
                stats.newLine();
                stats.write("Valid Records," + valid);
                stats.newLine();
                stats.write("Invalid Records," + invalid);
                stats.newLine();

                stats.write("Average Salary," +
                    (valid > 0
                        ? String.format(Locale.US, "%.2f",
                            totalSalary / valid) : "N/A"));
                stats.newLine();

                stats.write("Minimum Salary," +
                    (valid > 0 ? minSalary : "N/A"));
                stats.newLine();

                stats.write("Maximum Salary," +
                    (valid > 0 ? maxSalary : "N/A"));
                stats.newLine();

                stats.newLine();
                stats.write("Department,EmployeeCount,AverageSalary");
                stats.newLine();

                for (String dept : departmentCount.keySet()) {
                    int count = departmentCount.get(dept);
                    double avg = departmentSalary.get(dept) / count;

                    stats.write(dept + "," + count + "," +
                        String.format(Locale.US, "%.2f", avg));
                    stats.newLine();
                }
            }

            System.out.println("Processing completed!");
            System.out.println("Total records: " + total);
            System.out.println("Valid records: " + valid);
            System.out.println("Invalid records: " + invalid);
            System.out.println("Average salary: " +
                (valid > 0
                    ? String.format(Locale.US, "%.2f",
                        totalSalary / valid) : "N/A"));

            System.out.println("Generated files:");
            System.out.println("1. processed_employees.csv");
            System.out.println("2. invalid_employees.csv");
            System.out.println("3. employee_statistics.csv");

        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }
}
