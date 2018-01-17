package zomac.pointofsale;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SingleSale {

    private InputDevice barcodeScaner;
    private OutputDevice lcdDisplay;
    private OutputDevice printer;
    private Database database;
    private List<Product> items;

    public SingleSale(InputDevice barcodeScaner, OutputDevice lcdDisplay, OutputDevice printer, Database database) {
        this.barcodeScaner = barcodeScaner;
        this.lcdDisplay = lcdDisplay;
        this.printer = printer;
        this.database = database;
        this.items = new LinkedList<>();
    }

    void sale(byte[] barcode) {
        String id = barcodeScaner.read(barcode);

        if (id.isEmpty()) {
            lcdDisplay.write("Invalid bar-code");
            return;
        }

        if (id.equals("exit")) {
            BigDecimal total = new BigDecimal(0.0);

            StringBuilder builder = new StringBuilder();
            for (Product item : items) {
                total = total.add(item.getPrice());
                builder.append(toMessage(item));
                builder.append("\r\n");
            }
            builder.append(total);

            printer.write(builder.toString());
            lcdDisplay.write(total.toString());
            return;
        }

        Optional<Product> product = database.find(id);
        String message = "Product not found";
        if (product.isPresent()) {
            message = toMessage(product.get());
            items.add(product.get());
        }
        lcdDisplay.write(message);
    }

    private String toMessage(Product product) {
        return String.format("%s - %s", product.getName(), product.getPrice());
    }
}
