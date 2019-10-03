import com.amazon.textract.pdf.ImageType;
import com.amazon.textract.pdf.PDFDocument;
import com.amazon.textract.pdf.TextLine;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DemoPdfFromS3Image {

    private List<TextLine> extractText(String bucketName, String documentName){
        AmazonTextract client = AmazonTextractClientBuilder.defaultClient();

        DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                .withDocument(new Document()
                        .withS3Object(new S3Object()
                                .withName(documentName)
                                .withBucket(bucketName)));

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

    private BufferedImage getImageFromS3(String bucketName, String documentName) throws IOException {

        AmazonS3 s3client = AmazonS3ClientBuilder.defaultClient();
        com.amazonaws.services.s3.model.S3Object fullObject = s3client.getObject(new GetObjectRequest(bucketName, documentName));
        BufferedImage image = ImageIO.read(fullObject.getObjectContent());
        return image;
    }

    private void UploadToS3(String bucketName, String objectName, String contentType, byte[] bytes) throws IOException {
        AmazonS3 s3client = AmazonS3ClientBuilder.defaultClient();
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(bytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectName, baInputStream, metadata);
        s3client.putObject(putRequest);
    }

    public void run(String bucketName, String documentName, String outputDocumentName) throws IOException {

        System.out.println("Generating searchable pdf from: " + bucketName + "/" + documentName);

        ImageType imageType = ImageType.JPEG;
        if(documentName.toLowerCase().endsWith(".png"))
            imageType = ImageType.PNG;

        //Extract text
        List<TextLine> lines = extractText(bucketName, documentName);

        //Get image from S3
        BufferedImage image = getImageFromS3(bucketName, documentName);

        //Create PDF document
        PDFDocument pdfDocument = new PDFDocument();

        //Add page with text layer and image in the pdf document
        pdfDocument.addPage(image, imageType, lines);

        //Save PDF to stream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        pdfDocument.save(os);
        pdfDocument.close();

        //Upload PDF to S3
        UploadToS3(bucketName, outputDocumentName, "application/pdf", os.toByteArray());

        System.out.println("Generated searchable pdf: " + bucketName + "/" + outputDocumentName);
    }
}
