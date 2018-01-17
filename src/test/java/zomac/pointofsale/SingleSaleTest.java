package zomac.pointofsale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SingleSaleTest {

    @Mock
    private InputDevice barcodeScaner;
    @Mock
    private OutputDevice lcdDisplay;
    @Mock
    private OutputDevice printer;
    @Mock
    private Database database;
    @InjectMocks
    private SingleSale singleSale = new SingleSale(barcodeScaner, lcdDisplay, printer, database);

    @Test
    public void whenProductFoundThenPrintLcd() throws Exception {
        byte[] barcode = new byte[0];
        String message = "product - 10.5";

        when(barcodeScaner.read(any(byte[].class))).thenReturn("#");
        when(database.find(anyString()))
                .thenReturn(Optional.ofNullable(new Product("product", new BigDecimal(10.5))));

        singleSale.sale(barcode);

        verify(barcodeScaner, times(1)).read(any(byte[].class));
        verify(database, times(1)).find(anyString());
        verify(lcdDisplay, times(1)).write(message);
    }

    @Test
    public void whenProductNotFoundThenPrintLcdNotFound() throws Exception {
        byte[] barcode = new byte[0];

        when(barcodeScaner.read(any(byte[].class))).thenReturn("#");
        when(database.find(anyString())).thenReturn(Optional.ofNullable(null));

        singleSale.sale(barcode);

        verify(barcodeScaner, times(1)).read(any(byte[].class));
        verify(database, times(1)).find(anyString());
        verify(lcdDisplay, times(1)).write("Product not found");
    }

    @Test
    public void whenBarcodeEmptyThenPrintLcdInvalid() throws Exception {
        byte[] barcode = new byte[0];

        when(barcodeScaner.read(any(byte[].class))).thenReturn("");

        singleSale.sale(barcode);

        verify(barcodeScaner, times(1)).read(any(byte[].class));
        verify(lcdDisplay, times(1)).write("Invalid bar-code");
    }

    @Test
    public void whenExitThenPrintLcdTotalAndPrintPrinterProducts() throws Exception {
        byte[] barcode = new byte[0];
        String printerMessage = "product1 - 10.5\r\nproduct2 - 14.5\r\n25.0";
        String lcdMessage = "25.0";

        when(barcodeScaner.read(any(byte[].class)))
                .thenReturn("#1")
                .thenReturn("#2")
                .thenReturn("exit");
        when(database.find(anyString()))
                .thenReturn(Optional.ofNullable(new Product("product1", new BigDecimal(10.5))))
                .thenReturn(Optional.ofNullable(new Product("product2", new BigDecimal(14.5))));

        singleSale.sale(barcode);
        singleSale.sale(barcode);
        singleSale.sale(barcode);

        verify(barcodeScaner, times(3)).read(any(byte[].class));
        verify(database, times(2)).find(anyString());
        verify(printer, times(1)).write(printerMessage);
        verify(lcdDisplay, times(1)).write(lcdMessage);
    }
}