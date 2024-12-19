package com.teamb.textprocessingtoolteamproject.controller;

import com.teamb.textprocessingtoolteamproject.model.RegexObject;
import com.teamb.textprocessingtoolteamproject.util.RegexUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AppController {
    // Regex Module Components
    @FXML private TextField regexField;
    @FXML private TextArea textInputArea;
    @FXML private TextField replaceField;
    @FXML private TextArea resultOutput;
    @FXML private Button addReplacementToCollectionButton;

    @FXML private TextField keyField;
    @FXML private TextArea valueField;
    @FXML private ListView<String> dataList;
    @FXML
    private TextArea resultArea;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextFlow highlightedTextFlow;

    @FXML
    private TableColumn<RegexObject, String> titleColumn;
    @FXML
    private TableColumn<RegexObject, String> regexInputColumn;
    @FXML
    private TableColumn<RegexObject, String> regexPatternColumn;
    @FXML
    private TableColumn<RegexObject, String> replaceTextColumn;
    @FXML
    private TableColumn<RegexObject, Void> actionsColumn;
    @FXML
    private TableView<RegexObject> collectionsTableView;
    @FXML
    private Button addCollectionButton;

    @FXML
    private Label numMatches;


    private final DataManager dataManager = new DataManager();
    private boolean isUpdating = false;
    private int updatingKey = -1;


    @FXML
    private void initialize() {
        regexField.textProperty().addListener((observable, oldValue, newValue) -> {
            highlightMatches();
        });
        replaceField.textProperty().addListener((observable, oldValue, newValue) -> {
            highlightMatches();
        });

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        regexInputColumn.setCellValueFactory(new PropertyValueFactory<>("inputText"));
        regexPatternColumn.setCellValueFactory(new PropertyValueFactory<>("regexPattern"));
        replaceTextColumn.setCellValueFactory(new PropertyValueFactory<>("replaceText"));

        actionsColumn.setCellFactory(col -> new TableCell<RegexObject, Void>() {
            private final Button updateButton = new Button("Update");
            private final Button viewButton = new Button("View");
            private final Button deleteButton = new Button("Delete");

            {
                updateButton.setOnAction(e -> handleUpdateEntry(getTableRow().getItem()));
                viewButton.setOnAction(e -> handleViewEntry(getTableRow().getItem()));
                deleteButton.setOnAction(e -> handleDeleteEntry(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10, viewButton, updateButton, deleteButton);
                    setGraphic(hbox);
                }
            }
        });

        refreshCollections();
    }

    @FXML
    private void handleAddEntryToCollection() {
        String regexPattern = regexField.getText();
        String inputText = textInputArea.getText();
        String replaceText = replaceField.getText();

        if (isUpdating) {
            String title = dataManager.getCollection(updatingKey).getTitle();
            dataManager.deleteCollection(updatingKey);
            dataManager.addCollection(title, regexPattern, inputText, replaceText);

            isUpdating = false;
            updatingKey = -1;
            addCollectionButton.setText("Add to Collection");
            refreshCollections();

            return;
        }

        if (!regexPattern.isEmpty() && !inputText.isEmpty()) {
            Stage titleDialog = new Stage();

            titleDialog.setTitle("Add Title");
            titleDialog.initModality(Modality.APPLICATION_MODAL);

            VBox dialogVBox = new VBox(10);
            dialogVBox.setPadding(new Insets(10));

            Label instructionLabel = new Label("Add a title to easily identify this collection:");
            TextField titleField = new TextField();
            titleField.setPromptText("Enter title...");

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            Button saveButton = new Button("Save");
            Button skipButton = new Button("No, I won't add a title");

            buttonBox.getChildren().addAll(skipButton, saveButton);

            dialogVBox.getChildren().addAll(instructionLabel, titleField, buttonBox);

            Scene dialogScene = new Scene(dialogVBox, 400, 200);
            titleDialog.setScene(dialogScene);

            saveButton.setOnAction(e -> {
                String title = titleField.getText();
                if (title == null || title.isEmpty()) {
                    title = "Untitled Collection";
                }
                dataManager.addCollection(title, regexPattern, inputText, replaceText);
                refreshCollections();
                titleDialog.close();
            });

            skipButton.setOnAction(e -> {
                dataManager.addCollection("Untitled collection", regexPattern, inputText, replaceText);
                refreshCollections();
                titleDialog.close();
            });

            titleDialog.showAndWait();
            clearFields();
        }
    }

    @FXML
    private void refreshCollections() {
        collectionsTableView.getItems().clear();

        HashMap<Integer, RegexObject> entries = dataManager.getAllCollections();
        collectionsTableView.getItems().addAll(entries.values());
    }

    @FXML
    private void handleUpdateEntry(RegexObject entry) {
        if (entry != null) {
            regexField.setText(entry.getRegexPattern());
            textInputArea.setText(entry.getInputText());
            replaceField.setText(entry.getReplaceText());

            isUpdating = true;
            updatingKey = entry.getKey();
            addCollectionButton.setText("Update Collection");
            tabPane.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleViewEntry(RegexObject entry) {
        if (entry != null) {
            regexField.setText(entry.getRegexPattern());
            textInputArea.setText(entry.getInputText());
            replaceField.setText(entry.getReplaceText());

            tabPane.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleDeleteEntry(RegexObject entry) {
        if (entry != null) {
            dataManager.deleteCollection(entry.getKey());
            refreshCollections();
        }
    }

    @FXML
    public void viewCollections(){
        tabPane.getSelectionModel().select(1);
    }

    public void importFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );


        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {

                StringBuilder content = new StringBuilder();
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine()).append("\n");
                }
                scanner.close();


                textInputArea.setText(content.toString());
            } catch (IOException e) {
                showAlert("Error", "Failed to read the file: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }

    }

    @FXML
    public void saveToFile() {
        String regex = regexField.getText().trim();
        String text = textInputArea.getText().trim();
        String replacement = replaceField.getText().trim();
        String result = resultOutput.getText().trim();

        if (regex.isEmpty() || text.isEmpty() || replacement.isEmpty() || result.isEmpty()) {
            showAlert("Error", "All fields (Regex, Text, Replacement, Result) must be filled to save.", Alert.AlertType.ERROR);
            return;
        }

        int randomNum = new Random().nextInt(10000);
        String defaultFileName = "RegexDetails_"+regex + randomNum + ".txt";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Regex Details");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Regex Pattern: " + regex + "\n");
                writer.write("Input Text: " + text + "\n");
                writer.write("Replacement Text: " + replacement + "\n");
                writer.write("Result Text: " + result + "\n");
                showAlert("Success", "File saved successfully to: " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
                clearFields();
            } catch (IOException e) {
                showAlert("Error", "Failed to save the file: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }


    public void handleFindMatches(){
        String inputText = textInputArea.getText();
        String regexPattern = regexField.getText();

        if (inputText.isEmpty() || regexPattern.isEmpty()) {
            resultArea.setText("Please provide input text and a regex pattern.");
            return;
        }

        try {
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(inputText);

            StringBuilder matches = new StringBuilder();
            int matchCount = 0;

            while (matcher.find()) {
                matchCount++;
                matches.append(matcher.group())
                        .append(" (")
                        .append(matcher.start())
                        .append(", ")
                        .append(matcher.end() - 1)
                        .append(" )")
                        .append("\n");
            }

            numMatches.setText(String.valueOf(matchCount));
            highlightedTextFlow.getChildren().clear();
            Text listResult = new Text(matches.toString());
            highlightedTextFlow.getChildren().add(listResult);
        } catch (PatternSyntaxException e) {
            resultArea.setText("Invalid regex pattern: " + e.getMessage());
        }
    }


    private void highlightMatches() {
        String inputText = textInputArea.getText();
        String regexPattern = regexField.getText();
        String replacementText = replaceField.getText();

        highlightedTextFlow.getChildren().clear();
        if (!RegexUtils.isValidRegex(regexPattern)){
            Text invalidRegexText = new Text("Invalid regex pattern");
            highlightedTextFlow.getChildren().add(invalidRegexText);
            return;
        }

        if (inputText.isEmpty() || regexPattern.isEmpty()) {
            numMatches.setText("0");
            return;
        }

        try {
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(inputText);

            int lastMatchEnd = 0;
            int matchCount = 0;

            while (matcher.find()) {
                matchCount++;

                if (matcher.start() > lastMatchEnd) {
                    String beforeMatch = inputText.substring(lastMatchEnd, matcher.start());
                    Text beforeText = new Text(beforeMatch);
                    beforeText.setStyle("-fx-fill: black; -fx-font-size: 14px;");  // Normal text
                    highlightedTextFlow.getChildren().add(beforeText);
                }

                String matchedText = matcher.group();
                if (!replacementText.isEmpty()) {
                    matchedText = replacementText;
                }

                Text displayedText = new Text(matchedText);
                if (replacementText.isEmpty()) {
                    displayedText.setStyle("-fx-fill: red; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: yellow;");  // Highlighted text
                } else {
                    displayedText.setStyle("-fx-fill: green; -fx-font-weight: bold; -fx-font-size: 14px;");  // Replaced text
                }
                highlightedTextFlow.getChildren().add(displayedText);

                lastMatchEnd = matcher.end();
            }

            if (lastMatchEnd < inputText.length()) {
                String afterMatch = inputText.substring(lastMatchEnd);
                Text afterText = new Text(afterMatch);
                afterText.setStyle("-fx-fill: black; -fx-font-size: 14px;");
                highlightedTextFlow.getChildren().add(afterText);
            }

            numMatches.setText(String.valueOf(matchCount));
        } catch (PatternSyntaxException e) {
            numMatches.setText("0");
            highlightedTextFlow.getChildren().add(new Text(inputText));
        }
    }


    public void handleViewCollections(){

    }

    private void clearFields() {
        regexField.clear();
        textInputArea.clear();
        replaceField.clear();
        //resultOutput.clear();
        //addReplacementToCollectionButton.setDisable(true);
    }



    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
