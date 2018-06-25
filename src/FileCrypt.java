import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileCrypt extends Application {

  static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
  static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

  static Label statusLabel = new Label("Ready");

  public static void main(String[] args) {
    launch(args);
  }

  static boolean crypt(String pathString, String key) {
    Path path = Paths.get(pathString);

    try {
      byte[] fileArray = Files.readAllBytes(path);
      byte[] cryptArray = new byte[fileArray.length];
      //Manipulate array here
      //Parallel stream for performance
      IntStream calcStream = IntStream.range(0, fileArray.length).parallel()
          .map(i -> fileArray[i] ^ key.getBytes()[i % key.getBytes().length]);

      int[] streamOutArray = calcStream.toArray();
      for (int i = 0; i < streamOutArray.length; i++) {
        cryptArray[i] = (byte) streamOutArray[i];

      }

      // Change filename to indicate encryption
      Pattern p = Pattern.compile("^\\[(.+)\\]\\..+$");
      Matcher m = p.matcher(path.getFileName().toString());

      String newFileNameNoExt;
      if (m.find()) {
        // Encrypted
        newFileNameNoExt = m.group(1);
      } else {
        // Not encrypted
        newFileNameNoExt = "[" + path.getFileName().toString().split("\\.")[0] + "]";
      }

      p = Pattern.compile(".+\\.(.+)$");
      m = p.matcher(path.getFileName().toString());
      m.matches();
      String ext = m.group(1);
      String newFilePath = path.getParent() + "/" + newFileNameNoExt + "." + ext;

      // Ensure forward slash for OS compatibility
      newFilePath = newFilePath.replace("\\", "/");

      // Write out to new file
      FileOutputStream stream = new FileOutputStream(newFilePath);
      stream.write(cryptArray);
      stream.close();

      return true;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public void start(Stage stage) {
    stage.setTitle("File Encryption");

    BorderPane layout = new BorderPane();

    Font font = new Font("Arial", HEIGHT / 35);

    statusLabel.setFont(font);
    layout.setCenter(statusLabel);
    BorderPane.setAlignment(statusLabel, Pos.CENTER);

    TextField keyField = new TextField();
    keyField.setFont(font);
    layout.setTop(keyField);
    BorderPane.setAlignment(statusLabel, Pos.CENTER);

    Button cryptButton = new Button("Calculate");
    cryptButton.setFont(font);
    cryptButton.setMaxWidth(Double.MAX_VALUE); // Fill width
    cryptButton.setOnAction(actionEvent -> {
      if (!keyField.getText().equals("")) {
        statusLabel.setText("Working...");

        FileChooser fc = new FileChooser();
        fc.setTitle("File for encryption/decryption");
        File file = fc.showOpenDialog(stage);
        if (file != null) {
          statusLabel.setText((crypt(file.getAbsolutePath(), keyField.getText()))
              ? "Done" : "Error");
        } else {
          statusLabel.setText("Cancelled");
        }
      } else {
        statusLabel.setText("Empty key");
      }
    });
    layout.setBottom(cryptButton);


    Scene scene = new Scene(layout, WIDTH * 0.3, HEIGHT * 0.2);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();

  }
}
