package aralinks;

import aralinks.exception.InvalidCredentialsException;
import aralinks.model.User;
import aralinks.service.AuthService;
import aralinks.service.DataStore;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

/**
 * Entry point. For now it shows a working login screen that proves the layers connect:
 * UI → AuthService → model (User.login, displayMenu) → custom exceptions → UI message.
 * Replace this with the real screens in the ui package as you build them.
 */
public class App extends Application {

    private final DataStore db = DataStore.withDemoData();
    private final AuthService auth = new AuthService(db);

    @Override
    public void start(Stage stage) {
        loadFonts();

        ImageView logo = image("/logo/aralinks-logo.png", 240);
        ImageView sparty = image("/mascot/sparty-waving.png", 150);

        Label title = new Label("Find a peer tutor. Share what you know.");
        title.getStyleClass().add("title");

        TextField srCode = new TextField();
        srCode.setPromptText("SR-Code (00-00000)");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        Button login = new Button("LOG IN");
        login.getStyleClass().add("btn-primary");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setDefaultButton(true);

        Label message = new Label("Try 25-11111 / demo123 (learner) or 23-01234 / demo123 (tutor)");
        message.getStyleClass().add("muted");
        message.setWrapText(true);

        login.setOnAction(e -> {
            try {
                User user = auth.login(srCode.getText(), password.getText());
                // Polymorphism: the same call gives a different menu for each role.
                List<String> menu = user.displayMenu();
                message.setText("Welcome, " + user.getName() + "! (" + user.getRole() + ")\nYour menu: " + String.join(" · ", menu));
                message.getStyleClass().setAll("success");
                sparty.setImage(new Image(App.class.getResourceAsStream("/mascot/sparty-cheering.png")));
            } catch (InvalidCredentialsException ex) {
                message.setText("Oops! " + ex.getMessage());
                message.getStyleClass().setAll("error");
                sparty.setImage(new Image(App.class.getResourceAsStream("/mascot/sparty-worried.png")));
            }
        });

        VBox form = new VBox(12, srCode, password, login, message);
        form.getStyleClass().add("card");
        form.setMaxWidth(360);

        VBox left = new VBox(16, sparty, title);
        left.setAlignment(Pos.CENTER);
        left.setMaxWidth(340);

        HBox body = new HBox(48, left, form);
        body.setAlignment(Pos.CENTER);

        VBox root = new VBox(24, logo, body);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(28));

        Scene scene = new Scene(root, 960, 620);
        scene.getStylesheets().add(App.class.getResource("/aralinks.css").toExternalForm());
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/logo/aralinks-app-icon.png")));
        stage.setTitle("AraLinks");
        stage.setScene(scene);
        stage.show();
    }

    private static void loadFonts() {
        for (String weight : List.of("SemiBold", "Bold", "ExtraBold", "Black")) {
            Font.loadFont(App.class.getResourceAsStream("/fonts/Nunito-" + weight + ".ttf"), 14);
        }
    }

    private static ImageView image(String path, double width) {
        ImageView view = new ImageView(new Image(App.class.getResourceAsStream(path)));
        view.setFitWidth(width);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
