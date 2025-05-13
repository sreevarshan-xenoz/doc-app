package DoctorAppointmentSystem;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardView extends VBox {
    
    private List<Appointment> appointments;
    
    public AdminDashboardView() {
        this.setSpacing(20);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        // Add title
        Label titleLabel = new Label("Admin Dashboard");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        this.getChildren().add(titleLabel);
        
        // Initialize with empty data
        updateDashboard(null);
    }
    
    public void updateDashboard(List<Appointment> appointments) {
        this.appointments = appointments;
        
        // Clear existing content (except title)
        if (this.getChildren().size() > 1) {
            this.getChildren().remove(1, this.getChildren().size());
        }
        
        // If no appointments, show placeholder
        if (appointments == null || appointments.isEmpty()) {
            Label placeholder = new Label("No appointment data available");
            placeholder.setStyle("-fx-text-fill: #757575;");
            this.getChildren().add(placeholder);
            return;
        }
        
        // Add statistics cards
        this.getChildren().add(createStatisticsCards());
        
        // Add monthly appointments chart
        this.getChildren().add(createMonthlyAppointmentsChart());
    }
    
    private HBox createStatisticsCards() {
        HBox cards = new HBox(15);
        cards.setAlignment(Pos.CENTER);
        
        // Total appointments
        VBox totalCard = createStatCard("Total Appointments", String.valueOf(appointments.size()), "#4285f4");
        
        // Appointments this month
        YearMonth currentMonth = YearMonth.now();
        long appointmentsThisMonth = appointments.stream()
                .filter(a -> {
                    LocalDate date = LocalDate.parse(a.getDate());
                    YearMonth appointmentMonth = YearMonth.from(date);
                    return appointmentMonth.equals(currentMonth);
                })
                .count();
        VBox monthlyCard = createStatCard("This Month", String.valueOf(appointmentsThisMonth), "#34a853");
        
        // Appointments today
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        long appointmentsToday = appointments.stream()
                .filter(a -> a.getDate().equals(today))
                .count();
        VBox todayCard = createStatCard("Today", String.valueOf(appointmentsToday), "#ea4335");
        
        // Unique patients
        long uniquePatients = appointments.stream()
                .map(Appointment::getPatientName)
                .distinct()
                .count();
        VBox patientsCard = createStatCard("Unique Patients", String.valueOf(uniquePatients), "#fbbc05");
        
        cards.getChildren().addAll(totalCard, monthlyCard, todayCard, patientsCard);
        return cards;
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setMinWidth(150);
        card.setMinHeight(100);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-background-radius: 5;");
        
        Rectangle colorIndicator = new Rectangle(50, 5);
        colorIndicator.setFill(Color.web(color));
        colorIndicator.setArcWidth(5);
        colorIndicator.setArcHeight(5);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #757575;");
        
        card.getChildren().addAll(colorIndicator, valueLabel, titleLabel);
        return card;
    }
    
    private VBox createMonthlyAppointmentsChart() {
        VBox chartContainer = new VBox(10);
        chartContainer.setPadding(new Insets(15, 0, 0, 0));
        
        Label chartTitle = new Label("Appointments by Month");
        chartTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Appointments");
        
        // Create the chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Monthly Appointments");
        barChart.setLegendVisible(false);
        
        // Count appointments by month
        Map<YearMonth, Integer> appointmentsByMonth = new HashMap<>();
        
        // Get data for the last 6 months
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            appointmentsByMonth.put(month, 0);
        }
        
        // Count appointments
        for (Appointment appointment : appointments) {
            try {
                LocalDate date = LocalDate.parse(appointment.getDate());
                YearMonth month = YearMonth.from(date);
                
                if (appointmentsByMonth.containsKey(month)) {
                    appointmentsByMonth.put(month, appointmentsByMonth.get(month) + 1);
                }
            } catch (Exception e) {
                // Skip invalid dates
            }
        }
        
        // Create dataset
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Add data points in chronological order
        appointmentsByMonth.keySet().stream()
                .sorted()
                .forEach(month -> {
                    String monthLabel = month.getMonth().toString() + " " + month.getYear();
                    series.getData().add(new XYChart.Data<>(monthLabel, appointmentsByMonth.get(month)));
                });
        
        barChart.getData().add(series);
        
        // Style the bars
        series.getData().forEach(data -> {
            data.getNode().setStyle("-fx-bar-fill: #4285f4;");
        });
        
        chartContainer.getChildren().addAll(chartTitle, barChart);
        return chartContainer;
    }
} 