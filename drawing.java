import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.lang.Runtime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

// import com.oracle.tools.packager.Platform;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.text.Font;

/**
 * JavaFX お絵描きアプリケーションのメインクラス
 */
public class drawing extends Application {
  Canvas canvas, out_canvas, center_canvas, gan_canvas1, gan_canvas2, left_canvas, mix_canvas;
  GraphicsContext gc, out_gc, gan_gc1, gan_gc2, mix_gc;
  double oldx;
  double oldy;
  final int SIZE = 512;
  Button clear;
  Label state_Label = new Label();
  int seed=255, size=1, structure=1, color=1, mix_num=3;

  /**
   * お絵描きプログラムの準備をして、ウィンドウを開きます
   */
  public void start(Stage stage) {
    canvas = new Canvas(SIZE, SIZE);
    center_canvas = new Canvas(30, SIZE);
    left_canvas = new Canvas(30, SIZE);
    out_canvas = new Canvas(SIZE, SIZE);
    int gan_size = 256;
    gan_canvas1 = new Canvas(gan_size, gan_size);
    gan_canvas2 = new Canvas(gan_size, gan_size);
    mix_canvas = new Canvas(gan_size, gan_size);
    gc = canvas.getGraphicsContext2D();
    out_gc = out_canvas.getGraphicsContext2D();
    gan_gc1 = gan_canvas1.getGraphicsContext2D();
    gan_gc2 = gan_canvas2.getGraphicsContext2D();
    mix_gc = mix_canvas.getGraphicsContext2D();
    state_Label.setFont(new Font(16));
    gc.setFill(Color.WHITE);
    gc.fillRect(0, 0, SIZE, SIZE);
    out_gc.setFill(Color.WHITE);
    out_gc.fillRect(0, 0, SIZE, SIZE);
    gan_gc1.setFill(Color.WHITE);
    gan_gc1.fillRect(0, 0, SIZE, SIZE);
    gan_gc2.setFill(Color.WHITE);
    gan_gc2.fillRect(0, 0, SIZE, SIZE);
    mix_gc.setFill(Color.WHITE);
    mix_gc.fillRect(0, 0, SIZE, SIZE);
    // drawShapes(gc);
    Border border = new Border(
        new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT));

    Button generate1 = new Button("generate1");
    generate1.setBorder(border);
    generate1.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(5), Insets.EMPTY)));
    generate1.setOnAction(e->{
      System.out.println(seed);
      ExecutorService service = Executors.newSingleThreadExecutor();
      Thread thread = new Thread(()->ganMethod1());
      service.execute(thread);
      service.shutdown();
    });

    Button generate2 = new Button("generate2");
    generate2.setBorder(border);
    generate2.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(5), Insets.EMPTY)));
    generate2.setOnAction(e->{
      System.out.println(seed);
      ExecutorService service = Executors.newSingleThreadExecutor();
      Thread thread = new Thread(()->ganMethod2());
      service.execute(thread);
      service.shutdown();
    });

    Button mix = new Button("mix");
    mix.setBorder(border);
    mix.setBackground(new Background(new BackgroundFill(Color.LIGHTPINK, new CornerRadii(5), Insets.EMPTY)));
    mix.setOnAction(e->{
      ExecutorService service = Executors.newSingleThreadExecutor();
      Thread thread = new Thread(()->mix_ganMethod());
      service.execute(thread);
      service.shutdown();
    });

    Slider mix_Slider = new Slider(0, 6, 3);
    mix_Slider.setShowTickMarks(true);
    mix_Slider.setBlockIncrement(1);
    mix_Slider.setMajorTickUnit(1);
    mix_Slider.setMinorTickCount(0);
    mix_Slider.setSnapToTicks(true);
    mix_Slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number oldv, Number nv) -> {
      File out_File1 = new File("/Users/TAKEI/Desktop/chainer-PGGAN/out/mix"+String.valueOf((int) mix_Slider.getValue())+".png");
      Image out_image1 = new Image(out_File1.toURI().toString());
      mix_gc.drawImage(out_image1, 0, 0);
      mix_num = (int) mix_Slider.getValue();
    });

    Label size_label = new Label("size");
    Slider size_slider = new Slider(0,2,1);
    size_slider.setShowTickMarks(true);
    size_slider.setBlockIncrement(1);
    size_slider.setMajorTickUnit(1);
    size_slider.setMinorTickCount(0);
    size_slider.setSnapToTicks(true);
    size_slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number oldv, Number nv) -> {
      size = (int) size_slider.getValue();
    });

    Label structure_label = new Label("structure");
    Slider structure_slider = new Slider(0,2,1);
    structure_slider.setShowTickMarks(true);
    structure_slider.setBlockIncrement(1);
    structure_slider.setMajorTickUnit(1);
    structure_slider.setMinorTickCount(0);
    structure_slider.setSnapToTicks(true);
    structure_slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number oldv, Number nv) -> {
      structure = (int) structure_slider.getValue();
    });

    Label color_label = new Label("color");
    Slider color_slider = new Slider(0,2,1);
    color_slider.setShowTickMarks(true);
    color_slider.setBlockIncrement(1);
    color_slider.setMajorTickUnit(1);
    color_slider.setMinorTickCount(0);
    color_slider.setSnapToTicks(true);
    color_slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number oldv, Number nv) -> {
      color = (int) color_slider.getValue();
    });

    Label seed_label = new Label("seed");
    Slider seed_slider = new Slider(0, 512, 255);
    seed_slider.setBlockIncrement(1);
    seed_slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number oldv, Number nv) -> {
      seed = (int) seed_slider.getValue();
    });

    BorderPane bp = new BorderPane();
    HBox size_hb = new HBox();
    size_hb.setAlignment(Pos.CENTER);
    size_hb.getChildren().addAll(size_label, size_slider);
    HBox structure_hb = new HBox();
    structure_hb.setAlignment(Pos.CENTER);
    structure_hb.getChildren().addAll(structure_label, structure_slider);
    HBox color_hb = new HBox();
    color_hb.setAlignment(Pos.CENTER);
    color_hb.getChildren().addAll(color_label, color_slider);
    HBox seed_hb = new HBox();
    seed_hb.setAlignment(Pos.CENTER);
    seed_hb.getChildren().addAll(seed_label, seed_slider);
    VBox gan1_vb = new VBox();
    gan1_vb.setAlignment(Pos.CENTER);
    gan1_vb.getChildren().addAll(gan_canvas1, generate1);
    VBox gan2_vb = new VBox();
    gan2_vb.setAlignment(Pos.CENTER);
    gan2_vb.getChildren().addAll(gan_canvas2, generate2);
    HBox canvas_hb = new HBox();
    canvas_hb.setAlignment(Pos.CENTER);
    canvas_hb.getChildren().addAll(gan1_vb, left_canvas, gan2_vb);
    VBox slider_vb = new VBox();
    slider_vb.setAlignment(Pos.CENTER);
    slider_vb.getChildren().addAll(size_hb, structure_hb, color_hb, seed_hb);
    VBox gen_vb = new VBox();
    gen_vb.setAlignment(Pos.CENTER);
    gen_vb.getChildren().add(slider_vb);
    gen_vb.getChildren().add(canvas_hb);
    VBox mix_vb = new VBox();
    mix_vb.setAlignment(Pos.CENTER);
    mix_vb.getChildren().addAll(mix, mix_canvas, mix_Slider);
    HBox mix_hb = new HBox();
    mix_hb.getChildren().addAll(center_canvas, mix_vb);
    bp.setLeft(gen_vb);
    bp.setRight(mix_hb);

    Scene scene = new Scene(bp);
    stage.setScene(scene);
    stage.setResizable(false);
    stage.setTitle("Generative Adverarial Networks Demo");
    stage.show();
  }

  private void ganMethod1(){
    String [] cmd = {"python", "gen_from_npy.py", "--out", "out/1.png", "--seed", String.valueOf(seed),
        "--size", String.valueOf(size), "--structure", String.valueOf(structure), "--color", String.valueOf(color)};
    try {
      Process p1 = Runtime.getRuntime().exec(cmd, null);
      p1.waitFor();
    } catch (IOException | InterruptedException e2) {
      e2.printStackTrace();
    }
    File out_File = new File("./out/1.png");
    Image out_image = new Image(out_File.toURI().toString());
    Platform.runLater(()-> gan_gc1.drawImage(out_image, 0, 0));
  }

  private void ganMethod2(){
    String [] cmd = {"python", "gen_from_npy.py", "--out", "out/2.png", "--seed", String.valueOf(seed),
        "--size", String.valueOf(size), "--structure", String.valueOf(structure), "--color", String.valueOf(color)};
    try {
      Process p1 = Runtime.getRuntime().exec(cmd, null);
      p1.waitFor();
    } catch (IOException | InterruptedException e2) {
      e2.printStackTrace();
    }
    File out_File = new File("./out/2.png");
    Image out_image = new Image(out_File.toURI().toString());
    Platform.runLater(()-> gan_gc2.drawImage(out_image, 0, 0));
  }

  private void mix_ganMethod(){
    String [] cmd = {"python", "generate.py", "--out", "out/mix.png", "--vector_ope", "out/1.npy", "out/2.npy"};
    try {
      Process p1 = Runtime.getRuntime().exec(cmd, null);
      p1.waitFor();
    } catch (IOException | InterruptedException e2) {
      e2.printStackTrace();
    }
    File out_File = new File("./out/mix"+String.valueOf(mix_num)+".png");
    Image out_image = new Image(out_File.toURI().toString());
    Platform.runLater(()-> mix_gc.drawImage(out_image, 0, 0));
  }

}
