package jetuml.export;

import jetuml.app.DiagramDocument;
import jetuml.app.NotificationService;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;

public final class ClipboardImageService {
    private ClipboardImageService() {
    }

    public static void copy(DiagramDocument document, NotificationService notifications) {
        BufferedImage image = RasterImageExporter.render(document);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(image), null);
        notifications.info("Diagram copied to the clipboard.");
    }

    private record ImageSelection(Image image) implements Transferable {
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}
