import java.awt.Toolkit;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javax.swing.JFileChooser;

public class FileCrypt extends Application {

  static Label statusLabel = new Label("Ready");
  static final int WIDTH  = Toolkit.getDefaultToolkit().getScreenSize().width;
  static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;


  public static void main(String[] args) {

    System.out.println(WIDTH);

    launch(args);

    /*
    JFrame.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new JFrame("FileCrypt");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    totalGUI.setPreferredSize(new Dimension((int) (WIDTH * 0.3), (int) (HEIGHT * 0.2)));
    totalGUI.setLayout(new BorderLayout());

    JTextField keyField = new JTextField();
    keyField.setHorizontalAlignment(JTextField.CENTER);
    keyField.setPreferredSize(new Dimension((int) (WIDTH * 0.25), (int) (HEIGHT * 0.05)));
    keyField.setLocation(25, 15);
    totalGUI.add(keyField, BorderLayout.PAGE_START);

    statusLabel.setHorizontalAlignment(JLabel.CENTER);
    statusLabel.setFont(new Font("Calibri", Font.PLAIN, HEIGHT / 30));
    statusLabel.setPreferredSize(new Dimension((int) (WIDTH * 0.25), (int) (HEIGHT * 0.05)));
    totalGUI.add(statusLabel, BorderLayout.CENTER);

    JButton encryptButton = new JButton("Calculate");
    encryptButton.setPreferredSize(new Dimension((int) (WIDTH * 0.25), (int) (HEIGHT * 0.05)));
    encryptButton.addActionListener(e -> {
      if (!keyField.getText().equals("")) {
        statusLabel.setText("WORKING...");
        JFileChooser jfc = new JFileChooser();
        int returnVal = jfc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          statusLabel.setText((crypt(jfc.getSelectedFile()
              .getAbsolutePath(), keyField.getText())) ? "DONE"
              : "ERROR");
        }
      } else {
        statusLabel.setText("EMPTY KEY");
      }
    });
    totalGUI.add(encryptButton, BorderLayout.PAGE_END);

    frame.add(totalGUI);
    frame.setVisible(true);
    frame.pack();
    frame.setLocationRelativeTo(null);

    */

  }

  static boolean crypt(String pathString, String key) {
    Path path = Paths.get(pathString);

    try {
      byte[] fileArray = Files.readAllBytes(path);
      byte[] cryptArray = new byte[fileArray.length];
      //Manipulate array here
      //Parallel stream for performance
      IntStream pCalcstream = IntStream.range(0, fileArray.length).parallel()
          .map(i -> fileArray[i] ^ key.getBytes()[i % key.getBytes().length]);

      int[] streamOutArray = pCalcstream.toArray();
      for (int i = 0; i < streamOutArray.length; i++) {
        cryptArray[i] = (byte) streamOutArray[i];

      }

			/*
			for (int i = 0; i < fileArray.length; i++) {
				byte fileByte = fileArray[i];
				cryptArray[i]=(byte) (fileByte^key.getBytes()[i%key.getBytes().length]);
			}
			*/

      Pattern p = Pattern.compile("^\\[(.+)\\]\\..+$");
      Matcher m = p.matcher(path.getFileName().toString());

      String newFileNameNoExt;
      if (m.find()) { //encrypted
        newFileNameNoExt = m.group(1);
      } else { //not encrypted
        newFileNameNoExt = "[" + path.getFileName().toString().split("\\.")[0] + "]";
      }

      p = Pattern.compile(".+\\.(.+)$");
      m = p.matcher(path.getFileName().toString());
      m.matches();
      String ext = m.group(1);

      String newFilePath = path.getParent() + "\\" + newFileNameNoExt + "." + ext;

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
  public void start(Stage stage) throws Exception {
    stage.setTitle("File Encryption");

    BorderPane layout = new BorderPane();
    layout.setPadding(new Insets(0,0,10,0));

    Font font = new Font("Arial", HEIGHT / 35);

    statusLabel.setFont(font);
    layout.setBottom(statusLabel);
    BorderPane.setAlignment(statusLabel, Pos.CENTER);

    TextField keyField = new TextField();
    keyField.setFont(font);
    layout.setTop(keyField);
    BorderPane.setAlignment(statusLabel, Pos.CENTER);

    Button cryptButton = new Button("Calculate");
    cryptButton.setFont(font);
    cryptButton.setOnAction(actionEvent -> {
      if (!keyField.getText().equals("")) {
        statusLabel.setText("Working...");
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          statusLabel.setText((crypt(jfc.getSelectedFile()
              .getAbsolutePath(), keyField.getText())) ? "Done"
              : "Error");
        }
      } else {
        statusLabel.setText("Empty key");
      }
    });
    layout.setCenter(cryptButton);

    Scene scene = new Scene(layout, WIDTH * 0.3, HEIGHT * 0.2);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();

  }
}
