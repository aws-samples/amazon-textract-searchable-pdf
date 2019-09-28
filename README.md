## Generate Searchable PDF documents with Amazon Textract

This repository contains sample library and code examples showing how Amazon Textract can be used to extract text from documents and generate searchable pdf documents.

## How is searchable PDF generated

To generate a searchable PDF, we use [Amazon Textract](https://aws.amazon.com/textract/) to extract text from documents and then add extracted text as a layer to the image in the PDF document. Amazon Textract detect and analyze text input documents and returns information about detected items such as pages, words, lines, form data (key-value pairs), tables, selection elements etc. It also provides bounding box information which is an axis-aligned coarse representation of the location of the recognized item on the document page. We use detected text and its bounding box information to appropriately place text in the pdf page.

[SampleInput.pdf](./src/SearchablePDF/documents/SampleInput.pdf) is an example input document where text is locked inside the image. [SampleOutput.pdf](./src/SearchablePDF/documents/SampleOutput.pdf) is an example of a searchable pdf document where you can select and copy text and search within the document.

[PDFDocument](./src/SearchablePDF/src/main/java/com/amazon/textract/pdf/PDFDocument.java) library wraps all the necessary logic to generate searchable PDF document using output from Amazon Textract. It also uses open source Java library [Apache PDFBox](https://pdfbox.apache.org/) to create the PDF document but there similar pdf processing libraries available in other programming languages.

```
    ...
    
    //Extract text using Amazon Textract
    List<TextLine> lines = extractText(imageBytes);
        
    //Create new pdf document
    PDFDocument pdfDocument = new PDFDocument();

    //Add page with text layer and image in the pdf document
    pdfDocument.addPage(image, imageType, lines);
    
    //Save PDF to local disk
    try(OutputStream outputStream = new FileOutputStream(outputDocumentName)) {
        pdfDocument.save(outputStream);
        pdfDocument.close();
    }
```

## Code examples
[Sample project](./src/SearchablePDF.zip) has five different examples:

- [Create searchable PDF from image on local drive](./src/SearchablePDF/src/main/java/DemoPdfFromLocalImage.java)
- [Create searchable PDF from pdf on local drive](./src/SearchablePDF/src/main/java/DemoPdfFromLocalPdf.java)
- [Create searchable PDF from image in Amazon S3 bucket](./src/SearchablePDF/src/main/java/DemoPdfFromS3Image.java)
- [Create searchable PDF from pdf in Amazon S3 bucket](./src/SearchablePDF/src/main/java/DemoPdfFromS3Pdf.java)
- [Create searchable PDF from pdf in Amazon S3 bucket - by appending input document](./src/SearchablePDF/src/main/java/DemoPdfFromS3PdfAppend.java)

## Run code examples on local machine

1. Setup AWS Account and AWS CLI using [getting started with Amazon Textract](https://docs.aws.amazon.com/textract/latest/dg/getting-started.html).
2. Download and unzip the [sample project](./src/SearchablePDF.zip).
3. In the project directory run "mvn package". Install [Apache Maven](https://maven.apache.org/index.html) if it is not already installed.
4. Run: "java -cp target/searchable-pdf-1.0.jar Demo" to run Java project with [Demo](./src/SearchablePDF/src/main/java/Demo.java) as main class.
5. By default only first example to create searchable PDF from image on local drive is enabled. Uncomment relevant lines in [Demo](./src/SearchablePDF/src/main/java/Demo.java) to run other examples.

## Run code examples in AWS Lambda

1. Download and unzip the [sample project](./src/SearchablePDF.zip).
2. In the project directory run "mvn package". Install [Apache Maven](https://maven.apache.org/index.html) if it is not already installed.
3. The build creates .jar in project-dir/target/searchable-pdf1.0.jar, using information in the pom.xml to do the necessary transforms. This is a standalone .jar (.zip file) that includes all the dependencies. This is your [deployment package](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java-how-to-create-deployment-package.html) that you can upload to AWS Lambda to create a Lambda function.  [DemoLambda](https://github.com/darwaishx/textract-searchablepdf/blob/master/src/SearchablePDF/src/main/java/DemoLambda.java) has all the necessary code to read S3 events and take action based on the type of input document.
4. Create an AWS Lambda with Java 8 and IAM role that has read and write permissions to S3 bucket you created earlier. IAM role should also have permissions to call Amazon Textract.
5. Set handler to "DemoLambda::handleRequest", increase timeout to 15 minutes and upload jar file you build earlier.
6. Create an Amazon S3 bucket.
7. Create a folder “documents” in Amazon S3 bucket.
8. Add a trigger in the Lambda function such that when an object is uploaded to the folder “documents” in your Amazon S3 bucket, Lambda function gets executed. Make sure that you set trigger for “documents” folder. If you add trigger for the whole bucket then Lambda will trigger every time an output pdf document is generated resulting in cycle.
9. Upload an image (jpeg, png) or pdf document to documents folder in your Amazon S3 bucket.
10. In few seconds you should see searchable pdf document generated in the S3 bucket.
11. These steps show simple Amazon S3 and Lambda integration. In production you should consider [scalable architecture similar to this reference architecture](https://github.com/aws-samples/amazon-textract-serverless-large-scale-document-processing).

## Cost
- As you run these samples they call different Amazon Textract APIs in your AWS account. You will get charged for all the API calls made as part of the analysis.

## Other Resources

- [Large scale document processing with Amazon Textract - Reference Architecture](https://github.com/aws-samples/amazon-textract-serverless-large-scale-document-processing)
- [Amazon Textract code samples](https://github.com/aws-samples/amazon-textract-code-samples)
- [Batch processing tool](https://github.com/aws-samples/amazon-textract-textractor)
- [JSON response parser](https://github.com/aws-samples/amazon-textract-response-parser)

## License

This library is licensed under the MIT-0 License. See the LICENSE file.

