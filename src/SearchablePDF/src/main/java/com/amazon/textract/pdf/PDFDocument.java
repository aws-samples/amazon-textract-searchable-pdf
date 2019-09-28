package com.amazon.textract.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class PDFDocument {

    final PDFont font = PDType1Font.COURIER;

    private PDDocument document;

    public PDFDocument(){
        this.document = new PDDocument();
    }

    public PDFDocument(InputStream inputDocument) throws IOException {
        this.document = PDDocument.load(inputDocument);
    }

    public void addText(int pageIndex, List<TextLine> lines) throws IOException {
        PDPage page = document.getPage(pageIndex);

        float height = page.getMediaBox().getHeight();

        float width = page.getMediaBox().getWidth();

        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false );
        contentStream.setRenderingMode(RenderingMode.NEITHER);

        for (TextLine cline : lines){
            FontInfo fontInfo = calculateFontSize(cline.text, (float)cline.width*width, (float)cline.height*height);

            //System.out.println("FontSize: " + fontInfo.fontSize + " => for text: " + cline.text);
            contentStream.beginText();
            contentStream.setFont(this.font, fontInfo.fontSize);
            contentStream.newLineAtOffset((float)cline.left*width, (float)(height-height*cline.top-fontInfo.textHeight));
            contentStream.showText(cline.text);
            contentStream.endText();
        }

        contentStream.close();
    }

    private FontInfo calculateFontSize(String text, float bbWidth, float bbHeight) throws IOException {

        //PDFont font = PDType1Font.TIMES_ROMAN;

        int fontSize = 17;
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

        if(textWidth > bbWidth){
            while(textWidth > bbWidth){
                fontSize -= 1;
                textWidth = font.getStringWidth(text) / 1000 * fontSize;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
            }
        }
        else if(textWidth < bbWidth){
            while(textWidth < bbWidth){
                fontSize += 1;
                textWidth = font.getStringWidth(text) / 1000 * fontSize;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
            }
        }

        //System.out.println("Text height before returning font size: " + textHeight);

        FontInfo fi = new FontInfo();
        fi.fontSize = fontSize;
        fi.textHeight = textHeight;
        fi.textWidth = textWidth;

        return fi;
    }

    public void addPage(BufferedImage image, ImageType imageType, List<TextLine> lines) throws IOException {

        float width = image.getWidth();
        float height = image.getHeight();

        PDRectangle box = new PDRectangle(width, height);
        PDPage page = new PDPage(box);
        page.setMediaBox(box);
        this.document.addPage(page);

        PDImageXObject pdImage = null;

        if(imageType == ImageType.JPEG){
            pdImage = JPEGFactory.createFromImage(this.document, image);
        }
        else {
            pdImage = LosslessFactory.createFromImage(this.document, image);
        }

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.drawImage(pdImage, 0, 0);

        contentStream.setRenderingMode(RenderingMode.NEITHER);

        for (TextLine cline : lines){
            FontInfo fontInfo = calculateFontSize(cline.text, (float)cline.width*width, (float)cline.height*height);
            contentStream.beginText();
            contentStream.setFont(this.font, fontInfo.fontSize);
            contentStream.newLineAtOffset((float)cline.left*width, (float)(height-height*cline.top-fontInfo.textHeight));
            contentStream.showText(cline.text);
            contentStream.endText();
        }

        contentStream.close();
    }

    public void save(String path) throws IOException {
        this.document.save(new File(path));
    }

    public void save(OutputStream os) throws IOException {
        this.document.save(os);
    }

    public void close() throws IOException {
        this.document.close();
    }
}
