package com.amazon.textract.pdf;

public class TextLine {
    public double left;
    public double top;
    public double width;
    public double height;
    public String text;

    public TextLine(double left, double top, double width, double height, String text) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.text = text;
    }
}