package com.sampath.pdfviewer;

import javax.imageio.ImageIO;
import java.awt.print.*;
import java.awt.Graphics;

import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.geometry.Orientation;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.geometry.Bounds;

/**
 * CleanView PDF Viewer Application
 * 
 * A minimalist and lightweight PDF viewer built with JavaFX and PDFBox.
 * Supports zoom, search, printing, export, and basic dark/light theming.
 *
 * @author Sampath Kumar Medarametla
 * @version 1.0
 * @since 2025-07-06
 */

public class CleanView extends Application {

    private ImageView pdfImageView;
    private TabPane tabPane = new TabPane();
    private static String fileToOpen = null;
    private ScrollPane scrollPane;
    private float renderDPI = 150f; // Controls zoom
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private Canvas highlightCanvas = new Canvas();
    private List<Rectangle2D.Float> highlights = new ArrayList<>();
    private String currentKeyword = "";
    private File currentFilePath;
    private Label statusLabel = new Label("Ready");

    @Override
    public void start(Stage primaryStage) {
        pdfImageView = new ImageView();
        highlightCanvas = new Canvas();
        highlightCanvas.setMouseTransparent(true);
        pdfImageView.setPreserveRatio(true);
        pdfImageView.setFitWidth(800);
        ToggleButton themeToggle = new ToggleButton("🌙");

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        themeToggle.setSelected(false);
        themeToggle.setStyle("-fx-background-radius: 20;");
        themeToggle.setOnAction(e -> {
            boolean dark = themeToggle.isSelected();
            if (dark) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
                themeToggle.setText("☀️");
            } else {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource("/light-theme.css").toExternalForm());
                themeToggle.setText("🌙");
            }
        });

        if (fileToOpen != null) {
            File pdfFile = new File(fileToOpen);
            if (pdfFile.exists() && pdfFile.getName().endsWith(".pdf")) {
                try {
                    if (document != null) {
                        document.close();
                    }
                    document = PDDocument.load(pdfFile);
                    renderer = new PDFRenderer(document);
                    currentPage = 0;
                    showPage(currentPage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/icon.png")));
        logo.setFitHeight(24);
        logo.setFitWidth(24);

        Label appTitle = new Label("CleanView PDF"); // Change this to make title of the header in the App
        appTitle.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;-fx-font-family: 'Segoe UI';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Logo
        HBox headerBar = new HBox(10, logo, appTitle, spacer, themeToggle);
        headerBar.setAlignment(Pos.CENTER);
        headerBar.setStyle(
                "-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);" +
                        "-fx-padding: 12px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0.0, 0, 1);");

        FadeTransition fade = new FadeTransition(Duration.millis(800), headerBar);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        HBox iconToolbar = new HBox(12);
        iconToolbar.setStyle(
                "-fx-background-color: #F8F8F8;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 0 0 1 0;" +
                        "-fx-padding: 10 14;" +
                        "-fx-alignment: CENTER_LEFT;");

        ImageView openIcon = loadIcon("folder-open.svg", 30);
        openIcon.setPickOnBounds(true);
        ImageView prevIcon = loadIcon("back.svg", 30);
        prevIcon.setPickOnBounds(true);
        ImageView nextIcon = loadIcon("front.svg", 30);
        nextIcon.setPickOnBounds(true);
        ImageView zoomInIcon = loadIcon("zoom-in.svg", 30);
        zoomInIcon.setPickOnBounds(true);
        ImageView zoomOutIcon = loadIcon("zoom-out.svg", 30);
        zoomOutIcon.setPickOnBounds(true);
        ImageView printIcon = loadIcon("print.svg", 30);
        printIcon.setPickOnBounds(true);
        ImageView exportIcon = loadIcon("export.svg", 30);
        exportIcon.setPickOnBounds(true);
        ImageView fitWidthIcon = loadIcon("fitwidth.svg", 30);
        fitWidthIcon.setPickOnBounds(true);
        ImageView searchIcon = loadIcon("search.svg", 30);
        searchIcon.setPickOnBounds(true);
        ImageView goIcon = loadIcon("enter.svg", 30);
        goIcon.setPickOnBounds(true);

        TextField pageInput = new TextField();
        TextField searchField = new TextField();
        pageInput.setPromptText("Page #");
        pageInput.setPrefWidth(60);
        searchField.setPromptText("Search");
        searchField.setOnAction(e -> {
            String keyword = searchField.getText().trim();
            if (document != null && !keyword.isEmpty()) {
                searchAndGoToPage(keyword);
            } else if (keyword.isEmpty()) {
                searchAndGoToPage(""); // clear highlights
            }
        });

        searchField.setPrefWidth(100);

        for (ImageView iv : Arrays.asList(openIcon, prevIcon, nextIcon, zoomInIcon, zoomOutIcon, printIcon, exportIcon,
                fitWidthIcon, searchIcon, goIcon)) {
            iv.getStyleClass().add("toolbar-icon");
            iv.setPickOnBounds(true);
        }

        iconToolbar.getChildren().addAll(
                openIcon, printIcon, exportIcon,
                new Separator(Orientation.VERTICAL),
                prevIcon, nextIcon, new Region(), fitWidthIcon,
                zoomInIcon, zoomOutIcon,
                pageInput, goIcon,
                searchField, searchIcon
        // spacer only
        );
        HBox.setHgrow(iconToolbar.getChildren().get(iconToolbar.getChildren().size() - 1), Priority.ALWAYS);

        openIcon.setOnMouseClicked(e -> openPdf(primaryStage));
        prevIcon.setOnMouseClicked(e -> showPage(currentPage - 1));
        nextIcon.setOnMouseClicked(e -> showPage(currentPage + 1));
        zoomInIcon.setOnMouseClicked(e -> zoom(25));
        zoomOutIcon.setOnMouseClicked(e -> zoom(-25));
        printIcon.setOnMouseClicked(e -> printCurrentPage());
        exportIcon.setOnMouseClicked(e -> exportCurrentPageAsImage(primaryStage));
        searchIcon.setOnMouseClicked(e -> {
            if (document != null) {
                String keyword = searchField.getText().trim();
                if (!keyword.isEmpty()) {
                    searchAndGoToPage(keyword);
                }
            }
        });

        Tooltip.install(openIcon, new Tooltip("Open PDF"));
        Tooltip.install(prevIcon, new Tooltip("Go to Previous Page"));
        Tooltip.install(nextIcon, new Tooltip("Go to Next Page"));
        Tooltip.install(zoomInIcon, new Tooltip("Zoom In"));
        Tooltip.install(zoomOutIcon, new Tooltip("Zoom Out"));
        Tooltip.install(printIcon, new Tooltip("Print Current Page"));
        Tooltip.install(exportIcon, new Tooltip("Export Current Page as Image"));
        Tooltip.install(fitWidthIcon, new Tooltip("Fit Image to Width"));
        Tooltip.install(searchIcon, new Tooltip("Search Document"));
        Tooltip.install(goIcon, new Tooltip("Go to Page"));
        Tooltip.install(pageInput, new Tooltip("Enter Page Number"));
        Tooltip.install(searchField, new Tooltip("Enter Search Keyword"));

        searchField.getStyleClass().add("search-box");
        pageInput.getStyleClass().add("page-input");

        fitWidthIcon.setOnMouseClicked(e -> {
            if (pdfImageView.getImage() == null)
                return;

            double viewerWidth = scrollPane.getViewportBounds().getWidth();
            fitImageToWidth(viewerWidth > 0 ? viewerWidth : 800);

        });

        goIcon.setOnMouseClicked(e -> {
            if (document == null)
                return;
            try {
                int pageNum = Integer.parseInt(pageInput.getText()) - 1;
                if (pageNum >= 0 && pageNum < document.getNumberOfPages()) {
                    showPage(pageNum);
                } else {
                    System.out.println("Invalid page number");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number");
            }
        });

        // Stack the image and highlight layer
        StackPane layeredView = new StackPane();
        layeredView.getChildren().addAll(pdfImageView, highlightCanvas);
        highlightCanvas.setMouseTransparent(true);

        // Wrap with scroll pane
        scrollPane = new ScrollPane(layeredView);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        /*
         * Tab initialTab = new Tab("No File Open", scrollPane);
         * initialTab.setClosable(false);
         * //initialTab.setGraphic(closeIconBase); // Explicitly clear graphic
         * tabPane.getTabs().add(initialTab);
         * 
         * // statusLabel.setStyle("-fx-padding: 5px; -fx-font-size: 12px;");
         */

        Tab initialTab = new Tab();
        initialTab.setText("No File Open");
        initialTab.setClosable(false);
        initialTab.setGraphic(null); // Ensure old graphic (icon or X) isn't reused

        // Use a new container (don't reuse scrollPane)
        StackPane placeholder = new StackPane(new Label("No PDF Loaded"));
        placeholder.setMinHeight(400);
        initialTab.setContent(placeholder);

        tabPane.getTabs().add(initialTab);

        HBox statusBar = new HBox(statusLabel);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setStyle("-fx-background-color: #f2f2f2;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setId("status-bar");
        statusBar.setMinHeight(24);

        root.setCenter(tabPane);
        // root.setCenter(scrollPane);
        root.setBottom(statusBar);

        initialTab.setOnClosed(event -> {
            System.out.println("Initial tab closed.");
            // Optional: cleanup like closing PDDocument or updating UI
        });

        // System.out.println("TabPane children: " + tabPane.getTabs().size());

        VBox topSection = new VBox();
        topSection.getChildren().addAll(headerBar, iconToolbar);
        root.setTop(topSection);

        primaryStage.setTitle("CleanView");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ImageView loadIcon(String svgFileName, double size) {
        InputStream is = getClass().getResourceAsStream("/icons/" + svgFileName);
        if (is == null) {
            // System.err.println("SVG file not found: " + svgFileName);
            return new ImageView(); // return empty fallback
        }

        SVGPath svg = new SVGPath();
        svg.setFill(Color.web("#212121"));

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains(" d=\"")) {
                    String pathData = line.replaceAll(".*?d=\"([^\"]+)\".*", "$1");
                    svg.setContent(pathData);
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        svg.setFill(Color.web("#212121")); // You can make this dynamic
        svg.setScaleX(size / 24.0);
        svg.setScaleY(size / 24.0);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        Bounds bounds = svg.getLayoutBounds();
        WritableImage image = new WritableImage(
                (int) Math.ceil(bounds.getWidth() * svg.getScaleX()),
                (int) Math.ceil(bounds.getHeight() * svg.getScaleY()));

        WritableImage snapshot = svg.snapshot(params, image);
        return new ImageView(snapshot);
    }

    private void openPdf(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            currentFilePath = selectedFile;
            try {
                if (document != null) {
                    document.close();
                }
                document = PDDocument.load(selectedFile);
                renderer = new PDFRenderer(document);
                currentPage = 0;

                showPage(currentPage);

                // Update the tab title
                String filename = selectedFile.getName();
                Label title = new Label(filename);
                Label closeIcon = new Label("✖");
                closeIcon.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-cursor: hand;");
                HBox tabHeader = new HBox(title, closeIcon);
                tabHeader.setAlignment(Pos.CENTER_LEFT);
                tabHeader.setSpacing(5);

                Tab pdfTab = new Tab();
                pdfTab.setContent(scrollPane);
                pdfTab.setClosable(false); // optional for now
                pdfTab.setGraphic(tabHeader);

                closeIcon.setOnMouseClicked(e -> {
                    try {
                        if (document != null) {
                            document.close();
                            document = null; // reset reference
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    tabPane.getTabs().setAll(createNoFileTab());
                    updateStatusBar(); // 🔥 Reset status to "Ready"
                });

                tabPane.getTabs().setAll(pdfTab); // Replace current tab

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private Tab createNoFileTab() {
        Label placeholder = new Label("No PDF Loaded");
        placeholder.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");

        StackPane container = new StackPane(placeholder);
        container.setMinHeight(400);

        Tab tab = new Tab();
        tab.setText("No File Open");
        tab.setContent(container);
        tab.setClosable(false); // Ensure X is not shown

        return tab;
    }

    private void showPage(int pageIndex) {
        updateStatusBar();
        if (document == null || renderer == null)
            return;
        if (pageIndex >= 0 && pageIndex < document.getNumberOfPages()) {
            try {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, renderDPI);
                WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                pdfImageView.setImage(fxImage);
                pdfImageView.setFitWidth(fxImage.getWidth());
                pdfImageView.setFitHeight(fxImage.getHeight());

                currentPage = pageIndex;

                highlightCanvas.setWidth(fxImage.getWidth());
                highlightCanvas.setHeight(fxImage.getHeight());

                if (!currentKeyword.isEmpty()) {
                    highlights = findHighlights(currentPage, currentKeyword);
                } else {
                    highlights.clear();
                }
                drawHighlights();
                if (document != null) {
                    updateStatusBar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateStatusBar() {
        if (document != null) {
            statusLabel.setText("Page " + (currentPage + 1) + " of " + document.getNumberOfPages() +
                    " | Zoom: " + (int) renderDPI + "%");
        } else {
            statusLabel.setText("Ready");

        }
    }

    private void fitImageToWidth(double targetWidth) {
        if (pdfImageView.getImage() == null || document == null || renderer == null)
            return;

        try {
            double imageRatio = pdfImageView.getImage().getHeight() / pdfImageView.getImage().getWidth();
            double targetHeight = targetWidth * imageRatio;

            pdfImageView.setFitWidth(targetWidth);
            pdfImageView.setFitHeight(targetHeight);

            highlightCanvas.setWidth(targetWidth);
            highlightCanvas.setHeight(targetHeight);

            drawHighlights(); // Re-draw with new size

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<Rectangle2D.Float> findHighlights(int pageIndex, String keyword) {
        List<Rectangle2D.Float> boxes = new ArrayList<>();
        try {
            PDFTextStripper stripper = new PDFTextStripper() {
                StringBuilder buffer = new StringBuilder();
                List<TextPosition> currentWord = new ArrayList<>();

                @Override
                protected void processTextPosition(TextPosition text) {
                    String c = text.getUnicode();
                    if (c.trim().isEmpty() || c.equals(" ")) {
                        checkWord(buffer.toString(), currentWord);
                        buffer.setLength(0);
                        currentWord.clear();
                    } else {
                        buffer.append(c);
                        currentWord.add(text);
                    }
                }

                private void checkWord(String word, List<TextPosition> positions) {
                    if (word.toLowerCase().contains(keyword.toLowerCase()) && !positions.isEmpty()) {
                        float dpiScale = renderDPI / 72f;

                        float x = positions.get(0).getXDirAdj() * dpiScale;
                        float y = positions.get(0).getYDirAdj() * dpiScale;
                        float w = 0;
                        float h = 0;
                        for (TextPosition tp : positions) {
                            w += tp.getWidthDirAdj() * dpiScale;
                            h = Math.max(h, tp.getHeightDir() * dpiScale);
                        }
                        boxes.add(new Rectangle2D.Float(x, y, w, h));
                        // System.out.println("Matched: " + word + " at (" + x + "," + y + ")");
                    }
                }
            };

            stripper.setStartPage(pageIndex + 1);
            stripper.setEndPage(pageIndex + 1);
            stripper.getText(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boxes;
    }

    private void drawHighlights() {
        // Search and Highlights sucks but that is ok for a initial version lets fix it
        // later on writing this comment so that i don't forget
        GraphicsContext gc = highlightCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, highlightCanvas.getWidth(), highlightCanvas.getHeight());
        gc.setFill(Color.color(1, 1, 0, 0.4)); // semi-transparent yellow

        for (Rectangle2D.Float box : highlights) {
            gc.fillRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
        }
    }

    private void exportCurrentPageAsImage(Stage stage) {
        if (document == null || renderer == null)
            return;

        try {
            BufferedImage image = renderer.renderImageWithDPI(currentPage, 300); // high-res export

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Page As Image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
            fileChooser.setInitialFileName("Page_" + (currentPage + 1) + ".png");

            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                ImageIO.write(image, "png", file);
                System.out.println("Exported page to " + file.getAbsolutePath());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void zoom(double dpiChange) {
        renderDPI += dpiChange;
        if (renderDPI < 75f)
            renderDPI = 75f;
        if (renderDPI > 600f)
            renderDPI = 600f;
        showPage(currentPage);
    }

    private void printCurrentPage() {
        if (document == null || renderer == null)
            return;

        try {
            BufferedImage image = renderer.renderImageWithDPI(currentPage, 300);

            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setJobName("CleanView  - Page " + (currentPage + 1));

            System.out.println("Print clicked");

            // Printable wraps our image
            Printable printable = new Printable() {
                public int print(Graphics g, PageFormat pf, int pageIndex) {
                    if (pageIndex > 0)
                        return NO_SUCH_PAGE;

                    // Center image on the page
                    double x = pf.getImageableX();
                    double y = pf.getImageableY();
                    double width = pf.getImageableWidth();
                    double height = pf.getImageableHeight();

                    double scaleX = width / image.getWidth();
                    double scaleY = height / image.getHeight();
                    double scale = Math.min(scaleX, scaleY);

                    ((Graphics2D) g).translate(x, y);
                    ((Graphics2D) g).scale(scale, scale);
                    g.drawImage(image, 0, 0, null);

                    return PAGE_EXISTS;
                }

            };

            printJob.setPrintable(printable);
            boolean doPrint = printJob.printDialog();

            if (doPrint) {
                printJob.print();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void searchAndGoToPage(String keyword) {
        try {
            PDFTextStripper textStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int i = 0; i < totalPages; i++) {
                textStripper.setStartPage(i + 1);
                textStripper.setEndPage(i + 1);
                String pageText = textStripper.getText(document);

                if (pageText.toLowerCase().contains(keyword.toLowerCase())) {
                    currentKeyword = keyword;
                    showPage(i);
                    return;
                }
            }

            // System.out.println("Keyword not found.");
            currentKeyword = "";
            highlights.clear();
            drawHighlights();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        if (document != null) {
            document.close();
        }
        super.stop();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            fileToOpen = args[0];
        }
        launch(args);
    }
}
