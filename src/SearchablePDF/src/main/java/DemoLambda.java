import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;

public class DemoLambda implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event event, Context ctx) {

        S3EventNotification.S3EventNotificationRecord record = event.getRecords().get(0);

        String bucketName = record.getS3().getBucket().getName();
        String keyName = record.getS3().getObject().getKey();
        String keyNameLower = record.getS3().getObject().getKey().toLowerCase();

        System.out.println("Bucket Name is " + bucketName);
        System.out.println("File Path is " + keyName);

        try {
            if (keyNameLower.endsWith("pdf")) {
                DemoPdfFromS3Pdf s3Pdf = new DemoPdfFromS3Pdf();
                s3Pdf.run(bucketName, keyName, "Output.pdf");

            } else if (keyNameLower.endsWith("jpg") || keyNameLower.endsWith("jpeg") || keyNameLower.endsWith("png")) {
                DemoPdfFromS3Image s3Image = new DemoPdfFromS3Image();
                s3Image.run(bucketName, keyName, "Output.pdf");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }
}