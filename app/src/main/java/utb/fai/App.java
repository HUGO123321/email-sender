package utb.fai;

public class App {

    public static void main(String[] args) {

        if (args.length != 6) {
            System.err.println(
                    "Usage: java -jar app.jar <host> <port> <from> <to> <subject> <text>"
            );
            return;
        }

        String host = args[0];
        int port;
        String from = args[2];
        String to = args[3];
        String subject = args[4];
        String text = args[5];

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid SMTP port: " + args[1]);
            return;
        }

        EmailSender sender = null;

        try {
            sender = new EmailSender(host, port);

            sender.send(
                    from,
                    to,
                    subject,
                    text
            );

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (sender != null) {
                sender.close();
            }
        }
    }
}