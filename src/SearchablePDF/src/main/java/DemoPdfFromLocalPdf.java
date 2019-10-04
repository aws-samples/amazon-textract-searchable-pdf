import com.amazon.textract.pdf.ImageType;
import com.amazon.textract.pdf.PDFDocument;
import com.amazon.textract.pdf.TextLine;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DemoPdfFromLocalPdf {

    private List<TextLine> extractText(ByteBuffer imageBytes){

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

    public void run(String documentName, String outputDocumentName) throws IOException {

        System.out.println("Generating searchable pdf from: " + documentName);

        PDFDocument pdfDocument = new PDFDocument();

        List<TextLine> lines = null;
        BufferedImage image = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ByteBuffer imageBytes = null;

        //Load pdf document and process each page as image
        PDDocument inputDocument = PDDocument.load(new File(documentName));
        PDFRenderer pdfRenderer = new PDFRenderer(inputDocument);
        for (int page = 0; page < inputDocument.getNumberOfPages(); ++page) {

            //Render image
            image = pdfRenderer.renderImageWithDPI(page, 300, org.apache.pdfbox.rendering.ImageType.RGB);

            //Get image bytes
            byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIOUtil.writeImage(image, "jpeg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            imageBytes = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

            //Extract text
            lines = extractText(imageBytes);

            //Add extracted text to pdf page
            pdfDocument.addPage(image, ImageType.JPEG, lines);

            System.out.println("Processed page index: " + page);
        }

        inputDocument.close();

        //Save PDF to local disk
        try (OutputStream outputStream = new FileOutputStream(outputDocumentName)) {
            pdfDocument.save(outputStream);
            pdfDocument.close();
        }

        System.out.println("Generated searchable pdf: " + outputDocumentName);
    }
}
