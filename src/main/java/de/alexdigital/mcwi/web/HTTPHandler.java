package de.alexdigital.mcwi.web;

import com.google.common.base.Joiner;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class HTTPHandler {

    private ChannelHandlerContext ctx;

    public HTTPHandler(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Getter
    private HTTPResult result = new HTTPResult();

    public void parseLine(String line) {
        this.result = this.parseOne(line, result);
    }


    public HTTPResult parse(String[] lines) {
        HTTPResult result = new HTTPResult();
        for (String line : lines) {
            result = this.parseOne(line, result);
        }
        return result;
    }

    private HTTPResult parseOne(String line, HTTPResult result) {
        List<String> splitted = Arrays.asList(line.split(" "));
        switch (splitted.get(0)) {
            case "GET":
            case "POST":
                result.setMethod(splitted.get(0));
                result.setPath(splitted.get(1));
                result.setHttpVersion(splitted.get(2));
                break;
            case "Host:":
                result.setHost(splitted.get(1));
                break;
            case "User-Agent:":
                result.setHost(Joiner.on(' ').join(splitted.subList(1, splitted.size() - 1)));
                break;
        }
        return result;
    }

    public void sendText(String contentType, String text) {
        byte[] bytes = text.getBytes(Charset.forName("UTF-8"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss z");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HTTP/1.1 200 OK\n");
        stringBuilder.append("Date: " + simpleDateFormat.format(Calendar.getInstance().getTime()) + "\n");
        stringBuilder.append("Server: MCWI Server - Netty 4\n");
        stringBuilder.append("Last-Modified: " + simpleDateFormat.format(Calendar.getInstance().getTime()));
        stringBuilder.append("Content-Type: " + contentType + "; charset=UTF-8\n");
        stringBuilder.append("Content-Length: " + bytes.length + "\n");
        stringBuilder.append("Accept-Ranges: bytes\n");
        stringBuilder.append("Connection: close\n\n");
        stringBuilder.append(text);

        ctx.writeAndFlush(stringBuilder.toString());
    }

    public void send404() {
        String text = "<html><head><title>Not Found</title></head><body>The item was not found.<br><b>MCWI Server - based on Netty 4</b></body></html>";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HTTP/1.1 404 Not Found\n")
                .append("Content-type: text/html\n")
                .append("Content-length: " + text.getBytes().length + "\n\n")
                .append(text);

        ctx.writeAndFlush(stringBuilder.toString());
    }

}
