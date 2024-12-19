package com.teamb.textprocessingtoolteamproject.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

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

    }

    public void handleAddEntryToCollection(){

    }

    public void handleViewCollections(){

    }

    private void clearFields() {
        regexField.clear();
        textInputArea.clear();
        replaceField.clear();
        resultOutput.clear();
        addReplacementToCollectionButton.setDisable(true);
    }



    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
