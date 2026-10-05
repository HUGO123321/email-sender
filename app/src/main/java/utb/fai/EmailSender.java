package utb.fai;

import java.io.*;
import java.net.*;

public class EmailSender {

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    // Konstruktor pro připojení k hostu/portu

    public EmailSender(String host, int port) throws UnknownHostException, IOException {
        socket = new Socket(host, port);

        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8")
        );

        // SMTP zpráva po připojení
        readResponse();
    }

    // Pošle email na jiný email s textem apod.

    public void send(String from, String to, String subject, String text) throws IOException {

        sendCommand("EHLO localhost", 250);

        sendCommand("MAIL FROM:<" + from + ">", 250);

        sendCommand("RCPT TO:<" + to + ">", 250);

        sendCommand("DATA", 354);

        // SMTP headery
        writer.write("From: " + from + "\r\n");
        writer.write("To: " + to + "\r\n");
        writer.write("Subject: " + subject + "\r\n");
        writer.write("\r\n");

        // CRLF convert
        String normalizedText = text
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        // SMTP dot-stuffing
        String[] lines = normalizedText.split("\n", -1);

        for (String line : lines) {
            if (line.startsWith(".")) {
                writer.write(".");
            }

            writer.write(line);
            writer.write("\r\n");
        }

        // SMTP DATA end.
        writer.write(".\r\n");
        writer.flush();

        readResponseExpected(250);
    }

    // Poslání QUIT pro zavření socketu
    public void close() {
        try {
            if (!socket.isClosed()) {
                writer.write("QUIT\r\n");
                writer.flush();

                readResponse();
                socket.close();
            }
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void sendCommand(String command, int expectedCode) throws IOException {
        writer.write(command);
        writer.write("\r\n");
        writer.flush();

        readResponseExpected(expectedCode);
    }

    private void readResponseExpected(int expectedCode) throws IOException {
        int code = readResponse();

        if (code != expectedCode) {
            throw new IOException(
                    "SMTP server returned unexpected response code: " + code
            );
        }
    }

    private int readResponse() throws IOException {
        String line = reader.readLine();

        if (line == null) {
            throw new IOException("SMTP server closed the connection.");
        }

        if (line.length() < 3) {
            throw new IOException("Invalid SMTP response: " + line);
        }

        int code;

        try {
            code = Integer.parseInt(line.substring(0, 3));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid SMTP response: " + line, e);
        }

        while (line.length() >= 4 && line.charAt(3) == '-') {
            line = reader.readLine();

            if (line == null) {
                throw new IOException("SMTP server closed the connection.");
            }
        }

        return code;
    }
}