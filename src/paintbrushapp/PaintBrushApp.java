package paintbrushapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PaintBrushApp {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Paint Brush");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(0, 80));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton clearButton = new JButton(IconUtil.resizeIcon("images/broom.png", 24, 24));
        JButton saveButton = new JButton(IconUtil.resizeIcon("images/save.png", 24, 24));
        JButton openButton = new JButton(IconUtil.resizeIcon("images/open.png", 24, 24));
        JButton undoButton = new JButton(IconUtil.resizeIcon("images/undo.png", 24, 24));
        JButton EraserButton = new JButton(IconUtil.resizeIcon("images/eraser.png", 24, 24));
        JCheckBox fillCheckBox = new JCheckBox("Fill");
        JButton lineButton = new JButton(IconUtil.resizeIcon("images/line.png", 24, 24));
        JButton ovalButton = new JButton(IconUtil.resizeIcon("images/oval.png", 24, 24));
        JButton rectButton = new JButton(IconUtil.resizeIcon("images/rectangle (1).png", 24, 24));
        JButton freeHandButton = new JButton(IconUtil.resizeIcon("images/pencil.png", 24, 24));

        JButton blackButton = new JButton();
        JButton redButton = new JButton();
        JButton greenButton = new JButton();
        JButton blueButton = new JButton();

        blackButton.setBackground(Color.BLACK);
        redButton.setBackground(Color.RED);
        greenButton.setBackground(Color.GREEN);
        blueButton.setBackground(Color.BLUE);

        Dimension colorSize = new Dimension(50, 50);
        blackButton.setPreferredSize(colorSize);
        redButton.setPreferredSize(colorSize);
        greenButton.setPreferredSize(colorSize);
        blueButton.setPreferredSize(colorSize);

        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(EraserButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(fillCheckBox);
        buttonPanel.add(blackButton);
        buttonPanel.add(redButton);
        buttonPanel.add(greenButton);
        buttonPanel.add(blueButton);
        buttonPanel.add(lineButton);
        buttonPanel.add(ovalButton);
        buttonPanel.add(rectButton);
        buttonPanel.add(freeHandButton);

        frame.add(buttonPanel, BorderLayout.NORTH);

        DrawingPanel drawPanel = new DrawingPanel();
        drawPanel.setPreferredSize(new Dimension(1500, 850));
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        centerWrapper.setBackground(Color.lightGray);
        centerWrapper.add(drawPanel);
        frame.add(centerWrapper, BorderLayout.CENTER);

        clearButton.addActionListener(e -> drawPanel.clear());
        openButton.addActionListener(e -> drawPanel.openImage(frame));
        saveButton.addActionListener(e -> drawPanel.saveImage(frame));
        undoButton.addActionListener(e -> drawPanel.undo());
        fillCheckBox.addActionListener(e -> drawPanel.fill = fillCheckBox.isSelected());
        blackButton.addActionListener(e -> drawPanel.currentColor = Color.BLACK);
        redButton.addActionListener(e -> drawPanel.currentColor = Color.RED);
        greenButton.addActionListener(e -> drawPanel.currentColor = Color.GREEN);
        blueButton.addActionListener(e -> drawPanel.currentColor = Color.BLUE);

        lineButton.addActionListener(e -> drawPanel.setShape("line"));
        ovalButton.addActionListener(e -> drawPanel.setShape("oval"));
        rectButton.addActionListener(e -> drawPanel.setShape("rect"));
        freeHandButton.addActionListener(e -> drawPanel.setShape("free"));
        EraserButton.addActionListener(e -> {
            drawPanel.setShape("eraser");
            drawPanel.fill = fillCheckBox.isSelected();
            drawPanel.currentColor = Color.white;
        });

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
        private Shape currentShape;
        private int x1, y1;
        boolean fill = false;
        private BufferedImage loadedImage = null;
        Color currentColor = Color.BLACK;
        String currentTool = "free";

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
                        repaint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if ("free".equals(currentTool)) {
                        shapes.add(new Line(x1, y1, e.getX(), e.getY(), currentColor));
                        x1 = e.getX();
                        y1 = e.getY();
                    } else if ("eraser".equals(currentTool)) {
                        shapes.add(new Rect(e.getX(), e.getY(), e.getX() + 30, e.getY() + 30, Color.WHITE, true));
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
                case "line":
                    return new Line(x1, y1, x2, y2, currentColor);
                case "oval":
                    return new Oval(x1, y1, x2, y2, currentColor, fill);
                case "rect":
                    return new Rect(x1, y1, x2, y2, currentColor, fill);
                default:
                    return null;
            }
        }

        public void paint(Graphics g) {
            super.paint(g);

            for (Shape s : shapes) {
                s.draw(g);
            }

            if (currentShape != null) {
                currentShape.draw(g);
            }

            if (loadedImage != null) {
                g.drawImage(loadedImage, 0, 0, this.getWidth(), this.getHeight(), null);
            }
        }

        public void clear() {
            shapes.clear();
            repaint();
        }

        public void undo() {
            if (!shapes.isEmpty()) {
                shapes.remove(shapes.size() - 1);
                repaint();
            }
        }

        public void openImage(Component parent) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Open Image");
            int userSelection = fileChooser.showOpenDialog(parent);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToOpen = fileChooser.getSelectedFile();
                try {
                    loadedImage = ImageIO.read(fileToOpen);
                    repaint();
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to open image", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        public void saveImage(Component parent) {
            BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            this.paint(g2d);  // Draw the current panel contents into the image
            g2d.dispose();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Drawing");
            fileChooser.setSelectedFile(new File("drawing.png"));
            int userSelection = fileChooser.showSaveDialog(parent);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String format = "png"; // Default format
                try {
                    ImageIO.write(image, format, fileToSave);
                    JOptionPane.showMessageDialog(this, "Drawing saved to " + fileToSave.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to save image", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        abstract static class Shape {

            int x1, y1, x2, y2;
            Color color;

            Shape(int x1, int y1, int x2, int y2, Color color) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
                this.color = color;
            }

            abstract void draw(Graphics g);
        }

        static class Line extends Shape {

            Line(int x1, int y1, int x2, int y2, Color color) {
                super(x1, y1, x2, y2, color);
            }

            void draw(Graphics g) {
                g.setColor(color);
                g.drawLine(x1, y1, x2, y2);
            }
        }

        static class Oval extends Shape {

            boolean fill;

            Oval(int x1, int y1, int x2, int y2, Color color, boolean fill) {
                super(x1, y1, x2, y2, color);
                this.fill = fill;
            }

            void draw(Graphics g) {
                g.setColor(color);
                int x = Math.min(x1, x2);
                int y = Math.min(y1, y2);
                int w = Math.abs(x2 - x1);
                int h = Math.abs(y2 - y1);
                if (fill) {
                    g.fillOval(x, y, w, h);
                } else {
                    g.drawOval(x, y, w, h);
                }
            }
        }

        static class Rect extends Shape {

            boolean fill;

            Rect(int x1, int y1, int x2, int y2, Color color, boolean fill) {
                super(x1, y1, x2, y2, color);
                this.fill = fill;
            }

            void draw(Graphics g) {
                g.setColor(color);
                int x = Math.min(x1, x2);
                int y = Math.min(y1, y2);
                int w = Math.abs(x2 - x1);
                int h = Math.abs(y2 - y1);
                if (fill) {
                    g.fillRect(x, y, w, h);
                } else {
                    g.drawRect(x, y, w, h);
                }
            }
        }
    }
}
