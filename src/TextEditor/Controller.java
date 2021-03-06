package TextEditor;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class Controller implements Initializable {
    @FXML
    public MenuItem menu_item_exit;
    public TabPane tab_pane;
    public ImageView iv_image;
    public Button btn_clear;
    public Button btn_save;
    public Button btn_open;
    public Button btn_insert_img;
    public TextArea txtArea_main;
    public Label label_notif;
    public ChoiceBox<String> menu_font;
    public ChoiceBox<Double> menu_font_size;
    public MenuItem menu_item_wrap;
    public AnchorPane pane_atas;
    public Tab tab_image;

    public void setBtn_clear(ActionEvent event){
        txtArea_main.clear();
        label_notif.setVisible(true);
        label_notif.setText("Text deleted");
    }

    public void saveFile(ActionEvent event) {
        label_notif.setVisible(true);
        if (!txtArea_main.getText().isEmpty()){
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
            );
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            String fileToSave = fileChooser.showSaveDialog(null).getAbsolutePath();

            try {
                FileWriter myWriter = new FileWriter(fileToSave);
                myWriter.write(txtArea_main.getText());
                myWriter.close();
                System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                label_notif.setText(e.toString());
            }
        } else label_notif.setText("Text is Empty.");
    }

    public void insertImgBtnAction(ActionEvent e){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.png"));

        File imageToLoad = fileChooser.showOpenDialog(null);
        if (imageToLoad != null){
            Image image = new Image(imageToLoad.toURI().toString());
            iv_image.setImage(image);
            label_notif.setVisible(true);
            label_notif.setText("Image loaded from : \n" + imageToLoad.getAbsolutePath());
            tab_pane.getSelectionModel().select(1);
            tab_image.setText(imageToLoad.getName());
        } else {
            label_notif.setText("Error while reading file");
        }
    }


    public void chooseFile(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
        );
        File fileToLoad = fileChooser.showOpenDialog(null);
        if(fileToLoad != null){
            loadFileToTextArea(fileToLoad);
        }
    }

    private Task<String> fileLoaderTask(File fileToLoad){
        Task<String> loadFileTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                BufferedReader reader = new BufferedReader(new FileReader(fileToLoad));

                long lineCount;
                try (Stream<String> stream = Files.lines(fileToLoad.toPath())) {
                    lineCount = stream.count();
                }

                String line;
                StringBuilder sb = new StringBuilder();
                long linesLoaded = 0;
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                    updateProgress(++linesLoaded, lineCount);
                }
                return sb.toString();
            }
        };

        loadFileTask.setOnSucceeded(workerStateEvent -> {
            try {
                txtArea_main.setText(loadFileTask.get());
                label_notif.setVisible(true);
                label_notif.setText("File loaded: " + fileToLoad.getName());
            } catch (InterruptedException | ExecutionException e) {
                txtArea_main.setText("Could not load file from:\n" + fileToLoad.getAbsolutePath());
            }
        });

        loadFileTask.setOnFailed(workerStateEvent -> {
            label_notif.setVisible(true);
            txtArea_main.setText("Could not load file from:\n" + fileToLoad.getAbsolutePath());
            label_notif.setText("Failed to load file");
        });

        return loadFileTask;
    }

    private void loadFileToTextArea(File fileToLoad) {
        Task<String> loadFileTask = fileLoaderTask(fileToLoad);
        loadFileTask.run();
    }

    public void wrapText(ActionEvent e){
        txtArea_main.setWrapText(!txtArea_main.wrapTextProperty().get());
    }

    public void onCloseClicked(ActionEvent e){
        Utama.stage.close();
    }

    private void setupFontChoiceBox() {
        List<String> fonts = Font.getFamilies();

        menu_font.setValue("Calibri");
        menu_font.getItems().addAll(fonts);
        txtArea_main.setFont(new Font("Calibri", 12.0));

        menu_font.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, number, t1) -> txtArea_main.setFont(new Font(fonts.get(t1.intValue()), txtArea_main.getFont().getSize())));
    }

    private void setupFontSizeChoiceBox(){
        menu_font_size.setValue(12.0);
        List<Double> fontSizes = Arrays.asList(7.0, 8.0, 10.0, 11.0, 12.0, 14.0, 16.0, 18.0, 20.0, 22.0, 24.0, 26.0, 28.0, 36.0, 48.0, 72.0);
        menu_font_size.getItems().addAll(fontSizes);
        menu_font_size.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, number, t1) -> txtArea_main.setFont(new Font(txtArea_main.getFont().getFamily(), fontSizes.get(t1.intValue()))));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFontChoiceBox();
        setupFontSizeChoiceBox();
        label_notif.setVisible(false);
    }
}