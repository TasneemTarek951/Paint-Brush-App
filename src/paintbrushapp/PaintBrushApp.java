package paintbrushapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PaintBrushApp {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Paint Brush");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        // =========================
        // Create Buttons
        // =========================
        JButton clearButton = new JButton(IconUtil.resizeIcon("images/broom.png", 24, 24));
        JButton saveButton = new JButton(IconUtil.resizeIcon("images/save.png", 24, 24));
        JButton openButton = new JButton(IconUtil.resizeIcon("images/open.png", 24, 24));
        JButton undoButton = new JButton(IconUtil.resizeIcon("images/undo.png", 24, 24));
        JButton redoButton = new JButton(IconUtil.resizeIcon("images/redo.png", 24, 24));

        JButton eraserButton = new JButton(IconUtil.resizeIcon("images/eraser.png", 24, 24));
        JButton lineButton = new JButton(IconUtil.resizeIcon("images/line.png", 24, 24));
        JButton ovalButton = new JButton(IconUtil.resizeIcon("images/oval.png", 24, 24));
        JButton rectButton = new JButton(IconUtil.resizeIcon("images/rectangle (1).png", 24, 24));
        JButton freeHandButton = new JButton(IconUtil.resizeIcon("images/pencil.png", 24, 24));
        JButton triangleButton = new JButton(IconUtil.resizeIcon("images/triangle.png", 24, 24));

        JCheckBox fillCheckBox = new JCheckBox("Fill");

        // Color buttons
        JButton blackButton = new JButton();
        JButton redButton = new JButton();
        JButton greenButton = new JButton();
        JButton blueButton = new JButton();
        JButton colorPickerButton = new JButton("More...");

        blackButton.setBackground(Color.BLACK);
        redButton.setBackground(Color.RED);
        greenButton.setBackground(Color.GREEN);
        blueButton.setBackground(Color.BLUE);

        Dimension colorSize = new Dimension(30, 30);
        for (JButton btn : new JButton[]{blackButton, redButton, greenButton, blueButton}) {
            btn.setPreferredSize(colorSize);
        }

        // Stroke size selector
        String[] sizes = {"2px", "4px", "6px", "8px", "12px"};
        JComboBox<String> strokeSizeBox = new JComboBox<>(sizes);

        // =========================
        // Ribbon (Grouped Toolbar)
        // =========================
        JPanel ribbonPanel = new JPanel(new GridLayout(1, 5, 10, 0));

        ribbonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5)); 
        
        JPanel filePanel = new JPanel();
        filePanel.setBorder(BorderFactory.createTitledBorder("File"));
        filePanel.add(openButton);
        filePanel.add(saveButton);
        filePanel.add(clearButton);
        filePanel.add(undoButton);
        filePanel.add(redoButton);

        JPanel toolsPanel = new JPanel();
        toolsPanel.setBorder(BorderFactory.createTitledBorder("Tools"));
        toolsPanel.add(freeHandButton);
        toolsPanel.add(eraserButton);
        toolsPanel.add(fillCheckBox);

        JPanel shapesPanel = new JPanel();
        shapesPanel.setBorder(BorderFactory.createTitledBorder("Shapes"));
        shapesPanel.add(lineButton);
        shapesPanel.add(rectButton);
        shapesPanel.add(ovalButton);
        shapesPanel.add(triangleButton);

        JPanel colorsPanel = new JPanel();
        colorsPanel.setBorder(BorderFactory.createTitledBorder("Colors"));
        colorsPanel.add(blackButton);
        colorsPanel.add(redButton);
        colorsPanel.add(greenButton);
        colorsPanel.add(blueButton);
        colorsPanel.add(colorPickerButton);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        optionsPanel.add(new JLabel("Stroke:"));
        optionsPanel.add(strokeSizeBox);

        ribbonPanel.add(filePanel);
        ribbonPanel.add(toolsPanel);
        ribbonPanel.add(shapesPanel);
        ribbonPanel.add(colorsPanel);
        ribbonPanel.add(optionsPanel);

        frame.add(ribbonPanel, BorderLayout.NORTH);

        // =========================
        // Drawing Panel
        // =========================
        DrawingPanel drawPanel = new DrawingPanel();
        frame.add(drawPanel, BorderLayout.CENTER);

        // =========================
        // Action Listeners
        // =========================
        clearButton.addActionListener(e -> drawPanel.clear());
        openButton.addActionListener(e -> drawPanel.openImage(frame));
        saveButton.addActionListener(e -> drawPanel.saveImage(frame));
        undoButton.addActionListener(e -> drawPanel.undo());
        redoButton.addActionListener(e -> drawPanel.redo());

        fillCheckBox.addActionListener(e -> drawPanel.fill = fillCheckBox.isSelected());

        blackButton.addActionListener(e -> drawPanel.currentColor = Color.BLACK);
        redButton.addActionListener(e -> drawPanel.currentColor = Color.RED);
        greenButton.addActionListener(e -> drawPanel.currentColor = Color.GREEN);
        blueButton.addActionListener(e -> drawPanel.currentColor = Color.BLUE);
        colorPickerButton.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(frame, "Choose a color", drawPanel.currentColor);
            if (chosen != null) {
                drawPanel.currentColor = chosen;
            }
        });

        lineButton.addActionListener(e -> drawPanel.setShape("line"));
        ovalButton.addActionListener(e -> drawPanel.setShape("oval"));
        rectButton.addActionListener(e -> drawPanel.setShape("rect"));
        freeHandButton.addActionListener(e -> drawPanel.setShape("free"));
        triangleButton.addActionListener(e -> drawPanel.setShape("triangle"));
        eraserButton.addActionListener(e -> {
            drawPanel.setShape("eraser");
            drawPanel.currentColor = Color.WHITE;
        });

        strokeSizeBox.addActionListener(e -> {
            String sizeStr = (String) strokeSizeBox.getSelectedItem();
            int stroke = Integer.parseInt(sizeStr.replace("px", ""));
            drawPanel.strokeWidth = stroke;
        });

        frame.setVisible(true);
    }

    public static class IconUtil {
        public static ImageIcon resizeIcon(String path, int width, int height) {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage();
            Image resized = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resized);
        }
    }

    static class DrawingPanel extends JPanel {
        private final List<Shape> shapes = new ArrayList<>();
        private final List<Shape> redoStack = new ArrayList<>();
        private Shape currentShape;
        private int x1, y1;
        boolean fill = false;
        Color currentColor = Color.BLACK;
        String currentTool = "free";
        int strokeWidth = 2;
        private BufferedImage loadedImage = null;

        public DrawingPanel() {
            setBackground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    x1 = e.getX();
                    y1 = e.getY();
                    currentShape = createShape(x1, y1, x1, y1);
                }

                public void mouseReleased(MouseEvent e) {
                    if (currentShape != null) {
                        currentShape.x2 = e.getX();
                        currentShape.y2 = e.getY();
                        shapes.add(currentShape);
                        currentShape = null;
                        redoStack.clear();
                        repaint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if ("free".equals(currentTool)) {
                        shapes.add(new Line(x1, y1, e.getX(), e.getY(), currentColor, strokeWidth));
                        x1 = e.getX();
                        y1 = e.getY();
                    } else if ("eraser".equals(currentTool)) {
                        shapes.add(new Rect(e.getX(), e.getY(), e.getX() + 20, e.getY() + 20, Color.WHITE, true, strokeWidth));
                    } else if (currentShape != null) {
                        currentShape.x2 = e.getX();
                        currentShape.y2 = e.getY();
                    }
                    repaint();
                }
            });
        }

        public void setShape(String tool) {
            this.currentTool = tool;
        }

        private Shape createShape(int x1, int y1, int x2, int y2) {
            switch (currentTool) {
                case "line": return new Line(x1, y1, x2, y2, currentColor, strokeWidth);
                case "oval": return new Oval(x1, y1, x2, y2, currentColor, fill, strokeWidth);
                case "rect": return new Rect(x1, y1, x2, y2, currentColor, fill, strokeWidth);
                case "triangle": return new Triangle(x1, y1, x2, y2, currentColor, fill, strokeWidth);
                default: return null;
            }
        }

        public void paint(Graphics g) {
            super.paint(g);

            if (loadedImage != null) {
                g.drawImage(loadedImage, 0, 0, this.getWidth(), this.getHeight(), null);
            }

            for (Shape s : shapes) {
                s.draw(g);
            }

            if (currentShape != null) {
                currentShape.draw(g);
            }
        }

        public void clear() {
            shapes.clear();
            redoStack.clear();
            repaint();
        }

        public void undo() {
            if (!shapes.isEmpty()) {
                redoStack.add(shapes.remove(shapes.size() - 1));
                repaint();
            }
        }

        public void redo() {
            if (!redoStack.isEmpty()) {
                shapes.add(redoStack.remove(redoStack.size() - 1));
                repaint();
            }
        }

        public void openImage(Component parent) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Open Image");
            if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try {
                    loadedImage = ImageIO.read(fileChooser.getSelectedFile());
                    repaint();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Failed to open image", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        public void saveImage(Component parent) {
            BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            this.paint(g2d);
            g2d.dispose();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Drawing");
            fileChooser.setSelectedFile(new File("drawing.png"));
            if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try {
                    ImageIO.write(image, "png", fileChooser.getSelectedFile());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Failed to save image", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // ================= Shapes =================
        abstract static class Shape {
            int x1, y1, x2, y2, stroke;
            Color color;

            Shape(int x1, int y1, int x2, int y2, Color color, int stroke) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
                this.color = color;
                this.stroke = stroke;
            }
            abstract void draw(Graphics g);
        }

        static class Line extends Shape {
            Line(int x1, int y1, int x2, int y2, Color color, int stroke) {
                super(x1, y1, x2, y2, color, stroke);
            }
            void draw(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(color);
                g2.setStroke(new BasicStroke(stroke));
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        static class Oval extends Shape {
            boolean fill;
            Oval(int x1, int y1, int x2, int y2, Color color, boolean fill, int stroke) {
                super(x1, y1, x2, y2, color, stroke);
                this.fill = fill;
            }
            void draw(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(color);
                g2.setStroke(new BasicStroke(stroke));
                int x = Math.min(x1, x2);
                int y = Math.min(y1, y2);
                int w = Math.abs(x2 - x1);
                int h = Math.abs(y2 - y1);
                if (fill) g2.fillOval(x, y, w, h);
                else g2.drawOval(x, y, w, h);
            }
        }

        static class Rect extends Shape {
            boolean fill;
            Rect(int x1, int y1, int x2, int y2, Color color, boolean fill, int stroke) {
                super(x1, y1, x2, y2, color, stroke);
                this.fill = fill;
            }
            void draw(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(color);
                g2.setStroke(new BasicStroke(stroke));
                int x = Math.min(x1, x2);
                int y = Math.min(y1, y2);
                int w = Math.abs(x2 - x1);
                int h = Math.abs(y2 - y1);
                if (fill) g2.fillRect(x, y, w, h);
                else g2.drawRect(x, y, w, h);
            }
        }

        static class Triangle extends Shape {
            boolean fill;
            Triangle(int x1, int y1, int x2, int y2, Color color, boolean fill, int stroke) {
                super(x1, y1, x2, y2, color, stroke);
                this.fill = fill;
            }
            void draw(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(color);
                g2.setStroke(new BasicStroke(stroke));
                int[] xPoints = {x1, x2, (x1 + x2) / 2};
                int[] yPoints = {y1, y2, y1 - Math.abs(x2 - x1)};
                if (fill) g2.fillPolygon(xPoints, yPoints, 3);
                else g2.drawPolygon(xPoints, yPoints, 3);
            }
        }
    }
}

