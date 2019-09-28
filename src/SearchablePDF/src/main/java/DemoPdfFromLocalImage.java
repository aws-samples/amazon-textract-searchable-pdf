import com.amazon.textract.pdf.ImageType;
import com.amazon.textract.pdf.PDFDocument;
import com.amazon.textract.pdf.TextLine;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import com.amazonaws.util.IOUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DemoPdfFromLocalImage {

    private static BufferedImage getImage(String documentName) throws IOException {

        BufferedImage image = null;

        try(InputStream in = new FileInputStream(documentName)) {
            image = ImageIO.read(in);
        }

        return image;
    }

    private static List<TextLine> extractText(ByteBuffer imageBytes){

        AmazonTextract client = AmazonTextractClientBuilder.defaultClient();

        DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                        .withDocument(new Document()
                                .withBytes(imageBytes));

        DetectDocumentTextResult result = client.detectDocumentText(request);

        List<TextLine> lines = new ArrayList<TextLine>();
        List<Block> blocks = result.getBlocks();
        BoundingBox boundingBox = null;
        for (Block block : blocks) {
            if ((block.getBlockType()).equals("LINE")) {
                boundingBox = block.getGeometry().getBoundingBox();
                lines.add(new TextLine(boundingBox.getLeft(),
                        boundingBox.getTop(),
                        boundingBox.getWidth(),
                        boundingBox.getHeight(),
                        block.getText()));
            }
        }

        return lines;
    }

    public static void run(String documentName, String outputDocumentName) throws IOException {

        System.out.println("Generating searchable pdf from: " + documentName);

        ImageType imageType = ImageType.JPEG;
        if(documentName.toLowerCase().endsWith(".png"))
            imageType = ImageType.PNG;

        //Get image bytes
        ByteBuffer imageBytes = null;
        try(InputStream in = new FileInputStream(documentName)) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(in));
        }

        //Extract text
        List<TextLine> lines = extractText(imageBytes);

        //Get Image
        BufferedImage image = getImage(documentName);

        //Create new pdf document
        PDFDocument pdfDocument = new PDFDocument();

        //Add page with text layer and image in the pdf document
        pdfDocument.addPage(image, imageType, lines);

        //Save PDF to local disk
        try(OutputStream outputStream = new FileOutputStream(outputDocumentName)) {
            pdfDocument.save(outputStream);
            pdfDocument.close();
        }

        System.out.println("Generated searchable pdf: " + outputDocumentName);
    }
}
