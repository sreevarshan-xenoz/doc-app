package DoctorAppointmentSystem;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class CalendarView extends VBox {
    
    private YearMonth currentYearMonth;
    private GridPane calendarGrid;
    private Label monthYearLabel;
    private List<Appointment> appointments;
    private Consumer<LocalDate> onDateSelected;
    
    public CalendarView() {
        this.currentYearMonth = YearMonth.now();
        this.appointments = new ArrayList<>();
        setupCalendarView();
    }
    
    public void setOnDateSelected(Consumer<LocalDate> onDateSelected) {
        this.onDateSelected = onDateSelected;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = new ArrayList<>(appointments);
        updateCalendar();
    }
    
    private void setupCalendarView() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        // Month year navigation
        GridPane navigationPane = new GridPane();
        navigationPane.setHgap(10);
        navigationPane.setAlignment(Pos.CENTER);
        
        Label prevMonth = new Label("◀");
        prevMonth.setStyle("-fx-text-fill: #4285f4; -fx-cursor: hand;");
        prevMonth.setOnMouseClicked(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });
        
        monthYearLabel = new Label();
        monthYearLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        Label nextMonth = new Label("▶");
        nextMonth.setStyle("-fx-text-fill: #4285f4; -fx-cursor: hand;");
        nextMonth.setOnMouseClicked(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });
        
        navigationPane.add(prevMonth, 0, 0);
        navigationPane.add(monthYearLabel, 1, 0);
        navigationPane.add(nextMonth, 2, 0);
        
        this.getChildren().add(navigationPane);
        
        // Calendar grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setAlignment(Pos.CENTER);
        
        this.getChildren().add(calendarGrid);
        
        updateCalendar();
    }
    
    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        
        // Update month/year label
        String monthYear = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) 
                         + " " + currentYearMonth.getYear();
        monthYearLabel.setText(monthYear);
        
        // Add day of week headers
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMinWidth(60);
            dayLabel.setMinHeight(30);
            calendarGrid.add(dayLabel, i, 0);
        }
        
        // Get date information
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int firstDayOfMonth = currentYearMonth.atDay(1).getDayOfWeek().getValue() - 1; // 0-indexed (0=Monday)
        
        // Create calendar days
        for (int i = 0; i < daysInMonth; i++) {
            int day = i + 1;
            int column = (firstDayOfMonth + i) % 7;
            int row = ((firstDayOfMonth + i) / 7) + 1; // Add 1 to account for header row
            
            LocalDate date = currentYearMonth.atDay(day);
            
            StackPane dayPane = createDayPane(day, date);
            calendarGrid.add(dayPane, column, row);
        }
    }
    
    private StackPane createDayPane(int day, LocalDate date) {
        StackPane dayPane = new StackPane();
        dayPane.setMinSize(60, 60);
        
        Rectangle background = new Rectangle(58, 58);
        background.setFill(Color.TRANSPARENT);
        background.setStroke(Color.LIGHTGRAY);
        background.setArcWidth(5);
        background.setArcHeight(5);
        
        VBox content = new VBox(5);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(5));
        
        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.setFont(Font.font("System", 14));
        
        // Highlight current day
        if (date.equals(LocalDate.now())) {
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4285f4;");
            background.setStroke(Color.web("#4285f4"));
        }
        
        content.getChildren().add(dayLabel);
        
        // Add appointment indicators
        int appointmentsForDay = countAppointmentsForDay(date);
        if (appointmentsForDay > 0) {
            Rectangle indicator = new Rectangle(40, 10);
            indicator.setFill(Color.web("#4caf50"));
            indicator.setArcWidth(5);
            indicator.setArcHeight(5);
            
            Label countLabel = new Label(String.valueOf(appointmentsForDay));
            countLabel.setStyle("-fx-text-fill: white; -fx-font-size: 8;");
            
            StackPane indicatorPane = new StackPane(indicator, countLabel);
            content.getChildren().add(indicatorPane);
        }
        
        dayPane.getChildren().addAll(background, content);
        
        // Add click handler
        dayPane.setOnMouseClicked(e -> {
            if (onDateSelected != null) {
                onDateSelected.accept(date);
            }
        });
        
        // Add hover effect
        dayPane.setOnMouseEntered(e -> {
            background.setFill(Color.web("#f5f5f5"));
            dayPane.setStyle("-fx-cursor: hand;");
        });
        
        dayPane.setOnMouseExited(e -> {
            background.setFill(Color.TRANSPARENT);
        });
        
        return dayPane;
    }
    
    private int countAppointmentsForDay(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return (int) appointments.stream()
                .filter(a -> a.getDate().equals(dateStr))
                .count();
    }
    
    public void goToMonth(YearMonth yearMonth) {
        this.currentYearMonth = yearMonth;
        updateCalendar();
    }
    
    public YearMonth getCurrentYearMonth() {
        return currentYearMonth;
    }
} 