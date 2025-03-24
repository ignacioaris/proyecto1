import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LispGUI extends JFrame {
    private JTextArea codeArea;
    private JTextArea outputArea;
    private Interpreter interpreter;

    public LispGUI() {
        super("Intérprete LISP");
        interpreter = new Interpreter();
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Panel superior para el código
        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.setBorder(BorderFactory.createTitledBorder("Código LISP"));

        codeArea = new JTextArea();
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codePanel.add(codeScroll, BorderLayout.CENTER);

        add(codePanel, BorderLayout.CENTER);

        // Panel inferior para la salida
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Salida"));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        add(outputPanel, BorderLayout.SOUTH);

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        JButton runButton = new JButton("Correr");
        JButton clearButton = new JButton("Limpiar");

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCode();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputArea.setText("");
            }
        });

        buttonPanel.add(runButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.NORTH);
    }

    private void runCode() {
        String code = codeArea.getText().trim();
        if (!code.isEmpty()) {
            try {
                // Redirigir la salida estándar a nuestro outputArea
                System.setOut(new java.io.PrintStream(new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        outputArea.append(String.valueOf((char) b));
                    }
                }));

                interpreter.run(code);

                // Restaurar la salida estándar
                System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out)));
            } catch (Exception ex) {
                outputArea.append("Error: " + ex.getMessage() + "\n");
            }
            outputArea.append("\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LispGUI().setVisible(true);
            }
        });
    }
}