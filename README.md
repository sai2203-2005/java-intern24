# Employee CSV Processor & Validator

A standalone Java application that reads employee records from a CSV file, validates each entry against business rules, computes key summary and departmental statistics, and exports the results into separate, structured CSV reports.

Built using standard Java SE libraries (`java.nio.file`, `java.time`, and Java Collections) without external third-party dependencies.

---

## 📋 Features

- **Standard Java Implementation**: Runs with native Java SE classes (no Maven/Gradle or third-party packages required).
- **Comprehensive Data Validation**:
  - Validates CSV header structure.
  - Ensures required fields (`EmployeeID`, `Name`, `Department`, `JoiningDate`) are present and non-empty.
  - Checks for unique `EmployeeID` across valid entries.
  - Validates numerical ranges: Age must be between `18` and `60`.
  - Validates salary amounts: Must be positive, finite numbers.
  - Validates ISO-8601 date formatting: Must match `yyyy-MM-dd`.
- **Error Tracking**: Logs rejected records with exact error messages explaining why validation failed.
- **Statistical Aggregation**:
  - Calculates total records, valid count, and invalid count.
  - Calculates overall average, minimum, and maximum salary for valid employees.
  - Aggregates employee headcount and average salary by department using sorted `TreeMap` collections.
- **Multi-File Output**: Generates valid records, rejection logs, and statistics in distinct CSV files.

---

## 📁 Project Structure

```text
.
├── employees.csv              # Input CSV data
├── EmployeeCSVProcessor.java  # Main processor source code
├── processed_employees.csv    # Output: Validated employee records
├── invalid_employees.csv      # Output: Rejected records with error reasons
├── employee_statistics.csv    # Output: Summary and departmental metrics
└── README.md                  # Project documentation
```

---

## 📥 Input Data Format (`employees.csv`)

The input file must have the following comma-separated header and column structure:

```csv
EmployeeID,Name,Age,Department,Salary,JoiningDate
101,Ravi,25,IT,45000,2023-01-15
102,Sita,30,HR,50000,2022-06-10
103,Arun,28,IT,55000,2021-03-20
104,Priya,22,Finance,40000,2024-02-01
102,Kiran,35,HR,60000,2020-05-12
105,Rahul,-5,IT,-1000,2023-11-11
106,Anu,27,Finance,48000,2022-09-15
```

### Validation Rules

| Field | Validation Rule |
| :--- | :--- |
| **Header** | Must match exact schema: `EmployeeID,Name,Age,Department,Salary,JoiningDate` |
| **EmployeeID** | Required, non-empty, and unique across accepted records |
| **Name** | Required, non-empty |
| **Age** | Integer between `18` and `60` (inclusive) |
| **Department** | Required, non-empty |
| **Salary** | Finite numeric value $\ge 0$ |
| **JoiningDate** | Valid ISO date adhering to `yyyy-MM-dd` |

---

## 📤 Output Files

When processed, the program produces three files in the working directory:

### 1. `processed_employees.csv`
Contains all valid employee records:
```csv
EmployeeID,Name,Age,Department,Salary,JoiningDate
101,Ravi,25,IT,45000,2023-01-15
102,Sita,30,HR,50000,2022-06-10
103,Arun,28,IT,55000,2021-03-20
104,Priya,22,Finance,40000,2024-02-01
106,Anu,27,Finance,48000,2022-09-15
```

### 2. `invalid_employees.csv`
Contains rejected records along with the reasons for rejection:
```csv
EmployeeID,Name,Age,Department,Salary,JoiningDate,ErrorReason
102,Kiran,35,HR,60000,2020-05-12,"Duplicate employee ID"
105,Rahul,-5,IT,-1000,2023-11-11,"Age must be 18-60; Invalid salary"
```

### 3. `employee_statistics.csv`
Summary statistics and departmental breakdown:
```csv
Metric,Value
Total Records,7
Valid Records,5
Invalid Records,2
Average Salary,47600.00
Minimum Salary,40000.0
Maximum Salary,55000.0

Department,EmployeeCount,AverageSalary
Finance,2,44000.00
HR,1,50000.00
IT,2,50000.00
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher (tested with JDK 23).

Verify your Java installation:
```bash
javac -version
java -version
```

### Compilation & Execution

1. Clone or open the project folder in your terminal:
   ```bash
   cd "path/to/java intership"
   ```

2. Compile the Java file:
   ```bash
   javac EmployeeCSVProcessor.java
   ```

3. Run the application:
   ```bash
   java EmployeeCSVProcessor
   ```

### Terminal Output
Upon successful execution, you will see:
```text
Processing completed!
Total records: 7
Valid records: 5
Invalid records: 2
Average salary: 47600.00
Generated files:
1. processed_employees.csv
2. invalid_employees.csv
3. employee_statistics.csv
```

---

## 💡 Notes & Production Considerations

- **CSV Quoting**: This lightweight parser splits lines using basic comma delimiters (`line.split(",", -1)`). For production environments with quoted fields containing embedded commas or multiline text, consider using a dedicated CSV library like [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/) or [OpenCSV](https://opencsv.sourceforge.net/).
- **Memory Efficiency**: Streams the input file line-by-line using `BufferedReader`, ensuring low memory footprint even with large datasets.
