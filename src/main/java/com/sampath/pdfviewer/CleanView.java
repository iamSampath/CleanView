package com.sampath.pdfviewer;

import javax.imageio.ImageIO;
import java.awt.print.*;
import java.awt.Graphics;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CleanView extends Application {

    private ImageView pdfImageView;
    private ScrollPane scrollPane;
    private float renderDPI = 150f; // Controls zoom
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private Canvas highlightCanvas = new Canvas();
    private List<Rectangle2D.Float> highlights = new ArrayList<>();
    private String currentKeyword = "";

    @Override
    public void start(Stage primaryStage) {
        pdfImageView = new ImageView();
        highlightCanvas = new Canvas();
        highlightCanvas.setMouseTransparent(true);

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

        pdfImageView.setPreserveRatio(true);
        pdfImageView.setFitWidth(800);

        Button openButton = new Button("Open PDF");
        Button prevButton = new Button("Previous");
        Button nextButton = new Button("Next");
        Button zoomInButton = new Button("Zoom In");
        Button zoomOutButton = new Button("Zoom Out");
        Button goButton = new Button("Go");
        Button searchButton = new Button("Find");
        Button printButton = new Button("Print");
        Button exportButton = new Button("Export Page");
        Button fitToWidthButton = new Button("Fit to Width");

        openButton.setOnAction(e -> openPdf(primaryStage));
        prevButton.setOnAction(e -> showPage(currentPage - 1));
        nextButton.setOnAction(e -> showPage(currentPage + 1));
        zoomInButton.setOnAction(e -> zoom(25));
        zoomOutButton.setOnAction(e -> zoom(-25));
        printButton.setOnAction(e -> printCurrentPage());
        exportButton.setOnAction(e -> exportCurrentPageAsImage(primaryStage));
        fitToWidthButton.setOnAction(e -> {
    if (pdfImageView.getImage() == null) return;

    double viewerWidth = scrollPane.getViewportBounds().getWidth();

    if (viewerWidth <= 0) {
        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            fitImageToWidth(newVal.getWidth());
        });
    } else {
        fitImageToWidth(viewerWidth);
    }
});


        searchButton.setOnAction(e -> {
            if (document == null)
                return;
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                searchAndGoToPage(keyword);
            }
        });

        goButton.setOnAction(e -> {
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

        ToolBar toolbar = new ToolBar(openButton, prevButton, nextButton, zoomInButton, zoomOutButton,
                pageInput, goButton, searchField, searchButton, printButton,exportButton, fitToWidthButton);

        

        // Stack the image and highlight layer
        StackPane layeredView = new StackPane();
        layeredView.getChildren().addAll(pdfImageView, highlightCanvas);
        highlightCanvas.setMouseTransparent(true);

        // Wrap with scroll pane
        scrollPane = new ScrollPane(layeredView);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Root layout
        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1200, 1200);
        primaryStage.setTitle("CleanView");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openPdf(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                if (document != null) {
                    document.close();
                }

                document = PDDocument.load(selectedFile);
                renderer = new PDFRenderer(document);
                currentPage = 0;
                showPage(currentPage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showPage(int pageIndex) {
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void fitImageToWidth(double targetWidth) {
    if (pdfImageView.getImage() == null || document == null || renderer == null) return;

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
                        //System.out.println("Matched: " + word + " at (" + x + "," + y + ")");
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
        //Search and Highlights sucks but that is ok for a initial version lets fix it later on writing this comment so that i don't forget
        GraphicsContext gc = highlightCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, highlightCanvas.getWidth(), highlightCanvas.getHeight());
        gc.setFill(Color.color(1, 1, 0, 0.4)); // semi-transparent yellow

        for (Rectangle2D.Float box : highlights) {
            gc.fillRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
        }
    }
    private void exportCurrentPageAsImage(Stage stage) {
    if (document == null || renderer == null) return;

    try {
        BufferedImage image = renderer.renderImageWithDPI(currentPage, 300); // high-res export

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Page As Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        fileChooser.setInitialFileName("Page_" + (currentPage + 1) + ".png");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            ImageIO.write(image, "png", file);
            //System.out.println("Exported page to " + file.getAbsolutePath());
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
    if (document == null || renderer == null) return;

    try {
        BufferedImage image = renderer.renderImageWithDPI(currentPage, 300);

        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setJobName("CleanView  - Page " + (currentPage + 1));

        // Printable wraps our image
        Printable printable = new Printable() {
            public int print(Graphics g, PageFormat pf, int pageIndex) {
                if (pageIndex > 0) return NO_SUCH_PAGE;

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

            //System.out.println("Keyword not found.");
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
        launch(args);
    }
}
