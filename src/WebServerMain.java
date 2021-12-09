class WebServerMain {

    public static void main(String[] args) {
        try {
            WebServer ws = new WebServer(args[0], Integer.parseInt(args[1]));
        } catch (ArrayIndexOutOfBoundsException aob) {
            System.err.println("Usage: java WebServerMain <document_root> <port>");
        } catch (NumberFormatException nfe) {
            System.err.println("Second argument must be number\nUsage: java WebServerMain <document_root> <port>");
        }
    }
}
