public class Demo {
    public static void main(String args[]) {
        try {
            //Generate searchable PDF from local image
            DemoPdfFromLocalImage localImage = new DemoPdfFromLocalImage();
            localImage.run("./documents/SampleInput.png", "./documents/SampleOutput.pdf");

//            //Generate searchable PDF from local pdf
//            DemoPdfFromLocalPdf localPdf = new DemoPdfFromLocalPdf();
//            localPdf.run("./documents/SampleInput.pdf", "./documents/SampleOutput.pdf");
//
//            //Generate searchable PDF from image in Amazon S3 bucket
//            DemoPdfFromS3Image s3Image = new DemoPdfFromS3Image();
//            s3Image.run("ki-textract-demo-docs", "SampleInput.png", "SampleOutput.pdf");
//
//            //Generate searchable PDF from pdf in Amazon S3 bucket
//            DemoPdfFromS3Pdf s3Pdf = new DemoPdfFromS3Pdf();
//            s3Pdf.run("ki-textract-demo-docs", "SampleInput.pdf", "SampleOutput.pdf");
//
//            //Generate searchable PDF from pdf in Amazon S3 bucket
//            //(by adding text to the input pdf document)
//            DemoPdfFromS3PdfAppend s3PdfAppend = new DemoPdfFromS3PdfAppend();
//            s3PdfAppend.run("ki-textract-demo-docs", "SampleInput.pdf", "SampleOutput.pdf");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
